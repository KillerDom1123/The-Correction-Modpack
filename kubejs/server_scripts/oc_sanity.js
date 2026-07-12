// priority: 100
// Old Civilisation — Sanity mechanic.  Spec: docs/sanity_mechanic.md
//
// Invisible, per-player sanity (0..100, baseline 100) held in player.persistentData.
// Two consequence channels, both keyed off the band model:
//   A) Ambient events  — sounds/effects/particles that intensify as sanity falls (0 at full).
//   B) Spawn suppression — high sanity cancels natural horror spawns via checkSpawn;
//      at 0 sanity the pack's tuned base rate passes through untouched. SUPPRESSION-ONLY:
//      base rate is the ceiling, never exceeded (it can only ever make the world safer).
//
// Nothing here spawns an entity. Channel B only ever *vetoes* a spawn the game already tried.
//
// Rhino note: every var inside a repeatedly-invoked callback is declared `var` (never let/const)
// to avoid the block-scope mis-hoist that once silently killed oc_interactions.js.
// Logging: everything is [OldCiv/Sanity]-prefixed; failures log ONCE then suppress (warnOnce);
// verbose tracing is gated behind CONFIG.debug and throttled/sampled on hot paths (see §15).

// ============================================================================
// CONFIG — every lever for the feel of the mechanic lives here (docs §13)
// ============================================================================
const CONFIG = {
  interval: 40,            // ticks between evaluations (2s) — the throttle
  baseline: 100.0,
  drain:   { dark: 0.6, deepMin: 0.2, deepMax: 0.6, deepStartY: 0, deepFullY: -40,
             horror: 1.2, horrorRadius: 16, selfEvent: 2.0 },
  restore: { firelight: 0.5, daylight: 0.8, sleep: 8.0, clarityItem: 30.0 },
  light:   { darkBelow: 4, litAtOrAbove: 10 },
  bands:   { unease: 70, dread: 40, breakdown: 15, hysteresis: 5 },
  cooldown:{ unease: 12000, dread: 6000, breakdown: 2400, jitter: 0.25 }, // ticks
  spawn:   { enabled: true, gamma: 1.0, naturalOnly: true, noPlayerSanity: 0 },
  bandLines: true,
  affectPeaceful: true,
  debug: true,   // DIAG: temporarily ON for troubleshooting — set back to false once confirmed
}

// Horror entity ids — used for the proximity drain AND Channel B suppression.
// Mirrors config/openloader/data/OldCivSanity/.../entity_types/horror_mobs.json.
// (Timer-summoned horrors — mimic/cave_dweller/man — never trigger checkSpawn, so listing
//  them here only affects the proximity drain, which is intended.)
const HORROR_IDS = [
  'rake_the_arrival:rake_stalking', 'rake_the_arrival:rake_following', 'rake_the_arrival:rake_spawn_trigger',
  'the_one_who_watches:toww_staring', 'the_one_who_watches:toww_stalking',
  'whispering_spirits:whispering_spirit', 'antlers:wendigo', 'weeping_angels:weeping_angel',
  'themimic_er:mimicer', 'cave_dweller:cave_dweller',
  'man:manfromthefog', 'man:managgresive', 'pale_hound:pale_hound',
]
const HORROR_ID_SET = {}
HORROR_IDS.forEach(function (id) { HORROR_ID_SET[id] = true })

const BAND_NAMES = ['Calm', 'Unease', 'Dread', 'Breakdown']
const BAND_LINES = {
  1: 'Something about this place sits wrong.',
  2: 'Your thoughts feel heavier down here.',
  3: 'You can\'t tell if the sound is outside your head.',
}
const COOLDOWNS = { 1: CONFIG.cooldown.unease, 2: CONFIG.cooldown.dread, 3: CONFIG.cooldown.breakdown }

