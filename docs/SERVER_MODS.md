# Server Mod List — Spooky Dookie

**Server:** the live VM (`files/server` under the FTP login)  
**Minecraft:** 1.20.1 · **Loader:** Forge 47.4.10  
**Active jars:** 282 · **Disabled on the server:** 3  
**Generated:** 2026-11-07 by `tools/generate_server_modlist.py` from the server's own `mods/` folder — do not edit by hand.

> The server is the client instance's `mods/` **minus the client-only list** below, kept in step by
> `scripts/sync_server_mods.py`. Every jar here must match the client exactly, or players are refused at login.

**Parity:** matches the client instance (307 active jars − 25 client-only = 282).

> **Notes** (hand-maintained in `tools/generate_server_modlist.py`; everything else here is generated):
>
> - **Live server:** the Proxmox VM, deployed over FTP by `scripts/sync_server_mods.py`. `Servers/Server v2`
>   (built 2026-11-02) is now a local test bed (`--server`), and `Servers/spookydookie` (July 2026, TFC-era) is kept
>   unchanged for reference; neither is synced by default.
> - **Runtime:** the VM runs Java 21 (Ubuntu OpenJDK 21.0.12, `-Xms8G -Xmx32G`, G1). Forge 1.20.1 targets Java 17.
> - **Pinned on both sides — never sync newer versions:** `chunksending-1.20.1-2.8` (4.1 never delivers chunks
>   under DimThreads), `farsight-1.20.1-5.1`, `hardcoretruedarkness-4.0.0` (v6 blacks out the view under shaders and
>   is a hard client/server protocol gate). The newer jars are parked in the client's `local/mods-rolled-back-2026-11-01/`.
> - **Pack-built** (category *Local Fixes*, source in `tools/`): `spooky_tick_fixes` holds the server-tick fixes, the
>   activation range and the horror ray-cast guard (config `config/spooky_tick_fixes-common.toml`), on clients and the
>   server. `spooky_pregen` runs Chunky while the server is empty (config `config/spooky_pregen.json`, `/pregen`); it
>   is inert on clients. `farsight_immptl_compat` is client-only. `immptl_dimthread_compat` is disabled with DimThreads.
> - **DimThreads disabled 2026-11-06:** version 1.2.1 ticks every dimension under one global lock (no parallelism), and
>   a 46-minute profile with it off was no slower. Its jar and `immptl_dimthread_compat` are `.jar.disabled` on both sides.
> - **Server-side helpers added 2026-11-06:** Let Me Despawn (+ its library Almanac) lets mobs that picked up items
>   despawn normally; Dynamic View (`dynview`) lowers view/simulation distance when ticks run long.
> - **Removed / disabled 2026-11-06:** Data Anchor (an orphan library with a strict network channel, so it came off
>   every client too) and The Man From The Fog (disabled in the client and on the VM).
> - **Kept despite being client-leaning** (harmless idling on a server, and dropping them risked a dependency or
>   client refusal): `Camerapture`, `farsight`, `sounds_of_the_forest`, `clientcrafting`, plus client-side libraries
>   (`athena`, `fusion`, `Iceberg`, `Prism`, `CreativeCore`).

---

## Local Fixes (2)

- Spooky Dookie pre-generation scheduler — `spooky_pregen-1.2.0.jar`
- Spooky Dookie tick fixes — `spooky_tick_fixes-1.3.4.jar`

## Core libraries & performance (68)

