// priority: 100
// Old Civilisation story items — loot distribution (LootJS 2.13.1)
// Items are added plain; the random text variant (oc_variant) is stamped on acquisition
// by server_scripts/oc_interactions.js. Chances follow implore.md (common 5-10%, rare ~2-3%, very rare ~1%).
// correction_fragment is NOT looted — it is granted by the endgame mechanic in oc_interactions.js.

// ---- named table groups ----
const T_VILLAGE_HOUSES = [
  'minecraft:chests/village/village_plains_house', 'minecraft:chests/village/village_savanna_house',
  'minecraft:chests/village/village_snowy_house', 'minecraft:chests/village/village_taiga_house',
  'minecraft:chests/village/village_desert_house',
]
const T_VILLAGE_WORK = [
  'minecraft:chests/village/village_cartographer', 'minecraft:chests/village/village_temple',
  'minecraft:chests/village/village_toolsmith', 'minecraft:chests/village/village_mason',
  'minecraft:chests/village/village_fletcher',
]
const T_DUNGEON = ['minecraft:chests/simple_dungeon', 'minecraft:chests/abandoned_mineshaft']
const T_STRONGHOLD = ['minecraft:chests/stronghold_corridor', 'minecraft:chests/stronghold_crossing']
const T_LIBRARY = ['minecraft:chests/stronghold_library']
const T_MANSION = ['minecraft:chests/woodland_mansion']
const T_TEMPLES = ['minecraft:chests/jungle_temple', 'minecraft:chests/desert_pyramid']
const T_ANCIENT = ['minecraft:chests/ancient_city']
const T_BASTION = ['minecraft:chests/bastion_treasure', 'minecraft:chests/bastion_other']
const T_OUTPOST = ['minecraft:chests/pillager_outpost']
const T_OCEAN = [
  'minecraft:chests/shipwreck_treasure', 'minecraft:chests/buried_treasure',
  'minecraft:chests/underwater_ruin_big', 'minecraft:chests/underwater_ruin_small',
]
// Ad Astra More Structures — off-world chests
const T_SPACE_COMMON = ['ad_astra_more_structures:chests/common_moon', 'ad_astra_more_structures:chests/common_mars']
const T_SPACE_COMMON_ALL = [
  'ad_astra_more_structures:chests/common_moon', 'ad_astra_more_structures:chests/common_mars',
  'ad_astra_more_structures:chests/common_mercury', 'ad_astra_more_structures:chests/common_venus',
  'ad_astra_more_structures:chests/common_glacio', 'ad_astra_more_structures:chests/common_other',
]
const T_SPACE_RARE = [
  'ad_astra_more_structures:chests/rare_moon', 'ad_astra_more_structures:chests/rare_mars',
  'ad_astra_more_structures:chests/rare_mercury', 'ad_astra_more_structures:chests/rare_venus',
  'ad_astra_more_structures:chests/rare_glacio', 'ad_astra_more_structures:chests/rare_other',
]