// ============================================================================
// Logging helpers (docs §15)
// ============================================================================
const LOG = '[OldCiv/Sanity] '
var oc_sn_warned = {}          // site -> count; first logs, rest suppressed (anti-167x-spam)
var oc_sn_sampleCtr = 0        // hot-path debug sampler
var oc_sn_hb = 0               // DIAG heartbeat counter (real ticks, independent of player.age)
function warnOnce(site, e) {
  if (oc_sn_warned[site]) { oc_sn_warned[site]++; return }
  oc_sn_warned[site] = 1
  console.warn(LOG + 'degraded at [' + site + ']: ' + e + ' (further identical messages suppressed)')
}
function dlog(msg) { if (CONFIG.debug) console.info(LOG + msg) }
function dlogSampled(msg) { if (!CONFIG.debug) return; oc_sn_sampleCtr++; if (oc_sn_sampleCtr % 20 === 0) console.info(LOG + '(sampled) ' + msg) }

// graceful-degradation flags (a failing accessor disables only its own sub-feature)
var oc_sn_lightOk = true
var oc_sn_dayOk = true
var oc_sn_sleepOk = true
var oc_sn_channelB = false

// ============================================================================
// small utils
// ============================================================================
function clamp(v, lo, hi) { return v < lo ? lo : (v > hi ? hi : v) }
function round1(v) { return Math.round(v * 10) / 10 }
function round2(v) { return Math.round(v * 100) / 100 }
function gameTime(p) { try { return Number(p.server.overworld().getGameTime()) } catch (e) { return Number(p.age) } }

function getSanity(player) {
  try {
    var pd = player.persistentData
    if (!pd.getBoolean('oc_sanity_init')) return CONFIG.baseline
    return pd.getDouble('oc_sanity')
  } catch (e) { return CONFIG.baseline }
}
function ensureInit(pd) {
  if (!pd.getBoolean('oc_sanity_init')) {
    pd.putDouble('oc_sanity', CONFIG.baseline)
    pd.putBoolean('oc_sanity_init', true)
    pd.putInt('oc_sanity_band', 0)
  }
}
function restoreSanity(p, amt) {
  var pd = p.persistentData
  ensureInit(pd)
  var s = clamp(pd.getDouble('oc_sanity') + amt, 0, 100)
  pd.putDouble('oc_sanity', s)
  dlog('player ' + p.username + ': +' + round1(amt) + ' -> ' + round1(s))
  return s
}

// band from sanity, with hysteresis on *improvement* (must clear threshold + H to climb back up)
function computeBand(sanity, prevBand) {
  var raw
  if (sanity > CONFIG.bands.unease) raw = 0
  else if (sanity > CONFIG.bands.dread) raw = 1
  else if (sanity > CONFIG.bands.breakdown) raw = 2
  else raw = 3
  if (raw < prevBand) {
    var h = CONFIG.bands.hysteresis
    if (prevBand === 3 && sanity < CONFIG.bands.breakdown + h) return 3
    if (prevBand === 2 && sanity < CONFIG.bands.dread + h) return 2
    if (prevBand === 1 && sanity < CONFIG.bands.unease + h) return 1
  }
  return raw
}

// ---- environment reads (each degrades independently) ----
function readLight(player) { return player.block.getLight() } // effective/local brightness [verify live]
function isOverworld(player) {
  try { return ('' + player.level.dimension).indexOf('overworld') >= 0 }
  catch (e) { try { return ('' + player.level.dimensionKey).indexOf('overworld') >= 0 } catch (e2) { return true } }
}
function isDaylightSafe(player) {
  if (!player.block.canSeeSky) return false // KubeJS exposes getCanSeeSky() as a boolean property, not a method
  var t = Number(player.server.overworld().getDayTime()) % 24000
  return t < 12000
}
function countHorrorsNear(player, r) {
  var out = player.runCommandSilent('execute if entity @e[type=#oldcivilisation:horror_mobs,distance=..' + r + ']')
  return Number(out) || 0
}

