// priority: 100
// Old Civilisation story items — state, discovery tracking, interactions, endgame grant.
// - Stamps a random oc_variant on lore items when acquired (LootJS can't randomise NBT at drop time).
// - Tracks distinct discoveries + per-path flags in player.persistentData.
// - Escalates the stateful items (unstable_memory, signal_device) as discoveries grow.
// - Grants the Correction Fragment once, only after engineering + magic + space evidence (implore.md endgame rule).

const OC_PREFIX = 'oldcivilisation:'
const OC_VARIANT_ITEMS_MAX = 12   // upper bound of random variant index; tooltip wraps by pool length
// Clarity consumables (sanity mechanic) are functional items, not lore evidence: skip them from
// discovery tracking AND variant stamping so a stackable consumable never gets per-item random NBT.
const OC_NON_LORE = { 'oldcivilisation:sedative_ampoule': true, 'oldcivilisation:familiar_photograph': true }

function ocStage(id, discoveries) {
  if (id === 'oldcivilisation:unstable_memory') {
    if (discoveries >= 16) return 3
    if (discoveries >= 9) return 2
    if (discoveries >= 4) return 1
    return 0
  }
  if (id === 'oldcivilisation:signal_device') {
    return discoveries >= 10 ? 1 : 0
  }
  return 0
}

// throttled inventory scan: variant stamping + discovery tracking + stateful stage + endgame grant
PlayerEvents.tick(event => {
  var player = event.player
  if (!player || player.age % 60 !== 0) return
  try {
    var pd = player.persistentData
    var discoveries = pd.getInt('oc_discoveries') | 0
    var seen = pd.getString('oc_seen') || ''
    var inv = player.inventory
    var size = inv.getContainerSize()

    for (var i = 0; i < size; i++) {
      var st = inv.getItem(i)
      if (!st || st.isEmpty()) continue
      var id = st.id
      if (id.indexOf(OC_PREFIX) !== 0) continue
      if (OC_NON_LORE[id]) continue   // clarity consumables: not lore, no stamping/tracking

      // --- discovery tracking (distinct ids) ---
      if (seen.indexOf('[' + id + ']') < 0) {
        seen += '[' + id + ']'
        discoveries += 1
        pd.putString('oc_seen', seen)
        pd.putInt('oc_discoveries', discoveries)
        if (/incident_report|audio_log|containment_label|id_badge/.test(id)) pd.putBoolean('oc_flag_tech', true)
        if (/memory_fragment|arcane_notes|bound_thought/.test(id)) pd.putBoolean('oc_flag_magic', true)
        if (/colony_manifest|navigation_fragment|flight_recorder/.test(id)) pd.putBoolean('oc_flag_space', true)
      }

      var nbt = st.nbt
      var isStateful = (id === 'oldcivilisation:unstable_memory' || id === 'oldcivilisation:signal_device')

      if (isStateful) {
        var stage = ocStage(id, discoveries)
        var curStage = (nbt && nbt.oc_stage !== undefined && nbt.oc_stage !== null) ? Number(nbt.oc_stage) : -1
        if (curStage !== stage) {
          var curVar = (nbt && nbt.oc_variant !== undefined && nbt.oc_variant !== null) ? Number(nbt.oc_variant) : 0
          var ns = Item.of(id, st.count)
          ns.nbt = { oc_variant: curVar, oc_stage: stage }
          inv.setItem(i, ns)
        }
      } else if (!nbt || nbt.oc_variant === undefined || nbt.oc_variant === null) {
        var v = Math.floor(Math.random() * OC_VARIANT_ITEMS_MAX)
        var ns2 = Item.of(id, st.count)
        ns2.nbt = { oc_variant: v }
        inv.setItem(i, ns2)
      }
    }

    // --- endgame: grant the Correction Fragment once, after all three paths ---
    if (!pd.getBoolean('oc_correction_granted') &&
        pd.getBoolean('oc_flag_tech') && pd.getBoolean('oc_flag_magic') && pd.getBoolean('oc_flag_space') &&
        discoveries >= 12) {
      pd.putBoolean('oc_correction_granted', true)
      player.give(Item.of('oldcivilisation:correction_fragment'))
      player.tell(Text.of('Something settles into your pack. You did not pick it up.').darkRed().italic(true))
    }
  } catch (e) {
    console.error('[OldCiv] tick handler error: ' + e)
  }
})