- Ad Astra: Structures Overhaul — `aso-0.1.0.jar`
- AI-Improvements — `AI-Improvements-1.20-0.5.2.jar`
- Almanac — `almanac-forge-1.20.1-1.26.11.1.jar`
- Architectury — `architectury-9.2.14-forge.jar`
- Athena — `athena-forge-1.20.1-3.1.2.jar`
- Balm — `balm-forge-1.20.1-7.3.44.jar`
- Better Compatibility Checker — `BetterCompatibilityChecker-forge-4.0.8+mc1.20.1.jar`
- betterchunkloading mod — `betterchunkloading-1.20.1-5.4.jar`
- Bookshelf — `Bookshelf-Forge-1.20.1-20.2.15.jar`
- Botarium — `botarium-forge-1.20.1-2.3.4.jar`
- C4P (ComputerCraft Create Compatibility Patch) — `c4p-1.0.0.jar`
- Caelus API — `caelus-forge-3.2.0+1.20.1.jar`
- Canary — `canary-mc1.20.1-0.3.3.jar`
- chunksending mod — `chunksending-1.20.1-2.8.jar`
- Chunky — `Chunky-1.3.146.jar`
- Chunky x Farmer's Delight — `chunky_x_farmers_delight-1.0.0-forge-1.20.1.jar`
- Citadel — `citadel-2.6.3-1.20.1.jar`
- Cloth Config v10 API — `cloth-config-11.1.136-forge.jar`
- Clumps — `Clumps-forge-1.20.1-12.0.0.4.jar`
- Collective — `collective-1.20.1-8.73.jar`
- Connectivity Mod — `connectivity-1.20.1-7.7.jar`
- CoroUtil — `coroutil-forge-1.20.1-1.3.7.jar`
- CreateBetterFps — `createbetterfps-1.20.1-1.1.3.jar`
- CreativeCore — `CreativeCore_FORGE_v2.12.40_mc1.20.1.jar`
- Cristel Lib — `cristellib-1.1.6-forge.jar`
- Cupboard utilities — `cupboard-1.20.1-4.2.jar`
- Curios API — `curios-forge-5.14.1+1.20.1.jar`
- Deimos — `deimos-1.20.1-forge-2.8.jar`
- Dynamic view distance — `dynview-1.20.1-4.0.jar`
- EntityCulling — `entityculling-forge-1.11.3-mc1.20.1.jar`
- Fast Workbench — `FastWorkbench-1.20.1-8.0.4.jar`
- fastasyncworldsave mod — `fastasyncworldsave-1.20.1-2.6.jar`
- Ferrite Core — `ferritecore-6.0.1-forge.jar`
- flib — `flib-1.20.1-0.0.16.jar`
- Framework — `framework-forge-1.20.1-0.8.0.jar`
- Fzzy Config — `fzzy_config-0.7.7+1.20.1+forge.jar`
- GeckoLib 4 — `geckolib-forge-1.20.1-4.8.4.jar`
- GlitchCore — `GlitchCore-forge-1.20.1-0.0.1.1.jar`
- GuideME — `guideme-20.1.15.jar`
- Iceberg — `Iceberg-1.20.1-forge-1.1.25.jar`
- Integrated API — `integrated_api-forge-1.20.1-1.9.1.jar`
- Iron's Lib — `irons_lib-1.20.1-2.3.0.jar`
- kotlinforforge-4.12.0-all — `kotlinforforge-4.12.0-all.jar`
- LetMeDespawn — `letmedespawn-forge-1.20.1-1.26.11.1.jar`
- Limited Chunkloading — `limitedchunks-1.20.1-4.1.jar`
- LumiLib — `lumilib-0.4.2-forge-1.20.1-0.4.2.jar`
- Mixin Extras NeoForge on Forge Fix — `mixinextrasneoonforgefix-1.0.0.jar`
- Mobtimizations — `mobtimizations-forge-1.20.1-1.0.1.jar`
- ModernFix — `modernfix-forge-5.27.91+mc1.20.1.jar`
- Moonlight Library — `moonlight-1.20-2.16.38B-forge.jar`
- Placebo — `Placebo-1.20.1-8.6.3.jar`
- Player Animator — `player-animation-lib-forge-1.0.2-rc1+1.20.jar`
- Prism — `Prism-1.20.1-forge-1.0.5.jar`
- Puzzles Lib — `PuzzlesLib-v8.1.36-1.20.1-Forge.jar`
- recipeessentials mod — `recipeessentials-1.20.1-4.7.jar`
- Resourceful Lib — `resourcefullib-forge-1.20.1-2.1.29.jar`
- Resourcefulconfig — `resourcefulconfig-forge-1.20.1-2.1.3.jar`
- Rhino — `rhino-forge-2001.2.3-build.10.jar`
- Searchables — `Searchables-forge-1.20.1-1.0.3.jar`
- SmartBrainLib — `SmartBrainLib-forge-1.20.1-1.15.jar`
- Smoothchunk mod — `smoothchunk-1.20.1-4.4.jar`
- spark — `spark-1.10.53-forge.jar`
- Structure Essentials mod — `structureessentials-1.20.1-5.1.jar`
- SuperMartijn642's Config Library — `supermartijn642configlib-1.1.8-forge-mc1.20.jar`
- SuperMartijn642's Core Lib — `supermartijn642corelib-1.1.26-forge-mc1.20.1.jar`
- Valhelsia Core — `valhelsia_core-forge-1.20.1-1.1.2.jar`
- Voidless Framework — `voidlessframework-1.3.4-forge-1.20.1.jar`
- Zeta — `Zeta-1.0-31.jar`

