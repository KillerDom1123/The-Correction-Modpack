// priority: 90
// Old Civilisation — quest checks the quest book can't do with its own task types.
//
// FTB Quests "stage" tasks read vanilla player tags here: no stage mod is installed, so FTB Library falls back to its
// EntityTagStageProvider (Player#getTags / addTag). This script adds the tag when the player has really done the thing;
// the quest's stage task completes on its next check (every few seconds). `/tag <player> list` shows them.
//
//   oc_q_first_night    The Broken World / "The First Night": a whole Overworld night survived, under a roof for at
//                       least 75% of it (or slept through in bed).
//   oc_q_silent_nights  The Broken World / "The Silent Nights": 5 whole Overworld nights at least 256 blocks from world
//                       spawn, without sleeping. Not necessarily in a row.
//   oc_q_watchers       Field Reports / "The Watchers": 10 whole Overworld nights away from home: at least 256 blocks
//                       from world spawn and from your bed/respawn point, and never inside a MineColonies colony.
//   oc_q_power          The Machines That Remain / "The Power Question": any advancement in POWER_ADVANCEMENTS.
//   oc_q_cast_spell     The Other Science / "A Different Kind of Power": set by startup_scripts/oc_quest_spellcast.js
//                       when the player casts an Ars Nouveau spell.
// Mirrored from state other scripts keep (qcMirror):
//   oc_q_evidence_tech / _magic / _space   The Final Question / "The Answer": the oc_interactions.js path flags, the
//                       same three the Correction Fragment grant waits for.
//   oc_q_discoveries_4 / _10 / _12 / _16   distinct lore items held (oc_discoveries): Recovered Evidence milestones,
//                       and 12 for "The Answer" (the grant's threshold).
//   oc_q_contact        Field Reports / "Unidentified Contact": has held a Biological Sample or an Impossible Bone.
//   oc_q_breakdown      Field Reports / "Breakdown" (hidden): sanity has reached the Breakdown band (oc_sanity.js).
//
// "Whole night": seen at or before 14000 and at or after 22000 (dayTime of day; night is 13000-23000), alive throughout
// (a death that night voids it), in the Overworld at every check. Nights in other dimensions don't count.
// Per-night state lives in player.persistentData (oc_qn_*), the night counters in oc_q_nights_silent / oc_q_nights_watch.
//
// Rhino note (see oc_sanity.js): every variable inside a repeatedly-invoked callback is `var`, never let/const.

const QC = {
  interval: 40,           // ticks between checks per player (2 s)
  nightFrom: 13000,       // dayTime % 24000
  nightTo: 23000,
  wholeFrom: 14000,       // a night counts only if first seen at or before this...
  wholeTo: 22000,         // ...and last seen at or after this
  awayBlocks: 256,
  shelterShare: 0.75,
  silentNights: 5,
  watcherNights: 10,
  discoveries: [4, 10, 12, 16],   // distinct lore items held (oc_interactions.js oc_discoveries)
}
const POWER_ADVANCEMENTS = [
  'create:water_wheel', 'create:windmill', 'create:steam_engine',            // generating torque
  'immersiveengineering:main/place_windmill', 'immersiveengineering:main/place_dynamo',
  'immersiveengineering:multiblocks/mb_dieselgen',
  'pneumaticcraft:air_compressor', 'pneumaticcraft:liquid_compressor',
]
const QC_LOG = '[OldCiv/Quests] '
var oc_qc_warned = {}
function qcWarn(site, e) {
  if (oc_qc_warned[site]) return
  oc_qc_warned[site] = 1
  console.warn(QC_LOG + 'degraded at [' + site + ']: ' + e + ' (further identical messages suppressed)')
}

// MineColonies' colony check, if its API loads; otherwise "away from home" is distance only.
var oc_qc_colonies = null
try {
  oc_qc_colonies = Java.loadClass('com.minecolonies.api.colony.IColonyManager')
} catch (e) { qcWarn('minecolonies-api', e) }