// ============================================================================
// Channel A — ambient event pools (sounds / effects only; never entities)
// ============================================================================
function cmd(p, c) { try { p.runCommandSilent(c) } catch (e) { warnOnce('event-cmd', e) } }
function soundSelf(p, id, v, pitch) { cmd(p, 'playsound ' + id + ' master @s ~ ~ ~ ' + v + ' ' + pitch) }
// caret coords are relative to the executor's facing, so ^ ^ ^-d is 'd blocks behind the player'
function soundBehind(p, id, v, pitch, d) { cmd(p, 'execute positioned ^ ^ ^-' + d + ' run playsound ' + id + ' master @s ~ ~ ~ ' + v + ' ' + pitch) }
function effectSelf(p, id, secs, amp) { cmd(p, 'effect give @s ' + id + ' ' + secs + ' ' + (amp || 0) + ' true') } // 'true' = hidden particles/icon

const POOLS = {
  1: [ // Unease
    function (p) { soundSelf(p, 'minecraft:ambient.cave', 0.4, 1.0) },
    function (p) { soundSelf(p, 'minecraft:block.sculk_sensor.clicking', 0.5, 0.8) },
    function (p) { soundBehind(p, 'minecraft:block.wood.step', 0.7, 0.9, 2) },
  ],
  2: [ // Dread
    function (p) { soundBehind(p, 'minecraft:block.gravel.step', 0.8, 1.0, 3); soundBehind(p, 'minecraft:block.gravel.step', 0.8, 1.0, 2) },
    function (p) { soundBehind(p, 'minecraft:entity.warden.nearby_closest', 0.6, 1.0, 3) },
    function (p) { effectSelf(p, 'minecraft:nausea', 3, 0); soundSelf(p, 'minecraft:ambient.cave', 0.5, 0.8) },
    function (p) { effectSelf(p, 'minecraft:darkness', 2, 0) },
    function (p) { soundSelf(p, 'minecraft:ambient.crimson_forest.mood', 0.6, 0.8) },
  ],
  3: [ // Breakdown
    function (p) { effectSelf(p, 'minecraft:darkness', 5, 0); soundSelf(p, 'minecraft:entity.warden.heartbeat', 0.8, 1.0) },
    function (p) { effectSelf(p, 'minecraft:nausea', 8, 0) },
    function (p) { soundBehind(p, 'minecraft:entity.enderman.stare', 0.9, 1.0, 2); soundSelf(p, 'minecraft:entity.enderman.stare', 0.7, 1.0) },
    function (p) { soundSelf(p, 'minecraft:entity.creeper.primed', 0.8, 1.0) }, // hallucinated fuse — nothing there
    function (p) { soundBehind(p, 'minecraft:entity.warden.nearby_closest', 1.0, 0.7, 3); effectSelf(p, 'minecraft:nausea', 4, 0) },
  ],
}
function fireEvent(p, band) {
  var pool = POOLS[band]
  if (!pool || !pool.length) return
  var f = pool[Math.floor(Math.random() * pool.length)]
  try { f(p) } catch (e) { warnOnce('fireEvent', e) }
  dlog('player ' + p.username + ': Channel A event (band ' + BAND_NAMES[band] + ')')
}

