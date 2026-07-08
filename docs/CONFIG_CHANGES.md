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

**Why:** the *From The Fog* mod itself has no config (hardcoded), so this mod provides the tunable distance-sighting horror. More staring/footsteps/sounds without ramping up direct attacks.

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

## Appendix A — OpenLoader packs created
`config/openloader/data/`: `MekanismProgression`, `AE2Progression`, `AdAstraSpaceGate`, `OccultismExpensiveRituals`, `WaystonesGate`, `SecurityCraftBalance`, `WeepingAngelsBuff`, `MineColoniesSlowResearch`, `DayLength30`.
`config/openloader/resources/`: `QuietAmbience`.

## Appendix B — Known crash (NOT caused by these config changes)
Startup crash traced to a **Mixin injection failure**: TerraFirmaCraft's `BiomeMixin` (`shouldFreezeWithClimate` redirect) fails because **Serene Seasons** (and Cold Sweat's freezing mixin) rewrite the same vanilla biome freeze/precipitation methods. This is a **mod-vs-mod incompatibility** at class-load, before any config/datapack is read — data edits cannot cause or fix it. **Recommended fix:** disable one of the conflicting climate mods (most likely **TerraFirmaCraft** vs **Serene Seasons**). Not yet applied — awaiting your decision.

## Appendix C — Reverting
- Config-file edits: `*.bak` backups sit next to the originals (e.g. `alexsmobs.toml.bak`, `create-server.toml.bak`, `coldsweat/world.toml.bak`, `serversidehorror.json.bak`, `man_config.toml.bak`, `ars_nouveau.bak/`, `alexscaves_biome_generation.bak/`).
- Datapacks: delete the pack folder under `config/openloader/data/` (or `/resources/`).
- Per-world defaults: `defaultconfigs/*.toml`; to re-apply to an existing world, delete that world's `serverconfig/<mod>-server.toml` to regenerate.