## Create & addons (23)

- better create — `better_create-1.0.0-forge-1.20.1.jar`
- Compressed Creativity — `compressedcreativity-1.20.1-0.2.0-all.jar`
- Create — `create-1.20.1-6.0.8.jar`
- Create Ad Atra Compat — `create_ad_astra_compat-forge-1.20.1-1.0.0.jar`
- Create Crafts & Additions — `createaddition-1.20.1-1.3.3.jar`
- Create Deco — `createdeco-2.0.3-1.20.1-forge.jar`
- Create Enchantment Industry — `create_enchantment_industry-1.4.1-for-create-6.0.8.jar`
- Create Hypertube — `create_hypertube-0.6.0-FORGE.jar`
- Create Slice & Dice — `sliceanddice-forge-3.6.0.jar`
- Create: Alex's Caves Compat — `create_alexscaves_compat-1.20.1-1.6.2.jar`
- Create: Better Motors — `create_better_motors-4.0.0.jar`
- Create: Central Kitchen — `create_central_kitchen-1.20.1-for-create-6.0.8-1.5.1.jar`
- Create: Connected — `create_connected-1.2.3-mc1.20.1-all.jar`
- Create: Deep Dark — `create_deep_dark-1.7.0-forge-1.20.1.jar`
- Create: More Bridges — `morebridges-forge-1.0.2+1.20.1.jar`
- Create: Transmission — `createtransmission-1.2.2+forge-create6-1.20.1.jar`
- Create_waystones_recipes — `create_waystones_recipes-2.0.1.b.jar`
- Extreme Reactors Create Compat — `ExtremeReactors2-CreateCompat-1.20.1-1.0.4.jar`
- KubeJS Create — `kubejs-create-forge-2001.3.0-build.8.jar`
- PonderJS — `ponderjs-1.20.1-2.1.0.jar`
- Sophisticated Backpacks Create Integration — `sophisticatedbackpackscreateintegration-1.20.1-0.1.13.193.jar`
- Sophisticated Storage Create Integration — `sophisticatedstoragecreateintegration-1.20.1-0.1.26.245.jar`
- Waystone & Create — `waystoneandcreate.jar`

## Tech — energy, automation, industry (19)

- Ad Astra — `ad_astra-forge-1.20.1-1.15.21.jar`
- Ad Astra - Extra Additions — `ad_astra_extra_additions-1.20.1-1.1.1.jar`
- Ad Astra Extra — `adastraextra-forge-1.20.1-1.2.3.jar`
- Ad Astra: More Structures — `ad_astra_more_structures-1.20.1-forge.jar`
- AE2WTLib — `ae2wtlib-15.3.3-forge.jar`
- Applied Energistics 2 — `appliedenergistics2-forge-15.4.12.jar`
- Applied Mekanistics — `Applied-Mekanistics-1.4.3.jar`
- CC: Tweaked — `cc-tweaked-1.20.1-forge-1.113.1.jar`
- Create: Steam 'n' Rails — `Steam_Rails-1.7.3+forge-mc1.20.1.jar`
- Extreme Reactors — `ExtremeReactors2-1.20.1-2.0.97.jar`
- Immersive Engineering — `ImmersiveEngineering-1.20.1-10.2.0-183.jar`
- Immersive Petroleum — `ImmersivePetroleum-1.20.1-4.3.1-36b.jar`
- Mekanism — `Mekanism-1.20.1-10.4.16.80.jar`
- Mekanism: Additions — `MekanismAdditions-1.20.1-10.4.16.80.jar`
- Mekanism: Generators — `MekanismGenerators-1.20.1-10.4.16.80.jar`
- Mekanism: Tools — `MekanismTools-1.20.1-10.4.16.80.jar`
- PneumaticCraft: Repressurized — `pneumaticcraft-repressurized-6.0.23+mc1.20.1.jar`
- Simple Structures Ad Astra 1.20.1 — `Simple Structures Ad Astra 1.20.1 1.3.jar`
- Zero CORE 2 — `ZeroCore2-1.20.1-2.1.48.jar`

