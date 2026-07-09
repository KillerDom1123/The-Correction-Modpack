# Config Changes

A per-mod record of every configuration change made, with **what** changed and **why**.
Theme throughout: a **horror / survival-progression** pack where technology is earned, exploration matters, and the world stays dangerous.

## How changes are applied (scope)

| Mechanism | Location | Scope |
|---|---|---|
| Common/client configs | `config/*.toml`, `config/<mod>/…` | Global — shared by all worlds |
| Per-world server configs | `saves/<world>/serverconfig/*-server.toml` | Per-world; made global via `defaultconfigs/` (applies to every **new** world) |
| OpenLoader datapacks | `config/openloader/data/<Pack>/` | Global — injected into every world |
| OpenLoader resources | `config/openloader/resources/<Pack>/` | Global resource pack |

All per-world (`-server.toml`) changes below have been copied to `defaultconfigs/` so they become the default for every world. Backups of edited files were saved alongside them as `*.bak`.

---

## The Mimic (`themimic_er-server.toml`) — *per-world → defaultconfigs*
**Goal:** keep the best psychological-horror mob **extremely rare**.

| Setting | Before | After |
|---|---|---|
| `can_spawn_min` | 300 | 3600 |
| `can_spawn_max` | 600 | 7200 |
| `can_spawn_cooldown_chance` | 0.4 | 0.85 |
| `can_spawn_cooldown` | 1200 | 3600 |
| `spawn_chance_per_tick` | 0.05 | 0.01 |
| `maximum_amount` | 3 | 1 |

**Why:** encounters go from every ~5–10 min to hour-plus gaps, solitary. A sighting should be a rare, memorable dread moment.

---

## Cave Dweller (`cave_dweller-server.toml`) — *per-world → defaultconfigs*
**Goal:** rare, **deep-only** apex threat (Alex's Caves already fills caves with danger).

| Setting | Before | After |
|---|---|---|
| `can_spawn_min` | 300 | 7200 |
| `can_spawn_max` | 600 | 14400 |
| `can_spawn_cooldown_chance` | 0.4 | 0.85 |
| `can_spawn_cooldown` | 1200 | 3600 |
| `spawn_chance_per_tick` | 0.005 | 0.0025 |
| `spawn_height` | 40 | 0 (spawns only at Y ≤ 0) |
| `maximum_amount` | 5 | 1 |
| `override_biome_datapack_config` | false | true |
| `surface_biomes` (whitelist) | `[]` | `["minecraft:deep_dark"]` |

**Why:** restricted to the Deep Dark and to deep Y-levels, and made much rarer, so it's a distinct deep-cave predator rather than a constant cave nuisance.

---

## Cold Sweat (`config/coldsweat/world.toml`, `entity.toml`) — *global*
**Goal:** cold is the main threat (nights, rain, mountains); heat only in deserts/lava; **everything in Celsius**.

- **Units:** every stored value converted from °F to **°C** (behavior-preserving — absolute temps via `(F−32)×5⁄9`, offsets/deltas via `×5⁄9`). Display was already Celsius.
- **Heat only where intended:** non-arid biome **noon** temps capped at **30 °C** (below the ~35 °C heatstroke threshold). Deserts, badlands, and Terralith arid/volcanic biomes kept hot.
- **Cold nights:** non-hot biome **midnight** temps capped at **6 °C** (tropical biomes exempt).
- **Cold mountains:** peak/high-altitude biomes forced cold (high ≤ 12 °C, low ≤ 2 °C).
- **Rain bites:** `Max Rain Soak` 0.2 → **0.35**, `Rain Soak Speed` 0.0125 → **0.02**, `Default Water Temperature` −10 °F → **−8 °C**, `Shade Temperature Offset` −9 °F → **−6 °C**.
- Lava/magma still emit heat (unchanged) → overheating underground near lava.

**Why:** implements the requested climate alignment — survival pressure from cold at night/in rain/on peaks, heat reserved for deserts and lava.

---

## Waystones — *common config + OpenLoader datapack*
**Goal:** no free teleport, require XP, gate crafting to mid-game (crafting stays enabled).

**`config/waystones-common.toml`:**
| Setting | Before | After |
|---|---|---|
| `minimumBaseXpCost` | 0.0 | 1.0 |
| `waystoneXpCostMultiplier` | 0.0 | 1.0 |
| `warpStoneXpCostMultiplier` | 0.0 | 1.0 |
| `sharestoneXpCostMultiplier` | 0.0 | 1.0 |
| `portstoneXpCostMultiplier` | 0.0 | 1.0 |
| `warpPlateXpCostMultiplier` | 0.0 | 1.0 |
| `inventoryButtonXpCostMultiplier` | 0.0 | 1.0 |
| `globalWaystoneXpCostMultiplier` | 0.0 | 1.0 |

**`WaystonesGate` datapack** — `warp_stone` recipe center **emerald → Eye of Ender**.

**Why:** every teleport now costs XP (min 1 level, scaling with distance), and the shared Warp Stone core (used by all waystones/sharestones) requires a post-nether item, so waystones arrive mid-game. "Require activation" is already Waystones' built-in default (must visit a stone to use it) — no change possible or needed.

---

## Lootr — *no change*
Verified chests are already **one-time per player** (`refresh_all=false`, no refresh lists, `decay_all=false`). Loot is already maximally scarce; `max_age` is only conversion-queue cleanup, not a loot cooldown. Nothing edited.

---

## Sound Physics Remastered (`config/sound_physics_remastered/soundphysics.properties` + `options.txt`) — *global*
**Goal:** sound travels (echoes, cave reverb, occlusion) without becoming deafening.

| Setting | Before | After |
|---|---|---|
| `environment_evaluation_ray_bounces` | 4 | 8 (more echoes) |
| `reverb_gain` | 1.0 | 1.2 (cave reverb) |
| `default_block_reflectivity` | 0.5 | 0.7 (longer reverb tails) |
| `reverb_distance` | 1.5 | 2.0 |
| `default_block_occlusion_factor` | 1.0 | 1.5 (muffled through walls) |
| `soundCategory_master` (options.txt) | 0.705 | 0.60 (slight overall reduction) |

**Why:** richer, more directional/echoing soundscape; the master trim keeps the fuller mix from being overwhelming.

---

## AmbientSounds (`QuietAmbience` — OpenLoader resource pack) — *global*
**Goal:** wind (and ambience) much less frequent; silence matters.

- Overrode wind sound definitions to add long **pauses**: `light-wind` length 20–60 s, **pause 150–350 s** (was no pause = constant); `heavy-wind`, `wind-in-leaves`, `wind-mesa` similarly given 2–5 min silent gaps.

**Why:** the config's numbers are only volume multipliers; wind felt constant because its definition had **no pause**. Adding pauses creates real silence between gusts. (Ported into `config/openloader/resources/` so it's global; the manual `options.txt` resource-pack entry was reverted.)

