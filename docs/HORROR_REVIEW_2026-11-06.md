# Horror review — 2026-11-06

**Scope:** every horror and horror-adjacent mod in the pack, reviewed against player feedback, the lore, the pack's existing tuning rules and the server's performance limits. For each mod: what it actually does (read from its jar where it mattered), where it can appear, and a proposed configuration.
**Summary:** As reviewed on 2026-11-06, most horror mobs can appear on the Overworld surface; only Cave Dweller is cave-only. Five mobs (Creeping Woods, Rake's stalker, Baby John, The Hunted, Man From The Fog) check for open sky in their own spawn code, so they can only be kept or turned off. One KubeJS spawn check can make any biome-spawned mob cave-only; the timer stalkers summon themselves and need config instead. Born in Chaos runs seasonal events by the real-world calendar. Man From The Fog was disabled; everything else is proposed in three tiers: config and datapacks, one KubeJS rule, and jar removals.
**Status reviewed:** 2026-11-08

| # | Finding | Proposal | Status |
|---|---|---|---|
| 1 | Man From The Fog is surface horror by construction (§4.2) | §5.0 disable; §5.3 remove the jar | Applied: CONFIG_CHANGES "2026-11-06 — The Man From The Fog: disabled" and "2026-11-06 — The Man From The Fog jar disabled; second client instance deleted" (jar `.jar.disabled` on the client instance and the VM, 2026-11-06 evening). `config/man_config.toml` deleted 2026-11-07 with the mod disabled (CONFIG_CHANGES "2026-11-07 — Repository cleanup: personal files purged from history, orphaned configs removed") |
| 2 | Born in Chaos runs real-calendar seasonal events (§4.4) | §5.1 row 1 | Open |
| 3 | The timer stalkers can spawn on the surface, and surface time banks their timer (§4.3) | §5.1 rows 2–3 | Open |
| 4 | CANTBREATHE's cave noise plays every 12–22 min once its spawn timer is past half, audible about 32 blocks around its victim (§3, corrected 2026-11-08) | `only_play_noise_to_target = true` (§3) | Open. `spooky_tick_fixes` 1.5.0 `[sanityHorror]` plays it only for a Breakdown-sanity victim and only to Breakdown-sanity players: CONFIG_CHANGES "2026-11-08 — spooky_tick_fixes 1.5.0: The Obsessed and CANTBREATHE sanity-bound" (built, not installed) |
| 5 | Creeping Woods' visible apparitions (§4.1) | §5.1 row 4 | Open |
| 6 | Rake's stalker, and its sound emitter at weight 15 (§4.1) | §5.1 row 5 | Open |
| 7 | John Mod's sky-only variants (§4.1) | §5.1 row 6 | Open |
| 8 | Weeping Angels' three surface biomes (§4.7) | §5.1 row 7 | Open |
| 9 | Server-Side Horror: loud cues, streamer names, traps (§4.5) | §5.1 row 8 | Open. Traps: open |
| 10 | Horror Element bodies: surface clutter, and `self_terminated_body` (§4.6) | §5.1 row 9 | Open |
| 11 | More cave horror is available from installed Alex's Caves content (§4.7) | §5.1 row 10 (optional) | Open |
| 12 | The sanity horror tag lists `man:*` (§5.1) | §5.1 row 11 | Applied: CONFIG_CHANGES "2026-11-08 — Quest book: fixes and additions" (`oc_sanity.js` `HORROR_IDS` and the `horror_mobs` tag lost `man:*` and gained `cant_breathe:cant_breathe`) |
| 13 | Biome-spawned mobs reach the surface: Whispering Spirits, John, Wendigo, Born in Chaos, Foes' gloom (§4.1) | §5.2 cave-only rule | Open decision: the cave-only tag with Born in Chaos, or without it |
| 14 | The Wendigo spawns on any non-stone floor in the Overworld (§4.1) | §5.2 (in the tag) | Open. Reworked separately on 2026-11-07 after it attacked players without warning: it keeps away from players, attacks only a player who provokes it, and despawns like other monsters (`spooky_tick_fixes` 1.4.0, `[antlersWendigo]`); its roar and scream carry 128 blocks instead of 1,600 and 640; it has loot (`WendigoTrophies`). CONFIG_CHANGES "2026-11-07 — Antlers Wendigo: keeps away, attacks only when provoked (spooky_tick_fixes 1.4.0)" and "2026-11-07 — Wendigo loot: WendigoTrophies datapack" |
| 15 | The One Who Watches: surface Observers, a hanging variant that never despawns, the worst horror mod for lag (§4.7) | §5.3 remove | Open |
| 16 | The Obsessed: base and surface horror, 4–8% of the server thread (§4.7) | §5.3 remove | Rejected: superseded 2026-11-08 by `spooky_tick_fixes` 1.5.0 `[sanityHorror]`, which binds it to Breakdown sanity and keeps it out of bases (CONFIG_CHANGES "2026-11-08 — spooky_tick_fixes 1.5.0: The Obsessed and CANTBREATHE sanity-bound"; built, not installed) |
| 17 | Caves, the Nether and the End have no horror beyond installed content (§5.4) | §5.4 optional additions | Open |
| 18 | The installed Cave Dweller Reimagined is unsupported since 2023 (§5.4) | Swap to Cave Dweller Evolved | Rejected: it would lose Reimagined's models and sounds |
| 19 | Docs name Man From The Fog and "Knocker" (§7) | §7 | `ftbquest.md`: Unidentified Contact no longer lists either (2026-11-08, commit `a2189bd`). `thelore.md` still names Man From The Fog as the example Observer: Open (2026-11-08) |

Related: `thelore.md`, `blurb.md`, `PACK_DIRECTION_REVIEW_2026-07-08.md`, `ftbquest.md` (Horror Investigation path), `sanity_mechanic.md`, `CONFIG_CHANGES.md` (every horror section), `SERVER_TICK_AUDIT_2026-11-06.md`.

---

## 1. Brief and overview

**The brief** (player feedback, 2026-11-06):

> "Honestly reckon a lot of the spooky shit could be heavily tuned down. We're out of season. Be cool for some cave shit and nether / end, but I reckon above land in overworld is probably negligible to have. Ambient spooky."

**It fits the pack.** The lore already puts the horrors underground: *"Deep beneath the earth, strange creatures dwell in places untouched by sunlight"* (`blurb.md`), and the planned quest "Below" says *"The oldest warnings concern what exists beneath the surface"* (`ftbquest.md`). The sanity design already treats daylight under open sky as safe and depth as draining (`sanity_mechanic.md` §5).

**Key findings:**
1. **Most horror mobs can appear on the surface (2026-11-06).** Only Cave Dweller is cave-only. Weeping Angels is mostly caves and the End.
2. **A third of the horror mobs can never go underground.** Creeping Woods, Rake's stalker, Baby John, The Hunted and Man From The Fog check for open sky in their own spawn code. They can only be kept or turned off.
3. **"Out of season" is literal.** Born in Chaos runs a **Halloween event from 25 October to 7 November by the server's real clock**, and a "Chilling Horror" event over Christmas and New Year. Both can be switched off with gamerules.
4. **Man From The Fog was a surface-only mod.** Its spawn timer only runs while a player is near the surface, and it announced itself with a chat advancement. **Disabled** (§5.0).
5. **One KubeJS rule can make any biome-spawned mob cave-only.** It hooks the spawn check that `oc_sanity.js` already uses.
6. **The One Who Watches and The Obsessed should go, not move.** Both are surface or base horror by design, and both are named lag sources in the tick audit. (Superseded 2026-11-08 for The Obsessed: see §5.3.)

**Proposed end state:**

| Where | What players meet |
|---|---|
| **Overworld surface, day and night** | Ambience only: Server-Side Horror's quiet events, Door Knocker, Creeping Woods' and Rake's invisible sound emitters, dead-animal bodies, darkness. Vanilla night mobs and Foes' regional wildlife. |
| **Caves** | The four timer stalkers (Cave Dweller deepest), Weeping Angels in eerie cave biomes, Whispering Spirits, John, Wendigo, Born in Chaos, Alex's Caves and Deeper and Darker biomes. |
| **Nether** | Weeping Angels in soul sand valley, Whispering Spirits, Born in Chaos and Foes nether mobs. Optional new mods in §5.4. |
| **End** | Weeping Angels in every End biome, Whispering Spirits. Optional new mods in §5.4. |

---

## 2. Constraints this review works within

- **Rarity rule:** the pack's benchmark for psychological horror is spawn weight 2, solitary. "Rare > frequent" (`PACK_DIRECTION_REVIEW_2026-07-08.md` §11: *"Do not make the horrors stronger."*). The 2026-11-01 pass added the twin rule: *rarer, not weaker*. Nothing below changes a mob's stats.
- **Ambience over gimmicks:** Herobrine, fake joins and sign spam were removed on purpose (`CONFIG_CHANGES.md`, Server-Side Horror tuning pass).
- **Lore roles** (`thelore.md`): Observers watch, Imitations copy, Residuals are places, the Altered are changed life. The horrors are *evidence*, not enemies.
- **Performance:** the server is a Proxmox VM on a 2013 Xeon. Lag scales with *living entities × per-entity mod overhead* (`SERVER_TICK_AUDIT`). Ambient events are cheap. New mob packs are not. Moving spawns to caves doesn't add mobs, because hostile mobs share one spawn cap.

---

## 3. How this was investigated

- **Docs:** read the lore, design and quest documents in full, and the horror, sanity and audit sections of the rest.
- **Spawn rules:** read each mob's natural-spawn condition from its jar with `javap`. Names were decoded with the Mojang and MCP mapping files in the CurseForge install. This covered the MCreator `*NaturalEntitySpawningConditionProcedure` classes and Born in Chaos's own procedures.
- **Behaviour:** read in code where it mattered: Man From The Fog's timer and events, the Cave Dweller timer, Born in Chaos's gamerules and seasonal date checks, Horror Element's worldgen, and Server-Side Horror's structures.
- **Configs:** read every horror mod's config, plus the Server v2 world's `serverconfig/` (as of 2026-11-06).
- **New mods:** web research for cave, Nether and End horror on Forge 1.20.1.

In the game's code, "open sky" means full sky light at the position. That's never true in the Nether or the End, and never true under leaves.

**Limits:** nothing proposed here has been run in game. The Mimic, Pale Hound and CANTBREATHE use the Cave Dweller config layout and are assumed to share its timer code. Only Cave Dweller's code was read. (Corrected 2026-11-07: Pale Hound and CANTBREATHE do share Cave Dweller's timer code, `serverTick`, `handleLogic` and `playCaveSoundToSpelunkers`, read from bytecode. The Mimic is unchecked.) The KubeJS block property in §5.2 is marked *[verify live]*.
- **Cave noise:** one timer per dimension sends a cave noise to every qualifying player on the same tick, positioned at the chosen victim and audible about 32 blocks around them. CANTBREATHE's config (any height, any light, surface allowed, all biomes) qualifies every survival player in the Overworld, every 12–22 minutes once its spawn timer is past half, so players near its victim hear it. `only_play_noise_to_target = true` in `cant_breathe-server.toml` would limit it to the chosen victim. (Corrected 2026-11-08: this bullet, added 2026-11-07, said each player heard a random clip at their own position, which made the noise server-wide. The clip is positioned at the victim.)