function qcHasTag(p, tag) { return p.getTags().contains(tag) }

function qcGrant(p, tag, line) {
  if (qcHasTag(p, tag)) return
  p.addTag(tag)
  if (line) p.tell(Text.of(line).gray().italic(true))
  console.info(QC_LOG + p.username + ' -> ' + tag)
}

function qcDist2(p, x, z) { var dx = p.x - x; var dz = p.z - z; return dx * dx + dz * dz }

// Same test as oc_sanity.js: works whether KubeJS gives a ResourceKey or a ResourceLocation.
function qcIsOverworld(dim) { return ('' + dim).indexOf('overworld') >= 0 }

function qcInColony(p) {
  if (!oc_qc_colonies) return false
  try { return oc_qc_colonies.getInstance().isCoordinateInAnyColony(p.level, p.blockPosition()) }
  catch (e) { qcWarn('colony-check', e); return false }
}

function qcClearNight(pd) {
  pd.remove('oc_qn_day'); pd.remove('oc_qn_first'); pd.remove('oc_qn_last'); pd.remove('oc_qn_checks')
  pd.remove('oc_qn_shelter'); pd.remove('oc_qn_slept'); pd.remove('oc_qn_spawn'); pd.remove('oc_qn_home')
  pd.remove('oc_qn_bad')
}

// The tracked night is over: score it once.
function qcFinishNight(p, pd) {
  var checks = pd.getInt('oc_qn_checks')
  if (checks <= 0 || pd.getBoolean('oc_qn_bad')) return
  var slept = pd.getBoolean('oc_qn_slept')
  var whole = pd.getInt('oc_qn_first') <= QC.wholeFrom && pd.getInt('oc_qn_last') >= QC.wholeTo
  if ((whole || slept) && pd.getInt('oc_qn_shelter') >= QC.shelterShare * checks) {
    qcGrant(p, 'oc_q_first_night', 'Morning. The walls held.')
  }
  if (!whole) return
  if (!slept && pd.getBoolean('oc_qn_spawn') && !qcHasTag(p, 'oc_q_silent_nights')) {
    var s = pd.getInt('oc_q_nights_silent') + 1
    pd.putInt('oc_q_nights_silent', s)
    if (s >= QC.silentNights) qcGrant(p, 'oc_q_silent_nights', 'Five nights awake, far from spawn. You heard them too.')
    else p.tell(Text.of('Night ' + s + ' of ' + QC.silentNights + ' awake, far from spawn.').darkGray().italic(true))
  }
  if (pd.getBoolean('oc_qn_home') && !qcHasTag(p, 'oc_q_watchers')) {
    var w = pd.getInt('oc_q_nights_watch') + 1
    pd.putInt('oc_q_nights_watch', w)
    if (w >= QC.watcherNights) qcGrant(p, 'oc_q_watchers', 'Ten nights away from home. Something has been keeping count too.')
    else p.tell(Text.of('Night ' + w + ' of ' + QC.watcherNights + ' away from home.').darkGray().italic(true))
  }
}

// Mirror state other scripts already keep (oc_interactions.js, oc_sanity.js) into tags the quest book can check.
function qcMirror(p, pd) {
  if (pd.getBoolean('oc_flag_tech')) qcGrant(p, 'oc_q_evidence_tech', null)
  if (pd.getBoolean('oc_flag_magic')) qcGrant(p, 'oc_q_evidence_magic', null)
  if (pd.getBoolean('oc_flag_space')) qcGrant(p, 'oc_q_evidence_space', null)
  var d = pd.getInt('oc_discoveries')
  for (var i = 0; i < QC.discoveries.length; i++) {
    if (d >= QC.discoveries[i]) qcGrant(p, 'oc_q_discoveries_' + QC.discoveries[i], null)
  }
  var seen = pd.getString('oc_seen') || ''
  if (seen.indexOf('[oldcivilisation:biological_sample]') >= 0 || seen.indexOf('[oldcivilisation:impossible_bone]') >= 0) {
    qcGrant(p, 'oc_q_contact', null)
  }
  if (pd.getInt('oc_sanity_band') >= 3) qcGrant(p, 'oc_q_breakdown', null)   // 3 = Breakdown (oc_sanity.js BAND_NAMES)
}

