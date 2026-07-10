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

  // ---- Sanity: clarity consumables (relief items — see oc_sanity.js). Modest, so relief is earned. ----
  // Sedative Ampoule: medical/industrial caches. Familiar Photograph: homes/personal loot.
  add(T_DUNGEON.concat(T_STRONGHOLD, T_ANCIENT, T_OUTPOST), 0.05, Item.of('oldcivilisation:sedative_ampoule', 2))
  add(T_VILLAGE_HOUSES.concat(T_MANSION, ['minecraft:chests/simple_dungeon']), 0.04, 'oldcivilisation:familiar_photograph')

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

  // ============================================================================
  // Modded structures — thematic routing, slightly lower chances than the vanilla
  // equivalents above (this is a heavily-modded-structure pack; the vanilla-table
  // modifiers above rarely fire in practice). Added 2026-07-09.
  //   - YUNG's Better Dungeons/Mineshafts REUSE the vanilla tables above → already covered.
  //   - Nether fortresses (betterfortresses) are intentionally skipped: TFC disables
  //     nether portals in this pack (enableNetherPortals=false), so they're unreachable.
  // ============================================================================

  // ---- When Dungeons Arise (dungeons_arise:chests/<structure>/...) — routed by theme ----
  const WDA = groups => new RegExp('dungeons_arise:chests\\/(' + groups + ')\\/.*')

  // undead / plague / infested / abandoned → horror
  add(WDA('plague_asylum|infested_temple|undead_pirate_ship|abandoned_temple|scorched_mines'), 0.02, 'oldcivilisation:biological_sample')
  add(WDA('plague_asylum|infested_temple|abandoned_temple'), 0.012, 'oldcivilisation:impossible_bone')
  add(WDA('plague_asylum|infested_temple|undead_pirate_ship|abandoned_temple'), 0.02, 'oldcivilisation:memory_fragment_distorted')
  add(WDA('undead_pirate_ship|abandoned_temple'), 0.03, 'oldcivilisation:corrupted_map_fragment')

  // settlements / homes / camps → personal
  const WDA_HOMES = 'bandit_village|mushroom_village|mushroom_house|small_prairie_house|greenwood_pub|merchant_campsite|fishing_hut|jungle_tree_house|illager_campsite|bathhouse'
  add(WDA(WDA_HOMES), 0.05, 'oldcivilisation:personal_data_chip')
  add(WDA('bandit_village|mushroom_village|small_prairie_house|greenwood_pub|merchant_campsite|jungle_tree_house'), 0.04, 'oldcivilisation:damaged_photo')
  add(WDA(WDA_HOMES), 0.10, Item.of('oldcivilisation:old_currency', 3))
  add(WDA('greenwood_pub|merchant_campsite|bandit_village'), 0.03, 'oldcivilisation:employee_handbook')
  add(WDA('mushroom_house|mushroom_village|giant_mushroom|jungle_tree_house'), 0.04, 'oldcivilisation:memory_fragment_faded')

  // keeps / towers / forts / illager war → military-flavoured personal + technical
  add(WDA('keep_kayra|bandit_towers|thornborn_towers|illager_fort|illager_galley|illager_corsair|shiraz_palace|aviary'), 0.04, 'oldcivilisation:id_badge')
  add(WDA('keep_kayra|bandit_towers|thornborn_towers|illager_fort|shiraz_palace'), 0.04, 'oldcivilisation:incident_report')
  add(WDA('keep_kayra|shiraz_palace|illager_fort|illager_galley|illager_corsair'), 0.10, Item.of('oldcivilisation:old_currency', 3))

  // foundry / mechanical / mines / windmill / lighthouse / blimp → technical
  add(WDA('foundry|mechanical_nest|mining_system|mushroom_mines|mines_treasure_[a-z]+|illager_windmill|lighthouse|small_blimp'), 0.04, 'oldcivilisation:incident_report')
  add(WDA('foundry|mechanical_nest|mining_system|illager_windmill'), 0.012, 'oldcivilisation:audio_log')
  add(WDA('foundry|mechanical_nest'), 0.01, 'oldcivilisation:containment_label')
  add(WDA('mechanical_nest|small_blimp|lighthouse'), 0.012, 'oldcivilisation:signal_device')

  // monastery / heavenly / mythic beasts → arcane
  add(WDA('monastery|heavenly_challenger|heavenly_rider|heavenly_conqueror|typhon|ceryneian_hind'), 0.03, 'oldcivilisation:arcane_notes')
  add(WDA('monastery|heavenly_challenger|heavenly_rider|heavenly_conqueror'), 0.02, 'oldcivilisation:memory_fragment_distorted')
  add(WDA('typhon|ceryneian_hind|heavenly_conqueror'), 0.012, 'oldcivilisation:bound_thought')
  add(WDA('typhon|heavenly_conqueror|monastery'), 0.012, 'oldcivilisation:unstable_memory')

  // ---- YUNG's Better Strongholds → magic + technical (mirrors vanilla stronghold routing) ----
  add(/betterstrongholds:chests\/.*/, 0.04, 'oldcivilisation:id_badge')
  add(/betterstrongholds:chests\/.*/, 0.04, 'oldcivilisation:incident_report')
  add(/betterstrongholds:chests\/.*/, 0.03, 'oldcivilisation:arcane_notes')
  add(/betterstrongholds:chests\/.*/, 0.03, 'oldcivilisation:employee_handbook')
  add(/betterstrongholds:chests\/.*/, 0.02, 'oldcivilisation:memory_fragment_distorted')

  // ---- YUNG's Better Desert & Jungle Temples → ancient / magic ----
  const YTEMPLES = /better(desert|jungle)temples:chests\/.*/
  add(YTEMPLES, 0.02, 'oldcivilisation:memory_fragment_distorted')
  add(YTEMPLES, 0.03, 'oldcivilisation:arcane_notes')
  add(YTEMPLES, 0.03, 'oldcivilisation:corrupted_map_fragment')
  add(YTEMPLES, 0.10, Item.of('oldcivilisation:old_currency', 3))

  // ---- YUNG's Better Ocean Monuments → ocean ----
  add(/betteroceanmonuments:chests\/.*/, 0.10, Item.of('oldcivilisation:old_currency', 3))
  add(/betteroceanmonuments:chests\/.*/, 0.03, 'oldcivilisation:corrupted_map_fragment')
  add(/betteroceanmonuments:chests\/.*/, 0.04, 'oldcivilisation:memory_fragment_faded')

  // ---- YUNG's Better Witch Huts → magic / horror ----
  add(/betterwitchhuts:chests\/.*/, 0.04, 'oldcivilisation:memory_fragment_faded')
  add(/betterwitchhuts:chests\/.*/, 0.03, 'oldcivilisation:arcane_notes')
  add(/betterwitchhuts:chests\/.*/, 0.02, 'oldcivilisation:biological_sample')

  // ---- More Underground Structures (musgrave) → underground dungeon ----
  add(/more_underground_structures:chests\/.*/, 0.05, 'oldcivilisation:personal_data_chip')
  add(/more_underground_structures:chests\/.*/, 0.04, 'oldcivilisation:incident_report')
  add(/more_underground_structures:chests\/.*/, 0.04, 'oldcivilisation:memory_fragment_faded')
  add(/more_underground_structures:chests\/.*/, 0.02, 'oldcivilisation:biological_sample')

  // ---- Lost Architects → artificer's workshop (technical/arcane), brawler's guild (personal/library) ----
  add(/lost_architects:chests\/artificers_workshop.*/, 0.04, 'oldcivilisation:incident_report')
  add(/lost_architects:chests\/artificers_workshop.*/, 0.03, 'oldcivilisation:arcane_notes')
  add(/lost_architects:chests\/artificers_workshop.*/, 0.012, 'oldcivilisation:audio_log')
  add(/lost_architects:chests\/artificers_workshop.*/, 0.012, 'oldcivilisation:signal_device')
  add(/lost_architects:chests\/brawlers_guild.*/, 0.05, 'oldcivilisation:personal_data_chip')
  add(/lost_architects:chests\/brawlers_guild.*/, 0.10, Item.of('oldcivilisation:old_currency', 3))
  add(/lost_architects:chests\/brawlers_guild.*/, 0.03, 'oldcivilisation:employee_handbook')
  add(/lost_architects:chests\/brawlers_guild.*/, 0.03, 'oldcivilisation:arcane_notes')
})