---

## 4. Findings, mod by mod

### 4.1 Where each mob can spawn (2026-11-06)

| Mod / mob | How it spawns | Surface? | Caves? | Nether/End? | Current tuning |
|---|---|---|---|---|---|
| **Cave Dweller** (Reimagined 1.6.4) | Timer: exactly 1 h on 85% of cycles, otherwise 2–4 h (Corrected 2026-11-08: was "2–4 h"; §4.3) | No | Y ≤ 0 only | No | Already the model for "cave horror" |
| **The Mimic** | Timer: exactly 1 h on 85% of cycles, otherwise 1–2 h, assuming the shared timer code (Corrected 2026-11-08: was "1–2 h"; §4.3) | Yes | Yes | No | `allow_surface_spawn = true` |
| **Pale Hound** | Timer: exactly 1 h on 85% of cycles, otherwise 1–2 h (Corrected 2026-11-08: was "1–2 h"; §4.3) | 5 remote biomes | Yes | No | `allow_surface_spawn = true` |
| **CANTBREATHE** | Timer: exactly 1 h on 85% of cycles, otherwise 1–2 h (Corrected 2026-11-08: was "1–2 h"; §4.3) | Any biome | Yes | No | `allow_surface_spawn = true` |
| **Man From The Fog** | Surface timer + hanging variant | **Only** | No | No | **Disabled 2026-11-06** |
| **Weeping Angels** | Biome list, weight 2–3 | Dark forest, 2 Terralith moonlight biomes | Dripstone, deep dark, Terralith and Alex's Caves cave biomes | Soul sand valley, all End | Curated list |
| **The One Who Watches** | Biome, weight 2–30 | Yes (watchers need high open ground) | Hanging variant: anywhere without sky | No | `toww_hanging` w30 never despawns |
| **Rake** | Biome, forests | Code requires sky | No | No | Stalkers w2; sound emitter w15 |
| **Creeping Woods** | Biome, forests | Code requires sky, Y > 60 | No | No | All w2 |
| **John Mod** | Biome, any | John: yes. Baby John and The Hunted need sky | John only | No (Overworld check) | w1–2 |
| **Wendigo** (Antlers) | Biome, any Overworld biome, not on `#forge:stone` | Yes | Only on non-stone floors (gravel, dirt, moss, calcite…) | No (its spawn rule needs an Overworld biome) | w2, one per 1,000 blocks. Since 2026-11-07 provoke-only and despawns (status table, row 14) |
| **Whispering Spirits** | Biome, any | Yes | Yes | Yes | w2 |
| **The Obsessed** | Targets a player wherever they are | Yes | Yes | — | Griefing, stealing and building on |
| **Born in Chaos** (44 mobs) | Biome, any | 29 mobs: dark surface (light ≤ 4) | Same 29 in pitch-black caves; spiders cave-only | A few Nether mobs | Mod weights; seasonal events on |
| **Foes** | Regional biomes | Mostly, two need sky | Some | Two Nether mobs | Mod weights |