// ============================================================================
// Main tick loop — one handler, throttled to CONFIG.interval (docs §4)
// ============================================================================
PlayerEvents.tick(event => {
  var player = event.player
  if (!player) return

  // --- DIAG heartbeat (temporary): fires every ~3s INDEPENDENTLY of the age throttle, so we can
  //     see whether the throttle ever passes and what game-mode the player is in. A creative or
  //     spectator player is skipped entirely below — this line reveals that. Rate-limited by a
  //     real-tick counter (oc_sn_hb), NOT by player.age, so it prints even if the throttle never does.
  if (CONFIG.debug) {
    oc_sn_hb++
    if (oc_sn_hb % 60 === 0) {
      try {
        console.info(LOG + 'DIAG hb ' + player.username + ': age=' + player.age + ' (' + (typeof player.age) +
          ') age%' + CONFIG.interval + '=' + (player.age % CONFIG.interval) +
          ' throttlePass=' + (player.age % CONFIG.interval === 0) +
          ' creative=' + player.isCreative() + ' spectator=' + player.isSpectator())
      } catch (ed) { console.warn(LOG + 'DIAG hb err: ' + ed) }
    }
  }

  if (player.age % CONFIG.interval !== 0) return
  try {
    if (player.isCreative() || player.isSpectator()) { dlog('skip ' + player.username + ': creative/spectator'); return }
    var pd = player.persistentData
    ensureInit(pd)
    var sanity = pd.getDouble('oc_sanity')
    var delta = 0
    var light = -1
    var hc = 0
    var ow = isOverworld(player)
    var y = player.y

    // light: darkness drains, firelight restores
    if (oc_sn_lightOk) {
      try { light = readLight(player) } catch (e) { oc_sn_lightOk = false; warnOnce('getLight', e) }
      if (light >= 0) {
        if (light < CONFIG.light.darkBelow) delta -= CONFIG.drain.dark
        else if (light >= CONFIG.light.litAtOrAbove) delta += CONFIG.restore.firelight
      }
    }

    // depth (overworld only), scaling from deepStartY down to deepFullY
    try {
      if (ow && y < CONFIG.drain.deepStartY) {
        var span = (CONFIG.drain.deepStartY - CONFIG.drain.deepFullY)
        var t = span > 0 ? clamp((CONFIG.drain.deepStartY - y) / span, 0, 1) : 1
        delta -= (CONFIG.drain.deepMin + (CONFIG.drain.deepMax - CONFIG.drain.deepMin) * t)
      }
    } catch (e) { warnOnce('depth', e) }

    // daylight restore (day + open sky)
    if (oc_sn_dayOk) {
      try { if (isDaylightSafe(player)) delta += CONFIG.restore.daylight }
      catch (e) { oc_sn_dayOk = false; warnOnce('daylight', e) }
    }

    // sleep restore
    if (oc_sn_sleepOk) {
      try { if (player.isSleeping()) delta += CONFIG.restore.sleep }
      catch (e) { oc_sn_sleepOk = false; warnOnce('isSleeping', e) }
    }

    // horror proximity spike
    try { hc = countHorrorsNear(player, CONFIG.drain.horrorRadius); if (hc > 0) delta -= CONFIG.drain.horror }
    catch (e) { warnOnce('proximity', e) }

    // --- DIAG per-evaluation breakdown (temporary): every drain/restore input + the resulting delta.
    if (CONFIG.debug) {
      console.info(LOG + 'DIAG eval ' + player.username + ': sanity=' + round1(sanity) +
        ' light=' + light + ' y=' + round1(y) + ' overworld=' + ow + ' horrors=' + hc +
        ' dayOk=' + oc_sn_dayOk + ' sleepOk=' + oc_sn_sleepOk +
        ' => delta=' + round2(delta) + ' newSanity=' + round1(clamp(sanity + delta, 0, 100)))
    }

    if (delta !== 0) {
      sanity = clamp(sanity + delta, 0, 100); pd.putDouble('oc_sanity', sanity)
      if (CONFIG.debug) console.info(LOG + 'DIAG write ' + player.username + ': stored back=' + round1(pd.getDouble('oc_sanity')))
    }

    // band transitions (diegetic descent whisper only)
    var prevBand = pd.getInt('oc_sanity_band')
    var band = computeBand(sanity, prevBand)
    if (band !== prevBand) {
      pd.putInt('oc_sanity_band', band)
      if (band > prevBand) {
        if (CONFIG.bandLines && BAND_LINES[band]) player.tell(Text.of(BAND_LINES[band]).darkGray().italic(true))
        dlog('player ' + player.username + ': ' + BAND_NAMES[prevBand] + ' -> ' + BAND_NAMES[band] + ' (sanity ' + round1(sanity) + ')')
      }
    }

    // Channel A — roll an ambient event if past this band's cooldown
    if (band > 0) {
      var now = gameTime(player)
      var last = pd.getLong('oc_sanity_last_event')
      var jitter = 1 + (Math.random() - 0.5) * 2 * CONFIG.cooldown.jitter
      if (now - last >= COOLDOWNS[band] * jitter) {
        pd.putLong('oc_sanity_last_event', now)
        fireEvent(player, band)
        sanity = clamp(sanity - CONFIG.drain.selfEvent, 0, 100) // self-feedback drain
        pd.putDouble('oc_sanity', sanity)
      }
    }
  } catch (e) { warnOnce('tick', e) }
})