## Magic (9)

- Ars Nouveau — `ars_nouveau-1.20.1-4.12.7-all.jar`
- Ars Nouveau: Two-Way Portals — `ars_two_way_portals-1.3.4.jar`
- Blood Magic — `BloodMagic-1.18-1.20.1-3.3.8-50.jar`
- Forbidden & Arcanus — `forbidden_arcanus-1.20.1-2.2.6.jar`
- Hexerei — `hexerei-0.4.2.3.jar`
- Iron's Spells 'n Spellbooks — `irons_spellbooks-1.20.1-3.16.3.jar`
- Mahou Tsukai — `mahoutsukai-1.20.1-v1.34.81.jar`
- Occultism — `occultism-1.20.1-1.158.0.jar`
- Occultism KubeJS — `occultism_kubejs-1.20.1-1.0.0.jar`

## Colony / civilisation (11)

- Cozy Apartments — `cozyapartments 1.20.1 v1.2.jar`
- Domum Ornamentum — `domum_ornamentum-1.20.1-1.0.296-universal.jar`
- Lunara Colonies — `forge-lunara-colonies-1.20.1.28.jar`
- MineColonies — `minecolonies-1.20.1-1.1.1301.jar`
- Multi-Piston — `multipiston-1.20-1.2.43-RELEASE.jar`
- SmallColonies — `smallcolonies 1.20.1 v1.8.jar`
- SmallColonies_ME — `smallcoloniesme 1.20.1 v1.9.jar`
- Structurize — `structurize-1.20.1-1.0.818.jar`
- sword_and_kingdom_schematic — `sword_and_kingdom_schematic-1.7.jar`
- TownTalk — `towntalk-1.20.1-1.1.0.jar`
- UI Library Mod — `blockui-1.20.1-1.0.193.jar`

## Horror & atmosphere (20)

- Antlers — `antlers-1.0.0.jar`
- Born in Chaos — `born_in_chaos_[Forge]1.20.1_1.7.5.jar`
- CANTBREATHE — `cant_breathe-1.20.1-1.0.2.jar`
- cave_dweller — `cave_dweller-1.20.1-1.6.4.jar`
- Creeping Woods — `CreepingWoods-forge-1.20.1-2.4.jar`
- Door Knocker (Horror) — `door_knocker-forge-1.20.1-1.3.jar`
- Foes! — `foes-0.3-forge-1.20.1.jar`
- Hardcore True Darkness — `hardcoretruedarkness-4.0.0.jar`
- Horror Element Mod — `horror_element_mod-1.6.3-forge-1.20.1.jar`
- John Mod Reborn — `John_Mod_Reborn_1.0.4_mc1.20.1.jar`
- lootintegrations_borninchaos mod — `lootintegrations_borninchaos-1.1.jar`
- Rake: The Arrival — `RakeArrival-forge-1.20.1-1.7.jar`
- Server-Side Horror — `serversidehorror-1.20.1-forge-5.1.jar`
- Sounds of the Forest — `sounds_of_the_forest-1.0.2-forge-1.20.1.jar`
- The One Who Watches — `TheOneWhoWatches-V1.4.4-1.20.1.jar`
- The Pale Hound — `Pale_Hound-1.20.1-1.10.0.jar`
- the_obsessed — `the_obsessed-1.5.2g-forge-1.20.1.jar`
- themimic_er — `themimic_er-1.20.1-1.7.0.jar`
- Weeping Angels — `weeping_angels-46.1.6-forge.jar`
- Whispering Spirits — `WhisperingSpirits-forge-1.20.1-v1.4.0.jar`