### 4.2 Man From The Fog (disabled 2026-11-06)

Read from the 1.4 jar:
- **Spawn timer:** one world-wide timer that each player advances by one per tick **while within 2 blocks of the surface**. More players outside means more frequent spawns. He was summoned 35–99 blocks away, day or night.
- **Announcement:** granted `man:feelingwatched` with a toast and a chat line, which told everyone he'd arrived.
- **Chase:** looking at him triggered a chase. During it he applied Darkness, climbed, broke blocks, called lightning, prevented sleep within 200 blocks and ended a kill with a jumpscare.
- **Hanging variant:** weight 10 in every biome, under tree canopy at night.
- **Extras:** a cultist house structure, a Cultist villager job, and on first join per world a Ko-fi chat advert and a cassette tape. The advert isn't configurable.

He is surface horror by construction, so disabling him fits the brief better than tuning.

### 4.3 The timer stalkers (Cave Dweller family)

Cave Dweller keeps **one spawn timer per dimension**, built around a randomly chosen player. Two consequences:
- **Encounters are per server, not per player.** One cycle, one stalker, near one player.
- **`surface_timer_multiplier` stretches the timer while the chosen player is on the surface** (range 0–5). At 1.0, surface time counts fully. A cave-only stalker would then bank its timer during surface play and spawn soon after someone enters a cave. At **5.0**, surface time counts at about a fifth.
`allow_surface_spawn` controls whether the spawn itself may be on the surface. The Mimic, Pale Hound and CANTBREATHE expose the same keys.

