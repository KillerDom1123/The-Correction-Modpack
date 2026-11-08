// priority: 90
// Old Civilisation — surviving a marquee horror also yields its residue (Field Reports residue quests).
//
// oc_interactions.js drops each residue when its horror DIES, but these stalkers rarely die: they leave (CANTBREATHE
// gives up 60 s into a chase with 250 HP, the Mimic and Cave Dweller after 30 s). The design is "observe, don't kill"
// (docs/ftbquest.md, Field Reports), so an encounter you live through counts too:
//   - one of RESIDUES' horrors comes within ENC.radius blocks of a survival player (tracked by entity UUID);
//   - later it is gone (despawned, left, or its chunk unloaded because you got away) and was NOT killed,
//     and the player did not die in between  ->  the residue goes into the player's inventory.
// A kill still drops the residue through oc_interactions.js (EntityEvents.death), so this path skips dead entities.
// One residue per player per horror; state is in memory only (a restart forgets open encounters).
// Keep RESIDUES in step with OC_RESIDUES in oc_interactions.js.
//
// Rhino note (see oc_sanity.js): every variable inside a repeatedly-invoked callback is `var`, never let/const.

const ENC = {
  interval: 40,     // ticks between checks per player
  radius: 24,       // blocks: close enough to count as an encounter
}
const RESIDUES = {
  'themimic_er:mimicer': 'oldcivilisation:distorted_replica',
  'cave_dweller:cave_dweller': 'oldcivilisation:deep_echo',
  'cant_breathe:cant_breathe': 'oldcivilisation:fog_residue',
  'weeping_angels:weeping_angel': 'oldcivilisation:temporal_residue',
}
const ENC_LINES = {
  'oldcivilisation:distorted_replica': 'It is gone. Something shaped almost like you was left behind.',
  'oldcivilisation:deep_echo': 'It is gone. The sound it made has not stopped.',
  'oldcivilisation:fog_residue': 'It is gone. The air is still thin where it stood.',
  'oldcivilisation:temporal_residue': 'It is gone. Something it left behind is older than it was a moment ago.',
}
const ENC_LOG = '[OldCiv/Encounters] '
var oc_enc_open = {}      // player uuid -> { entity uuid -> residue id }
var oc_enc_warned = {}
function encWarn(site, e) {
  if (oc_enc_warned[site]) return
  oc_enc_warned[site] = 1
  console.warn(ENC_LOG + 'degraded at [' + site + ']: ' + e + ' (further identical messages suppressed)')
}

PlayerEvents.tick(event => {
  var p = event.player
  if (!p || p.age % ENC.interval !== 0) return
  try {
    if (p.isSpectator() || p.isCreative()) return
    var key = String(p.getUUID())
    var open = oc_enc_open[key]
    if (!open) { open = {}; oc_enc_open[key] = open }

    // new encounters (the entity object is kept, so its removal reason can be read later)
    var near = p.level.getEntities(p, p.getBoundingBox().inflate(ENC.radius))
    for (var i = 0; i < near.size(); i++) {
      var e = near.get(i)
      var residue = RESIDUES[String(e.type)]
      if (!residue || !e.isAlive()) continue
      var eid = String(e.getUUID())
      if (open[eid] === undefined) open[eid] = { ent: e, residue: residue }
    }

    // finished encounters: the horror is gone, and not because it was killed
    for (var id in open) {
      var o = open[id]
      if (o === null) continue                       // already settled
      if (!o.ent.isRemoved()) {
        if (!o.ent.isAlive()) open[id] = null       // dying: oc_interactions.js drops on death
        continue                                     // still around
      }
      var reason = String(o.ent.getRemovalReason())  // KILLED / DISCARDED / UNLOADED_TO_CHUNK / CHANGED_DIMENSION ...
      open[id] = null
      if (reason === 'KILLED') continue
      p.give(Item.of(o.residue))
      p.tell(Text.of(ENC_LINES[o.residue] || 'It is gone.').darkGray().italic(true))
      console.info(ENC_LOG + p.username + ' survived an encounter (' + reason + ') -> ' + o.residue)
    }
  } catch (err) { encWarn('tick', err) }
})

// Dying ends every open encounter without a reward.
EntityEvents.death(event => {
  try {
    var e = event.entity
    if (e && e.isPlayer()) delete oc_enc_open[String(e.getUUID())]
  } catch (err) { encWarn('death', err) }
})

PlayerEvents.loggedOut(event => {
  try { delete oc_enc_open[String(event.player.getUUID())] } catch (err) { encWarn('logout', err) }
})