---

## Weeping Angels — *spawn config + common config + OpenLoader datapack*
**Goal:** rare, extremely dangerous, controlled teleporting.

**`config/weeping_angels_spawns.json`:** disabled in **186 of 202 biomes**; enabled (rare, solitary — weight 2, `min/max` 1; Deep Dark weight 3) only in spooky/remote biomes (Deep Dark, Dripstone, Dark Forest, Soul Sand Valley, eerie Terralith caves, Moonlight Valley/Grove, all End biomes, Alex's Caves Forlorn Hollows & Abyssal Chasm).

**`config/weeping_angels-common.toml`:**
| Setting | Before | After |
|---|---|---|
| `teleport_chance` | 50 | 10 |
| `interdimensional_teleporting` | true | false |
| `teleport_range` | 400 | 150 |

**`WeepingAngelsBuff` datapack** (per-tick attribute buff, applied once per angel): max health **150**, attack damage **12**, armor **10**, knockback resistance **1.0**.

**Why:** solitary rare spawns in fitting locations = "we found something impossible"; teleporting stays possible but rare, never cross-dimension, and only a moderate distance; the stat buff makes a single angel a genuine fight.

---

## Ars Nouveau (`config/ars_nouveau/glyph_*.toml`) — *global*
**Goal:** reduce spell damage slightly, encourage utility, discourage "machine-gun" magic.

- **Damage glyphs −~15–20%** (base + per-Amplify): Harm 5→4, Crush 3→2.5, Lightning 5→4, Explosion 6→5, Flare 7→6, Cold Snap 6→5, Fangs 6→5, Wind Shear 5→4.
- **Anti-spam:** `Cut` mana cost **0 → 10** (was free, infinitely spammable).
- **Cheaper utility:** Blink 50→30, Leap 25→15, Launch 30→20, Glide 100→70, Slowfall 30→20, Intangible 30→20, Light 25→15, Heal 50→35 (heal amount 3→4), Conjure Water 80→50, Invisibility 30→20, Dispel 30→20.

**Why:** magic becomes a tactical toolkit (teleport/light/support cheap) rather than a rapid-fire damage rifle.

---

## Occultism (`OccultismExpensiveRituals` datapack) — *global*
**Goal:** summoning demons should never be trivial.

- 19 demon-summon rituals had their ingredients swapped for **tier-scaled valuables** (ingredient **count kept identical** so existing pentacle sacrificial-bowl layouts still work):
  - **Foliot:** gold ingot, glowstone, lapis block, redstone block
  - **Djinni:** diamond, gold block, blaze powder, ender pearl
  - **Afrit / Wild Afrit / Demonic spouse:** diamond block, blaze rod, ghast tear, ender eye
  - **Marid / Wild Hunt:** netherite ingot, diamond block, ghast tear, wither skeleton skull

**Why:** the client toml is cosmetic-only; ritual cost lives in recipes. Scaling by demon tier makes each summon a real investment from "meaningful" (Foliot) to "serious commitment" (Marid).

---

## MineColonies — *server config + research datapack*
**Goal:** not a "free-labour simulator"; slow, earned growth; guards that don't trivialize; colonies that aren't safe.

**`minecolonies-server.toml`:**
| Setting | Before | After | Why |
|---|---|---|---|
| `initialcitizenamount` | 4 | 2 | Slower start |
| `maxcitizenpercolony` | 250 | 500 | Cap lifted (growth paced by housing/research instead) |
| `foodmodifier` | 1.0 | 1.5 | Feeding the colony is real work |
| `guardDamageMultiplier` | 1.0 | 0.6 | Guards aren't a murder-wall |
| `guardhealthmult` | 1.0 | 0.7 | Guards are killable |
| `pathfindingmaxthreadcount` | 1 | 4 | Builders/citizens stall less |
| `barbarianhordedifficulty` | 5 | 8 | Raids are a real threat |
| `averagenumberofnightsbetweenraids` | 14 | 7 | Raids more frequent |
| `minimumnumberofnightsbetweenraids` | 10 | 4 | — |

`dobarbariansbreakthroughwalls` and `mobattackcitizens` left **true** (walls matter but aren't absolute; night is dangerous).

**`MineColoniesSlowResearch` datapack:** research **time ×3** on the civilian/combat/technology branches (`base-time` 1.0 → 3.0), and **item costs scaled** ×2 (tier 1–2), ×3 (3–4), ×4 (5–6) across 198 researches (values ≥1024 left alone, capped at 1024).

**Why:** a sprawling self-sufficient city becomes a months-long project needing walls, guards, and lighting — not a week-one autopilot base.
*(Not configurable in this version: happiness weightings and per-building material costs — those live in internal logic / Structurize blueprints.)*

---

## Ad Astra (`AdAstraSpaceGate` datapack) — *global*
**Goal:** space is very late-game — conquer Earth first. Keep oxygen/survival.

- **NASA Workbench** (the chokepoint for all rockets/suits/rovers) now requires one signature item from each core system:
  - Create → **Precision Mechanism**
  - Mekanism → **Atomic Alloy**
  - AE2 → **ME Controller**
  - MineColonies → **Colony Teleport Scroll** (needs an Enchanter's Tower)

**Why:** you can't begin the space program until Create + Mekanism + AE2 + an established colony all exist. **Oxygen, temperature, gravity, air-vortex survival systems left ON** (`config/ad_astra.jsonc` unchanged).

---

## Mekanism (`MekanismProgression` datapack) — *global*
**Goal:** don't trivialize mining/resources/power; delay high ore multiplication; late-game jetpacks; nuclear as a milestone. **2× (Enrichment Chamber) kept early.**

| Item | Now requires |
|---|---|
| Purification Chamber (3×) | reinforced alloy + elite circuit + osmium block |
| Chemical Injection Chamber (4×) | atomic alloy + elite circuit + diamond block |
| Digital Miner | + **ultimate control circuit** (on top of atomic alloy, 2× teleportation core, sorters, robit) |
| Jetpack | reinforced alloy + elite circuit + elite chemical tank |
| Armored Jetpack | + atomic alloy |
| Fission Reactor Casing | reinforced alloy + steel casing (scales with reactor size) |
| Fission Fuel Assembly | steel + elite circuit + reinforced alloy + elite chemical tank |
| Control Rod Assembly | reinforced alloy + **ultimate control circuit** + steel |
| Fission Reactor Port | + ultimate control circuit |
| Fission Logic Adapter | redstone **blocks** + casing |

**Why:** the "press button, receive every resource" Digital Miner and 3×/4× processing become endgame; flight requires real infrastructure; a fission reactor becomes "humanity has returned," not a day-three build.

---

## SecurityCraft (`SecurityCraftBalance` datapack) — *global*
**Goal:** security useful but not silly — a bunker, not "AI kills everything."

- **Sentry** recipe now also requires a **diamond block**.

**Why:** no per-turret count limit exists in config, so making sentries a premium build means you place a few at chokepoints instead of carpeting the map. Reinforced blocks, keypads, and scanners are untouched.

---

## Create (`create-server.toml`) — *per-world → defaultconfigs*
**Goal:** factories require planning; keep trains cheap.

- **Stress impact ×1.25** on all 25 non-zero machine values (`[kinetics.stressValues.v2.impact]`).
- **Trains: unchanged** (kept cheap so the world can become connected by rail).

**Why:** machines cost more Stress Units, so generators/gearing need genuine planning. *(Create farms: no clean config lever — the stress bump makes large auto-farms costlier; targeted farm-machine gating is available on request.)*

---

## Applied Energistics 2 (`AE2Progression` datapack) — *global*
**Goal:** large storage expensive; autocrafting not rushed. Early game = drawers/backpacks/chests.

| Item | Now requires |
|---|---|
| ME Drive | 2× engineering processor + energy cell |
| 256k Storage Component (largest cell) | + engineering processor |
| Crafting Unit (base of every Crafting CPU) | 2× engineering processor |

**Why:** bulk storage and autocrafting become a late-game "rebuilt civilisation" payoff gated behind processor automation.

---

## Alex's Caves (`alexscaves-general.toml`, `alexscaves_biome_generation/*.json`) — *global*
**Goal:** cave biomes rare and far apart; unique materials require exploration.

| Setting | Before | After |
|---|---|---|
| `cave_biome_mean_separation` | 900 | 1800 (~4× rarer by area) |
| `distance_from_spawn` (per biome, ×1.5) | 400 / 450 / 500 / 650 | 600 / 675 / 750 / 975 |

**Why:** finding a cave biome becomes "something impossible," not "another cave biome"; the powerful rewards inside them now require real travel. *(No global loot-rarity slider exists; rarity-by-distance handles the exploration goal. Per-item loot nerfs available on request.)*

---

## Alex's Mobs (`alexsmobs.toml`) — *global*
**Goal:** quieter surface — a still forest is scary.

- **34 passive surface fauna** spawn weights reduced **~30%** (herbivores, monkeys, birds, small critters — e.g. gazelle 40→28, elephant 30→21, capuchin 28→20).
- **Left at full strength:** predators (tiger, crocodile, komodo, dropbear, froststalker, rhino, anaconda…), hostile/horror mobs, aquatic, and nether/end/cave spawns.

**Why:** thins out ambient forest clutter for atmosphere while keeping every actual threat intact.

---

## Serverside Horror (`config/serversidehorror.json`) — *global*
**Goal (the "From The Fog" distance-horror layer):** more distance events & rare sightings; **do not** increase attacks.

- Sighting/ambient event chances made ~1.5× more frequent (chance ÷ ~1.5): `herobrine_starer`, `starer`, `fake_steps`, `fake_mining`, `scary_sound`, `random_signs`, `fake_joiner`, `random_fake_joiner`, `heads_from_list`, `random_heads`.
- **Unchanged:** jumpscare, torch-breaking, traps, arson, lightning, spawn/join events (the aggressive/attack side).

**Why:** provides a tunable distance-sighting horror layer alongside the entity-based stalkers. More staring/footsteps/sounds without ramping up direct attacks.

> **Correction:** an earlier note here called *From The Fog* "hardcoded, no config." That's wrong — From The Fog (`watching`) is configured via **in-game datapack functions/commands** (`data/watching/functions/config/…`), not a `.toml`, and has an `autoConfig` that ramps intensity up over in-game days. See the handoff note below.

### Tuning pass — trim the meme/aggressive events, keep the subtle ones
The event mix leaned too gimmicky (Herobrine, fake player joins, sign spam). Rebalanced toward quiet, ambiguous dread. Reminder: `_chance` is ~"1 in N per tick", so **higher = rarer**.

| Event | Change | Why |
|---|---|---|
| `herobrine_starer_enable` | true → **false** | No Herobrine — off-theme meme horror |
| `jumpscare_enable` | true → **false** | The Herobrine jumpscare — off-theme |
| `fake_joiner_enable` | true → **false** | No fake "player joined" messages |
| `random_fake_joiner_enable` | true → **false** | Same — the second fake-joiner variant |
| `random_lightning_enable` | true → **false** | No lightning from a clear sky |
| `long_night_enable` | true → **false** | No artificially extended nights |
| `random_signs_enable` | true → **false** | No auto-placed message signs |
| `joining_in_dungeon_enable` | true → **false** | No fake joins while in dungeons |
| `break_torches_chance` | 720000 → **2000000** | Torch-breaking now *pretty rare* |
| `replace_torches_chance` | 720000 → **2000000** | Kept in step with break_torches (torch-tampering as a whole is pretty rare) |
| `fake_mining_chance` | 325000 → **2000000** | Distant fake-mining noises now *pretty rare* |
| `fake_steps_chance` | 325000 → **325000 (kept)** | Footsteps stay rare **but not too rare** — now the most present ambient cue |
| `starer_chance` | 273000 → **8000000** | The (non-Herobrine) starer made *incredibly rare* — a once-in-a-playthrough dread moment |

Left enabled at prior values: `scary_sound`, `heads_from_list`, `random_heads` (subtle, on-theme). `burn_down_house` and `joining_on_bedrock` were already off.

### Mod swap — From The Fog removed; Server-Side Horror is the sole events layer
*From The Fog* (`watching`) was removed from the pack; Server-Side Horror now provides the entire "something is interfering" layer. (A brief prior pass had disabled SSH's `break_torches`/`replace_torches`/`fake_mining` to avoid duplicating From The Fog — that was reverted, since there's no longer anything to duplicate.)

**Final SSH enabled set:** `break_torches` + `replace_torches` + `fake_mining` (all *pretty rare*, `chance` 2000000), `fake_steps` (rare-but-present, 325000), `starer` (*incredibly rare*, 8000000), `scary_sound`, `heads_from_list`, `random_heads` (+ `traps`/`setting_up_new_traps`/`old_villages`, untouched).

**Kept disabled** (per author preference — off-theme or disliked): `herobrine_starer`, `jumpscare`, `fake_joiner`, `random_fake_joiner`, `long_night`, `random_lightning`, `random_signs`, `joining_in_dungeon` (+ `burn_down_house`, `joining_on_bedrock`, `removing_leaves`, already off).

**Removal note:** From The Fog is not tracked in git (`mods/` is excluded), so removing the jar is the whole job. It stored state only in world scoreboards (`ftf.*` objectives) and used vanilla blocks, so leftover data is harmless — no missing-block or chunk damage on load. Any spawned Herobrine entity is dropped as an unknown entity when the world loads without the mod.

---

## The Man From The Fog (`config/man_config.toml`) — *global*
**Goal:** rare, persistent, stronger — a once-a-week dread, not nightly.

| Setting | Before | After |
|---|---|---|
| `min_spawn_rate` | 5000 | 48000 |
| `max_spawn_rate` | 20000 | 168000 |
| `min_chase_duration` | 400 | 800 |
| `max_chase_duration` | 1000 | 2400 |
| `vanish_distance` | 30 | 48 |
| `block_hardness` | 2.0 | 4.0 |
| `block_hardness_search` | 5.0 | 8.0 |
| `block_break_timer` | 10.0 | 6.0 (breaks faster) |

**Why:** appears every ~2–8 in-game days, pursues longer and is harder to shake, and breaks through more/faster — a rare but serious event.

---

## Day Length (`DayLength30` datapack) — *global*
**Goal:** ~30-minute days (requested 25–35).

- Disables vanilla auto time-advance (`doDaylightCycle false` on world load) and re-advances world time at **2 game-ticks per 3 real ticks** via a tick function → 24000-tick day over 36000 real ticks = **30 min**.

**Why:** longer days/nights (nights longer in real time suits the horror theme). *Retune:* change the tick ratio (25 min = 4/5, 35 min = 4/7). *Revert note:* if the pack is removed, run `/gamerule doDaylightCycle true` or time will freeze.

---

## New-mods pass (added mod batch)

A batch of new mods was added and reviewed. Most are **QoL / libraries / client / Create-addon / structures / decoration** that need no balance alignment. The ones touched:

### The Obsessed (`config/obsessed_configurations.toml`) — *global*
A targeted stalker horror mod. Aligned to the pack's "rare psychological horror" philosophy:
| Setting | Before | After | Why |
|---|---|---|---|
| `Stalk Frequency` | 4000 | **2000** | Rarer stalking |
| `Natural Targeter Spawnrate` | 2400 | **1200** | Rarer targeting |
| `Guaranteed Initial Targeting Time` | 3600 (1 hr forced) | **9999999** (effectively off) | No scripted early targeting — encounters stay organic/rare |

Left `Allow Multiple Targets = false` (one obsession at a time). Arachnophobia/hallucination toggles left default (player accessibility choice).

### Door Knocker (`config/doorknocker-common.toml`) — *global*
Ambient "something's at the door" event. Made **rarer** so it stays unsettling rather than background noise (consistent with "rare > frequent"):
| Setting | Before | After |
|---|---|---|
| `dayMinTicks` / `dayMaxTicks` | 24000 / 48000 | **48000 / 96000** (~40–80 min) |
| `nightMinTicks` / `nightMaxTicks` | 12000 / 24000 | **24000 / 48000** (~20–40 min) |

`openChance` (0.1) and line-of-sight (false = knocks through walls, creepier) left as-is.

### Reviewed — no change needed
- **Goblin Traders** — already gated (25% spawn chance + intervals); default is fine.
- **Guard Villagers** — fits the colony/village-defence theme; default fine.
- **Applied Mekanistics, Create Slice & Dice** — Create/AE2/Mek-tier automation that already sits behind existing gating.
- **Xaero's World Map** (world-map only, no live minimap/radar) — mild exploration concern only; left default.
- **Phantom Remover, FallingTree, Too Fast, Fast Leaf Decay, clientcrafting, Visual Workbench, Double Doors** — deliberate QoL choices; left as chosen.
- **Structures** (Spooky Campsite, Big Lost City, Abandoned Watchtowers Refurnished) — fit "explore ruins"; loot handled by existing Lootr.
- **Furniture/Connected Glass, libraries, client/perf mods** — cosmetic/support; no balance impact.

### Rare horror spawns via OpenLoader (`HorrorSpawnRarity` datapack)
**The Pale Hound, Rake: The Arrival, The One Who Watches, Antlers (Wendigo)** have no in-mod rarity config, so they're gated by overriding their Forge biome-modifier spawns. Reasoning: vanilla hostiles spawn at weight ~95–100 each; the pack's established rare-horror benchmark (Weeping Angels) is **weight 2**, so all are aligned to that:
| Mob | Spawn entities | Was | Now | Biomes |
|---|---|---|---|---|
| Rake: The Arrival | `rake_spawn_trigger`, `rake_stalking`, `rake_following` | 8 | **2** (~4× rarer) | forest / taiga (kept) |
| The One Who Watches | `toww_stalking`, `toww_staring` | 20 | **2** (~10× rarer) | any |
| Wendigo | `antlers:wendigo` | 20 | **2** (~10× rarer) | any |
| Pale Hound | *(code-weighted — no biome-modifier file)* | all "spooky" biomes | biome tag **replaced** with a tight remote set | deep_dark, dark_forest, snowy_taiga, grove, frozen_peaks |

The three data-driven mobs are done by overriding the mods' own `forge/biome_modifier/*.json` at the same path (clean replacement, no duplication). Pale Hound's weight isn't config-exposed, so it's rarified by restricting its spawn-biome tag to a few remote cold/dark biomes; if it still feels too common, a `remove_spawns` + `add_spawns` biome modifier is the follow-up (carries a modifier-ordering caveat).

#### Antlers dead-animal bodies — *worldgen decoration, same datapack*
Antlers also scatters static **dead-animal body blocks** (`bloody_cow`, `bloody_fox`, `bloody_pig`, `bloody_sheep`) across the surface of coniferous biomes (`#forge:is_coniferous`) via worldgen placed features — not mob spawns. They were dense enough to read as clutter rather than an eerie discovery. Each of the four `worldgen/placed_feature/*.json` is overridden at the same path with its `rarity_filter` **`chance` 17 → 68** (≈4× rarer, ~1 body per 68 chunks). Nothing else in the placement (biome/height/square) changed, so bodies still only appear on the ground in the same biomes — just far less often.

| Body | Placed feature | `chance` was | now |
|---|---|---|---|
| Dead cow / fox / pig / sheep | `antlers:deadcow/deadfox/deadpig/deadsheep` | 17 | **68** (~4× rarer) |

**Why:** keeps the unsettling "something died out here" atmosphere as an occasional find instead of a constant sight, matching the pack's "rare > frequent" and ambient-declutter passes (cf. Alex's Mobs fauna thinning). *Tunable:* raise `chance` further for even rarer bodies, lower back toward 17 for more. *Note:* worldgen decoration is baked in at chunk generation, so this only affects **newly generated** chunks — already-explored terrain keeps whatever it generated with.

*(FTB Quests / KubeJS: no additions — none of the new mods add craftable progression items needing gating; the new horror mobs are encounters that fit the planned "Horror Investigation" quest path if/when it's built.)*

---

## Power & magic pass (Extreme Reactors, Blood Magic, Mahou Tsukai)

A second batch of mods was added: **Extreme Reactors 2** (+ ZeroCore2 lib + Create-compat addon), **Blood Magic**, and **Mahou Tsukai** (found on re-scan — a very powerful Fate-style magic mod not previously reviewed). All three are tech/magic power systems and were reviewed against the pack's "power is earned, world stays dangerous" theme.

### Extreme Reactors (`config/extremereactors/common.toml` + `ExtremeReactorsProgression` datapack) — *global*
**Goal:** temper a notoriously overpowered power system so it's a genuine late-game milestone, parallel to Mekanism fission — not a cheap infinite-FE shortcut.

**`config/extremereactors/common.toml` (raw output — the only lever for this; reactant/reaction energy is code-registered, not data-driven, so OpenLoader can't touch it):**
| Setting | Before | After |
|---|---|---|
| `powerProductionMultiplier` | 1.0 | **0.5** (output halved) |
| `fuelUsageMultiplier` | 1.0 | **1.5** (yellorium burns 50% faster) |

**`ExtremeReactorsProgression` datapack** — the **reactor and turbine controllers** (the single one-per-multiblock chokepoint; you can't build either machine without one) now require Mekanism tech tiers, mirroring the `MekanismProgression` fission gating:
| Controller | Now also requires |
|---|---|
| Basic reactor / turbine controller | **reinforced alloy + elite control circuit** (mid-game) |
| Reinforced reactor / turbine controller | **atomic alloy + ultimate control circuit** (late-game) |

Both the yellorium and uranium recipe variants are overridden so there's no cheap bypass. Casings, fuel rods and other bulk parts are left as-is (gating the one-per-build controller delays access without making the bulk build tedious).

**Why:** output ×0.5 + fuel ×1.5 keeps reactors strong but ties them to an ongoing yellorium cost (mining/exploration) instead of being free perpetual power; the controller gate means you can't tap ER at all until Mekanism mid/late tech exists, so it sits *alongside* fission as an earned milestone rather than skipping the tech tree. *(Tunable: the two multipliers in common.toml — raise toward 1.0 for a lighter touch, lower for heavier.)*

### Blood Magic (`config/bloodmagic-common.toml`) — *global, light touch*
**Goal:** align the LP economy with "resources are earned" — everything else about Blood Magic (altar tiers, blood orbs, demon will, soul forge, 814 self-tiered recipes) is already grindy and thematically **perfect** for a horror pack, so no recipe gating was added (it would be redundant and fight the mod's own progression).

- **`sacrificialValues`:** passive breed-farm livestock (cow, chicken, horse, sheep, pig, rabbit) **100 → 50 LP/HP**. Villager (100 — a real, weighty sacrifice), tamed pets wolf/ocelot (100), slime (15), enderman (10) left unchanged.

**Why:** an auto-breeding cow/chicken farm feeding a Well of Suffering is a free LP firehose — the same "press button, receive resource" pattern the pack nerfs elsewhere (e.g. gating the Mek Digital Miner). Halving trivial livestock keeps the blood economy meaningful without gutting the intended sacrifice/altar loop. The Teleposer (a possible "free teleport" conflict) is already gated behind the demon-will Teleposer Focus + ender pearls and costs LP per jump, so it's consistent with the pack's "teleport costs something" rule — left as-is.

### Mahou Tsukai (`mahoutsukai-server.toml` → *defaultconfigs*) — *global (new worlds)*
**Goal:** rein in an extremely powerful spell mod so its teleportation falls in line with the pack's deliberate travel rules; leave its (already soulbind-gated) combat power intact pending playtest.

Mahou's balance lives entirely in a **server** config (`mahoutsukai-server.toml` — ~1350 lines, hundreds of `*_MANA_COST` / `*_DAMAGE` keys plus toggles) that Forge only generates on **world creation**. A world was generated; the pristine file was copied to `defaultconfigs/` (same generate → edit → copy workflow as `cave_dweller-server.toml`, `create-server.toml`, `minecolonies-server.toml`, `themimic_er-server.toml`) and these keys edited:

| Key | Before | After | Why |
|---|---|---|---|
| `EQUIVALENT_DISPLACEMENT_DIMENSIONAL_TRAVEL` | true | **false** | No cross-dimension teleport (matches Weeping Angels `interdimensional_teleporting=false`) |
| `EQUIVALENT_DISPLACEMENT_MAX_DISTANCE` | -1.0 (∞) | **256.0** | Displacement becomes a *local* tactical/return tool; long-distance travel stays channelled through the XP-costed Waystones |
| `REPLICA_TELEPORT_CROSS_DIMENSION` | true | **false** | Combat clone can't chase across dimensions |

**Why:** these are the only spells that directly conflict with an established pack rule ("no free/cross-dim teleport"). Everything else in Mahou is heavily self-gated (soulbound spell binding, slow mana-pool growth, ritual requirements) and fits the forbidden-magic horror theme, so combat costs/damage were **left at default** rather than inflated on guesswork.

*Left at default (candidates for a post-playtest pass if magic feels too strong):* offensive spell mana costs (Black Flame 300, Emrys focused 200/s, Borrowed Authority 900, Mystic Staff Big Explosion 5000, Reality Marble 4000), Drain Life, and Death Collection revival. `CREATIVE_IGNORES_MANA_COSTS` left true (creative-only). **Note:** `defaultconfigs/` applies to **new** worlds; the existing `Test*` worlds keep their own `serverconfig/mahoutsukai-server.toml` (delete it to regenerate from the new default).

---

## Performance — server-tick lag (worldgen)

**Symptom:** severe *tick* lag (not FPS) — entities stutter, interactions delayed. `logs/latest.log` shows the server up to **1351 ticks (67 s) behind** (`Can't keep up! … Running Xms behind`), clustered during exploration/chunk generation.

**Root cause:** **More Underground Structures** places its `catacomb` (a 29 KB multi-chunk structure) as a worldgen **`structure_feature`** rather than a proper jigsaw structure. Being larger than one chunk, its generation writes blocks into not-yet-generated neighbour chunks → `Detected setBlock in a far chunk`, which forces synchronous cascade chunk-gen mid-generation and stalls the worldgen workers / server thread. The log recorded **15,476** such errors from `more_underground_structures:catacomb_feature` alone (next worst: `creepy_1_feature`, 2).

**Fix (`PerfFixes` OpenLoader datapack):** override the mod's own `forge/biome_modifier/catacomb_feature_biome_modifier.json` at the same path with `{ "type": "forge:none" }` — Forge's no-op modifier. The catacomb feature is no longer added to any biome, so it stops generating (and stops the far-chunk cascade). The rest of the mod (chest dungeons, ore blobs/veins, ice/obsidian spikes, other small structure-features) is untouched.

*Scope:* worldgen is baked in at chunk generation, so this only affects **newly generated** chunks — already-explored terrain (and any catacombs already placed) is unchanged, but exploring new terrain will no longer trigger the stall. *Revert:* delete `config/openloader/data/PerfFixes/`.

---

## KubeJS — Old Civilisation script bugfix (`oc_interactions.js`)

`logs/latest.log` showed **167** repeated errors from `oc_interactions.js` — `InternalError: TypeError: redeclaration of var pd` (tick handler, ×125) and `… var e` (residue-drop death handler, ×42). KubeJS's Rhino engine mis-hoists `const`/`let` declared inside a `try` block in a repeatedly-invoked callback, so **the entire OldCiv discovery / stateful-item / endgame-grant / horror-residue system silently never ran.** Fixed by switching the in-callback declarations from `const`/`let` to `var` (the Rhino-safe idiom) in all four runtime handlers (`PlayerEvents.tick`, `EntityEvents.death`, both `ItemEvents.rightClicked`). Module-level `const`s (which load once and were fine) left as-is.

---

## TerraFirmaCraft — HUD (`config/tfc-client.toml`) — *client display only*

TFC's HUD overhaul is on by default and replaced the vanilla health/hunger bars (continuous TFC bars, health shown as `750 / 1000`), which also shifted the armour row. Reverted the two that map onto vanilla stats:

| Setting | Before | After |
|---|---|---|
| `enableHealthBar` | true | **false** (vanilla hearts) |
| `enableHungerBar` | true | **false** (vanilla hunger) |

**Kept `enableThirstBar = true`** — that bar is the readout for TFC's thirst *mechanic*, so hiding it would leave thirst draining with no gauge. `healthDisplayStyle` is now moot (health bar is vanilla). Client-side display only — no gameplay/balance effect, and per-client (ships in the tracked config for everyone on this instance).

---

## Xaero's World Map — performance (`config/xaero/world-map/…`) — *client*

A client-thread Spark profile flagged `xaero.map.*` (region / MapWriter / MapProcessor / biome) as the dominant **mod** load on the render thread — Xaero continuously scans chunks to write map tiles, which hitches as you explore. Trimmed the map-writing hot path:

| Setting | File | Before | After | Effect |
|---|---|---|---|---|
| `max_loaded_regions` | `world-map/client.cfg` | 300 | **100** | Fewer map regions held/processed in RAM (regions reload from disk when revisited — no data loss) |
| ~~`map_writing_distance`~~ | `world-map/profiles/default.cfg` | -1 | **-1 (reverted)** | Attempted 192, but Xaero rejected it (`not valid for option map_writing_distance` in the log) and fell back to default — reverted to -1 |
| `biome_blending` | `world-map/profiles/default.cfg` | true | **false** | Drops per-pixel biome-edge blending during tile writing (slightly less smooth biome borders on the map) |

**Why:** these three sit directly on the `MapWriter`/`biome` write path Spark flagged, and reduce it without disabling the map. Client-side/per-client, but shipped in the tracked config. *Tunable:* raise `map_writing_distance` if the map fringe stops filling too close; restore `biome_blending`/`max_loaded_regions` for prettier/larger caching at more cost. *Further step if needed:* `update_chunks = false` stops Xaero re-scanning already-mapped chunks (bigger saving, but the map won't auto-update when you change terrain) — left **on** for now.

**Note:** this was a *render-thread* profile — it does not explain the **server-tick** lag (entities/interactions). A `Server thread` profile (`/spark profiler --only-ticks-over 100`) is still needed for that.

---

## KubeJS — automatic dropped-item cleanup (`kubejs/server_scripts/anti_lag_items.js`)

A `Server thread` Spark profile of a **fresh** world (≈11 TPS) showed the tick dominated not by any mod but by **~2,400 `minecraft:item` entities** on the ground (every loose item ticks every tick). The entity census in the profile metadata confirmed it: `minecraft:item` = 2405, everything else ≤122. No config was disabling item despawn, so a high-rate source (very likely a loaded When-Dungeons-Arise dungeon — the census also showed 32 `lootr_minecart` + 24 `tnt_minecart` + 122 skeletons — and/or TFC collapses) was outpacing the 5-minute vanilla despawn.

Added a throttled, threshold-gated cleanup:
| Knob | Value | Meaning |
|---|---|---|
| `ITEM_THRESHOLD` | 400 | only acts when loose items in a player's dimension exceed this (normal play rarely does) |
| `WARN_TICKS` | 200 (10s) | broadcasts a warning before clearing so players can grab keepers |
| `CHECK_INTERVAL` | 300 (~15s) | how often it counts (cheap: one `execute if entity` per interval) |

It counts via `execute if entity @e[type=item]` and, only above the threshold, warns then runs `kill @e[type=item]` in the player's dimension. Below the threshold it does nothing. Uses `var` inside the `PlayerEvents.tick` handler (the Rhino block-scope quirk that broke `oc_interactions.js`).

**Why:** a safety net so runaway item accumulation can't tank TPS again, without touching normal play. *This treats the symptom* — the root source (the mob-dense dungeon / collapse producing items) is still worth locating and lighting up / relocating away from. *Tunable:* raise `ITEM_THRESHOLD` if 400 ever fires during legitimate play; lower it for a tighter cap.

---

## The Forgotten (`config/theforgotten-common.toml`) — *global*

A tension-driven psychological-horror mod (stalkers, apparitions, paranoia events, 3 dimensions, fake-presence scares). Trimmed the intrusive/meta scares to match the pack's "quiet dread > gimmick" line:

| Setting | Before | After | Why |
|---|---|---|---|
| `[dialogue] enabled` | true | **false** | Disables the sign-dialogue system (write on a sign → entity replies) |
| `[chat.creepy] sendChance` | 0.3 | **0.02** | Fake creepy chat lines (incl. the "guest? joined the game" spoof) made *really rare* |
| `[chat.creepy.interval] min / max` | 180 / 420 | **1800 / 3600** | …and the attempt window widened to 30–60 min |

**Fake open-to-LAN spoof — not configurable.** Decompiling the jar, the LAN scare (`LAN_EVENT_TRIGGERED` / `S2COpenToLanPacket` in `ServerEvents`) is a **hardcoded scripted event with no config chance or toggle** — it's independent of `[chat.creepy]`, so it can't be made "rare" via config. It appears to be a limited/tension-gated one-off rather than a frequent roll. Removing it entirely would need disabling the mod or a patch. Left as-is (rare by nature); flagged for the author.

**Mob-spawn limiter — left ON, documented for a decision.** `[mobSpawning.limiter] enabled = true` is a **server-wide natural-spawn interceptor** (`MobSpawnLimiter` hooks the spawn event and returns `DENY`): it blocks *most natural mob spawns across categories*, allows peaceful/animal mobs only ~8% of the time (`[mobSpawning.peaceful] allowChance = 0.08`, minus a `theforgotten:allow_peaceful_spawn` tag exception), and periodically culls excess ambient mobs (bats). It targets **natural** spawns only — spawner/structure spawns still work. **This sits on top of and largely overrides the pack's per-mob spawn tuning** (horror-mob rarity, Alex's Mobs −30%, etc.), so the overworld feels far emptier of normal wildlife/monsters than those configs imply.

**Loosened (not disabled):** `[mobSpawning.peaceful] allowChance` **0.08 → 0.35** — ~4.4× more natural animal/peaceful spawns get through, so the world feels alive again while the limiter still keeps things sparser than vanilla. **Caveat:** the config only exposes the *peaceful* allowance; monster/hostile natural-spawn suppression is hardcoded in `MobSpawnLimiter` with no knob, so hostiles stay heavily reduced (spawner/structure spawns unaffected). To also restore natural hostile spawns, the only lever is disabling the limiter outright. *Further option:* a `theforgotten:allow_peaceful_spawn` tag datapack can whitelist specific animals to always spawn.

---

## Performance-mods batch (tick + multicore) — *global*

A batch of server-tick / multicore performance mods was added (see `modlist.md`, regenerated via `tools/generate_modlist.py` — category rules updated so they bucket under *Core libraries & performance*). Config decisions:

### DimThreads / Dimensional Threading Reforked (`config/dimthread-common.toml`) — *tuned*
Ticks each dimension on its own thread (the one real multicore lever on Forge; well-suited here given the ~43 registered dimensions).
| Setting | Before | After | Why |
|---|---|---|---|
| `default_gamerule_threads` | 3 | **4** | Balanced dimension-thread pool — parallelism for the multi-dimension pack without starving the client/main thread; the mod auto-caps to available CPU cores |
| `ignore_tick_crash` | false | **false (kept)** | The mod flags this as *"very VERY experimental… world corruption"* — left OFF deliberately |

⚠️ **Caveats (unchanged from prior advice):** DimThreads lists known incompatibility with *some Applied Energistics 2 features* (AE2 is installed), and thread-safety is inherently risky in a 285-mod pack. **Back up before serious play** (Simple Backups is present). Existing world: the thread count is a gamerule initialised from the config default above; the crash-isolation gamerule `/gamerule dimthread_skip_crashing true` can keep one bad dimension from downing the session.

### Left at defaults (verified appropriate — no change)
- **Canary** (Lithium-for-Forge) — `canary.properties` is intentionally empty = all optimizations ON. Drop-in; nothing to tune.
- **AI Improvements** (`aiimprovements-common.toml`) — defaults apply only the safe wins (cached-math look controller, AI call-bubbling). All the behaviour-degrading `remove_*` goal toggles are OFF by default; left off (removing them would break mob behaviour — cows not swimming, etc. — for marginal gain, wrong for a survival/horror pack).
- **Mobtimizations** — throttles idle/off-screen mob pathfinding; defaults are perf-positive without altering visible behaviour.
- **Connectivity** — packet/login-size limit fixes (beneficial for a 285-mod handshake) + malformed-traffic blocking; defaults fine.
- **Limited Chunkloading** — unloads chunkloaded chunks 10 min after a player leaves them (helps the idle-dimension baseline); default kept.
- **Structure Essentials** — faster structure lookup + reduced map/locate search radius; notably `disableLegacyRandomCrashes=true` and `warnMissingRegistryEntry=true`, which *harden worldgen against DimThreads' multithreaded RNG* — good synergy; defaults kept.
- **ChunkSending** (5 chunks/tick, dynamic), **Chunky** (pre-gen tool, on-demand), **CoroUtil / Data Anchor / RecipeEssentials** (libraries) — no meaningful config.

### Also now reflected in the modlist
The regenerated `modlist.md`/`modlist.json`/README also picked up previously-untracked mods: **Blood Magic, Mahou Tsukai, Extreme Reactors (+ ZeroCore 2)** — the modlist had been stale.

---

## Appendix A — OpenLoader packs created
`config/openloader/data/`: `MekanismProgression`, `AE2Progression`, `AdAstraSpaceGate`, `OccultismExpensiveRituals`, `WaystonesGate`, `SecurityCraftBalance`, `WeepingAngelsBuff`, `MineColoniesSlowResearch`, `DayLength30`, `HorrorSpawnRarity`, `ExtremeReactorsProgression`, `PerfFixes`.
`config/openloader/resources/`: `QuietAmbience`.

## Appendix B — Known crash (NOT caused by these config changes)
Startup crash traced to a **Mixin injection failure**: TerraFirmaCraft's `BiomeMixin` (`shouldFreezeWithClimate` redirect) fails because **Serene Seasons** (and Cold Sweat's freezing mixin) rewrite the same vanilla biome freeze/precipitation methods. This is a **mod-vs-mod incompatibility** at class-load, before any config/datapack is read — data edits cannot cause or fix it. **Recommended fix:** disable one of the conflicting climate mods (most likely **TerraFirmaCraft** vs **Serene Seasons**). Not yet applied — awaiting your decision.

## Appendix C — Reverting
- Config-file edits: `*.bak` backups sit next to the originals (e.g. `alexsmobs.toml.bak`, `create-server.toml.bak`, `coldsweat/world.toml.bak`, `serversidehorror.json.bak`, `man_config.toml.bak`, `ars_nouveau.bak/`, `alexscaves_biome_generation.bak/`, `extremereactors/common.toml.bak`, `bloodmagic-common.toml.bak`).
- Datapacks: delete the pack folder under `config/openloader/data/` (or `/resources/`).
- Per-world defaults: `defaultconfigs/*.toml`; to re-apply to an existing world, delete that world's `serverconfig/<mod>-server.toml` to regenerate.
