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

## Cold Sweat (`config/coldsweat/…`) — *REMOVED*
> **⚠️ Cold Sweat was removed from the pack** (jar disabled, along with its `create_cold_sweat` compat addon). Everything below **no longer applies** and is kept only as a historical record. Cold is **no longer a survival threat**; the README's climate framing was updated to match. The leftover `config/coldsweat/` folder is now inert. *(TerraFirmaCraft still provides a world climate/seasons system, but not Cold Sweat-style player hypothermia damage.)*

**Goal (obsolete):** cold is the main threat (nights, rain, mountains); heat only in deserts/lava; **everything in Celsius**.

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

### What the mod actually does (read from the 1.4 jar, 2026-11-06)
- **Spawn timer:** one world-wide timer (`timer_global`) that every player advances by one per tick **while within 2 blocks of the surface heightmap**; underground players don't count. When it passes a random target between `min_spawn_rate` and `max_spawn_rate`, the Man is summoned in an Overworld biome, day or night (`spawn_at_day = true`), and moved 35–99 blocks from the player with `spreadplayers`. Because every surface player advances the same timer, three players outside bring him about three times as often. Nothing spawns if one is already within 200 blocks.
- **On spawn:** a random spawn sound, and the player is granted `man:feelingwatched` ("Feeling Watched", description "Man spawning first time"). The shipped advancement is its own tab with `show_toast` and `announce_to_chat` true, so it told everyone he had arrived.
- **Stalk, then chase:** looking at him (an unobstructed ray within 300 blocks) turns him into the aggressive form, which targets you and gives Darkness for `darkness_effect_duration`. The chase lasts `min/max_chase_duration`; he climbs, swims fast, breaks blocks up to `block_hardness` (`block_hardness_search` while searching), calls lightning (`lightnings`), and vanishes if you get `vanish_distance` away.
- **Death:** a full-screen jumpscare (`jumpscare`). Killing him drops the Tear of the Man and grants `man:kill` (+100 XP, announced). Dying to him with a book and the Tear nearby yields the Fog Speed / Fog Protection enchantments.
- **Other:** you can't sleep with him within 200 blocks ("You can't sleep..." plus 0.1 damage); `man:mftfhang`, a hanging variant at weight 10 in every biome, spawns under tree canopy at night and turns to face players within 100 blocks; a cultist `man:house` structure (spacing 60, any non-water biome) with a Cultist villager profession; once per world the first player to join gets a Ko-fi chat advert and Cassette 1 (not configurable). Commands live under `/man` (`spawn`, `timer`, `reset_timer`, `jumpscare_screen`, `debug`).