// ============================================================================
// Channel B — natural-spawn suppression via checkSpawn (docs §8.2) [verify live]
// ============================================================================
function spawnReason(event) {
  // CheckLivingEntitySpawnEventJS exposes the MobSpawnType as `type` (field) / getType()
  try { if (event.type) return '' + event.type } catch (e) {}
  try { if (typeof event.getType === 'function') return '' + event.getType() } catch (e) {}
  return null
}
function nearestSanity(ent) {
  try {
    var players = ent.level.players
    if (!players) return CONFIG.spawn.noPlayerSanity
    var n = (typeof players.size === 'function') ? players.size() : players.length
    if (!n) return CONFIG.spawn.noPlayerSanity
    var best = null, bestD = 1e18
    for (var i = 0; i < n; i++) {
      var pl = (typeof players.get === 'function') ? players.get(i) : players[i]
      var dx = pl.x - ent.x, dy = pl.y - ent.y, dz = pl.z - ent.z
      var d = dx * dx + dy * dy + dz * dz
      if (d < bestD) { bestD = d; best = pl }
    }
    return best ? getSanity(best) : CONFIG.spawn.noPlayerSanity
  } catch (e) { warnOnce('nearestSanity', e); return CONFIG.spawn.noPlayerSanity }
}
if (CONFIG.spawn.enabled) {
  try {
    EntityEvents.checkSpawn(event => {
      // Decide inside try/catch, but call event.cancel() OUTSIDE it: in KubeJS cancel()
      // works by throwing EventExit, which must propagate to the event system to deny the
      // spawn. Wrapping it in try/catch (as before) swallowed the exit and the deny was lost.
      var doDeny = false
      try {
        var ent = event.entity
        if (!ent) return
        var id = '' + ent.type
        if (!HORROR_ID_SET[id]) return
        if (CONFIG.spawn.naturalOnly) {
          var reason = spawnReason(event)
          if (reason !== null && reason.toUpperCase().indexOf('NATURAL') < 0) return
        }
        var s = nearestSanity(ent)
        var cancelChance = Math.pow(clamp(s, 0, 100) / 100, CONFIG.spawn.gamma)
        if (Math.random() < cancelChance) {
          doDeny = true
          dlogSampled('suppress ' + id + ' sanity=' + round1(s) + ' cancel=' + round2(cancelChance))
        }
      } catch (e) { warnOnce('checkSpawn-body', e); return }
      if (doDeny) event.cancel() // EventExit propagates -> spawn denied
    })
    oc_sn_channelB = true
  } catch (e) { oc_sn_channelB = false; warnOnce('checkSpawn-register', e) }
}

// ============================================================================
// Death reset — deterministic clean slate on respawn (docs §3.2) [verify live]
// ============================================================================
try {
  PlayerEvents.respawned(event => {
    try {
      var p = event.player
      if (!p) return
      p.persistentData.putDouble('oc_sanity', CONFIG.baseline)
      p.persistentData.putBoolean('oc_sanity_init', true)
      p.persistentData.putInt('oc_sanity_band', 0)
    } catch (e) { warnOnce('respawn', e) }
  })
} catch (e) { warnOnce('respawn-register', e) }

