# Config Changes

The record of configuration, datapack, script and mod-set changes in the pack: what changed and why. Theme throughout: a **horror / survival-progression** pack where technology is earned, exploration matters, and the world stays dangerous.

The file has two parts:

- **Baseline (2026-07):** a per-mod record of the July 2026 rebalance, as first applied. Each section that a later change replaced carries a *Superseded* line.
- **2026-11:** a dated log from 2026-11 on, one entry per change, oldest first.

Appendices list the OpenLoader packs, a known crash and how to revert.

## How changes are applied (scope)

| Mechanism | Location | Scope |
|---|---|---|
| Common/client configs | `config/*.toml`, `config/<mod>/…` | Global — shared by all worlds |
| Per-world server configs | `saves/<world>/serverconfig/*-server.toml` | Per-world; made global via `defaultconfigs/` (applies to every **new** world) |
| OpenLoader datapacks | `config/openloader/data/<Pack>/` | Global — injected into every world |
| OpenLoader resources | `config/openloader/resources/<Pack>/` | Global resource pack |

Every per-world (`-server.toml`) change in the Baseline was also copied to `defaultconfigs/`, so it seeds every new world. Backups of edited files (`*.bak`, beside each original) exist only in the maintainer's client instance; `*.bak` is git-ignored.

---

## Baseline (2026-07)

The per-mod rebalance as first applied in July 2026, kept as a record, with a Superseded line on each section that a later change replaced.

### The Mimic (`themimic_er-server.toml`) — *per-world → defaultconfigs*
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

### Cave Dweller (`cave_dweller-server.toml`) — *per-world → defaultconfigs*
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

### Cold Sweat (`config/coldsweat/…`)
> Superseded 2026-07: Cold Sweat and its `create_cold_sweat` compat addon were removed from the pack (jars disabled). `config/coldsweat/` was deleted from the repo on 2026-11-07 ("2026-11-07 — Repository cleanup: personal files purged from history, orphaned configs removed").

With Cold Sweat removed, cold stopped being a survival threat, and the README's climate framing was updated to match. In July, TerraFirmaCraft provided a world climate and seasons system, without Cold Sweat-style player hypothermia damage. TerraFirmaCraft was disabled on 2026-11-01; Serene Seasons, re-added that day, is the climate and seasons layer ("2026-11-01 — Lore and config consistency pass").

**Goal:** cold is the main threat (nights, rain, mountains); heat only in deserts/lava; **everything in Celsius**.

- **Units:** every stored value converted from °F to **°C** (behavior-preserving — absolute temps via `(F−32)×5⁄9`, offsets/deltas via `×5⁄9`). Display was already Celsius.
- **Heat only where intended:** non-arid biome **noon** temps capped at **30 °C** (below the ~35 °C heatstroke threshold). Deserts, badlands, and Terralith arid/volcanic biomes kept hot.
- **Cold nights:** non-hot biome **midnight** temps capped at **6 °C** (tropical biomes exempt).
- **Cold mountains:** peak/high-altitude biomes forced cold (high ≤ 12 °C, low ≤ 2 °C).
- **Rain bites:** `Max Rain Soak` 0.2 → **0.35**, `Rain Soak Speed` 0.0125 → **0.02**, `Default Water Temperature` −10 °F → **−8 °C**, `Shade Temperature Offset` −9 °F → **−6 °C**.
- Lava/magma still emit heat (unchanged) → overheating underground near lava.

**Why:** climate alignment — survival pressure from cold at night, in rain and on peaks, with heat reserved for deserts and lava.

---

### Waystones — *common config + OpenLoader datapack*
> Partly superseded 2026-11-08 by "2026-11-08 — Waystones: warping costs no XP" (XP cost only; the Eye of Ender gate stands).

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

**Why:** every teleport costs XP (min 1 level, scaling with distance), and the shared Warp Stone core (used by all waystones/sharestones) requires a post-nether item, so waystones arrive mid-game. "Require activation" is already Waystones' built-in default (must visit a stone to use it) — no change possible or needed.

---

### Lootr — *no change*
Verified: chests are one-time per player (`refresh_all=false`, no refresh lists, `decay_all=false`). Loot is already maximally scarce; `max_age` is only conversion-queue cleanup, not a loot cooldown. Nothing edited.

---

### Sound Physics Remastered (`config/sound_physics_remastered/soundphysics.properties` + `options.txt`) — *global*
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

### AmbientSounds (`QuietAmbience` — OpenLoader resource pack) — *global*
**Goal:** wind (and ambience) much less frequent; silence matters.

- Overrode wind sound definitions to add long **pauses**: `light-wind` length 20–60 s, **pause 150–350 s** (was no pause = constant); `heavy-wind`, `wind-in-leaves`, `wind-mesa` similarly given 2–5 min silent gaps.

**Why:** the config's numbers are only volume multipliers; wind felt constant because its definition had **no pause**. Adding pauses creates real silence between gusts. The pack lives in `config/openloader/resources/`, so it is global; `options.txt` carries no resource-pack entry for it.

---

### Weeping Angels — *spawn config + common config + OpenLoader datapack*
**Goal:** rare, extremely dangerous, controlled teleporting.