## Mobs & world creatures (4)

- Alex's Caves — `alexscaves-2.0.2.jar`
- Alex's Mobs — `alexsmobs-1.22.9.jar`
- Goblin Traders — `goblintraders-forge-1.20.1-1.11.5.jar`
- Guard Villagers — `guardvillagers-1.20.1-1.6.19.jar`

## Worldgen & structures (28)

- Abandoned Watchtowers Refurnished — `abandoned_watchtower_refurnished-7.0-forge-1.20.1.jar`
- Amplified Nether — `Amplified_Nether_26.2_v1.2.15.jar`
- big lost city — `big_lost_city-1.0.1-forge-1.20.1.jar`
- Deeper and Darker — `deeperdarker-forge-1.20.1-1.3.3.jar`
- Immersive Weathering — `immersive_weathering-1.20.1-2.0.5-forge.jar`
- Incendium — `Incendium_1.20.x_v5.3.5.jar`
- Integrated Dungeons and Structures — `idas_forge-1.13.0+1.20.1.jar`
- lootintegration_townsandtowers mod — `lootintegration_townsandtowers-1.5.jar`
- Lost Architects — `LostArchitects-forge-1.0.4.jar`
- more underground structures — `more_undrground_structures_1.20.1_8.1.jar`
- Repurposed Structures — `repurposed_structures-7.1.25+1.20.1-forge.jar`
- Serene Seasons — `SereneSeasons-forge-1.20.1-9.1.0.3.jar`
- Spooky Campsite — `spooky_campsite-1.0.2-forge-1.20.1.jar`
- Structory — `Structory_1.20.x_v1.3.5.jar`
- Terralith — `Terralith_1.20.x_v2.5.4.jar`
- Towns and Towers — `Towns-and-Towers-1.12-Fabric+Forge.jar`
- When Dungeons Arise — `DungeonsArise-1.20.x-2.1.58-release.jar`
- YUNG's API — `YungsApi-1.20-Forge-4.0.6.jar`
- YUNG's Better Caves — `YungsBetterCaves-1.20.1-Forge-2.0.7.jar`
- YUNG's Better Desert Temples — `YungsBetterDesertTemples-1.20-Forge-3.0.3.jar`
- YUNG's Better Dungeons — `YungsBetterDungeons-1.20-Forge-4.0.4.jar`
- YUNG's Better End Island — `YungsBetterEndIsland-1.20-Forge-2.0.6.jar`
- YUNG's Better Jungle Temples — `YungsBetterJungleTemples-1.20-Forge-2.0.5.jar`
- YUNG's Better Mineshafts — `YungsBetterMineshafts-1.20-Forge-4.0.4.jar`
- YUNG's Better Nether Fortresses — `YungsBetterNetherFortresses-1.20-Forge-2.0.6.jar`
- YUNG's Better Ocean Monuments — `YungsBetterOceanMonuments-1.20-Forge-3.0.4.jar`
- YUNG's Better Strongholds — `YungsBetterStrongholds-1.20-Forge-4.0.3.jar`
- YUNG's Better Witch Huts — `YungsBetterWitchHuts-1.20-Forge-3.0.3.jar`

## Food & farming (7)