// ============================================================================
// Clarity consumables — active recovery (docs §10)
// ============================================================================
ItemEvents.rightClicked('oldcivilisation:sedative_ampoule', event => {
  var p = event.player
  if (!p) return
  try {
    restoreSanity(p, CONFIG.restore.clarityItem)
    p.runCommandSilent('playsound minecraft:entity.generic.drink master @s ~ ~ ~ 1 1')
    var held = p.getMainHandItem()
    if (!p.isCreative() && held && !held.isEmpty()) held.shrink(1)
    p.tell(Text.of('The noise recedes. A little.').darkGray().italic(true))
  } catch (e) { warnOnce('sedative', e) }
})
ItemEvents.rightClicked('oldcivilisation:familiar_photograph', event => {
  var p = event.player
  if (!p) return
  try {
    var pd = p.persistentData
    var now = gameTime(p)
    if (now - pd.getLong('oc_photo_cd') < 2400) {
      p.tell(Text.of('You have already looked. It does not help twice so soon.').darkGray().italic(true))
      return
    }
    pd.putLong('oc_photo_cd', now)
    restoreSanity(p, CONFIG.restore.clarityItem * 0.4)
    p.tell(Text.of('A face you are sure you knew. It steadies you.').darkGray().italic(true))
  } catch (e) { warnOnce('photo', e) }
})

// ============================================================================
// /sanity command — read a player's hidden state (docs §15.5)
//   /sanity              -> the caller's own sanity + band
//   /sanity <target>     -> another player's (requires permission level 2)
// ============================================================================
function sanityReport(ctx, target) {
  try {
    if (!target) { ctx.source.sendSystemMessage(Text.of(LOG + 'no target player.')); return 0 }
    var s = getSanity(target)
    var prevBand = 0
    try { prevBand = target.persistentData.getInt('oc_sanity_band') } catch (e) {}
    var band = BAND_NAMES[computeBand(s, prevBand)]
    var msg = target.username + ' — sanity ' + round1(s) + '/100  [' + band + ']'
    var caller = ctx.source.player
    if (caller) caller.tell(Text.of(msg).gray())
    else ctx.source.sendSystemMessage(Text.of(msg))
    return 1
  } catch (e) { warnOnce('sanity-report', e); return 0 }
}
try {
  ServerEvents.commandRegistry(event => {
    var Commands = event.commands
    var Arguments = event.arguments
    event.register(
      Commands.literal('sanity')
        .executes(ctx => sanityReport(ctx, ctx.source.player))
        .then(Commands.argument('target', Arguments.PLAYER.create(event))
          .requires(src => src.hasPermission(2))
          .executes(ctx => sanityReport(ctx, Arguments.PLAYER.getResult(ctx, 'target'))))
    )
  })
} catch (e) { warnOnce('command-register', e) }

// ============================================================================
// Load-time confirmation (docs §15.2)
// ============================================================================
console.info(LOG + 'loaded. interval=' + CONFIG.interval + 't  bands(U/D/B)=' +
  CONFIG.bands.unease + '/' + CONFIG.bands.dread + '/' + CONFIG.bands.breakdown +
  '  spawn.enabled=' + CONFIG.spawn.enabled + ' gamma=' + CONFIG.spawn.gamma + '  debug=' + CONFIG.debug)
console.info(LOG + 'Channel A (ambient): ENABLED.  Channel B (spawn suppression): ' +
  (CONFIG.spawn.enabled ? (oc_sn_channelB ? 'ENABLED' : 'FAILED TO REGISTER — spawns fall back to base rate') : 'DISABLED by config'))
console.info(LOG + 'horror set: ' + HORROR_IDS.length + ' entity ids (proximity drain + Channel B). Unresolved ids are inert (datapack tag uses required:false).')