**`config/weeping_angels_spawns.json`:** disabled in **186 of 202 biomes**; enabled (rare, solitary — weight 2, `min/max` 1; Deep Dark weight 3) only in spooky/remote biomes (Deep Dark, Dripstone, Dark Forest, Soul Sand Valley, eerie Terralith caves, Moonlight Valley/Grove, all End biomes, Alex's Caves Forlorn Hollows & Abyssal Chasm).

**`config/weeping_angels-common.toml`:**
| Setting | Before | After |
|---|---|---|
| `teleport_chance` | 50 | 10 |
| `interdimensional_teleporting` | true | false |
| `teleport_range` | 400 | 150 |

**`WeepingAngelsBuff` datapack** (per-tick attribute buff, applied once per angel): max health **150**, attack damage **12**, armor **10**, knockback resistance **1.0**. (Corrected 2026-11-01: its `wa_buff:tick` function never ran until `DayLength30` was retired, because that pack's broken function dropped the whole `minecraft:tick` tag; see "2026-11-01 — Mod-update review: about 80 mods updated, 13 jars removed".)

**Why:** solitary rare spawns in fitting locations = "something impossible"; teleporting stays possible but rare, never cross-dimension, and only a moderate distance; the stat buff makes a single angel a genuine fight.

---

### Ars Nouveau (`config/ars_nouveau/glyph_*.toml`) — *global*
**Goal:** reduce spell damage slightly, encourage utility, discourage "machine-gun" magic.

- **Damage glyphs −~15–20%** (base + per-Amplify): Harm 5→4, Crush 3→2.5, Lightning 5→4, Explosion 6→5, Flare 7→6, Cold Snap 6→5, Fangs 6→5, Wind Shear 5→4.
- **Anti-spam:** `Cut` mana cost **0 → 10** (was free, infinitely spammable).
- **Cheaper utility:** Blink 50→30, Leap 25→15, Launch 30→20, Glide 100→70, Slowfall 30→20, Intangible 30→20, Light 25→15, Heal 50→35 (heal amount 3→4), Conjure Water 80→50, Invisibility 30→20, Dispel 30→20.

**Why:** magic becomes a tactical toolkit (teleport/light/support cheap) rather than a rapid-fire damage rifle.

---

### Occultism (`OccultismExpensiveRituals` datapack) — *global*
**Goal:** summoning demons should never be trivial.

- 19 demon-summon rituals had their ingredients swapped for **tier-scaled valuables** (ingredient **count kept identical** so existing pentacle sacrificial-bowl layouts still work):
  - **Foliot:** gold ingot, glowstone, lapis block, redstone block
  - **Djinni:** diamond, gold block, blaze powder, ender pearl
  - **Afrit / Wild Afrit / Demonic spouse:** diamond block, blaze rod, ghast tear, ender eye
  - **Marid / Wild Hunt:** netherite ingot, diamond block, ghast tear, wither skeleton skull

**Why:** the client toml is cosmetic-only; ritual cost lives in recipes. Scaling by demon tier makes each summon a real investment from "meaningful" (Foliot) to "serious commitment" (Marid).

---

### MineColonies — *server config + research datapack*
> Partly superseded 2026-11-04 by "2026-11-04 — MineColonies: server config replaced" (`raidDifficulty` 5, the mod default, in place of 8; the 1.1.1231-only keys, including `barbarianhordedifficulty`, `dobarbariansbreakthroughwalls` and `pathfindingmaxthreadcount`, are gone; other values changed, listed there). MineColonies 1.1.1301 had already renamed the raid keys and dropped `pathfindingmaxthreadcount`: see "2026-11-01 — Mod-update review: about 80 mods updated, 13 jars removed".

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
*(Not configurable in MineColonies 1.1.1231: happiness weightings and per-building material costs — those live in internal logic / Structurize blueprints.)*

---

### Ad Astra (`AdAstraSpaceGate` datapack) — *global*
**Goal:** space is very late-game — conquer Earth first. Keep oxygen/survival.

- **NASA Workbench** (the chokepoint for all rockets/suits/rovers) requires one signature item from each core system:
  - Create → **Precision Mechanism**
  - Mekanism → **Atomic Alloy**
  - AE2 → **ME Controller**
  - MineColonies → **Colony Teleport Scroll** (needs an Enchanter's Tower)

**Why:** you can't begin the space program until Create + Mekanism + AE2 + an established colony all exist. **Oxygen, temperature, gravity, air-vortex survival systems left ON** (`config/ad_astra.jsonc` unchanged).

---

### Mekanism (`MekanismProgression` datapack) — *global*
**Goal:** don't trivialize mining/resources/power; delay high ore multiplication; late-game jetpacks; nuclear as a milestone. **2× (Enrichment Chamber) kept early.**

| Item | Requires |
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

### SecurityCraft (`SecurityCraftBalance` datapack) — *global*
**Goal:** security useful but not silly — a bunker, not "AI kills everything."

- **Sentry** recipe also requires a **diamond block**.

**Why:** no per-turret count limit exists in config, so making sentries a premium build means you place a few at chokepoints instead of carpeting the map. Reinforced blocks, keypads, and scanners are untouched.

---

### Create (`create-server.toml`) — *per-world → defaultconfigs*
**Goal:** factories require planning; keep trains cheap.

- **Stress impact ×1.25** on all 25 non-zero machine values (`[kinetics.stressValues.v2.impact]`).
- **Trains: unchanged** (kept cheap so the world can become connected by rail).

**Why:** machines cost more Stress Units, so generators/gearing need genuine planning. *(Create farms: no clean config lever — the stress bump makes large auto-farms costlier. Optional, not applied: targeted farm-machine gating.)*

---

### Applied Energistics 2 (`AE2Progression` datapack) — *global*
**Goal:** large storage expensive; autocrafting not rushed. Early game = drawers/backpacks/chests.

| Item | Requires |
|---|---|
| ME Drive | 2× engineering processor + energy cell |
| 256k Storage Component (largest cell) | + engineering processor |
| Crafting Unit (base of every Crafting CPU) | 2× engineering processor |

**Why:** bulk storage and autocrafting become a late-game "rebuilt civilisation" payoff gated behind processor automation.

---

### Alex's Caves (`alexscaves-general.toml`, `alexscaves_biome_generation/*.json`) — *global*
**Goal:** cave biomes rare and far apart; unique materials require exploration.

| Setting | Before | After |
|---|---|---|
| `cave_biome_mean_separation` | 900 | 1800 (~4× rarer by area) |
| `distance_from_spawn` (per biome, ×1.5) | 400 / 450 / 500 / 650 | 600 / 675 / 750 / 975 |

**Why:** finding a cave biome becomes "something impossible," not "another cave biome"; the powerful rewards inside them require real travel. *(No global loot-rarity slider exists; rarity-by-distance handles the exploration goal. Optional, not applied: per-item loot nerfs.)*

---

### Alex's Mobs (`alexsmobs.toml`) — *global*
**Goal:** quieter surface — a still forest is scary.

- **34 passive surface fauna** spawn weights reduced **~30%** (herbivores, monkeys, birds, small critters — e.g. gazelle 40→28, elephant 30→21, capuchin 28→20).
- **Left at full strength:** predators (tiger, crocodile, komodo, dropbear, froststalker, rhino, anaconda…), hostile/horror mobs, aquatic, and nether/end/cave spawns.

**Why:** thins out ambient forest clutter for atmosphere while keeping every actual threat intact.

---

### Serverside Horror (`config/serversidehorror.json`) — *global*
**Goal (the "From The Fog" distance-horror layer):** more distance events & rare sightings; **do not** increase attacks.

- Sighting/ambient event chances made ~1.5× more frequent (chance ÷ ~1.5): `herobrine_starer`, `starer`, `fake_steps`, `fake_mining`, `scary_sound`, `random_signs`, `fake_joiner`, `random_fake_joiner`, `heads_from_list`, `random_heads`.
- **Unchanged:** jumpscare, torch-breaking, traps, arson, lightning, spawn/join events (the aggressive/attack side).

The tuning pass ("Tuning pass — trim the meme/aggressive events, keep the subtle ones") replaced part of this step: it disabled eight events, among them `herobrine_starer`, `random_signs`, `fake_joiner`, `random_fake_joiner`, `jumpscare` and `random_lightning`, and made `starer`, `fake_mining`, `break_torches` and `replace_torches` much rarer. `fake_steps`, `scary_sound`, `heads_from_list` and `random_heads` keep this step's values.

**Why:** provides a tunable distance-sighting horror layer alongside the entity-based stalkers. More staring/footsteps/sounds without ramping up direct attacks.

From The Fog (`watching`) was not hardcoded: it was configured through **in-game datapack functions/commands** (`data/watching/functions/config/…`), not a `.toml`, and had an `autoConfig` that ramped intensity up over in-game days.

#### Tuning pass — trim the meme/aggressive events, keep the subtle ones
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
| `break_torches_chance` | 720000 → **2000000** | Torch-breaking *pretty rare* |
| `replace_torches_chance` | 720000 → **2000000** | Kept in step with break_torches (torch-tampering as a whole is pretty rare) |
| `fake_mining_chance` | 325000 → **2000000** | Distant fake-mining noises *pretty rare* |
| `fake_steps_chance` | 325000 → **325000 (kept)** | Footsteps stay rare **but not too rare** — the most present ambient cue |
| `starer_chance` | 273000 → **8000000** | The (non-Herobrine) starer made *incredibly rare* — a once-in-a-playthrough dread moment |

Left enabled at prior values: `scary_sound`, `heads_from_list`, `random_heads` (subtle, on-theme). `burn_down_house` and `joining_on_bedrock` were already off.

#### Mod swap — From The Fog removed; Server-Side Horror is the sole events layer
*From The Fog* (`watching`) was removed from the pack; Server-Side Horror provides the entire "something is interfering" layer. SSH's `break_torches`, `replace_torches` and `fake_mining` stay enabled: with From The Fog gone, nothing duplicates them.

**Final SSH enabled set:** `break_torches` + `replace_torches` + `fake_mining` (all *pretty rare*, `chance` 2000000), `fake_steps` (rare-but-present, 325000), `starer` (*incredibly rare*, 8000000), `scary_sound`, `heads_from_list`, `random_heads` (+ `traps`/`setting_up_new_traps`/`old_villages`, untouched).

**Kept disabled** (Design: off-theme): `herobrine_starer`, `jumpscare`, `fake_joiner`, `random_fake_joiner`, `long_night`, `random_lightning`, `random_signs`, `joining_in_dungeon` (+ `burn_down_house`, `joining_on_bedrock`, `removing_leaves`, already off).

**Removal note:** From The Fog is not tracked in git (`mods/` is excluded), so removing the jar is the whole job. It stored state only in world scoreboards (`ftf.*` objectives) and used vanilla blocks, so leftover data is harmless — no missing-block or chunk damage on load. Any spawned Herobrine entity is dropped as an unknown entity when the world loads without the mod.

---

### The Man From The Fog (`config/man_config.toml`) — *global*
> Superseded 2026-11-06 by "2026-11-06 — The Man From The Fog: disabled" and "2026-11-06 — The Man From The Fog jar disabled; second client instance deleted" (spawning switched off, then the jar disabled on the client instance and the VM). `config/man_config.toml` was deleted from the repo on 2026-11-07.

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

"Feeling Watched" advancement silenced on 2026-11-06: see "2026-11-06 — The Man From The Fog: Feeling Watched advancement silenced".

#### What the mod does (read from the 1.4 jar, 2026-11-06)
- **Spawn timer:** one world-wide timer (`timer_global`) that every player advances by one per tick **while within 2 blocks of the surface heightmap**; underground players don't count. When it passes a random target between `min_spawn_rate` and `max_spawn_rate`, the Man is summoned in an Overworld biome, day or night (`spawn_at_day = true`), and moved 35–99 blocks from the player with `spreadplayers`. Because every surface player advances the same timer, three players outside bring him about three times as often. Nothing spawns if one is already within 200 blocks.
- **On spawn:** a random spawn sound, and the player is granted `man:feelingwatched` ("Feeling Watched", description "Man spawning first time"). The shipped advancement is its own tab with `show_toast` and `announce_to_chat` true, so it told everyone he had arrived.
- **Stalk, then chase:** a player looking at him (an unobstructed ray within 300 blocks) turns him into the aggressive form, which targets that player and gives Darkness for `darkness_effect_duration`. The chase lasts `min/max_chase_duration`; he climbs, swims fast, breaks blocks up to `block_hardness` (`block_hardness_search` while searching), calls lightning (`lightnings`), and vanishes once the player gets `vanish_distance` away.
- **Death:** a full-screen jumpscare (`jumpscare`). Killing him drops the Tear of the Man and grants `man:kill` (+100 XP, announced). Dying to him with a book and the Tear nearby yields the Fog Speed / Fog Protection enchantments.
- **Other:** players can't sleep with him within 200 blocks ("You can't sleep..." plus 0.1 damage); `man:mftfhang`, a hanging variant at weight 10 in every biome, spawns under tree canopy at night and turns to face players within 100 blocks; a cultist `man:house` structure (spacing 60, any non-water biome) with a Cultist villager profession; once per world the first player to join gets a Ko-fi chat advert and Cassette 1 (not configurable). Commands live under `/man` (`spawn`, `timer`, `reset_timer`, `jumpscare_screen`, `debug`).

---

### Day Length (`DayLength30` datapack) — *global*
> Superseded 2026-11-01 by "2026-11-01 — Mod-update review: about 80 mods updated, 13 jars removed": the tick function never compiled (TerraFirmaCraft replaced `/time add` with its own syntax), so the pack never worked. Retired to `config/openloader/data-disabled/DayLength30/`; day length is Simple Day Length Extender's config (30 min).

**Goal:** ~30-minute days (target range 25–35 min).

- **Intended mechanism:** disables vanilla auto time-advance (`doDaylightCycle false` on world load) and re-advances world time at **2 game-ticks per 3 real ticks** via a tick function → 24000-tick day over 36000 real ticks = **30 min**.

**Why:** longer days/nights (nights longer in real time suits the horror theme). *Retune:* change the tick ratio (25 min = 4/5, 35 min = 4/7). *Revert note:* if the pack is removed, run `/gamerule doDaylightCycle true` or time will freeze.

---

### New-mods pass (added mod batch)

A batch of new mods was added and reviewed. Most are **QoL / libraries / client / Create-addon / structures / decoration** that need no balance alignment. The ones touched:

#### The Obsessed (`config/obsessed_configurations.toml`) — *global*
> Note 2026-11-08: the stalk gap is `rand − min(K, Stalk Frequency)` with K capped at 1800, so 2000 does not slow the intense and extreme stages. `spooky_tick_fixes` 1.5.0 `[sanityHorror]` (built, not installed) binds The Obsessed to Breakdown sanity: see "2026-11-08 — spooky_tick_fixes 1.5.0: The Obsessed and CANTBREATHE sanity-bound".

A targeted stalker horror mod. Aligned to the pack's "rare psychological horror" philosophy:
| Setting | Before | After | Why |
|---|---|---|---|
| `Stalk Frequency` | 4000 | **2000** | Rarer stalking |
| `Natural Targeter Spawnrate` | 2400 | **1200** | Rarer targeting |
| `Guaranteed Initial Targeting Time` | 3600 (1 hr forced) | **9999999** (effectively off) | No scripted early targeting — encounters stay organic/rare |

Left `Allow Multiple Targets = false` (one obsession at a time). Arachnophobia/hallucination toggles left default (player accessibility choice).

#### Door Knocker (`config/doorknocker-common.toml`) — *global*
Ambient "something's at the door" event. Made **rarer** so it stays unsettling rather than background noise (consistent with "rare > frequent"):
| Setting | Before | After |
|---|---|---|
| `dayMinTicks` / `dayMaxTicks` | 24000 / 48000 | **48000 / 96000** (~40–80 min) |
| `nightMinTicks` / `nightMaxTicks` | 12000 / 24000 | **24000 / 48000** (~20–40 min) |

`openChance` (0.1) and line-of-sight (false = knocks through walls, creepier) left as-is.

#### Reviewed — no change needed
> Partly superseded 2026-11-01 by "2026-11-01 — Lore and config consistency pass" (Xaero's Minimap was added with the 2026-11-01 update; its entity radar is off).

- **Goblin Traders** — already gated (25% spawn chance + intervals); default is fine.
- **Guard Villagers** — fits the colony/village-defence theme; default fine.
- **Applied Mekanistics, Create Slice & Dice** — Create/AE2/Mek-tier automation that already sits behind existing gating.
- **Xaero's World Map** (world-map only, no live minimap/radar) — mild exploration concern only; left default.
- **Phantom Remover, FallingTree, Too Fast, Fast Leaf Decay, clientcrafting, Visual Workbench, Double Doors** — deliberate QoL choices; left as chosen.
- **Structures** (Spooky Campsite, Big Lost City, Abandoned Watchtowers Refurnished) — fit "explore ruins"; loot handled by existing Lootr.
- **Furniture/Connected Glass, libraries, client/perf mods** — cosmetic/support; no balance impact.

#### Rare horror spawns via OpenLoader (`HorrorSpawnRarity` datapack)
> Partly superseded 2026-11-01 by "2026-11-01 — Lore and config consistency pass" (the Pale Hound is timer-summoned; its biome-tag override was ignored, and it is set by `pale_hound-server.toml` instead).

**The Pale Hound, Rake: The Arrival, The One Who Watches, Antlers (Wendigo)** have no in-mod rarity config, so they're gated by overriding their Forge biome-modifier spawns. Reasoning: vanilla hostiles spawn at weight ~95–100 each; the pack's established rare-horror benchmark (Weeping Angels) is **weight 2**, so all are aligned to that:
| Mob | Spawn entities | Was | Now | Biomes |
|---|---|---|---|---|
| Rake: The Arrival | `rake_spawn_trigger`, `rake_stalking`, `rake_following` | 8 | **2** (~4× rarer) | forest / taiga (kept) |
| The One Who Watches | `toww_stalking`, `toww_staring` | 20 | **2** (~10× rarer) | any |
| Wendigo | `antlers:wendigo` | 20 | **2** (~10× rarer) | any |
| Whispering Spirits (added 2026-07-09) | `whispering_spirits:whispering_spirit` | 20 | **2** (~10× rarer) | any |
| Pale Hound | *(code-weighted — no biome-modifier file; corrected 2026-11-01, see below the table)* | all "spooky" biomes | biome tag **replaced** with a tight remote set | deep_dark, dark_forest, snowy_taiga, grove, frozen_peaks |

Whispering Spirits was also a top server-tick cost: see "Performance — Spark server-thread profile (2026-07-09)".

The four data-driven mobs are done by overriding the mods' own `forge/biome_modifier/*.json` at the same path (clean replacement, no duplication). Pale Hound's weight isn't config-exposed, so it's rarified by restricting its spawn-biome tag to a few remote cold/dark biomes; if it still feels too common, a `remove_spawns` + `add_spawns` biome modifier is the follow-up (carries a modifier-ordering caveat). (Corrected 2026-11-01: the Pale Hound is timer-summoned through its per-world `pale_hound-server.toml`. That config's 40-biome blacklist applied, so this tag override was ignored and the hound could spawn in every Terralith and modded biome.)

> Note 2026-11-06: the July overrides missed `toww_hanging` (weight 30), `toww_delusion` (3), `toww_hallucination` (5) and Rake's `ambient_sound` (15); all were set to 2 in "2026-11-06 — Server tick audit: phases 0 and 1".

##### Antlers dead-animal bodies — *worldgen decoration, same datapack*
Antlers also scatters static **dead-animal body blocks** (`bloody_cow`, `bloody_fox`, `bloody_pig`, `bloody_sheep`) across the surface of coniferous biomes (`#forge:is_coniferous`) via worldgen placed features — not mob spawns. They were dense enough to read as clutter rather than an eerie discovery. Each of the four `worldgen/placed_feature/*.json` is overridden at the same path with its `rarity_filter` **`chance` 17 → 68** (≈4× rarer, ~1 body per 68 chunks). Nothing else in the placement (biome/height/square) changed, so bodies still only appear on the ground in the same biomes — just far less often.

| Body | Placed feature | `chance` was | after |
|---|---|---|---|
| Dead cow / fox / pig / sheep | `antlers:deadcow/deadfox/deadpig/deadsheep` | 17 | **68** (~4× rarer) |

**Why:** keeps the unsettling "something died out here" atmosphere as an occasional find instead of a constant sight, matching the pack's "rare > frequent" and ambient-declutter passes (cf. Alex's Mobs fauna thinning). *Tunable:* raise `chance` further for even rarer bodies, lower back toward 17 for more. *Note:* worldgen decoration is baked in at chunk generation, so this only affects **newly generated** chunks — already-explored terrain keeps whatever it generated with.

*(FTB Quests / KubeJS: no additions — none of the new mods add craftable progression items needing gating; the new horror mobs are encounters that fit the planned "Horror Investigation" quest path, not built in July 2026.)*

---

### Power & magic pass (Extreme Reactors, Blood Magic, Mahou Tsukai)

A second batch of mods was added: **Extreme Reactors 2** (+ ZeroCore2 lib + Create-compat addon), **Blood Magic**, and **Mahou Tsukai** (a very powerful Fate-style magic mod). All three are tech/magic power systems and were reviewed against the pack's "power is earned, world stays dangerous" theme.

#### Extreme Reactors (`config/extremereactors/common.toml` + `ExtremeReactorsProgression` datapack) — *global*
**Goal:** temper a notoriously overpowered power system so it's a genuine late-game milestone, parallel to Mekanism fission — not a cheap infinite-FE shortcut.

**`config/extremereactors/common.toml` (raw output — the only lever for this; reactant/reaction energy is code-registered, not data-driven, so OpenLoader can't touch it):**
| Setting | Before | After |
|---|---|---|
| `powerProductionMultiplier` | 1.0 | **0.5** (output halved) |
| `fuelUsageMultiplier` | 1.0 | **1.5** (yellorium burns 50% faster) |

**`ExtremeReactorsProgression` datapack** — the **reactor and turbine controllers** (the single one-per-multiblock chokepoint; neither machine can be built without one) require Mekanism tech tiers, mirroring the `MekanismProgression` fission gating:
| Controller | Also requires |
|---|---|
| Basic reactor / turbine controller | **reinforced alloy + elite control circuit** (mid-game) |
| Reinforced reactor / turbine controller | **atomic alloy + ultimate control circuit** (late-game) |

Both the yellorium and uranium recipe variants are overridden so there's no cheap bypass. Casings, fuel rods and other bulk parts are left as-is (gating the one-per-build controller delays access without making the bulk build tedious).

**Why:** output ×0.5 + fuel ×1.5 keeps reactors strong but ties them to an ongoing yellorium cost (mining/exploration) instead of being free perpetual power; the controller gate means ER can't be tapped at all until Mekanism mid/late tech exists, so it sits *alongside* fission as an earned milestone rather than skipping the tech tree. *(Tunable: the two multipliers in common.toml — raise toward 1.0 for a lighter touch, lower for heavier.)*

#### Blood Magic (`config/bloodmagic-common.toml`) — *global, light touch*
**Goal:** align the LP economy with "resources are earned" — everything else about Blood Magic (altar tiers, blood orbs, demon will, soul forge, 814 self-tiered recipes) is already grindy and thematically **perfect** for a horror pack, so no recipe gating was added (it would be redundant and fight the mod's own progression).

- **`sacrificialValues`:** passive breed-farm livestock (cow, chicken, horse, sheep, pig, rabbit) **100 → 50 LP/HP**. Villager (100 — a real, weighty sacrifice), tamed pets wolf/ocelot (100), slime (15), enderman (10) left unchanged.

**Why:** an auto-breeding cow/chicken farm feeding a Well of Suffering is a free LP firehose — the same "press button, receive resource" pattern the pack nerfs elsewhere (e.g. gating the Mek Digital Miner). Halving trivial livestock keeps the blood economy meaningful without gutting the intended sacrifice/altar loop. The Teleposer (a possible "free teleport" conflict) is already gated behind the demon-will Teleposer Focus + ender pearls and costs LP per jump, so it's consistent with the pack's "teleport costs something" rule — left as-is.

#### Mahou Tsukai (`mahoutsukai-server.toml` → *defaultconfigs*) — *global (new worlds)*
> Note 2026-11-08: Waystone warping costs no XP since "2026-11-08 — Waystones: warping costs no XP", so the XP-costed-Waystones reason for the 256-block displacement cap no longer holds. The cap is unchanged.

**Goal:** rein in an extremely powerful spell mod so its teleportation falls in line with the pack's deliberate travel rules; leave its (already soulbind-gated) combat power intact pending playtest.

Mahou's balance lives entirely in a **server** config (`mahoutsukai-server.toml` — ~1350 lines, hundreds of `*_MANA_COST` / `*_DAMAGE` keys plus toggles) that Forge only generates on **world creation**. A world was generated; the pristine file was copied to `defaultconfigs/` (same generate → edit → copy workflow as `cave_dweller-server.toml`, `create-server.toml`, `minecolonies-server.toml`, `themimic_er-server.toml`) and these keys edited:

| Key | Before | After | Why |
|---|---|---|---|
| `EQUIVALENT_DISPLACEMENT_DIMENSIONAL_TRAVEL` | true | **false** | No cross-dimension teleport (matches Weeping Angels `interdimensional_teleporting=false`) |
| `EQUIVALENT_DISPLACEMENT_MAX_DISTANCE` | -1.0 (∞) | **256.0** | Displacement becomes a *local* tactical/return tool; long-distance travel stays channelled through the XP-costed Waystones |
| `REPLICA_TELEPORT_CROSS_DIMENSION` | true | **false** | Combat clone can't chase across dimensions |

**Why:** these are the only spells that directly conflict with an established pack rule ("no free/cross-dim teleport"). Everything else in Mahou is heavily self-gated (soulbound spell binding, slow mana-pool growth, ritual requirements) and fits the forbidden-magic horror theme, so combat costs/damage were **left at default** rather than inflated on guesswork.

*Left at default (candidates for a post-playtest pass if magic feels too strong):* offensive spell mana costs (Black Flame 300, Emrys focused 200/s, Borrowed Authority 900, Mystic Staff Big Explosion 5000, Reality Marble 4000), Drain Life, and Death Collection revival. `CREATIVE_IGNORES_MANA_COSTS` left true (creative-only). **Note:** `defaultconfigs/` applies to **new** worlds; the `Test*` worlds of July 2026 keep their own `serverconfig/mahoutsukai-server.toml` (delete it to regenerate from the new default).

---

### Performance — server-tick lag (worldgen)

**Symptom:** severe *tick* lag (not FPS) — entities stutter, interactions delayed. `logs/latest.log` showed the server up to **1351 ticks (67 s) behind** (`Can't keep up! … Running Xms behind`), clustered during exploration/chunk generation.

**Root cause:** **More Underground Structures** places its `catacomb` (a 29 KB multi-chunk structure) as a worldgen **`structure_feature`** rather than a proper jigsaw structure. Being larger than one chunk, its generation writes blocks into not-yet-generated neighbour chunks → `Detected setBlock in a far chunk`, which forces synchronous cascade chunk-gen mid-generation and stalls the worldgen workers / server thread. The log recorded **15,476** such errors from `more_underground_structures:catacomb_feature` alone (next worst: `creepy_1_feature`, 2).

**Fix (`PerfFixes` OpenLoader datapack):** override the mod's own `forge/biome_modifier/catacomb_feature_biome_modifier.json` at the same path with `{ "type": "forge:none" }` — Forge's no-op modifier. The catacomb feature is no longer added to any biome, so it stops generating (and stops the far-chunk cascade). The rest of the mod (chest dungeons, ore blobs/veins, ice/obsidian spikes, other small structure-features) is untouched.

*Scope:* worldgen is baked in at chunk generation, so this only affects **newly generated** chunks — already-explored terrain (and any catacombs already placed) is unchanged, but exploring new terrain will no longer trigger the stall. *Revert:* delete `config/openloader/data/PerfFixes/`.

#### Follow-up (2026-07-09) — extended to the `creepy_1`–`creepy_4` features
The catacomb fix was **incomplete**: post-fix logs (2026-07-09 sessions) still recorded the same `Detected setBlock in a far chunk` cascade, from `more_underground_structures:creepy_1_feature` (12×) and `creepy_4_feature` (9×) during exploration. These are the same oversized-building class as catacomb — multi-chunk structures injected via `forge:add_features` at the `underground_structures` step. Added `forge:none` overrides for **`creepy_1`, `creepy_2`, `creepy_3`, `creepy_4`** (only 1 & 4 were observed in logs; 2 & 3 disabled proactively as the same structure family). Ore veins/blobs, spikes, fossils, and the small chest-dungeon features remain untouched. *Monitor:* if new far-chunk errors name other MUS features (e.g. the `big*` hideouts, tombs, shrines, gateways, wells), disable those the same way.

> **Note — this is not the whole lag story.** The worldgen cascade explains the *exploration* spikes. The **worst** stalls in the 2026-07-09 logs were *progressive* (7 s → 65 s over ~6 min) and coincide with `Saving oversized chunk [-4, 66] (1.4–1.6 MB) to saves/Bruh/entities/…` — a runaway **entity accumulation** at Bruh chunk **[-4, 66]** (block ~x=-64, z=1056). That is a separate, likely-dominant cause and is **not** addressed by this datapack. See "Performance — entity accumulation at `Bruh [-4, 66]`".

---

### Performance — entity accumulation at `Bruh [-4, 66]`

**Symptom:** the largest server-tick stalls on 2026-07-09 grew progressively (up to **1314 ticks / 65 s behind**) and coincided with repeated `Saving oversized chunk [-4, 66] (1.48 MB → 1.64 MB) to saves/Bruh/entities/c.-4.66.mcc`. A 1.6 MB **entity** chunk = thousands of entities piled in one spot (block ~x=-64…-49, z=1056…1071 in the **Bruh** world). Every entity ticks every tick, so the pile drags the whole server thread.

**Likely source:** consistent with the anti-lag Spark census in "KubeJS — automatic dropped-item cleanup" (lootr_minecart + tnt_minecart + skeletons) — most probably a loaded **When Dungeons Arise** dungeon (or other mob-dense structure) at that location, spawning faster than mobs despawn.

**Why `anti_lag_items.js` doesn't catch it:** `kubejs/server_scripts/anti_lag_items.js` only runs `kill @e[type=item]` — it clears **loose items only**. A pile of **mobs/minecarts** is untouched by it.

**Safety net added (`anti_lag_mobs.js` + `AntiLagEntities` datapack):** a companion to `anti_lag_items.js` that culls **runaway hostile mobs** (the item script only handles `@e[type=item]`; a pile of mobs sailed past it). Same warn-then-clear, throttled, threshold-gated design.
- **Whitelist-only:** culls only entity types in the `#antilag:cullable_hostiles` tag (common vanilla hostiles: zombie/husk/drowned/zombie_villager, skeleton/stray, spider/cave_spider, creeper, silverfish, endermite, witch, phantom, slime). Everything else — pets, villagers, animals, **minecarts (incl. Lootr/TNT dungeon loot)**, item frames, armor stands, paintings, boats, bosses, and all modded/horror mobs — is untouched by omission.
- **Persistence-protected:** `nbt=!{PersistenceRequired:1b}` spares name-tagged mobs, geared mobs, spawner-persistent mobs, and raid mobs.
- **Threshold:** `MOB_THRESHOLD = 200` cullable hostiles per dimension (well above normal, since the pack suppresses natural hostile spawns), 10 s warning, ~15 s check interval.
- *Tunable:* edit the knobs at the top of `kubejs/server_scripts/anti_lag_mobs.js`; extend the whitelist by editing `config/openloader/data/AntiLagEntities/data/antilag/tags/entity_types/cullable_hostiles.json`. *Revert:* delete the script + the `AntiLagEntities` datapack.

**Open (needs an in-game visit):** what accumulates at Bruh **~x=-64, z=1056** (dungeon? spawner? mob crowd? loot minecarts?) was not identified. A spawner can be lit or removed; structural loot minecarts are outside the cull by design and would have to be relocated or accepted. The safety net caps *mobs*, not structural entities.

---

### Performance — Spark server-thread profile (2026-07-09)

Server-thread Spark profile, 224 s of samples. Source: https://spark.lucko.me/aYH6AP5pvl. Ranked by self-time:

| Rank | Frame | Self-time | Notes |
|---|---|---|---|
| 1 (mods) | `whisperingspirits…PlayerLookAtSpiritProcedure.onPlayerTick` | ~10.7 s (**~4.8%**) | **#1 mod cost.** Runs every tick per player, scanning nearby spirit entities. |
| — | `net.minecraft.server.commands.TeleportCommand` (+lambda) | ~8.7 s (**~3.9%**) | Heavy programmatic teleports — almost certainly Whispering Spirits repositioning its spirit entities each tick. |
| — | vanilla `MinecraftServer` / `BlockableEventLoop` / `Level` / `ServerChunkCache` / `Entity` | ~40%+ combined | Engine baseline + idle/wait for a 285-mod pack; not a single fixable hog. Entity ticking ≈3.7%. |

Other MCreator horror procedures were **minor** in this sample (`net.theobsessed` ~0.1%, John mod / Weeping Angels negligible) — Whispering Spirits is the outlier.

**Root cause of the WS cost:** it spawned at **weight 20 in `forge:any` biome** — vs. the pack's documented horror-mob benchmark of **weight 2** (Weeping Angels, Rake, TOWW, Wendigo). So ~10× too many spirit entities existed everywhere, and its per-tick look-detection + teleport procedure processed all of them. WS ships **no config file** (only a spawn biome-modifier), so it can't be tuned directly.

**Fix (`HorrorSpawnRarity` datapack):** override `whispering_spirits:whispering_spirit_biome_modifier` to **weight 20 → 2**, mirroring the exact pattern/precedent used for Rake/TOWW/Wendigo (see "Rare horror spawns via OpenLoader"). ~10× fewer spirits ⇒ proportionally cheaper per-tick procedure and far fewer teleports — the single biggest systematic tick win available without removing a mod. Worldgen-independent; takes effect on next world load. *Tunable:* raise the weight for more spirits / heavier tick cost. *If still too heavy:* WS would have to be removed, as it has no other lever.

> **Relationship to "Performance — entity accumulation at `Bruh [-4, 66]`":** these are distinct. The Spark aggregate shows the *systematic* per-tick drain (Whispering Spirits). The oversized-entity-chunk stalls are *episodic* spikes at one location. Both are addressed — WS rarified here, runaway mobs capped by `anti_lag_mobs.js`.

---

### KubeJS — Old Civilisation script bugfix (`oc_interactions.js`)

`logs/latest.log` showed **167** repeated errors from `oc_interactions.js` — `InternalError: TypeError: redeclaration of var pd` (tick handler, ×125) and `… var e` (residue-drop death handler, ×42). KubeJS's Rhino engine mis-hoists `const`/`let` declared inside a `try` block in a repeatedly-invoked callback, so **the entire OldCiv discovery / stateful-item / endgame-grant / horror-residue system silently never ran.** Fixed by switching the in-callback declarations from `const`/`let` to `var` (the Rhino-safe idiom) in all four runtime handlers (`PlayerEvents.tick`, `EntityEvents.death`, both `ItemEvents.rightClicked`). Module-level `const`s (which load once and were fine) left as-is.

**Verification (2026-07-09):** the log after the fix showed **0** `redeclaration of var` errors, so the interaction/variant/residue system runs.

---

### KubeJS — Old Civilisation loot coverage (`oc_loot.js`) — *modded-structure gap*
> Partly superseded 2026-11-01 by "2026-11-01 — Mod-update review: about 80 mods updated, 13 jars removed" and "2026-11-02 — Mod set changes, 2026-11-01 to 2026-11-02" (nether portals enabled, then TerraFirmaCraft disabled: the nether is reachable, and Better Nether Fortresses chests are an un-routed gap, listed in `UPDATE_REVIEW_2026-11-01.md` §6 and still noted as not routed in `oc_loot.js` on 2026-11-08).

**Symptom:** custom lore items did not appear in loot chests.
- **Not a code bug.** Items register fine; `oc_interactions.js` is fixed ("KubeJS — Old Civilisation script bugfix"); and `oc_loot.js`'s LootJS chain (`addLootTableModifier(...).randomChance(...).addLoot(...)`) *is* valid for LootJS **2.13.1** — the returned `LootActionsBuilderJS` implements the `LootConditionsContainer` + `LootActionsContainer` interfaces, so `randomChance`/`addLoot` are inherited default methods. (The script's own try/catch never logged a failure, confirming no runtime error.)
- **Cause: coverage gap.** `oc_loot.js` originally injected only into **vanilla** `minecraft:chests/*` tables (+ `ad_astra_more_structures` space chests + `born_in_chaos_v1`/`foes` mob drops). But the pack's headline structures ship their **own** loot tables, which were never targeted: **When Dungeons Arise** (168 tables, `dungeons_arise:chests/*`), **YUNG's Better Strongholds / Desert & Jungle Temples / Ocean Monuments / Witch Huts** (own `better*:chests/*`), **More Underground Structures** (`more_underground_structures:chests/*`), **Lost Architects** (`lost_architects:chests/*`). Only YUNG's Better **Dungeons** & **Mineshafts** were covered — they reuse the vanilla `simple_dungeon`/`abandoned_mineshaft` tables. Combined with the intentionally-low per-item chances, players exploring the pack's real content saw effectively nothing.

**Fix:** extended `oc_loot.js` with LootJS regex modifiers routing each modded structure family to fitting item categories, at **slightly lower chances than the vanilla equivalents** (horror-pack "rare finds"):
- **When Dungeons Arise** — routed by structure theme: undead/plague/infested → horror (biological_sample, impossible_bone, corrupted_map, distorted memory); settlements/homes/camps → personal (data chip, photo, currency, handbook); keeps/towers/forts → military-personal + technical (id_badge, incident_report, currency); foundry/mechanical/mines → technical (incident_report, audio_log, containment_label, signal_device); monastery/heavenly/mythic → arcane (arcane_notes, distorted memory, bound_thought, unstable_memory).
- **YUNG's Better Strongholds** → magic + technical; **Desert/Jungle Temples** → ancient/magic; **Ocean Monuments** → ocean; **Witch Huts** → magic/horror.
- **More Underground Structures** → underground-dungeon (personal, technical, faded memory, biological_sample).
- **Lost Architects** → artificer's workshop = technical/arcane; brawler's guild = personal/library.

**Deliberately skipped:** YUNG's Better **Nether Fortresses** (`betterfortresses`) — TFC disabled nether portals in July (`enableNetherPortals=false`), so they were unreachable. YUNG's Dungeons/Mineshafts already covered via vanilla tables.

*Scope:* server script, takes effect on world load / `/reload`. All 17 referenced item IDs verified to have models; script passes `node --check`. *Tunable:* the per-line `randomChance` values in `oc_loot.js`. *Note:* loot is baked when a chest is first rolled, so this affects **newly generated / not-yet-opened** chests, not ones already looted. (Lootr is compatible — LootJS modifies the table itself, which Lootr then rolls per-player.)

---

### TerraFirmaCraft — HUD (`config/tfc-client.toml`) — *client display only*
> Superseded 2026-11-01 by "2026-11-02 — Mod set changes, 2026-11-01 to 2026-11-02" (TerraFirmaCraft disabled). Its configs and `TFC*` packs were deleted from the client instance and the repo on 2026-11-07 ("2026-11-07 — Repository cleanup: personal files purged from history, orphaned configs removed").

TFC's HUD overhaul is on by default and replaced the vanilla health/hunger bars (continuous TFC bars, health shown as `750 / 1000`), which also shifted the armour row. The two bars that map onto vanilla stats were turned off, and the thirst bar with thirst itself:

| Setting | Before | After |
|---|---|---|
| `enableHealthBar` | true | **false** (vanilla hearts) |
| `enableHungerBar` | true | **false** (vanilla hunger) |
| `enableThirstBar` | true | **false** (2026-07-09) |

`enableThirstBar` went to `false` on 2026-07-09, when thirst was disabled ("TerraFirmaCraft — disable thirst + lessen hunger"); until then it stayed `true` as the readout for TFC's thirst mechanic. `healthDisplayStyle` is moot (health bar is vanilla). Client-side display only.

---

### TerraFirmaCraft — disable thirst + lessen hunger (`tfc-server.toml` + `tfc-client.toml`) — *per-world → defaultconfigs (+ client)*
> Superseded 2026-11-01 by "2026-11-02 — Mod set changes, 2026-11-01 to 2026-11-02" (TerraFirmaCraft disabled; its configs deleted on 2026-11-07, "2026-11-07 — Repository cleanup: personal files purged from history, orphaned configs removed"). Earlier, `passiveExhaustionMultiplier` went 0.5 → 0.0 on 2026-07-12 in "TerraFirmaCraft — hunger back to (essentially) vanilla (2026-07-12)".

**Goal:** remove the thirst survival layer entirely and make hunger drain more slowly.

| Setting | File | Before | After | Effect |
|---|---|---|---|---|
| `thirstModifier1` | `tfc-server.toml` `[mechanics.player]` | 5.0 | **0.0** | Thirst never drains (`0 = None` per TFC) — no dehydration, ever |
| `enableThirstOverheating` | `tfc-server.toml` | true | **false** | Belt-and-suspenders: no extra thirst loss in heat (moot at modifier 0) |
| `enableThirstBar` | `tfc-client.toml` | true | **false** | Hides the now-static thirst bar from the HUD |
| `passiveExhaustionMultiplier` | `tfc-server.toml` | 1.0 | **0.5** | Hunger accrues at **half** rate (~1 hunger bar per 5 days instead of 2.5) |

**Why:** Design: no thirst mechanic, and roughly half the hunger pressure. `thirstModifier1 = 0` is TFC's documented off switch; the bar is hidden since it would just sit full. Hunger pressure is roughly halved rather than removed (set `passiveExhaustionMultiplier` to `0` to disable hunger entirely, or higher toward 1.0 for more pressure).

**Scope/apply:** thirst/hunger are per-world server config — edited in the world `saves/Bazinga/…` and in `defaultconfigs/tfc-server.toml` for new worlds; the bar toggle is global client config. Backups (local only; `*.bak` is git-ignored): `tfc-server.toml.bak` (both), `tfc-client.toml.bak`. Active on next world load (server) / game restart (client HUD). *Revert:* restore the `.bak`s or reset the four values.

---

### TerraFirmaCraft — food hunger by type (`TFCFoodHunger` datapack) — *global*
> Superseded 2026-11-01 by "2026-11-02 — Mod set changes, 2026-11-01 to 2026-11-02" (TerraFirmaCraft disabled). The pack was deleted from the client instance and the repo on 2026-11-07 ("2026-11-07 — Repository cleanup: personal files purged from history, orphaned configs removed"); the VM still has it, inert (Appendix A).

**Goal:** undo TFC's flat "every food restores 4 hunger (2 shanks)" — scale hunger by food type so cooking real meals is worthwhile, while keeping TFC's nutrition-category system intact.

**Mechanism (not a config lever):** TFC sets hunger per-food in `data/tfc/tfc/food_items/*.json` (`"hunger": 4` on every one — 170 defs). There's no global multiplier, so this is a datapack overriding those defs at the same path (same technique as `TFCLightItems`/`PerfFixes`). **Only the `hunger` field is changed** — `saturation`, `decay_modifier`, and the `protein`/`grain`/`vegetables`/`fruit`/`dairy` nutrition values (which drive max-health) are preserved exactly.

| Food tier | Detection | Hunger |
|---|---|---|
| Cooked meats/fish + prepared meals (soup, stew, salad, sandwich) | `cooked_*` or meal keyword | **8** |
| Bread | `*bread*` (not dough/flour) | **6** |
| Raw meat/fish, raw fruit/veg | has `protein` / `fruit` / `vegetables` | **5** |
| Grain / flour / dough (processing intermediates) | grain keywords | **4** (TFC default — left unchanged) |

**Result:** 146 overrides written (57 → 8, 6 → 6, 83 → 5); the 24 grain/flour/dough intermediates keep the default 4 (no file needed). Cooking a meat or making a meal restores 4× a raw grain and 2× a raw ingredient, preserving the "cook real food" incentive.

**Scope/apply:** global datapack, active on world load / `/reload`; all 146 files validated. *Note:* food data is applied live (not baked into worldgen), so it affects all food immediately. *Tunable:* re-run with different tier values, or edit individual `food_items/*.json` in the pack. *Revert:* delete `config/openloader/data/TFCFoodHunger/`.

---

### TerraFirmaCraft — modded-food edibility + food decay off (`TFCModdedFood` datapack + `tfc-server.toml`) — *global (+ per-world → defaultconfigs)*
> Superseded 2026-07-12 by "TerraFirmaCraft — hunger back to (essentially) vanilla (2026-07-12)": the `#forge:foods` reference never loaded, so the catch-all matched no item; the pack was retired to `data-disabled/` and replaced by `kubejs/server_scripts/tfc_vanilla_food.js` (`foodDecayModifier = 0` kept). TerraFirmaCraft itself was disabled on 2026-11-01 ("2026-11-02 — Mod set changes, 2026-11-01 to 2026-11-02"), and `data-disabled/TFCModdedFood` was deleted on 2026-11-07 ("2026-11-07 — Repository cleanup: personal files purged from history, orphaned configs removed").

**Goal:** make non-TFC (modded) foods actually restore hunger, and stop food rotting / not stacking.

**Cause:** TFC swaps the player's food data for `TFCFoodData`, whose `eat()` calls `FoodCapability.get(stack)` and **returns early if the item has no TFC food definition**. So every modded/undefined food (Rationcraft cans, Farmer's Delight, Soldier's Delight, Candlelight, Farm & Charm, …) restores **zero** hunger. Hardcoded — no config toggle; the only fix is to give those items a TFC food definition. Source: decompiled `TFCFoodData#eat`.

**`TFCModdedFood` datapack:** one food def (`data/tfc/tfc/food_items/modded_catchall.json`) matching a custom aggregate tag `#tfcmoddedfood:nourishing` → `hunger 4, saturation 1.0`. That tag unions `#forge:foods` plus the Rationcraft/Voidless food tags (`#rationcraft:cans`, `#rationcraft:milk`, all `#voidcans:*`, `#voidless_mre:*`). TFC's own foods are **not** in `#forge:foods`, so their tuned defs aren't shadowed (`getDefinition` would otherwise return whichever matches first).

**`foodDecayModifier` 1.0 → 0.0** — set in **all four** `tfc-server.toml` (the client instance's and the July server's `defaultconfigs/`, plus the worlds `saves/Bazinga/serverconfig/` and the July server's `world/serverconfig/`). Disables food decay entirely; per TFC's own note this also makes food **lose creation dates → stack freely** (fixes food not stacking). Required so the catch-all defs don't turn modded food into rotting, freshness-split TFC food. **Irreversible for existing food** — creation dates are dropped and can't be restored.

**Why:** consistent with the pack's TFC-survival trims (thirst off, hunger halved, item sizes neutralized). Intended result: modded food nourishes and stacks; nothing rots. *Tunable:* values in `modded_catchall.json` (optional: add TFC nutrients so modded food builds max-health nutrition, not just fills hunger). *Coverage caveat:* the **opened**-can variants (`open_*`/`h_open_*`, reached only via the can-opener recipe) aren't tagged, so they're uncovered — but with Rationcraft's `Edible Cans = true` sealed cans are eaten directly, and those are covered. Optional: add the opened variants to `nourishing.json` to cover the opener path. *Apply:* world load / `/reload`. *Revert:* delete `config/openloader/data/TFCModdedFood/` (decay dates already lost regardless).

---

### TerraFirmaCraft — collapses & landslides (`tfc-server.toml`) — *per-world → defaultconfigs*
> Superseded 2026-11-01 by "2026-11-02 — Mod set changes, 2026-11-01 to 2026-11-02" (TerraFirmaCraft disabled). Its configs were deleted from the client instance and the repo on 2026-11-07 ("2026-11-07 — Repository cleanup: personal files purged from history, orphaned configs removed").

**Goal:** stop TFC's block-gravity mechanics from tearing apart world-gen **floating structures** (e.g. skyships), where breaking a single block cascades the build apart.

TFC has no concept of "intended build" — it applies geology physics to any qualifying block. Two mechanics live in `[mechanics.collapses]`; both were fully disabled via their `enable*` gates (chance/radius values left as-is, since the toggles gate the behaviour entirely):

| Setting | Before | After | What it does |
|---|---|---|---|
| `enableBlockLandslides` | true | **false** | Gravity blocks (TFC dirt/gravel/sand/cobble) fall on block update — **the skyship culprit** |
| `enableBlockCollapsing` | true | **false** | Raw-rock collapses when mining unsupported stone |
| `enableExplosionCollapsing` | true | **false** | Explosions trigger immediate raw-rock collapses |
| `enableChiselsStartCollapses` | true | **false** | Chiselling raw rock can start a collapse |

**Why:** the landslide toggle is the direct fix for floating structures (breaking a block no longer slides the rest away); the three collapse toggles are disabled alongside it so mining/explosions/chisels can't drop rock on builds or players either. Removes TFC's signature mining hazard — acceptable trade to keep structure integrity. As a side benefit, this also removes collapse/landslide item-spam, one of the loose-item sources flagged in "KubeJS — automatic dropped-item cleanup".

**Scope/apply:** edited in the worlds `saves/New World/…` and `saves/Bruh/…`, and copied to `defaultconfigs/tfc-server.toml` for every new world. Backups (local only; `*.bak` is git-ignored): `tfc-server.toml.bak` beside each world file. *Revert:* restore the `.bak`, or set the four toggles back to `true`; delete `defaultconfigs/tfc-server.toml` (or flip its toggles) for new worlds. *Tunable:* re-enable collapses alone (keep landslides off) to bring the mining hazard back without the structure damage.

---

### TerraFirmaCraft — item size & weight (`TFCLightItems` datapack + `TFCNoSizeTooltip` resource pack) — *global*
> Superseded 2026-11-01 by "2026-11-02 — Mod set changes, 2026-11-01 to 2026-11-02" (TerraFirmaCraft disabled). Both packs were deleted from the client instance and the repo on 2026-11-07 ("2026-11-07 — Repository cleanup: personal files purged from history, orphaned configs removed"); the VM still has `TFCLightItems`, inert (Appendix A).

**Goal:** neutralize TFC's item Size/Weight system — every item stacks to 64, no storage/overburden restrictions, and no Size/Weight tooltip clutter.

**Not config-toggleable.** There is no flag for this in any `tfc-*.toml`. The system is code- and data-driven (`net.dries007.tfc.common.capabilities.size.ItemSizeManager`): an item's size/weight comes from a data definition (`data/tfc/tfc/item_sizes/*.json`) if one matches, else from hardcoded defaults (`ARMOR_SIZE`/`TOOL_SIZE`/`BLOCK_SIZE`/`DEFAULT_SIZE`). Weight → max-stack mapping is baked into the `Weight` enum (`very_light`=64 … `very_heavy`=4) and can't be redefined by data. The tooltip line (`addTooltipInfo`) is added unconditionally — no toggle removes it.

**`TFCLightItems` datapack** (`config/openloader/data/`): overrides all **56** built-in `item_sizes/*.json` at the same path, keeping each original `ingredient` but forcing `size: tiny`, `weight: very_light`. Effect: every TFC-defined item (ingots, ore, food, tools, blocks, vessels…) stacks to **64** and is small enough to bypass vessel/storage size gates and the Overburdened effect. (Armor/tools are already stack-1, so their stack is unchanged; the override removes their *storage* restriction where a data def covered them.)

**Added defs — `planks` + `lumber` (2026-07-10).** TFC ships **no** `item_sizes` def for planks or lumber, so both fell through to the code default weight **LIGHT = stack 32** (some woods stacked only to 32). Added two *new* defs at the same path forcing `size: tiny, weight: very_light` → stack **64**: `planks.json` (`#minecraft:planks`, which includes all `tfc:wood/planks/*`) and `lumber.json` (`tfc:lumber`). Logs/bark/stripped were already covered via `logs.json` (`#minecraft:logs`, weight → 64). This is exactly the "explicit tag override" escape hatch flagged in the caveats below. The datapack ships **56 built-in overrides + 2 added defs**.

**`TFCNoSizeTooltip` resource pack** (`config/openloader/resources/`): blanks the 12 `tfc.enum.size.*` / `tfc.enum.weight.*` lang keys to `""`, so the Size/Weight **wording** disappears from the tooltip on **every** item — including code-default items like armor that the datapack can't reach.

**Why:** this is the closest achievable to "disable it" without patching the jar. The datapack kills the *mechanic* (small stacks, storage limits, overburden); the resource pack kills the *display text*.

**Caveats / limits:**
- The tooltip **line** is hardcoded, so blanking the lang may leave a thin empty line — the *text* is gone but the line itself can't be fully removed by a resource pack.
- Blanking those enum keys also empties the words wherever else they appear (e.g. the TFC field guide's size/weight references).
- Code-default items (armor, and any modded item not matched by a data def) keep their underlying code-assigned size for *storage* purposes — only their tooltip text is blanked. Fully neutralizing their storage behaviour would need explicit tag overrides in `TFCLightItems`.
- A true, complete removal (line and all) would require a mixin/jar patch — out of scope for the config/datapack methodology.

**Scope/apply:** global (all worlds); active on next world load (datapack) / game restart (resource pack). English-only lang blanking (`en_us`); optional: add other `*_us`/locale files. *Revert:* delete `config/openloader/data/TFCLightItems/` and `config/openloader/resources/TFCNoSizeTooltip/`.

---

### TerraFirmaCraft — restore vanilla crafting table (`TFCVanillaCraftingTable` datapack) — *global*
> Superseded 2026-11-01 by "2026-11-02 — Mod set changes, 2026-11-01 to 2026-11-02" (TerraFirmaCraft disabled). The pack was deleted from the client instance and the repo on 2026-11-07 ("2026-11-07 — Repository cleanup: personal files purged from history, orphaned configs removed"); the VM still has it, inert (Appendix A).

**Goal:** craft a crafting table the vanilla way (4 planks in a 2×2), reverting TFC's early-game gate.

TFC **disables** the vanilla recipe — it ships `data/minecraft/recipes/crafting_table.json` as `{"conditions":[{"type":"forge:false"}]}` (a permanently-false Forge condition) — and replaces it with `data/tfc/recipes/crafting/vanilla/crafting_table.json`, a `tfc:damage_inputs_shapeless_crafting` requiring a **saw** (`#tfc:saws`) **+ an existing workbench** (`#tfc:workbenches`). That's a chicken-and-egg gate on the first table.

**Fix (`TFCVanillaCraftingTable` datapack):** overrides `data/minecraft/recipes/crafting_table.json` at the same path with the stock vanilla shaped recipe (`##`/`##` of `#minecraft:planks` → `minecraft:crafting_table`). OpenLoader datapacks load over mod-jar data (same mechanism as `TFCFoodHunger` / `TFCLightItems` overriding TFC's own data), so the vanilla recipe wins. TFC's saw+workbench recipe is **left in place** as an additional path (both craft a table). Optional, for vanilla-only: add an override deleting `data/tfc/recipes/crafting/vanilla/crafting_table.json`.

**Scope/apply:** global datapack; active on world load / `/reload`. JSON validated. *Revert:* delete `config/openloader/data/TFCVanillaCraftingTable/`.

---

### Xaero's World Map — performance (`config/xaero/world-map/…`) — *client*

A client-thread Spark profile flagged `xaero.map.*` (region / MapWriter / MapProcessor / biome) as the dominant **mod** load on the render thread — Xaero continuously scans chunks to write map tiles, which hitches during exploration. Trimmed the map-writing hot path:

| Setting | File | Before | After | Effect |
|---|---|---|---|---|
| `max_loaded_regions` | `world-map/client.cfg` | 300 | **100** | Fewer map regions held/processed in RAM (regions reload from disk when revisited — no data loss) |
| `biome_blending` | `world-map/profiles/default.cfg` | true | **false** | Drops per-pixel biome-edge blending during tile writing (slightly less smooth biome borders on the map) |

`map_writing_distance` (`world-map/profiles/default.cfg`) rejects 192 (log: `not valid for option map_writing_distance`); left at -1.

**Why:** both sit directly on the `MapWriter`/`biome` write path Spark flagged, and reduce it without disabling the map. Client-side/per-client, but shipped in the tracked config. *Tunable:* restore `biome_blending`/`max_loaded_regions` for prettier/larger caching at more cost. *Optional further step:* `update_chunks = false` stops Xaero re-scanning already-mapped chunks (bigger saving, but the map won't auto-update when terrain changes) — left **on**.

**Note:** this was a *render-thread* profile — it does not explain the **server-tick** lag (entities/interactions), which needs a `Server thread` profile (`/spark profiler --only-ticks-over 100`). Those are in "Performance — Spark server-thread profile (2026-07-09)" and "KubeJS — automatic dropped-item cleanup".

---

### KubeJS — automatic dropped-item cleanup (`kubejs/server_scripts/anti_lag_items.js`)

A `Server thread` Spark profile of a **fresh** world (≈11 TPS) showed the tick dominated not by any mod but by **~2,400 `minecraft:item` entities** on the ground (every loose item ticks every tick). The entity census in the profile metadata confirmed it: `minecraft:item` = 2405, everything else ≤122. No config was disabling item despawn, so a high-rate source (very likely a loaded When-Dungeons-Arise dungeon — the census also showed 32 `lootr_minecart` + 24 `tnt_minecart` + 122 skeletons — and/or TFC collapses) was outpacing the 5-minute vanilla despawn.

Added a throttled, threshold-gated cleanup:
| Knob | Value | Meaning |
|---|---|---|
| `ITEM_THRESHOLD` | 400 | only acts when loose items in a player's dimension exceed this (normal play rarely does) |
| `WARN_TICKS` | 200 (10s) | broadcasts a warning before clearing so players can grab keepers |
| `CHECK_INTERVAL` | 300 (~15s) | how often it counts (cheap: one `execute if entity` per interval) |

It counts via `execute if entity @e[type=item]` and, only above the threshold, warns then runs `kill @e[type=item]` in the player's dimension. Below the threshold it does nothing. Uses `var` inside the `PlayerEvents.tick` handler (the Rhino block-scope quirk that broke `oc_interactions.js`).

**Why:** a safety net so runaway item accumulation can't tank TPS again, without touching normal play. *This treats the symptom* — the root source (the mob-dense dungeon / collapse producing items) was not located; recommendation: find it and light it up or move away from it. *Tunable:* raise `ITEM_THRESHOLD` if 400 ever fires during legitimate play; lower it for a tighter cap.

---

### The Forgotten (`config/theforgotten-common.toml`) — *global*
> Superseded 2026-11-01 by "2026-11-01 — Mod-update review: about 80 mods updated, 13 jars removed" (jar removed; it was already disabled per `SERVER_MODS.md`). `config/theforgotten-common.toml` was deleted from the repo on 2026-11-07 ("2026-11-07 — Repository cleanup: personal files purged from history, orphaned configs removed").

A tension-driven psychological-horror mod (stalkers, apparitions, paranoia events, 3 dimensions, fake-presence scares). Trimmed the intrusive/meta scares to match the pack's "quiet dread > gimmick" line:

| Setting | Before | After | Why |
|---|---|---|---|
| `[dialogue] enabled` | true | **false** | Disables the sign-dialogue system (write on a sign → entity replies) |
| `[chat.creepy] sendChance` | 0.3 | **0.02** | Fake creepy chat lines (incl. the "guest? joined the game" spoof) made *really rare* |
| `[chat.creepy.interval] min / max` | 180 / 420 | **1800 / 3600** | …and the attempt window widened to 30–60 min |

**Fake open-to-LAN spoof — not configurable.** The LAN scare (`LAN_EVENT_TRIGGERED` / `S2COpenToLanPacket` in `ServerEvents`, read from the decompiled jar) is a **hardcoded scripted event with no config chance or toggle** — it's independent of `[chat.creepy]`, so it can't be made "rare" via config. It appears to be a limited/tension-gated one-off rather than a frequent roll. Removing it entirely would need disabling the mod or a patch. Left as is (rare by nature).

**Mob-spawn limiter — left ON.** `[mobSpawning.limiter] enabled = true` is a **server-wide natural-spawn interceptor** (`MobSpawnLimiter` hooks the spawn event and returns `DENY`): it blocks *most natural mob spawns across categories*, allows peaceful/animal mobs only ~8% of the time (`[mobSpawning.peaceful] allowChance = 0.08`, minus a `theforgotten:allow_peaceful_spawn` tag exception), and periodically culls excess ambient mobs (bats). It targets **natural** spawns only — spawner/structure spawns still work. **This sits on top of and largely overrides the pack's per-mob spawn tuning** (horror-mob rarity, Alex's Mobs −30%, etc.), so the overworld feels far emptier of normal wildlife/monsters than those configs imply.

**Loosened (not disabled):** `[mobSpawning.peaceful] allowChance` **0.08 → 0.35** — ~4.4× more natural animal/peaceful spawns get through, so the world feels alive again while the limiter still keeps things sparser than vanilla. **Caveat:** the config only exposes the *peaceful* allowance; monster/hostile natural-spawn suppression is hardcoded in `MobSpawnLimiter` with no knob, so hostiles stay heavily reduced (spawner/structure spawns unaffected). To also restore natural hostile spawns, the only lever is disabling the limiter outright. *Further option:* a `theforgotten:allow_peaceful_spawn` tag datapack can whitelist specific animals to always spawn.

---

### Performance-mods batch (tick + multicore) — *global*

A batch of server-tick / multicore performance mods was added (see `modlist.md`, regenerated via `tools/generate_modlist.py` — category rules updated so they bucket under *Core libraries & performance*). Config decisions:

#### DimThreads / Dimensional Threading Reforked (`config/dimthread-common.toml`)
> Superseded 2026-11-06 by "2026-11-06 — DimThreads disabled" (DimThreads and `immptl_dimthread_compat` are `.jar.disabled` on the client instance and the VM). `config/dimthread-common.toml` was deleted from the repo on 2026-11-07 ("2026-11-07 — Repository cleanup: personal files purged from history, orphaned configs removed").

Ticks each dimension on its own thread (the one real multicore lever on Forge; well-suited here given the ~43 registered dimensions). (Corrected 2026-11-06: 1.2.1 wraps every dimension's tick in one global lock — `DimThread.swapThreadsAndRun` is `static synchronized` — so dimensions tick one after another, with no parallelism.)
| Setting | Before | After | Why |
|---|---|---|---|
| `default_gamerule_threads` | 3 | **4** | Balanced dimension-thread pool — parallelism for the multi-dimension pack without starving the client/main thread; the mod auto-caps to available CPU cores |
| `ignore_tick_crash` | false | **false (kept)** | The mod flags this as *"very VERY experimental… world corruption"* — left OFF deliberately |

⚠️ **Caveats:** DimThreads lists known incompatibility with *some Applied Energistics 2 features* (AE2 is installed), and thread-safety is inherently risky in a 285-mod pack. **Back up before serious play** (Simple Backups is present). Existing world: the thread count is a gamerule initialised from the config default above; the crash-isolation gamerule `/gamerule dimthread_skip_crashing true` can keep one bad dimension from downing the session.

> Note 2026-11-02: Immersive Portals is also not thread-safe under DimThreads (its chunk-ticket flush assumes the server thread); bridged by the pack's own `immptl_dimthread_compat` shim. See "2026-11-02 — Immersive Portals × DimThreads: compat shim".

#### Left at defaults (verified appropriate — no change)
> Partly superseded 2026-11-06 by "2026-11-06 — Server tick audit: phases 0 and 1" (Data Anchor removed: an orphan library whose tick hooks cost ~1% of the overworld tick) and 2026-11-07 by "2026-11-07 — Chunky: silent by default" (Chunky `silent` true).

- **Canary** (Lithium-for-Forge) — `canary.properties` is intentionally empty = all optimizations ON. Drop-in; nothing to tune.
- **AI Improvements** (`aiimprovements-common.toml`) — defaults apply only the safe wins (cached-math look controller, AI call-bubbling). All the behaviour-degrading `remove_*` goal toggles are OFF by default; left off (removing them would break mob behaviour — cows not swimming, etc. — for marginal gain, wrong for a survival/horror pack).
- **Mobtimizations** — throttles idle/off-screen mob pathfinding; defaults are perf-positive without altering visible behaviour.
- **Connectivity** — packet/login-size limit fixes (beneficial for a 285-mod handshake) + malformed-traffic blocking; defaults fine.
- **Limited Chunkloading** — unloads chunkloaded chunks 10 min after a player leaves them (helps the idle-dimension baseline); default kept.
- **Structure Essentials** — faster structure lookup + reduced map/locate search radius; notably `disableLegacyRandomCrashes=true` and `warnMissingRegistryEntry=true`, which *harden worldgen against DimThreads' multithreaded RNG* — good synergy; defaults kept.
- **ChunkSending** (5 chunks/tick, dynamic), **Chunky** (pre-gen tool, on-demand), **CoroUtil / Data Anchor / RecipeEssentials** (libraries) — no meaningful config.

#### Also reflected in the modlist
The regenerated `modlist.md`/`modlist.json`/README also picked up previously-untracked mods: **Blood Magic, Mahou Tsukai, Extreme Reactors (+ ZeroCore 2)** — the modlist had been stale.

---

### KubeJS — Sanity mechanic (`kubejs/server_scripts/oc_sanity.js` + `OldCivSanity` datapack)
> Partly superseded 2026-11-08 by "2026-11-08 — Quest book: fixes and additions" (`HORROR_IDS` and the `horror_mobs` tag gained `cant_breathe:cant_breathe` and lost `man:*`).

**Full design & technical spec: `docs/sanity_mechanic.md`.** A hidden, per-player sanity value (0–100, baseline 100, in `persistentData`) that makes the world *respond to the player's exposure to it* — the lore's premise as a mechanic. **Invisible** (no bar/number; deliberately consistent with the reverted-to-vanilla HUD), **suppression-only**, and **spawns nothing**.

**Drains** (per 2 s, throttled tick): darkness (light < 4), depth (overworld Y < 0, scaling to −40), horror-mob within 16 blocks, and self-feedback from its own events. **Restores:** firelight (light ≥ 10), daylight + open sky, sleep (fast), and two **clarity consumables** — `sedative_ampoule` (flat restore, consumed) and `familiar_photograph` (smaller, cooldown'd) — registered through the existing Old Civilisation item/tooltip/loot pipeline.

Two consequence channels, both keyed off four bands (Calm/Unease/Dread/Breakdown, with hysteresis):
- **Channel A — ambient events:** sounds / hidden `nausea`+`darkness` / positional cues, more frequent and intense as sanity falls; zero at full. A single diegetic italic line prints on descent into a worse band (the only way an invisible stat announces itself).
- **Channel B — natural-spawn suppression:** `EntityEvents.checkSpawn` cancels tagged horror spawns with probability `(sanity/100)^gamma`. **Full sanity ≈ 0 % horror; zero sanity = the pack's tuned base rate, untouched.** It can only *reduce* below the authored base rate, never exceed it — so it cannot undo the pack's rarity work. Reaches the **biome-weighted** horrors (`HorrorSpawnRarity` set: Rake, TOWW, Wendigo, Whispering Spirits, Pale Hound; Weeping Angels); the **timer-summoned** ones (Mimic, Cave Dweller, Man From The Fog) bypass `checkSpawn` by design and keep their tuned cadence. (Corrected 2026-11-01: the Pale Hound is timer-summoned, as is CANTBREATHE, so `checkSpawn` doesn't reach either; see "2026-11-01 — Lore and config consistency pass".)

**Suppression / proximity target list:** `config/openloader/data/OldCivSanity/…/entity_types/horror_mobs.json` (modded ids use `required:false`). **All tunable** via one `CONFIG` object at the top of `oc_sanity.js` (`spawn.enabled=false` keeps ambient-only; `spawn.gamma` sets how hard high sanity holds horror back; `debug=true` enables gated/sampled tracing).

**Command:** `/sanity` (self) and `/sanity <player>` (op-gated, permission 2) print `sanity NN.N/100 [Band]` — the intended in-game way to check a level.

**Logging (docs §15):** `[OldCiv/Sanity]` prefix; load-time confirmation of channels + config; **log-once** failure suppression (the direct antidote to the 167×-error pattern) with **graceful per-accessor degradation** (a broken `getLight`/`isSleeping`/`checkSpawn` disables only its own sub-feature and logs one WARN). Marked `[verify live]` in the spec: `player.block.getLight()`, `isSleeping()`, `EntityEvents.checkSpawn` cancelability, `PlayerEvents.respawned`, and the `commandRegistry` arg API — each degrades safely if the accessor differs on the installed KubeJS build.

**Scope/apply:** server + client + startup scripts (active on world load / `/reload`, restart for the client tooltip); datapack global. All 6 touched JS files pass `node --check`; all new JSON validated. *Revert:* delete `oc_sanity.js`, the two clarity items from `oc_items.js`/`oc_creative.js`/`oc_tooltips.js`/`oc_loot.js`, the `OC_NON_LORE` guard in `oc_interactions.js`, the two model JSONs, and `config/openloader/data/OldCivSanity/`. Leftover `oc_sanity*` persistentData keys are inert.

---

### TerraFirmaCraft — hunger back to (essentially) vanilla (2026-07-12)
> Superseded 2026-11-01 by "2026-11-02 — Mod set changes, 2026-11-01 to 2026-11-02" (TerraFirmaCraft disabled; its configs deleted on 2026-11-07). `kubejs/server_scripts/tfc_vanilla_food.js` stays in place and writes `tfc:` data that nothing reads ("2026-11-07 — Repository cleanup: personal files purged from history, orphaned configs removed").

**Goal:** foods from any mod restore their **vanilla** hunger/saturation; hunger drains like vanilla; flat 20 max health. Cause: modded foods restored nothing.

**Root cause of "food does nothing" (server log, 20:16):** `TagLoader ERROR — Couldn't load tag tfcmoddedfood:nourishing as it is missing following references: #forge:foods`. No mod in the pack defines `#forge:foods`, and in 1.20.1 one missing required reference voids the **whole tag** → the `TFCModdedFood` catchall matched zero items → TFC's `TFCFoodData.eat()` (which returns early for items without a TFC food def) restored nothing for every non-TFC food. The shim was broken since creation.

| What | Where | Old | New | Why |
|---|---|---|---|---|
| `TFCModdedFood` pack | `config/openloader/data/` | active (broken) | **retired** → `data-disabled/` | Dead tag; superseded below |
| Per-item food defs | `kubejs/server_scripts/tfc_vanilla_food.js` | — | **new** | Generates a TFC food def for **every edible item** in the registry (skips `tfc:` + the 2 minecraft items TFC defines): `hunger = getNutrition()`, `saturation = nutrition × satModifier × 2`. TFC applies defs as `delegate.eat(hunger, sat/(2×hunger))` (verified in 3.2.23 bytecode) → **exact vanilla restore values**, no tags to break |
| `passiveExhaustionMultiplier` | `tfc-server.toml` | 0.5 | **0.0** | Vanilla has no passive hunger drain — drain comes from activity only |
| `naturalRegenerationModifier` | `tfc-server.toml` | 1.0 | **3.0** | TFC regen (0.2 HP/s base, 3× when hunger>80%) → 0.6–1.8 HP/s ≈ vanilla pace. (TFC forces `naturalRegeneration=false` and replaces regen; this is the lever) |
| `nutritionMin/Default/MaxHealthModifier` | `tfc-server.toml` | 0.2 / 0.85 / 3.0 | **1.0 / 1.0 / 1.0** | Flat vanilla 20 max HP — nutrition no longer scales health (generated defs carry 0 nutrients, so this also prevents nutrition-decay health loss) |

**Known residual (hardcoded, no config):** TFC's `TFCFoodData.addExhaustion()` scales all vanilla activity exhaustion by **0.4×** — hunger drains from activity at ~40% vanilla rate. Optional: raise `passiveExhaustionMultiplier` (e.g. 0.5–1.0) to add pressure back.

**Kept:** `TFCFoodHunger` tier pack (TFC's own foods at 5/6/8 hunger — vanilla-scaled), `thirstModifier1 = 0` (no thirst), `foodDecayModifier = 0` (no rot — vanilla has none).

**Scope/apply:** TOML edited in the client instance's `defaultconfigs/`, both `saves/*/serverconfig/` and the July server's `defaultconfigs/` (that server had no world at edit time — its next world seeds from defaults). The script was synced between the client instance and the July server. Active on next boot; `/reload` refreshes the generated defs on a running server (TOML needs world load). *Revert:* move `TFCModdedFood` back from `data-disabled/`, delete the kubejs script, restore the five TOML values.

---

---

## 2026-11

### 2026-11-01 — Mod-update review: about 80 mods updated, 13 jars removed

**Scope:** config follow-ups to the CurseForge update of 2026-11-01, three rollbacks, and corrections to the Baseline. Full write-up and evidence: `docs/UPDATE_REVIEW_2026-11-01.md`.
**Applies to:** the client instance. The July server was not changed in this pass.
**Status:** client instance (2026-11-01). Server-side configs were carried into Server v2 at its build (2026-11-02), from which the VM was set up. Exceptions are noted per sub-change.

#### Corrections to the Baseline

- **Day Length (`DayLength30`) never worked.** Every log since July shows `Failed to load function spooky_daylen:tick` (TFC replaces `/time add <n>` with its own syntax, so the function fails to compile), followed by `Couldn't load tag minecraft:tick … (from data/DayLength30)`. A tag with a missing reference is dropped whole, so `wa_buff:tick` (WeepingAngelsBuff) never ran either. The day length in effect came from Simple Day Length Extender: 2.0×/2.0× plus TFC auto-adjust, about a 60-minute day. Fix: "Day length: DayLength30 retired, Simple Day Length Extender 1.5×".
- **TerraFirmaCraft world generation was never active.** All nine worlds on disk (three in `saves/`, six in `simplebackups/`) generate `minecraft:overworld` with `minecraft:noise` and the vanilla/Terralith multi-noise biome source. Their region data is vanilla stone/deepslate, Terralith cave biomes and Mekanism/IE/Create ores. TFC's `tfc:overworld` preset was never selected. TerraFirmaCraft was disabled on the evening of 2026-11-01: "2026-11-02 — Mod set changes, 2026-11-01 to 2026-11-02".
- **The Forgotten** (Baseline) is historical: the jar was removed on 2026-11-01 (it was already disabled per `SERVER_MODS.md`). The remaining surface-monster suppressor was TFC's `enableVanillaMonstersOnSurface = false` (set to true in "TerraFirmaCraft 3.2.23 → 3.2.28").

#### Day length: DayLength30 retired, Simple Day Length Extender 1.5×

- **Change:** the `DayLength30` datapack moved to `config/openloader/data-disabled/DayLength30/`. `config/simpledaylengthextender-common.toml`: `dayLengthMultiplier` 2.0 → **1.5**, `nightLengthMultiplier` 2.0 → **1.5**, `autoCalendarAdjustments` → **false**.
- **Reason:** `DayLength30` never loaded (see "Corrections to the Baseline"); the effective day was Simple Day Length Extender's, about 60 minutes.
- **Mechanism:** a day is 36000 real ticks = **30 min**. Sleeping skips the night. With the broken pack gone, the Weeping Angels buff tick (`wa_buff:tick`) loads again.

#### MineColonies 1.1.1231 → 1.1.1301: raid keys and research

> Superseded 2026-11-04 by "2026-11-04 — MineColonies: server config replaced" (`raidDifficulty` 8 → 5; the 1231-only keys dropped).

- **Change:**
  - `defaultconfigs/minecolonies-server.toml`: the new 1301 raid keys added with the documented values (`enablecolonyraids = true`, `raidDifficulty = 8`, `maxRaiders = 80`, `raidersbreakblocks = true`, `raidersbreakdoors = true`, `pathNodeLimitMultiplier = 1.0`), alongside the old keys.
  - The local world `saves/New World (2)`: `raidDifficulty` 5 → **8**.
  - `MineColoniesSlowResearch`: 1301's 11 new mounted-combat/barding researches scaled with the documented rule (×2 tiers 1–2, ×3 tiers 3–4, ×4 tiers 5–6, cap 1024). The pack has 212 files.
- **Reason:** 1301 renamed the raid keys, and the new world seeded `raidDifficulty = 5` (the mod default), silently dropping the documented 8.
- **Mechanism:** each version discards the keys it doesn't know, so the one file seeds either server version. `pathfindingmaxthreadcount` no longer exists in 1301.
- **Verification:** all 201 existing research overrides still match jar paths, and the jar's versions of them are byte-identical to 1231's.
- **Status:** Server v2's world seeded `raidDifficulty 8` (2026-11-02). The VM world kept these values until the 2026-11-04 file was uploaded on 2026-11-08, live at the next VM restart.

#### Weeping Angels 46.1.5 → 46.1.6: new biomes off, snow angels off

- **Change:**
  - `config/weeping_angels_spawns.json`: the 9 newly enabled entries set to `canSpawnHere = false`.
  - `config/weeping_angels-common.toml` `[spawns]`: `snow_angels` true → **false**, `max_nearby` 6 → **1**.
- **Reason:** 46.1.6 regenerated the spawn file with 37 new biome entries; 9 arrived enabled at weight 8, in groups of 1–4 (8 Incendium nether biomes and `quark:glimmering_weald`). Snow angels: worldgen would bury angels in every snowy biome, bypassing per-biome rarity. `max_nearby` 1: solitary.
- **Unchanged:** the curated 16-biome set (weight 2, Deep Dark 3, solitary); `teleport_chance`, `interdimensional_teleporting` and `teleport_range`.

#### Server-Side Horror 4.2 → 5.1: random_starer off

- **Change:** `config/serversidehorror.json`: added `random_starer_enable = false`, `random_starer_chance = 8000000`. All existing keys preserved.
- **Reason:** 5.0 added the event `random_starer` (changelog: "new starer event which picks its players from players who joined the server"). Absent keys fall back to the mod defaults (enabled). The values match the Baseline's starer rarity and its no-fake-players design.

#### Door Knocker 1.2.0 → 1.3: trapdoor timings

- **Change:** `config/doorknocker-common.toml` `[doorknocker.trapdoors]`: 24000/48000 day, 12000/24000 night → **48000/96000 day, 24000/48000 night**.
- **Reason:** the new, independent trapdoor event arrived at the mod's frequent defaults; aligned to the Baseline's door timings.
- **Mechanism:** the mod dropped its sound keys.

#### Waystones: WaystonesGate covers the Create route

- **Change:** `WaystonesGate` gained `data/waystoneandcreate/recipes/compact_warp_stone.json`: one emerald replaced by `minecraft:ender_eye`.
- **Reason:** `waystoneandcreate` ships a Create compacting route (amethyst + 4 pearls + 4 emeralds → uncut warpstone → cutting → Warp Stone) that skipped the Eye of Ender gate. The gap predates the update.
- **Mechanism:** Create Waystones Recipes 2.0.1 also ships `data/waystones/recipes/warp_stone.json` (mechanical crafting, no eye) at the same path as the pack's override, so the pack's recipe wins.

#### Hardcore True Darkness: 6 rolled back to 4.0.0

- **Change:** `hardcore_true_darkness-6.jar` → **`hardcoretruedarkness-4.0.0.jar`** (the July version, from `Servers/spookydookie/mods/`), on the evening of 2026-11-01. The v6 jar is parked in `local/mods-rolled-back-2026-11-01/` (local only), not left as `.disabled`. No `defaultconfigs/hardcore_true_darkness-server.toml` (a v6-only file).
- **Reason:** v6 (new mod id `hardcore_true_darkness`) is a complete rewrite: server-enforced with a client/server protocol handshake (clients without the matching v6 jar are disconnected), gamma locked to 0, and a full-screen "final darkness" post-process pass with a blackout fallback. With Oculus + Hysteria that pass rendered the whole view pure black; with shaders off it was fine. It was not the cause of the void world (see "Chunk Sending: 4.1 rolled back to 2.8").
- **Status:** the July server runs 4.0.0. Server v2 was built with 4.0.0 (2026-11-02).
- **Maintenance:** pin 4.0.0 in the CurseForge app. The app re-enables `.disabled` jars it believes are installed; that is how Chunk Sending 4.1 came back at 21:30 on 2026-11-01.

#### Chunk Sending: 4.1 rolled back to 2.8 (the void world)

- **Change:** Chunk Sending 4.1 → **`chunksending-1.20.1-2.8.jar`** (July's known-good pairing with DimThreads 1.2.1). `config/chunksending.json` restored to the 2.8 format (`maxChunksPerTick = 5`); the 4.1-format file was kept as `chunksending.json.v41` (deleted 2026-11-07, "2026-11-07 — Repository cleanup: personal files purged from history, orphaned configs removed"). The 4.1 jar is parked in `local/mods-rolled-back-2026-11-01/` (local only).
- **Reason:** 4.1 never delivered chunks to the client in this pack (the "void" world, resolved on the evening of 2026-11-01). F3 showed 0 client chunks against thousands server-side. With `debugLogging` on, its per-tick line claimed "Sent: 15" while its own queue only grew (426 → 1487 in 76 s), and the client logged no drops.
- **Mechanism:** 4.1's rewritten send path runs on the player tick, which under DimThreads is the `dimthread_server_overworld` thread.
- **Verification:** restoring 2.8 fixed it at once (queue 392 → 0).
- **Status:** the July server runs 2.8. Server v2 was built with 2.8 (2026-11-02).
- **Maintenance:** pin 2.8 in the CurseForge app with "Change version". A renamed `.disabled` jar is not a pin: the app re-enabled 4.1 once, at 21:30 on 2026-11-01, and the void returned.

#### Farsight: 5.8 rolled back to 5.1

- **Change:** Farsight 5.8 → **5.1**. `config/farsight.json` back to the July values (`enableChunkManager = true`). The 5.8 jar is parked in `local/mods-rolled-back-2026-11-01/` (local only).
- **Reason:** 5.8 is a rewrite of the client chunk path (a new `ClientChunkHandler`, a new cancellable HEAD hook on `ClientPacketListener.updateLevelChunk`, an emptied Embeddium compat class). It was not the cause of the void: rolling it back changed nothing on its own. Design: Farsight stays on the known-good 5.1.
- **Status:** the July server runs 5.1. Server v2 was built with 5.1 (2026-11-02).
- **Maintenance:** pin 5.1 in the CurseForge app.
- **Related:** Farsight is inert while Immersive Portals is installed (finding of 2026-11-03, in "2026-11-02 — Immersive Portals × DimThreads: compat shim"). The 5.1 pin is still needed for the Chunk Sending 2.8 stack.

#### TerraFirmaCraft 3.2.23 → 3.2.28

> Superseded 2026-11-01 by "2026-11-02 — Mod set changes, 2026-11-01 to 2026-11-02" (TerraFirmaCraft disabled); these files were deleted on 2026-11-07 by "2026-11-07 — Repository cleanup: personal files purged from history, orphaned configs removed".

- **Change:**
  - `config/tfc-client.toml`: `showGuideBookTabInInventory`, `showGuideBookLinksAlways`, `showHoeOverlaysInInfoMods` → **false**.
  - New resource pack `TFCHideInventoryTabs` (`config/openloader/resources/`): blanks the tab strip in `tfc:textures/gui/icons.png`. The buttons still exist, invisibly.
  - `tfc-server.toml` (the client instance's `defaultconfigs/` and the local world `saves/New World (2)`; no server changed): `enableNetherPortals` false → **true**; `enableVanillaMonstersOnSurface` false → **true**; `enableInfestations` true → **false**; `enableOverburdening` true → **false**.
- **Reason:**
  - Nether portals: the Waystones Eye-of-Ender gate, the Occultism ritual costs and three installed nether worldgen mods all assume a reachable nether.
  - Surface monsters: TFC was silently denying every surface vanilla monster spawn, which contradicts "a world that fights back".
  - Overburdening: a direct toggle for what `TFCLightItems` works around.
  - Design: TFC's inventory tabs, guide-book tab and links, and hoe overlays are hidden; infestations are off.
- **Mechanism:** the Calendar/Nutrition/Climate inventory tabs have no config toggle. Source: `ClientForgeEventHandler.onScreenOpen`, TFC 3.2.28.
- **Verification:** the data overrides `TFCFoodHunger` (146), `TFCLightItems` (58) and `TFCVanillaCraftingTable` all still matched; no `item_sizes`/`food_items` file was added, removed or changed in 3.2.28. `tfc_vanilla_food.js` generated 628 definitions cleanly.

#### KubeJS: oc_sanity.js debug off

- **Change:** `kubejs/server_scripts/oc_sanity.js` `CONFIG.debug` true → **false**.
- **Reason:** left on after the "Sanity fix" commits; it logged DIAG lines every 2 s per player.
- **Verification:** the live log confirmed both channels load and evaluate correctly.

#### FTB Quests: Warmth quest removed

- **Change:** the undocumented "Warmth" quest removed from the Broken World chapter (`broken_world.snbt`). Backup: `broken_world.snbt.bak` (local only; `*.bak` is git-ignored).
- **Reason:** its icon and both tasks were `cold_sweat:*` items, and Cold Sweat was removed (2026-07).
- **Verification:** every other quest, reward and icon ID in the eight pack chapters resolves against the mod set. The WDA chapter is the `wda_questline` mod's own content.

#### modlist regenerated

`modlist.md` regenerated with `tools/generate_modlist.py`: 286 enabled, 0 disabled. Uncategorised: cozyapartments, lunaracolonies, hardcore_true_darkness, nochatrestrictions, sword_and_kingdom_schematic.

#### Reviewed, no change

Smooth Chunk 4.4; Sophisticated Backpacks/Storage/Core; Storage Drawers 12.15 (new Push/Pull upgrades); The Obsessed 1.5.2 (tuned values intact; the new structure-building and horse-stealing left on); JEI 15.56; Collective and Double Doors (configs moved to `config/serilum/`); Blood Magic 3.3.8; AmbientSounds 6.3.11 (`QuietAmbience` paths unchanged); AE2 15.4.12; Ad Astra 1.15.21; the Extreme Reactors, Mekanism, Occultism and SecurityCraft overrides (all 17 OpenLoader packs checked programmatically against the updated jars).

### 2026-11-01 — Lore and config consistency pass

**Scope:** every client config re-checked at 22:00 against the documented rules (`docs/CONFIG_CHANGES.md`, `README.md`, `docs/thelore.md`, `docs/PACK_DIRECTION_REVIEW_2026-07-08.md`) and against the July server's copies of `config/` and `defaultconfigs/`. Every July-documented value was re-verified in place; the only textual drift is The Obsessed 1.5.2 and The Man From The Fog writing `2000.0`-style floats (same values). The sub-changes are configs the docs had never covered, or documented intents that were not in effect.
**Applies to:** the client instance; Server v2 from its build (2026-11-02).
**Status:** client instance (2026-11-01). Per sub-change for the VM.

#### Corrections to the Baseline

- **The Pale Hound is a timer-summoned stalker, not a biome-weighted spawn.** It generates `pale_hound-server.toml` per world (the same `[Timers]` / `["Spawn Conditions"]` layout as Cave Dweller) and summons itself every **800–1200 s** (30 % chance of a 1400 s cooldown), up to **3** at once. Its mod default is `override_biome_datapack_config = true` with `surface_biomes_is_whitelist = false`, so the config's 40-biome vanilla **blacklist** applied and the `pale_hound_surface_biomes` tag that `HorrorSpawnRarity` overrides was **ignored**: it could spawn in every Terralith and modded biome. The new-mods pass note "code-weighted — no config" is wrong. Fix: "The Pale Hound: hour-plus timer, five cold or dark biomes".
- **CANTBREATHE** (`cant_breathe-1.20.1-1.0.2`, in the horror suite since July) had never been reviewed. It is the same timer family: every **850–1500 s**, any overworld biome, surface allowed, breaks doors in 1 s, spots players from 1000 blocks. Fix: "CANTBREATHE: hour-plus timer".
- `docs/sanity_mechanic.md` §11 listed the Pale Hound as biome-weighted (reachable by the sanity spawn-suppression channel). It belongs in the timer-summoned row with the Mimic, Cave Dweller and The Man From The Fog, as does CANTBREATHE. Corrected there.
- TFC's `enableVanillaMonstersOnSurface` check is tag-based (`tfc:vanilla_monsters`), so it never suppressed the modded horror spawns; their July weights were live all along.

#### The Pale Hound: hour-plus timer, five cold or dark biomes

- **Change:** new `defaultconfigs/pale_hound-server.toml` (a per-world config, seeded into new worlds), also applied to the local world `saves/New World (2)`:

  | Setting | Before (mod default) | After |
  |---|---|---|
  | `can_spawn_min` / `can_spawn_max` | 800 / 1200 | **3600 / 7200** |
  | `can_spawn_cooldown_chance` / `can_spawn_cooldown` | 0.3 / 1400 | **0.85 / 3600** |
  | `spawn_chance_per_tick` | 0.005 | **0.0025** |
  | `maximum_amount` | 3 | **1** |
  | `surface_biomes_is_whitelist` | false | **true** |
  | `surface_biomes` | 40-biome blacklist | **`deep_dark`, `dark_forest`, `snowy_taiga`, `grove`, `frozen_peaks`** |

  `override_biome_datapack_config` left `true`: the config is the lever, and the datapack tag stays as a fallback if it is ever flipped.
- **Reason:** hour-plus gaps, solitary, and only in the documented remote cold or dark biomes: the "rare > frequent" rule the rest of the suite follows. Aligned to the Mimic / Cave Dweller cadence and to the biome set the July pass intended.
- **Status:** live on the VM: the VM world's `pale_hound-server.toml` matches `defaultconfigs/` (checked 2026-11-07).
- **Related:** the timer note under "CANTBREATHE: hour-plus timer" applies to the same values.

#### CANTBREATHE: hour-plus timer

- **Change:** new `defaultconfigs/cant_breathe-server.toml`, also applied to the local world `saves/New World (2)`:

  | Setting | Before (mod default) | After |
  |---|---|---|
  | `can_spawn_min` / `can_spawn_max` | 850 / 1500 | **3600 / 7200** |
  | `can_spawn_cooldown_chance` / `can_spawn_cooldown` | 0.4 / 1000 | **0.85 / 3600** |
  | `spawn_chance_per_tick` | 0.005 | **0.0025** |

  `maximum_amount` was already 1. Biomes (any), door-breaking and the 1000-block spotting range are unchanged.
- **Reason:** the pack rule is *rarer*, not weaker ("do not make the horrors stronger" / weaker, `PACK_DIRECTION_REVIEW_2026-07-08.md`).
- **Status:** live on the VM: the VM world's `cant_breathe-server.toml` matches `defaultconfigs/` (checked 2026-11-07).
- **Related:** `docs/HORROR_REVIEW_2026-11-06.md` (correction): the timer-summoned stalkers' timer is mostly exactly 1 h, because the cooldown chance replaces the range, and their noise plays at the victim, carrying about 32 blocks. CANTBREATHE becomes sanity-bound in "2026-11-08 — spooky_tick_fixes 1.5.0: The Obsessed and CANTBREATHE sanity-bound" (built, not installed).

#### John Mod Reborn and Creeping Woods: 19 HorrorSpawnRarity overrides

- **Change:** 19 new same-path overrides in `config/openloader/data/HorrorSpawnRarity/data/{john_mod_reborn,creepingwoods}/forge/biome_modifier/` (the per-file weights are the tuning point):
  - **John Mod Reborn** (`forge:any`: every biome, every dimension): `john` 6 → **2** (groups 1–2 → 1), `baby_john` 9 → **2** (packs of 3–9 → **1–3**), `baby_john_pack_lead` 9 → **2**. `the_hunted` was already 1 and is untouched.
  - **Creeping Woods** (all forest and taiga biomes): 17 apparition/stalker spawn entries at weights **10–130** (combined about 430, on a par with the whole vanilla monster table, so those forests were mostly Creeping Woods). The 16 above the benchmark → **2**, groups 1–1 (`quick_light` was 130, 1–3; `human_1` was already 2). This includes the invisible `ambient_sound` emitter (15 → 2), consistent with the AmbientSounds "silence matters" pass.
- **Reason:** the documented horror benchmark: weight 2, solitary; "a still forest is scary".
- **Mechanism:** neither mod has a config. Both spawn through `forge:add_spawns` biome modifiers, so the same-path override precedent (Rake, TOWW, Wendigo, Whispering Spirits) applies.
- **Applies to:** both; from the next world load; independent of worldgen.

#### Xaero's Minimap: radar off

- **Change:** `config/xaero/minimap/profiles/default.cfg` `display_radar` true → **false**. The minimap, waypoints and cave mode are left at the profile's values (`display_minimap` false in the profile as of 2026-11-01).
- **Reason:** Xaero's Minimap arrived with the 2026-11-01 update, alongside the world map. The July decision was "world-map only, no live minimap/radar"; the entity radar is the part that breaks the horror layer (every stalker is a dot before a player ever sees it).
- **Applies to:** client-side.

#### Jade: horror entities not identified

- **Change:** `config/jade/hide-entities.json`: added the 113 entity ids of the horror suite (every `entity.<mod>.*` key from the mods' own lang files: CANTBREATHE, Pale Hound, The Mimicer, Cave Dweller, Man From The Fog, Rake, The One Who Watches, Whispering Spirits, Antlers, Weeping Angels, John Mod Reborn, Creeping Woods, The Obsessed). Looking at one shows **no** name, health or armour overlay.
- **Reason:** `thelore.md`: "Researchers have tried to classify them … Nobody has found an answer"; a HUD line reading `Cave Dweller 40/40` is the opposite. Born in Chaos and Foes (the hostile ecology, "The Altered") stay identified.
- **Applies to:** client-side; from the next client start.

#### Nature's Compass: Alex's Caves biomes blacklisted

- **Change:** `config/naturescompass-common.toml` `biomeBlacklist` `[]` → **the six Alex's Caves biomes** (`abyssal_chasm`, `candy_cavity`, `forlorn_hollows`, `magnetic_caves`, `primordial_caves`, `toxic_caves`).
- **Reason:** the Alex's Caves pass made cave biomes about 4× rarer and 600–975 blocks from spawn so that finding one is "something impossible"; a compass with a 2500-block search radius pointed straight at them. Explorer's Compass is unchanged (Proposal 4).

#### Quark: Totem of Holding off

- **Change:** `config/quark-common.toml` `[oddities] "Totem Of Holding"` true → **false**.
- **Reason:** Corpse (`corpse-server.toml` defaults: the inventory is kept in the body, anyone may loot it, and it never despawns while it holds items) is the pack's death system and the more thematic one. Two drop-capturing systems on the same death event means undefined ordering and, at best, a confusing empty totem.

#### Serene Seasons: day_duration 24000 → 36000

- **Change:** `config/sereneseasons/seasons.toml` `day_duration` 24000 → **36000**. Everything else at the mod defaults. Reviewed and kept at default:
  - `sub_season_duration = 8` (a 96-day year, about 48 h of 30-minute days);
  - winters at `biome_temp_adjustment −0.8` with `generate_snow_ice = true` (snow reaches temperate biomes: "the winters were never this harsh", `PACK_DIRECTION_REVIEW_2026-07-08.md`);
  - `fertility.toml` `seasonal_crops = true` / `out_of_season_crop_behavior = 0`: out-of-season crops grow *slowly* rather than fail, which is survival pressure that doesn't break MineColonies farms, whose `foodmodifier 1.5` already makes feeding real work;
  - overworld only.
- **Reason:** Serene Seasons was re-added on 2026-11-01 when TFC was removed, and is the pack's only climate/seasons layer (README: "harsh biomes and weather bite"). Its season clock counts real server ticks, and the key's own comment says it is "intended to be used in conjunction with another mod which adjusts the actual length of a Minecraft day". Simple Day Length Extender makes a day 36000 real ticks, so at 24000 each sub-season would have lasted 5⅓ game days instead of the 8 configured.
- **Applies to:** both; seasons run server-side.
- **Status:** the July server has no Serene Seasons jar. Server v2 has included it since its build (2026-11-02), and the VM has it (`docs/SERVER_MODS.md`).

#### Reviewed, no change (rationale recorded)

- **Corpse** (`corpse-server.toml` defaults): the body holds the inventory; `only_owner = false` (a corpse is lootable evidence, fitting "the world documents itself"); skeleton after 72000 ticks; never force-despawns. Kept.
- **Comforts** (`comforts-server.toml` defaults): sleeping bags night-only, hammocks day-only, no re-sleep restriction. Kept as the July QoL choice; a stricter option is Proposal 5.
- **Fragile Torches** (`chancetobreak 0.01`; players, villagers and animals "gentle"): mobs knock torches over, players don't. Fits "darkness fights back"; kept.
- **Sounds of the Forest** (`SOTF.toml`, `cooldown 18000`): at most one forest sound per 15 min; kept (silence matters).
- **Void Fog** (fog from `min height + 12`, 30-block distance) and **Cave Dust** (Y −64…64): client atmosphere; kept.
- **Sodium Dynamic Lights** (`self = true`, entities off): a held torch lights the player's surroundings. It softens Hardcore True Darkness, but it is a July-era accessibility choice; kept. Optional: `self = false` for the strict experience.
- **Artifacts** (`artifactRarity 1.0`, campsites 4 attempts per chunk, mimic chance 0.3): see Proposal 2 (night-vision goggles).
- **Explorer's Compass** (`maxRadius 10000`, no XP cost): see Proposal 4.
- **Iron's Spellbooks**: `irons_spellbooks-server.toml` at defaults (`spellGriefing = false`, `manaSpawnPercent 0`), no per-spell overrides generated. See Proposal 3 (Recall / Portal against the no-cross-dimension-teleport rule).
- **Ars Nouveau**: `enableWarpPortals = true`; warp scrolls uncosted. See Proposal 3.
- **Quark**, other modules as shipped: Monster Box (cave monster caches, vanilla-flavoured dungeon danger), Forgotten / Wraith (dark-only skeleton variants), Pipes / Crates / Magnets (early logistics the AE2 gating assumes exist), the Compass and Clock nerfs on (they don't work until crafted, which fits "minimal hand-holding"), Pathfinder Maps on (a paid, villager-sourced way to find biomes, the intended alternative to the compass).
- **Born in Chaos** (40+ `forge:any` hostiles, weights 2–30, some in packs) and **Foes** (regional hostiles, weights 1–80): the hostile ecology ("The Altered"), not the psychological layer; no config; left at the mod weights. These are what "a world that fights back" means in practice. Recommendation: if nights read as a swarm rather than a threat, apply the `HorrorSpawnRarity` override pattern to them too.
- **Horror Element Mod**: five worldgen body features (impaled soldiers and knights, a scarecrow farmer, a desert marine head, …) placed as 3–4 random-height attempts per chunk with **no** rarity filter (Antlers' bodies had `chance 17`, raised to 68). Baked in at worldgen; no in-game density reading as of 2026-11-01. Recommendation: if they read as clutter, override `worldgen/placed_feature/*.json` with a `rarity_filter`, exactly as the Antlers fix.
- **Loot Integrations** (`moddedItemWeight 3`): modded loot 3× likelier in integrated chests; kept (`oc_loot.js` carries the lore layer).
- **Update-generated diffs against the server copies with no gameplay meaning:** Entity Culling (`configVersion 9`, new `safeMode`), Not Enough Animations, Weeping Angels client (`donator_lookup`), Sophisticated (stack-conversion recipes), Storage Drawers (Push/Pull upgrades), Smooth Chunk and Chunk Sending (comment format), Sound Physics (Macaw's and MineColonies sound rates), JEI, Oculus, Spark.
- **Crash Assistant** `send_uploaded_logs_data_to_kostromdan_dev = true`: it only acts when the upload button is pressed; left on. `config/crash_assistant/` has been git-ignored and purged from the repository since 2026-11-07 ("2026-11-07 — Repository cleanup: personal files purged from history, orphaned configs removed").

#### Proposals (2026-11-01)

1. **Singleplayer view and simulation distance are 64 / 64** (`options.txt`, a local client setting not in the repository; the log shows `Changing view distance to 64, from 10` and `simulation distance to 64`). The integrated server therefore keeps about 16,600 chunks loaded and ticking (the "≈5000 and rising" F3 reading). Every horror timer, spawn cap and Serene Seasons snow pass scales with that area, and it is why the chunk-send backlog was so deep. The July server runs 10 / 10. Recommendation: 12–16 render (Farsight fills the horizon beyond the server distance) and 8–10 simulation. **Status:** proposed.
2. **Artifacts: Night Vision Goggles** contradict "darkness is genuinely dark" and are ordinary chest and campsite loot at `artifactRarity 1.0`. Lever: the Artifacts gamerules (`artifacts.nightVisionGoggles.enabled` / `.strength`) set from a `minecraft:load` function datapack (the DayLength30 pattern), plus `artifactRarity` 1.0 → 2–3. **Status:** proposed.
3. **Iron's Spellbooks Recall / Portal** and **Ars Nouveau warp scrolls / warp portals** are uncosted, unbounded, cross-dimension teleports: the same conflict the Mahou pass resolved (`EQUIVALENT_DISPLACEMENT_*`). Levers: Iron's per-spell config (`/ironsSpellbooks config`, then `enabled = false` or a mana/cooldown increase); `enableWarpPortals = false` plus a recipe override for the warp scroll. **Status:** proposed (`enableWarpPortals = true` as of 2026-11-08). Related: Waystone warping costs no XP on the client instance since 2026-11-08 (VM: not applied; "2026-11-08 — Waystones: warping costs no XP").
4. **Compass XP cost:** `defaultXpLevels` in both compass configs (for example 3–5) mirrors "every teleport costs XP"; otherwise Explorer's Compass locates any structure within 10 km for free. **Status:** proposed (`defaultXpLevels = 0` in both as of 2026-11-08). Related: Waystone warping costs no XP on the client instance since 2026-11-08 (VM: not applied; "2026-11-08 — Waystones: warping costs no XP").
5. **Comforts:** `restrictSleeping = true` (one sleep per in-game day) or `sleepingBagBreakChance = 25`. Nights are the horror window, and a sleeping bag skips every one. **Status:** proposed (`restrictSleeping = false`, `sleepingBagBreakChance = 0` as of 2026-11-08).
6. **TFC leftovers** are inert but still load: `tfc_vanilla_food.js` (generates 629 food definitions for a mod that is absent), the five `TFC*` OpenLoader packs, `config/tfc-*.toml`, `defaultconfigs/tfc-server.toml`. Proposed: park them under `data-disabled/` / `resources-disabled/` and out of `kubejs/server_scripts/` once TFC is confirmed gone. The CurseForge app can re-enable the `.disabled` jars from "2026-11-02 — Mod set changes, 2026-11-01 to 2026-11-02" the way it re-enabled Chunk Sending 4.1; remove them from the profile or park them in `local/`. **Status:** done in part, 2026-11-07 ("2026-11-07 — Repository cleanup: personal files purged from history, orphaned configs removed"): the TFC configs and the `TFC*` OpenLoader packs (including `data-disabled/TFCModdedFood`) were deleted. `tfc_vanilla_food.js` was left in place and still runs. The TFC jars are still `.disabled` in `mods/` as of 2026-11-08 (`modlist.md`).

### 2026-11-02 — Born in Chaos: infected diamond ore removed

- **Change:** new OpenLoader datapack `config/openloader/data/BornInChaosNoInfectedOre/`. It overrides both `data/born_in_chaos_v1/forge/biome_modifier/infected_*_diamond_ore_feature_biome_modifier.json` with `{ "type": "forge:none" }`, the same no-op-modifier technique as `PerfFixes`.
- **Reason:** Design: Born in Chaos's infected diamond ores do not generate.
- **Mechanism:** Born in Chaos adds **Infected Diamond Ore** (stone, Y 0–16) and **Infected Deepslate Diamond Ore** (Y −128…−8), one vein attempt per chunk in every overworld biome, through two `forge:add_features` biome modifiers. The ore drops nothing (empty loot table); breaking it, or blowing it up, runs a procedure that spawns a **Deceived Hopes** mob, the only way that mob enters the world. Nothing else in the mod references the ores (no recipes, no tags). The block and item still exist (creative, JEI), so nothing breaks on load.
- **Applies to:** both (global datapack); newly generated chunks only. Veins already placed in explored terrain stay, and still spawn Deceived Hopes when mined.
- **Status:** client instance and Server v2 (2026-11-02; one of the 17 data packs at Server v2's first boot). On the VM: not separately recorded. The VM was deployed as Server v2 (SERVER_TICK_AUDIT_2026-11-06, as first written: "Server v2 on the Proxmox VM"), so the pack is expected there.
- **Revert:** delete the pack folder.

### 2026-11-02 — Immersive Portals × DimThreads: compat shim

> Superseded 2026-11-06 by "2026-11-06 — DimThreads disabled" (the shim only): DimThreads and `immptl_dimthread_compat-1.0.0.jar` are `.jar.disabled` on the client instance and the VM, because the shim's required mixin references DimThreads classes. Immersive Portals and its addons stay.

- **Change:**
  - Four jars added on 2026-11-02 at about 01:00: **Immersive Portals 3.0.7** (mod ids `immersive_portals`, `imm_ptl_core`, `q_misc_util`), **Immersive Portals: Full Immersion 1.0.20**, **Ars Two-Way Portals 1.3.4** and the **Make it Compatible** patch.
  - New pack-built **`immptl_dimthread_compat-1.0.0.jar`**: source and `build.py` in `tools/immptl_dimthread_compat/`; problem, fix, build, deploy and revert in its `README.md`. One `@Redirect` on the `isSameThread()` call in `ImmPtlChunkTickets.flushThrottling` also accepts the DimThreads worker that DimThreads records as the level's current main thread; any other thread is still refused. Built with the server's JDK 17 against the jars in the CurseForge install; a copy is kept in `local/` (local only). Its `mods.toml` depends on `imm_ptl_core` and `dimthread`.
  - `config/immersive_portals.json` `disabledWarnings` `[]` → **`["many_mods"]`**. This hides Immersive Portals' "you are using many mods" chat warning, whose in-chat dismiss command is rejected as an unknown command. The key is the one `IPModInfoChecking` checks. The online mod-info check and the update notification stay on.
  - Addon configs reviewed and kept at defaults. Full Immersion: mobs attack, target and pursue through portals (`true`; fits a world where things follow players), `portalChunkLoadingRadiusCap = 4` (raised to 6 in "2026-11-02 — Full Immersion: portal chunk-loading radius cap 4 → 6"), `portalDiscoveryRangeChunks = 8`, `spawnChunkRadius = 3`, `keepaliveTimeoutSeconds = 120`. `spawnChunkRadius = 3` shrinks the permanent spawn-chunk ticket from vanilla's radius 11, a performance win; if something at spawn stops ticking while nobody is near, this is the setting to check. Ars Two-Way Portals: Dominion Wand rotation, gravity portals and the Immersive Portals integration for Double-Sided Stable Warp Scrolls all on.
- **Reason:** with Immersive Portals and DimThreads together, the first world load wrote a **1.3 GB `latest.log` in under three minutes**: about 470,000 × `[dimthread_server_<dim>/ERROR]: Called in a non-server-main thread.`, each with a 14-line stack trace (419,000 from the overworld).
- **Mechanism:** cause and fix as in `tools/immptl_dimthread_compat/README.md`. Source: `javap` of the shipped jars (Immersive Portals 3.0.7, DimThreads 1.2.1). Beyond the README:
  - DimThreads' `tickWorlds` hands every level to a `dimthread_server_*` worker, and the server thread blocks in `awaitCompletion()` until all of them finish, so the main thread and the workers never overlap.
  - Immersive Portals' hook at the end of `DistanceManager.runAllUpdates` is gated on `enableImmPtlChunkLoading`. `isSameThread` compares against `MinecraftServer.serverThread`, which DimThreads does not (and cannot) swap; it swaps only the per-level and per-chunk-cache "main thread" fields.
  - It was not the "void" problem returning. Immersive Portals' own server-tick hook calls the same flush on the real server thread, which passes the check; New World (5) generated 11,841 chunks in that session, the normal rate. What was lost was the in-tick top-up flush (slower portal chunk loading); what was added was the log flood and about 2,700 stack-trace constructions per second at the 64-chunk simulation distance.
  - Upstream: DimThreads for 1.20.1 has no build newer than v1.2.1 (November 2024), and its tracker has had this exact error open since June 2025 (SrRapero720/dimthreads#47, no reply). Immersive Portals' repository was archived in April 2026. DimThreads has no per-dimension exclusion, and a thread count of 1 still runs on a worker. Immersive Portals detects DimThreads at startup but only logs "Dimensional Threading is present".
  - Safety: the flush touches only the distance manager and ticket queue of the level passed in, from inside that level's own update loop, on the thread already ticking that level; vanilla's chunk code there runs under the same assumption. The shim has the shape of DimThreads v1.2.1's own Tardis Refined fix. Residual: Immersive Portals' per-dimension `WeakHashMap` is read from several workers; its keys are live levels for the whole session, so it never mutates after warm-up. The one hang report on the DimThreads tracker (#46, NeoForge 1.21.1, after 8 h) was with portals set to vanilla and is a different path.
- **Rejected:** removing one of the two mods; `enableImmPtlChunkLoading = false` (stops the flood, but hands player chunk loading back to vanilla, and portals then never load their far side); `/gamerule dimthread_active false` per world (DimThreads inert where portals matter); filtering the log line (hides the symptom only).
- **Verification:** headless A/B on a throwaway dedicated server, 2026-11-02 01:33–01:42: a scratch folder with the 281 server-safe jars from `mods/`, the same world for both runs, 90 s of ticking after `Done`, no players.
  - Without the shim: 80,685 `Called in a non-server-main thread` lines (about 1,790 per dimension, one per tick in every dimension); 154 MB log.
  - With the shim: the mod's load line present; no mixin apply failure other than Better Chunk Loading's long-standing Canary clash; **0** such lines; 504 KB log; the remaining ERROR set identical to the control's.
  - No `Can't keep up` in either run.
  - In game (2026-11-02): portals load and render their far side with DimThreads active.
- **Applies to:** both. The shim is mandatory on client and server. Both addons and the patch jar are server-side too. The July server has none of the four jars and no shim; Server v2 has all of them from its build (2026-11-02).
- **Status:** Immersive Portals, its addons and the `many_mods` setting: client instance and Server v2 (2026-11-02); on the VM (`docs/SERVER_MODS.md`). Shim: `.jar.disabled` on the client instance and the VM since 2026-11-06.
- **Revert:** delete the shim jar from `mods/` (and `local/`). Nothing is persisted in worlds.
- **Related:** finding of 2026-11-03: Farsight is inert while Immersive Portals is installed. The client log says `Farsight not enabled for: ResourceKey[minecraft:dimension / minecraft:overworld] as other mods changed the chunk source to: class qouteall.imm_ptl.core.chunk_loading.ImmPtlClientChunkMap`. Both mods replace the client chunk cache and Immersive Portals wins, so Farsight's client-side chunk keeping does nothing, and the view range is limited by the server's `view-distance` (Server v2: 12) whatever the client slider says. Farsight has no server-side code. The 5.1 pin is still needed for the Chunk Sending 2.8 stack, so the jar stays. Simulation distance is what costs tick time; view distance mostly costs chunk loading, memory and bandwidth. Addressed by "2026-11-03 — Farsight × Immersive Portals: client chunk-keeping shim".

### 2026-11-02 — Full Immersion: portal chunk-loading radius cap 4 → 6

- **Change:** `config/immersive_portals_full_immersion-common.toml` `portalChunkLoadingRadiusCap` 4 → **6**, on the client instance and in `Servers/Server v2` (the server does the loading, so it must change there). `portalDiscoveryRangeChunks` stays 8.
- **Reason:** Design: a portal's far side loads farther: 169 chunks per active portal (was 81).
- **Mechanism:** the effective radius is the lower of this value and Immersive Portals' own `indirectLoadingRadiusCap` (8), so 8 is the ceiling unless that is raised too. The cost is remote chunk ticking, autosave and entity spawning per active portal.
- **Applies to:** both; the server's value governs loading. Applied at a client restart and a Server v2 restart.
- **Status:** client instance and Server v2 (2026-11-02). VM: not separately recorded; the VM was deployed as Server v2 (SERVER_TICK_AUDIT_2026-11-06), which had the change by 2026-11-03.
- **Maintenance:** watch server TPS with several portals in range.
- **Related:** finding of 2026-11-02: portal artifacts (the far side looking zoomed, blocks at a portal cut off) are caused by shaders (Oculus + Hysteria), not chunk loading. Turning TAA off in Hysteria reduced them but did not remove them all. The shader setting is per player and is not in the pack files.

### 2026-11-02 — Mod set changes, 2026-11-01 to 2026-11-02

- **Change:** the mod-set changes made in the CurseForge app from the evening of 2026-11-01 to 2026-11-02, compared with the committed `modlist.json` (commit "Config changes"). The config work for each is in its own entry.
  - **Disabled** (left in `mods/` as `.disabled`): **TerraFirmaCraft 3.2.28** and its dependants **Macaw's All-In-One for TFC** and **TFC: Carry On Storage**; **Ars Sophisticated Compatibility** and **Mekanism + Sophisticated Backpacks**.
  - **Added on the evening of 2026-11-01:** **Serene Seasons 9.1.0.3**; **Macaw's** Bridges, Doors, Fences and Walls, Furniture, Holidays, Lights and Lamps, Paths and Pavings, Roofs, Stairs and Balconies, Trapdoors, Windows (decoration, no balance surface); **Create: More Bridges 1.0.2** (decoration); **LumiLib 0.4.1** (library); **Sophisticated Backpacks: Realistic Crafting 1.1** (changes backpack recipes; not reviewed for balance). Xaero's Minimap came with the update itself (radar off: "2026-11-01 — Lore and config consistency pass").
  - **Added on 2026-11-02:** **Immersive Portals 3.0.7** (bundles Immersive Portals Core and QMiscUtil), **Immersive Portals: Full Immersion 1.0.20**, **Ars Nouveau: Two-Way Portals 1.3.4**, **Make it Compatible: Immersive Portals 1.1.0**, and the pack-built **`immptl_dimthread_compat` 1.0.0** ("2026-11-02 — Immersive Portals × DimThreads: compat shim").
  - **Pinned back to the July server's jars:** Chunk Sending 4.1 → **2.8**, Farsight 5.8 → **5.1**, Hardcore True Darkness 6 → **4.0.0**. The newer jars are parked in `local/mods-rolled-back-2026-11-01/` (local only).
  - **Effects of disabling TerraFirmaCraft:**
    - Every TerraFirmaCraft section of the Baseline (HUD bars, thirst and hunger, food hunger tiers, modded food, collapses, item size and weight, the vanilla crafting table) and the 2026-11-01 `tfc-server.toml` toggles became historical. The pieces that still loaded did nothing: `TFCFoodHunger`, `TFCLightItems`, `TFCVanillaCraftingTable` (data), `TFCNoSizeTooltip`, `TFCHideInventoryTabs` (resources), `kubejs/server_scripts/tfc_vanilla_food.js` (still generating about 629 food definitions for an absent mod), `config/tfc-*.toml`, `defaultconfigs/tfc-server.toml`.
    - Vanilla behaviour returns by default: hunger and natural regeneration, nether portals, bonemeal, wandering traders, pillager patrols, surface monster spawns, and `/time add`.
    - Worlds created while TFC was installed keep TFC's forced game rules in `level.dat`: `naturalRegeneration`, `doInsomnia`, `doTraderSpawning` and `doPatrolSpawning` are `false` there (`New World (2)`, `New World (3)`). In such a world, run `/gamerule <rule> true` for each. Worlds created after TFC was disabled were checked: `New World (4)` has all four `true`.
    - Serene Seasons is the climate/seasons layer ("2026-11-01 — Lore and config consistency pass").
- **Reason:** TerraFirmaCraft: the removal the update review recommended. TFC's worldgen was never active; only its mechanics were, and the pack was working around all of them. Ars Sophisticated Compatibility and Mekanism + Sophisticated Backpacks: disabled on 2026-11-01; reason not recorded. The pins: "2026-11-01 — Mod-update review: about 80 mods updated, 13 jars removed".
- **Applies to:** the client instance. Server v2 was built with this mod set (2026-11-02).
- **Status:** client instance (2026-11-02). The five disabled jars are still `.disabled` in `mods/` as of 2026-11-08 (`modlist.md`). The TFC configs and `TFC*` OpenLoader packs were deleted on 2026-11-07; `tfc_vanilla_food.js` was left in place ("2026-11-07 — Repository cleanup: personal files purged from history, orphaned configs removed").
- **Maintenance:** the CurseForge app re-enables `.disabled` jars it believes are installed (it did so to Chunk Sending 4.1). To make the removal stick, remove these five from the profile or move them to `local/`.
- **Related:** Proposal 6 in "2026-11-01 — Lore and config consistency pass".

### 2026-11-02 — Server v2: dedicated server built to match the client

> Superseded 2026-11-06 by "2026-11-06 — Server tick audit: phases 0 and 1" (the VM became the live server; Server v2 is a local test bed) and "2026-11-06 — Deployment tooling: VM sync, mod-list generators, shared build script" (`scripts/sync_server_mods.py` and `tools/generate_server_modlist.py` target the VM by default).

- **Change:** a new dedicated server, `Servers/Server v2`, built beside the July server from copies (nothing written to the July server), with a new world:
  - **Forge 47.4.10:** `libraries/`, `run.bat`, `run.sh` and the installer jar copied from the July server (same Forge version; Java 17 still pinned in the launch scripts).
  - **Mods:** `scripts/sync_server_mods.py --server "…/Server v2"`: the client's 303 active jars minus the 22 client-only ones = **281 jars**, including the pinned Chunk Sending 2.8, Farsight 5.1 and Hardcore True Darkness 4.0.0, Serene Seasons, the Immersive Portals set and the `immptl_dimthread_compat` shim. TFC and its dependants absent.
  - **Pack content from the client instance:** `config/` (all OpenLoader packs, FTB Quests, every mod config), `defaultconfigs/`, `kubejs/`, `blueprints/`. Left out: `*.bak` backups, `chunksending.json.v41`, and JEI's per-world client state.
  - **`server.properties`** from the July file, with three changes: `level-type` `tfc:overworld` → **`minecraft:normal`** (TFC's own default had been written there); `difficulty` `easy` → **`normal`** (every client world is Normal; Easy was the vanilla default); a fresh, empty `level-seed`. Otherwise as July: online mode, port 25565, view and simulation distance 10, no whitelist.
  - **`user_jvm_args.txt`:** the July memory (`-Xms6G -Xmx8G`). The custom `log4j2_server.xml` was dropped, because its only purpose was silencing TFC's calendar log spam; Forge's default logging applies.
  - `eula.txt` carried over from the July server (already accepted there).
  - **Tooling:** `scripts/sync_server_mods.py` gained `--server <folder>` and defaulted to `Servers/Server v2`, so a routine sync could not touch the July server. New `tools/generate_server_modlist.py` regenerates `docs/SERVER_MODS.md` from the server's actual `mods/` folder (same parser and categories as `tools/generate_modlist.py`); it is run after each sync.
- **Reason:** a dedicated server up to date with the client instance, with a new world. The July server (`Servers/spookydookie`: 264 mods, TFC-era, no world) is kept unchanged for reference.
- **Verification:** first boot, 2026-11-02 02:07–02:09, clean log:
  - `Done` after about 11 s of world load (about 60 s total start). The shim loaded with **0** "non-server-main thread" lines. OpenLoader injected **17/17** data packs. KubeJS: **3/3** startup and **7/7** server scripts, 0 errors. The sanity mechanic loaded with both channels on. No `Can't keep up`.
  - The world's generated `serverconfig/` carries the pack defaults: Pale Hound and CANTBREATHE 3600–7200 s, solitary; Mimic 3600–7200; Cave Dweller 7200–14400; MineColonies `raidDifficulty 8`; Mahou displacement cap 256; no TFC file.
  - `level.dat`: Normal difficulty, `minecraft:noise` generator, `naturalRegeneration`, `doInsomnia`, `doTraderSpawning` and `doPatrolSpawning` all **true**, DimThreads active at 4 threads. `doDaylightCycle` reads false only because Simple Day Length Extender holds time until the first player joins.
  - The remaining ERROR lines are the same mod-data noise the client logs on every launch.
- **Applies to:** Server v2. `defaultconfigs/` only seeds new worlds; an existing world's per-world values live in `world/serverconfig/`.
- **Status:** built 2026-11-02. The VM was set up from Server v2 (`docs/SERVER_TICK_AUDIT_2026-11-06.md`) and has been the live server since 2026-11-06. Since then Server v2 has been a local test bed, not live and out of step with the VM.
- **Related:** Server v2's `view-distance` went to 12 after the build (date not recorded) and to 16 on 2026-11-03 ("2026-11-03 — Farsight × Immersive Portals: client chunk-keeping shim").

### 2026-11-03 — Farsight × Immersive Portals: client chunk-keeping shim

> Superseded in part 2026-11-07 by "2026-11-07 — farsight_immptl_compat 1.0.1: held chunks follow the full render distance" (hold-radius cap 32 → 64).

- **Change:**
  - New client-only mod `farsight_immptl_compat-1.0.0.jar` (source and build in `tools/farsight_immptl_compat/`; jar in `mods/` and `local/`). It holds the server's chunk-unload packets for the player's own dimension while the chunk is within `min(render distance, 32) + 1` chunks, drops a hold when the server sends that chunk again, and releases holds from the client tick once they are 3 chunks past that radius or in a dimension the player has left. Portal views into other dimensions unload as before.
  - Kept off the server: `farsight_immptl_compat-` added to the client-only prefixes in `scripts/sync_server_mods.py` and to `tools/generate_server_modlist.py`; `docs/SERVER_MODS.md` regenerated (281 server jars, 23 client-only excluded).
  - `Servers/Server v2/server.properties`: `view-distance` 12 → **16**; `simulation-distance` stays 10. History: Server v2 was built on 2026-11-02 with the July value 10 and raised to 12 after the build (date not recorded).
- **Reason:** with Immersive Portals installed, Farsight's chunk keeping is off (see the "Farsight is inert" note in "2026-11-02 — Immersive Portals × DimThreads: compat shim"), so on a server the view ends at the server's `view-distance` however far the client slider goes. `view-distance` 16: Design: live terrain on Server v2 reaches further for every player.
- **Mechanism:** Farsight's radius hooks (`handleLogin` / `handleSetChunkCacheRadius` report `maxRenderDistance` 64) still work under Immersive Portals, so rendering is not capped. What is lost is its delayed unload, which lives in Farsight's own chunk manager. Immersive Portals installs `ImmPtlClientChunkMap` unconditionally and its renderer depends on it, so Farsight's manager cannot simply win. Immersive Portals' map already keeps chunks at any distance, and nothing else in Immersive Portals drops client chunks, so only the hold-and-release half is missing. Source: bytecode of Farsight 1.20.1-5.1 and Immersive Portals 3.0.7.
  - **Limits:** held chunks are snapshots. Block changes beyond the server's view distance (Server v2: 16; the VM: 8–12, set by Dynamic View from 2026-11-06) show only when the player comes back, and mobs there disappear because the server removes entities separately. Only terrain already received is kept; Farsight's disk preview stays off. Client memory is bounded by the 32-chunk cap; a client render distance of 32 matches it, and anything higher draws empty space past the held area.
  - **View distance 16** means 1,089 chunks around each player, up from 625. View distance mostly costs chunk loading, memory and bandwidth, not tick time.
- **Verification:** compiles against the Forge 1.20.1 SRG client jar and Immersive Portals 3.0.7; every injection target and shadowed field was checked against the real classes with `javap`; the sync dry run keeps the server at 281 jars. Not tested in game. The mixin config is `required: true`, so a bad target stops the client at startup with a clear error rather than failing silently. Expected log line on start: `[farsight_immptl_compat] loaded: Farsight's delayed chunk unload now runs on Immersive Portals' chunk map`.
- **Applies to:** the shim: the client instance only; players without the jar are unaffected. `view-distance`: Server v2, from its next start.
- **Status:** shim: client instance only (2026-11-03); replaced by 1.0.1 (2026-11-07). Players get it with the pack update. `view-distance` 16: Server v2 (2026-11-03); Server v2 is a local test bed, not live.
- **Maintenance:** after a view-distance change, check TPS with `/spark health`.
- **Revert:** delete the jar from `mods/` (nothing is saved in worlds). Set Server v2's `view-distance` back to 12.
- **Related:** `tools/farsight_immptl_compat/README.md` (details, limits and tuning flags).

### 2026-11-03 — Better Chunk Loading: smart post-processing off

- **Change:** `config/betterchunkloading.json`: `enableSmartPostProcessing` true → **false** on the client instance; this is the only path that makes the blocking call. Server v2: the client's `config/betterchunkloading.json` copied over (2026-11-03; the only difference was this line).
- **Reason:** a singleplayer Chunky pregeneration in "The Correction" instance (the same 303 mods) froze for good at 22.79% (01:04:33), with no error in the log before the hang.
- **Mechanism:** Cause: `enableSmartPostProcessing` (mixin `LevelChunkPostProcessMixin` on `LevelChunk.postProcessGeneration`) defers post-processing of new chunks whose neighbours are not loaded, and later fetches them with `Level.getChunk` from the server thread at the end of the tick. Under DimThreads that fetch was treated as off-thread and queued to the overworld's executor, which only its worker drains during a tick, so the server thread waited forever. Chunky makes such deferred chunks constant. Why the chunk cache did not count the server thread as its main thread at that moment is not determined; DimThreads swaps it to the worker during the level tick and back afterwards. The mod describes the feature as a slight loading-speed improvement. Source: two `jstack` dumps 33 s apart and ModernFix's integrated-server watchdog: the server thread parked in `ServerChunkCache.getChunkOffThread` under `com.betterchunkloading.event.EventHandler.applyToChunk`, called from `onServerTick`; the DimThreads overworld worker idle in its pool; Chunky waiting on its semaphore, so nothing could complete the queued chunk request. No Java-level deadlock (it is a `CompletableFuture.join`), no GC pressure (old gen 88%, 0 full GCs).
- **Verification:** a full client-to-Server v2 comparison (2026-11-03) found nothing else to carry: mods in sync (281), `defaultconfigs/`, `kubejs/` and `blueprints/` identical, portal radius 6 and `view-distance` 16 already applied. Deliberately not copied: `oculus.properties` (client shader state), `quark-common.toml` (the server's Quark strips its client-only `hotbar_changer` section), `config/chunky/tasks/` (a cancelled client pregen task), and the `*.bak` backup folders.
- **Applies to:** the client instance (from its next launch) and Server v2 (from its next start). "The Correction" instance kept `true`; it was deleted 2026-11-06.
- **Status:** client instance and Server v2 (2026-11-03). VM: not recorded.
- **Revert:** set `enableSmartPostProcessing` back to `true` (the mod default).
- **Related:** the hang needed DimThreads, which was disabled on 2026-11-06 ("2026-11-06 — DimThreads disabled").

### 2026-11-04 — MineColonies: server config replaced

- **Change:** `minecolonies-server.toml` replaced with a 1.1.1301-format file (the MineColonies version on both client and server), copied verbatim to the client instance's `defaultconfigs/`, to Server v2's `defaultconfigs/` and its world's `world/serverconfig/` (2026-11-04), and to the VM world's `world/serverconfig/minecolonies-server.toml` (2026-11-08). Value changes compared with the previous files:

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
  | `raidDifficulty` | 8 | **5** (the mod default) |

  All other values, including the guard multipliers, raid frequency and the other 1301 raid keys, are unchanged. The new file no longer contains the 1.1.1231-only keys (`dobarbariansspawn`, `barbarianhordedifficulty`, `maxBarbarianSize`, `dobarbariansbreakthroughwalls`, `shouldraiderbreakdoors`, `pathfindingdebugverbosity`, `pathfindingmaxthreadcount`). They only matter for a 1231 server, and none is in use.
- **Reason:** Design: larger colonies that stay loaded longer and sit further apart, workers who work in rain, the home teleport command allowed, and the mod-default raid difficulty (the values in the table).
- **Mechanism:** load note: with strictness 1 and a 40-minute load time, a large colony keeps all of its claimed chunks ticking for 40 minutes after its owner logs off. `defaultconfigs/` only seeds new worlds; an existing world keeps its own `world/serverconfig/` copy. Forge configs don't hot-reload on the VM.
- **Verification:** VM, 2026-11-07: `defaultconfigs/minecolonies-server.toml` was the new file, but the world's `world/serverconfig/minecolonies-server.toml` still had the old values for all 10 settings; the world's other per-world configs (`cant_breathe`, `cave_dweller`, `create`, `mahoutsukai`, `pale_hound`, `themimic_er`) matched `defaultconfigs/`. VM, 2026-11-08: the world's file differed from the new one in exactly the 10 settings; the new file was uploaded and read back identical.
- **Applies to:** new worlds (through `defaultconfigs/`), Server v2's world (from its next start) and the VM's world. Not changed: existing singleplayer worlds in `saves/` (each keeps its own `serverconfig/`), the July server, and "The Correction" instance (deleted 2026-11-06).
- **Status:** client instance `defaultconfigs/` and Server v2 (2026-11-04). VM: `defaultconfigs/` has the new file (checked 2026-11-07); the world's `world/serverconfig/` copy was uploaded 2026-11-08, after the 01:14 restart, so it is uploaded to the VM and live after the next restart (2026-11-08).
- **Maintenance:** if server tick time rises once colonies grow, check the colony load (load note) first in spark.
- **Revert:** VM: move the old file back from `files/server/deploy-backups/2026-11-08-minecolonies/world/serverconfig/` and restart. Client instance and Server v2: set the 10 values back to the Before column.
- **Related:** `raidDifficulty` 5 reverses the restoration to 8 in "2026-11-01 — Mod-update review: about 80 mods updated, 13 jars removed" (MineColonies). `docs/SERVER_TICK_AUDIT_2026-11-06.md` cites the load note in its MineColonies row.

### 2026-11-06 — The Man From The Fog: disabled

> Superseded 2026-11-06 by "2026-11-06 — The Man From The Fog jar disabled; second client instance deleted": the jar is `.jar.disabled` on the client instance and the VM, which also drops the structure, the job and the advert listed under Applies to.

- **Change:**
  - `config/man_config.toml` `enable_spawning` true → **false**. This stops the timer-summoned Man, and with it the chase, jumpscare and no-sleep effects.
  - `HorrorSpawnRarity/data/man/forge/biome_modifier/mftfhang_biome_modifier.json` → `{"type": "forge:none"}` (Forge's registered no-op modifier). The hanging variant spawned at weight 10 in every biome and is not covered by the config switch.
- **Reason:** Player feedback: far less surface horror ("above land in overworld is probably negligible"). The mod is surface-only by construction (its spawn timer only runs while a player is within 2 blocks of the surface), so it was switched off rather than tuned. Full reasoning and the other horror proposals: `docs/HORROR_REVIEW_2026-11-06.md`.
- **Applies to:** both. Not affected by the config switch: the cultist `man:house` structure, the Cultist villager job, and the once-per-world Ko-fi advert and cassette on first join (only removing the jar drops those). Hanging Men already spawned stay until `/kill @e[type=man:mftfhang]`. `HorrorQuietAdvancements` stays in place.
- **Status:** client instance and the VM (2026-11-06): `man_config.toml`, the `mftfhang` override and the `HorrorQuietAdvancements` pack uploaded and verified byte for byte (see "2026-11-06 — Server tick audit: phases 0 and 1"). Server v2: not applied (2026-11-06). "The Correction (Dev)" (singleplayer only): not applied; that instance was deleted on 2026-11-06. `config/man_config.toml` was deleted from the repository on 2026-11-07 ("2026-11-07 — Repository cleanup: personal files purged from history, orphaned configs removed").

### 2026-11-06 — The Man From The Fog: Feeling Watched advancement silenced

- **Change:** new OpenLoader datapack `HorrorQuietAdvancements` (`config/openloader/data/HorrorQuietAdvancements/`): `data/man/advancements/feelingwatched.json` overridden with the same `minecraft:impossible` criterion and **no `display` block**.
- **Reason:** Design: the Man's arrival is not announced. The shipped `man:feelingwatched` ("Feeling Watched", description "Man spawning first time") was its own advancement tab with `show_toast` and `announce_to_chat` true, so it told every player he had arrived.
- **Mechanism:** the advancement is still granted, because the mod's spawn code does not null-check it and deleting it could throw in the entity tick. Without a `display` block it shows no toast, no chat line and no advancement tab. Players who already have it keep it, invisibly. `man:kill` and the two enchantment advancements are left as they are. Source: The Man From The Fog 1.4 jar (read 2026-11-06).
- **Applies to:** both (global datapack, every world); takes effect on the next world load or `/reload`. Needed on every server that runs the mod.
- **Status:** uploaded to the VM on 2026-11-06 and verified byte for byte ("2026-11-06 — Server tick audit: phases 0 and 1"); live. Inert since 2026-11-06, when the jar was disabled on the client instance and the VM ("2026-11-06 — The Man From The Fog jar disabled; second client instance deleted"); the pack stays in place. Server v2: not applied (2026-11-06).
- **Revert:** delete `config/openloader/data/HorrorQuietAdvancements/`.
- **Related:** Baseline "The Man From The Fog" (what the mod does, read from the 1.4 jar). `HORROR_REVIEW_2026-11-06.md` keeps the pack in case the mod is turned back on.

### 2026-11-06 — Server tick audit: phases 0 and 1

- **Change:**
  - **Hosting:** the live server is the Proxmox VM (`files/server`), reached over FTP and deployed from this repository. `Servers/Server v2` is kept as a local test bed and is out of step with the VM. The VM mirrors the client instance's mods minus the 23 client-only jars (280 jars after this change).
  - **Phase 0, applied on the VM by the server operator:**
    - JVM: Aikar's G1 flag set added to `user_jvm_args.txt`. Memory kept at `-Xms8G -Xmx32G` by choice.
    - Host: CPU governor confirmed on `performance`.
    - `server.properties`: `sync-chunk-writes` true → **false**.
    - File ownership: all server files chown-ed to the VM's FTP user. `config/spark/tmp` had been root-owned, so spark could not unpack async-profiler (`AccessDeniedException`) and fell back to the slower Java sampler. The server process runs as root (recorded 2026-11-06 in "2026-11-06 — Let Me Despawn, Almanac and Dynamic View added"), so files it creates afterwards are root-owned again.
  - **Phase 1 (client instance and VM):**

    | Change | Where | Why |
    |---|---|---|
    | `HorrorSpawnRarity` overrides: `toww_hanging` **30 → 2**, `toww_delusion` **3 → 2**, `toww_hallucination` **5 → 2**, Rake `ambient_sound` **15 → 2** | `config/openloader/data/HorrorSpawnRarity/data/{the_one_who_watches,rake_the_arrival}/forge/biome_modifier/` (new files). The hallucination and Rake files are copied from the jars with only `weight` changed, so their biome lists stay as shipped | The July pass aligned TOWW and Rake to the weight-2 benchmark but missed these entries. `toww_hanging` never despawns and was piling up (6 → 8 loaded across the profiles); each one carried a 2048-block follow range |
    | Fragile Torches gentle list: removed `boat`, `ender_pearl`, `egg`, `item`, `experience_orb`, `minecart` | `config/fragiletorches.toml` | The handler only sees living entities, so these six could never match. But the list is string-parsed for every mob on every tick, before the chance roll. No behaviour change |
    | **Data Anchor removed** (`Data_Anchor-forge-1.20.1-1.0.0.24.jar`) | Jar parked in `local/mods-removed-2026-11-06/` on the client instance and in "The Correction (Dev)"; deleted from the VM's `mods/` | Orphan library: no jar on either side uses it. Its chunk and block-entity tick hooks cost ~1% of the overworld tick |
    | The Man From The Fog disabled | VM: `man_config.toml`, the `mftfhang` override, `HorrorQuietAdvancements` | See "2026-11-06 — The Man From The Fog: disabled" |
- **Reason:** the server-tick audit; plan, findings and estimates in `docs/SERVER_TICK_AUDIT_2026-11-06.md`. The per-change reasons are in the table.
- **Verification:** the phase 1 files were uploaded and verified byte for byte while the VM was stopped; full backup at `files/serverbak` (the 18:00 copy). Checks listed for the first start (results not recorded): OpenLoader injects **18** data packs (`HorrorQuietAdvancements` is new); no Data Anchor in the mod list; no "Unable to initialise the async-profiler engine" warning from spark. Baseline command: `/spark profiler start --thread * --timeout 300`. A post-fix all-threads profile was taken 19:06–19:20 (see "2026-11-06 — spooky_tick_fixes 1.2.0").
- **Applies to:** the VM and the client instance. Data Anchor registers its own sync packets on a network channel with a strict version check (`"1"` on both sides): every client that joins must drop it too, or it is refused at login. Remove it from each CurseForge profile as well, so the app doesn't restore it.
- **Status:** live on the VM (2026-11-06; running in the 19:06–19:20 profile). Server v2: not applied (it still had Data Anchor and an active Man From The Fog on 2026-11-06). "The Correction (Dev)" was deleted 2026-11-06. Not applied (open decisions as of 2026-11-06): Artifacts campsites and the optional configs (audit §5.2.4, §5.2.6) and the cleanup commands (§5.5.3).
- **Revert:**
  - delete the four new override files;
  - restore the six list entries in `fragiletorches.toml`;
  - move the Data Anchor jar back into `mods/` on every client, and copy it back from `files/serverbak/mods/` on the VM.
- **Related:** "2026-11-06 — Fog Residue: drop moved from The Man From The Fog to CANTBREATHE"; "2026-11-06 — VM disk: duplicate backup copy deleted" (the `files/serverbak` copy filled the disk); audit §5.2.6's Incendium row is applied in "2026-11-06 — Incendium: clock functions trimmed".

### 2026-11-06 — Fog Residue: drop moved from The Man From The Fog to CANTBREATHE

- **Change:**
  - `kubejs/server_scripts/oc_interactions.js`: the `OC_RESIDUES` entries for `man:manfromthefog` / `man:managgresive` replaced by `cant_breathe:cant_breathe`. Same 90% drop chance on death.
  - Quest "Recover Fog Residue" (`field_reports.snbt`, `7A05000000000109`): title "THE MAN" → "THE BREATHLESS", subtitle "The Man" → "The Breathless", and the description rewritten for CANTBREATHE (it spots you from very far, breaks doors, thins the air). Quest and task IDs are unchanged, so progress is kept.
- **Reason:** with The Man From The Fog disabled, the Field Reports quest "Recover Fog Residue" could not be completed. CANTBREATHE is the other timer-summoned surface stalker, with the same configured cadence (1–2 hours, solitary; the timer mostly fires at exactly 1 h, per the correction in `docs/HORROR_REVIEW_2026-11-06.md`), any overworld biome, and no damage immunity, so it can be killed.
- **Rejected:** TOWW (1,024 HP, 100 armor, custom damage handling); the Pale Hound (five remote biomes only); Creeping Woods' Demon Cloud (5 HP, too trivial for a marquee drop).
- **Applies to:** both.
- **Status:** live on the VM (2026-11-08).
- **Related:** `PACK_DIRECTION_REVIEW_2026-07-08.md` and `implore_implementation_plan.md` still name the Man as the residue source; they are historical. The item's display name became Breathless Residue in "2026-11-08 — Quest book: fixes and additions". The Man's disabling: "2026-11-06 — The Man From The Fog: disabled".

### 2026-11-06 — spooky_tick_fixes 1.0.0: first tick-fix pass

- **Change:** new pack-built Forge mod `spooky_tick_fixes` 1.0.0 (`tools/spooky_tick_fixes/`) of behaviour-preserving server-tick fixes (audit §5.3). Each patch skips work that cannot change the outcome, applies only when its target mod is installed (a mixin plugin logs `… -> applying (modid)`), and has its own switch in `config/spooky_tick_fixes-common.toml`. It has no network channels or registry entries (`displayTest = IGNORE_ALL_VERSION`), so a client without it can still join.

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
- **Reason:** the costs measured by the server-tick audit (§4), listed in the table.
- **Verification:** headless scratch server (the client instance's 280 server-side jars plus the shim, the repository's `config/`, `defaultconfigs/` and `kubejs/`, a fresh world, Java 17):
  - all 13 patches applied; no shim-related error or mixin failure (the only one is the long-standing Better Chunk Loading/Canary clash);
  - config generated;
  - a dormant mimic summoned 12 blocks up stayed at y 84.0, while an awake one fell and landed (84 → 73);
  - watcher follow range: new hanging and staring watchers get 32, and hanging and staring watchers loaded with 2048 in their NBT are clamped to 32 within the same tick (checked through a datapack function); `toww_hunting` stays at 2048;
  - cow, pig, zombie, Born in Chaos and Creeping Woods mobs ticked for 20 s with the gates active.

  Not tested in game: the player-dependent patches (The Obsessed, Whispering Spirits, Farm & Charm, Fragile Torches next to a torch).
- **Applies to:** both; a client without it can still join.
- **Status:** client instance (`mods/` and `local/`, via `build.py --install`) and "The Correction (Dev)" `mods/` (2026-11-06). Uploaded to the VM's `mods/` while the VM was stopped (281 jars, verified byte for byte; 2026-11-06). Replaced by 1.1.0 everywhere (2026-11-06).
- **Revert:** delete the jar, or set any single switch to false. Nothing is stored in worlds.
- **Related:** `tools/spooky_tick_fixes/README.md` (source, design notes and the per-patch safety argument). Not included in 1.0.0: Full Immersion portal-AI gating (§5.3.5; its portal index needed verifying first), Alex's Caves magnets (§5.3.10), Slice & Dice and Citadel (§5.3.9), activation range (§5.5.1); see "2026-11-06 — spooky_tick_fixes 1.1.0: second pass and activation range".

### 2026-11-06 — spooky_tick_fixes 1.1.0: second pass and activation range

- **Change:** 1.1.0 replaces 1.0.0. New behaviour-preserving patches (cost as a share of the overworld tick):
  - `fullImmersionGate` (~2.9%): Full Immersion's per-mob portal-AI wrapper (`PortalAiContext.begin`/`finishAi`) and its portal despawn check are skipped in any dimension with no Immersive Portals portal loaded; begin/finishAi are skipped as a pair.
  - `alexsCavesMovingBlockGate` (~0.4%): Alex's Caves' per-movement search for moving magnetic blocks is skipped in dimensions with none loaded; both functions then return their empty answer. Its per-entity magnetism check (~1.8%) is unchanged; that needs a magnet-block count.
  - `citadelTrackerCache` (~0.3%): Citadel's tick-rate tracker is cached per server instead of a saved-data lookup on every entity tick. The cached path still marks the data dirty, so persistence is unchanged.
  - Portal and moving-block counts come from entity join/leave events and can only err towards "present", which switches a gate off, never on.
  - **Activation range** (`[activationRange]`, audit §5.5.1), which changes behaviour for far mobs only:
    - mobs more than 48 blocks from every player tick 1 in 4 (staggered);
    - ageing and despawn checks run on the skipped ticks too, so they keep their normal pace;
    - exempt: targets, recently hurt, burning, in lava, leashed, riding, in a raid; bosses; the `minecolonies`, `guardvillagers`, scripted-horror and `mahoutsukai` namespaces (config lists);
    - design decisions: villagers are throttled; adaptive mode is on (throttle only while the average tick is above 50 ms, off below 30 ms);
    - `/tickfixes stats` (op) shows it.
- **Reason:** the audit's second-pass shim patches (§5.3) and activation range (§5.5.1), with the costs listed.
- **Rejected:** a datapack tag for the exempt mobs (the audit's proposal): config lists are simpler.
- **Verification:** headless scratch server:
  - all 18 conditional patches applied, and the activation-range hook ran;
  - `/tickfixes stats` reported 4–6 far mobs skipped per tick (adaptive off for the test) and `portals=0 acMovingBlocks=0`;
  - a throttled baby cow aged −23,900 → −23,700 in 10 s (normal rate);
  - a ridden (immune) pig dropped from 30 blocks died on landing within ~2 s, while the throttled plain pig was still at y 98.7;
  - dormant mimics still slept;
  - no shim-related errors.

  Not tested: the in-game checks with players.
- **Applies to:** both.
- **Status:** live on the VM (2026-11-06; running in the 19:06–19:20 profile, which measured 20 TPS at a 46 ms median, from 6.6–7.2 TPS at about 140 ms). Also on the client instance (`mods/`, `local/`) and "The Correction (Dev)" (deleted 2026-11-06). The VM had 281 jars. Replaced by 1.2.0 (2026-11-06).
- **Revert:** delete the jar, or set the patch's switch to false. Nothing is stored in worlds.
- **Related:** `tools/spooky_tick_fixes/README.md` (safety argument per patch, activation-range rules). Open as of 2026-11-06: the in-game checks (with players), Alex's Caves' magnetism check, Slice & Dice wet air, The Obsessed's cached-namespace fix, and the watcher despawn rule (§5.5.2; held back because it changes horror pacing, and needs a decision). The post-change spark profile is the 19:06–19:20 profile in "2026-11-06 — spooky_tick_fixes 1.2.0".

### 2026-11-06 — The Man From The Fog jar disabled; second client instance deleted

- **Change:**
  - `The-Man-From-The-Fog-1.4-1.20.1.jar` → `.jar.disabled` on the client instance and the VM.
  - The second client instance, "The Correction (Dev)", was deleted; the client instance is the only client instance.
- **Reason:** Design: The Man From The Fog is removed entirely. The config switch in "2026-11-06 — The Man From The Fog: disabled" stopped its spawns; disabling the jar also drops the `man:house` structure, the Cultist villager job and the first-join advert and cassette.
- **Mechanism:** `man:man` is a strict network channel, so the client and the VM must agree: a mismatch refuses the login with "mismatched mod channel list".
- **Verification:** VM parity: 280 active jars = the client's 303 − 23 client-only. Data Anchor is gone from every side, and `spooky_tick_fixes-1.1.0` is on both.
- **Applies to:** both; every player needs the pack update with the jar disabled.
- **Status:** live on the VM (2026-11-08). Server v2: not synced (it still had an active Man From The Fog on 2026-11-06).
- **Revert:** re-enable the jar on every client and the VM together. `man_config.toml` was deleted on 2026-11-07 ("2026-11-07 — Repository cleanup: personal files purged from history, orphaned configs removed"), so the mod starts from its default config.
- **Related:** "2026-11-06 — The Man From The Fog: disabled".

### 2026-11-06 — VM disk: duplicate backup copy deleted

- **Change:** on the VM, the `simplebackups/` folder inside `files/serverbak` (9 zips, 2.44 GB, an exact copy of the live one, with the same names and sizes) was deleted, leaving about 2.7 GB free.
- **Reason:** at 19:06 the 12.6 GB VM disk had 247 MB free and SimpleBackups aborted ("Not enough space on disk"). The full 18:00 copy in `files/serverbak` (the phase 1 backup) had taken the remaining space.
- **Applies to:** the VM.
- **Status:** applied on the VM (2026-11-06). SimpleBackups was left unchanged (full backup every 120 min, keep 10, about 1 GB each), so the VM disk was to be grown in Proxmox. The disk was still 12.6 GB on 2026-11-07.
- **Related:** "2026-11-08 — SimpleBackups: every 6 hours, keep 5"; "2026-11-07 — spooky_pregen: no daily growth cap" (2 GB free-space floor on the same disk).

### 2026-11-06 — Deployment tooling: VM sync, mod-list generators, shared build script

- **Change:**
  - **`scripts/sync_server_mods.py`** deploys to the live VM by default, over FTP with the login from `~/.spooky_ftp_netrc`; `--server <folder>` targets a local server folder. It shows the changes and, for the VM, asks before applying (`--dry-run` only shows them, `--yes` skips the question).
    - Uploads go to `<name>.part` and are then renamed, so a broken upload never loads.
    - Unwanted jars are no longer deleted. They are renamed to `.jar.disabled` when the client has them disabled too, otherwise moved to `mods-removed/` beside `mods/`.
    - Local targets compare SHA-256; the VM compares sizes.
    - The client-only list carries its groups and notes (`CLIENT_ONLY_GROUPS`), so the server doc can no longer miss a jar.
  - **`tools/generate_server_modlist.py`** reads the VM by default. It computes the server's disabled jars, the Forge version (from `libraries/`) and client parity. Its fixed prose is a single hand-maintained `NOTES` block, which says the VM runs Java 21 (2026-11-06).
  - **`tools/generate_modlist.py`:**
    - new first category **Local Fixes**: `spooky_tick_fixes`, `immptl_dimthread_compat`, `farsight_immptl_compat` (the last two were filed under libraries and QoL);
    - the Minecraft version is the most common declared x.y.z reading, no longer the first jar alphabetically;
    - jars are closed after reading, and counts say jars and mods.
  - **Build scripts:** the three `tools/*/build.py` share `tools/_javabuild.py`.
    - The mod id and version are read from `mods.toml`, which must match `MANIFEST.MF`.
    - `--install` removes older versions from `mods/` and `local/` for all three. Before this, two of them left the old jar, which would stop Forge with a duplicate mod.
    - Library wildcards pick the highest version numerically; `mods/` jars must match exactly once.
    - The JDK and library paths can be overridden (`SPOOKY_JDK`, `SPOOKY_MC_LIBS`).
- **Reason:** the live server moved to the VM (FTP), and the mod lists and build scripts had the faults listed.
- **Verification:**
  - `docs/SERVER_MODS.md` regenerated from the VM: 280 jars, 1 disabled, parity OK.
  - `modlist.md`, `modlist.json` and the README block regenerated: 303 enabled jars, 306 mods, 6 disabled. The previous files predated Data Anchor's removal and the tick-fix jar.
  - All three mods rebuilt to contents identical to the installed jars.
- **Applies to:** repository tooling.
- **Status:** in the repository (2026-11-06). Server v2: not synced (2026-11-06): it still had Data Anchor and an active Man From The Fog, and lacked `spooky_tick_fixes`. `python scripts/sync_server_mods.py --server "…/Servers/Server v2"` brings it in line.

### 2026-11-06 — spooky_tick_fixes 1.2.0

- **Change:**
  - **`antlersDayCounterOverworldOnly`**, a bug fix that changes behaviour: Antlers' day counter runs only on the overworld's tick. Day counting returns to the real pace, and its sync packets drop 45×.
  - **`towwEmptyLevelSyncSkip`:** The One Who Watches no longer encodes its per-level sync packet when that level has no players; the data is still marked dirty.
  - **Shim overhead** (2.7% of the serial tick): the activation-range immunity check reads fire ticks instead of calling `isOnFire()`, which goes through Create's NBT read; the Fragile Torches pre-check uses one chunk lookup instead of two.
- **Reason:** the post-fix all-threads profile of the VM (2026-11-06 19:06–19:20). With the same population as before 1.1.0 (about 550–600 entities, about 150 mimics), the VM ran at 20 TPS with a 46 ms median, down from 6.6–7.2 TPS at about 140 ms. DimThreads 1.2.1 doesn't run levels in parallel, so all ~45 levels tick one after another, and any cost paid per level is paid about 45 times per tick. Antlers' day counter ran on every level's tick, but the counter is global, and each run sent a full sync to every player.
- **Mechanism:** `DimThread.swapThreadsAndRun` is `static synchronized` and wraps each level's tick. The day counter's only effect is the one-time `wendigospawn` flag at day 2, which is already set in existing worlds. Source: `config/spark/profile-2026-11-06_19.20.44.sparkprofile` (local only; decoded locally, too large for spark's upload).
- **Verification:** scratch server:
  - day counter: **10,170 counts in about 224 ticks (45.4 per tick) without the patch, and 227 with it**;
  - an empty dimension's `WorldTime` still advances and is saved;
  - all 20 conditional patches applied, no errors.
- **Applies to:** both.
- **Status:** client instance (`mods/` and `local/`, replacing 1.1.0) and the VM (uploaded with `scripts/sync_server_mods.py --yes`; 1.1.0 moved to `mods-removed/`; 280 jars, parity verified), 2026-11-06. Live on the VM (2026-11-06; running at the 21:56 crash). Replaced by 1.3.0 (2026-11-06).
- **Revert:** delete the jar, or set the patch's switch to false.
- **Related:** `tools/spooky_tick_fixes/README.md` (patch details). Separate finding: `Mixin apply failed betterchunkloading.mixins.json:ServerChunkCacheMixin` (it can't inject into `ServerChunkCache.getChunk`) also appears in the VM log. It predates the shim, so Better Chunk Loading's chunk-priority hook has never been active. The lock finding led to "2026-11-06 — DimThreads disabled".

### 2026-11-06 — Incendium: clock functions trimmed

- **Change:** new OpenLoader datapack `IncendiumClockTrim`: same-path overrides of two Incendium 5.3.5 functions. Changed lines are marked `trim`.
  - **`clocks/main`:**
    - **(A) Shield scans:** the two shield-poison scans use `@a` instead of an untyped `@e`. Only players ever get `in.cd_shield`: it is set by player init/main and by the shield functions, which run as the player.
    - **(A) Freeze scans:** the two `in.frozen` scans (countdown and snowflake particles) run only while `$trim.frozen in.dummy` is above 0.
    - **(B) Missing-ID fallback:** runs every 10 ticks instead of every tick.
  - **`item/chilling_blade/on_hit`:** also sets `$trim.frozen` to 120, the freeze length. That is the only place `in.frozen` is ever set.
- **Reason:** all five of the world's scheduled functions are Incendium clocks. `incendium:clocks/main` reschedules itself every tick and runs 9 `@e` scans across every loaded dimension. Together they were 10% of the serial tick (about 4.5 ms), and 7.8 of those points were entity scanning. This reverses the earlier "leave Incendium as is" decision.
- **Mechanism:**
  - **(A) is identical.** One cosmetic edge: a mob that unloads mid-freeze and reloads later keeps its leftover score without particles until the next Chilling Blade hit.
  - **(B) adds a delay of up to 0.5 s** for mobs that arrive already tagged `in.checked` (the castle round-table room, both sanctum structures, two sentry-blaze summons) or whose ID was cleared by an ID-counter reset. In that window, Incendium's melee weapon effects don't trigger on them.
  - **Expected saving:** about 2–2.7 ms per tick. Without a Chilling Blade freeze active, 4 of the 9 scans are gone, and a fifth runs 1 tick in 10.
- **Rejected:**
  - limiting the scans to the Nether: the Chilling Blade, Blazing Hatchet, Daybreaker, Warping Witherblade and Cake Batter find the mob they hit by its `in.eid`;
  - running the clock less often: projectiles and castle AI run every tick.
- **Verification:** scratch server:
  - OpenLoader loaded the pack ("Found new data pack … loading it automatically"), with no function errors;
  - the `$trim.eid` counter cycles;
  - a mob pre-tagged `in.checked` had no ID, then had one 2 s later;
  - with the timer at 0, a mob's `in.frozen` 40 stayed at 40; with the timer at 40, it counted to 0 and so did the timer.
- **Applies to:** both (global).
- **Status:** live on the VM (2026-11-08). Uploaded to `config/openloader/data/IncendiumClockTrim/` (19 packs there) and verified byte for byte (2026-11-06).
- **Maintenance:** the override pins Incendium 5.3.5's `clocks/main`. When Incendium updates, re-diff it against the jar's copy, or delete the pack.
- **Revert:** delete `config/openloader/data/IncendiumClockTrim/` on the client instance and the VM.
- **Related:** audit §5.2.6 (Incendium per-tick clock).

### 2026-11-06 — DimThreads disabled

- **Change:** `dimthread-FORGE-mc1.20.1-v1.2.1.jar` and `immptl_dimthread_compat-1.0.0.jar` → `.jar.disabled` on the client instance and the VM.
- **Reason:** DimThreads 1.2.1 wraps every dimension's tick in one global lock, so it never ticked dimensions in parallel. The 46-minute profile with `/gamerule dimthread_active false` (2026-11-06 20:40–21:26) averaged 18.6 TPS with 2 players and 1,313 entities, no worse than with it on.
- **Mechanism:** `DimThread.swapThreadsAndRun` is `static synchronized`. The compat jar has to go too: its required mixin references DimThreads classes. No other mod depends on DimThreads. The world's saved `dimthread_*` gamerules are ignored.
- **Verification:** the scratch server started cleanly without either jar.
- **Applies to:** both.
- **Status:** live on the VM (2026-11-06): `scripts/sync_server_mods.py --yes` renamed both jars to `.jar.disabled` before the start that ended in the 22:05 crash.
- **Revert:** rename both jars back to `.jar` on every client and the VM. `dimthread-common.toml` was deleted on 2026-11-07, so DimThreads starts from its default config.
- **Related:** "2026-11-06 — spooky_tick_fixes 1.2.0" (where the lock was found); "2026-11-02 — Immersive Portals × DimThreads: compat shim"; "2026-11-03 — Better Chunk Loading: smart post-processing off" (a hang that needed DimThreads).

### 2026-11-06 — spooky_tick_fixes 1.3.0: horror look-check ray casts

- **Change:** the `clip` calls of seven per-player look-check procedures (Creeping Woods ×3, Rake, Whispering Spirits, John, The Obsessed) go through `RayGuard`. A ray spanning an unloaded chunk returns vanilla's miss result instead of loading it, and the three identical casts per step reuse one result. Switches: `horrorRayCastLoadedOnly`, `horrorRayCastDedupe`. `/tickfixes stats` reports the guard's counters.
- **Reason:** during the 2026-11-06 20:40–21:26 profile a sailing player outran chunk loading. The procedures' vanilla `clip` loads chunks synchronously. The Server thread blocked for 97 s in two minutes, with one 18.9 s tick, while those chunks were loaded or generated.
- **Verification:** scratch server: all 27 conditional patches applied (7 new ray targets), with no injection errors. Not tested: the guard itself, which needs players to exercise it.
- **Applies to:** both.
- **Status:** client instance (2026-11-06). VM: uploaded with `scripts/sync_server_mods.py --yes` (1.2.0 moved to `mods-removed/`; 281 jars, parity verified). Live on the VM (2026-11-06; it ran at the 22:05 and 22:10 crashes). Replaced by 1.3.2 at the 22:25 restart (2026-11-06).
- **Revert:** set `horrorRayCastLoadedOnly` and `horrorRayCastDedupe` to false.
- **Related:** `tools/spooky_tick_fixes/README.md` (targets and safety argument). The span check had a bug, fixed in "2026-11-06 — Watchdog crashes 21:56 and 22:05; spooky_tick_fixes 1.3.1".

### 2026-11-06 — Let Me Despawn, Almanac and Dynamic View added

- **Change:**
  - **Let Me Despawn** `letmedespawn-forge-1.20.1-1.26.11.1.jar`, plus its required library **Almanac** `almanac-forge-1.20.1-1.26.11.1.jar`. DungeonConfig, its other requirement, is embedded in the Almanac jar (jar-in-jar), so that jar covers both.
  - **Dynamic View** `dynview-1.20.1-4.0.jar` (reports itself as 2.3). Its config is a Cupboard JSON, `config/dynamicview.json`, created on first start. The defaults were min/max view 10/10 and simulation 4/10. Applied values:

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
- **Reason:** Design: mobs that pick up items despawn normally and drop what they picked up (Let Me Despawn); view and simulation distance follow the average tick time (Dynamic View).
- **Mechanism:** Let Me Despawn needs Almanac on the server: without it, the start fails with "Mod letmedespawn requires almanac … and dungeonconfig … not installed" (the 21:58 restart, 2026-11-06). The VM's `dynamicview.json`, created by the server, was root-owned because the server process runs as root (recorded 2026-11-06), so the FTP user couldn't overwrite it. It was renamed to `dynamicview.json.root-owned-old`; the default copy is `dynamicview.json.default-bak`.
- **Verification:** Cupboard's file watcher reloaded `dynamicview.json` live on the VM at 22:07 (2026-11-06). The VM sync reported 281 jars, parity verified.
- **Applies to:** both (server-side mods, also in the client instance for singleplayer).
- **Status:** live on the VM (2026-11-06). Almanac was uploaded by `scripts/sync_server_mods.py --yes` before the start that ended in the 22:05 crash; `config/dynamicview.json` is on the client instance and the VM.
- **Revert:** remove the three jars from the client instance and the VM, and `config/dynamicview.json`.
- **Related:** `docs/SERVER_MODS.md` notes; "2026-11-06 — Server tick audit: phases 0 and 1" (file ownership).

### 2026-11-06 — Watchdog crashes 21:56 and 22:05; spooky_tick_fixes 1.3.1

- **Change:** `spooky_tick_fixes` 1.3.1: `RayGuard`'s span check uses `ServerChunkCache.getChunkNow` instead of `hasChunk`. `getChunkNow` reads the chunk's FULL future with `getNow` (verified in bytecode) and never waits, so a chunk that hasn't finished counts as not loaded and the cast returns vanilla's miss result.
- **Reason:** both crashes were the same failure. In each, the watchdog killed the server after one tick ran past `max-tick-time` (120 s). The Server thread was inside `ServerChunkCache.getChunkBlocking`, called from a horror look-check ray cast while a player moved faster than chunks loaded. One world-generation thread was busy with terrain noise; the others were idle.
  - **21:56, VM on 1.2.0 (no guard):** Creeping Woods' `CreatureFromTheWoodsRaytracingProcedure` called vanilla `clip`. The restart loop then hit the missing Almanac four times (21:56–21:58) until the sync.
  - **22:05, VM on 1.3.0:** Rake's `RakeRaytracingProcedure` called `RayGuard.clip`. The guard judged the span loaded and called vanilla `clip`, which blocked.
- **Mechanism:** the 1.3.0 bug: in `ServerChunkCache`, `hasChunk` only tests that the chunk has a ticket at full-chunk level, not that its load has finished, so a chunk still generating ahead of the player passed. Source: the 21:56 and 22:05 crash reports (2026-11-06).
- **Verification:** 1.3.1 boots on the scratch server: 27 patches applied, no errors.
- **Applies to:** both.
- **Status:** client instance (`mods/` and `local/`, 2026-11-06). VM: uploaded after the 22:13–22:18 profile (2026-11-06) and replaced by 1.3.2 before the 22:25 restart. Until then the VM ran 1.3.0 and hit the same hang at 22:10 (see "2026-11-06 — spooky_tick_fixes 1.3.2: The Obsessed waits for chunks").
- **Revert:** set `horrorRayCastLoadedOnly` to false.
- **Related:** `tools/spooky_tick_fixes/README.md`. The long-standing Better Chunk Loading mixin failure is a conflict with Canary's `chunk_access.ServerChunkCacheMixin`.

### 2026-11-06 — spooky_tick_fixes 1.3.2: The Obsessed waits for chunks

> Superseded in part 2026-11-07 by "2026-11-07 — The Obsessed: chunk radius 3 → 6" (radius 3 → 6).

- **Change:** 1.3.2 skips The Obsessed's per-player tick while any chunk within 3 chunks of the player isn't finished (`getChunkNow`). Switches: `obsessedWaitForChunks`, `obsessedChunkRadius`.
- **Reason:** after the 22:11 restart (VM still on 1.3.0), a player logged in at x −5537, far outside the pre-generated ±2,048 area. The profile (22:13–22:18, one 27.6 s tick) shows the Server thread blocked on chunks still being generated:
  - Creeping Woods ray casts 30 s, Rake 25 s, shadow figure 5 s, all through the 1.3.0 guard bug that 1.3.1 fixes;
  - The Obsessed's `PlayerTickProcedure` block reads 19 s, which the ray-cast guard doesn't cover.

  The 22:10 crash was the same 1.3.0 bug, through the Creeping Woods ray cast.
- **Mechanism:** The Obsessed's block reads reach about 20 blocks from the player (random offsets ±10, 1–15, 1–20).
- **Verification:** boot-tested on the scratch server: 28 patches, no errors.
- **Applies to:** both.
- **Status:** client instance (2026-11-06). VM: uploaded with the sync (1.3.1 moved to `mods-removed/`; 281 jars, parity verified). Live on the VM (2026-11-06, from the 22:25 restart). Replaced by 1.3.3 (2026-11-07).
- **Revert:** set `obsessedWaitForChunks` to false.
- **Related:** `tools/spooky_tick_fixes/README.md`. Recommendation (no application recorded): a world border at the pre-generated edge (`/worldborder center 0 0`, `/worldborder set 4096`) until the world is pre-generated further out. Pre-generation: "2026-11-07 — spooky_pregen 1.0.0: pre-generation while the server is empty".

### 2026-11-06 — Simple Zoom added

- **Change:** Simple Zoom 1.0.1 by MrRockis, `simplezoom-forge-1.20.1-1.0.1.jar` ([Modrinth](https://modrinth.com/project/zoom-simple)), added to the client instance. `simplezoom-` added to `CLIENT_ONLY_GROUPS` in `scripts/sync_server_mods.py`; a `simplezoom` category rule (Storage, QoL & utility) added to `tools/generate_modlist.py`. `modlist.md`, `modlist.json` and the README at-a-glance block regenerated; `docs/SERVER_MODS.md` regenerated from the VM.
- **Reason:** Design: an OptiFine-style zoom. Hold **C** (rebindable under Controls → "Simple Zoom") to zoom 3×, and scroll while holding to change it, up to 50×.
- **Mechanism:** the only dependency is Forge 47.2.30+. Client-only: both of its mixins (`GameRenderer`, `MouseHandler`) are in the mixin config's `client` list, and it has no network code, so players without it can still join. Its `mods.toml` line `side = "CLIENT"` isn't a field Forge 1.20.1 recognises, so nothing stops it loading on a dedicated server; hence the client-only list. Gameplay: free zoom makes long-range scouting easier than the spyglass or Hexerei's Reading Glasses. It doesn't extend any horror mob's server-side look check.
- **Rejected:** `simple_zoom-1.20.1-1.0.jar`, a 2023 mod also called "Simple Zoom" (`com.anubis.simpleZoom`, with an unedited Forge example `mods.toml`).
- **Verification:** sync dry run against the VM: 305 active − 24 client-only = 281, parity unchanged, nothing to upload. Not tested: zoom with Oculus + Hysteria (TAA), and zoom looking through Immersive Portals.
- **Applies to:** the client instance; never on the VM.
- **Status:** client instance only (2026-11-06). Players get it with the pack update.
- **Revert:** delete the jar from `mods/`.

### 2026-11-07 — Configured added

- **Change:** Configured 2.2.3 by MrCrayfish, `configured-forge-1.20.1-2.2.3.jar` (CurseForge only; the Modrinth project named "Configured" is an unrelated Fabric mod), added to the client instance. `configured-` added to `CLIENT_ONLY_GROUPS` in `scripts/sync_server_mods.py`, so it can't be used to edit the VM's server configs from a client. A `configured` category rule added to `tools/generate_modlist.py`; `modlist.md`, `modlist.json`, the README block and `docs/SERVER_MODS.md` regenerated.
- **Reason:** JEI 15 has no settings screen of its own. Its wrench button looks for a screen another mod registers, and without one it prints *Install the "Configured" mod to access the in-game config*.
- **Mechanism:** the only required dependency is Forge 47.1.47+; JEI, Framework (already in the pack, 0.8.0) and Catalogue are optional links. It gives JEI a settings screen, and gives one to every mod with Forge configs, through Mods → *mod* → Config. Client-only: both of its mixins are in the mixin config's `client` list. Its `configured:play` channel accepts any version or a missing peer (both checks return true), so players without it can still join. Every player can change the pack's client-side config choices from these screens; they could always edit the files.
- **Verification:** sync dry run: 306 active − 25 client-only = 281, parity unchanged.
- **Applies to:** the client instance; never on the VM.
- **Status:** client instance only (2026-11-07). Players get it with the pack update.
- **Revert:** delete the jar from `mods/`.

### 2026-11-07 — farsight_immptl_compat 1.0.1: held chunks follow the full render distance

- **Change:** hold-radius limit `min(render distance, 32)` → default **64** (`HeldUnloads.MAX_HOLD_RADIUS`; `mods.toml`/`MANIFEST.MF` 1.0.1; README updated in `tools/farsight_immptl_compat/`). 64 is Farsight's `maxRenderDistance`, the slider's maximum, so held chunks follow the client's render distance at any setting. `build.py --install` replaced 1.0.0 in `mods/` and `local/`.
- **Reason:** with 1.0.0, a slider above 32 drew empty space past 32 chunks. The VM's view distance is 8–12 (Dynamic View, from 2026-11-06), so held chunks are everything a player sees beyond that.
- **Mechanism:** cost: a render distance of 64 holds about 16,600 chunks against 4,200 at 32: four times the chunk memory on the client heap, and four times the meshes on the GPU. Players on a small client heap can set `-Dfarsight_immptl_compat.maxHoldRadius=32` (the 1.0.0 behaviour) in their JVM arguments, or lower the slider.
- **Verification:** the built jar was checked: its default is 64.
- **Applies to:** the client instance only; the jar is never synced to the VM. Players who keep 1.0.0 keep the 32-chunk limit; nothing breaks either way.
- **Status:** client instance only (2026-11-07). Players get it with the pack update.
- **Revert:** `-Dfarsight_immptl_compat.maxHoldRadius=32` restores the 1.0.0 behaviour on a client.
- **Related:** `tools/farsight_immptl_compat/README.md`; "2026-11-03 — Farsight × Immersive Portals: client chunk-keeping shim".

### 2026-11-07 — The Obsessed: chunk radius 3 → 6

- **Change:** `config/spooky_tick_fixes-common.toml`: `obsessedChunkRadius` 3 → **6**, on the client instance and the VM. The VM's previous file is kept as `spooky_tick_fixes-common.toml.bak-20261107-radius3`.
- **Reason:** after the 22:25 restart (2026-11-06), with 1.3.2 running, The Obsessed still made the server thread wait 277 s for chunks in 35 minutes (spark, 22:27–23:02; stalls of up to 29 s).
- **Mechanism:** the stalker spawn search keeps a scan point in the player's data (`idk_x`/`idk_z`) and checks blocks there. The point moves 10 blocks at a time while a player is within 37.5 blocks of it, and resets to the player when none is within 70. So the reads land about 37–80 blocks away, at most 5 chunks from the player's chunk; a radius of 3 (48 blocks) missed them. 6 chunks gives one chunk of margin over the 5-chunk reach. The check costs 169 chunk lookups per player per tick, which is negligible. Forge's config watcher didn't notice the change on the VM, either for the replaced file or for an in-place append, so it applies at a restart. The code default was 3 until 1.3.3, which defaults to 6; the config file overrides it. Source: The Obsessed's bytecode; the spark profile of 2026-11-06 22:27–23:02.
- **Applies to:** both; in multiplayer the VM's file applies.
- **Status:** live on the VM (2026-11-08).
- **Revert:** set `obsessedChunkRadius = 3`, or restore `spooky_tick_fixes-common.toml.bak-20261107-radius3` on the VM, and restart.
- **Related:** remaining gap: with two players, one player's scan point can sit near the other player, outside both radius checks. `obsessedReadsLoadedOnly` closes it in "2026-11-07 — Watchdog crash 01:44 (villager bed); spooky_tick_fixes 1.3.3".

### 2026-11-07 — Repository cleanup: personal files purged from history, orphaned configs removed

- **Change:**
  - **Purged from every commit and tag** (history rewritten and force-pushed, so every commit ID changed; commits use the GitHub noreply email): `servers.dat`, `servers.dat_old`, `config/jei/world/`, `config/embeddium-fingerprint.json`, `config/chunky/tasks/`, `config/xaeropatreon.txt`, `config/oculus.properties`, `config/spark/`, `config/crash_assistant/`, `rhino.local.properties`, `.qmenu_opened.marker`, `observable_announce`, `win_event1783619581648.txt`, `user-prefs.json`. The files stay on disk (local only) and are in `.gitignore`. The v1–v13 release zips were repacked without them, except `oculus.properties`, which still turns Hysteria on by default.
  - **Removed, stray copies:** `minecolonies-serverNEW.toml` (identical to `defaultconfigs/minecolonies-server.toml`), `config/chunksending.json.v41`, `XaeroWaypoints_BACKUP240807/`, `config/ftb-quests-entity-vis/` (the mod reads `config/ftbquestsentityvis/`).
  - **Removed, mod not installed:** `config/coldsweat/`, `mobdismemberment-client.toml`, `theforgotten-common.toml`, `jeioptimizer-client.toml`, `firstperson.json`.
  - **Removed, mod disabled on both sides:** TerraFirmaCraft (`tfc-client.toml`, `tfc-common.toml`, `defaultconfigs/tfc-server.toml`, and the OpenLoader packs `TFCFoodHunger`, `TFCLightItems`, `TFCVanillaCraftingTable`, `TFCHideInventoryTabs`, `TFCNoSizeTooltip`, `data-disabled/TFCModdedFood`), DimThreads (`dimthread-common.toml`) and The Man From The Fog (`man_config.toml`). Re-enabling one starts from its default config; the old files are in git history and in the local `*.bak` copies (`*.bak` is git-ignored).
  - **Kept:** `transition.json` and `trender.json`. They are not orphans: they belong to tr7zw's TRansition and TRender libraries, bundled inside Entity Culling 1.11.3 and Not Enough Animations 1.12.7, and the game writes them at start. Both stay tracked.
  - **Left in place:** `kubejs/server_scripts/tfc_vanilla_food.js` still runs. Without TFC it only writes `tfc:` data that nothing reads.
- **Reason:** the repository is public: personal files are kept out of it. The removed configs belong to mods that are not installed or are disabled, or duplicate a live file.
- **Applies to:** the repository only; no gameplay change.
- **Status:** history rewritten and force-pushed; release zips repacked (2026-11-07).
- **Related:** the VM still has three inert TFC data packs (as of 2026-11-08).

### 2026-11-07 — Watchdog crash 01:44 (villager bed); spooky_tick_fixes 1.3.3

- **Change:** `spooky_tick_fixes` 1.3.3:
  - `villagerSleepLoadedOnly`: the villager's home-bed read answers air when the chunk isn't loaded.
  - `obsessedReadsLoadedOnly`: all 57 of The Obsessed's block reads answer "not empty" / air in unloaded chunks.
  - `obsessedChunkRadius` defaults to 6.
  - `offThreadChunkLog` (diagnostic) logs the first chunk request from each caller on another thread, with its stack.
- **Reason:** `ServerHangWatchdog` crash, 2026-11-07 01:44: one tick took 120 s in vanilla `SleepInBed.checkExtraStartConditions`, reading a villager's remembered home bed in a chunk that wasn't loaded and still had to be generated. `obsessedReadsLoadedOnly` closes the two-player and after-teleport gaps left by the radius check. `offThreadChunkLog` is to name the mod behind the between-tick stalls.
- **Mechanism:** cause: vanilla reads the block at the home bed *before* checking that the bed is within 2 blocks. Finishing that chunk needs a ring of neighbouring chunks generated too, and the world-generation threads were busy with noise and YUNG's Better Caves carving: not a deadlock, but more than 120 s of work on the VM's CPU. A player was online, probably exploring; Chunky had finished at 00:51. The crash report names Canary and Observable as "suspected" only because they appear in the stack. A ticking villager can't be within 2 blocks of a bed in an unloaded chunk, so the stub leaves the result unchanged. Source: the 2026-11-07 01:44 crash report and thread dump.
- **Verification:** checked before building:
  - no other mod in the pack patches `SleepInBed` or The Obsessed's `PlayerTickProcedure`;
  - Canary `@Overwrite`s `ServerChunkCache.getChunk`, so the logger's mixin has priority 1500 and applies after it;
  - every Obsessed `isEmptyBlock` / `getBlockState` call site was read in bytecode to choose the safe stub value.

  The `spooky_pregen` 1.0.0 scratch-server tests ran with 1.3.3 installed. On the VM, the off-thread logger had caught vanilla `Cat.finalizeSpawn` by the 03:08 crash.
- **Applies to:** both.
- **Status:** client instance (2026-11-07). Live on the VM (2026-11-07; running at the 03:08 crash). Replaced by 1.3.4 (2026-11-07).
- **Revert:** set the patch's switch to false (`villagerSleepLoadedOnly`, `obsessedReadsLoadedOnly`, `offThreadChunkLog`).
- **Related:** `tools/spooky_tick_fixes/README.md` (patch details and safety arguments); "2026-11-07 — The Obsessed: chunk radius 3 → 6"; "2026-11-07 — Watchdog crash 03:08; max-tick-time; spooky_tick_fixes 1.3.4 chunk-safe ticking".

### 2026-11-07 — spooky_pregen 1.0.0: pre-generation while the server is empty

> Superseded in part 2026-11-07 by "2026-11-07 — spooky_pregen 1.1.0: blob mode" (the `spawn-3072` job and the nearest-edge default) and by "2026-11-07 — spooky_pregen: no daily growth cap" (the 1000 MB daily cap).

- **Change:** new pack-built mod `spooky_pregen` 1.0.0 (source in `tools/spooky_pregen/`; jar in `mods/` and `local/`). It drives Chunky through its API, and only on a dedicated server; on clients and in singleplayer it does nothing.
  - Once the server has been empty for `emptyGraceMinutes` (5), it works through `jobs`: each grows a square around a point from `radius` to `maxRadius` in `step`s. Then it fills outward from the edge of the generated area, nearest the centre first. The edge comes from the region files' 4 KB location tables, so chunks that already exist are never targeted.
  - It pauses the moment a player logs in, and resumes after a restart. Guards: `minFreeDiskGb` (2.0) and `maxDiskGrowthMbPerDay` (1000). Commands: `/pregen status|next|hold|resume|skip|reload`.
  - `config/spooky_pregen.json`: the first job widens the overworld square from ±2048 to ±3072 in 256-block steps; then the overworld edge rule fills within 4096 of 0,0.
  - Full description: `tools/spooky_pregen/README.md`.
- **Reason:** world generation is mostly single-threaded per dimension. On the VM, Chunky manages about 3–7 new chunks per second, and a player in ungenerated terrain makes the server generate on demand, which caused the stalls and watchdog crashes of 2026-11-06 and 2026-11-07. Pre-generating while nobody is online uses time the server would otherwise spend idle (`tools/spooky_pregen/README.md`, Why).
- **Mechanism:** Chunky's `GenerationCompleteEvent` fires when a task's loop has *handed out* its last chunk, while up to 50 chunks can still be generating, and it also fires on pause and cancel. Treated as completion, it makes tasks overlap. spooky_pregen takes completion from a progress event with `complete()` set, which Chunky raises once, when the finished count reaches the total.
  - A pause stops Chunky from handing out more chunks, but chunks already handed out (up to Chunky's 50 in flight) finish generating. On the VM that is roughly 10–15 s of extra generation right after someone logs in. Optional: `-Dchunky.maxWorkingCount=16` in `user_jvm_args.txt` would shorten it. Generation is serial anyway, so fewer chunks in flight shouldn't cost throughput.
- **Verification:** on the scratch test server (281 jars + `spooky_tick_fixes` 1.3.3 + `spooky_pregen` 1.0.0), with tiny squares and near-zero waits:
  - the job grew 32 → 64 → 96;
  - 48 edge windows followed, nearest first, and **0** of the 54 tasks across both boots overlapped;
  - `hold` stopped new work, and `status` / `reload` worked;
  - an unknown dimension was skipped with one warning;
  - a task paused at shutdown resumed after a restart and finished.
  - Not tested headless: the pause on player login. It's the same `pause()` path as `hold`, and it runs at HIGHEST priority on `PlayerLoggedInEvent`.
- **Applies to:** the VM (dedicated server only; the jar is inert on the client instance).
- **Status:** uploaded to the VM 2026-11-07: the jar with the sync (282 jars, parity verified), the config to `config/` (read back identical). Replaced on the VM by 1.1.0 (2026-11-07). As of 2026-11-08, 1.3.1 is installed on the client instance and the VM, and live on the VM.
- **Revert:** delete the jar. Delete `world/spooky_pregen_state.json` to forget progress. Chunky tasks it left saved can be cleared with `/chunky cancel`.
- **Related:** "2026-11-06 — Watchdog crashes 21:56 and 22:05; spooky_tick_fixes 1.3.1", "2026-11-07 — Watchdog crash 01:44 (villager bed); spooky_tick_fixes 1.3.3".

### 2026-11-07 — Watchdog crash 03:08; max-tick-time; spooky_tick_fixes 1.3.4 chunk-safe ticking

- **Change:**
  - The VM's `server.properties`: `max-tick-time` 120000 → **300000**.
  - `spooky_tick_fixes` 1.3.4 adds chunk-safe ticking (`[chunkSafeTicking]`, on by default): while a dimension ticks, block and fluid reads on the server thread never generate terrain.
  - Also in 1.3.4: the off-thread logger hooks only the off-thread branch, so it costs nothing on the main thread; the Fragile Torches pre-check no longer waits for chunks.
  - Details: `tools/spooky_tick_fixes/README.md`.
- **Reason:** crash at 03:08:58, a 120 s tick. A fluid tick's neighbour update reached Immersive Weathering's fluid-generator check (`SelfFluidGenerator$AdjacentBlocks.isMet`), which read a block in a chunk that still had to be generated while a player explored around x 6000–7000. The server stayed down until restarted.
  - The 1.3.3 off-thread logger also caught **vanilla `Cat.finalizeSpawn`**: village cats placed during generation look up structures through the live world (`structure_references`). Cheap, but it confirms the logger works.
  - Its other catches (Immersive Petroleum reservoirs, Guard Villagers' `getVariantFromBiome`, Alex's Caves) only read biomes.
- **Mechanism:**
  - Chunk-safe ticking answers a read by the state of its chunk: loaded chunks are unchanged; chunks finished in memory or on disk take vanilla's path (a disk load, about 10 ms); terrain that doesn't exist yet or is still being generated gets a **barrier** block at once.
  - Not affected: player actions, commands, worldgen and chunk post-processing.
  - Monitoring: the first barrier read per caller is logged with its stack; `/tickfixes stats` has the counts; `/tickfixes probe <x> <z>` tests a read; `exemptCallers` lets a mod keep generating on the spot, and `enabled = false` restores vanilla.
  - `max-tick-time` 300000: a generation stall freezes the server for up to 5 minutes instead of crashing it; a true deadlock still ends in a crash after 5 minutes.
- **Rejected:** a patch per mod. One guard covers the whole class of reads.
- **Verification:** on the scratch test server: 33 patches applied, with no mixin errors.
  - `probe 0 0` (loaded): fast path, 8 µs.
  - `probe 400 8` (generated but unloaded): real terrain (andesite) loaded from disk in 7–11 ms.
  - `probe 300000 300000` (never generated): barrier with no generation; 4.3 ms the first time, 86 µs once cached.
  - While `spooky_pregen` generated 29 squares under the guard, no other read was answered with a barrier.
  - Not measured: the overhead on the VM under real load. To measure it, compare a spark profile taken after deploying with the median of about 40 ms before.
- **Applies to:** `max-tick-time`: the VM. 1.3.4: both.
- **Status:** live on the VM (by the 2026-11-08 01:14 restart). `max-tick-time` was set 2026-11-07; the backup is `server.properties.bak-20261107-ticktime`. 1.3.4 was uploaded with the sync 2026-11-07 (1.3.3 moved to `mods-removed/`; 282 jars, parity verified), then replaced by 1.4.0, which keeps chunk-safe ticking. Installed as of 2026-11-08: 1.4.0 on the client instance and the VM.
- **Revert:** `[chunkSafeTicking] enabled = false` restores vanilla. `max-tick-time`: the VM's previous file is `server.properties.bak-20261107-ticktime` (120000).

### 2026-11-07 — spooky_pregen 1.1.0: blob mode

> Superseded in part 2026-11-07 by "2026-11-07 — spooky_pregen 1.3.0: blob size command, smaller default" (the pack's blob size, 1024 → 512).

- **Change:** blob mode, the edge rule's new default:
  - **Seed:** the edge chunk of any non-speck island that is nearest to an **anchor**, meaning a player's last logout position or bed.
  - **Grow:** a square centred on it, from 256 to 1024 radius in 256 steps.
  - **Repeat:** recompute the edge and pick again.
  - **Anchors** are read once from `world/playerdata` and updated on every logout. Specks (under 64 chunks) are ignored unless an anchor is near them.
  - **Pack config** (`config/spooky_pregen.json`): the `spawn-3072` job was removed. The edge rule is blob mode with no distance limit.
  - Full description: `tools/spooky_pregen/README.md`.
- **Reason:** the pre-generator should work where players actually go. The generated overworld is 32 islands (from the region tables, 2026-11-07): the main area around spawn (88,091 chunks), a western island (9,734), two far-eastern ones (about 1,250 each) and 28 small specks left by fast travel. Jobs run before edges, and the `spawn-3072` job was about 80,000 chunks (roughly 4.5 h on the VM) away from where players are.
- **Verification:**
  - On the scratch test server, with tiny blobs and a test anchor at (700, 650): the first blob centred at (696, 760), 112 blocks from the anchor, and grew 32 → 64 → 96 before a new blob was picked; 15 tasks ran with 0 overlaps; `status` shows the blob, the reason it was picked and the anchors.
  - Against the VM's real data: anchors for one player at (7130, −4859), and for a second player at (−4197, −3432), with that player's bed beside it. Simulation: blob 1 at (−4136, −3176), the second player's area, 13,348 new chunks; blob 2 at (7352, −5096), the first player's, 15,096; then alternating, about 7,500–8,600 each (25–50 min per blob at 5 chunks/s).
- **Applies to:** the VM.
- **Status:** live on the VM (by the 2026-11-08 01:14 restart). Uploaded 2026-11-07: the jar with the sync (1.0.0 moved to `mods-removed/`; 282 jars, parity verified), and the config, read back identical, with the 1.0.0 version kept as `spooky_pregen.json.bak-20261107-v100`. The jar was replaced by 1.2.0, 1.3.0 and 1.3.1 (2026-11-07).
- **Revert:** the 1.0.0 config is the VM's `spooky_pregen.json.bak-20261107-v100`. `"mode": "nearest"` is the 1.0.0 edge behaviour.
- **Related:** Better Chunk Loading's `ServerChunkCacheMixin` fails to apply on every boot ("@At INVOKE … cannot inject into m_7587_"), because Canary overwrites `ServerChunkCache.getChunk`. So its chunk-priority feature had never been active on the VM (as of 2026-11-07). Nothing was changed. See "2026-11-03 — Better Chunk Loading: smart post-processing off".

### 2026-11-07 — spooky_pregen 1.2.0: /pregen start and /pregen stop

> Superseded in part 2026-11-07 by "2026-11-07 — spooky_pregen 1.3.1: a join always pauses" (force mode starts alongside the players already online, but the next player to join pauses the task and ends force mode).

- **Change:**
  - **`/pregen start` (force-start):** begins or resumes work immediately, without waiting for an empty server, the grace time or the check interval, and keeps running with players online. It clears a hold.
  - **`/pregen stop`:** ends force mode and returns to running only while the server is empty. With players online, the current task pauses at once.
  - **`/pregen hold`** also ends force mode. `/pregen status` shows `FORCED by /pregen start N min ago` while it's active.
  - Documented in `README.md` (Custom commands), `tools/spooky_pregen/README.md` (Commands) and the `PregenCommand` Javadoc.
- **Reason:** Design: operators can start or resume pre-generation at once, without waiting for an empty server, and stop it again.
- **Mechanism:** force mode lasts until `/pregen stop`, `/pregen hold` or a restart. It is never saved, so it can't keep generating under players by accident after a reboot. The disk guards still apply. Expect extra load while players are on: generation competes with their chunk loading. 1.3.4's chunk-safe ticking still stops their ticking code from waiting on it.
- **Verification:** on the scratch test server with a 10-minute grace, so the normal path couldn't start anything:
  - `/pregen stop` without force mode replied "not in force mode";
  - `/pregen hold`, then `/pregen start`: the hold was cleared and the paused blob resumed in the same second;
  - `status` showed `FORCED`, and blobs kept chaining;
  - `/pregen stop` ended force mode. With nobody online, the running square continued, as the normal rules allow.
  - Not testable headless: running with players online. Force mode only bypasses the player check, and the login pause is the same check.
- **Applies to:** the VM.
- **Status:** live on the VM (by the 2026-11-08 01:14 restart). Uploaded 2026-11-07 with the sync (1.1.0 moved to `mods-removed/`; 282 jars, parity verified); a blob in progress carries on after the restart. Replaced by 1.3.0 and 1.3.1 (2026-11-07).

### 2026-11-07 — spooky_pregen 1.3.0: blob size command, smaller default

- **Change:**
  - `config/spooky_pregen.json`: the pack's `blobMaxRadius` 1024 → **512**, so blobs grow 256 → 512 and cover 1,024 × 1,024 blocks.
  - **`/pregen radius [<blocks> | reset]`:** shows or sets the blob size on the fly (16–8192).
  - **Config edits apply to the blob in progress too:** a changed `blobMaxRadius` takes effect after `/pregen reload`. Before 1.3.0, a blob kept the size it was picked with.
  - `/pregen status` shows the blob radius and whether it comes from the config or the command.
- **Reason:** Design: smaller blobs, each about a quarter of the old size, finished in roughly a quarter of the time and placed closer to where players are.
- **Mechanism:** the `/pregen radius` setting is kept in the state file, so it survives restarts, and it overrides the config until `reset`. It applies to the blob in progress. If that blob is already larger, its running square is cancelled (what it generated stays) and a new blob is picked.
- **Applies to:** the VM.
- **Status:** live on the VM (by the 2026-11-08 01:14 restart). Uploaded 2026-11-07: the jar with the sync (1.2.0 moved to `mods-removed/`; 282 jars, parity verified), and the config with radius 512, read back identical (the old one is `spooky_pregen.json.bak-20261107-r1024`). At upload, the VM's first blob, at (6920, −4616) near a player, was already on its last step (radius 1024), started 05:07. The jar was replaced by 1.3.1 (2026-11-07).
- **Revert:** `blobMaxRadius` back to 1024; the VM's previous file is `spooky_pregen.json.bak-20261107-r1024`.
- **Related:** after the 1.3.0 restart, a paused square resumed at the old size. Fixed in "2026-11-07 — spooky_pregen 1.3.1: a join always pauses".

### 2026-11-07 — pregen_map.py: live map mode

- **Change:** `python tools/pregen_map.py --serve [--interval 10] [--port 8765] [--no-browser]` runs a small web server bound to localhost only. It re-reads the VM every `--interval` seconds (default 10, minimum 5), and the page repaints in place, keeping its zoom, position and hover.
- **Reason:** Design: a live version of the pregen map. `tools/pregen_map.py` (added with `spooky_pregen` 1.2.0) reads the VM over FTP and writes a static map, `tools/build/pregen_map.html`. It shows what is generated, the selected blob, the square generating at the time of the read and what is still to come.
- **Mechanism:**
  - **Polling:** the page asks the local server every half interval (2–10 s), and not while its tab is hidden.
  - **Page status:** the eyebrow shows "live · read HH:MM:SS". It says so when the VM can't be read (the last map stays) or when the server has stopped.
  - **Reads only what changed:**
    - region headers of unchanged files are reused;
    - rotated logs are read once, and `latest.log` from where the last read stopped (FTP `REST`);
    - terrain is held in memory, and a region file being written is downloaded again at most every 30 s;
    - relief shading is recomputed only around chunks that changed.
- **Verification:** measured against the VM while generating: first read 8.7 s; each 10 s cycle took 8–12 s, including a terrain refresh every 30 s. A 204 means nothing changed; new data is about 5 MB to localhost.
- **Applies to:** the maintainer's PC (a local tool that reads the VM over FTP). No gameplay change.
- **Status:** in the repository (2026-11-07). The static snapshot (`--fragment`) is not live, because its sandbox can't reach the LAN.

### 2026-11-07 — spooky_pregen 1.3.1: a join always pauses

- **Change:**
  - Force mode means "start now, without waiting for the server to be empty". It still runs alongside whoever is online when it starts, so an operator in game can use it. **The next player to join pauses the task and ends force mode**, logged as `force mode ended: <name> joined`. `/pregen stop`, `/pregen hold` and a restart still end it too.
  - The blob size in force is applied at startup, on `/pregen reload` and by `/pregen radius`, through one routine.
- **Reason:**
  - **Generating with a player online:** `/pregen start` was run at 05:28 (2026-11-07). A player joined at 05:37:44, and generation carried on: force mode skipped the join pause by design (1.2.0). That contradicted the mod's purpose.
  - **Old size after a restart:** after the 1.3.0 restart, the paused blob square resumed at radius 1024 although the config said 512. In 1.3.0 a new config size reached the blob only at its next pick, and only `/pregen radius` cancelled a square that was already larger. A paused square resumes without a pick, so it carried on at the old size.
- **Mechanism:** a paused or running square larger than the size is cancelled (what it generated stays). A blob whose next step is larger than the size is finished, so a new one is picked. A size between two steps can end a blob one step early: with steps 256 → 512, `/pregen radius 384` ends a blob that has only done 256.
- **Verification:** headless on the scratch test server:
  - a paused radius-128 square with the config at 96 was cancelled at boot, logged as `blob size from the config applied; ... now grows to radius 96 (was 128); cancelled its radius-128 square`;
  - `/pregen start` then ran seven squares back to back, and `/pregen hold` stopped the work.
  - Not testable headless: the join pause itself. It is the same pause call the normal empty-server rule uses, without the force-mode exception.
- **Applies to:** the VM.
- **Status:** live on the VM (by the 2026-11-08 01:14 restart). Uploaded 2026-11-07 with the sync (1.3.0 moved to `mods-removed/`; 282 jars, parity verified). Installed on the client instance and the VM (2026-11-08).

### 2026-11-07 — Chunky: silent by default

- **Change:** `config/chunky/config.json`: `"silent"` `false` → **`true`**.
- **Reason:** Chunky showed every operator a progress boss bar while a task ran, and logged a `[Chunky] Task running for …` console line about once a second while generating. These lines made up most of the VM's `latest.log`.
- **Mechanism:**
  - **Still available:** `/chunky progress` and `/pregen status` show progress on demand, and `Task finished` lines are still logged.
  - **spooky_pregen is unaffected:** Chunky posts the progress event that spooky_pregen uses for completion before its silent check.
  - **When it applies:** Chunky reads the file only at startup (and writes it back then; task progress is saved separately, so a shutdown doesn't overwrite it). In game, `/chunky silent` toggles it at once, but only until the next restart.
- **Verification:** seven squares completed in a row with no `Task running` lines.
- **Applies to:** both.
- **Status:** live on the VM (by the 2026-11-08 01:14 restart). Uploaded 2026-11-07 and read back identical; the old file is `config/chunky/config.json.bak-20261107-silent`.
- **Revert:** `"silent": false`, then restart (or `/chunky silent` until the next restart).

### 2026-11-07 — spooky_pregen: no daily growth cap

- **Change:** `config/spooky_pregen.json`: `maxDiskGrowthMbPerDay` 1000 → **0 (off)**.
- **Reason:** the cap tripped on the VM at 08:20 on 2026-11-07 ("the world grew 1006 MB today (limit 1000 MB)") and held pre-generation for the rest of that day. Design: pre-generation runs for as long as the server is empty, with no daily limit.
- **Mechanism:** what still stops it:
  - `minFreeDiskGb` 2.0: it holds while the world's disk has less than 2 GB free, and continues if space is freed;
  - a player joining;
  - `/pregen hold`.
  - `maxDistance` was already 0 (unlimited), so blobs keep spreading outward from the edges nearest the players.
- **Applies to:** the VM.
- **Status:** live on the VM (by the 2026-11-08 01:14 restart). Uploaded 2026-11-07 and read back identical; the old file is `config/spooky_pregen.json.bak-20261107-nocap`. The setting applies at `/pregen reload` or a restart.
- **Maintenance:** the VM disk is 12.6 GB. At the rate seen on the morning of 2026-11-07 (about 1 GB in 2 hours), pre-generation reaches the 2 GB floor and holds there. That leaves 2 GB for logs, backups and player growth. `/pregen status` shows the free space.
- **Revert:** `maxDiskGrowthMbPerDay` back to 1000, then `/pregen reload`.
- **Related:** "2026-11-08 — SimpleBackups: every 6 hours, keep 5" (backups use the same free space).

### 2026-11-07 — JEI: installed on the server

- **Change:** `scripts/sync_server_mods.py`: `jei-` removed from `CLIENT_ONLY_GROUPS`, with a comment saying why. Sync dry run against the VM: 307 active − 24 client-only = 283, and the only change was `copy + jei-1.20.1-forge-15.56.0.205.jar`.
- **Reason:** JEI's recipe "+" button (move a recipe's ingredients into a crafting table) failed with *Server must have JEI installed*. Cause: since the sync script was first written (2026-07-14, commit `b034083`), `jei-` was in `CLIENT_ONLY_GROUPS` as a "client-only utility", so the VM never had it. No crash or reason was recorded for that.
- **Mechanism:** JEI is a two-sided mod. The client asks the server, over JEI's channel, to move the items (`PacketRecipeTransfer`), and the server does the move. The server side of JEI is recipe transfer plus the cheat-mode give/delete buttons. Its server config defaults (read from the jar) are `enableCheatModeForOp = true`, `enableCheatModeForCreative = true` and `enableCheatModeForGive = false`, so survival non-ops get recipe transfer and nothing more. It adds no ticking work.
- **Applies to:** the VM, through the sync script.
- **Status:** live on the VM (by the 2026-11-08 01:14 restart). Uploaded with the sync 2026-11-07; parity verified (283 jars).
- **Revert:** add `jei-` back to `CLIENT_ONLY_GROUPS` and run the sync.

### 2026-11-07 — Antlers Wendigo: keeps away, attacks only when provoked (spooky_tick_fixes 1.4.0)

- **Change:** `spooky_tick_fixes` 1.4.0 adds `[antlersWendigo]` in `config/spooky_tick_fixes-common.toml`. Design: aggressive when provoked, but otherwise it actively keeps away from people.
  - **Calm:** it moves away from any player it can see within 16 blocks (its daytime form already ran from players).
  - **Provoked by:**
    - hitting it, at any time;
    - at night, being within 8 blocks in its sight (4 while sneaking). Because it keeps away, that means surprising it, cornering it or running it down;
    - at night, a 5 s continuous stare within 24 blocks, with the player's crosshair on it, clear sight both ways, and the Wendigo not walking away at the time.
  - **Once provoked:** it chases only that player, and calms down when that player leaves, dies, changes dimension or gets 32+ blocks away.
  - **After calming down:** it retreats from everyone within 48 blocks; only a hit re-provokes it. It vanishes once nobody is within 24 blocks and nobody within 64 can see it, or after 60 s once nobody is within 16.
  - **Sounds:** no stalking roar. It roars every 15–30 s only while provoked, and the roar and the scream are both capped at 128 blocks.
  - **Despawning:** it and its daytime form despawn like other monsters.
  - **Unchanged:** the freeze, the scream and the hit-and-run removal after an attack.
  - Installed in `mods/` and `local/` (1.3.4 removed). The `[antlersWendigo]` section was added to the client instance's `config/spooky_tick_fixes-common.toml`.
  - Details: `tools/spooky_tick_fixes/README.md` (1.4.0) and `config/spooky_tick_fixes-common.toml` `[antlersWendigo]`.
- **Reason:** two players were attacked without warning, one inside their base. The second time, the Wendigo was hunting another player, who logged off; it then switched to the next nearest player, in a `terralith:glacial_chasm`. Players also heard a "loud roar" that several of them got at the same moment. Design: a creature that a player provokes, knowingly or not.
- **Mechanism:** the unpatched mod (Source: antlers-1.0.0 bytecode):
  - **Night hunt:** about every 25 ticks it targets the **nearest** player within ±300 blocks, with no line of sight needed. It then sprints, with Speed III within ±150. Its player target goal also picks any survival player it can see within 60 blocks.
  - **Sounds:** a stalking roar at volume 100, sent to the nearest player within ±355 blocks (heard up to 1,600 blocks) about once a minute. When it targets a survival player within ~12 blocks: a 12.5 s freeze and an "I see you" scream at volume 40 on the **Master** channel, which everyone within 640 blocks hears at once.
  - **Lifecycle:** it never despawns (`removeWhenFarAway` is false). At dawn it swaps to a daytime form and back at dusk, as fresh mobs that bypass the sanity spawn gate. It spawns in any Overworld biome on a non-stone floor in the dark, one per 1,000 blocks, weight 2 (already lowered by `HorrorSpawnRarity`).
- **Verification:** on the scratch test server (no player can join headless):
  - all five patches log `-> applying (antlers)`, with no injection errors after a Wendigo was summoned (that's when its goal classes load);
  - the `[antlersWendigo]` section is generated with its defaults;
  - summoned Wendigos show as `calm` in `/tickfixes wendigo` (the per-tick hook runs), and at daybreak they still turned into the daytime form;
  - three summoned Wendigos built their goals, including the keep-away goal and the retreat, without errors.
  - **Not tested:** keeping away, the provocation itself, the calm-down, the retreat and vanishing, and despawning. All of these need a player. To check in game: `/tickfixes wendigo` lists the loaded Wendigos and their state. The server log gets `wendigo at x y z provoked by <player>: <reason>`, `… calmed down: …; retreating` and `… vanished after calming down`.
- **Applies to:** both.
- **Status:** live on the VM (by the 2026-11-08 01:14 restart). Uploaded 2026-11-07 with the sync (1.3.4 moved to `mods-removed/`; 283 jars, parity verified). Forge adds `[antlersWendigo]` with these defaults to the VM's `config/spooky_tick_fixes-common.toml` on the first 1.4.0 start. 1.4.0 is installed on the client instance and the VM (2026-11-08).
- **Revert:** `[antlersWendigo] enabled = false` (and `despawnNormally = false`), or go back to the 1.3.4 jar.
- **Related:**
  - "2026-11-07 — Wendigo loot: WendigoTrophies datapack".
  - Also found, not changed: CANTBREATHE sends its cave noise to **every** survival player in the Overworld on the same tick, every 12–22 min once its spawn timer is past half (VM `world/serverconfig/cant_breathe-server.toml`: any height, any light, surface allowed, all biomes, `only_play_noise_to_target = false`). Each player gets a random clip. (Corrected 2026-11-08: the noise plays at the victim's position, with about 32 blocks of range, so only players near the victim hear it; it is not server-wide. CANTBREATHE's spawn timer is mostly exactly 1 h.) Setting `only_play_noise_to_target = true` would send it to the chosen victim only. `spooky_tick_fixes` 1.5.0 (built, not installed) plays it only for a critical victim: "2026-11-08 — spooky_tick_fixes 1.5.0: The Obsessed and CANTBREATHE sanity-bound".

### 2026-11-07 — Wendigo loot: WendigoTrophies datapack

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
- **Reason:** Antlers ships no loot table for the Wendigo or its daytime form, and neither has custom drop code. Killing a 300-health Wendigo gave 30 XP and nothing else. Design: exotic, forest-themed loot.
- **Mechanism:** a Wendigo that vanishes (after calming down, its hit-and-run, or the dawn/dusk swap) is removed, not killed, so it drops nothing.
- **Verification:** item IDs were checked against the jars' item models. On the scratch test server: both tables load with no loot warnings. A test copy without the kill condition, rolled 25 times, gave the antlers plus one relic every time. The real table dropped nothing without a player killer, as intended.
- **Applies to:** both.
- **Status:** live on the VM (by the 2026-11-08 01:14 restart). The pack folder was uploaded to `config/openloader/data/WendigoTrophies/` 2026-11-07 and read back identical.
- **Revert:** delete `config/openloader/data/WendigoTrophies/` on both sides.

### 2026-11-08 — SimpleBackups: every 6 hours, keep 5

- **Change:** `config/simplebackups-common.toml`: `timer` 120 → **360** minutes and `backupsToKeep` 10 → **5**.
- **Reason:** Design: a backup every 6 hours. Fewer are kept because pre-generation grew the world: full backups grew from about 1 GB (2026-11-06) to about 3.3 GB (2026-11-07). Ten of them would be about 33 GB, past the 25 GB `maxDiskSize`. Five are about 17 GB, which still covers at least a day and a half at 6-hourly backups.
- **Mechanism:**
  - **Unchanged:** full backups (`FULL_BACKUPS`), the 25 GB cap, and `noPlayerBackups = false`, so nothing is backed up while the server is empty. Pre-generation runs only while it's empty.
  - At its next backup, SimpleBackups deletes the oldest backups beyond five.
  - **Disk:** the backups live in the server's `simplebackups/` folder and come out of the same free space `spooky_pregen` stops at (2 GB). A 3.3 GB backup needs that much room when it runs.
- **Applies to:** both.
- **Status:** uploaded to the VM 2026-11-08, after the 01:14 restart, and read back identical; the old file is `config/simplebackups-common.toml.bak-20261108-every2h`. Live at the next VM restart: Forge's config watcher has not been seen to fire on the VM.
- **Revert:** `timer` 120 and `backupsToKeep` 10 (the VM's previous file is `simplebackups-common.toml.bak-20261108-every2h`), then restart.

### 2026-11-08 — Quest book: real checks instead of click-to-complete

- **Change**, following the design doc (`docs/ftbquest.md`):

  | Quest | Was | Now |
  |---|---|---|
  | The Broken World / A World Left Behind | click | structure `#oldcivilisation:abandoned_places` (179 ruins, dungeons and abandoned buildings from 21 mods and vanilla) |
  | The Broken World / The First Survey | 3 clicks | 5 biome tasks (forest, plains, river, mountain, taiga), 3 structure tasks (village, mineshaft, ruined portal), 1 biome `#forge:is_underground` |
  | The Broken World / The Silent Nights | click | stage `oc_q_silent_nights`: 5 whole nights at least 256 blocks from spawn without sleeping |
  | The Broken World / The First Night | click | stage `oc_q_first_night`: a whole night survived under a roof for at least 75% of it, or slept through |
  | Field Reports / Into The Dark | click | biome `#forge:is_underground`, structure `#oldcivilisation:abandoned_places`, biome `#oldcivilisation:dense_forests` |
  | Field Reports / The Forest Does Not Forget | click | structure `#oldcivilisation:creeping_woods` (all 24 Creeping Woods sites) |
  | Field Reports / Below | click | biome `#alexscaves:alexs_caves_biomes`, and `minecraft:deep_dark` |
  | Field Reports / The Watchers | click | stage `oc_q_watchers`: 10 whole nights 256+ blocks from spawn and the player's bed, outside any MineColonies colony |
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
- **Reason:** 15 of the 186 tasks were FTB Quests "checkmark" tasks, which complete when the player clicks them. They were concentrated in the chapters players see first: The Broken World (6 of 8 tasks), The Second Civilisation (3 of 3) and Field Reports (4 of 10). The other 171 tasks (items, advancements, dimensions) already check something, and every item, advancement, dimension, reward and icon they name exists in the pack.
- **Mechanism:** what FTB Quests can check here (read from the 2001.4.22 jar):
  - item (exact item only; there's no filter mod for tags), advancement, dimension, biome and structure (both accept `#tags`), stat (vanilla custom stats), kill, location and observation;
  - "stage" tasks, which here read vanilla **player tags**: no stage mod is installed, so FTB Library falls back to its entity-tag provider. A KubeJS script can therefore complete one with `player.addTag(...)`. There's no KubeJS bridge mod for FTB Quests (FTB XMod Compat) in the pack. Check a player's tags with `/tag <player> list`; grant one by hand with `/tag <player> add oc_q_power`.
  - A quest with **no tasks** counts as complete once its dependencies are.
- **Applies to:** both.
- **Status:** live on the VM since the 2026-11-08 01:14 restart. Uploaded together with "2026-11-08 — Quest book: fixes and additions"; the file list is in its Status.
- **Revert:** see "2026-11-08 — Quest book: fixes and additions" (one rollback covers both entries).

### 2026-11-08 — Quest book: fixes and additions

- **Change:**
  - **Quests that couldn't be finished:**
    - **The Final Question ran backwards.** "The Departure Records" (the four flight recorders) came first, but the recorders are space loot, and the Launch recorder was only in three Ad Astra tables that no structure places (`common_moon`, `common_other`, `rare_moon`; checked by scanning every structure file in the pack). New order: The Skyward Project → Following Their Path → The Departure Records → The Answer. In `oc_loot.js`:
      - the Launch recorder moved to the moon tables that are placed (`ad_astra:chests/dungeon/moon/dungeon_chest`, `village/moon/house`, `village/moon/blacksmith`; 10%);
      - Transit moved to the moon dungeon chest and `ad_astra_more_structures:chests/common_mars`;
      - the Unstable Memory's `rare_moon` slot moved to the moon dungeon chest.
    - **The Skyward Project** is the chapter's first quest. It also depends on the quests behind each NASA Workbench part (`AdAstraSpaceGate`): Brass and Precision, A Familiar Design, Storage Is Not Enough, and a new Enchanter quest (teleport scroll). It still depends on The Missing Years and Unidentified Contact. Its text names the four parts.
    - **A Familiar Design** asked for a Digital Miner, which `MekanismProgression` makes endgame (atomic alloy, ultimate control circuit, Robit, teleportation cores). It asks for a Crusher; the Digital Miner moved to Distant Hands.
    - **The four residue quests** (It Wears Faces, The One Below, The Breathless, It Was Not There Before) only paid out on a kill, but these horrors rarely die: CANTBREATHE gives up a chase after 60 s, and the Mimic and the Cave Dweller after 30 s. New `kubejs/server_scripts/oc_encounters.js` gives the residue when the horror leaves without being killed:
      - the encounter starts when one of the four comes within 24 blocks of a survival player;
      - it pays out if the horror then disappears without being killed and the player hasn't died meanwhile;
      - the player gets the residue and a one-line message;
      - a kill still drops the residue as before (`oc_interactions.js`);
      - open encounters are kept in memory only, so a restart forgets them.
    - **The First Survey gave a waystone** as the second quest, which skipped `WaystonesGate` (warp stones need an Ender Eye). The waystone moved to a new Nether quest, "Through the Portal".
  - **Text and rewards brought up to date:**
    - **Breathless Residue:** Fog Residue was renamed (display name and tooltip only; the item ID stays `oldcivilisation:fog_residue`). The residue comes from CANTBREATHE.
    - **The Answer** lists what it waits for. Before, these conditions were invisible:
      - the three evidence kinds (technical, magical, exodus) and 12 kinds of evidence, which are the conditions of the Correction Fragment grant in `oc_interactions.js`;
      - the assembled HS-LGTFO Mission Data.
    - **Blank books replaced:** three rewards promised a fragment and gave a blank Book and Quill. Automation gives an Incident Report Fragment, The Silent Nights a Familiar Photograph, and Unidentified Contact a Containment Label.
    - **The Unacceptable Solution** has no task of its own. It completes on any one of three new sub-quests: The Split Atom (fission reactor port), A Star in a Box (fusion controller), or The Reinforced Core (Extreme Reactors).
    - **Unidentified Contact:** holding a Biological Sample **or** an Impossible Bone Fragment (stage `oc_q_contact`), not only the sample.
    - **The Watchers:** "No claimed ground" (there's no claim mod) became "Nowhere near a bed, a colony…".
    - **The Second Civilisation:** People Return and Secure The Future stay hidden until the opening quests of two of the four paths are done.
    - **A Moving World** no longer gives a second backpack (A World Left Behind already gives one).
    - **AE2 Drive:** `ae2:drive` moved from Storage Is Not Enough to Networks.
    - **Residue objectives** read "Survive an encounter, or end one."
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
  - **Fix after going live (2026-11-08):** `oc_encounters.js` stopped on its first player tick with *Cannot find function getUUID*, so the survive-an-encounter residues never paid out. Cause: this KubeJS/Rhino exposes `Entity#getUUID` as `getUuid()` (also `uuid` and `stringUuid`). A probe on the scratch test server confirmed it, and also confirmed that the script's other calls (`isRemoved()`, `getRemovalReason()`, which reports e.g. `DISCARDED`, and `getEntities(entity, aabb)`) work as written.
  - **Also deleted:** `config/ftbquests/quests/chapters/broken_world.snbt.bak`, which git ignores and which is identical to `broken_world.snbt` in commit `85ada22`.
- **Reason:** a review of the quest book against the mods and scripts added since 2026-07. Some quests couldn't be finished as written, some text no longer matched the pack, and the newer mods had no quests.
- **Mechanism:** progress is kept.
  - Task IDs moved with their tasks: the Crusher keeps the Digital Miner's ID, the Drive and the fusion controller keep theirs, and Unidentified Contact keeps its task ID.
  - FTB Quests records completion per quest, so a quest that was already complete stays complete when it gains a task.
- **Verification:**
  - **Checked offline:** every quest parses; all 714 IDs are unique, 16-hex and every dependency resolves; every item, advancement, dimension, entity, reward and icon exists in the pack; every stage is one a script grants; every `#oldcivilisation:` structure tag exists.
  - **On the scratch test server:**
    - FTB Quests loaded the chapters with no errors and didn't rewrite them;
    - all server scripts loaded, including `oc_encounters.js`, and the sanity horror set has 12 IDs; `kubejs errors startup` found nothing;
    - `locate` found `#oldcivilisation:nether_strongholds` (a Better Fortresses fortress, 440 blocks) and `dungeons_arise:jungle_tree_house` (2,012 blocks).
  - **On the VM after the 2026-11-08 01:14 restart:** the log shows *Loaded 9 chapters, 133 quests*, and all KubeJS server and startup scripts loaded with 0 errors. `oc_quest_checks.js` granted its first mirrored tag (`oc_q_evidence_magic`) at 01:24.
  - **Not tested:** anything that needs a player, such as the encounter payout, the mirrored tags and the quest screens. Check those in game.
- **Applies to:** both. Players see the Breathless Residue name and tooltip (`oc_items.js`, `oc_tooltips.js`) only after the pack update; until then, the old name shows on their side.
- **Status:** live on the VM since the 2026-11-08 01:14 restart, together with "2026-11-08 — Quest book: real checks instead of click-to-complete". 21 files were uploaded for the two entries, each read back identical:
  - the nine chapter files;
  - the `QuestChecks` pack (new);
  - `OldCivSanity`'s `horror_mobs.json`;
  - `kubejs/server_scripts/` `oc_quest_checks.js` (new), `oc_encounters.js` (new), `oc_sanity.js` and `oc_loot.js`;
  - `kubejs/startup_scripts/` `oc_items.js` and `oc_quest_spellcast.js` (new).
  - Before the upload, every VM file that was replaced matched a committed version, so there were no VM-only edits to lose.
  - The `oc_encounters.js` fix was uploaded after the 01:14 restart and read back identical. Live at the next VM restart, or after an op runs `/reload`.
- **Revert:** the replaced files were moved to `files/server/deploy-backups/2026-11-08-quests/`, under the same relative paths. To roll back, move them back and delete the new files. The broken `oc_encounters.js` copy is in `files/server/deploy-backups/2026-11-08-encounters-fix/`.

### 2026-11-08 — Ultimine Shapes+ added; FTB Ultimine max_blocks 64 → 81

- **Change:**
  - **Mod:** Ultimine Shapes+ 1.0.1, `ultimine-shapes-plus-forge-1.20.1-1.0.1.jar` (mod id `ultimineshapes`, MIT). It adds mining shapes to FTB Ultimine, defined as JSON files. It requires FTB Ultimine exactly 2001.1.8 (the pack's version), FTB Library 2001.2.1+ and Architectury 9.2.14+, all already in the pack. It is not on the sync script's client-only list.
  - **FTB Ultimine `max_blocks` 64 (its default) → 81:**
    - VM: `world/serverconfig/ftbultimine-server.snbt`.
    - New worlds: `defaultconfigs/ftbultimine/ftbultimine-server.snbt` sets `max_blocks: 81` (it was empty, so the defaults applied).
    - Not changed: the singleplayer worlds in the client instance's `saves/` keep 64.
  - `tools/generate_modlist.py`: an `ultimineshapes` rule (Storage, QoL & utility). FTB Ultimine itself lands in Quests, loot, data & guides through the `ftb` rule.
- **Reason:** Design: more FTB Ultimine shapes, defined as JSON. `max_blocks`: Flatten 9×9 is 81 blocks, so at 64 it stopped short on each use. 81 is the smallest value that completes it; it also lets vein mining (ores, trees) take up to 81 blocks. The hunger cost (`exhaustion_per_block` 20) is unchanged.
- **Mechanism:**
  - **Shapes:** loaded at startup from `config/ultimineshapes/shapes/*.json` (files up to 2 MiB, sorted by id; duplicates are skipped). On first start it installs three examples once: Flatten 3×3, 5×5 and 9×9 (`"type": "plane"`), marked by `.examples-installed`, so deleted examples stay deleted.
  - **Both sides:** the server holds each player's selected shape. The client only sends "next/previous shape" (`ModeChangedPacket`), and the server cycles its own shape list and sends the index back (`SendShapePacket`). So the server needs the jar and the same shape files, or the new shapes don't exist there. The mod has no network channel of its own, so a client without it can still join.
  - FTB Ultimine reads `ftbultimine-server.snbt` at startup.
- **Verification:** sync dry run against the VM: 308 active − 24 client-only = 284, and the only change was `copy + ultimine-shapes-plus-forge-1.20.1-1.0.1.jar`.
- **Applies to:** both. `max_blocks` in existing worlds: only where the world's `serverconfig/ftbultimine-server.snbt` was changed (the VM).
- **Status:** uploaded to the VM 2026-11-08, after the 01:14 restart: the jar with the sync (284 jars, parity verified), and `ftbultimine-server.snbt`, read back identical (the old file is `ftbultimine-server.snbt.bak-20261108-maxblocks`). Live at the next VM restart: the jar, the three example shapes (created on the server's first start with the jar) and the block limit.
- **Maintenance:** a new or edited file in `config/ultimineshapes/shapes/` has to be copied to the VM's `config/ultimineshapes/shapes/` as well, and both sides restarted, so the two lists stay the same.
- **Revert:** remove the jar on both sides; `max_blocks` back to 64 (the VM's previous file is `ftbultimine-server.snbt.bak-20261108-maxblocks`).
- **Related:** "2026-11-08 — spooky_ultimine_shapes 1.0.0: face-oriented 2x2, 5x5 and 5x3 shapes".

### 2026-11-08 — spooky_ultimine_shapes 1.0.0: face-oriented 2x2, 5x5 and 5x3 shapes

- **Change:** a new local mod, `spooky_ultimine_shapes-1.0.0.jar`. Source, build and README are in `tools/spooky_ultimine_shapes/`; the jar is in `mods/` and `local/`. It adds **Square (2x2)**, **Square (5x5)** and **Rectangle (5x3)** (5 wide, 3 tall). Each is one layer laid flat on the face of the block hit, so it turns with the player. `tools/generate_modlist.py`: `spooky_ultimine_shapes` added to the Local Fixes rule.
- **Reason:** Design: face-oriented 2x2, 5x5 and 5x3 shapes. The 3x3 already exists: FTB Ultimine's own **Small Square (3x3)** is exactly 9 blocks, one layer, facing the side hit. **Large Tunnel (3x3)** is the shape that keeps going deeper (up to `max_blocks`).
- **Mechanism:**
  - **Layout:**
    - Odd sizes are centred on the hit block.
    - Even sizes put the extra block to the player's right, forward on floors and ceilings, and towards eye level on walls. A 2x2 aimed at either the head-height or the foot-height block clears the same 2-tall walkway.
    - Order: nearest blocks are taken first.
    - Matching: the same rules as FTB's shaped modes (`merge_tags_shaped`, default any block). Air is skipped.
    - Block limit: fits inside `max_blocks` 81.
  - **Shape order:** FTB Ultimine links client and server shape lists by index. This mod registers in the parallel part of common setup, after FTB's constructor-registered built-ins (0–5) and before Ultimine Shapes+'s `enqueueWork` (Flatten, 9–11). That gives the same order on every start. A scan of every jar in `mods/` found no other mod that registers shapes. The log line `[spooky_ultimine_shapes] registered 3 shapes at indices 6..8` confirms the order.
- **Rejected:** Ultimine Shapes+ JSON. Its offsets are fixed to world axes, and its `wall` type is always along X or Z and never turns to face the side hit, so it can't make shapes that face the player.
- **Verification:** the shape layout offline: 14 cases (every wall direction, floor, ceiling, and head- and foot-height aims), all pass. Sync dry run against the VM: 309 active − 24 client-only = 285, and the only change was `copy + spooky_ultimine_shapes-1.0.0.jar`. Not tested in game.
- **Applies to:** both; on the VM from the next restart. A client without the jar can still join, because the mod has no network channel, but it shows the wrong names from index 6 onward, including the Flatten shapes. The server must not run it while players' packs lack it, so the server and the players' pack update together.
- **Status:** enabled on the client instance and the VM (2026-11-08). Uploaded to the VM with `scripts/sync_server_mods.py --yes`; parity verified (285 jars; the VM file is 6,183 bytes, and a sync dry run reports *nothing to do*). Live at the next VM restart. Players need the pack update with the jar.
- **Maintenance:** pinned to FTB Ultimine 2001.1.8; after an FTB Ultimine update, see `tools/spooky_ultimine_shapes/README.md` (Build, deploy and revert).
- **Revert:** delete the jar from `mods/` on both sides. To hold it back without deleting it, rename it to `.jar.disabled` on both sides (it is a local build, so the CurseForge app won't re-enable it).

### 2026-11-08 — JEI: colour search on, building-block palette

- **Change:**
  - `config/jei/jei-client.ini`: `colorSearchMode` DISABLED → **REQUIRE_PREFIX**. Colours are searched only with `^`, so a plain search for "red" still searches names only.
  - `config/jei/jei-colors.ini`: JEI's 35 default colours were replaced with 50 anchors under 46 names:
    - **Families:** Black, Grey, White, Brown, Beige, Tan, Red, Orange, Yellow, Green, Teal, Cyan, Blue, Purple, Magenta and Pink.
    - **Shades:** `Dark…`, `Light…`, `Bright…` and `Pale…`.
    - **In-between colours carry both family names:** BlueGrey, GreenGrey, BrownGrey, RedBrown, OliveGreen and RustOrange, so they turn up under either family.
    - **Spelling:** British "Grey", like the rest of the pack. `^gray` finds nothing.
    - **Extra anchors:** Grey has 3, DarkGrey and Brown 2 each. Without them, plain dark greys such as deepslate tiles came out as DarkGreen or BrownGrey.
- **Reason:** finding blocks that suit a build's colour scheme. With 30 decoration mods, searching by name doesn't help. JEI's own list is tuned for bright item icons, and building blocks are mostly muted. Against the pack's blocks, the default list put:
  - oak and jungle planks under "BrickRed";
  - prismarine under "Gray, Silver" and purpur under "Gray, Pink";
  - moss under "Brown" and nether bricks under "NetherBrick", which `^red` doesn't find.
- **Mechanism:** Source: JEI 15.56.0.205 jar.
  - **Where the colours come from:** the particle texture of the block's default state (or the item model), multiplied by the block's tint. JEI takes the 2 dominant colours (`ColorThief`, a median cut).
  - **How a colour gets its name:** JEI picks the nearest entry in `config/jei/jei-colors.ini`. The distance is `ColorUtil.slowPerceptualColorDistanceSquared`: the "redmean" RGB distance plus a penalty when the two colours differ in saturation, so greys keep to greys.
  - **Duplicate names are allowed:** the palette is a plain list, so one name can have several anchors.
  - **Matching:** colour names are matched as substrings. `^red` therefore finds DarkRed, BrickRed, RedBrown and the rest.
  - **Tooltip:** while colour search is on, the JEI panel's tooltip shows each item's colour names ("Colors: DarkGrey, Grey").
  - **Search examples:** `^darkred` finds one shade. `^grey brick` finds grey blocks with "brick" in the name. `^teal|^cyan` means either. `^grey -^light` leaves out the light greys.
  - JEI rewrites its config files on launch but keeps these values.
- **Verification:** offline. JEI's colour step (particle texture → 2-colour median cut → nearest name by JEI's exact distance) was rebuilt in Python and run over the 25,526 block items whose models resolve to a texture (Minecraft plus every enabled jar).
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
- **Applies to:** the client instance. Both files are client config, with nothing on the server. Players get them with the pack update.
- **Status:** client instance only (2026-11-08).
- **Maintenance:** load cost. JEI reads every ingredient's texture when it builds its search index on world join. Before the change: "Building ingredient filter took 1.642 seconds" (log, 2026-11-08 01:44). Compare that line after the next launch.
- **Revert:** set `colorSearchMode = DISABLED`. To get JEI's own palette back, delete the `searchColors` line from `jei-colors.ini`, and JEI writes its default again.

### 2026-11-08 — spooky_tick_fixes 1.5.0: The Obsessed and CANTBREATHE sanity-bound

- **Change:** `spooky_tick_fixes` 1.5.0 adds `[sanityHorror]` (in `config/spooky_tick_fixes-common.toml`): three rules applied to both mods. Full table: `tools/spooky_tick_fixes/README.md` (1.5.0).
  - **Critical sanity** = the band `oc_sanity.js` stores, at or past `criticalBand` (3, Breakdown: entered below 15, left at 20). It's read from KubeJS's own persistent data (`KubeJSPersistentData`), not Forge's. **Base** = within 48 blocks (±32 y) of any online player's respawn point, or inside a MineColonies colony's claim (±32 y of its centre).
  - **The Obsessed:** a targeter appears only for a critical player outside a base (its timer is held just short of zero otherwise, so one comes 10–60 s into a breakdown if it was due), and locks on only when a critical player **looks at it** (the Enderman stare test plus a clear line of sight). Stalks happen only while the target is critical (retried every ~20 s otherwise); a stalk spot inside a base is moved out of it. The behind-you/spider hallucinations and the block-break surprise don't happen inside a base, and the surprise also needs critical sanity. Unchanged: the lurker at the player's windows and sleep paralysis (both need an existing obsession).
  - **CANTBREATHE:** its timer still runs on everyone's time (the hourly cap stays). A spawn goes ahead only for a critical victim, and victims are picked among critical players first, so leaving Breakdown makes the next spawn attempt fail and the mod re-picks. Its cave noise plays only for a critical victim, and only critical players get it. Spawn spots inside a base are rejected. The "any player within 15 blocks" chase is off; looking at it or hitting it still provokes it.
  - **Gap:** The Obsessed's intruder form can still walk into a base after spawning outside one.
- **Reason:** Design: The Obsessed is sanity-bound (critical sanity enables spawning); its first contact needs the player to look at it, rather than it seeing the player; its stalks never spawn in a base; the window lurker and the sleep-paralysis hallucination are kept. CANTBREATHE gets the same three rules.
- **Mechanism:** how each mod schedules things (Source: bytecode reads, 2026-11-08):
  - **The Obsessed** has no biome spawns.
    - A per-player targeter timer (3 min in the pack's config) places a "targeter" 40–80 blocks away, at light below 9, behind the player.
    - Unpatched, it locks on to the nearest player it has line of sight to (not sneaking), and that player becomes the target.
    - From then on a stalk timer spawns it 37–70 blocks away, day or night, anywhere in the Overworld, while the player faces away.
    - Its gap is `rand − min(K, Stalk Frequency)` by stage. The pack's 2000 doesn't slow the intense/extreme stages, because K is capped at 1800.
    - The countdown runs at quarter speed except during the world's first night: the mod checks absolute day time.
  - **CANTBREATHE** (Cave Dweller code):
    - It keeps one Overworld timer, and keeps the same victim across cycles.
    - A victim stops qualifying only when they die, log off, leave the Overworld or switch to creative/spectator (the pack's config passes every height, light and biome), or when a spawn attempt fails. The mod then picks a random qualifying player.
- **Verification:** compiles against the installed jars (javac, 67 sources). Every injection target (owner, name, descriptor and call-site count) was checked against the class files: 9 of 9 match. Not boot-tested; a headless boot is required before deploying (any mixin error stops startup with the patch named). Not tested in game: everything that needs a player.
  - To check in game: `/sanity <player>` and `/tickfixes horror` show each player's band, base status and Obsessed state. At Breakdown in the dark, a targeter should appear within a minute or so and lock on only once the player looks at it. Stalks should then stop when the player recovers, and never appear within 48 blocks of a bed.
- **Applies to:** the VM; the server can run it alone. On clients it only adds singleplayer behaviour.
- **Status:** built, not installed (2026-11-08): the jar is `tools/spooky_tick_fixes/build/spooky_tick_fixes-1.5.0.jar`. Not deployed; not boot-tested. Installed on the client instance and the VM: 1.4.0.
  - The `[sanityHorror]` section is not in `config/spooky_tick_fixes-common.toml`: a running 1.4.0 could strip unknown keys on a reload. Forge adds it with the defaults on the first 1.5.0 start.
  - To install and deploy: `python tools/spooky_tick_fixes/build.py --install` (replaces 1.4.0 in `mods/` and `local/`; not while the game holds the old jar open), then a headless boot check, then `python scripts/sync_server_mods.py` and a VM restart.
- **Revert:** the six switches in `[sanityHorror]`, or the 1.4.0 jar.
- **Related:**
  - `docs/HORROR_REVIEW_2026-11-06.md` §5.3 proposed removing The Obsessed. `[sanityHorror]` is the change made instead (built, not installed).
  - The Baseline "The Obsessed" section sets `Stalk Frequency` 2000 and `Natural Targeter Spawnrate` 1200.
  - "2026-11-07 — Antlers Wendigo: keeps away, attacks only when provoked (spooky_tick_fixes 1.4.0)" (the CANTBREATHE cave-noise finding, corrected 2026-11-08).

### 2026-11-08 — Waystones: warping costs no XP

- **Change:** `config/waystones-common.toml` `[xpCost]`: `minimumBaseXpCost` 1.0 → **0.0**, `maximumBaseXpCost` 3.0 → **0.0** (the mod's own "disable all distance-based XP costs"), `dimensionalWarpXpCost` 3 → **0**. `xpCostPerLeashed` was already 0.
- **Reason:** Design: Waystone warping costs no XP. The Eye of Ender recipe gate on the Warp Stone (`WaystonesGate`), activation by visiting, and cooldowns are unchanged.
- **Mechanism:** cost = round((base + leashed animals × `xpCostPerLeashed`) × the warp mode's multiplier × `globalWaystoneXpCostMultiplier` for a global waystone). Creative players pay nothing.
  - Base, between dimensions: `dimensionalWarpXpCost`, at least `minimumBaseXpCost`.
  - Base, otherwise: distance ÷ `blocksPerXpLevel`, clamped between `minimumBaseXpCost` and `maximumBaseXpCost`.
  - With a base of 0, every warp mode is free: waystone to waystone, Warp Stone, sharestone, portstone, warp plate, inventory button, and global waystones. The multipliers (all 1.0) are left as they are; they have nothing to multiply.
  - Source: `PlayerWaystoneManager#getExperienceLevelCost`, Waystones 14.1.21.
- **Applies to:** both; in multiplayer the VM's values apply. Players get the client file with the pack update.
- **Status:** client instance (2026-11-08). VM: not applied; the VM's file matches the old repo copy. To apply: back it up, upload `config/waystones-common.toml` to `files/server/config/`, and restart (Forge config edits don't hot-reload on the VM).
- **Revert:** set the three values back (1.0 / 3.0 / 3).
- **Related:** the Mahou Tsukai displacement cap (256 blocks, Baseline "Mahou Tsukai") cited XP-costed Waystones as its reason. That reason no longer holds; the cap is unchanged.

### 2026-11-08 — Sanity: slower recovery

- **Change:** `kubejs/server_scripts/oc_sanity.js`, `CONFIG.restore`:
  - **Rates:** daylight +0.8 → **+0.4**, firelight +0.5 → **+0.2**, sleep +8.0 → **+5.0**.
  - **No stacking:** the larger of daylight and firelight applies.
  - **Firelight cap:** firelight only restores up to **70** (new `firelightCap`). Getting back to Calm (75) needs
    daylight, sleep or a clarity item. A lit base at night holds a recovering player in Unease, which brings a faint
    ambient cue every ~10 min.
  - **Settle delay:** passive recovery only starts **30 s** (`settleTicks` 600) after the last acute drain: darkness,
    or a horror within 16 blocks. The time is stored in the player's `oc_sanity_last_drain`. Depth doesn't reset the
    clock, so a lit cave at Y −40 nets −0.4 per interval (was −0.1). Sleep and the clarity items (sedative +30,
    photograph +12) are exempt and unchanged.
  - **Unchanged:** all drains, the bands and their hysteresis, and the event pools.
- **Reason:** recovery outpaced the drain. Breakdown could be left in ~10 s outside, or ~24 s by a torch.
- **Mechanism:** KubeJS's `getLight()` is the brightest light at the player's position, so open daylight (15) also
  counted as firelight. Standing outside by day stacked both restores: +1.3 per 2 s interval (39 per minute), more
  than twice the darkness drain (18 per minute). Any torch-lit spot healed too, including lit caves above Y 0.
  Calculated effect of the change:
  - leaving Breakdown takes ~55 s outside by day and ~80 s by torchlight;
  - Breakdown → Calm takes ~5.5 min outside, and torchlight stops at 70;
  - a night's sleep gives about +12.
- **Verification:** `node --check` parses the script. The variables in the tick callback are all `var` (the Rhino rule
  in `docs/sanity_mechanic.md` §4). Not tested in game; `CONFIG.debug = true` logs `dark/lit/daylight/settled` per
  evaluation.
- **Applies to:** server scripts; in multiplayer the VM's copy applies.
- **Status:** client instance (2026-11-08). VM: not applied; the VM's `oc_sanity.js` matches the previous repo copy.
  To apply: upload `kubejs/server_scripts/oc_sanity.js` to `files/server/kubejs/server_scripts/`, then `/reload` or
  restart.
- **Revert:** set `CONFIG.restore` back to `{ firelight: 0.5, daylight: 0.8, sleep: 8.0 }`, and set
  `firelightCap: 100` and `settleTicks: 0`. The no-stacking rule then still applies; the previous behaviour summed the
  two.
- **Related:** `docs/sanity_mechanic.md` §5.2 (values, reasoning, times). The longer Breakdown windows give the
  `spooky_tick_fixes` 1.5.0 `[sanityHorror]` gate time to act; once both are live, The Obsessed and CANTBREATHE are
  expected to appear more often during breakdowns.

---

## Appendix A — OpenLoader packs

Packs in the client instance and the repository, as of 2026-11-08:

- `config/openloader/data/`: `MekanismProgression`, `AE2Progression`, `AdAstraSpaceGate`, `OccultismExpensiveRituals`, `WaystonesGate`, `SecurityCraftBalance`, `WeepingAngelsBuff`, `MineColoniesSlowResearch`, `HorrorSpawnRarity` (extended 2026-11-01: John Mod Reborn + Creeping Woods), `ExtremeReactorsProgression`, `BornInChaosNoInfectedOre` (2026-11-02), `HorrorQuietAdvancements` (2026-11-06), `IncendiumClockTrim` (2026-11-06; pins Incendium 5.3.5's `clocks/main`), `WendigoTrophies` (2026-11-07; Wendigo loot), `QuestChecks` (2026-11-08; structure and biome tags for the quest book), `PerfFixes`, `AntiLagEntities`, `OldCivSanity`.
- `config/openloader/resources/`: `QuietAmbience`.

Retired or deleted packs:

- `TFCModdedFood` (data): retired 2026-07-12. Its `#forge:foods` tag reference never loaded; superseded by `kubejs/server_scripts/tfc_vanilla_food.js`. The folder was parked in `config/openloader/data-disabled/`, then deleted from the client instance and the repo on 2026-11-07.
- `DayLength30` (data): retired 2026-11-01. Its tick function never compiled under TFC's `/time` override, and its failure dropped the whole `minecraft:tick` tag. Day length is Simple Day Length Extender's config. The folder is parked in `config/openloader/data-disabled/DayLength30/`.
- `TFCFoodHunger`, `TFCLightItems`, `TFCVanillaCraftingTable` (data) and `TFCNoSizeTooltip`, `TFCHideInventoryTabs` (resources; `TFCHideInventoryTabs` added 2026-11-01): deleted from the client instance and the repo on 2026-11-07, with TFC disabled on both sides. The VM still has the three data packs `TFCFoodHunger`, `TFCLightItems` and `TFCVanillaCraftingTable`. Without TFC they do nothing: the first two hold only `tfc:` data, and the third re-states vanilla's own crafting-table recipe.

## Appendix B — Known crash: TerraFirmaCraft × Serene Seasons freeze mixin

- **Crash:** a startup **Mixin injection failure**. TerraFirmaCraft's `BiomeMixin` (`shouldFreezeWithClimate` redirect) fails because **Serene Seasons** (and Cold Sweat's freezing mixin) rewrite the same vanilla biome freeze/precipitation methods.
- **Cause:** a mod-vs-mod incompatibility at class-load, before any config or datapack is read. It is not caused by the pack's config changes, and data edits cannot cause or fix it. The fix is to run only one of the conflicting climate mods.
- **History:**
  - 2026-07: Serene Seasons and Cold Sweat were disabled/removed, which resolved the conflict; TerraFirmaCraft was then the only climate mixin on those methods.
  - 2026-11-01: TerraFirmaCraft and its dependants were disabled, and Serene Seasons was re-added. TFC's `BiomeMixin` no longer loads.
- **Status:** the conflict cannot occur (2026-11-01 onward). Do not re-enable TFC alongside Serene Seasons.

## Appendix C — Reverting

- **Config-file edits:** `*.bak` backups sit next to the originals in `config/` (local only; `*.bak` is git-ignored): `alexsmobs.toml.bak`, `coldsweat/world.toml.bak`, `serversidehorror.json.bak`, `man_config.toml.bak`, `ars_nouveau.bak/`, `alexscaves_biome_generation.bak/`, `extremereactors/common.toml.bak`, `bloodmagic-common.toml.bak`.
- **Datapacks:** delete the pack folder under `config/openloader/data/` (or `config/openloader/resources/`).
- **Per-world defaults:** `defaultconfigs/*.toml`. To re-apply one to an existing world, delete that world's `serverconfig/<mod>-server.toml` so it regenerates.