- [Let's Do] Candlelight — `letsdo-candlelight-forge-2.0.5.jar`
- [Let's Do] Farm & Charm — `letsdo-farm_and_charm-forge-1.0.14.jar`
- AppleSkin — `appleskin-forge-mc1.20.1-2.5.1.jar`
- CookingForBlockheads — `cookingforblockheads-forge-1.20.1-16.0.15.jar`
- Farmer's Delight — `FarmersDelight-1.20.1-1.3.4.jar`
- Rationcraft — `rationcraft-1.3.7-forge-1.20.1.jar`
- Soldier's Delight — `soldiersdelight-1.2-forge-1.20.1.jar`

## Decoration & building (30)

- Amendments — `amendments-1.20-2.2.6.jar`
- Another Furniture — `another_furniture-forge-1.20.1-3.0.4.jar`
- Beautify — `beautify-2.0.2.jar`
- Chipped — `chipped-forge-1.20.1-3.0.7.jar`
- Cluttered — `cluttered-[Forge]-3.0.3-1.20.1.jar`
- Connected Glass — `connectedglass-1.1.14-forge-mc1.20.1.jar`
- Create: Copycats+ — `copycats-3.0.10+mc.1.20.1-forge.jar`
- Create: Interiors — `interiors-1.20.1-forge-0.6.0.jar`
- Decorative Blocks — `decorative_blocks-forge-1.20.1-4.1.3.jar`
- Fragile Torches — `fragiletorches-1.20.1-1.0.3.jar`
- FramedBlocks — `FramedBlocks-9.4.3.jar`
- Fusion — `fusion-1.3.16-forge-mc1.20.1.jar`
- Handcrafted — `handcrafted-forge-1.20.1-3.0.6.jar`
- Macaw's Bridges — `mcw-bridges-3.1.2-mc1.20.1forge.jar`
- Macaw's Doors — `mcw-doors-1.1.5-mc1.20.1forge.jar`
- Macaw's Fences and Walls — `mcw-mcwfences-1.2.1-mc1.20.1forge.jar`
- Macaw's Furniture — `mcw-furniture-3.4.1-mc1.20.1forge.jar`
- Macaw's Holidays — `mcw-holidays-1.1.2-mc1.20.1forge.jar`
- Macaw's Lights and Lamps — `mcw-lights-1.1.5-mc1.20.1forge.jar`
- Macaw's Paths and Pavings — `mcw-mcwpaths-1.1.1-mc1.20.1forge.jar`
- Macaw's Roofs — `mcw-roofs-2.3.2-mc1.20.1forge.jar`
- Macaw's Stairs and Balconies — `mcw-mcwstairs-1.0.2-mc1.20.1forge.jar`
- Macaw's Trapdoors — `mcw-trapdoors-1.1.5-mc1.20.1forge.jar`
- Macaw's Windows — `mcw-mcwwindows-2.4.2-mc1.20.1forge.jar`
- MrCrayfish's Furniture Mod: Refurbished — `refurbished_furniture-forge-1.20.1-1.0.20.jar`
- Rechiseled — `rechiseled-1.2.6-forge-mc1.20.1.jar`
- Rechiseled: Applied Energistics 2 — `rechiseledae-1.0.1-forge-mc1.20.1.jar`
- Rechiseled: Chipped — `rechiseled_chipped-2.0-1.20.1.jar`
- Supplementaries — `supplementaries-1.20-3.1.44-forge.jar`
- Supplementaries Squared — `suppsquared-1.20-1.1.29.jar`

## Storage, QoL & utility (43)

- Artifacts — `artifacts-forge-9.5.19.jar`
- Camerapture — `Camerapture-1.10.12+mc1.20.1-forge.jar`
- Carry On — `carryon-forge-1.20.1-2.1.2.7.jar`
- Carry On Back — `carry_on_back-1.0.0-forge-1.20.1.jar`
- clientcrafting mod — `clientcrafting-1.20.1-2.1.jar`
- Comforts — `comforts-forge-6.4.0+1.20.1.jar`
- Corpse — `corpse-forge-1.20.1-1.0.23.jar`
- CosmeticArmorReworked — `cosmeticarmorreworked-1.20.1-v1a.jar`
- Crash Assistant — `CrashAssistant-forge-1.19-1.20.1-1.11.12.jar`
- Double Doors — `doubledoors-1.20.1-7.5.jar`
- Elytra Slot — `elytraslot-forge-6.4.4+1.20.1.jar`
- EnchantmentDescriptions — `EnchantmentDescriptions-Forge-1.20.1-17.1.21.jar`
- Explorer's Compass — `ExplorersCompass-1.20.1-1.4.0-forge.jar`
- FallingTree — `FallingTree-1.20.1-4.3.6.jar`
- Farsight mod — `farsight-1.20.1-5.1.jar`
- Fast Leaf Decay — `FastLeafDecay-32.jar`
- Immersive Portals + Immersive Portals Core + QMiscUtil — `immersive-portals-3.0.7-all.jar`
- Immersive Portals: Full Immersion — `immersive_portals_full_immersion-1.0.20.jar`
- Jade — `Jade-1.20.1-Forge-11.13.3.jar`
- Jade Addons — `JadeAddons-1.20.1-Forge-5.5.1.jar`
- Lootr — `lootr-forge-1.20-0.7.35.94.jar`
- Make it Compatible: Immersive Portals — `make-it-compatible-immersive-portals-forge-1.20.1.jar`
- MmmMmmMmmMmm — `dummmmmmy-1.20-2.0.12-forge.jar`
- Nature's Compass — `NaturesCompass-1.20.1-1.12.0-forge.jar`
- No Chat Reports — `NoChatReports-FORGE-1.20.1-v2.2.3.jar`
- No Chat Restrictions — `NoChatRestrictions-Forge-MC1.20.1-v1.0.0.jar`
- Observable — `observable-4.4.1.jar`
- OpenLoader — `OpenLoader-Forge-1.20.1-19.0.5.jar`
- Phantom Remover — `phantom_remover-1.0.1.jar`
- Quark — `Quark-4.0-462.jar`
- Simple Backups — `SimpleBackups-1.20.1-3.1.18.jar`
- Simple Day Length Extender — `SimpleDayLengthExtender-1.2.2-Forge-1.20.1.jar`
- Sophisticated Backpacks — `sophisticatedbackpacks-1.20.1-3.26.19.2239.jar`
- Sophisticated Backpacks: Realistic Crafting — `sbrc-1.1-1.20.1.jar`
- Sophisticated Core — `sophisticatedcore-1.20.1-1.5.21.2444.jar`
- Sophisticated Storage — `sophisticatedstorage-1.20.1-1.5.13.2212.jar`
- Storage Drawers — `StorageDrawers-forge-1.20.1-12.15.1.jar`
- Too Fast — `toofast-1.20-0.4.3.5.jar`
- Visual Workbench — `VisualWorkbench-v8.0.1-1.20.1-Forge.jar`
- Waystones — `waystones-forge-1.20.1-14.1.21.jar`
- Waystones Teleport Pets — `WaystonesTeleportPets-1.20-1.20.1--1.2.jar`
- Xaero's Map - Waystones Compability — `xaeros_waystones_compatibility-1.1 - 1.20.1.jar`
- Xaero's World Map — `xaeroworldmap-forge-1.20.1-1.47.0.jar`

## Security & protection (3)

- More Protectables — `moreprotectables-1.20.1-1.2.3.10.jar`
- SC CRT — `scfilters-1.0.0.jar`
- SecurityCraft — `[1.20.1] SecurityCraft v1.10.2.1.jar`

## Quests, loot, data & guides (15)

- FTB Library — `ftb-library-forge-2001.2.13.jar`
- FTB Quests — `ftb-quests-forge-2001.4.22.jar`
- FTB Quests Entity Visualization — `ftbquestsentityvis-1.13.0-1.20.1-forge.jar`
- FTB Teams — `ftb-teams-forge-2001.3.2.jar`
- FTB Ultimine — `ftb-ultimine-forge-2001.1.8.jar`
- FTBQuestsOptimizer — `FTBQuestsOptimizer-forge-2.1.0-1.20.1.jar`
- KubeJS — `kubejs-forge-2001.6.5-build.26.jar`
- lootintegration_wda mod — `lootintegration_wda-1.8.jar`
- Lootintegrations mod — `lootintegrations-1.20.1-4.7.jar`
- lootintegrations_integrated mod — `lootintegrations_integrated-1.6.jar`
- LootJS — `lootjs-forge-1.20.1-2.13.1.jar`
- Modonomicon — `modonomicon-1.20.1-forge-1.79.3.jar`
- Patchouli — `Patchouli-1.20.1-85-FORGE.jar`
- When Dungeons Arise Quest Installer — `wda-questline-1.20.1-1.0.0.jar`
- Ycurrenci — `ycurrenci-2.1.0-forge-1.20.1.jar`

---

## Client-only mods excluded from the server (25)
Present in the client instance but deliberately **not** copied to the server. They set
`IGNORE_SERVER_VERSION`, so clients keep them and are not refused for the server lacking them.
The groups and notes come from `CLIENT_ONLY_GROUPS` in `scripts/sync_server_mods.py`.

**Rendering / shaders / visual FX — hard-crash a dedicated server:**
- cavedust-2.0.4-1.20.1-forge.jar — confirmed server crash 2026-07-12 (loads `client.gui.screens.Screen`)
- embeddium-0.3.31+mc1.20.1.jar
- ImmediatelyFast-Forge-1.5.5+1.20.4.jar
- oculus-mc1.20.1-1.8.0.jar
- sodiumdynamiclights-forge-1.0.10-1.20.1.jar
- sodiumoptionsapi-forge-1.0.10-1.20.1.jar
- VoidFog-1.20.1-2.0.23.jar — confirmed server crash 2026-07-12 (loads `client.renderer.FogRenderer`)

**Client HUD / GUI / input — no server function:**
- BetterF3-7.0.2-Forge-1.20.1.jar
- blur-forge-3.1.1.jar
- BorderlessWindow-1.20-1.6.0.jar
- chat_heads-0.15.7-forge-1.20.jar
- configured-forge-1.20.1-2.2.3.jar — in-game config screens (JEI's wrench); kept off the server so server configs can't be edited from the client; its channel accepts a missing peer
- Controlling-forge-1.20.1-12.0.2.jar
- LegendaryTooltips-1.20.1-forge-1.4.5.jar
- moreoverlays-1.24.1-mc1.20.1-forge.jar
- MouseTweaks-forge-mc1.20.1-2.25.1.jar
- simplezoom-forge-1.20.1-1.0.1.jar — hold-to-zoom key (C); client mixins only

**Client animation / audio:**
- AmbientSounds_FORGE_v6.3.11_mc1.20.1.jar
- mobends-1.20.1-6.0.1-forge.jar
- notenoughanimations-forge-1.12.7-mc1.20.1.jar
- PresenceFootsteps-1.20.1-1.9.1-beta.1.jar
- sound-physics-remastered-forge-1.20.1-1.5.1.jar

**Client-only utilities:**
- farsight_immptl_compat-1.0.1.jar — pack-built; source in `tools/farsight_immptl_compat/`, restores Farsight's chunk keeping under Immersive Portals
- jei-1.20.1-forge-15.56.0.205.jar — recipe viewer
- xaerominimap-forge-1.20.1-26.6.0.jar — minimap; the world map's server component is kept

## Disabled jars
**On the server** (3) — left in `mods/` as `.jar.disabled`, not loaded:

- dimthread-FORGE-mc1.20.1-v1.2.1.jar.disabled
- immptl_dimthread_compat-1.0.0.jar.disabled
- The-Man-From-The-Fog-1.4-1.20.1.jar.disabled

**In the client instance** (8) — kept as `.jar.disabled`; none may be active on the server:

- TFC_CarryOnStorage1.2.0.jar.disabled
- TerraFirmaCraft-Forge-1.20.1-3.2.28.jar.disabled
- The-Man-From-The-Fog-1.4-1.20.1.jar.disabled
- arssophisticatedcompat-0.3.0+mc1.20.1-forge.jar.disabled
- dimthread-FORGE-mc1.20.1-v1.2.1.jar.disabled
- immptl_dimthread_compat-1.0.0.jar.disabled
- mcw_tfc_aio-0.2.30-1.20.1.jar.disabled
- mekanismsophisticatedbackpacks-1.0.1+mc1.20.1-forge.jar.disabled