### "Feeling Watched" silenced (2026-11-06) — `HorrorQuietAdvancements` OpenLoader datapack — *global*
`data/man/advancements/feelingwatched.json` is overridden with the same `minecraft:impossible` criterion and **no `display` block**, so the advancement is still granted (the mod's spawn code does not null-check it, so deleting it could throw in the entity tick) but shows no toast, no chat line and no advancement tab. Players who already have it keep it invisibly. `man:kill` and the two enchantment advancements are left as they are. Takes effect on the next world load or `/reload`. Needed on every server that runs the mod (Server v2, the VM server) and in "The Correction".

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
| Whispering Spirits | `whispering_spirits:whispering_spirit` | 20 | **2** (~10× rarer) | any | *(added 2026-07-09 — also a top server-tick cost; see the Spark-profile section)* |
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

### Follow-up (2026-07-09) — extended to the `creepy_1`–`creepy_4` features
The catacomb fix was **incomplete**: post-fix logs (2026-07-09 sessions) still recorded the same `Detected setBlock in a far chunk` cascade, now from `more_underground_structures:creepy_1_feature` (12×) and `creepy_4_feature` (9×) during exploration. These are the same oversized-building class as catacomb — multi-chunk structures injected via `forge:add_features` at the `underground_structures` step. Added `forge:none` overrides for **`creepy_1`, `creepy_2`, `creepy_3`, `creepy_4`** (only 1 & 4 were observed in logs; 2 & 3 disabled proactively as the same structure family). Ore veins/blobs, spikes, fossils, and the small chest-dungeon features remain untouched. *Monitor:* if new far-chunk errors name other MUS features (e.g. the `big*` hideouts, tombs, shrines, gateways, wells), disable those the same way.

> **Note — this is not the whole lag story.** The worldgen cascade explains the *exploration* spikes. The **worst** stalls in the 2026-07-09 logs were *progressive* (7 s → 65 s over ~6 min) and coincide with `Saving oversized chunk [-4, 66] (1.4–1.6 MB) to saves/Bruh/entities/…` — a runaway **entity accumulation** at Bruh chunk **[-4, 66]** (block ~x=-64, z=1056). That is a separate, likely-dominant cause and is **not** addressed by this datapack. See the entity-accumulation note below.

---

## Performance — entity accumulation at `Bruh [-4, 66]` (*diagnosis — no fix applied yet*)

**Symptom:** the largest server-tick stalls on 2026-07-09 grew progressively (up to **1314 ticks / 65 s behind**) and coincided with repeated `Saving oversized chunk [-4, 66] (1.48 MB → 1.64 MB) to saves/Bruh/entities/c.-4.66.mcc`. A 1.6 MB **entity** chunk = thousands of entities piled in one spot (block ~x=-64…-49, z=1056…1071 in the **Bruh** world). Every entity ticks every tick, so the pile drags the whole server thread.

**Likely source:** consistent with the earlier anti-lag Spark census (lootr_minecart + tnt_minecart + skeletons) — most probably a loaded **When Dungeons Arise** dungeon (or other mob-dense structure) at that location, spawning faster than mobs despawn.

**Why the existing safety net doesn't catch it:** `kubejs/server_scripts/anti_lag_items.js` only runs `kill @e[type=item]` — it clears **loose items only**. A pile of **mobs/minecarts** is untouched by it.

**Safety net added (`anti_lag_mobs.js` + `AntiLagEntities` datapack):** a companion to `anti_lag_items.js` that culls **runaway hostile mobs** (the item script only handles `@e[type=item]`; a pile of mobs sailed past it). Same warn-then-clear, throttled, threshold-gated design.
- **Whitelist-only:** culls only entity types in the `#antilag:cullable_hostiles` tag (common vanilla hostiles: zombie/husk/drowned/zombie_villager, skeleton/stray, spider/cave_spider, creeper, silverfish, endermite, witch, phantom, slime). Everything else — pets, villagers, animals, **minecarts (incl. Lootr/TNT dungeon loot)**, item frames, armor stands, paintings, boats, bosses, and all modded/horror mobs — is untouched by omission.
- **Persistence-protected:** `nbt=!{PersistenceRequired:1b}` spares name-tagged mobs, geared mobs, spawner-persistent mobs, and raid mobs.
- **Threshold:** `MOB_THRESHOLD = 200` cullable hostiles per dimension (well above normal, since the pack suppresses natural hostile spawns), 10 s warning, ~15 s check interval.
- *Tunable:* edit the knobs at the top of `kubejs/server_scripts/anti_lag_mobs.js`; extend the whitelist by editing `config/openloader/data/AntiLagEntities/data/antilag/tags/entity_types/cullable_hostiles.json`. *Revert:* delete the script + the `AntiLagEntities` datapack.

**Still worth doing (targeted, needs in-game):** visit Bruh **~x=-64, z=1056** to see what's accumulating (dungeon? spawner? mob crowd? loot minecarts?). If it's a spawner, light/remove it; if it's structural loot minecarts, the cull won't (and shouldn't) touch them — relocate or accept. The safety net caps *mobs*, not structural entities.

---

## Performance — Spark server-thread profile (2026-07-09, https://spark.lucko.me/aYH6AP5pvl)

A server-thread Spark profile was captured and parsed (224 s of samples). Ranked by self-time:

| Rank | Frame | Self-time | Notes |
|---|---|---|---|
| 1 (mods) | `whisperingspirits…PlayerLookAtSpiritProcedure.onPlayerTick` | ~10.7 s (**~4.8%**) | **#1 mod cost.** Runs every tick per player, scanning nearby spirit entities. |
| — | `net.minecraft.server.commands.TeleportCommand` (+lambda) | ~8.7 s (**~3.9%**) | Heavy programmatic teleports — almost certainly Whispering Spirits repositioning its spirit entities each tick. |
| — | vanilla `MinecraftServer` / `BlockableEventLoop` / `Level` / `ServerChunkCache` / `Entity` | ~40%+ combined | Engine baseline + idle/wait for a 285-mod pack; not a single fixable hog. Entity ticking ≈3.7%. |

Other MCreator horror procedures were **minor** in this sample (`net.theobsessed` ~0.1%, John mod / Weeping Angels negligible) — Whispering Spirits is the outlier.

**Root cause of the WS cost:** it spawned at **weight 20 in `forge:any` biome** — vs. the pack's documented horror-mob benchmark of **weight 2** (Weeping Angels, Rake, TOWW, Wendigo). So ~10× too many spirit entities existed everywhere, and its per-tick look-detection + teleport procedure processed all of them. WS ships **no config file** (only a spawn biome-modifier), so it can't be tuned directly.

**Fix (`HorrorSpawnRarity` datapack):** override `whispering_spirits:whispering_spirit_biome_modifier` to **weight 20 → 2**, mirroring the exact pattern/precedent used for Rake/TOWW/Wendigo (see the horror-spawns table above). ~10× fewer spirits ⇒ proportionally cheaper per-tick procedure and far fewer teleports — the single biggest systematic tick win available without removing a mod. Worldgen-independent; takes effect on next world load. *Tunable:* raise the weight for more spirits / heavier tick cost. *If still too heavy:* WS would have to be removed, as it has no other lever.

> **Relationship to the entity-pile note above:** these are distinct. The Spark aggregate shows the *systematic* per-tick drain (Whispering Spirits). The oversized-entity-chunk stalls are *episodic* spikes at one location. Both are now addressed — WS rarified here, runaway mobs capped by `anti_lag_mobs.js`.

---

## KubeJS — Old Civilisation script bugfix (`oc_interactions.js`)

`logs/latest.log` showed **167** repeated errors from `oc_interactions.js` — `InternalError: TypeError: redeclaration of var pd` (tick handler, ×125) and `… var e` (residue-drop death handler, ×42). KubeJS's Rhino engine mis-hoists `const`/`let` declared inside a `try` block in a repeatedly-invoked callback, so **the entire OldCiv discovery / stateful-item / endgame-grant / horror-residue system silently never ran.** Fixed by switching the in-callback declarations from `const`/`let` to `var` (the Rhino-safe idiom) in all four runtime handlers (`PlayerEvents.tick`, `EntityEvents.death`, both `ItemEvents.rightClicked`). Module-level `const`s (which load once and were fine) left as-is.

**Verified fixed (2026-07-09):** the current session's log shows **0** `redeclaration of var` errors, so the interaction/variant/residue system now runs.

---

## KubeJS — Old Civilisation loot coverage (`oc_loot.js`) — *modded-structure gap*

**Report:** "custom lore items don't seem to spawn in loot chests." Traced end-to-end:
- **Not a code bug.** Items register fine; `oc_interactions.js` is fixed (above); and `oc_loot.js`'s LootJS chain (`addLootTableModifier(...).randomChance(...).addLoot(...)`) *is* valid for LootJS **2.13.1** — the returned `LootActionsBuilderJS` implements the `LootConditionsContainer` + `LootActionsContainer` interfaces, so `randomChance`/`addLoot` are inherited default methods. (The script's own try/catch never logged a failure, confirming no runtime error.)
- **Root cause = coverage gap.** `oc_loot.js` originally injected only into **vanilla** `minecraft:chests/*` tables (+ `ad_astra_more_structures` space chests + `born_in_chaos_v1`/`foes` mob drops). But this pack's headline structures ship their **own** loot tables, which were never targeted: **When Dungeons Arise** (168 tables, `dungeons_arise:chests/*`), **YUNG's Better Strongholds / Desert & Jungle Temples / Ocean Monuments / Witch Huts** (own `better*:chests/*`), **More Underground Structures** (`more_underground_structures:chests/*`), **Lost Architects** (`lost_architects:chests/*`). Only YUNG's Better **Dungeons** & **Mineshafts** were covered — they reuse the vanilla `simple_dungeon`/`abandoned_mineshaft` tables. Combined with the intentionally-low per-item chances, players exploring the pack's real content saw effectively nothing.

**Fix:** extended `oc_loot.js` with LootJS regex modifiers routing each modded structure family to fitting item categories, at **slightly lower chances than the vanilla equivalents** (horror-pack "rare finds"):
- **When Dungeons Arise** — routed by structure theme: undead/plague/infested → horror (biological_sample, impossible_bone, corrupted_map, distorted memory); settlements/homes/camps → personal (data chip, photo, currency, handbook); keeps/towers/forts → military-personal + technical (id_badge, incident_report, currency); foundry/mechanical/mines → technical (incident_report, audio_log, containment_label, signal_device); monastery/heavenly/mythic → arcane (arcane_notes, distorted memory, bound_thought, unstable_memory).
- **YUNG's Better Strongholds** → magic + technical; **Desert/Jungle Temples** → ancient/magic; **Ocean Monuments** → ocean; **Witch Huts** → magic/horror.
- **More Underground Structures** → underground-dungeon (personal, technical, faded memory, biological_sample).
- **Lost Architects** → artificer's workshop = technical/arcane; brawler's guild = personal/library.

**Deliberately skipped:** YUNG's Better **Nether Fortresses** (`betterfortresses`) — TFC disables nether portals in this pack (`enableNetherPortals=false`), so they're unreachable. YUNG's Dungeons/Mineshafts already covered via vanilla tables. *(Superseded 2026-11-01: TFC is removed and the nether is reachable again, so Better Nether Fortresses chests are now an un-routed gap — listed in `UPDATE_REVIEW_2026-11-01.md` §6.)*

*Scope:* server script, takes effect on world load / `/reload`. All 17 referenced item IDs verified to have models; script passes `node --check`. *Tunable:* the per-line `randomChance` values in `oc_loot.js`. *Note:* loot is baked when a chest is first rolled, so this affects **newly generated / not-yet-opened** chests, not ones already looted. (Lootr is compatible — LootJS modifies the table itself, which Lootr then rolls per-player.)

---

## TerraFirmaCraft — HUD (`config/tfc-client.toml`) — *client display only*

TFC's HUD overhaul is on by default and replaced the vanilla health/hunger bars (continuous TFC bars, health shown as `750 / 1000`), which also shifted the armour row. Reverted the two that map onto vanilla stats:

| Setting | Before | After |
|---|---|---|
| `enableHealthBar` | true | **false** (vanilla hearts) |
| `enableHungerBar` | true | **false** (vanilla hunger) |

**~~Kept `enableThirstBar = true`~~ → now `false` (2026-07-09).** The thirst bar was originally kept because it was the readout for TFC's thirst *mechanic*. Since thirst has now been **disabled** (see next section), the static bar was hidden too. `healthDisplayStyle` is moot (health bar is vanilla). Client-side display only.

---

## TerraFirmaCraft — disable thirst + lessen hunger (`tfc-server.toml` + `tfc-client.toml`) — *per-world → defaultconfigs (+ client)*
**Goal:** remove the thirst survival layer entirely and make hunger drain more slowly.

| Setting | File | Before | After | Effect |
|---|---|---|---|---|
| `thirstModifier1` | `tfc-server.toml` `[mechanics.player]` | 5.0 | **0.0** | Thirst never drains (`0 = None` per TFC) — no dehydration, ever |
| `enableThirstOverheating` | `tfc-server.toml` | true | **false** | Belt-and-suspenders: no extra thirst loss in heat (moot at modifier 0) |
| `enableThirstBar` | `tfc-client.toml` | true | **false** | Hides the now-static thirst bar from the HUD |
| `passiveExhaustionMultiplier` | `tfc-server.toml` | 1.0 | **0.5** | Hunger accrues at **half** rate (~1 hunger bar per 5 days instead of 2.5) |

**Why:** requested — thirst removed as a mechanic (`thirstModifier1 = 0` is TFC's documented off switch; the bar is hidden since it would just sit full), and hunger pressure roughly halved rather than removed (set `passiveExhaustionMultiplier` to `0` to disable hunger entirely, or higher toward 1.0 for more pressure).

**Scope/apply:** thirst/hunger are per-world server config — edited in the current world (`saves/Bazinga/…`) and `defaultconfigs/tfc-server.toml` for new worlds; the bar toggle is global client config. Backups: `tfc-server.toml.bak` (both), `tfc-client.toml.bak`. Active on next world load (server) / game restart (client HUD). *Revert:* restore the `.bak`s or reset the four values.

---

## TerraFirmaCraft — food hunger by type (`TFCFoodHunger` datapack) — *global*
**Goal:** undo TFC's flat "every food restores 4 hunger (2 shanks)" — scale hunger by food type so cooking real meals is worthwhile, while keeping TFC's nutrition-category system intact.

**Mechanism (not a config lever):** TFC sets hunger per-food in `data/tfc/tfc/food_items/*.json` (`"hunger": 4` on every one — 170 defs). There's no global multiplier, so this is a datapack overriding those defs at the same path (same technique as `TFCLightItems`/`PerfFixes`). **Only the `hunger` field is changed** — `saturation`, `decay_modifier`, and the `protein`/`grain`/`vegetables`/`fruit`/`dairy` nutrition values (which drive max-health) are preserved exactly.

| Food tier | Detection | Hunger | 
|---|---|---|
| Cooked meats/fish + prepared meals (soup, stew, salad, sandwich) | `cooked_*` or meal keyword | **8** |
| Bread | `*bread*` (not dough/flour) | **6** |
| Raw meat/fish, raw fruit/veg | has `protein` / `fruit` / `vegetables` | **5** |
| Grain / flour / dough (processing intermediates) | grain keywords | **4** (TFC default — left unchanged) |

**Result:** 146 overrides written (57 → 8, 6 → 6, 83 → 5); the 24 grain/flour/dough intermediates keep the default 4 (no file needed). Cooking a meat or making a meal now restores 4× a raw grain and 2× a raw ingredient, preserving the "cook real food" incentive.

**Scope/apply:** global datapack, active on world load / `/reload`; all 146 files validated. *Note:* food data is applied live (not baked into worldgen), so it affects all food immediately. *Tunable:* re-run with different tier values, or edit individual `food_items/*.json` in the pack. *Revert:* delete `config/openloader/data/TFCFoodHunger/`.

---

## TerraFirmaCraft — modded-food edibility + food decay off (`TFCModdedFood` datapack + `tfc-server.toml`) — *global (+ per-world → defaultconfigs)*
**Goal:** make non-TFC (modded) foods actually restore hunger, and stop food rotting / not stacking.

**Root cause (decompiled):** TFC swaps the player's food data for `TFCFoodData`, whose `eat()` calls `FoodCapability.get(stack)` and **returns early if the item has no TFC food definition**. So every modded/undefined food (Rationcraft cans, Farmer's Delight, Soldier's Delight, Candlelight, Farm & Charm, …) restores **zero** hunger. Hardcoded — no config toggle; the only fix is to give those items a TFC food definition.

**`TFCModdedFood` datapack:** one food def (`data/tfc/tfc/food_items/modded_catchall.json`) matching a custom aggregate tag `#tfcmoddedfood:nourishing` → `hunger 4, saturation 1.0`. That tag unions `#forge:foods` plus the Rationcraft/Voidless food tags (`#rationcraft:cans`, `#rationcraft:milk`, all `#voidcans:*`, `#voidless_mre:*`). TFC's own foods are **not** in `#forge:foods`, so their tuned defs aren't shadowed (`getDefinition` would otherwise return whichever matches first).

**`foodDecayModifier` 1.0 → 0.0** — set in **all four** `tfc-server.toml` (instance + server `defaultconfigs/`, plus the live worlds `saves/Bazinga/serverconfig/` and server `world/serverconfig/`). Disables food decay entirely; per TFC's own note this also makes food **lose creation dates → stack freely** (fixes the "food won't stack" complaint). Required so the catch-all defs don't turn modded food into rotting, freshness-split TFC food. **Irreversible for existing food** — creation dates are dropped and can't be restored.

**Why:** consistent with the pack's TFC-survival trims (thirst off, hunger halved, item sizes neutralized). Modded food now nourishes and stacks; nothing rots. *Tunable:* values in `modded_catchall.json` (add TFC nutrients if you want modded food to build max-health nutrition, not just fill hunger). *Coverage caveat:* the **opened**-can variants (`open_*`/`h_open_*`, reached only via the can-opener recipe) aren't tagged, so they're uncovered — but with Rationcraft's `Edible Cans = true` you eat sealed cans directly, which are covered. Add them to `nourishing.json` if you use the opener path. *Apply:* world load / `/reload`. *Revert:* delete `config/openloader/data/TFCModdedFood/` (decay dates already lost regardless).

---

## TerraFirmaCraft — collapses & landslides (`tfc-server.toml`) — *per-world → defaultconfigs*
**Goal:** stop TFC's block-gravity mechanics from tearing apart world-gen **floating structures** (e.g. skyships), where breaking a single block cascades the build apart.

TFC has no concept of "intended build" — it applies geology physics to any qualifying block. Two mechanics live in `[mechanics.collapses]`; both were fully disabled via their `enable*` gates (chance/radius values left as-is, since the toggles gate the behaviour entirely):

| Setting | Before | After | What it does |
|---|---|---|---|
| `enableBlockLandslides` | true | **false** | Gravity blocks (TFC dirt/gravel/sand/cobble) fall on block update — **the skyship culprit** |
| `enableBlockCollapsing` | true | **false** | Raw-rock collapses when mining unsupported stone |
| `enableExplosionCollapsing` | true | **false** | Explosions trigger immediate raw-rock collapses |
| `enableChiselsStartCollapses` | true | **false** | Chiselling raw rock can start a collapse |

**Why:** the landslide toggle is the direct fix for floating structures (breaking a block no longer slides the rest away); the three collapse toggles are disabled alongside it so mining/explosions/chisels can't drop rock on builds or players either. Removes TFC's signature mining hazard — acceptable trade to keep structure integrity. As a side benefit, this also removes collapse/landslide item-spam, one of the loose-item sources flagged in the *anti-lag* pass above.

**Scope/apply:** edited in both existing worlds (`saves/New World/…`, `saves/Bruh/…`) and copied to `defaultconfigs/tfc-server.toml` for every new world. Backups: `tfc-server.toml.bak` beside each world file. *Revert:* restore the `.bak`, or set the four toggles back to `true`; delete `defaultconfigs/tfc-server.toml` (or flip its toggles) for new worlds. *Tunable:* re-enable collapses alone (keep landslides off) if you want the mining hazard back without the structure damage.

---

## TerraFirmaCraft — item size & weight (`TFCLightItems` datapack + `TFCNoSizeTooltip` resource pack) — *global*
**Goal:** neutralize TFC's item Size/Weight system — every item stacks to 64, no storage/overburden restrictions, and no Size/Weight tooltip clutter.

**Not config-toggleable.** There is no flag for this in any `tfc-*.toml`. The system is code- and data-driven (`net.dries007.tfc.common.capabilities.size.ItemSizeManager`): an item's size/weight comes from a data definition (`data/tfc/tfc/item_sizes/*.json`) if one matches, else from hardcoded defaults (`ARMOR_SIZE`/`TOOL_SIZE`/`BLOCK_SIZE`/`DEFAULT_SIZE`). Weight → max-stack mapping is baked into the `Weight` enum (`very_light`=64 … `very_heavy`=4) and can't be redefined by data. The tooltip line (`addTooltipInfo`) is added unconditionally — no toggle removes it.

**`TFCLightItems` datapack** (`config/openloader/data/`): overrides all **56** built-in `item_sizes/*.json` at the same path, keeping each original `ingredient` but forcing `size: tiny`, `weight: very_light`. Effect: every TFC-defined item (ingots, ore, food, tools, blocks, vessels…) stacks to **64** and is small enough to bypass vessel/storage size gates and the Overburdened effect. (Armor/tools are already stack-1, so their stack is unchanged; the override removes their *storage* restriction where a data def covered them.)

**Added defs — `planks` + `lumber` (2026-07-10).** TFC ships **no** `item_sizes` def for planks or lumber, so both fell through to the code default weight **LIGHT = stack 32** (the "some woods only stack to 32" report). Added two *new* defs at the same path forcing `size: tiny, weight: very_light` → stack **64**: `planks.json` (`#minecraft:planks`, which includes all `tfc:wood/planks/*`) and `lumber.json` (`tfc:lumber`). Logs/bark/stripped were already covered via `logs.json` (`#minecraft:logs`, weight → 64). This is exactly the "explicit tag override" escape hatch flagged in the caveats below. Datapack now ships **56 built-in overrides + 2 added defs**.

**`TFCNoSizeTooltip` resource pack** (`config/openloader/resources/`): blanks the 12 `tfc.enum.size.*` / `tfc.enum.weight.*` lang keys to `""`, so the Size/Weight **wording** disappears from the tooltip on **every** item — including code-default items like armor that the datapack can't reach.

**Why:** this is the closest achievable to "disable it" without patching the jar. The datapack kills the *mechanic* (small stacks, storage limits, overburden); the resource pack kills the *display text*.

**Caveats / limits:**
- The tooltip **line** is hardcoded, so blanking the lang may leave a thin empty line — the *text* is gone but the line itself can't be fully removed by a resource pack.
- Blanking those enum keys also empties the words wherever else they appear (e.g. the TFC field guide's size/weight references).
- Code-default items (armor, and any modded item not matched by a data def) keep their underlying code-assigned size for *storage* purposes — only their tooltip text is blanked. Fully neutralizing their storage behaviour would need explicit tag overrides in `TFCLightItems`.
- A true, complete removal (line and all) would require a mixin/jar patch — out of scope for the config/datapack methodology.

**Scope/apply:** global (all worlds); active on next world load (datapack) / game restart (resource pack). English-only lang blanking (`en_us`); add other `*_us`/locale files if needed. *Revert:* delete `config/openloader/data/TFCLightItems/` and `config/openloader/resources/TFCNoSizeTooltip/`.

---

## TerraFirmaCraft — restore vanilla crafting table (`TFCVanillaCraftingTable` datapack) — *global*
**Goal:** craft a crafting table the vanilla way (4 planks in a 2×2), reverting TFC's early-game gate.

TFC **disables** the vanilla recipe — it ships `data/minecraft/recipes/crafting_table.json` as `{"conditions":[{"type":"forge:false"}]}` (a permanently-false Forge condition) — and replaces it with `data/tfc/recipes/crafting/vanilla/crafting_table.json`, a `tfc:damage_inputs_shapeless_crafting` requiring a **saw** (`#tfc:saws`) **+ an existing workbench** (`#tfc:workbenches`). That's a chicken-and-egg gate on your first table.

**Fix (`TFCVanillaCraftingTable` datapack):** overrides `data/minecraft/recipes/crafting_table.json` at the same path with the stock vanilla shaped recipe (`##`/`##` of `#minecraft:planks` → `minecraft:crafting_table`). OpenLoader datapacks load over mod-jar data (same mechanism as `TFCFoodHunger` / `TFCLightItems` overriding TFC's own data), so the vanilla recipe wins. TFC's saw+workbench recipe is **left in place** as an additional path (both now craft a table); add an override deleting `data/tfc/recipes/crafting/vanilla/crafting_table.json` if you want vanilla-only.

**Scope/apply:** global datapack; active on world load / `/reload`. JSON validated. *Revert:* delete `config/openloader/data/TFCVanillaCraftingTable/`.

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

⚠️ **Caveats (unchanged from prior advice):** DimThreads lists known incompatibility with *some Applied Energistics 2 features* (AE2 is installed), and thread-safety is inherently risky in a 285-mod pack. **Back up before serious play** (Simple Backups is present). Existing world: the thread count is a gamerule initialised from the config default above; the crash-isolation gamerule `/gamerule dimthread_skip_crashing true` can keep one bad dimension from downing the session. **2026-11-02:** Immersive Portals is also not thread-safe under DimThreads (its chunk-ticket flush assumes the server thread); bridged by the pack's own `immptl_dimthread_compat` shim — see "Immersive Portals × DimThreads" below.

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

## KubeJS — Sanity mechanic (`kubejs/server_scripts/oc_sanity.js` + `OldCivSanity` datapack)
**Full design & technical spec: `docs/sanity_mechanic.md`.** A hidden, per-player sanity value (0–100, baseline 100, in `persistentData`) that makes the world *respond to the player's exposure to it* — the lore's premise as a mechanic. **Invisible** (no bar/number; deliberately consistent with the reverted-to-vanilla HUD), **suppression-only**, and **spawns nothing**.

**Drains** (per 2 s, throttled tick): darkness (light < 4), depth (overworld Y < 0, scaling to −40), horror-mob within 16 blocks, and self-feedback from its own events. **Restores:** firelight (light ≥ 10), daylight + open sky, sleep (fast), and two **clarity consumables** — `sedative_ampoule` (flat restore, consumed) and `familiar_photograph` (smaller, cooldown'd) — registered through the existing Old Civilisation item/tooltip/loot pipeline.

Two consequence channels, both keyed off four bands (Calm/Unease/Dread/Breakdown, with hysteresis):
- **Channel A — ambient events:** sounds / hidden `nausea`+`darkness` / positional cues, more frequent and intense as sanity falls; zero at full. A single diegetic italic line prints on descent into a worse band (the only way an invisible stat announces itself).
- **Channel B — natural-spawn suppression:** `EntityEvents.checkSpawn` cancels tagged horror spawns with probability `(sanity/100)^gamma`. **Full sanity ≈ 0 % horror; zero sanity = the pack's tuned base rate, untouched.** It can only *reduce* below the authored base rate, never exceed it — so it cannot undo the pack's rarity work. Reaches the **biome-weighted** horrors (`HorrorSpawnRarity` set: Rake, TOWW, Wendigo, Whispering Spirits, Pale Hound; Weeping Angels); the **timer-summoned** ones (Mimic, Cave Dweller, Man From The Fog) bypass `checkSpawn` by design and keep their tuned cadence.

**Suppression / proximity target list:** `config/openloader/data/OldCivSanity/…/entity_types/horror_mobs.json` (modded ids use `required:false`). **All tunable** via one `CONFIG` object at the top of `oc_sanity.js` (`spawn.enabled=false` keeps ambient-only; `spawn.gamma` sets how hard high sanity holds horror back; `debug=true` enables gated/sampled tracing).

**Command:** `/sanity` (self) and `/sanity <player>` (op-gated, permission 2) print `sanity NN.N/100 [Band]` — the intended in-game way to check a level.

**Logging (docs §15):** `[OldCiv/Sanity]` prefix; load-time confirmation of channels + config; **log-once** failure suppression (the direct antidote to the 167×-error pattern) with **graceful per-accessor degradation** (a broken `getLight`/`isSleeping`/`checkSpawn` disables only its own sub-feature and logs one WARN). Marked `[verify live]` in the spec: `player.block.getLight()`, `isSleeping()`, `EntityEvents.checkSpawn` cancelability, `PlayerEvents.respawned`, and the `commandRegistry` arg API — each degrades safely if the accessor differs on this build.

**Scope/apply:** server + client + startup scripts (active on world load / `/reload`, restart for the client tooltip); datapack global. All 6 touched JS files pass `node --check`; all new JSON validated. *Revert:* delete `oc_sanity.js`, the two clarity items from `oc_items.js`/`oc_creative.js`/`oc_tooltips.js`/`oc_loot.js`, the `OC_NON_LORE` guard in `oc_interactions.js`, the two model JSONs, and `config/openloader/data/OldCivSanity/`. Leftover `oc_sanity*` persistentData keys are inert.

---

## TerraFirmaCraft — hunger back to (essentially) vanilla (2026-07-12) — *kubejs + tfc-server.toml, both sides*
**Goal:** foods from any mod restore their **vanilla** hunger/saturation; hunger drains like vanilla; flat 20 max health. Requested after modded foods restored nothing.

**Root cause of "food does nothing" (server log, 20:16):** `TagLoader ERROR — Couldn't load tag tfcmoddedfood:nourishing as it is missing following references: #forge:foods`. No mod in the pack defines `#forge:foods`, and in 1.20.1 one missing required reference voids the **whole tag** → the `TFCModdedFood` catchall matched zero items → TFC's `TFCFoodData.eat()` (which returns early for items without a TFC food def) restored nothing for every non-TFC food. The shim was broken since creation.

| What | Where | Old | New | Why |
|---|---|---|---|---|
| `TFCModdedFood` pack | `config/openloader/data/` | active (broken) | **retired** → `data-disabled/` | Dead tag; superseded below |
| Per-item food defs | `kubejs/server_scripts/tfc_vanilla_food.js` | — | **new** | Generates a TFC food def for **every edible item** in the registry (skips `tfc:` + the 2 minecraft items TFC defines): `hunger = getNutrition()`, `saturation = nutrition × satModifier × 2`. TFC applies defs as `delegate.eat(hunger, sat/(2×hunger))` (verified in 3.2.23 bytecode) → **exact vanilla restore values**, no tags to break |
| `passiveExhaustionMultiplier` | `tfc-server.toml` | 0.5 | **0.0** | Vanilla has no passive hunger drain — drain now comes from activity only |
| `naturalRegenerationModifier` | `tfc-server.toml` | 1.0 | **3.0** | TFC regen (0.2 HP/s base, 3× when hunger>80%) → 0.6–1.8 HP/s ≈ vanilla pace. (TFC forces `naturalRegeneration=false` and replaces regen; this is the lever) |
| `nutritionMin/Default/MaxHealthModifier` | `tfc-server.toml` | 0.2 / 0.85 / 3.0 | **1.0 / 1.0 / 1.0** | Flat vanilla 20 max HP — nutrition no longer scales health (generated defs carry 0 nutrients, so this also prevents nutrition-decay health loss) |

**Known residual (hardcoded, no config):** TFC's `TFCFoodData.addExhaustion()` scales all vanilla activity exhaustion by **0.4×** — hunger drains from activity at ~40% vanilla rate. To add pressure back, raise `passiveExhaustionMultiplier` (e.g. 0.5–1.0).

**Kept:** `TFCFoodHunger` tier pack (TFC's own foods at 5/6/8 hunger — vanilla-scaled), `thirstModifier1 = 0` (no thirst), `foodDecayModifier = 0` (no rot — vanilla has none).

**Scope/apply:** TOML edited in instance `defaultconfigs` + both `saves/*/serverconfig` + server `defaultconfigs` (server had no world at edit time — next world seeds from defaults). Script synced instance ↔ server. Active on next boot; `/reload` refreshes the generated defs on a running server (TOML needs world load). *Revert:* move `TFCModdedFood` back from `data-disabled/`, delete the kubejs script, restore the five TOML values.

---

## 2026-11-01 — Mod-update review (≈80 mods updated, 13 jars removed) — *client instance only*
Full write-up with evidence and the open decisions: **`docs/UPDATE_REVIEW_2026-11-01.md`**. The dedicated server was **not** changed in this pass.

### Corrections to earlier sections
- **Day Length (`DayLength30`) — never worked.** Every log since July shows `Failed to load function spooky_daylen:tick` (TFC replaces `/time add <n>` with its own syntax, so the function fails to compile) followed by `Couldn't load tag minecraft:tick … (from data/DayLength30)`. A tag with a missing reference is dropped whole, so **`wa_buff:tick` (WeepingAngelsBuff) never ran either**. The day length actually in effect came from Simple Day Length Extender (2.0×/2.0× + TFC auto-adjust ≈ 60-minute day). Pack retired to `config/openloader/data-disabled/DayLength30/`; `simpledaylengthextender-common.toml` now `dayLengthMultiplier = 1.5`, `nightLengthMultiplier = 1.5`, `autoCalendarAdjustments = false` → 36000 real ticks per day = **30 min**, sleeping skips the night, and the Weeping Angels buff tick loads again.
- **TerraFirmaCraft world generation has never been active.** All nine worlds on disk (three in `saves/`, six in `simplebackups/`) generate `minecraft:overworld` with `minecraft:noise` + the vanilla/Terralith multi-noise biome source; region data is vanilla stone/deepslate, Terralith cave biomes and Mekanism/IE/Create ores. TFC's `tfc:overworld` preset was never selected. **Removed the same evening** — see "Mod set changes" below.
- **The Forgotten** section above is historical: the jar was removed 2026-11-01 (it was already disabled per `SERVER_MODS.md`). The remaining surface-monster suppressor was TFC's `enableVanillaMonstersOnSurface = false` (fixed below).

### MineColonies 1.1.1231 → 1.1.1301 — `defaultconfigs/minecolonies-server.toml` + `MineColoniesSlowResearch`
Raid keys were renamed; the new world seeded `raidDifficulty = 5` (mod default), silently dropping the documented 8. Added the new keys with the documented values (`enablecolonyraids = true`, `raidDifficulty = 8`, `maxRaiders = 80`, `raidersbreakblocks = true`, `raidersbreakdoors = true`, `pathNodeLimitMultiplier = 1.0`) alongside the old ones (each version discards the keys it doesn't know, so the one file seeds either server version). Live `saves/New World (2)` fixed 5 → 8. `pathfindingmaxthreadcount` no longer exists in 1301.
1301 also added 11 mounted-combat/barding researches; scaled with the documented rule (×2 tiers 1–2, ×3 tiers 3–4, ×4 tiers 5–6, cap 1024) → pack now 212 files. All 201 existing overrides still match jar paths and the jar's versions are byte-identical to 1231.

### Weeping Angels 46.1.5 → 46.1.6
- `config/weeping_angels_spawns.json` was regenerated with 37 new biome entries; **9 arrived enabled at weight 8, groups of 1–4** (8 Incendium nether biomes + `quark:glimmering_weald`). Set `canSpawnHere = false`; the curated 16-biome set (weight 2, Deep Dark 3, solitary) is unchanged.
- `config/weeping_angels-common.toml` gained `[spawns]`: `snow_angels` **true → false** (worldgen would bury angels in every snowy biome, bypassing per-biome rarity), `max_nearby` **6 → 1** (solitary). `teleport_chance` / `interdimensional_teleporting` / `teleport_range` preserved.

### Server-Side Horror 4.2 → 5.1 — `config/serversidehorror.json`
New event `random_starer` (5.0 changelog: "new starer event which picks its players from players who joined the server"); absent keys fall back to mod defaults (enabled). Added `random_starer_enable = false`, `random_starer_chance = 8000000` to match the documented starer rarity and the no-fake-players preference. All existing keys preserved.

### Door Knocker 1.2.0 → 1.3 — `config/doorknocker-common.toml`
New independent `[doorknocker.trapdoors]` event arrived at the mod's frequent defaults (24000/48000 day, 12000/24000 night). Aligned to the documented door timings: **48000/96000 day, 24000/48000 night**. The mod dropped its sound keys.

### Waystones — `WaystonesGate` datapack extended
`waystoneandcreate` ships a Create compacting route (amethyst + 4 pearls + 4 emeralds → uncut warpstone → cutting → Warp Stone) that skipped the Eye of Ender gate (pre-existing). Added `data/waystoneandcreate/recipes/compact_warp_stone.json`: one emerald → `minecraft:ender_eye`. Create Waystones Recipes 2.0.1 also ships `data/waystones/recipes/warp_stone.json` (mechanical crafting, no eye) at the **same path** as the pack's override, so the pack's recipe still wins.

### Hardcore True Darkness 4.0.0 → 6 (new mod id `hardcore_true_darkness`)
A complete rewrite: server-enforced with a client/server protocol handshake (clients without the matching v6 jar are disconnected), gamma locked to 0, a full-screen "final darkness" post-process pass with a blackout fallback. Not the cause of the "void" world (that was Chunk Sending 4.1, below), but with Oculus + Hysteria its post-process pass rendered the whole view pure black (fine with shaders off). **Rolled back to `hardcoretruedarkness-4.0.0.jar`** (July's version, from `Servers/spookydookie/mods/`) on 2026-11-01 evening; `hardcore_true_darkness-6.jar` parked in `local/mods-rolled-back-2026-11-01/` (not left as `.disabled` — the CurseForge app re-enables `.disabled` jars it believes are installed, which is how Chunk Sending 4.1 came back at 21:30); the v6-only `defaultconfigs/hardcore_true_darkness-server.toml` (added earlier the same day) removed again. **Pin 4.0.0 in CurseForge.** The server already runs 4.0.0.

### Chunk Sending 2.8 → 4.1 — **rolled back to 2.8** (the "void" world, resolved 2026-11-01 evening)
4.1 never delivered chunks to the client in this pack: F3 showed 0 client chunks against thousands server-side; with `debugLogging` on, its per-tick line claimed "Sent: 15" while its own queue only grew (426 → 1487 in 76 s) and the client logged no drops. Its rewritten send path runs on the player tick, which under DimThreads is the `dimthread_server_overworld` thread. Restoring `chunksending-1.20.1-2.8.jar` (July's known-good pairing with DimThreads 1.2.1) fixed it immediately (queue 392 → 0). `config/chunksending.json` restored to the 2.8 format (`maxChunksPerTick = 5`); the 4.1-format file is kept as `chunksending.json.v41`. The 4.1 jar is parked in `local/mods-rolled-back-2026-11-01/`. **Pin 2.8 in CurseForge** with "Change version": a renamed `.disabled` jar is not a pin — the app flipped 4.1 back on once at 21:30 and the void returned. The server already runs 2.8.

### Farsight 5.1 → 5.8 — **rolled back to 5.1**
5.8 is a rewrite of the client chunk path (new `ClientChunkHandler`, a new cancellable HEAD hook on `ClientPacketListener.updateLevelChunk`, an emptied Embeddium compat class). It was not the cause of the void (rolling it back changed nothing on its own) but is kept on the known-good 5.1 by request; `config/farsight.json` is back to the July values (`enableChunkManager = true`). The 5.8 jar is parked in `local/mods-rolled-back-2026-11-01/`. **Pin 5.1 in CurseForge.** The server already runs 5.1.

### TerraFirmaCraft 3.2.23 → 3.2.28
- Data overrides verified: `TFCFoodHunger` (146), `TFCLightItems` (58) and `TFCVanillaCraftingTable` all still match; no `item_sizes`/`food_items` file was added, removed or changed in 3.2.28. `tfc_vanilla_food.js` generated 628 defs cleanly.
- `config/tfc-client.toml`: `showGuideBookTabInInventory`, `showGuideBookLinksAlways`, `showHoeOverlaysInInfoMods` → **false**. The Calendar/Nutrition/Climate inventory tabs have **no config toggle** (verified in `ClientForgeEventHandler.onScreenOpen`); new **`TFCHideInventoryTabs`** resource pack (`config/openloader/resources/`) blanks the tab strip in `tfc:textures/gui/icons.png` — the buttons still exist, invisibly.
- `tfc-server.toml` (instance `defaultconfigs/` + live `saves/New World (2)`; **server not changed**): `enableNetherPortals` **false → true** (the Waystones Eye-of-Ender gate, the Occultism ritual costs and three installed nether worldgen mods all assume a reachable nether); `enableVanillaMonstersOnSurface` **false → true** (TFC was silently denying every surface vanilla monster spawn — contradicts "a world that fights back"); `enableInfestations` **true → false**; `enableOverburdening` **true → false** (direct toggle for what `TFCLightItems` works around).

### KubeJS — `oc_sanity.js`
`CONFIG.debug` **true → false** (left on after the "Sanity fix" commits; it logged DIAG lines every 2 s per player). The live log confirmed both channels load and evaluate correctly.

### FTB Quests — Broken World
Removed the undocumented "Warmth" quest: its icon and both tasks were `cold_sweat:*` items (Cold Sweat removed). Backup `broken_world.snbt.bak`. Every other quest/reward/icon ID in the eight pack chapters resolves against the current mod set; the WDA chapter is the `wda_questline` mod's own content.

### modlist
Regenerated via `tools/generate_modlist.py` — 286 enabled, 0 disabled. Uncategorised: cozyapartments, lunaracolonies, hardcore_true_darkness, nochatrestrictions, sword_and_kingdom_schematic.

### Reviewed — no change
Smooth Chunk 4.4, Sophisticated Backpacks/Storage/Core, Storage Drawers 12.15 (new Push/Pull upgrades), The Obsessed 1.5.2 (tuned values intact; new structure-building/horse-stealing left on), JEI 15.56, Collective/Double Doors (configs moved to `config/serilum/`), Blood Magic 3.3.8, AmbientSounds 6.3.11 (`QuietAmbience` paths unchanged), AE2 15.4.12, Ad Astra 1.15.21, Extreme Reactors/Mekanism/Occultism/SecurityCraft overrides (all 17 OpenLoader packs checked programmatically against the updated jars).

---

## 2026-11-01 (22:00) — Lore & config consistency pass — *client instance only*
Second pass over every client config against the documented rules (this file, `README.md`, `docs/thelore.md`, `docs/configcheck.md`) and against the server's July copies of `config/` and `defaultconfigs/`. Every July-documented value was re-verified in place (the only textual drift is The Obsessed 1.5.2 and Man From The Fog now writing `2000.0`-style floats — same values). Everything below is either a config the docs had never covered, or a documented intent that turned out not to be in effect.

### Corrections to earlier sections
- **The Pale Hound is a timer-summoned stalker, not a biome-weighted spawn.** It generates `pale_hound-server.toml` per world (the same `[Timers]` / `["Spawn Conditions"]` layout as Cave Dweller) and summons itself every **800–1200 s** (30 % chance of a 1400 s cooldown), up to **3** at once. Its mod default is `override_biome_datapack_config = true` with `surface_biomes_is_whitelist = false`, so the config's 40-biome vanilla **blacklist** is what applied and the `pale_hound_surface_biomes` tag that `HorrorSpawnRarity` overrides was **ignored** — it could spawn in every Terralith/modded biome. The "code-weighted — no config" note in the new-mods pass was wrong. Fixed below.
- **CANTBREATHE** (`cant_breathe-1.20.1-1.0.2`, in the horror suite since July) had never been reviewed. Same timer family: every **850–1500 s**, any overworld biome, surface allowed, breaks doors in 1 s, spots you from 1000 blocks. Fixed below.
- `docs/sanity_mechanic.md` §11 listed Pale Hound as biome-weighted (reachable by the sanity spawn-suppression channel); it belongs in the timer-summoned row with Mimic / Cave Dweller / Man From The Fog, as does CANTBREATHE. Corrected there.
- TFC's `enableVanillaMonstersOnSurface` check is tag-based (`tfc:vanilla_monsters`), so the modded horror spawns below were never suppressed by it — their July weights were live all along.

### The Pale Hound (`defaultconfigs/pale_hound-server.toml`, new) — *per-world → defaultconfigs*
Aligned to the Mimic / Cave Dweller cadence and to the biome set the July pass intended:
| Setting | Before (mod default) | After |
|---|---|---|
| `can_spawn_min` / `can_spawn_max` | 800 / 1200 | **3600 / 7200** |
| `can_spawn_cooldown_chance` / `can_spawn_cooldown` | 0.3 / 1400 | **0.85 / 3600** |
| `spawn_chance_per_tick` | 0.005 | **0.0025** |
| `maximum_amount` | 3 | **1** |
| `surface_biomes_is_whitelist` | false | **true** |
| `surface_biomes` | 40-biome blacklist | **`deep_dark`, `dark_forest`, `snowy_taiga`, `grove`, `frozen_peaks`** |

`override_biome_datapack_config` left `true` (the config is now the lever; the datapack tag stays as a fallback if it is ever flipped). Applied to `saves/New World (2)` too. **Why:** hour-plus gaps, solitary, only in the documented remote cold/dark biomes — the "rare > frequent" rule the rest of the suite follows.

### CANTBREATHE (`defaultconfigs/cant_breathe-server.toml`, new) — *per-world → defaultconfigs*
| Setting | Before (mod default) | After |
|---|---|---|
| `can_spawn_min` / `can_spawn_max` | 850 / 1500 | **3600 / 7200** |
| `can_spawn_cooldown_chance` / `can_spawn_cooldown` | 0.4 / 1000 | **0.85 / 3600** |
| `spawn_chance_per_tick` | 0.005 | **0.0025** |

`maximum_amount` was already 1. Biomes (any), door-breaking and the 1000-block spotting range are left alone — the pack rule is *rarer*, not weaker ("do not make the horrors stronger" / weaker, `configcheck.md`). Applied to `saves/New World (2)`.

### John Mod Reborn + Creeping Woods — `HorrorSpawnRarity` extended (19 new overrides)
Neither mod has a config; both spawn through `forge:add_spawns` biome modifiers, so the same-path override precedent (Rake / TOWW / Wendigo / Whispering Spirits) applies.
- **John Mod Reborn** (`forge:any` — every biome, every dimension): `john` **6 → 2** (groups 1–2 → 1), `baby_john` **9 → 2** (packs of **3–9 → 1–3**), `baby_john_pack_lead` **9 → 2**. `the_hunted` was already 1 — untouched.
- **Creeping Woods** (all forest/taiga biomes): 17 apparition/stalker spawn entries at weights **10–130** (combined ≈ 430 — on a par with the entire vanilla monster table, so those forests were mostly Creeping Woods). The 16 above the benchmark all **→ 2**, groups 1–1 (`quick_light` was 130, 1–3; `human_1` was already 2). Includes the invisible `ambient_sound` emitter (15 → 2), consistent with the AmbientSounds "silence matters" pass.

**Why:** the documented horror benchmark — weight 2, solitary; "a still forest is scary". *Tunable:* per-file weights in `config/openloader/data/HorrorSpawnRarity/data/{john_mod_reborn,creepingwoods}/forge/biome_modifier/`. Takes effect on next world load; worldgen-independent.

### Xaero's Minimap (`config/xaero/minimap/profiles/default.cfg`) — *client; new mod*
Added 2026-11-01 alongside the world map. The July decision was "world-map only, no live minimap/radar"; the entity **radar** is the part that breaks the horror layer (every stalker is a dot before you ever see it). `display_radar` **true → false**. The minimap itself, waypoints and cave mode are left as the player set them (`display_minimap` is currently false in the profile).

### Jade (`config/jade/hide-entities.json`) — *client*
Added the 113 entity ids of the horror suite (every `entity.<mod>.*` key from the mods' own lang files: CANTBREATHE, Pale Hound, The Mimicer, Cave Dweller, Man From The Fog, Rake, The One Who Watches, Whispering Spirits, Antlers, Weeping Angels, John Mod Reborn, Creeping Woods, The Obsessed) to Jade's blocklist, so looking at one shows **no** name / health / armour overlay. **Why:** `thelore.md` — "Researchers have tried to classify them … Nobody has found an answer"; a HUD line reading `Cave Dweller 40/40` is the opposite. Born in Chaos / Foes (the hostile ecology — "The Altered") stay identified. Restart to apply.

### Nature's Compass (`config/naturescompass-common.toml`)
`biomeBlacklist` **`[]` → the six Alex's Caves biomes** (`abyssal_chasm`, `candy_cavity`, `forlorn_hollows`, `magnetic_caves`, `primordial_caves`, `toxic_caves`). **Why:** the Alex's Caves pass made cave biomes ~4× rarer and 600–975 blocks from spawn precisely so that finding one is "something impossible"; a compass with a 2500-block search radius pointed straight at them. Explorer's Compass left as is (see proposals).

### Quark (`config/quark-common.toml`)
`[oddities] "Totem Of Holding"` **true → false**. **Why:** Corpse (`corpse-server.toml` defaults: inventory kept in the body, anyone may loot it, never despawns while it holds items) is the pack's death system and the more thematic one; two drop-capturing systems on the same death event is undefined ordering and, at best, a confusing empty totem.

### Serene Seasons (`config/sereneseasons/`) — *re-added 2026-11-01 when TFC was removed*
Now the pack's only climate/seasons layer (README: "harsh biomes and weather bite"). Config is the mod default except:
| Setting | Before | After |
|---|---|---|
| `day_duration` (`seasons.toml`) | 24000 | **36000** |

The season clock counts real server ticks, and the key's own comment says it is "intended to be used in conjunction with another mod which adjusts the actual length of a Minecraft day". Simple Day Length Extender makes a day 36000 real ticks, so at 24000 each sub-season would have lasted 5⅓ game days instead of the 8 configured.
Reviewed and kept at default: `sub_season_duration = 8` (96-day year ≈ 48 h of 30-min days); winters at `biome_temp_adjustment −0.8` with `generate_snow_ice = true` (snow reaches temperate biomes — "the winters were never this harsh", `configcheck.md`); `fertility.toml` `seasonal_crops = true` / `out_of_season_crop_behavior = 0` (out-of-season crops grow *slowly* rather than fail — survival pressure that doesn't break MineColonies farms, whose `foodmodifier 1.5` already makes feeding real work); overworld only. **Server note:** the dedicated server has **no Serene Seasons jar**; seasons run server-side, so it must be added when the server is updated.

### Reviewed — rationale recorded (no change)
- **Corpse** (`corpse-server.toml` defaults) — the body holds the inventory, `only_owner = false` (a corpse is lootable evidence, fitting "the world documents itself"), skeleton after 72000 ticks, never force-despawns. Kept.
- **Comforts** (`comforts-server.toml` defaults) — sleeping bags night-only, hammocks day-only, no re-sleep restriction. Kept as the July QoL choice; a stricter option is in proposals.
- **Fragile Torches** (`chancetobreak 0.01`; players, villagers, animals "gentle") — mobs knock torches over, players don't. Fits "darkness fights back"; kept.
- **Sounds of the Forest** (`SOTF.toml`, `cooldown 18000`) — one forest sound per ≥ 15 min; kept (silence matters).
- **Void Fog** (fog from `min height + 12`, 30-block distance) and **Cave Dust** (Y −64…64) — client atmosphere; kept.
- **Sodium Dynamic Lights** (`self = true`, entities off) — a held torch lights the player's surroundings. Softens Hardcore True Darkness, but it is a July-era accessibility choice; kept (`self = false` for the strict experience).
- **Artifacts** (`artifactRarity 1.0`, campsites 4 attempts/chunk, mimic chance 0.3) — see proposals (night-vision goggles).
- **Explorer's Compass** (`maxRadius 10000`, no XP cost) — see proposals.
- **Iron's Spellbooks** — `irons_spellbooks-server.toml` at defaults (`spellGriefing = false`, `manaSpawnPercent 0`), no per-spell overrides generated. See proposals (Recall / Portal vs the no-cross-dimension-teleport rule).
- **Ars Nouveau** — `enableWarpPortals = true`; warp scrolls uncosted. See proposals.
- **Quark** modules otherwise as shipped: Monster Box (cave monster caches — vanilla-flavoured dungeon danger), Forgotten / Wraith (dark-only skeleton variants), Pipes / Crates / Magnets (early logistics the AE2 gating assumes exist), Compass and Clock nerfs on (they don't work until crafted — fits "minimal hand-holding"), Pathfinder Maps on (a paid, villager-sourced way to find biomes — the intended alternative to the compass).
- **Born in Chaos** (40+ `forge:any` hostiles, weights 2–30, some in packs) and **Foes** (regional hostiles, weights 1–80) — the hostile ecology ("The Altered"), not the psychological layer; no config; left at mod weights. Flag: these are what "a world that fights back" means in practice; if nights feel like a swarm rather than a threat, the `HorrorSpawnRarity` override pattern applies to them too.
- **Horror Element Mod** — five worldgen body features (impaled soldiers/knights, scarecrow farmer, desert marine head, …) placed as 3–4 random-height attempts per chunk with **no** rarity filter (Antlers' bodies had `chance 17`, raised to 68). Worldgen-baked; no in-game density reading yet — if they read as clutter, override `worldgen/placed_feature/*.json` with a `rarity_filter` exactly as the Antlers fix.
- **Loot Integrations** (`moddedItemWeight 3`) — modded loot 3× likelier in integrated chests; kept (`oc_loot.js` carries the lore layer).
- **Update-generated diffs vs the server copies with no gameplay meaning:** Entity Culling (`configVersion 9`, new `safeMode`), Not Enough Animations, Weeping Angels client (`donator_lookup`), Sophisticated (stack-conversion recipes), Storage Drawers (Push/Pull upgrades), Smooth Chunk / Chunk Sending (comment format), Sound Physics (Macaw's / MineColonies sound rates), JEI, Oculus, Spark.
- **Crash Assistant** `send_uploaded_logs_data_to_kostromdan_dev = true` — only acts when you press "upload"; left.

### Proposals — not applied (your call)
1. **Singleplayer view/simulation distance is 64 / 64** (`options.txt`; the log shows `Changing view distance to 64, from 10` and `simulation distance to 64`). The integrated server therefore keeps ~16,600 chunks loaded and ticking (the "≈5000 and rising" F3 reading), every horror timer, spawn cap and Serene Seasons snow pass scales with that area, and it is why the chunk-send backlog was so deep. The dedicated server runs 10 / 10. Recommend 12–16 render (Farsight fills the horizon beyond the server distance) and 8–10 simulation.
2. **Artifacts: Night Vision Goggles** contradict "darkness is genuinely dark" and are ordinary chest/campsite loot at `artifactRarity 1.0`. Lever: the Artifacts gamerules (`artifacts.nightVisionGoggles.enabled` / `.strength`) set from a `minecraft:load` function datapack (the DayLength30 pattern), plus `artifactRarity` 1.0 → 2–3.
3. **Iron's Spellbooks Recall / Portal** and **Ars Nouveau warp scrolls / warp portals** are uncosted, unbounded, cross-dimension teleports — the same conflict the Mahou pass resolved (`EQUIVALENT_DISPLACEMENT_*`). Levers: Iron's per-spell config (`/ironsSpellbooks config`, then `enabled = false` or a mana/cooldown increase), `enableWarpPortals = false` plus a recipe override for the warp scroll.
4. **Compass XP cost**: `defaultXpLevels` in both compass configs (e.g. 3–5) mirrors "every teleport costs XP"; Explorer's Compass otherwise locates any structure within 10 km for free.
5. **Comforts**: `restrictSleeping = true` (one sleep per in-game day) or `sleepingBagBreakChance = 25` — nights are the horror window and a bag in the pack currently skips every one.
6. **TFC leftovers** are inert but still load: `tfc_vanilla_food.js` (generates 629 food defs for a mod that is no longer there), the five `TFC*` OpenLoader packs, `config/tfc-*.toml`, `defaultconfigs/tfc-server.toml`. Park them under `data-disabled/` / `resources-disabled/` and out of `kubejs/server_scripts/` once TFC is confirmed gone — and the `.disabled` jars (TFC + four dependants) can be flipped back on by the CurseForge app the way Chunk Sending 4.1 was: remove them from the profile or park them in `local/`.

### Born in Chaos — infected diamond ore removed (`BornInChaosNoInfectedOre` datapack, 2026-11-02) — *global, new chunks*
Born in Chaos adds **Infected Diamond Ore** (stone, Y 0–16) and **Infected Deepslate Diamond Ore** (Y −128…−8), one vein attempt per chunk in every overworld biome, via two `forge:add_features` biome modifiers. The ore drops nothing (empty loot table); breaking it (or blowing it up) runs a procedure that spawns a **Deceived Hopes** mob — the only way that mob enters the world. Nothing else in the mod references the ores (no recipes, no tags). Requested removed.

**Fix:** `config/openloader/data/BornInChaosNoInfectedOre/` overrides both `data/born_in_chaos_v1/forge/biome_modifier/infected_*_diamond_ore_feature_biome_modifier.json` with `{ "type": "forge:none" }` — the same no-op-modifier technique as `PerfFixes`. The block and item still exist (creative/JEI), so nothing breaks on load.

**Scope:** worldgen is baked in at chunk generation, so this affects **newly generated chunks only**; veins already placed in explored terrain stay and still spawn Deceived Hopes when mined. *Revert:* delete the pack folder.

### Immersive Portals × DimThreads (2026-11-02) — `tools/immptl_dimthread_compat/` + `config/immersive_portals.json`
Four jars were added 2026-11-02 ~01:00: **Immersive Portals 3.0.7** (mod ids `immersive_portals`, `imm_ptl_core`, `q_misc_util`), **Immersive Portals: Full Immersion 1.0.20**, **Ars Two-Way Portals 1.3.4** and the **Make it Compatible** patch. The first world load produced a **1.3 GB `latest.log` in under three minutes**: ≈470,000 × `[dimthread_server_<dim>/ERROR]: Called in a non-server-main thread.` each with a 14-line stack trace (419k from the overworld).

**Mechanism (verified with `javap` on the shipped jars, not from docs):**
- DimThreads' `tickWorlds` hands every level to a `dimthread_server_*` worker and the server thread blocks in `awaitCompletion()` until all finish, so the main thread and the workers never overlap.
- Immersive Portals injects at the end of vanilla `DistanceManager.runAllUpdates` (gated on `enableImmPtlChunkLoading`) and calls `ImmPtlChunkTickets.flushThrottling(level)`, whose first statement is `if (!server.isSameThread()) { LOGGER.error("Called in a non-server-main thread.", new Throwable()); return; }`. `isSameThread` compares against `MinecraftServer.serverThread`, which DimThreads does not (and cannot) swap — it swaps the per-level and per-chunk-cache "main thread" fields only. So on a worker the guard fails every call.
- It is **not** the "void" problem returning: the same flush is also called from Immersive Portals' own server-tick hook on the real server thread, which passes the check. New World (5) generated 11,841 chunks in that session, the normal rate. What was lost was the in-tick top-up flush (slower portal chunk loading) plus the log flood and ~2,700 stack-trace constructions per second at the 64-chunk simulation distance.
- Neither project fixes it: DimThreads 1.20.1 has no build newer than v1.2.1 (Nov 2024) and its tracker has this exact error open since June 2025 (SrRapero720/dimthreads#47, no reply); Immersive Portals' repository was archived April 2026. DimThreads has no per-dimension exclusion, and a thread count of 1 still runs on a worker. Immersive Portals detects DimThreads at startup but only logs "Dimensional Threading is present".

**Options weighed:** remove one mod; `enableImmPtlChunkLoading = false` (stops the flood, hands player chunk loading back to vanilla, but portals then never load their far side); `/gamerule dimthread_active false` per world (DimThreads inert where portals matter); filtering the log line (hides the symptom only — rejected); a compat shim. **Chosen: the shim.**

**`immptl_dimthread_compat-1.0.0.jar`** (source + `build.py` in `tools/immptl_dimthread_compat/`, built with the server's JDK 17 against the jars already in the CurseForge install; copy kept in `local/`). One `@Redirect` on that single `isSameThread()` call inside `flushThrottling`: it now also returns true when the current thread is a DimThreads worker (`DimThread.owns(thread)`) **and** that worker is the one DimThreads has recorded as the level's current main thread (`IMutableMainThread.dimThreads$getMainThread() == current`). Any other thread still gets Immersive Portals' refusal. **Why it is safe:** the flush only touches the distance manager and ticket queue of the level passed in, and it is invoked from inside that level's own update loop on the thread already ticking that level — the same assumption vanilla's chunk code is running under there. It is the shape of DimThreads v1.2.1's own Tardis Refined fix. Mixin is `required: true`, so a symbol change in either mod fails loudly at startup instead of silently dropping the fix. Mandatory on **both** client and server (`mods.toml` depends on `imm_ptl_core` and `dimthread`). *Residual:* Immersive Portals' per-dimension `WeakHashMap` is read from several workers — its keys are live levels for the whole session, so it never mutates after warm-up; the one hang report on the DimThreads tracker (#46, NeoForge 1.21.1, after 8 h) was with portals set to vanilla and is a different path. *Revert:* delete the jar from `mods/` (and `local/`); nothing is persisted in worlds.

**Verified 2026-11-02 01:33–01:42, headless A/B on a throwaway dedicated server** (scratch folder, the 281 server-safe jars from `mods/`, same world for both runs, 90 s of ticking after `Done`, no players): **without the shim** 80,685 `Called in a non-server-main thread` lines (≈1,790 per dimension — one per tick in every dimension), 154 MB log; **with the shim** the mod's load line is present, no mixin apply failure (the only one is Better Chunk Loading's long-standing Canary clash), **0** such lines, 504 KB log, and the remaining ERROR set is identical to the control's. No `Can't keep up` in either run. **Confirmed in-game 2026-11-02:** portals load and render their far side with DimThreads active.

**Also:** `config/immersive_portals.json` `disabledWarnings` `[]` → `["many_mods"]` — the "you are using many mods" chat warning you tried to dismiss with the in-chat command (which was rejected as an unknown command); the key is the one `IPModInfoChecking` checks. Online mod-info check and update notification left on.

**Addon configs reviewed, kept at defaults:** Full Immersion — mobs attack, target and pursue through portals (`true`; fits a world where things follow you), `portalChunkLoadingRadiusCap = 4`, `portalDiscoveryRangeChunks = 8`, `spawnChunkRadius = 3` (the mod shrinks the permanent spawn-chunk ticket from vanilla's radius 11 — a performance win, note it if something at spawn stops ticking while nobody is near), `keepaliveTimeoutSeconds = 120`. Ars Two-Way Portals — Dominion Wand rotation, gravity portals and Immersive Portals integration for Double-Sided Stable Warp Scrolls all on. Both addons and the patch jar are **server-side too** (the server currently has none of the four jars, nor the shim).

**Portal far-side distance (2026-11-02, later):** Full Immersion `portalChunkLoadingRadiusCap` `4` → **`6`** in `config/immersive_portals_full_immersion-common.toml` on the client **and** in `Servers/Server v2` (the server does the loading, so it must change there). The far side of a portal now loads 169 chunks (was 81) per active portal. The effective radius is the lower of this and Immersive Portals' own `indirectLoadingRadiusCap` (8), so 8 is the ceiling without raising that too. Cost is remote chunk ticking, autosave and entity spawning per active portal; watch server TPS with several portals in range. `portalDiscoveryRangeChunks` stays 8. Needs a client restart and a Server v2 restart to apply. Separate finding from the same session: portal artifacts (far side looking zoomed, blocks at a portal cut off) were caused by shaders (Oculus + Hysteria), not chunk loading; turning TAA off in Hysteria helped but did not fully remove them. The shader setting is per player and is not in the pack files.

**Farsight is inert while Immersive Portals is installed (2026-11-03):** the client log says `Farsight not enabled for: ResourceKey[minecraft:dimension / minecraft:overworld] as other mods changed the chunk source to: class qouteall.imm_ptl.core.chunk_loading.ImmPtlClientChunkMap`. Both mods replace the client chunk cache and Immersive Portals wins, so Farsight's client-side chunk keeping does nothing and the view range is limited by the server's `view-distance` (Server v2: 12) whatever the client slider says. Farsight has no server-side code. The pin to 5.1 is still needed for the Chunk Sending 2.8 stack, so the jar stays. To see farther on the server, raise `view-distance` in `Servers/Server v2/server.properties`; simulation distance is what costs tick time, view distance mostly costs chunk loading, memory and bandwidth. **Addressed 2026-11-03:** see "Farsight × Immersive Portals" below.

### Man From The Fog disabled (2026-11-06) — `config/man_config.toml` + `HorrorSpawnRarity` — *global*
Player feedback asked for far less surface horror ("above land in overworld is probably negligible"); the mod is surface-only by construction (its spawn timer only runs while a player is within 2 blocks of the surface), so it was switched off rather than tuned. Full reasoning and the rest of the horror proposals: `HORROR_REVIEW_2026-11-06.md`.
- `config/man_config.toml` `enable_spawning` **true → false** (stops the timer-summoned Man and therefore the chase, jumpscare and no-sleep effects).
- `HorrorSpawnRarity/data/man/forge/biome_modifier/mftfhang_biome_modifier.json` → `{"type": "forge:none"}` (the hanging variant spawned at weight 10 in every biome and is not covered by the config switch; `forge:none` is Forge's registered no-op modifier).
- Not affected: the cultist `man:house` structure, the Cultist villager job, and the once-per-world Ko-fi advert and cassette on first join (only removing the jar drops those). Already-spawned hanging Men stay until `/kill @e[type=man:mftfhang]`. `HorrorQuietAdvancements` stays in place.
- Applied to this instance, and **deployed to the VM server on 2026-11-06**: `man_config.toml`, the `mftfhang` override and the `HorrorQuietAdvancements` pack, all uploaded and verified byte for byte (see "Server tick audit — phases 0–1" below). Still to copy: "The Correction (Dev)" (singleplayer only) and the local test bed `Servers/Server v2`.
- **Later the same day the jar itself was disabled** (`.jar.disabled`) in this instance and on the VM, which also drops the structure, job and advert listed above. See "Man From The Fog jar disabled, deployment tooling" below.

### Mod set changes, 2026-11-01 evening → 2026-11-02 (record)
Changes made in the CurseForge app after the update review started, compared with the committed `modlist.json` (commit "Config changes"). Each one's config work is in the entries above.

**Disabled** (still in `mods/` as `.disabled`):
- **TerraFirmaCraft 3.2.28** and its dependants **Macaw's All-In-One for TFC** and **TFC: Carry On Storage** — the removal the review recommended (TFC worldgen was never active; only its mechanics were, and the pack was working around all of them).
- **Ars Sophisticated Compatibility** and **Mekanism + Sophisticated Backpacks** — disabled the same day; reason not recorded.
- ⚠️ The CurseForge app re-enables `.disabled` jars it believes are installed (it did exactly that to Chunk Sending 4.1). Remove these five from the profile, or move them to `local/`, to make the removal stick.

**Consequences of removing TFC:**
- Every TerraFirmaCraft section above (HUD bars, thirst and hunger, food hunger tiers, modded food, collapses, item size/weight, the vanilla crafting table, the 2026-11-01 `tfc-server.toml` toggles) is now **historical**. The pieces that still load do nothing: `TFCFoodHunger`, `TFCLightItems`, `TFCVanillaCraftingTable` (data), `TFCNoSizeTooltip`, `TFCHideInventoryTabs` (resources), `kubejs/server_scripts/tfc_vanilla_food.js` (still generates ≈629 food definitions for a mod that is absent), `config/tfc-*.toml`, `defaultconfigs/tfc-server.toml`. Retiring them is an open proposal.
- Vanilla behaviour returns by default: hunger and natural regeneration, nether portals, bonemeal, wandering traders, pillager patrols, surface monster spawns, and `/time add`.
- **Worlds created while TFC was installed keep TFC's forced game rules in `level.dat`**: `naturalRegeneration`, `doInsomnia`, `doTraderSpawning` and `doPatrolSpawning` are `false` there (`New World (2)`, `New World (3)`). Run `/gamerule <rule> true` for each in any of those worlds you keep. Worlds created afterwards were checked: `New World (4)` has all four `true`.
- Serene Seasons is now the climate/seasons layer (see the 22:00 entry).

**Added:**
- 2026-11-01 evening: **Serene Seasons 9.1.0.3**; **Macaw's** Bridges, Doors, Fences and Walls, Furniture, Holidays, Lights and Lamps, Paths and Pavings, Roofs, Stairs and Balconies, Trapdoors, Windows (decoration, no balance surface); **Create: More Bridges 1.0.2** (decoration); **LumiLib 0.4.1** (library); **Sophisticated Backpacks: Realistic Crafting 1.1** (changes backpack recipes; not reviewed for balance). Xaero's Minimap came with the update itself (radar off, 22:00 entry).
- 2026-11-02: **Immersive Portals 3.0.7** (bundles Immersive Portals Core and QMiscUtil), **Immersive Portals: Full Immersion 1.0.20**, **Ars Nouveau: Two-Way Portals 1.3.4**, **Make it Compatible: Immersive Portals 1.1.0**, and the pack-built **`immptl_dimthread_compat` 1.0.0** (entry above).

**Pinned back to the server's July jars:** Chunk Sending 4.1 → **2.8**, Farsight 5.8 → **5.1**, Hardcore True Darkness 6 → **4.0.0**. The newer jars are parked in `local/mods-rolled-back-2026-11-01/`.

### Server v2 (2026-11-02) — `Servers/Server v2`, a new dedicated server matching the client
The July server (`Servers/spookydookie`, 264 mods, TFC-era, no world) is **left unchanged** for reference. A new server was built beside it, up to date with the client, with a new world.

**How it was built** (all copies; nothing written to the July server):
- **Forge 47.4.10:** `libraries/`, `run.bat`, `run.sh` and the installer jar copied from the July server (same Forge version; Java 17 still pinned in the launch scripts).
- **Mods:** `scripts/sync_server_mods.py --server "…/Server v2"` → the client's 303 active jars minus the 22 client-only ones = **281 jars**, including the pinned Chunk Sending 2.8 / Farsight 5.1 / Hardcore True Darkness 4.0.0, Serene Seasons, the Immersive Portals set and the `immptl_dimthread_compat` shim. TFC and its dependants are absent.
- **Pack content from the client:** `config/` (all OpenLoader packs, FTB Quests, every mod config), `defaultconfigs/`, `kubejs/`, `blueprints/`. Left out: `*.bak` backups, `chunksending.json.v41`, and JEI's per-world client state.
- **`server.properties`** from the July file, with three changes: `level-type` `tfc:overworld` → **`minecraft:normal`** (TFC's own default had been written there); `difficulty` `easy` → **`normal`** (every client world is Normal; Easy was just the vanilla default); a fresh, empty `level-seed`. Everything else as July: online mode, port 25565, view and simulation distance 10, no whitelist.
- **`user_jvm_args.txt`:** same memory (`-Xms6G -Xmx8G`); the custom `log4j2_server.xml` was dropped, because its only purpose was silencing TFC's calendar log spam. Forge's default logging applies.
- `eula.txt` carried over from the July server (already accepted there).

**Verified on first boot** (02:07–02:09, clean log): `Done` in ≈11 s of world load (≈60 s total start); the shim loaded with **0** "non-server-main thread" lines; OpenLoader injected **17/17** data packs; KubeJS **3/3** startup and **7/7** server scripts with 0 errors; the sanity mechanic loaded with both channels on; no `Can't keep up`. The world's generated `serverconfig/` carries the pack defaults: Pale Hound and CANTBREATHE 3600–7200 s, solitary; Mimic 3600–7200; Cave Dweller 7200–14400; MineColonies `raidDifficulty 8`; Mahou displacement cap 256; no TFC file. `level.dat`: Normal difficulty, `minecraft:noise` generator, `naturalRegeneration`/`doInsomnia`/`doTraderSpawning`/`doPatrolSpawning` all **true**, DimThreads active at 4 threads. (`doDaylightCycle` reads false only because Simple Day Length Extender holds time until the first player joins.) Remaining ERROR lines are the same mod-data noise the client logs on every launch.

**Tooling changed with it:**
- `scripts/sync_server_mods.py` now takes `--server <folder>` and **defaults to `Servers/Server v2`**, so a routine sync can no longer touch the July server.
- New `tools/generate_server_modlist.py` regenerates `docs/SERVER_MODS.md` from the server's actual `mods/` folder (same parser and categories as `tools/generate_modlist.py`). Run it after each sync.

**To keep it in step later:** after changing the client, run the sync script, then copy whichever of `config/`, `defaultconfigs/`, `kubejs/` changed, then regenerate the server mod list. `defaultconfigs/` only seeds new worlds; for this world, edit `world/serverconfig/` directly.

### Farsight × Immersive Portals (2026-11-03) — `tools/farsight_immptl_compat/` + Server v2 `view-distance` 16
**Problem:** with Immersive Portals installed, Farsight's chunk keeping is off (see the "Farsight is inert" note in the Immersive Portals entry), so on a server the view ends at the server's `view-distance` however far the client slider goes. Read from the jars' bytecode: Farsight's radius hooks (`handleLogin` / `handleSetChunkCacheRadius` report `maxRenderDistance` 64) still work, so rendering is not capped; what is lost is its delayed unload, which lives in Farsight's own chunk manager. Immersive Portals installs `ImmPtlClientChunkMap` unconditionally and its renderer depends on it, so Farsight's manager cannot simply win. Immersive Portals' map already keeps chunks at any distance, and nothing else in Immersive Portals drops client chunks.

**Applied:**
- **New client-only shim `farsight_immptl_compat-1.0.0.jar`** (source and build in `tools/farsight_immptl_compat/`, jar in `mods/` + `local/`). It holds the server's chunk-unload packets for the player's own dimension while the chunk is within `min(render distance, 32) + 1` chunks, drops a hold when the server sends that chunk again, and releases holds from the client tick once they are 3 chunks past that radius or in a dimension the player has left. Portal views into other dimensions unload as before. Details, limits and tuning flags in the shim's `README.md`.
- **Kept off the server:** `farsight_immptl_compat-` added to the client-only prefixes in `scripts/sync_server_mods.py` and to `tools/generate_server_modlist.py`; `docs/SERVER_MODS.md` regenerated (281 server jars, 23 client-only excluded). Players without the jar are unaffected.
- **`Servers/Server v2/server.properties` `view-distance` 12 → 16** (simulation distance stays 10) at the user's request, so live terrain reaches further for everyone. It had already changed from the July value 10 to 12 at some point after the build. 16 means 1,089 chunks around each player, up from 625; view distance mostly costs chunk loading, memory and bandwidth, not tick time. Takes effect on the next server start. Check TPS with `/spark health`.

**Limits:** held chunks are snapshots. Block changes beyond the server's 16 chunks show only when you come back, and mobs there disappear because the server removes entities separately. Only terrain already received is kept; Farsight's disk preview stays off. Client memory is bounded by the 32-chunk cap; a client render distance of 32 matches it, and anything higher just draws empty space past the held area.

**Verified so far:** compiles against the Forge 1.20.1 SRG client jar and Immersive Portals 3.0.7; every injection target and shadowed field was checked against the real classes with `javap`; the sync dry run keeps the server at 281 jars. Not yet run in game: the mixin config is `required: true`, so a bad target would stop the client at startup with a clear error rather than fail silently. Expected log line on start: `[farsight_immptl_compat] loaded: Farsight's delayed chunk unload now runs on Immersive Portals' chunk map`.

### Better Chunk Loading smart post-processing off (2026-11-03) — `config/betterchunkloading.json`, *client instance + Server v2*
**Problem:** a singleplayer Chunky pregeneration in the user's "The Correction" instance, which runs the same 303 mods, froze for good at 22.79% (01:04:33). Two `jstack` dumps 33 s apart and ModernFix's integrated-server watchdog show the server thread parked in `ServerChunkCache.getChunkOffThread` under `com.betterchunkloading.event.EventHandler.applyToChunk`, called from `onServerTick`. The DimThreads overworld worker was idle in its pool and Chunky was waiting on its semaphore, so nothing could complete the queued chunk request. No Java-level deadlock (it is a `CompletableFuture.join`), no GC pressure (old gen 88%, 0 full GCs), and no error in the log before the hang.

**Cause:** Better Chunk Loading's `enableSmartPostProcessing` (mixin `LevelChunkPostProcessMixin` on `LevelChunk.postProcessGeneration`) defers post-processing of new chunks whose neighbours are not loaded and later fetches them with `Level.getChunk` from the server thread at the end of the tick. Under DimThreads that fetch was treated as off-thread and queued to the overworld's executor, which only its worker drains during a tick, so the server thread waited forever. Chunky makes such deferred chunks constant. Why the chunk cache did not count the server thread as its main thread at that moment is not determined; DimThreads swaps it to the worker during the level tick and back afterwards.

**Applied:** `enableSmartPostProcessing` `true` → **`false`** in this instance's `config/betterchunkloading.json`, the only path that makes this call. The mod describes the feature as a slight loading-speed improvement. Takes effect on the next launch.

**Server v2 updated the same day at the user's request:** the client's `config/betterchunkloading.json` copied over (the only difference was this line). A full client-to-server comparison found nothing else to carry: mods already in sync (281), `defaultconfigs/`, `kubejs/` and `blueprints/` identical, portal radius 6 and `view-distance` 16 already applied. Deliberately not copied: `oculus.properties` (client shader state), `quark-common.toml` (the server's Quark strips its client-only `hotbar_changer` section), `config/chunky/tasks/` (a cancelled client pregen task), and the `*.bak` backup folders. Takes effect on the next server start.

**Still `true`:** the "The Correction" instance, which the user plays; it can hang the same way during Chunky runs until changed.

### MineColonies server config replaced (2026-11-04) — `minecolonies-server.toml`, *client defaultconfigs + Server v2*
The user supplied a new `minecolonies-server.toml` (1.1.1301 format, the version on both client and server). It was copied verbatim to this instance's `defaultconfigs/`, to Server v2's `defaultconfigs/`, and to Server v2's live `world/serverconfig/`. Value changes compared with the previous files:

| Setting | Before | After |
|---|---|---|
| `workersalwaysworkinrain` | false | **true** |
| `luckyblockchance` | 1 | **3** |
| `loadtime` (minutes a colony stays loaded after its owner leaves) | 10 | **40** |
| `colonyloadstrictness` | 3 | **1** (every claimed chunk is loaded) |
| `canplayerusehometpcommand` | false | **true** |
| `maxColonySize` | 20 | **75** |
| `minColonyDistance` | 8 | **16** |
| `initialColonySize` | 4 | **6** |
| `maxdistancefromworldspawn` | 30000 | **75000** |
| `raidDifficulty` | 8 | **5** (the mod default; this undoes the 2026-11-01 restoration to 8 above) |

All other values, including the guard multipliers, raid frequency and the other 1301 raid keys, are unchanged. The new file no longer contains the 1.1.1231-only keys (`dobarbariansspawn`, `barbarianhordedifficulty`, `maxBarbarianSize`, `dobarbariansbreakthroughwalls`, `shouldraiderbreakdoors`, `pathfindingdebugverbosity`, `pathfindingmaxthreadcount`). That only matters for a 1231 server, and none is in use.

**Load note:** with strictness 1 and a 40-minute load time, a large colony keeps all of its claimed chunks ticking for 40 minutes after its owner logs off. If server tick time rises once colonies grow, check this first in spark.

**Not changed:** existing singleplayer worlds in `saves/` (each keeps its own `serverconfig/`), the "The Correction" instance, and the July `Servers/spookydookie`. Server v2 picks up the change on its next start.

**Live VM, checked 2026-11-07: not applied.** The VM's `defaultconfigs/minecolonies-server.toml` is the new file, but its world's `world/serverconfig/minecolonies-server.toml` still has the old values for all 10 settings above. `defaultconfigs/` only seeds new worlds. To apply it, copy the file over the world's copy and restart, bearing in mind the load note. The VM world's other per-world configs (`cant_breathe`, `cave_dweller`, `create`, `mahoutsukai`, `pale_hound`, `themimic_er`) match `defaultconfigs/`.

**Applied to the VM world (2026-11-08, at the user's request):**
- **Checked first:** the world's file differed from the new one in exactly the 10 settings above.
- **Uploaded:** the new file to `world/serverconfig/minecolonies-server.toml`, and it read back identical.
- **Backup:** the old file was moved to `files/server/deploy-backups/2026-11-08-minecolonies/world/serverconfig/`.
- **Takes effect** at the next restart, because Forge configs don't hot-reload on the VM.
- **Rollback:** move the backup back and restart.

### Server tick audit — phases 0–1 (2026-11-06) — *VM server + client instances*
Plan, findings and estimates: `SERVER_TICK_AUDIT_2026-11-06.md`.

**The live server is now the Proxmox VM** (`/home/thecorrection/files/server`). It is reached over FTP and deployed to from this repo. `Servers/Server v2` is kept only as a local test bed and is out of step with the VM. The VM mirrors this instance's mods minus the 23 client-only jars (280 jars after this change).

**Applied on the VM by the owner (phase 0):**
- **JVM:** Aikar's G1 flag set added to `user_jvm_args.txt`. Memory deliberately kept at `-Xms8G -Xmx32G`.
- **Host:** CPU governor confirmed on `performance`.
- **`server.properties`:** `sync-chunk-writes` **true → false**.
- **File ownership:** all server files chown-ed to the `thecorrection` user. `config/spark/tmp` had been root-owned since the server once ran as root, so spark could not unpack async-profiler (`AccessDeniedException`) and fell back to the slower Java sampler.

**Phase 1 (this instance + VM; files uploaded and verified byte for byte; the VM was stopped; full backup at `/files/serverbak`):**

| Change | Where | Why |
|---|---|---|
| `HorrorSpawnRarity` overrides: `toww_hanging` **30 → 2**, `toww_delusion` **3 → 2**, `toww_hallucination` **5 → 2**, Rake `ambient_sound` **15 → 2** | `config/openloader/data/HorrorSpawnRarity/data/{the_one_who_watches,rake_the_arrival}/forge/biome_modifier/` (new files). The hallucination and Rake files are copied from the jars with only `weight` changed, so their biome lists stay as shipped | The July pass aligned TOWW and Rake to the weight-2 benchmark but missed these entries. `toww_hanging` never despawns and was piling up (6 → 8 loaded across the profiles); each one carried a 2048-block follow range |
| Fragile Torches gentle list: removed `boat`, `ender_pearl`, `egg`, `item`, `experience_orb`, `minecart` | `config/fragiletorches.toml` | The handler only sees living entities, so these six could never match. But the list is string-parsed for every mob on every tick, before the chance roll. No behaviour change |
| **Data Anchor removed** (`Data_Anchor-forge-1.20.1-1.0.0.24.jar`) | Jar parked in `local/mods-removed-2026-11-06/` in **both** "Spooky Dookie" and "The Correction (Dev)"; deleted from the VM's `mods/` | Orphan library: no jar on either side uses it. Its chunk and block-entity tick hooks cost ~1% of the overworld tick |
| Man From The Fog disabled | VM: `man_config.toml`, the `mftfhang` override, `HorrorQuietAdvancements` | See "Man From The Fog disabled" above |

**Client compatibility (Data Anchor):** Data Anchor registers its own sync packets on a network channel with a strict version check (`"1"` on both sides). **Every client that joins must drop it too, or it will be refused at login.** Also remove it from each CurseForge profile, so the app doesn't restore it.

**Check on the next server start:**
- OpenLoader injects **18** data packs (`HorrorQuietAdvancements` is new);
- no Data Anchor in the mod list;
- no "Unable to initialise the async-profiler engine" warning from spark.

Then take the new baseline with `/spark profiler start --thread * --timeout 300`.

**`fog_residue` moved to CANTBREATHE (same day; this instance + VM):** with the Man disabled, the Field Reports quest "Recover Fog Residue" (`7A05000000000109`) could not be completed.
- **`kubejs/server_scripts/oc_interactions.js`:** the `OC_RESIDUES` entries for `man:manfromthefog` / `man:managgresive` are replaced by `cant_breathe:cant_breathe`. Same 90% drop chance on death.
- **Why CANTBREATHE:** it's the other timer-summoned surface stalker, with the same 1–2 hour solitary cadence, any overworld biome, and no damage immunity, so it can be killed.
- **Rejected candidates:** TOWW (1,024 HP, 100 armor, custom damage handling), the Pale Hound (five remote biomes only), Creeping Woods' Demon Cloud (5 HP, too trivial for a marquee drop).
- **Quest (`field_reports.snbt`):** title "THE MAN" → "THE BREATHLESS", subtitle "The Man" → "The Breathless", and the description rewritten for CANTBREATHE (it spots you from very far, breaks doors, thins the air). Quest and task IDs are unchanged, so progress is kept.
- **Lore docs:** `configcheck.md` / `implore_implementation_plan.md` still name the Man as the residue source; they are historical.

**Not done yet:**
- Artifacts campsites and the optional configs (audit §5.2.4, §5.2.6) and the cleanup commands (§5.5.3), pending decisions;
- syncing these configs to "The Correction (Dev)" (singleplayer) and `Servers/Server v2`.

**Revert:**
- delete the four new override files;
- restore the six list entries in `fragiletorches.toml`;
- move the Data Anchor jar back into `mods/` on every client and copy it back from `/files/serverbak/mods/` on the VM.

### `spooky_tick_fixes` 1.0.0 — tick-fix shim, first pass (2026-11-06) — `tools/spooky_tick_fixes/`, *client instances + VM*
A pack-built Forge mod of behaviour-preserving server-tick fixes (audit §5.3). Each patch skips work that cannot change the outcome. Each applies only when its target mod is installed (a mixin plugin logs `… -> applying (modid)`), and each has its own switch in `config/spooky_tick_fixes-common.toml`. Source, design notes and the per-patch safety argument: `tools/spooky_tick_fixes/README.md`. It has no network channels or registry entries (`displayTest = IGNORE_ALL_VERSION`), so a client without it can still join.

| Patch | Target | Measured cost it removes (audit §4) |
|---|---|---|
| Dormant mimics skip their tick with no player within 24 blocks | Artifacts `MimicEntity#tick` | 11–17% of the overworld tick |
| Follow range 2048 → 32 for TOWW hanging, staring, crawling, hallucination, delusion and looked_at (type default + clamp on join); chase variants untouched | `EntityAttributeModificationEvent` / `EntityJoinLevelEvent` | up to 15% (path searches) |
| The Obsessed player-variable sync: one send per player per tick | `TheObsessedModVariables$PlayerVariables#syncPlayerVariables` | 5–11 ms per player per tick (server thread) |
| Whispering Spirits look check skipped with no spirit loaded | `PlayerLookAtSpiritProcedure#execute` | 1.3–3.4% of the server thread |
| Fragile Torches: check for a torch before the list parse | `FragTorchEvent#onEntityUpdate` | ~2.3% |
| Farm & Charm saturation sync only on change (plus a 100-tick refresh and on start-tracking) | `PacketHandler#sendSaturationSync` | 0.75% plus ~1,500 packets/s per player |
| MCreator animation handlers skip other mods' mobs (Born in Chaos, The Obsessed, Creeping Woods, Foes, TOWW, Rake, Antlers) | `EntityAnimationFactory#onEntityTick` × 7 | ~1.6% |
| Ad Astra Extra skips mobs that are neither gassed nor on Jupiter | `EntityTickProcedure#onEntityTick` | ~1.0% |

**Verified on a headless scratch server** (this instance's 280 server-side jars plus the shim, the repo's `config/`, `defaultconfigs/` and `kubejs/`, fresh world, Java 17):
- all 13 patches applied; no shim-related error or mixin failure (the only one is the long-standing Better Chunk Loading/Canary clash);
- config generated;
- a dormant mimic summoned 12 blocks up stayed at y 84.0, while an awake one fell and landed (84 → 73);
- watcher follow range: new hanging and staring watchers get 32, and hanging and staring watchers loaded with 2048 in their NBT are clamped to 32 within the same tick (checked through a datapack function); `toww_hunting` stays at 2048;
- cow, pig, zombie, Born in Chaos and Creeping Woods mobs ticked for 20 s with the gates active.

The player-dependent patches (The Obsessed, Whispering Spirits, Farm & Charm, Fragile Torches next to a torch) still need an in-game check.

**Deployed:**
- the jar is in `mods/` and `local/` of this instance (`build.py --install`) and in "The Correction (Dev)" `mods/`;
- uploaded to the VM's `mods/` (281 jars), verified byte for byte. The VM was stopped; it loads on the next start.

**Not in this pass:** Full Immersion portal-AI gating (§5.3.5: its portal index needs verifying first), Alex's Caves magnets (§5.3.10), Slice & Dice and Citadel (§5.3.9), activation range (§5.5.1).

**Revert:** delete the jar, or set any single switch to false. Nothing is stored in worlds.

### `spooky_tick_fixes` 1.1.0 — second pass + activation range (2026-11-06) — *client instances + VM*
It replaces 1.0.0 everywhere (this instance's `mods/` and `local/`, "The Correction (Dev)", the VM; the VM has 281 jars). Details: `tools/spooky_tick_fixes/README.md`.

**New behaviour-preserving patches:**
- **Full Immersion (`fullImmersionGate`, ~2.9% of the overworld tick).**
  - **What's skipped:** the per-mob portal-AI wrapper (`PortalAiContext.begin`/`finishAi`) and the portal despawn check, in any dimension with no Immersive Portals portal loaded.
  - **Why that's safe:** the mod only acts through `Portal` entities in the mob's level and indexes every one that joins.
  - **Pairing:** begin/finishAi are skipped together via a per-mob flag, so its per-thread state can't be left stale.
- **Alex's Caves (`alexsCavesMovingBlockGate`, ~0.4%).** The per-movement search for moving magnetic blocks is skipped in dimensions with none loaded; both functions then return their empty answer. Its per-entity magnetism check (~1.8%) is not changed; that needs a magnet-block count and is still open.
- **Citadel (`citadelTrackerCache`, ~0.3%).** The tick-rate tracker is cached per server instead of a saved-data lookup on every entity tick. The cached path still marks the data dirty, so persistence is unchanged.
- **Fail-open counts.** Portal and moving-block counts come from entity join/leave events and can only err towards "present", which switches a gate off, never on.

**Activation range (`[activationRange]`, audit §5.5.1). This one changes behaviour, for far mobs only:**
- **Rule:** mobs more than 48 blocks from every player tick 1 in 4 (staggered).
- **Still normal pace:** ageing and despawn checks run on the skipped ticks too.
- **Exempt:**
  - targets, recently hurt, burning, in lava, leashed, riding, in a raid;
  - bosses;
  - the `minecolonies`, `guardvillagers`, scripted-horror and `mahoutsukai` namespaces (config lists; the audit doc proposed a datapack tag, but config lists turned out simpler);
  - per the decisions taken: villagers are throttled, and adaptive mode is on (throttle only while the average tick is above 50 ms, off below 30 ms).
- **Status:** `/tickfixes stats` (op) shows it.

**Verified on the headless scratch server:**
- all 18 conditional patches applied, and the activation-range hook ran;
- `/tickfixes stats` reported 4–6 far mobs skipped per tick (adaptive off for the test) and `portals=0 acMovingBlocks=0`;
- a throttled baby cow aged −23,900 → −23,700 in 10 s (normal rate);
- a ridden (immune) pig dropped from 30 blocks died on landing within ~2 s, while the throttled plain pig was still at y 98.7;
- dormant mimics still slept;
- no shim-related errors.

**Still open:** the in-game checks (with players), the post-change spark profile, Alex's Caves' magnetism check, Slice & Dice wet air, The Obsessed's cached-namespace fix, and the watcher despawn rule (§5.5.2, held back because it changes horror pacing; it needs a decision).

### Man From The Fog jar disabled, deployment tooling (2026-11-06, evening) — *client + VM*
**Mod set.**
- `The-Man-From-The-Fog-1.4-1.20.1.jar` is `.jar.disabled` in this instance and on the VM. The client and the VM must agree: `man:man` is a strict network channel, so a mismatch refuses the login with "mismatched mod channel list".
- The second client instance, "The Correction (Dev)", was deleted; this instance is the only client.
- VM parity: 280 active jars = the client's 303 − 23 client-only. Data Anchor is gone from every side, and `spooky_tick_fixes-1.1.0` is on both.

**VM disk.** At 19:06 the 12.6 GB VM disk had 247 MB free and SimpleBackups aborted ("Not enough space on disk"). The full 18:00 copy in `/files/serverbak` had taken the remaining space. Its `simplebackups/` folder (9 zips, 2.44 GB) was an exact copy of the live one (same names and sizes) and was deleted, leaving about 2.7 GB free. SimpleBackups is unchanged (full backup every 120 min, keep 10, about 1 GB each), so the VM disk is to be grown in Proxmox.

**Tooling (`scripts/`, `tools/`).**
- **`scripts/sync_server_mods.py`** now deploys to the live VM by default, over FTP with the login from `~/.spooky_ftp_netrc`; `--server <folder>` targets a local server folder. It shows the changes and, for the VM, asks before applying (`--dry-run` only shows them, `--yes` skips the question).
  - Uploads go to `<name>.part` and are then renamed, so a broken upload never loads.
  - Unwanted jars are no longer deleted. They're renamed to `.jar.disabled` when the client has them disabled too, otherwise moved to `mods-removed/` beside `mods/`.
  - Local targets compare SHA-256; the VM compares sizes.
  - The client-only list now carries its groups and notes (`CLIENT_ONLY_GROUPS`), so the server doc can no longer miss a jar.
- **`tools/generate_server_modlist.py`** reads the VM by default.
  - It computes the server's disabled jars, the Forge version (from `libraries/`) and client parity.
  - Its fixed prose is a single hand-maintained `NOTES` block, which now says the VM runs Java 21.
  - `docs/SERVER_MODS.md` was regenerated from the VM: 280 jars, 1 disabled, parity OK.
- **`tools/generate_modlist.py`:**
  - new first category **Local Fixes**: `spooky_tick_fixes`, `immptl_dimthread_compat`, `farsight_immptl_compat` (the last two were filed under libraries and QoL);
  - the Minecraft version is the most common declared x.y.z reading, no longer the first jar alphabetically;
  - jars are closed after reading, and counts say jars and mods;
  - `modlist.md`/`modlist.json`/README were regenerated (303 enabled jars, 306 mods, 6 disabled). The previous files predated Data Anchor's removal and the tick-fix jar.
- **Build scripts:** the three `tools/*/build.py` share `tools/_javabuild.py`.
  - The mod id and version are read from `mods.toml`, which must match `MANIFEST.MF`.
  - `--install` removes older versions from `mods/` and `local/` for all three. Before this, two of them left the old jar, which would stop Forge with a duplicate mod.
  - Library wildcards pick the highest version numerically; `mods/` jars must match exactly once.
  - The JDK and library paths can be overridden (`SPOOKY_JDK`, `SPOOKY_MC_LIBS`).
  - All three rebuilt to contents identical to the installed jars.
- **Not synced:** the local test bed `Servers/Server v2` still has Data Anchor and an active Man From The Fog, and lacks `spooky_tick_fixes`. `python scripts/sync_server_mods.py --server "…/Servers/Server v2"` brings it in line.

### `spooky_tick_fixes` 1.2.0 (2026-11-06, evening) — *client instance + VM*
**Source.** The post-fix all-threads profile of the VM (19:06–19:20; it was too large to upload, so it was decoded from `config/spark/profile-2026-11-06_19.20.44.sparkprofile`).
- **What changed after 1.1.0:** same population (about 550–600 entities, about 150 mimics); 20 TPS with a 46 ms median, down from 6.6–7.2 TPS at about 140 ms.
- **DimThreads 1.2.1 doesn't run levels in parallel.** `DimThread.swapThreadsAndRun` is `static synchronized` and wraps each level's tick. So all ~45 levels tick one after another, and any cost paid per level is paid about 45 times per tick.

**New patches** (details: `tools/spooky_tick_fixes/README.md`):
- **`antlersDayCounterOverworldOnly`: a bug fix that changes behaviour.** Antlers' day counter runs on every level's tick, but the counter is global, and each run sent a full sync to every player.
  - Measured on the scratch server: **10,170 counts in about 224 ticks (45.4 per tick) without the patch, and 227 with it.**
  - Day counting returns to the real pace, and sync packets drop 45×. The counter's only effect is the one-time `wendigospawn` flag at day 2, which is already set in existing worlds.
- **`towwEmptyLevelSyncSkip`:** The One Who Watches no longer encodes its per-level sync packet when that level has no players; the data is still marked dirty. Verified: an empty dimension's `WorldTime` still advances and is saved.

**Our own overhead** (2.7% of the serial tick):
- The activation-range immunity check reads fire ticks instead of calling `isOnFire()`, which goes through Create's NBT read.
- The Fragile Torches pre-check uses one chunk lookup instead of two.

**Verification.**
- Scratch server: all 20 conditional patches applied, no errors.
- Installed in this instance's `mods/` and `local/`, replacing 1.1.0.
- Uploaded to the VM with `scripts/sync_server_mods.py --yes`. 1.1.0 was moved to `mods-removed/`; 280 jars, parity verified. It takes effect on the next restart.

**Seen while testing.** `Mixin apply failed betterchunkloading.mixins.json:ServerChunkCacheMixin` (it can't inject into `ServerChunkCache.getChunk`) appears in the VM log as well. It predates the shim, so Better Chunk Loading's chunk-priority hook has never been active.

### Incendium clock trim (2026-11-06, evening) — `IncendiumClockTrim` OpenLoader datapack — *global (client instance + VM)*
**Why.** All five of the world's scheduled functions are Incendium clocks. `incendium:clocks/main` reschedules itself every tick and runs 9 `@e` scans across every loaded dimension. Together they were 10% of the serial tick (about 4.5 ms), and 7.8 of those points were entity scanning. This reverses the earlier "leave Incendium as is" decision.

**What.** Same-path overrides of two Incendium 5.3.5 functions. Changed lines are marked `trim`:
- **`clocks/main`:**
  - **(A) Shield scans:** the two shield-poison scans use `@a` instead of an untyped `@e`. Only players ever get `in.cd_shield`: it's set by player init/main and by the shield functions, which run as the player.
  - **(A) Freeze scans:** the two `in.frozen` scans (countdown and snowflake particles) run only while `$trim.frozen in.dummy` is above 0.
  - **(B) Missing-ID fallback:** runs every 10 ticks instead of every tick.
- **`item/chilling_blade/on_hit`:** also sets `$trim.frozen` to 120, the freeze length. That's the only place `in.frozen` is ever set.

**Behaviour.**
- **(A) is identical.** One cosmetic edge: a mob that unloads mid-freeze and reloads later keeps its leftover score without particles until the next Chilling Blade hit.
- **(B) adds a delay of up to 0.5 s** for mobs that arrive already tagged `in.checked` (the castle round-table room, both sanctum structures, two sentry-blaze summons) or whose ID was cleared by an ID-counter reset. In that window, Incendium's melee weapon effects don't trigger on them.

**Rejected.**
- Limiting the scans to the Nether: the Chilling Blade, Blazing Hatchet, Daybreaker, Warping Witherblade and Cake Batter find the mob they hit by its `in.eid`.
- Running the clock less often: projectiles and castle AI run every tick.

**Expected saving:** about 2–2.7 ms per tick. Without a Chilling Blade freeze active, 4 of the 9 scans are gone, and a fifth runs 1 tick in 10.

**Verified on the scratch server.**
- OpenLoader loaded the pack ("Found new data pack … loading it automatically"), with no function errors.
- The `$trim.eid` counter cycles.
- A mob pre-tagged `in.checked` had no ID, then had one 2 s later.
- With the timer at 0, a mob's `in.frozen` 40 stayed at 40. With the timer at 40, it counted to 0 and so did the timer.

**Deployed.** Uploaded to the VM's `config/openloader/data/IncendiumClockTrim/` (19 packs there now) and verified byte for byte. It takes effect on restart.

**Maintenance.** The override pins Incendium 5.3.5's `clocks/main`. When Incendium updates, re-diff it against the jar's copy, or delete the pack.

### DimThreads disabled, `spooky_tick_fixes` 1.3.0, new server-side mods (2026-11-06, night) — *client instance + VM*
**DimThreads off.**
- DimThreads 1.2.1 wraps every dimension's tick in one global lock (`DimThread.swapThreadsAndRun` is `static synchronized`), so it never ticked dimensions in parallel.
- The 46-minute profile with `/gamerule dimthread_active false` (20:40–21:26) averaged 18.6 TPS with 2 players and 1,313 entities. That's no worse than with it on.
- `dimthread-FORGE-mc1.20.1-v1.2.1.jar` and `immptl_dimthread_compat-1.0.0.jar` are now `.jar.disabled` in this instance. The compat jar has to go too: its required mixin references DimThreads classes.
- No other mod depends on DimThreads. The world's saved `dimthread_*` gamerules are simply ignored. The scratch server started cleanly without either jar.

**`spooky_tick_fixes` 1.3.0: horror look-check ray casts.**
- **Cause.** During that profile a sailing player outran chunk loading. Seven per-player look-check procedures (Creeping Woods ×3, Rake, Whispering Spirits, John, The Obsessed) cast rays whose vanilla `clip` loads chunks synchronously. The Server thread blocked for 97 s in two minutes, with one 18.9 s tick, while those chunks were loaded or generated.
- **Fix.** Their `clip` calls now go through `RayGuard`. A ray spanning an unloaded chunk returns vanilla's miss result instead of loading it, and the three identical casts per step reuse one result. Switches: `horrorRayCastLoadedOnly`, `horrorRayCastDedupe`.
- **Testing.** On the scratch server all 27 conditional patches applied (7 new ray targets), with no injection errors. The guard itself needs players to exercise it: `/tickfixes stats` now reports its counters.
- **Details:** `tools/spooky_tick_fixes/README.md`.

**New server-side mods** (added to this instance; the server needs them too):
- **Let Me Despawn** `letmedespawn-forge-1.20.1-1.26.11.1.jar`, plus its required library **Almanac** `almanac-forge-1.20.1-1.26.11.1.jar`. Mobs that pick up items now despawn normally and drop what they picked up.
- **Dynamic View** `dynview-1.20.1-4.0.jar` (reports itself as 2.3). It adjusts view and simulation distance from the average tick time.
  - Its config is a Cupboard JSON, `config/dynamicview.json`, created on first start. The defaults were min/max view 10/10 and simulation 4/10.
  - Applied values:

    | Setting | Value | Why |
    |---|---|---|
    | `maxChunkViewDist` | 12 | The new `server.properties` `view-distance` |
    | `minChunkViewDist` | 8 | |
    | `maxSimulationDist` | 8 | The new `simulation-distance`; kept ≤ the minimum view distance |
    | `minSimulationDist` | 6 | Keeps a colony around the player ticking |
    | `meanAvgTickTime` | 40 ms | 10 ms of headroom, and below the activation range's 50 ms trigger |
    | `viewDistanceUpdateRate` | 60 s | Each change makes clients reload chunks |
    | `adjustSimulationDistance` | true | |
    | `logMessages` | true | |

  - The file is in this instance and on the VM. Cupboard's file watcher reloaded it live at 22:07.
  - The VM's original was root-owned, because the server process runs as root, so the FTP user couldn't overwrite it. It was renamed to `dynamicview.json.root-owned-old`; the default copy is `dynamicview.json.default-bak`.

**VM.**
- Dynamic View and Let Me Despawn were uploaded first, without Almanac, and the 21:58 restart failed: "Mod letmedespawn requires almanac … and dungeonconfig … not installed". DungeonConfig is embedded in the Almanac jar (jar-in-jar), so that jar covers both.
- `scripts/sync_server_mods.py --yes` then:
  - uploaded Almanac and `spooky_tick_fixes-1.3.0`;
  - renamed the DimThreads and compat jars to `.jar.disabled`;
  - moved `spooky_tick_fixes-1.2.0` to `mods-removed/`.
- 281 jars, parity verified. It takes effect on the next start.

### Watchdog crashes 21:56 and 22:05, `spooky_tick_fixes` 1.3.1 (2026-11-06, night) — *client instance + VM*
**Both crashes were the same failure.** In each, the watchdog killed the server after one tick ran past `max-tick-time` (120 s). The Server thread was inside `ServerChunkCache.getChunkBlocking`, called from a horror look-check ray cast while a player moved faster than chunks loaded. One world-generation thread was busy with terrain noise; the others were idle.
- **21:56, old instance (1.2.0, no guard):** Creeping Woods' `CreatureFromTheWoodsRaytracingProcedure` called vanilla `clip`. The restart loop then hit the missing Almanac four times (21:56–21:58) until the sync.
- **22:05, new instance (1.3.0):** Rake's `RakeRaytracingProcedure` called `RayGuard.clip`. The guard judged the span loaded and called vanilla `clip`, which blocked.

**The 1.3.0 bug.** The span check used `hasChunk`. In `ServerChunkCache` that only tests that the chunk has a ticket at full-chunk level, not that its load has finished, so a chunk still generating ahead of the player passed.

**The 1.3.1 fix.** The check now uses `ServerChunkCache.getChunkNow`. It reads the chunk's FULL future with `getNow` (verified in bytecode) and never waits, so a chunk that hasn't finished counts as not loaded and the cast returns vanilla's miss result.

**Testing.** 1.3.1 boots on the scratch server: 27 patches applied, no errors. It's installed in this instance (`mods/` and `local/`).

**VM.** The automated upload was refused by the session's permission check. Run `python scripts/sync_server_mods.py` (swaps 1.3.0 for 1.3.1), then restart. Until then the live server can hit the same hang. **Later that night** 1.3.1 was deployed, and 1.3.2 (below) then replaced it on the VM.

**Seen in passing.** The long-standing Better Chunk Loading mixin failure is a conflict with Canary's `chunk_access.ServerChunkCacheMixin`.

### `spooky_tick_fixes` 1.3.2 — The Obsessed waits for chunks (2026-11-06, night) — *client instance + VM*
- **What happened:** after the 22:11 restart (VM still on 1.3.0), Milton___ logged in at x −5537, far outside the pre-generated ±2,048 area. The profile (22:13–22:18, one 27.6 s tick) shows the Server thread blocked on chunks still being generated:
  - Creeping Woods ray casts 30 s, Rake 25 s, shadow figure 5 s. All three went through the 1.3.0 guard bug, which 1.3.1 fixes; 1.3.1 was deployed after this profile.
  - The Obsessed's `PlayerTickProcedure` block reads 19 s. The ray-cast guard doesn't cover these.
- **The 22:10 crash** was the same 1.3.0 bug, through the Creeping Woods ray cast.
- **1.3.2:**
  - Skips The Obsessed's per-player tick while any chunk within 3 chunks of the player isn't finished (`getChunkNow`). Its reads reach about 20 blocks (random offsets ±10, 1–15, 1–20).
  - Switches: `obsessedWaitForChunks`, `obsessedChunkRadius`.
  - Boot-tested on the scratch server: 28 patches, no errors. Installed in this instance.
- **VM:** uploaded with the sync (1.3.1 moved to `mods-removed/`; 281 jars, parity verified). Takes effect on restart.
- **Also advised:** a world border at the pre-generated edge (`/worldborder center 0 0`, `/worldborder set 4096`) until the world is pre-generated further out.

### Simple Zoom added (2026-11-06, night) — *client instance only*
- **Mod:** Simple Zoom 1.0.1 by MrRockis, `simplezoom-forge-1.20.1-1.0.1.jar` ([Modrinth](https://modrinth.com/project/zoom-simple)). It's an OptiFine-style zoom: hold **C** (rebindable under Controls → "Simple Zoom") to zoom 3×, and scroll while holding to change it, up to 50×. The only dependency is Forge 47.2.30+.
- **Client-only:** both of its mixins (`GameRenderer`, `MouseHandler`) are in the mixin config's `client` list. It has no network code, so players without it can still join. Its `mods.toml` line `side = "CLIENT"` isn't a field Forge 1.20.1 recognises, so nothing stops it loading on a dedicated server. `simplezoom-` was added to `CLIENT_ONLY_GROUPS` in `scripts/sync_server_mods.py`. The sync dry run against the VM: 305 active − 24 client-only = 281, parity unchanged, nothing to upload.
- **Docs:** `modlist.md`, `modlist.json` and the README at-a-glance block were regenerated. A `simplezoom` category rule was added to `tools/generate_modlist.py` (Storage, QoL & utility). `docs/SERVER_MODS.md` was regenerated from the VM.
- **First jar removed:** a 2023 mod also called "Simple Zoom" (`simple_zoom-1.20.1-1.0.jar`, `com.anubis.simpleZoom`, an unedited Forge example `mods.toml`) was added first and then replaced by this one.
- **Not yet in "The Correction"** (the instance actually played). Add it there through CurseForge.
- **Untested:** zoom with Oculus + Hysteria (TAA) and zoom looking through Immersive Portals.
- **Gameplay note:** free zoom makes long-range scouting easier than the spyglass or Hexerei's Reading Glasses. It doesn't extend any horror mob's server-side look check.

### Configured added (2026-11-07) — *client instance only*
- **Why:** JEI 15 has no settings screen of its own. Its wrench button looks for a screen another mod registers, and without one it prints *Install the "Configured" mod to access the in-game config*.
- **Mod:** Configured 2.2.3 by MrCrayfish, `configured-forge-1.20.1-2.2.3.jar` (CurseForge only; the Modrinth project named "Configured" is an unrelated Fabric mod). The only required dependency is Forge 47.1.47+. JEI, Framework (already in the pack, 0.8.0) and Catalogue are optional links. It gives JEI a settings screen, and also gives one to every mod with Forge configs, through Mods → *mod* → Config.
- **Client-only:** both of its mixins are in the mixin config's `client` list. Its `configured:play` channel accepts any version or a missing peer (both checks return true), so players without it can still join. `configured-` was added to `CLIENT_ONLY_GROUPS` in `scripts/sync_server_mods.py`, so it can't be used to edit the VM's server configs from a client. The sync dry run: 306 active − 25 client-only = 281, parity unchanged.
- **Docs:** `modlist.md`, `modlist.json`, the README block and `docs/SERVER_MODS.md` were regenerated. A `configured` category rule was added to `tools/generate_modlist.py`.
- **Note:** the pack's client-side config choices are now one click away for every player. They could always edit the files.
- **Not yet in "The Correction".**

### `farsight_immptl_compat` 1.0.1 — held chunks follow the full render distance (2026-11-07) — *client instance only*
- **Change:** the hold radius was `min(render distance, 32)`. The default limit is now **64**, Farsight's `maxRenderDistance` and so the slider's maximum, which means it follows the client's render distance at any setting. Before, a slider above 32 drew empty space past 32 chunks. The server's view distance is now 8–12 (dynview), so held chunks are everything a player sees beyond that.
- **Cost:** a render distance of 64 holds about 16,600 chunks against 4,200 at 32: four times the chunk memory on the client heap, and four times the meshes on the GPU. Players on a small client heap can set `-Dfarsight_immptl_compat.maxHoldRadius=32` (the 1.0.0 behaviour) in their JVM arguments, or lower the slider.
- **Build:** `tools/farsight_immptl_compat/` (`HeldUnloads.MAX_HOLD_RADIUS`, `mods.toml`/`MANIFEST.MF` 1.0.1, README). `build.py --install` replaced 1.0.0 in `mods/` and `local/`. The built jar was checked: its default is 64.
- **Client release needed:** the jar is client-only and never synced to the VM. Players who keep 1.0.0 keep the 32-chunk limit; nothing breaks either way.
- **Not yet in "The Correction".**

### The Obsessed chunk radius 3 → 6 (2026-11-07) — *client instance + VM config; applies on restart*
- **Why:** after the 22:25 restart, 1.3.2 was running, but The Obsessed still made the server thread wait 277 s for chunks in 35 minutes (spark, 22:27–23:02; stalls of up to 29 s). Read from its bytecode: the stalker spawn search keeps a scan point in the player's data (`idk_x`/`idk_z`) and checks blocks there. The point moves 10 blocks at a time while a player is within 37.5 blocks of it, and resets to the player when none is within 70. So the reads land about 37–80 blocks away, which is at most 5 chunks from the player's chunk. A radius of 3 (48 blocks) missed them.
- **Change:** `obsessedChunkRadius` = **6** in `config/spooky_tick_fixes-common.toml`, locally and on the VM. The VM's previous file is kept as `spooky_tick_fixes-common.toml.bak-20261107-radius3`. 6 chunks gives one chunk of margin over the 5-chunk reach. The check costs 169 chunk lookups per player per tick, which is negligible.
- **Not hot-reloaded:** Forge's config watcher didn't notice the change on the VM, either for the replaced file or for an in-place append, so it takes effect at the next restart. The code default stays 3 until the next build; the config file overrides it.
- **Remaining gap:** with two players, one player's scan point can sit near the other player, outside both radius checks. A 1.3.3 that checks the chunks around the scan point itself would close it.

### Repo cleanup: personal files purged from git history, orphaned configs removed (2026-11-07) — *repo only; no gameplay change*
- **Purged from every commit and tag** (history rewritten and force-pushed, so every commit ID changed; commits now use the GitHub noreply email): `servers.dat`, `servers.dat_old`, `config/jei/world/`, `config/embeddium-fingerprint.json`, `config/chunky/tasks/`, `config/xaeropatreon.txt`, `config/oculus.properties`, `config/spark/`, `config/crash_assistant/`, `rhino.local.properties`, `.qmenu_opened.marker`, `observable_announce`, `win_event1783619581648.txt`, `user-prefs.json`. The files are still on disk and are now in `.gitignore`. The v1–v13 release zips were repacked without them, except `oculus.properties`, which still turns Hysteria on by default.
- **Removed, stray copies:** `minecolonies-serverNEW.toml` (identical to `defaultconfigs/minecolonies-server.toml`), `config/chunksending.json.v41`, `XaeroWaypoints_BACKUP240807/`, `config/ftb-quests-entity-vis/` (the mod reads `config/ftbquestsentityvis/`).
- **Removed, mod not installed:** `config/coldsweat/`, `mobdismemberment-client.toml`, `theforgotten-common.toml`, `jeioptimizer-client.toml`, `firstperson.json`.
- **Correction (2026-11-07, later):** `transition.json` and `trender.json` were also removed here as orphans, but they aren't. They belong to tr7zw's TRansition and TRender libraries, bundled inside Entity Culling 1.11.3 and Not Enough Animations 1.12.7. The game wrote them again at the next start, and they are tracked again.
- **Removed, mod disabled on both sides:** TerraFirmaCraft (`tfc-client.toml`, `tfc-common.toml`, `defaultconfigs/tfc-server.toml`, and the OpenLoader packs `TFCFoodHunger`, `TFCLightItems`, `TFCVanillaCraftingTable`, `TFCHideInventoryTabs`, `TFCNoSizeTooltip`, `data-disabled/TFCModdedFood`), DimThreads (`dimthread-common.toml`) and The Man From The Fog (`man_config.toml`). Re-enabling one starts from its default config; the old files are in git history and the local `*.bak` copies.
- **Left in place:** `kubejs/server_scripts/tfc_vanilla_food.js` still runs. Without TFC it only writes `tfc:` data that nothing reads.

### Watchdog crash 01:44 (villager bed), `spooky_tick_fixes` 1.3.3 (2026-11-07) — *client instance + VM*
- **Crash:** `ServerHangWatchdog`: one tick took 120 s. The server thread was in vanilla `SleepInBed.checkExtraStartConditions`. It reads the block at a villager's remembered home bed *before* checking that the bed is within 2 blocks, and that bed's chunk wasn't loaded. The chunk still had to be generated. Finishing it needs a ring of neighbouring chunks generated too, and the thread dump shows the world-generation threads busy with noise and YUNG's Better Caves carving. It's not a deadlock, just more than 120 s of work on this CPU. ilovepuffles111 was online, probably exploring. Chunky had finished at 00:51. Canary and Observable are named as "suspected" only because they appear in the stack.
- **1.3.3:**
  - `villagerSleepLoadedOnly`: the bed read answers air when the chunk isn't loaded. A ticking villager can't be within 2 blocks of a bed in an unloaded chunk, so the result is unchanged.
  - `obsessedReadsLoadedOnly`: all 57 of The Obsessed's block reads answer "not empty" / air in unloaded chunks. This closes the two-player and after-teleport gaps left by the radius check.
  - `obsessedChunkRadius` now defaults to 6.
  - `offThreadChunkLog` (diagnostic) logs the first chunk request from each caller on another thread, with its stack, to name the mod behind the between-tick stalls.
  - Details are in `tools/spooky_tick_fixes/README.md`.
- **Checked before building:**
  - No other mod in the pack patches `SleepInBed` or The Obsessed's `PlayerTickProcedure`.
  - Canary `@Overwrite`s `ServerChunkCache.getChunk`, so the logger's mixin has priority 1500 and applies after it.
  - Every Obsessed `isEmptyBlock` / `getBlockState` call site was read in bytecode to choose the safe stub value.

### `spooky_pregen` 1.0.0: pre-generation while the server is empty (2026-11-07) — *client instance + VM*
- **What:** a new pack-built mod (`tools/spooky_pregen/`, jar in `mods/` + `local/`).
  - It drives Chunky through its API, and only on a dedicated server; on clients and in singleplayer it does nothing.
  - Once the server has been empty for `emptyGraceMinutes` (5), it works through `jobs`. Each job grows a square around a point from `radius` to `maxRadius` in `step`s.
  - Then it fills outward from the **edge** of the generated area, nearest to the centre first. The edge comes from the region files' 4 KB location tables, so chunks that already exist are never targeted.
  - It pauses the moment a player logs in, and resumes after a restart.
  - Guards: `minFreeDiskGb` (2.0) and `maxDiskGrowthMbPerDay` (1000).
  - Commands: `/pregen status|next|hold|resume|skip|reload`.
  - Full description: `tools/spooky_pregen/README.md`.
- **Config:** `config/spooky_pregen.json`. Its first job widens the overworld square from ±2048 to ±3072 in 256-block steps; then the overworld edge rule fills within 4096 of 0,0. On the VM: the jar was uploaded with the sync (282 jars, parity verified) and the config to `config/` (read back identical). It takes effect at the next restart.
- **Found while testing:** Chunky's `GenerationCompleteEvent` fires when a task's loop has *handed out* its last chunk. Up to 50 chunks can still be generating at that point, and the event also fires on pause and cancel. The first build trusted it, so tasks overlapped. Completion is now a progress event with `complete()` set, which Chunky raises once, when the finished count reaches the total.
- **Tested** on the scratch test server (281 jars + 1.3.3 + this) with tiny squares and near-zero waits:
  - the job grew 32 → 64 → 96;
  - 48 edge windows followed, nearest first, and **0** of the 54 tasks across both boots overlapped;
  - `hold` stopped new work, and `status` / `reload` worked;
  - an unknown dimension was skipped with one warning;
  - a task paused at shutdown resumed after a restart and finished.
  - Untested headless: the pause on player login. It's the same `pause()` path as `hold`, and it runs at HIGHEST priority on `PlayerLoggedInEvent`.
- **Limit:** a pause stops Chunky from handing out more chunks, but chunks already handed out (up to Chunky's 50 in flight) finish generating. On the VM that is roughly 10–15 s of extra generation right after someone logs in. `-Dchunky.maxWorkingCount=16` in `user_jvm_args.txt` would shorten it. Generation is serial anyway, so fewer chunks in flight shouldn't cost throughput.

### Watchdog crash 03:08 (Immersive Weathering), `max-tick-time`, `spooky_tick_fixes` 1.3.4 "chunk-safe ticking" (2026-11-07) — *client instance + VM*
- **Crash 03:08:58:** a 120 s tick. A fluid tick's neighbour update reached Immersive Weathering's fluid-generator check (`SelfFluidGenerator$AdjacentBlocks.isMet`), which read a block in a chunk that still had to be generated while ilovepuffles111 explored around x 6000–7000. The server stayed down until restarted.
  - The 1.3.3 off-thread logger also caught **vanilla `Cat.finalizeSpawn`**: village cats placed during generation look up structures through the live world (`structure_references`). Cheap, but it confirms the logger works.
  - Its other catches (Immersive Petroleum reservoirs, Guard Villagers' `getVariantFromBiome`, Alex's Caves) only read biomes.
- **`max-tick-time` 120000 → 300000** in the VM's `server.properties`. The backup is `server.properties.bak-20261107-ticktime`, and the change applies at the next restart. A generation stall now freezes the server for up to 5 minutes instead of crashing it; a true deadlock still ends in a crash after 5 minutes.
- **1.3.4, chunk-safe ticking (`[chunkSafeTicking]`, on by default):** one fix for the whole class instead of a patch per mod.
  - **How:** while a dimension ticks, block and fluid reads on the server thread never generate terrain.
    - loaded chunks: unchanged;
    - chunks finished in memory or on disk: vanilla (a disk load, about 10 ms);
    - terrain that doesn't exist yet or is still being generated: a **barrier** block at once.
  - **Not affected:** player actions, commands, worldgen and chunk post-processing.
  - **Monitoring:**
    - the first barrier read per caller is logged with its stack;
    - `/tickfixes stats` has the counts;
    - `/tickfixes probe <x> <z>` tests a read;
    - `exemptCallers` lets a mod keep generating on the spot, and `enabled = false` restores vanilla.
  - **Also in 1.3.4:**
    - the off-thread logger now hooks only the off-thread branch, so it costs nothing on the main thread;
    - the Fragile Torches pre-check no longer waits for chunks.
  - **Details:** `tools/spooky_tick_fixes/README.md`.
- **Tested** on the scratch test server: 33 patches applied, with no mixin errors.
  - `probe 0 0` (loaded): fast path, 8 µs.
  - `probe 400 8` (generated but unloaded): real terrain (andesite) loaded from disk in 7–11 ms.
  - `probe 300000 300000` (never generated): barrier with no generation; 4.3 ms the first time, 86 µs once cached.
  - While `spooky_pregen` generated 29 squares under the guard, no other read was answered with a barrier.
  - **On the VM:** uploaded with the sync (1.3.3 moved to `mods-removed/`; 282 jars, parity verified). It takes effect at the next restart.
  - Not yet measured: the overhead on the VM under real load. Compare a spark profile after deploying with the median of about 40 ms before.

### `spooky_pregen` 1.1.0: blob mode, players first (2026-11-07) — *client instance + VM*
- **Why:** the pre-generator should work where people actually go. The generated overworld is 32 islands (from the region tables, 2026-11-07): the main area around spawn (88,091 chunks), a western island (9,734), two far-eastern ones (about 1,250 each) and 28 small specks left by fast travel.
- **Blob mode** (the edge rule's new default):
  - **Seed:** the edge chunk of any non-speck island that is nearest to an **anchor**, meaning a player's last logout position or bed.
  - **Grow:** a square centred on it, from 256 to 1024 radius in 256 steps.
  - **Repeat:** recompute the edge and pick again.
  - **Anchors** are read once from `world/playerdata` and updated on every logout. Specks (under 64 chunks) are ignored unless an anchor is near them.
  - **Full description:** `tools/spooky_pregen/README.md`.
- **On the VM:**
  - the jar was uploaded with the sync (1.0.0 moved to `mods-removed/`; 282 jars, parity verified);
  - the config was uploaded and read back identical, with the 1.0.0 version kept as `spooky_pregen.json.bak-20261107-v100`;
  - it takes effect at the next restart.
- **Pack config:** the `spawn-3072` job was removed. Jobs run before edges, and it was about 80,000 chunks (roughly 4.5 h on the VM) away from where players are. The edge rule is now blob mode with no distance limit.
- **On the VM's real data:**
  - **Anchors:** ilovepuffles111 at (7130, −4859), and Milton___ at (−4197, −3432) with his bed beside it.
  - **Simulation:** blob 1 at (−4136, −3176), Milton's area, 13,348 new chunks; blob 2 at (7352, −5096), ilovepuffles111's, 15,096; then alternating, about 7,500–8,600 each (25–50 min per blob at 5 chunks/s).
- **Tested** on the scratch test server with tiny blobs and a test anchor at (700, 650):
  - the first blob centred at (696, 760), 112 blocks from the anchor, and grew 32 → 64 → 96 before a new blob was picked;
  - 15 tasks ran with 0 overlaps;
  - `status` shows the blob, the reason it was picked and the anchors.
- **Separate finding, nothing changed:** Better Chunk Loading's `ServerChunkCacheMixin` fails to apply on every boot ("@At INVOKE … cannot inject into m_7587_"), because Canary overwrites `ServerChunkCache.getChunk`. So its chunk-priority feature has never been active on the VM.

### `spooky_pregen` 1.2.0: `/pregen start` and `/pregen stop` (2026-11-07) — *client instance + VM*
- **`/pregen start` (force-start):** begins or resumes work immediately, without waiting for an empty server, the grace time or the check interval, and **keeps running with players online**. It clears a hold.
  - **Lasts until** `/pregen stop`, `/pregen hold` or a restart. Force mode is never saved, so it can't keep generating under players by accident after a reboot.
  - **Guards:** the disk guards still apply.
  - **Expect extra load while players are on:** generation competes with their chunk loading. 1.3.4's chunk-safe ticking still stops their ticking code from waiting on it.
  - **Changed in 1.3.1 (below):** force mode now starts alongside the players already online, but the next player to join pauses the task and ends force mode.
- **On the VM:** uploaded with the sync (1.1.0 moved to `mods-removed/`; 282 jars, parity verified). It takes effect at the next restart; the blob in progress carries on after it.
- **`/pregen stop`:** ends force mode and returns to running only while the server is empty. With players online, the current task pauses at once.
- **`/pregen hold`** now also ends force mode. `/pregen status` shows `FORCED by /pregen start N min ago` while it's active.
- **Docs:** README (Custom commands), `tools/spooky_pregen/README.md` (Commands) and the `PregenCommand` Javadoc.
- **Tested** on the scratch test server with a 10-minute grace, so the normal path couldn't start anything:
  - `/pregen stop` without force mode replied "not in force mode";
  - `/pregen hold`, then `/pregen start`: the hold was cleared and the paused blob resumed in the same second;
  - `status` showed `FORCED`, and blobs kept chaining;
  - `/pregen stop` ended force mode. With nobody online, the running square continued, as the normal rules allow.
  - Not testable headless: running with players online. Force mode only bypasses the player check, and the login pause is the same check.

### `spooky_pregen` 1.3.0: blob size as a command and a smaller default (2026-11-07) — *client instance + VM*
- **Smaller default:** the pack's `blobMaxRadius` went from 1024 to **512**, so blobs grow 256 → 512 and cover 1,024 × 1,024 blocks. That's about a quarter of the old size per blob, finished in roughly a quarter of the time, and placed closer to where players are.
- **`/pregen radius [<blocks> | reset]`:** shows or sets the blob size on the fly (16–8192).
  - The setting is kept in the state file, so it survives restarts, and it overrides the config until `reset`.
  - It applies to the blob in progress. If that blob is already larger, its running square is cancelled (what it generated stays) and a new blob is picked.
- **Config edits now apply to the blob in progress too:** a changed `blobMaxRadius` takes effect after `/pregen reload`. Before 1.3.0, a blob kept the size it was picked with.
- **Status:** `/pregen status` shows the blob radius and whether it comes from the config or the command.
- **On the VM:** the jar was uploaded with the sync (1.2.0 moved to `mods-removed/`; 282 jars, parity verified), and the config with radius 512 was uploaded and read back identical (the old one is `spooky_pregen.json.bak-20261107-r1024`). Both take effect at the next restart.
- **VM at the time:** the first blob, at (6920, −4616) near ilovepuffles111, was already on its last step (radius 1024), started 05:07.

### `tools/pregen_map.py --serve`: a live pregen map (2026-11-07) — *this PC only*
- **Background:** `tools/pregen_map.py` (added with `spooky_pregen` 1.2.0) reads the VM over FTP and writes a static map, `tools/build/pregen_map.html`. It shows what is generated, the selected blob, the square generating now and what is still to come.
- **What it does:** `python tools/pregen_map.py --serve [--interval 10] [--port 8765] [--no-browser]` runs a small web server bound to 127.0.0.1. It re-reads the VM every `--interval` seconds (default 10, minimum 5), and the page repaints in place, keeping its zoom, position and hover.
- **Polling:** the page asks the local server every half interval (2–10 s), and not while its tab is hidden.
- **Page status:** the eyebrow shows "live · read HH:MM:SS". It says so when the VM can't be read (the last map stays) or when the server has stopped.
- **Reads only what changed:**
  - region headers of unchanged files are reused;
  - rotated logs are read once, and `latest.log` from where the last read stopped (FTP `REST`);
  - terrain is held in memory, and a region file being written is downloaded again at most every 30 s;
  - relief shading is recomputed only around chunks that changed.
- **Measured against the VM while generating:** first read 8.7 s; each 10 s cycle took 8–12 s, including a terrain refresh every 30 s. A 204 means nothing changed; new data is about 5 MB to localhost.
- **Not published:** the published Claude artifact stays a static snapshot (`--fragment`), because its sandbox can't reach the LAN.

### `spooky_pregen` 1.3.1: a join always pauses; blob size applied at startup (2026-11-07) — *client instance + VM*
- **Problem 1, generating with a player online:** `/pregen start` was run at 05:28. ilovepuffles111 joined at 05:37:44, and generation carried on: force mode skipped the join pause by design (1.2.0). That contradicted the mod's purpose.
- **Fix:** force mode now means "start now, without waiting for the server to be empty". It still runs alongside whoever is online when it starts, so an operator in game can use it. **The next player to join pauses the task and ends force mode**, logged as `force mode ended: <name> joined`. `/pregen stop`, `/pregen hold` and a restart still end it too.
- **Problem 2, old size after a restart:** after the 1.3.0 restart, the paused blob square resumed at radius 1024 although the config said 512. In 1.3.0 a new config size reached the blob only at its next pick, and only `/pregen radius` cancelled a square that was already larger. A paused square resumes without a pick, so it carried on at the old size.
- **Fix:** the blob size in force is now applied at startup, on `/pregen reload` and by `/pregen radius`, through one routine. A paused or running square larger than the size is cancelled (what it generated stays). A blob whose next step is larger than the size is finished, so a new one is picked. A size between two steps can end a blob one step early: with steps 256 → 512, `/pregen radius 384` ends a blob that has only done 256.
- **Tested headless** on the scratch server:
  - A paused radius-128 square with the config at 96 was cancelled at boot, logged as `blob size from the config applied; ... now grows to radius 96 (was 128); cancelled its radius-128 square`.
  - `/pregen start` then ran seven squares back to back, and `/pregen hold` stopped the work.
  - Not testable headless: the join pause itself. It is the same pause call the normal empty-server rule uses, now without the force-mode exception.
- **On the VM:** the jar was uploaded with the sync (1.3.0 moved to `mods-removed/`; 282 jars, parity verified). It takes effect at the next restart.

### Chunky: silent by default (2026-11-07) — *client instance + VM*
- `config/chunky/config.json`: `"silent": true` (was `false`).
- **What it hides:**
  - the progress boss bar that Chunky shows every operator while a task runs;
  - the `[Chunky] Task running for …` console line, about one a second while generating. These lines made up most of the VM's `latest.log`.
- **Still available:** `/chunky progress` and `/pregen status` show progress on demand, and `Task finished` lines are still logged.
- **spooky_pregen is unaffected:** Chunky posts the progress event that spooky_pregen uses for completion before its silent check. Tested: seven squares completed in a row with no `Task running` lines.
- **When it applies:** Chunky reads the file only at startup (and writes it back then; task progress is saved separately, so a shutdown doesn't overwrite it). In game, `/chunky silent` toggles it at once, but only until the next restart.
- **On the VM:** uploaded and read back identical; the old file is `config/chunky/config.json.bak-20261107-silent`. It takes effect at the next restart.

### `spooky_pregen`: no daily growth cap (2026-11-07) — *client instance + VM*
- `config/spooky_pregen.json`: `maxDiskGrowthMbPerDay` 1000 → **0 (off)**. At the user's request, pre-generation now runs for as long as the server is empty, with no daily limit.
- **What still stops it:**
  - `minFreeDiskGb` 2.0: it holds while the world's disk has less than 2 GB free, and continues if space is freed;
  - a player joining;
  - `/pregen hold`.
- `maxDistance` was already 0 (unlimited), so blobs keep spreading outward from the edges nearest the players.
- **Why:** the cap tripped on the VM at 08:20 ("the world grew 1006 MB today (limit 1000 MB)") and held pregen for the rest of the day.
- **On the VM:** uploaded and read back identical; the old file is `config/spooky_pregen.json.bak-20261107-nocap`. It applies at `/pregen reload` or the next restart.
- **Disk:** the VM disk is 12.6 GB. At the morning's rate (about 1 GB in 2 hours), pregen will reach the 2 GB floor and hold there. That leaves 2 GB for logs, backups and player growth. `/pregen status` shows the free space.

### JEI on the server (2026-11-07) — *sync script + VM*
- **Symptom:** JEI's recipe "+" button (move a recipe's ingredients into a crafting table) failed with *Server must have JEI installed*.
- **Cause:** since the sync script was first written (July, commit `b034083`), `jei-` was in `CLIENT_ONLY_GROUPS` as a "client-only utility", so the VM never had it. No crash or reason was recorded. JEI is a two-sided mod. The client asks the server, over JEI's channel, to move the items (`PacketRecipeTransfer`), and the server does the move.
- **Change:** `jei-` was removed from `CLIENT_ONLY_GROUPS` in `scripts/sync_server_mods.py`, with a comment saying why. Sync dry run against the VM: 307 active − 24 client-only = 283, and the only change is `copy + jei-1.20.1-forge-15.56.0.205.jar`.
- **Server side of JEI:** recipe transfer, plus the cheat-mode give/delete buttons. Its server config defaults (read from the jar) are `enableCheatModeForOp = true`, `enableCheatModeForCreative = true` and `enableCheatModeForGive = false`, so survival non-ops get recipe transfer and nothing more. It adds no ticking work.
- **On the VM:** uploaded with the sync, and parity was verified (283 jars). `docs/SERVER_MODS.md` was regenerated from the VM. JEI loads at the next restart, and the button works after that.

### Antlers Wendigo: keeps away, attacks only when provoked — `spooky_tick_fixes` 1.4.0 (2026-11-07) — *client instance + VM*
- **Why:** two players were attacked without warning, one inside their base. The second time it was hunting another player, who logged off; it then switched to the next nearest player, in a `terralith:glacial_chasm`. They also heard a "loud roar" that several players got at the same moment. The user wants a creature that a player provokes, knowingly or not.
- **What the mod does (read from antlers-1.0.0 bytecode):**
  - **Night hunt:** about every 25 ticks it targets the **nearest** player within ±300 blocks, with no line of sight needed. It then sprints, with Speed III within ±150. Its player target goal also picks any survival player it can see within 60 blocks.
  - **Sounds:** a stalking roar at volume 100, sent to the nearest player within ±355 blocks (heard up to 1,600 blocks) about once a minute. When it targets a survival player within ~12 blocks: a 12.5 s freeze and an "I see you" scream at volume 40 on the **Master** channel, which everyone within 640 blocks hears at once.
  - **Lifecycle:** it never despawns (`removeWhenFarAway` is false). At dawn it swaps to a daytime form and back at dusk, as fresh mobs that bypass the sanity spawn gate. It spawns in any Overworld biome on a non-stone floor in the dark, one per 1,000 blocks, weight 2 (already lowered by `HorrorSpawnRarity`).
- **Change (user's choices):** aggressive when provoked, but otherwise it actively keeps away from people.
  - **Calm:** it moves away from any player it can see within 16 blocks (its daytime form already ran from players).
  - **Provoked by:**
    - hitting it, at any time;
    - at night, being within 8 blocks in its sight (4 while sneaking). Because it keeps away, that means surprising it, cornering it or running it down;
    - at night, a 5 s continuous stare within 24 blocks, with your crosshair on it, clear sight both ways, and the Wendigo not walking away at the time. The first version used 2 s within 32 blocks.
  - **Once provoked:** it chases only that player, and calms down when that player leaves, dies, changes dimension or gets 32+ blocks away. The first version used 64.
  - **After calming down:** it retreats from everyone within 48 blocks; only a hit re-provokes it. It vanishes once nobody is within 24 blocks and nobody within 64 can see it, or after 60 s once nobody is within 16.
  - **Sounds:** no stalking roar. It roars every 15–30 s only while provoked, and the roar and the scream are both capped at 128 blocks.
  - **Despawning:** it and its daytime form now despawn like other monsters.
  - **Unchanged:** the freeze, the scream and the hit-and-run removal after an attack.
  - Details are in `tools/spooky_tick_fixes/README.md` (1.4.0) and `config/spooky_tick_fixes-common.toml` `[antlersWendigo]`.
- **Tested on the scratch server (no player can join headless):**
  - all five patches log `-> applying (antlers)`, with no injection errors after a Wendigo was summoned (that's when its goal classes load);
  - the `[antlersWendigo]` section is generated with its defaults;
  - summoned Wendigos show as `calm` in `/tickfixes wendigo` (the per-tick hook runs), and at daybreak they still turned into the daytime form;
  - the final build, with the keep-away goal and the retreat, booted the same way, and three summoned Wendigos built their goals without errors.
  - **Not tested:** keeping away, the provocation itself, the calm-down, the retreat and vanishing, and despawning. All of these need a player. Check them in game.
- **On the VM:** uploaded with the sync (1.3.4 moved to `mods-removed/`; 283 jars, parity verified). Live from the next restart, when Forge adds `[antlersWendigo]` with these defaults to the VM's `config/spooky_tick_fixes-common.toml`.
- **Installed:** `mods/` and `local/` (1.3.4 removed). The new config section was added to `config/spooky_tick_fixes-common.toml`, and `modlist.md`/`modlist.json` were regenerated.
- **Check in game:** `/tickfixes wendigo` lists the loaded Wendigos and their state. The server log gets `wendigo at x y z provoked by <player>: <reason>`, `… calmed down: …; retreating` and `… vanished after calming down`.
- **Revert:** `[antlersWendigo] enabled = false` (and `despawnNormally = false`), or go back to the 1.3.4 jar.
- **Also found, not changed:** CANTBREATHE plays a cave noise to **every** survival player in the Overworld on the same tick, every 12–22 min once its spawn timer is past half (VM `world/serverconfig/cant_breathe-server.toml`: any height, any light, surface allowed, all biomes, `only_play_noise_to_target = false`). Each player gets a random clip. Setting `only_play_noise_to_target = true` would make it the chosen victim only.

### Wendigo loot — `WendigoTrophies` datapack (2026-11-07) — *client instance + VM*
- **Why:** Antlers ships no loot table for the Wendigo or its daytime form, and neither has custom drop code. Killing a 300-health Wendigo gave 30 XP and nothing else. The user wanted something exotic and forest-themed.
- **Change:** OpenLoader pack `config/openloader/data/WendigoTrophies/`, with `data/antlers/loot_tables/entities/wendigo.json` and an identical `wendigomad.json`. Both apply only when a player gets the kill (`killed_by_player`):
  - **Always:** 1–2 Moose Antlers (`alexsmobs:moose_antler`), up to +1 per Looting level. Two antlers, an iron ingot and 2 string craft Alex's Mobs' Antler Headdress.
  - **1 in 3 kills** (+10% per Looting level): exactly one forest relic, picked by weight:

    | Item | Mod | Weight | Chance per kill (no Looting) |
    |---|---|---|---|
    | Antler Headdress (`alexsmobs:moose_headgear`) | Alex's Mobs | 2 | 7.3% |
    | Rooted Boots: slowly refill hunger while walking on grass | Artifacts | 2 | 7.3% |
    | Thorn Pendant: chance to damage attackers | Artifacts | 2 | 7.3% |
    | Whirlisprig Charm: summons a forest sprite that produces natural materials | Ars Nouveau | 2 | 7.3% |
    | Druidic Tome: nature-school spellbook | Iron's Spellbooks | 1 | 3.7% |
- **Not affected:** a Wendigo that vanishes (after calming down, its hit-and-run, or the dawn/dusk swap) is removed, not killed, so it drops nothing.
- **Item IDs** were checked against the jars' item models.
- **Tested on the scratch server:** both tables load with no loot warnings. A test copy without the kill condition, rolled 25 times, gave the antlers plus one relic every time. The real table dropped nothing without a player killer, as intended.
- **On the VM:** the pack folder was uploaded to `config/openloader/data/WendigoTrophies/` and read back identical. It loads at the next restart.

### SimpleBackups: every 6 hours, keep 5 (2026-11-08) — *client instance + VM*
- `config/simplebackups-common.toml`: `timer` 120 → **360** minutes and `backupsToKeep` 10 → **5**, at the user's request.
- **Why keep fewer:** pre-generation grew the world, and full backups grew from about 1 GB (2026-11-06) to about 3.3 GB (2026-11-07). Ten of them would be about 33 GB, past the 25 GB `maxDiskSize`. Five are about 17 GB, which still covers at least a day and a half at 6-hourly backups.
- **Unchanged:** full backups (`FULL_BACKUPS`), the 25 GB cap, and `noPlayerBackups = false`, so nothing is backed up while the server is empty. Pre-generation runs only while it's empty.
- **Effect:** at its next backup, SimpleBackups deletes the oldest backups beyond five.
- **On the VM:** uploaded and read back identical; the old file is `config/simplebackups-common.toml.bak-20261108-every2h`. Forge's config watcher hasn't fired on the VM before, so expect it to apply at the next restart.
- **Disk:** the backups live in the server's `simplebackups/` folder and come out of the same free space `spooky_pregen` stops at (2 GB). A 3.3 GB backup needs that much room when it runs.

### Quest book: real checks instead of click-to-complete (2026-11-08) — *client instance + VM*
- **Why:** 15 of the 186 tasks were FTB Quests "checkmark" tasks, which complete when the player clicks them. They were concentrated in the chapters players see first: The Broken World (6 of 8 tasks), The Second Civilisation (3 of 3) and Field Reports (4 of 10). The other 171 tasks (items, advancements, dimensions) already check something, and every item, advancement, dimension, reward and icon they name exists in the pack.
- **What FTB Quests can check here** (read from the 2001.4.22 jar):
  - item (exact item only; there's no filter mod for tags), advancement, dimension, biome and structure (both accept `#tags`), stat (vanilla custom stats), kill, location and observation;
  - "stage" tasks, which here read vanilla **player tags**: no stage mod is installed, so FTB Library falls back to its entity-tag provider. A KubeJS script can therefore complete one with `player.addTag(...)`. There's no KubeJS bridge mod for FTB Quests (FTB XMod Compat) in the pack.
  - A quest with **no tasks** counts as complete once its dependencies are.
- **Changes**, following the design doc (`docs/ftbquest.md`):

  | Quest | Was | Now |
  |---|---|---|
  | The Broken World / A World Left Behind | click | structure `#oldcivilisation:abandoned_places` (179 ruins, dungeons and abandoned buildings from 21 mods and vanilla) |
  | The Broken World / The First Survey | 3 clicks | 5 biome tasks (forest, plains, river, mountain, taiga), 3 structure tasks (village, mineshaft, ruined portal), 1 biome `#forge:is_underground` |
  | The Broken World / The Silent Nights | click | stage `oc_q_silent_nights`: 5 whole nights at least 256 blocks from spawn without sleeping |
  | The Broken World / The First Night | click | stage `oc_q_first_night`: a whole night survived under a roof for at least 75% of it, or slept through |
  | Field Reports / Into The Dark | click | biome `#forge:is_underground`, structure `#oldcivilisation:abandoned_places`, biome `#oldcivilisation:dense_forests` |
  | Field Reports / The Forest Does Not Forget | click | structure `#oldcivilisation:creeping_woods` (all 24 Creeping Woods sites) |
  | Field Reports / Below | click | biome `#alexscaves:alexs_caves_biomes`, and `minecraft:deep_dark` |
  | Field Reports / The Watchers | click | stage `oc_q_watchers`: 10 whole nights 256+ blocks from spawn and your bed, outside any MineColonies colony |
  | The Machines That Remain / The Power Question | click | stage `oc_q_power`: any one of Create's water wheel, windmill or steam engine advancements (each needs real torque); IE's windmill, kinetic dynamo or diesel generator; or PneumaticCraft's air or liquid compressor |
  | The Other Science / A Different Kind of Power | click | item `ars_nouveau:source_gem` ("Generate Source"), and stage `oc_q_cast_spell`: actually casting an Ars Nouveau spell |
  | The Second Civilisation / People Return | click | advancement `minecolonies:minecolonies/colony_population_25`. MineColonies has milestones at 10 and 25, none at 20 |
  | The Second Civilisation / Secure The Future | click | items: Security Camera, Keypad Door, Sentry |
  | The Second Civilisation / Knowledge, Power, Shelter | click | no task: it completes once its six dependencies do, which already cover the design doc's "one from each" path |
- **New files:**
  - `kubejs/server_scripts/oc_quest_checks.js` sets the night and power tags. It checks each player every 2 s. A "whole night" means seen at or before 14000 and at or after 22000 (night is 13000–23000), alive, and in the Overworld throughout. Players get a short line after each counted night ("Night 2 of 5…").
  - `kubejs/startup_scripts/oc_quest_spellcast.js` sets `oc_q_cast_spell` from Ars Nouveau's `SpellCastEvent`. `ForgeEvents` only exists in startup scripts.
  - OpenLoader pack `QuestChecks`: the tags `oldcivilisation:abandoned_places` and `creeping_woods` (structures) and `dense_forests` (biomes). Every entry is `required: false`, so removing a mod can't break the tag.
- **Progress kept:** each replaced checkmark's task ID is reused for its first replacement, so players who already clicked one keep it. Added tasks have new IDs in their chapter's range.
- **Check a player's tags:** `/tag <player> list`. Grant one by hand: `/tag <player> add oc_q_power`.
- **On the VM:** uploaded together with the next entry (see its "On the VM").

### Quest book: fixes and additions (2026-11-08) — *client instance + VM*
- **Why:** a review of the quest book against the mods and scripts added since July. Some quests couldn't be finished as written, some text no longer matched the pack, and the newer mods had no quests.
- **Quests that couldn't be finished:**
  - **The Final Question ran backwards.** "The Departure Records" (the four flight recorders) came first, but the recorders are space loot, and the Launch recorder was only in three Ad Astra tables that no structure places (`common_moon`, `common_other`, `rare_moon`; checked by scanning every structure file in the pack). New order: The Skyward Project → Following Their Path → The Departure Records → The Answer. In `oc_loot.js`:
    - the Launch recorder moved to the moon tables that are placed (`ad_astra:chests/dungeon/moon/dungeon_chest`, `village/moon/house`, `village/moon/blacksmith`; 10%);
    - Transit moved to the moon dungeon chest and `ad_astra_more_structures:chests/common_mars`;
    - the Unstable Memory's `rare_moon` slot moved to the moon dungeon chest.
  - **The Skyward Project** is now the chapter's first quest. It also depends on the quests behind each NASA Workbench part (`AdAstraSpaceGate`): Brass and Precision, A Familiar Design, Storage Is Not Enough, and a new Enchanter quest (teleport scroll). It still depends on The Missing Years and Unidentified Contact. Its text names the four parts.
  - **A Familiar Design** asked for a Digital Miner, which `MekanismProgression` makes endgame (atomic alloy, ultimate control circuit, Robit, teleportation cores). It now asks for a Crusher; the Digital Miner moved to Distant Hands.
  - **The four residue quests** (It Wears Faces, The One Below, The Breathless, It Was Not There Before) only paid out on a kill, but these horrors rarely die: CANTBREATHE gives up a chase after 60 s, and the Mimic and the Cave Dweller after 30 s. New `kubejs/server_scripts/oc_encounters.js` gives the residue when the horror leaves without being killed:
    - the encounter starts when one of the four comes within 24 blocks of a survival player;
    - it pays out if the horror then disappears without being killed and the player hasn't died meanwhile;
    - the player gets the residue and a one-line message;
    - a kill still drops the residue as before (`oc_interactions.js`);
    - open encounters are kept in memory only, so a restart forgets them.
  - **The First Survey gave a waystone** as the second quest, which skipped `WaystonesGate` (warp stones need an Ender Eye). The waystone moved to a new Nether quest, "Through the Portal".
- **Text and rewards brought up to date:**
  - **Breathless Residue:** Fog Residue was renamed (display name and tooltip only; the item ID stays `oldcivilisation:fog_residue`). The residue now comes from CANTBREATHE.
  - **The Answer** lists what it waits for. Before, these conditions were invisible:
    - the three evidence kinds (technical, magical, exodus) and 12 kinds of evidence, which are the conditions of the Correction Fragment grant in `oc_interactions.js`;
    - the assembled HS-LGTFO Mission Data.
  - **Blank books replaced:** three rewards promised a fragment and gave a blank Book and Quill. Automation now gives an Incident Report Fragment, The Silent Nights a Familiar Photograph, and Unidentified Contact a Containment Label.
  - **The Unacceptable Solution** has no task of its own now. It completes on any one of three new sub-quests: The Split Atom (fission reactor port), A Star in a Box (fusion controller), or The Reinforced Core (Extreme Reactors).
  - **Unidentified Contact:** holding a Biological Sample **or** an Impossible Bone Fragment (stage `oc_q_contact`), not only the sample.
  - **The Watchers:** "No claimed ground" (there's no claim mod) became "Nowhere near a bed, a colony…".
  - **The Second Civilisation:** People Return and Secure The Future stay hidden until the opening quests of two of the four paths are done.
  - **A Moving World** no longer gives a second backpack (A World Left Behind already gives one).
  - **AE2 Drive:** `ae2:drive` moved from Storage Is Not Enough to Networks.
  - **Residue objectives** now read "Survive an encounter, or end one."
  - **Sanity horror list:** `oc_sanity.js` `HORROR_IDS` and the `OldCivSanity` tag `horror_mobs` gained `cant_breathe:cant_breathe` and lost `man:*` (The Man From The Fog was removed on 2026-11-06).
- **New quests (31):**

  | Chapter | Quest | Checks |
  |---|---|---|
  | The Broken World | Through the Portal | enter the Nether, and structure `#oldcivilisation:nether_strongholds` (fortress, Better Fortresses' fortress, bastion, Incendium's Forbidden Castle and Sanctum). Gives the waystone |
  | The Machines That Remain | The Split Atom, A Star in a Box, The Reinforced Core | fission reactor port; fusion reactor controller; Extreme Reactors reinforced reactor controller |
  | The Machines That Remain | The Yellow Ore | Yellorium ingot, basic reactor controller, basic turbine controller |
  | The Other Science | The Price → What the Blood Bought → The Great Work | Blood Magic: dagger, altar, weak orb; soul snare, Hellfire Forge; master ritual stone, apprentice orb |
  | The Other Science | Circles on the Floor → The Lesser Grail | Mahou Tsukai: mortar and pestle plus advancement `mahoutsukai:root` (first circle); `mahoutsukai:lesser_grail` |
  | Recovered Data | Chemical Memory | Applied Mekanistics: chemical storage cell 1k, chemical P2P tunnel |
  | Field Reports | Below the Bedrock | Deeper and Darker: enter the Otherside; find an Ancient Temple |
  | Field Reports (optional) | Fixation, The Figure, Hunger, Day Three | The Obsessed `first_impressions`; Creeping Woods `demons_of_the_forest` (encounter the Figure); kill a Wendigo; Born in Chaos `fighting_nightmares` (kill the Nightmare Stalker) |
  | Field Reports (invisible until done) | Breakdown | stage `oc_q_breakdown`: sanity reaches Breakdown. Gives 4 Sedative Ampoules |
  | The Second Civilisation | The Centrepiece, The Enchanter's Tower, A Village Again, Take Arms | MineColonies: town hall level 5, enchanter, 50 citizens, barracks |
  | The Final Question (optional, after The Answer) | Follow Them, Launch Again, Stay | reach Glacio; a Tier 4 rocket; 75 citizens |
  | Recovered Evidence (optional) | Deeper Memories, The Map That Lies, Coping | Distorted and Forbidden Memory; Corrupted Map; Sedative Ampoule and Familiar Photograph |
  | Recovered Evidence (invisible until done) | Four Entries, Ten Entries, Sixteen Entries | stages `oc_q_discoveries_4`, `_10`, `_16` |
  | When Dungeons Arise | Find a Jungle Tree House | structure `dungeons_arise:jungle_tree_house` (WDA has no advancement for it) |

- **Script changes:** `oc_quest_checks.js` also turns state that other scripts already keep into tags:
  - `oc_q_evidence_tech`, `_magic` and `_space`, from the `oc_flag_*` path flags;
  - `oc_q_discoveries_4`, `_10`, `_12` and `_16`, from `oc_discoveries`;
  - `oc_q_contact`, when `oc_seen` has a sample or a bone;
  - `oc_q_breakdown`, when `oc_sanity_band` is 3.

  These are checked every 2 s, so existing players catch up the first time they're online.
- **Progress kept:**
  - Task IDs moved with their tasks: the Crusher keeps the Digital Miner's ID, the Drive and the fusion controller keep theirs, and Unidentified Contact keeps its task ID.
  - FTB Quests records completion per quest, so a quest that was already complete stays complete when it gains a task.
- **Checked offline:** every quest parses; all 714 IDs are unique, 16-hex and every dependency resolves; every item, advancement, dimension, entity, reward and icon exists in the pack; every stage is one a script grants; every `#oldcivilisation:` structure tag exists.
- **Tested on the scratch server:**
  - FTB Quests loaded the chapters with no errors and didn't rewrite them;
  - all server scripts loaded, including `oc_encounters.js`, and the sanity horror set has 12 IDs; `kubejs errors startup` found nothing;
  - `locate` found `#oldcivilisation:nether_strongholds` (a Better Fortresses fortress, 440 blocks) and `dungeons_arise:jungle_tree_house` (2,012 blocks).
  - **Not tested:** anything that needs a player, such as the encounter payout, the mirrored tags and the quest screens. Check those in game.
- **Also deleted:** `config/ftbquests/quests/chapters/broken_world.snbt.bak`, which git ignores and which is identical to `broken_world.snbt` in commit `85ada22`.
- **On the VM (2026-11-08):** 21 files uploaded with this entry's changes and the previous entry's:
  - what went up:
    - the nine chapter files;
    - the `QuestChecks` pack (new);
    - `OldCivSanity`'s `horror_mobs.json`;
    - `kubejs/server_scripts/` `oc_quest_checks.js` (new), `oc_encounters.js` (new), `oc_sanity.js` and `oc_loot.js`;
    - `kubejs/startup_scripts/` `oc_items.js` and `oc_quest_spellcast.js` (new).
  - Each upload was read back identical.
  - **Checked first:** every VM file that was replaced matched a committed version, so there were no VM-only edits to lose.
  - **Rollback:** the replaced files were moved to `files/server/deploy-backups/2026-11-08-quests/`, under the same relative paths. To roll back, move them back and delete the new files.
  - **Live since the 01:14 restart:** the log shows *Loaded 9 chapters, 133 quests*, and all KubeJS server and startup scripts loaded with 0 errors. `oc_quest_checks.js` granted its first mirrored tag (`oc_q_evidence_magic`) at 01:24.
  - **Bug found live, fixed:**
    - **Symptom:** `oc_encounters.js` stopped on its first player tick with *Cannot find function getUUID*, so the survive-an-encounter residues never paid out.
    - **Cause:** this KubeJS/Rhino exposes `Entity#getUUID` as `getUuid()` (also `uuid` and `stringUuid`). A probe on the scratch server confirmed it, and also confirmed that the script's other calls (`isRemoved()`, `getRemovalReason()`, which reports e.g. `DISCARDED`, and `getEntities(entity, aabb)`) work as written.
    - **Fix:** uploaded and read back identical. The broken copy is in `deploy-backups/2026-11-08-encounters-fix/`.
    - **Takes effect** at the next restart, or after an op runs `/reload`.
- **Client pack update:** players see the Breathless Residue name and tooltip (`oc_items.js`, `oc_tooltips.js`) only after updating the pack. Until then, the old name shows on their side.

### Ultimine Shapes+ added, FTB Ultimine `max_blocks` 64 → 81 (2026-11-08) — *client instance + VM*
- **Mod:** Ultimine Shapes+ 1.0.1, `ultimine-shapes-plus-forge-1.20.1-1.0.1.jar` (mod id `ultimineshapes`, MIT). It adds mining shapes to FTB Ultimine, defined as JSON files. It requires FTB Ultimine exactly 2001.1.8 (the pack's version), FTB Library 2001.2.1+ and Architectury 9.2.14+, all already in the pack.
- **Shapes:** loaded at startup from `config/ultimineshapes/shapes/*.json` (files up to 2 MiB, sorted by id; duplicates are skipped). On first start it installs three examples once: Flatten 3×3, 5×5 and 9×9 (`"type": "plane"`), marked by `.examples-installed`, so deleted examples stay deleted. The game hasn't been started since the jar was added, so `config/ultimineshapes/` doesn't exist yet in this instance.
- **Both sides:** the server holds each player's selected shape. The client only sends "next/previous shape" (`ModeChangedPacket`), and the server cycles its own shape list and sends the index back (`SendShapePacket`). So the server needs the jar and the same shape files, or the new shapes don't exist there. The mod has no network channel of its own, so a client without it can still join. It is not on the client-only list; the sync dry run against the VM: 308 active − 24 client-only = 284, and the only change is `copy + ultimine-shapes-plus-forge-1.20.1-1.0.1.jar`.
- **Block limit raised to 81:** FTB Ultimine's `max_blocks` was 64 (its default). Flatten 9×9 is 81 blocks, so it stopped at 64 per use. 81 is the smallest value that completes it; it also lets vein mining (ores, trees) take up to 81 blocks. The hunger cost (`exhaustion_per_block` 20) is unchanged.
  - **VM:** `world/serverconfig/ftbultimine-server.snbt`, uploaded and read back identical; the old file is `ftbultimine-server.snbt.bak-20261108-maxblocks`. FTB Ultimine reads it at startup.
  - **New worlds:** `defaultconfigs/ftbultimine/ftbultimine-server.snbt` now sets `max_blocks: 81` (it was empty, so the defaults applied).
  - **Not changed:** the singleplayer worlds in this instance's `saves/` keep 64.
- **Custom shapes later:** a new or edited file in `config/ultimineshapes/shapes/` has to be copied to the VM's `config/ultimineshapes/shapes/` as well, and both sides restarted, so the two lists stay the same.
- **Docs:** `modlist.md`, `modlist.json` and the README block were regenerated. An `ultimineshapes` rule was added to `tools/generate_modlist.py` (Storage, QoL & utility; FTB Ultimine itself lands in Quests, loot, data & guides through the `ftb` rule). `docs/SERVER_MODS.md` was regenerated from the VM.
- **On the VM:** the jar was uploaded with the sync (284 jars, parity verified). The jar, the three example shapes (created on the server's first start) and the new block limit all take effect at the next restart.

### Face-oriented Ultimine shapes: 2x2, 5x5, 5x3 (2026-11-08) — *client instance + VM*
- **What:** a new local mod, `spooky_ultimine_shapes-1.0.0.jar`. Source, build and README are in `tools/spooky_ultimine_shapes/`; the jar is in `mods/` and `local/`. It adds **Square (2x2)**, **Square (5x5)** and **Rectangle (5x3)** (5 wide, 3 tall). Each is one layer laid flat on the face of the block you hit, so it turns with you.
- **The 3x3 already existed:** FTB Ultimine's own **Small Square (3x3)** is exactly 9 blocks, one layer, facing the side you hit. **Large Tunnel (3x3)** is the shape that keeps going deeper (up to `max_blocks`).
- **Why a mod, not Ultimine Shapes+ JSON:** Ultimine Shapes+ offsets are fixed to world axes. Its `wall` type is always along X or Z and never turns to face the side you hit, so it can't make shapes that face you.
- **Layout:**
  - **Odd sizes** are centred on the hit block.
  - **Even sizes** put the extra block to your right, forward on floors and ceilings, and towards eye level on walls. A 2x2 aimed at either the head-height or the foot-height block clears the same 2-tall walkway.
  - **Order:** nearest blocks are taken first.
  - **Matching:** the same rules as FTB's shaped modes (`merge_tags_shaped`, default any block). Air is skipped.
  - **Block limit:** fits inside `max_blocks` 81.
- **Shape order:** FTB Ultimine links client and server shape lists by index. This mod registers in the parallel part of common setup, after FTB's constructor-registered built-ins (0–5) and before Ultimine Shapes+'s `enqueueWork` (Flatten, now 9–11). That gives the same order on every start. A scan of every jar in `mods/` found no other mod that registers shapes. The log line `[spooky_ultimine_shapes] registered 3 shapes at indices 6..8` confirms the order.
- **Both sides:** a client without the jar can still join, because the mod has no network channel. That client would show the wrong names from index 6 onward, including the Flatten shapes. So the server and the players' pack should update together. Sync dry run against the VM: 309 active − 24 client-only = 285, and the only change is `copy + spooky_ultimine_shapes-1.0.0.jar`.
- **On the VM (2026-11-08, at the user's request):** uploaded with `scripts/sync_server_mods.py --yes`; the script verified parity (285 jars). Takes effect at the next restart. `docs/SERVER_MODS.md` was regenerated from the VM.
- **Disabled again the same day, at the user's request, until a client update ships.** The server must not run it while players' packs lack it. The VM was never restarted with it loaded.
  - **Client:** `mods/spooky_ultimine_shapes-1.0.0.jar.disabled` (a local build, so the CurseForge app won't re-enable it). The copy in `local/` is unchanged.
  - **VM:** the sync moved it to `mods/spooky_ultimine_shapes-1.0.0.jar.disabled` and verified parity (284 jars).
  - **Docs:** `docs/SERVER_MODS.md`, `modlist.md`, `modlist.json` and the README block were regenerated.
- **Re-enabled on both sides shortly after, at the user's request:**
  - **Client:** the jar is back to `.jar` in `mods/`.
  - **VM:** the `.jar.disabled` file was renamed back over FTP; it's the same 6,183-byte file, not a new upload. The sync dry run reports *nothing to do* (285 jars).
  - **Docs:** `docs/SERVER_MODS.md`, `modlist.md`, `modlist.json` and the README block were regenerated.
  - **Takes effect** at the next VM restart. Players need the pack update with the jar, or their shape names will be wrong (see *Both sides* above).
- **Tested:** the shape layout offline: 14 cases (every wall direction, floor, ceiling, and head- and foot-height aims), all pass. Not yet tested in game.
- **Docs:** `modlist.md`, `modlist.json` and the README block were regenerated. `spooky_ultimine_shapes` was added to the Local Fixes rule in `tools/generate_modlist.py`.
- **Revert:** delete the jar from `mods/` on both sides.

### JEI colour search on, with a palette for building blocks (2026-11-08) — *client only*
- **Why:** finding blocks that suit a build's colour scheme. With 30 decoration mods, searching by name doesn't help.
- **What JEI does** (read from the 15.56.0.205 jar):
  - **Where the colours come from:** the particle texture of the block's default state (or the item model), multiplied by the block's tint. JEI takes the 2 dominant colours (`ColorThief`, a median cut).
  - **How a colour gets its name:** JEI picks the nearest entry in `config/jei/jei-colors.ini`. The distance is `ColorUtil.slowPerceptualColorDistanceSquared`: the "redmean" RGB distance plus a penalty when the two colours differ in saturation, so greys keep to greys.
  - **Duplicate names are allowed:** the palette is a plain list, so one name can have several anchors.
  - **Matching:** colour names are matched as substrings. `^red` therefore finds DarkRed, BrickRed, RedBrown and the rest.
  - **Tooltip:** while colour search is on, the JEI panel's tooltip shows each item's colour names ("Colors: DarkGrey, Grey").
- **Changes:**
  - `config/jei/jei-client.ini`: `colorSearchMode` **DISABLED → REQUIRE_PREFIX**. Colours are searched only with `^`, so a plain search for "red" still searches names only.
  - `config/jei/jei-colors.ini`: JEI's 35 default colours were replaced with 50 anchors under 46 names. JEI's own list is tuned for bright item icons, and building blocks are mostly muted. Against the pack's blocks, the default list put:
    - oak and jungle planks under "BrickRed";
    - prismarine under "Gray, Silver" and purpur under "Gray, Pink";
    - moss under "Brown" and nether bricks under "NetherBrick", which `^red` doesn't find.
- **The new palette:**
  - **Families:** Black, Grey, White, Brown, Beige, Tan, Red, Orange, Yellow, Green, Teal, Cyan, Blue, Purple, Magenta and Pink.
  - **Shades:** `Dark…`, `Light…`, `Bright…` and `Pale…`.
  - **In-between colours carry both family names:** BlueGrey, GreenGrey, BrownGrey, RedBrown, OliveGreen and RustOrange, so they turn up under either family.
  - **Spelling:** British "Grey", like the rest of the pack. `^gray` finds nothing.
  - **Extra anchors:** Grey has 3, DarkGrey and Brown 2 each. Without them, plain dark greys such as deepslate tiles came out as DarkGreen or BrownGrey.
- **How it was tested:** offline. JEI's colour step (particle texture → 2-colour median cut → nearest name by JEI's exact distance) was rebuilt in Python and run over the 25,526 block items whose models resolve to a texture (Minecraft plus every enabled jar).
  - **Examples with the new palette:**

    | Block | Colours |
    |---|---|
    | Oak planks | LightBrown, Tan |
    | Spruce planks | Brown |
    | Dark oak planks | DarkBrown |
    | Mangrove planks | RedBrown, Brown |
    | Deepslate | DarkGrey, Grey |
    | Blackstone | Black, DarkGrey |
    | Nether bricks | Black, DarkRed |
    | Prismarine | Teal, LightTeal |
    | Moss block | OliveGreen |
    | Mud bricks | BrownGrey, LightBrown |
    | Calcite | PaleGrey, White |

  - **Family sizes:** `^grey` matches about 10,300 blocks, `^brown` 6,900, `^red` 3,100, `^green` 2,600, `^teal` 1,950 and `^blue` 1,300.
  - **Not tested in game.** Blocks with custom renderers (for example FramedBlocks frames, or blocks drawn by a block entity) are classed by their particle texture, whatever they look like placed.
- **Load cost:** JEI now reads every ingredient's texture when it builds its search index on world join. Before the change, "Building ingredient filter took 1.642 seconds" (log, 2026-11-08 01:44). Compare that line after the next launch.
- **Using it:** `^darkred` finds one shade. `^grey brick` finds grey blocks with "brick" in the name. `^teal|^cyan` means either. `^grey -^light` leaves out the light greys.
- **Scope:** both files are client config, with nothing on the server. Players get them with the next pack update. JEI rewrites its config files on launch but keeps these values.
- **Revert:** set `colorSearchMode = DISABLED`. To get JEI's own palette back, delete the `searchColors` line from `jei-colors.ini` and JEI writes its default again.

### The Obsessed and CANTBREATHE: sanity-bound, never in a base, provoked by a look — `spooky_tick_fixes` 1.5.0 (2026-11-08) — *built; not yet installed or deployed*
- **Why:** the user asked for The Obsessed to be sanity-bound ("critical" sanity enables spawning), for its first contact to need you to look at it rather than it seeing you, and for its stalks never to spawn in a base, keeping the window lurker and the sleep-paralysis hallucination. CANTBREATHE gets the same three rules.
- **How each mod schedules things (bytecode reads, 2026-11-08):**
  - **The Obsessed** has no biome spawns.
    - A per-player targeter timer (3 min with our config) puts a "targeter" 40–80 blocks away, at light below 9, behind you.
    - It locked on to the nearest player it had line of sight to (not sneaking), and that player became the target.
    - From then on a stalk timer spawns it 37–70 blocks away, day or night, anywhere in the Overworld, while you face away.
    - Its gap is `rand − min(K, Stalk Frequency)` by stage. Our 2000 doesn't slow the intense/extreme stages, because K is capped at 1800.
    - The countdown runs at quarter speed except during the world's first night: the mod checks absolute day time.
  - **CANTBREATHE** (Cave Dweller code):
    - It keeps one Overworld timer, and keeps the same victim across cycles.
    - A victim stops qualifying only when they die, log off, leave the Overworld or switch to creative/spectator (our config passes every height, light and biome), or when a spawn attempt fails. The mod then picks a random qualifying player.
- **Change** (`[sanityHorror]` in `config/spooky_tick_fixes-common.toml`; full table in `tools/spooky_tick_fixes/README.md`, 1.5.0):
  - **Critical sanity** = the band `oc_sanity.js` stores at or past `criticalBand` (3, Breakdown: below 15, left at 20). It's read from KubeJS's own persistent data (`KubeJSPersistentData`), not Forge's.
  - **Base** = within 48 blocks (±32 y) of any online player's respawn point, or inside a MineColonies colony's claim (±32 y of its centre).
  - **The Obsessed:**
    - A targeter only appears for a critical player, outside a base. Its timer is held just short of zero while you aren't critical, so it comes 10–60 s into a breakdown if it was due.
    - It locks on only when a critical player **looks at it**: the Enderman stare test plus a clear line of sight.
    - Stalks only happen while the target is critical, retried every ~20 s otherwise. A stalk spot inside a base is moved out of it.
    - The behind-you/spider hallucinations and the block-break surprise don't happen inside a base. The surprise also needs critical sanity.
    - **Unchanged:** the lurker at your windows and sleep paralysis (both need an existing obsession).
  - **CANTBREATHE:**
    - Its timer still runs on everyone's time (the hourly cap stays).
    - A spawn only goes ahead for a critical victim, and victims are picked among critical players first.
    - Its cave noise only plays for a critical victim, and only critical players get it.
    - Spawn spots inside a base are rejected.
    - The "any player within 15 blocks" chase is off. Looking at it or hitting it still provokes it.
  - With the gate, "stops qualifying" effectively also includes leaving Breakdown: the next spawn attempt fails for a sane victim, and the mod re-picks.
- **Tested:**
  - Compiles against the installed jars (javac, 67 sources).
  - Every injection target (owner, name, descriptor and call-site count) was checked against the class files: 9/9 match.
  - **Not boot-tested:** the user's game was running, with 2.7 GB of RAM free, too little for a headless server next to it. Do that before deploying (any mixin error stops startup with the patch named).
  - Not tested in game: everything that needs a player.
- **Not installed yet:** the jar is at `tools/spooky_tick_fixes/build/spooky_tick_fixes-1.5.0.jar`. Installing it means replacing 1.4.0 in `mods/` and `local/` (`build.py --install`), which shouldn't happen while the game holds the old jar open.
  - The `[sanityHorror]` section wasn't added to the config file by hand: the running 1.4.0 could strip unknown keys on a reload. Forge adds it with the defaults on the first 1.5.0 start.
- **To deploy:** `python tools/spooky_tick_fixes/build.py --install`, then a headless boot check, then `python scripts/sync_server_mods.py` and a VM restart. The server can run it alone; clients only gain singleplayer behaviour.
- **Check in game:**
  - `/sanity <player>` and `/tickfixes horror` show each player's band, base status and Obsessed state.
  - At Breakdown in the dark, a targeter should appear within a minute or so and lock on only once you look at it. Stalks should then stop when you recover, and never appear inside 48 blocks of a bed.
  - **Gap:** The Obsessed's intruder form can still walk into a base after spawning outside one.
- **Revert:** the six switches in `[sanityHorror]`, or the 1.4.0 jar.

## Appendix A — OpenLoader packs created
`config/openloader/data/`: `MekanismProgression`, `AE2Progression`, `AdAstraSpaceGate`, `OccultismExpensiveRituals`, `WaystonesGate`, `SecurityCraftBalance`, `WeepingAngelsBuff`, `MineColoniesSlowResearch`, `DayLength30`, `HorrorSpawnRarity` (extended 2026-11-01: John Mod Reborn + Creeping Woods), `ExtremeReactorsProgression`, `BornInChaosNoInfectedOre` (2026-11-02), `HorrorQuietAdvancements` (2026-11-06), `IncendiumClockTrim` (2026-11-06; pins Incendium 5.3.5's `clocks/main`), `WendigoTrophies` (2026-11-07; Wendigo loot), `QuestChecks` (2026-11-08; structure and biome tags for the quest book), `PerfFixes`, `TFCLightItems`, `AntiLagEntities`, `TFCFoodHunger`, `OldCivSanity`, `TFCVanillaCraftingTable`. (~~`DayLength30`~~ — retired 2026-11-01: its tick function never compiled under TFC's `/time` override and its failure dropped the whole `minecraft:tick` tag; day length is now Simple Day Length Extender's config.) (~~`TFCModdedFood`~~ — retired 2026-07-12: its `#forge:foods` tag reference never loaded; superseded by `kubejs/server_scripts/tfc_vanilla_food.js`, folder parked in `config/openloader/data-disabled/`.)
`config/openloader/resources/`: `QuietAmbience`, `TFCNoSizeTooltip`, `TFCHideInventoryTabs` (2026-11-01).
(All `TFC*` packs above, data and resources, were deleted from this instance and the repo on 2026-11-07, with TFC disabled on both sides. The VM still has the three data packs `TFCFoodHunger`, `TFCLightItems` and `TFCVanillaCraftingTable`. Without TFC they do nothing: the first two hold only `tfc:` data, and the third re-states vanilla's own crafting-table recipe.)

## Appendix B — Known crash (NOT caused by these config changes)
Startup crash traced to a **Mixin injection failure**: TerraFirmaCraft's `BiomeMixin` (`shouldFreezeWithClimate` redirect) fails because **Serene Seasons** (and Cold Sweat's freezing mixin) rewrite the same vanilla biome freeze/precipitation methods. This is a **mod-vs-mod incompatibility** at class-load, before any config/datapack is read — data edits cannot cause or fix it. **Recommended fix:** disable one of the conflicting climate mods (most likely **TerraFirmaCraft** vs **Serene Seasons**). **Update (July):** both **Serene Seasons** and **Cold Sweat** were disabled/removed, so this specific freeze-mixin conflict was resolved — TerraFirmaCraft was then the only climate mixin on those methods. **Update 2026-11-01:** reversed — TerraFirmaCraft and its dependants are now disabled and Serene Seasons is back, so TFC's `BiomeMixin` no longer loads and the conflict cannot occur. Do not re-enable TFC alongside Serene Seasons.

## Appendix C — Reverting
- Config-file edits: `*.bak` backups sit next to the originals (e.g. `alexsmobs.toml.bak`, `create-server.toml.bak`, `coldsweat/world.toml.bak`, `serversidehorror.json.bak`, `man_config.toml.bak`, `ars_nouveau.bak/`, `alexscaves_biome_generation.bak/`, `extremereactors/common.toml.bak`, `bloodmagic-common.toml.bak`).
- Datapacks: delete the pack folder under `config/openloader/data/` (or `/resources/`).
- Per-world defaults: `defaultconfigs/*.toml`; to re-apply to an existing world, delete that world's `serverconfig/<mod>-server.toml` to regenerate.