// Signal device — right-click sample, message escalates with discoveries
ItemEvents.rightClicked('oldcivilisation:signal_device', event => {
  var p = event.player
  if (!p) return
  try {
    var d = p.persistentData.getInt('oc_discoveries') | 0
    if (d < 10) {
      p.tell(Text.of('SIGNAL DETECTED.').gray())
      p.tell(Text.of('SOURCE: UNKNOWN.').gray())
    } else {
      p.tell(Text.of('SIGNAL DETECTED.').gray())
      p.tell(Text.of('SOURCE: LOCAL.').darkRed().italic(true))
    }
    p.runCommandSilent('playsound minecraft:block.sculk_sensor.clicking master @s ~ ~ ~ 1 0.6')
  } catch (e) { console.error('[OldCiv] signal_device: ' + e) }
})

// Audio log — right-click plays an eerie recording + prints a transcript line
ItemEvents.rightClicked('oldcivilisation:audio_log', event => {
  var p = event.player
  if (!p) return
  try {
    p.runCommandSilent('playsound minecraft:entity.elder_guardian.curse master @s ~ ~ ~ 0.8 0.8')
    var lines = [
      '"Day 42. We have confirmed the signal is responding."',
      '"Day 61. It responds faster than we transmit."',
      '"Day 90. It answered a question we had not asked yet."',
    ]
    var v = (p.persistentData.getInt('oc_discoveries') | 0) % lines.length
    p.tell(Text.of('[ playback ] ').darkGray().append(Text.of(lines[v]).gray()))
  } catch (e) { console.error('[OldCiv] audio_log: ' + e) }
})

// #7 — marquee horrors leave a unique residue, not generic loot. Near-guaranteed on the (rare) kill,
// spawned at the death location. These mobs have no JSON loot table, so a death event is the reliable route.
const OC_RESIDUES = {
  'themimic_er:mimicer': 'oldcivilisation:distorted_replica',
  'cave_dweller:cave_dweller': 'oldcivilisation:deep_echo',
  'man:manfromthefog': 'oldcivilisation:fog_residue',
  'man:managgresive': 'oldcivilisation:fog_residue',
  'weeping_angels:weeping_angel': 'oldcivilisation:temporal_residue',
}
EntityEvents.death(event => {
  try {
    var e = event.entity
    if (!e || !e.type) return
    var drop = OC_RESIDUES[e.type]
    if (!drop) return
    if (Math.random() > 0.9) return // rare encounter — evidence is almost, but not quite, certain
    e.server.runCommandSilent(`summon item ${e.x} ${e.y} ${e.z} {Item:{id:"${drop}",Count:1b}}`)
  } catch (err) { console.error('[OldCiv] residue drop: ' + err) }
})

ServerEvents.recipes(event => {
  // Corrupted Map — combine 4 fragments into the (still-uninterpretable) map
  event.shapeless('oldcivilisation:corrupted_map', [
    'oldcivilisation:corrupted_map_fragment',
    'oldcivilisation:corrupted_map_fragment',
    'oldcivilisation:corrupted_map_fragment',
    'oldcivilisation:corrupted_map_fragment',
  ])
  // #4 — assemble the full HS-LGTFO record from every recovered exodus fragment (flavour; does NOT gate rockets)
  event.shapeless('oldcivilisation:hs_lgtfo_mission_data', [
    'oldcivilisation:flight_recorder_launch',
    'oldcivilisation:flight_recorder_transit',
    'oldcivilisation:flight_recorder_arrival',
    'oldcivilisation:flight_recorder_final',
    'oldcivilisation:colony_manifest',
    'oldcivilisation:navigation_fragment',
  ])
})