function qcCheckPower(p) {
  if (qcHasTag(p, 'oc_q_power')) return
  for (var i = 0; i < POWER_ADVANCEMENTS.length; i++) {
    if (p.isAdvancementDone(POWER_ADVANCEMENTS[i])) { qcGrant(p, 'oc_q_power', null); return }
  }
}

PlayerEvents.tick(event => {
  var p = event.player
  if (!p || p.age % QC.interval !== 0) return
  try {
    if (p.isSpectator() || p.isCreative()) return
    var pd = p.persistentData
    var dt = Number(p.server.overworld().getDayTime())
    var day = Math.floor(dt / 24000)
    var tod = dt % 24000
    var night = tod >= QC.nightFrom && tod < QC.nightTo
    var tracked = pd.contains('oc_qn_day') ? pd.getInt('oc_qn_day') : -1

    if (tracked >= 0 && (!night || tracked !== day)) {   // the tracked night ended (or is from another day)
      if (tracked === day || tracked === day - 1) qcFinishNight(p, pd)
      qcClearNight(pd)
      tracked = -1
    }
    if (night) {
      if (tracked < 0) {
        pd.putInt('oc_qn_day', day); pd.putInt('oc_qn_first', tod); pd.putInt('oc_qn_checks', 0)
        pd.putInt('oc_qn_shelter', 0); pd.putBoolean('oc_qn_slept', false)
        pd.putBoolean('oc_qn_spawn', true); pd.putBoolean('oc_qn_home', true); pd.putBoolean('oc_qn_bad', false)
      }
      pd.putInt('oc_qn_last', tod)
      if (!qcIsOverworld(p.level.dimension)) {
        pd.putBoolean('oc_qn_bad', true)
      } else {
        pd.putInt('oc_qn_checks', pd.getInt('oc_qn_checks') + 1)
        if (!p.block.canSeeSky) pd.putInt('oc_qn_shelter', pd.getInt('oc_qn_shelter') + 1)
        if (p.isSleeping()) pd.putBoolean('oc_qn_slept', true)
        var sp = p.server.overworld().getSharedSpawnPos()
        var far = QC.awayBlocks * QC.awayBlocks
        var awaySpawn = qcDist2(p, sp.x, sp.z) >= far
        if (!awaySpawn) { pd.putBoolean('oc_qn_spawn', false); pd.putBoolean('oc_qn_home', false) }
        else if (pd.getBoolean('oc_qn_home')) {
          var bed = p.getRespawnPosition()
          var bedHere = bed && qcIsOverworld(p.getRespawnDimension())
          if ((bedHere && qcDist2(p, bed.x, bed.z) < far) || qcInColony(p)) pd.putBoolean('oc_qn_home', false)
        }
      }
    }
    qcMirror(p, pd)
    if (p.age % (QC.interval * 30) === 0) qcCheckPower(p)   // backfill every minute (cheap: 8 lookups)
  } catch (e) { qcWarn('tick', e) }
})

EntityEvents.death(event => {
  try {
    var e = event.entity
    if (e && e.isPlayer() && e.persistentData.contains('oc_qn_day')) e.persistentData.putBoolean('oc_qn_bad', true)
  } catch (err) { qcWarn('death', err) }
})

PlayerEvents.advancement(event => {
  try {
    var id = String(event.advancement.id())
    if (POWER_ADVANCEMENTS.indexOf(id) >= 0) qcGrant(event.player, 'oc_q_power', null)
  } catch (e) { qcWarn('advancement', e) }
})

PlayerEvents.loggedIn(event => {
  try { qcCheckPower(event.player) } catch (e) { qcWarn('login', e) }
})