**The cooldown replaces the range.** (Corrected 2026-11-08: on each reset, with probability `can_spawn_cooldown_chance` the timer is set to the fixed `can_spawn_cooldown` instead of a random value between `can_spawn_min` and `can_spawn_max`. All four stalkers' configs use 0.85 and 3600 s, so 85% of cycles are exactly 1 h, whatever their min/max. For the Mimic this assumes the shared timer code, which is unchecked; §3.)

### 4.4 Born in Chaos: seasonal events and gamerules

- **Seasonal events** compare against the server's real calendar:
  - `halloweenEvent`: 25 October to 7 November by the real-world date, a window that includes the review date.
  - `chillingHorrorEvent`: around Christmas and New Year.
  - `chaoticSpringEvent`: mid-April.
  - A `seasonalEvents` rule also exists.
- **Spawn gamerules:** every mob has its own, e.g. `zombieClownSpawn`, `krampusSpawn`, `serPumpkinheadSpawn`, `decayingZombieSpawn`. There are also mechanics rules such as `naughtinessMechanics`, `maggotsAppearance` and `soulStratificationEffect`. Gamerules are per world and persist in `level.dat`.
- **Spawn rule:** 29 mobs use "open sky and light ≤ 4, or no sky and light 0, not deep dark, not mushroom fields". So they already populate pitch-black caves.

### 4.5 Server-Side Horror

**Kept as ambience:** `fake_steps` (rare but present), `fake_mining`, torch breaking and replacing (all "pretty rare"), `random_heads`, `starer` (incredibly rare).

Three things don't fit "ambient":
- **`scary_sound_list` is mostly loud cues:** TNT priming, explosions, creeper hiss, ghast scream, polar bear warning, dragon fireball. These are jump scares, not ambience.
- **`heads_from_list` and `starer_list` name real streamers** (DanTDM, Projekt_M and others). A fake player named DanTDM staring from a hill is the same kind of meme horror as the Herobrine events the pack already removed.
- **`traps` and `setting_up_new_traps`** place pre-built trap structures, some with TNT minecarts.

### 4.6 Horror Element (worldgen bodies)

- **Placement:** five body blocks are placed as "ore" features that replace **grass and mushrooms**, 3–4 attempts per chunk, Y 0–128, with no rarity filter. In practice that means surface clutter in forests, jungles, swamps and plains.
- **Content:** one feature is `self_terminated_body`, which depicts a suicide.
- **Scope:** changes affect newly generated chunks only.

### 4.7 The rest

- **The One Who Watches:**
  - Its watchers need the column's surface at Y ≥ 70 and world "phase" variables.
  - The hanging variant spawns wherever there's no open sky and never despawns.
  - Follow range is 2,048 blocks.
  - It also places campsites, tents, a burned building and a sacrifice site.
  - The tick audit ranks it the worst horror mod for lag.
- **The Obsessed:**
  - Targets one player.
  - Steals items from chests, builds structures that copy the player's, steals horses, plants wither roses and breaks glass and leaves.
  - Costs 4–8% of the server thread from player-variable syncing, regardless of whether it is active.
- **Whispering Spirits:** a pure random roll, so it already appears in caves, the Nether and the End. Its look-at check costs 1.3–3.4% of the server thread per player.
- **Weeping Angels:** surface biomes are dark forest and Terralith's Moonlight Grove and Moonlight Valley. Everything else in its list is a cave, the End or soul sand valley.
- **Door Knocker:** knocks on doors near the player every 20–80 minutes. It's base ambience, cheap and on-brief.
- **Atmosphere:** Hardcore True Darkness, Cave Dust and Void Fog (client), AmbientSounds with `QuietAmbience`, Sounds of the Forest. All on-brief.
- **Cave content already installed:**
  - Alex's Caves: Forlorn Hollows, with Watchers that possess players, and Abyssal Chasm.
  - Deeper and Darker: the Otherside.
  - The 2026-07 pass made Alex's Caves biomes about 4× rarer, and 600–975 blocks from spawn.

---

## 5. Proposed configuration

### 5.0 Applied: Man From The Fog disabled

- **Timer spawn:** `config/man_config.toml` `enable_spawning` **true → false**.
- **Hanging variant:** `config/openloader/data/HorrorSpawnRarity/data/man/forge/biome_modifier/mftfhang_biome_modifier.json` is replaced with `{"type": "forge:none"}`. The config switch doesn't cover it.
- **Kept:** the `HorrorQuietAdvancements` pack, which silences "Feeling Watched", is harmless and still useful if he is ever turned back on.
- **Existing worlds:** already-spawned hanging Men stay until removed with `/kill @e[type=man:mftfhang]` in loaded areas.
- **Not changed:** his cultist house, Cultist job, and first-join advert and cassette still exist. Removing the jar (§5.3) is the only way to drop those. (Corrected 2026-11-08: the jar was disabled on 2026-11-06 (evening), on the client instance and the VM, which drops these too. CONFIG_CHANGES "2026-11-06 — The Man From The Fog jar disabled; second client instance deleted".)

### 5.1 Tier 1: config and datapack, no code

| # | Change | File(s) | Effect |
|---|---|---|---|
| 1 | Born in Chaos seasonal events off: `/gamerule halloweenEvent false`, `chillingHorrorEvent false`, `chaoticSpringEvent false`, `seasonalEvents false` | Per world: run on the VM and in each singleplayer world | No calendar events. Halloween would end on its own on 7 November, but Chilling Horror starts around Christmas. |
| 2 | Timer stalkers cave-only: Mimic, Pale Hound and CANTBREATHE set `allow_surface_spawn = false`, `surface_timer_multiplier = 5.0`, `can_spawn_min/max = 7200 / 14400` | `defaultconfigs/themimic_er-server.toml`, `pale_hound-server.toml`, `cant_breathe-server.toml`, and **the VM world's `serverconfig/`** | Three stalkers move underground at Cave Dweller's 2–4 h cadence. (Corrected 2026-11-08: with `can_spawn_cooldown_chance` 0.85 and `can_spawn_cooldown` 3600, 85% of cycles stay at exactly 1 h whatever the min/max; §4.3.) |
| 3 | Cave Dweller `surface_timer_multiplier` 1.0 → **5.0** | `defaultconfigs/cave_dweller-server.toml` + VM world | No backlog from surface time. |
| 4 | Creeping Woods visible entities off: replace the 15 existing entity overrides with `{"type": "forge:none"}` and add one for `human_1`. **Keep** `ambient_sound` at weight 2. | `HorrorSpawnRarity/data/creepingwoods/forge/biome_modifier/{creature_from_the_woods_hanging,creature_from_the_woods_idle,cursed_figure,cursed_goat,demon_cloud,figure,filmer,ghost_girl,ghost_player,ghostly_anomaly,glowing_eyes,hallucination,human_1,murder,quick_light,stalking_face}_biome_modifier.json` | Forests stay eerie through sound. No apparitions. |
| 5 | Rake stalker off, sound kept: `rake_spawn_trigger`, `rake_stalking`, `rake_following` → `forge:none`. **Add** `ambient_sound` at weight 2, copying the biome list from the jar. | `HorrorSpawnRarity/data/rake_the_arrival/forge/biome_modifier/` | Same as Creeping Woods. Also closes the weight-15 gap the tick audit found. |
| 6 | John Mod sky-only variants off: `baby_john`, `baby_john_pack_lead`, `the_hunted` → `forge:none` | `HorrorSpawnRarity/data/john_mod_reborn/forge/biome_modifier/` | Adult John stays and goes cave-only via §5.2. |
| 7 | Weeping Angels surface biomes off: `canSpawnHere = false` for `minecraft:dark_forest`, `terralith:moonlight_grove`, `terralith:moonlight_valley` | `config/weeping_angels_spawns.json` | Angels only in caves, the End and soul sand valley. *Optional:* one more Nether biome, e.g. `minecraft:basalt_deltas`, at weight 2, solitary. |
| 8 | Server-Side Horror, ambient only: replace `scary_sound_list` with quiet cues, e.g. `minecraft:ambient.cave`, `minecraft:block.sculk_sensor.clicking`, `minecraft:block.wooden_door.close`, `minecraft:block.chest.close`, `minecraft:entity.warden.heartbeat`, `minecraft:block.gravel.break`. Set `heads_from_list_enable = false`. Make `starer_list` neutral or set `starer_enable = false`. | `config/serversidehorror.json` | Keeps the "something is interfering" layer without jump scares or streamer names. Traps (`traps`, `setting_up_new_traps`) unchanged: open. |
| 9 | Horror Element thinned: insert `{"type": "minecraft:rarity_filter", "chance": 16}` before `in_square` in each placed feature, and set `self_terminated_body` to `forge:none` | New datapack paths `data/horror_element_mod/worldgen/placed_feature/*.json` and `data/horror_element_mod/forge/biome_modifier/self_terminated_body_biome_modifier.json` | Bodies become rare finds, matching the Antlers fix. New chunks only. |
| 10 | *Optional:* more cave horror from installed content: Alex's Caves Forlorn Hollows `distance_from_spawn` 975 → 650 and `cave_biome_mean_separation` 1800 → 1500 | `config/alexscaves_biome_generation/forlorn_hollows.json`, `config/alexscaves-general.toml` | More Watchers and gloom underground. New chunks only. Partly reverses a deliberate July choice. |
| 11 | Housekeeping: remove `man:*` and any removed mods from the sanity horror tag. The Jade hide list can stay. | `config/openloader/data/OldCivSanity/.../entity_types/horror_mobs.json` | Keeps the sanity proximity drain accurate. |

**Expected cave cadence after rows 2 and 3:** four stalkers, each roughly once per 2–4 hours of underground play. Together that's about one encounter every 30–60 minutes of cave time, shared across the server. If that's too much, raise the three to `10800 / 21600`. (Corrected 2026-11-08: the min/max values apply only on the 15% of cycles where the cooldown doesn't; all four stalkers use `can_spawn_cooldown_chance` 0.85 with `can_spawn_cooldown` 3600, so most cycles are exactly 1 h; §4.3.)

### 5.2 Tier 2: one KubeJS rule for cave-only spawns

A new tag `oldcivilisation:cave_only`, and a check that cancels **natural** Overworld spawns of tagged mobs wherever the block gets any sky light. Caves get none. The Nether and End get none anywhere, so they're untouched.

- **Tag contents (proposed):**
  - `whispering_spirits:whispering_spirit`
  - `john_mod_reborn:john`
  - `antlers:wendigo`
  - *optionally* all `born_in_chaos_v1:*` land mobs, and `foes:gloom`
- **Born in Chaos is the big decision.** Cave-only Born in Chaos removes most of the "spooky" from surface nights and leaves vanilla mobs there. Its mobs already spawn in pitch-black caves, so caves just get a larger share. If caves get crowded, use its per-mob gamerules (§4.4) to drop the sillier ones, such as `zombieClownSpawn` and the pumpkin mobs.
- **Where:** a small `kubejs/server_scripts/oc_cave_horror.js`, or a second check inside `oc_sanity.js`'s existing `EntityEvents.checkSpawn` handler.

Sketch, following `oc_sanity.js`'s idioms:

```js
EntityEvents.checkSpawn(event => {
  var deny = false
  try {
    var ent = event.entity
    if (!ent || !ent.type) return
    if (!CAVE_ONLY_SET['' + ent.type]) return                 // built from the tag or a list
    if ('' + ent.level.dimension !== 'minecraft:overworld') return
    var reason = '' + event.type                              // natural spawns only
    if (reason.toUpperCase().indexOf('NATURAL') < 0) return
    if (ent.block.skyLight > 0) deny = true                   // [verify live] BlockContainerJS sky light
  } catch (e) { return }
  if (deny) event.cancel()  // outside the try: cancel() throws EventExit, which must propagate
})
```

- **Cost:** one tag lookup per spawn attempt.
- **Doesn't cover timer stalkers:** they summon themselves and never reach `checkSpawn` (`sanity_mechanic.md` §8.2), which is why they're handled by config in §5.1.

### 5.3 Tier 3: removals

Jar removals must happen on **every client and the server at once**, or players are refused at login. Existing entities of a removed mod are dropped harmlessly when chunks load.

- **The One Who Watches: remove.** Its watchers are surface Observers that need high open ground. The hanging variant accumulates forever, and the mod has no config. Disabling all 11 of its spawn files with `forge:none` is the fallback if the jar must stay, but its player-tick code would still run. Its campsites and buildings stop generating in new chunks.
- **The Obsessed: remove.** It's base and surface horror: chest theft, copied buildings, horse theft. Its server-thread cost is per player whether or not it's stalking. If it stays, set these to `false`:
  - `Allow Obsessed Griefing`
  - `Allow Item Stealing`
  - `Allow Building Structures`
  - `Horse Stealing`
  - `Splash Text`

  (Superseded 2026-11-08: The Obsessed stays, bound to sanity by `spooky_tick_fixes` 1.5.0 `[sanityHorror]`, built and not installed. It comes only for a player at Breakdown sanity and never appears inside a base (within 48 blocks of a respawn point, or in a MineColonies colony), and its targeter locks on only when the player looks at it. Its window lurker and sleep paralysis are kept. CANTBREATHE gets the same sanity and base rules, and no longer chases whoever comes within 15 blocks. CONFIG_CHANGES "2026-11-08 — spooky_tick_fixes 1.5.0: The Obsessed and CANTBREATHE sanity-bound".)
- **Man From The Fog: optional removal later.** The config switch already stops him. Removing the jar would also drop the cultist house, the Cultist job and the first-join advert. (Applied 2026-11-06, evening: the jar is `.jar.disabled` on the client instance and the VM. CONFIG_CHANGES "2026-11-06 — The Man From The Fog jar disabled; second client instance deleted".)

### 5.4 Optional additions for caves, Nether and End

All exist for Forge 1.20.1. None has been tested with this pack, DimThreads or Immersive Portals. Prefer the ambient ones, which cost almost nothing on the server.

| Mod | What it adds | Fit |
|---|---|---|
| [Presence](https://modrinth.com/mod/presence-an-ambient-horror-mod) | Ambient dread: no loud sounds, no sudden deaths. Animals get scared and drawn toward "something". The config sets frequency, duration and what appears. Client and server. | Best match for "ambient spooky". Unknown whether it can be limited to underground. |
| [Enderman Overhaul](https://modrinth.com/mod/enderman-overhaul) 1.0.4 | Hostile Cave Enderman (underground below sea level, dripstone, lush caves; teleports away from light). Hostile Soulsand Valley Enderman. Hostile End variants. | Covers caves, Nether and End in one mod. Its neutral surface variants would need trimming. |
| [The Nether Stalker](https://www.curseforge.com/minecraft/mc-mods/the-nether-stalker) | An 8-block-tall Nether stalker that paralyses on sight. Needs GeckoLib. | Nether horror, but aggressive. Tune to the rarity rule before adding. |
| [The Undergarden](https://www.curseforge.com/minecraft/mc-mods/the-undergarden) 0.8.14 | A dark dimension beneath bedrock, with its own portal. | Fits Cave Dweller's planned "Deep Echo" lore item (*"Below known geological limits"*). It's a big content addition. |
| [End's Phantasm](https://www.curseforge.com/minecraft/mc-mods/phantasm), [Unusual End](https://www.curseforge.com/minecraft/mc-mods/unusual-end) | End worldgen and mood. Unusual End adds Endstone Trappers. | Not horror. No real End horror mod turned up. |

Not recommended: swapping Cave Dweller. The installed one is "Cave Dweller Reimagined", unsupported since 2023. The newer [Cave Dweller Evolved](https://www.curseforge.com/minecraft/mc-mods/cave-dweller-evolved) 1.7.0 shares its code but would lose Reimagined's models and sounds.

---

## 6. Rollout

| Location | What to apply |
|---|---|
| **The client instance (source of truth)** | Everything in §5.1–5.2. `defaultconfigs/` seeds new worlds only. |
| **The VM** (the live server) | `config/` and OpenLoader packs copied over. Timer edits (§5.1 rows 2–3) in **its world's** `serverconfig/`. Gamerules (row 1) run once in game. Restart. |
| **`Servers/Server v2`** (local test bed, not live) | Optional: same as the VM. |
| **Jar removals** (§5.3) | All clients and servers in the same update. Then `scripts/sync_server_mods.py` and `tools/generate_server_modlist.py`. |

Worldgen changes (rows 9 and 10, and The One Who Watches' structures) only affect chunks generated afterwards.

## 7. Docs to update once applied

- `thelore.md` lists Man From The Fog as the example Observer. Pick another, such as Cave Dweller's noises or Whispering Spirits, or keep him as a historical record.
- `ftbquest.md`:
  - "Unidentified Contact" lists Man From The Fog and "Knocker". Only Door Knocker, the ambient event, is installed.
  - "The Forest Does Not Forget" asks players to visit Creeping Woods, which would become sound-only.
- `CONFIG_CHANGES.md`: one dated entry per applied tier.

## 8. Sources

- Jar reads: `mods/*.jar` bytecode and data files, decoded with `Install/libraries/.../client-1.20.1-20230612.114412-mappings.txt` and `mcp_config-1.20.1-20230612.114412-mappings-merged.txt`.
- [Presence](https://modrinth.com/mod/presence-an-ambient-horror-mod) · [Enderman Overhaul](https://modrinth.com/mod/enderman-overhaul) · [The Nether Stalker](https://www.curseforge.com/minecraft/mc-mods/the-nether-stalker) · [The Undergarden](https://www.curseforge.com/minecraft/mc-mods/the-undergarden) · [End's Phantasm](https://www.curseforge.com/minecraft/mc-mods/phantasm) · [Unusual End](https://www.curseforge.com/minecraft/mc-mods/unusual-end) · [Cave Dweller Reimagined](https://www.curseforge.com/minecraft/mc-mods/cave-dweller-reimagined) · [Cave Dweller Evolved](https://www.curseforge.com/minecraft/mc-mods/cave-dweller-evolved)