LootJS.modifiers(event => {
  const add = (tables, chance, item) => {
    try { event.addLootTableModifier(tables).randomChance(chance).addLoot(item) }
    catch (e) { console.error('[OldCiv] loot add failed for ' + item + ': ' + e) }
  }

  // ---- Category 1: personal ----
  add(T_VILLAGE_HOUSES.concat(T_DUNGEON, T_MANSION), 0.08, 'oldcivilisation:personal_data_chip')
  add(T_STRONGHOLD.concat(T_DUNGEON, T_OUTPOST), 0.06, 'oldcivilisation:id_badge')
  add(T_VILLAGE_HOUSES.concat(T_MANSION, ['minecraft:chests/simple_dungeon']), 0.06, 'oldcivilisation:damaged_photo')
  add(T_VILLAGE_HOUSES.concat(T_VILLAGE_WORK, T_DUNGEON, T_OCEAN), 0.15, Item.of('oldcivilisation:old_currency', 3))
  add(T_LIBRARY.concat(T_MANSION, ['minecraft:chests/village/village_temple', 'minecraft:chests/village/village_cartographer']), 0.05, 'oldcivilisation:employee_handbook')

  // ---- Category 2: technical (engineer) ----
  add(T_DUNGEON.concat(T_MANSION, T_ANCIENT), 0.07, 'oldcivilisation:incident_report')
  add(T_MANSION.concat(T_LIBRARY, T_ANCIENT), 0.02, 'oldcivilisation:audio_log')
  add(T_LIBRARY.concat(T_MANSION, T_ANCIENT), 0.015, 'oldcivilisation:containment_label')

  // ---- Category 3: magic (mage / spirit) ----
  add(T_VILLAGE_HOUSES.concat(T_DUNGEON), 0.06, 'oldcivilisation:memory_fragment_faded')
  add(T_MANSION.concat(T_STRONGHOLD, T_TEMPLES), 0.03, 'oldcivilisation:memory_fragment_distorted')
  add(T_ANCIENT.concat(T_MANSION), 0.012, 'oldcivilisation:memory_fragment_forbidden')
  add(T_MANSION.concat(T_LIBRARY, T_ANCIENT, T_TEMPLES), 0.05, 'oldcivilisation:arcane_notes')
  add(T_ANCIENT.concat(T_BASTION), 0.02, 'oldcivilisation:bound_thought')

  // ---- Category 4: horror (chest sources; mob sources below) ----
  add(T_ANCIENT.concat(T_MANSION), 0.03, 'oldcivilisation:biological_sample')
  add(T_ANCIENT, 0.02, 'oldcivilisation:impossible_bone')
  add(T_MANSION.concat(T_OCEAN, T_ANCIENT), 0.05, 'oldcivilisation:corrupted_map_fragment')

  // ---- Category 5: exodus / space ----
  add(T_SPACE_RARE, 0.10, 'oldcivilisation:colony_manifest')
  add(T_SPACE_COMMON, 0.12, 'oldcivilisation:navigation_fragment')
  add(['ad_astra_more_structures:chests/common_moon', 'ad_astra_more_structures:chests/common_other'], 0.10, 'oldcivilisation:flight_recorder_launch')
  add(['ad_astra_more_structures:chests/common_moon', 'ad_astra_more_structures:chests/common_mars'], 0.10, 'oldcivilisation:flight_recorder_transit')
  add(['ad_astra_more_structures:chests/common_mars', 'ad_astra_more_structures:chests/rare_mars'], 0.10, 'oldcivilisation:flight_recorder_arrival')
  add(['ad_astra_more_structures:chests/rare_mars', 'ad_astra_more_structures:chests/rare_venus', 'ad_astra_more_structures:chests/rare_mercury'], 0.06, 'oldcivilisation:flight_recorder_final')

  // ---- Category 6: anomalous ----
  add(T_ANCIENT.concat(T_MANSION, T_LIBRARY, ['ad_astra_more_structures:chests/rare_moon', 'ad_astra_more_structures:chests/rare_mars']), 0.02, 'oldcivilisation:unstable_memory')
  add(T_ANCIENT.concat(['ad_astra_more_structures:chests/rare_other', 'ad_astra_more_structures:chests/rare_venus']), 0.02, 'oldcivilisation:signal_device')

  // ---- Horror mob drops: born_in_chaos (53 tables) + foes (11 tables) via regex ----
  // #7 rebalance: these are COMMON swarm mobs. Keep evidence rare so horror is not a farm.
  // The marquee horrors get their own guaranteed unique residues via EntityEvents.death
  // (see oc_interactions.js) — that is where the "supernatural, memorable" drops live.
  try {
    event.addLootTableModifier(/born_in_chaos_v1:entities\/.*/).randomChance(0.02).addLoot('oldcivilisation:biological_sample')
    event.addLootTableModifier(/born_in_chaos_v1:entities\/.*/).randomChance(0.01).addLoot('oldcivilisation:impossible_bone')
    event.addLootTableModifier(/foes:entities\/.*/).randomChance(0.025).addLoot('oldcivilisation:biological_sample')
    event.addLootTableModifier(/foes:entities\/.*/).randomChance(0.012).addLoot('oldcivilisation:impossible_bone')
  } catch (e) {
    console.error('[OldCiv] horror mob regex modifier failed (loot tables may differ): ' + e)
  }
})
