# Mod-update review — 2026-11-01

**Scope:** the CurseForge update of 2026-11-01 on the client instance (about 80 mods updated, 13 jars removed),
checked against the docs in `docs/` (source of truth), the pre-update git state, the July server
(`Servers/spookydookie`) and the older pack exports under `CurseForge/mc/`. Also covers the consistency pass of
2026-11-01 (§2.9) and Immersive Portals × DimThreads (2026-11-02, §2.10). The July server was not changed.
**Summary:** The "void" world was Chunk Sending 4.1, whose send queue never drained in this pack; it was rolled back
to 2.8, and Hardcore True Darkness v6, which blacked out the view with shaders on, was rolled back to 4.0.0.
TerraFirmaCraft's world generation was never active in any world, while its mechanics and two silent defaults
contradicted the docs; TFC was removed on 2026-11-01. The `DayLength30` datapack never loaded under TFC's `/time`
command, which also dropped the Weeping Angels buff tick; day length is Simple Day Length Extender's config. The
update reset or bypassed tuning in MineColonies, Weeping Angels, Server-Side Horror and Door Knocker, all restored on
the client instance. Server v2 was built to match the client on 2026-11-02; it has been a local test bed since the VM
became the live server on 2026-11-06.
**Status reviewed:** 2026-11-08

| # | Finding | Proposal | Status |
|---|---|---|---|
| 1 | Chunk Sending 4.1 never sends chunks to the client: the "void" world (§2.1) | Roll back to 2.8 | Applied: CONFIG_CHANGES "2026-11-01 — Mod-update review: about 80 mods updated, 13 jars removed". `mods/` holds `chunksending-1.20.1-2.8.jar` (2026-11-08) |
| 2 | Hardcore True Darkness v6 blacks out the view with shaders on (§2.2) | Roll back to 4.0.0 | Applied: CONFIG_CHANGES "2026-11-01 — Mod-update review: about 80 mods updated, 13 jars removed". `mods/` holds `hardcoretruedarkness-4.0.0.jar` (2026-11-08) |
| 3 | Farsight 5.8 rewrites the client chunk path; not the cause of the void (§2.1) | Keep the known-good 5.1 | Applied: CONFIG_CHANGES "2026-11-01 — Mod-update review: about 80 mods updated, 13 jars removed". `mods/` holds `farsight-1.20.1-5.1.jar` (2026-11-08) |
| 4 | CurseForge re-enables `.disabled` jars on a profile rescan (§2.3) | Keep rolled-back jars outside `mods/`; pin versions in the app | Applied: CONFIG_CHANGES "2026-11-01 — Mod-update review: about 80 mods updated, 13 jars removed". The 4.1, 5.8 and v6 jars are in `local/mods-rolled-back-2026-11-01/` (2026-11-08) |
| 5 | TFC world generation was never active; its mechanics contradict the docs (§2.4) | Remove TFC and its dependants | Applied: CONFIG_CHANGES "2026-11-02 — Mod set changes, 2026-11-01 to 2026-11-02" |
| 6 | TFC denied nether portals and surface vanilla monsters (§2.4) | `enableNetherPortals`, `enableVanillaMonstersOnSurface` → true | Applied: CONFIG_CHANGES "2026-11-01 — Mod-update review: about 80 mods updated, 13 jars removed". Moot since TFC's removal; `tfc-server.toml` deleted 2026-11-07 |
| 7 | TFC leftovers are inert but still load (§2.4) | Retire them | Applied in part: CONFIG_CHANGES "2026-11-07 — Repository cleanup: personal files purged from history, orphaned configs removed". Open (2026-11-08): `kubejs/server_scripts/tfc_vanilla_food.js`; the three TFC jars, still `.jar.disabled` in `mods/`; the VM's three TFC data packs |
| 8 | Worlds created while TFC was installed keep its forced game rules (§2.4) | §3.7 | Open (last recorded 2026-11-02) |
| 9 | `DayLength30` never loaded, and took `wa_buff:tick` down with it (§2.5) | Retire it; day length from Simple Day Length Extender | Applied: CONFIG_CHANGES "2026-11-01 — Mod-update review: about 80 mods updated, 13 jars removed" |
| 10 | Day-length variant with a longer night (§2.5) | §3.8 | Open |
| 11 | The update reset or bypassed tuning, and broke a quest (§2.6) | Restore the documented values | Applied: CONFIG_CHANGES "2026-11-01 — Mod-update review: about 80 mods updated, 13 jars removed". MineColonies `raidDifficulty` 8 → 5 on 2026-11-04 by the replacement config (CONFIG_CHANGES "2026-11-04 — MineColonies: server config replaced") |
| 12 | Warp Stone gate bypass; `oc_sanity.js` debug logging left on (§2.7) | Fix both | Applied: CONFIG_CHANGES "2026-11-01 — Mod-update review: about 80 mods updated, 13 jars removed" |
| 13 | Configs the July docs never covered (§2.9) | Consistency pass | Applied: CONFIG_CHANGES "2026-11-01 — Lore and config consistency pass" |
| 14 | Immersive Portals' chunk-ticket flush fails under DimThreads: a 1.3 GB log (§2.10) | `immptl_dimthread_compat` shim | Applied: CONFIG_CHANGES "2026-11-02 — Immersive Portals × DimThreads: compat shim". Retired 2026-11-06 with DimThreads: CONFIG_CHANGES "2026-11-06 — DimThreads disabled" |
| 15 | Crash Assistant 1.11.12 sends upload metadata to a third-party server (§3.1) | `send_uploaded_logs_data_to_kostromdan_dev = false` | Rejected: it sends only when a player presses "upload"; left `true` (CONFIG_CHANGES "2026-11-01 — Lore and config consistency pass") |
| 16 | Lore-loot coverage gaps in `oc_loot.js` (§3.2) | Routing pass | Open: none of the listed tables is routed (2026-11-08) |
| 17 | EntityCulling 1.11 added `safeMode = true` (§3.3) | Check its FPS cost in game | Open: `safeMode` is still `true` (2026-11-08) |
| 18 | Lore and design docs out of step with the mod set (§3.4) | Update them | `ftbquest.md`: updated 2026-11-08 (commit `a2189bd`). `thelore.md` and `implore_implementation_plan.md`: Open (2026-11-08). The Forgotten section: marked historical in CONFIG_CHANGES "2026-11-01 — Mod-update review: about 80 mods updated, 13 jars removed" |
| 19 | Quest-book gaps against the design doc (§3.5) | Quest-book polish | Applied: CONFIG_CHANGES "2026-11-08 — Quest book: fixes and additions" |
| 20 | Singleplayer view and simulation distance 64 / 64 (§3.6) | 12–16 render, 8–10 simulation | Open |
| 21 | Artifacts Night Vision Goggles are ordinary loot (§3.6) | Artifacts gamerules; `artifactRarity` 2–3 | Open |
| 22 | Iron's Spellbooks and Ars Nouveau teleports are uncosted (§3.6) | Per-spell config; `enableWarpPortals = false` | Open. Related: Waystone warping costs no XP since 2026-11-08 (CONFIG_CHANGES "2026-11-08 — Waystones: warping costs no XP") |
| 23 | Compass searches are free (§3.6) | `defaultXpLevels` 3–5 | Open. Its rationale, "every teleport costs XP", no longer matches Waystones since 2026-11-08 (CONFIG_CHANGES "2026-11-08 — Waystones: warping costs no XP") |
| 24 | Comforts sleeping bags skip every night (§3.6) | `restrictSleeping` or `sleepingBagBreakChance` | Open |
| 25 | The server needed the client's update (§3.9) | Server update checklist | Applied: CONFIG_CHANGES "2026-11-02 — Server v2: dedicated server built to match the client" |

---

## 1. Method and limits

- **Worlds:** the new world's region file was parsed (`saves/New World (2)/region/r.-1.0.mca`), and the overworld
  generator was read from `level.dat` in every world in `saves/` and in the `simplebackups/` zips.
- **Logs:** the client's `logs/latest.log` of 2026-11-01 and `2026-07-12-6.log.gz`.
- **The void:** bisected in game with F3 and Chunk Sending's debug logging.
- **Jars:** read where behaviour mattered (for example TFC 3.2.28's inventory-tab code, Farsight 5.8's hooks, and
  Immersive Portals 3.0.7 and DimThreads 1.2.1 with `javap`). All 17 OpenLoader packs' override paths were checked
  programmatically against the updated jars.
- **Comparison:** the July server's `mods/`, `config/` and `defaultconfigs/`.
- **Limits:** client instance only; the July server was not changed. The server-side checks were headless boots on
  2026-11-02 (§2.10, §3.9).

---

## 2. Findings

### 2.1 The "void" world: Chunk Sending 4.1

**Symptom:** the new world (`saves/New World (2)`, created 19:51) showed only void. The world itself is fine on the
server side:

| Evidence | Where |
|---|---|
| Spawn chunk `[-15, 19]` is `minecraft:full`, 24 sections, grass/dirt/stone/ores, Terralith cave biomes | `region/r.-1.0.mca` (parsed) |
| 1018 generated chunks in that region file after a 4-minute session | `region/r.-1.0.mca` |
| Player stood at y = 71 for 30 s with sky light 15 (not falling) | `oc_sanity.js` DIAG lines in `logs/latest.log` |
| Xaero's World Map wrote ~800 KB of map cache for this world at 19:56 | `xaero/world-map/New World (2)/null/cache_1/*.xwmc.outdated` — **not evidence of delivery**: in singleplayer Xaero reads the save's region files directly |

**Cause:** Chunk Sending 2.8 → 4.1. F3 showed the client holding **0 loaded chunks** while the integrated server had
thousands and climbing: chunk packets never reached the client. With debug logging on, 4.1's per-tick line claimed
"Sent: 15 packets" every tick, yet its own queue only grew (426 → 1487 in 76 s, roughly the generation rate) and the
client logged zero "Ignoring chunk" drops, so the server-side queue never drained. The 4.1 rewrite moved the send
path onto the player tick, which under DimThreads runs on the `dimthread_server_overworld` thread.

**Fix:** `chunksending-1.20.1-2.8.jar` restored, July's known-good pairing with the same DimThreads build. The queue
went 392 → 0 within seconds and terrain appeared. Confirmed in game 2026-11-01 21:40 (§2.2).

**Eliminated:** TFC (the void persisted with TFC disabled), Hardcore True Darkness v6 (persisted with it disabled),
Farsight 5.8 (persisted after rolling it back to 5.1), worldgen (chunks were always on disk).

**Farsight** stays on the known-good 5.1. 5.8 adds a new cancellable HEAD hook on
`ClientPacketListener.updateLevelChunk`; it was not the cause. The client's Chunk Sending 2.8 and Farsight 5.1 jars
are byte-identical to the July server's.

### 2.2 Shaders-on black screen: Hardcore True Darkness v6

**Symptom:** with Oculus + Hysteria enabled, the view at (-49, 109, -34) was pure black; with shaders off it was fine.

**Cause:** the only new component on the shader path since July was Hardcore True Darkness v6. It runs a vanilla
post-process chain after level rendering (`DarknessFloor` 0.018, `DarknessStrength` default 1.0, `DaylightPreserve`
default 0), which under Oculus crushes the whole frame. v6 is also a hard protocol gate (clients without the matching
jar are disconnected), with a 10 s join timeout that kicks slow clients.

**Fix:** `hardcoretruedarkness-4.0.0.jar` (July's version, from `Servers/spookydookie/mods/`) restored in place of
`hardcore_true_darkness-6.jar`. 4.0.0 has a different mod id and no server gate, so the v6-only
`defaultconfigs/hardcore_true_darkness-server.toml` is not in the pack. Pin 4.0.0 in CurseForge.

**Verification:** in game, 2026-11-01 21:40: chunks load and the shaders-on view is no longer black.

### 2.3 CurseForge re-enables `.disabled` jars

A `.disabled` rename is not a pin in a CurseForge-managed instance: the app re-enables `.disabled` jars it believes
are installed when it rescans the profile. Rolled-back jars therefore live outside `mods/`, in
`local/mods-rolled-back-2026-11-01/` (`chunksending-1.20.1-4.1.jar`, `farsight-1.20.1-5.8.jar`,
`hardcore_true_darkness-6.jar`), so only one jar per mod exists in `mods/`. The app adopts the remaining jar by
fingerprint (it did so for Hardcore True Darkness 4.0.0).

Source: at 21:28 on 2026-11-01 a profile rescan turned `chunksending-1.20.1-4.1.jar.disabled` back into `.jar`, Forge
loaded 4.1 over 2.8, and the void returned.

**Maintenance:** in the CurseForge app, Chunk Sending, Farsight and Hardcore True Darkness should show 2.8 / 5.1 /
4.0.0; "Change version" pins one that shows the newer file. Profile auto-update is off. The server side is pinned to
the same three versions (`docs/SERVER_MODS.md`); the parked jars are never synced to it.

### 2.4 TerraFirmaCraft

**World generation was never active.** Every world on disk uses the vanilla noise generator with the
vanilla/Terralith multi-noise biome source:

| World | Overworld generator | Source |
|---|---|---|
| New World, New World (1), New World (2) | `minecraft:noise` / `minecraft:multi_noise` / `minecraft:overworld` | `saves/*/level.dat` |
| A kinder, Bazinga, Bruh, Boom-era "New World" backups | same | `simplebackups/*.zip` |

TFC's preset (`tfc:overworld` generator + `tfc:overworld` biome source, from
`data/tfc/worldgen/world_preset/overworld.json`) was never chosen, even though `tfc-common.toml` set it as the
default. The spawn chunk is vanilla stone/deepslate with Terralith cave biomes and Mekanism/IE/Create ores, with no
TFC rock layer anywhere.

TFC's generator replaces the biome source, so Terralith, Alex's Caves and most YUNG's and Dungeons Arise structures
(which key off vanilla biome tags) would not generate in a TFC world, and vanilla and modded ores would not either.
The pack's documented worldgen set ("TerraFirmaCraft, Terralith, Incendium, Alex's Caves, YUNG's…") works only because
TFC's generator is not in use.

**Its mechanics were all active.** TFC provided none of the worldgen the pack wants and all of the mechanics the pack
kept working around: the HUD bars, the Calendar/Nutrition/Climate tabs, food/thirst/decay, item sizes and weights,
collapses, forced game rules, and two silent defaults that contradicted the docs:

- `enableNetherPortals = false`: the Waystones gate (Eye of Ender), the Occultism ritual costs (blaze, ghast tear,
  wither skull) and three installed nether worldgen mods all assume the Nether is reachable. Set to true (instance
  defaults and the live world).
- `enableVanillaMonstersOnSurface = false`: TFC denied every vanilla monster spawn on the surface. CONFIG_CHANGES had
  blamed the empty nights on The Forgotten's limiter; that mod is gone, and this was the remaining suppressor. Set to
  true (instance defaults and the live world).

Also set: `enableInfestations = false` (rat infestations on stored food) and `enableOverburdening = false` (the direct
toggle for what `TFCLightItems` worked around).

**Decision (2026-11-01): TerraFirmaCraft removed**, with its dependants `mcw_tfc_aio-0.2.30` and
`TFC_CarryOnStorage1.2.0` (CONFIG_CHANGES "2026-11-02 — Mod set changes, 2026-11-01 to 2026-11-02"). Effects:

- Vanilla hunger and natural regeneration return by themselves (the docs' end goal). No day-length impact (§2.5).
- Nether portals, bonemeal on vanilla crops, wandering traders and pillager patrols come back (TFC's
  `enableForcedTFCGameRules` was switching the last two off).
- Existing worlds lose nothing of value: no TFC blocks were ever generated.
- Worlds created while TFC was installed keep its forced game rules (§3.7).

**Leftovers.** The TFC configs and OpenLoader packs became inert, and were deleted from the client instance and the
repo on 2026-11-07 (CONFIG_CHANGES "2026-11-07 — Repository cleanup: personal files purged from history, orphaned
configs removed"): `tfc-client.toml`, `tfc-common.toml`, `defaultconfigs/tfc-server.toml`, the data packs
`TFCFoodHunger`, `TFCLightItems` and `TFCVanillaCraftingTable`, the resource packs `TFCNoSizeTooltip` and
`TFCHideInventoryTabs`, and `data-disabled/TFCModdedFood`. Remaining as of 2026-11-08:

- `kubejs/server_scripts/tfc_vanilla_food.js` still runs. Without TFC it only writes `tfc:` data that nothing reads.
- `TerraFirmaCraft-Forge-1.20.1-3.2.28.jar`, `mcw_tfc_aio-0.2.30-1.20.1.jar` and `TFC_CarryOnStorage1.2.0.jar` are
  `.jar.disabled` in `mods/`, where the CurseForge app can re-enable them (§2.3).
- The VM has the three inert data packs `TFCFoodHunger`, `TFCLightItems` and `TFCVanillaCraftingTable`.

### 2.5 Day length: the `DayLength30` datapack never loaded

Every log since July (checked: `2026-07-12-6.log.gz` and the 2026-11-01 log) contains:

```
Failed to load function spooky_daylen:tick
Couldn't load tag minecraft:tick as it is missing following references: spooky_daylen:tick (from data/DayLength30)
```

**Cause:** TFC replaces the vanilla `/time` command with its own (`add <n> days|ticks|…`), so the datapack's
`time add 1` does not parse and the function fails to compile. A tag with a missing reference is dropped whole, so
**the entire `minecraft:tick` function tag was discarded**. That also killed `wa_buff:tick`: the documented Weeping
Angels stat buff never applied.

The day length in effect was Simple Day Length Extender's: 2.0×/2.0× with TFC calendar auto-adjust ≈ **60-minute
days** (the save of 2026-11-01: `Time` 2937 vs `DayTime` 757 = exactly SDLE's 3.87× day multiplier).

**Fix:** `DayLength30` retired to `config/openloader/data-disabled/`, and SDLE set to 1.5 / 1.5 without calendar
auto-adjust: 36 000 real ticks per day, the documented **30 minutes**. Sleeping skips the night (SDLE overrides the
sleep gamerule check), and the Weeping Angels buff tick loads again. Values: CONFIG_CHANGES "2026-11-01 — Mod-update
review: about 80 mods updated, 13 jars removed".

### 2.6 Update breakage

| Mod (old → new) | What the update broke | Fix |
|---|---|---|
| MineColonies 1.1.1231 → 1301 | Raid keys renamed (`barbarianhordedifficulty` → `raidDifficulty` etc.); the new world seeded the mod default **5**, losing the documented 8 | Documented values under the new keys, old keys kept so either server version seeds correctly; live world 5 → 8 |
| MineColonies (research data) | 11 new mounted-combat/barding researches at default cost | Scaled by the documented ×2/×3/×4 rule; `MineColoniesSlowResearch` has 212 files |
| Weeping Angels 46.1.5 → 46.1.6 | `weeping_angels_spawns.json` regenerated: **9 new biomes enabled at weight 8, groups of 1–4** (Incendium nether + Glimmering Weald); new `snow_angels = true` buries angels in every snowy biome; `max_nearby = 6` | The 9 biomes off (curated 16 untouched); `snow_angels = false`; `max_nearby = 1` |
| Server-Side Horror 4.2 → 5.1 | New `random_starer` event, absent from the JSON, so it ran at mod defaults (enabled) | `random_starer_enable = false`, `random_starer_chance = 8000000` (the documented starer rarity) |
| Door Knocker 1.2.0 → 1.3 | New independent `[trapdoors]` event at the old frequent defaults | Aligned to the documented door timings (48000/96000 day, 24000/48000 night) |
| TFC 3.2.23 → 3.2.28 | `tfc-client.toml` rewritten (harmless); data files unchanged | All 205 TFC data overrides verified |
| Cold Sweat removed | Quest "Warmth" (Broken World) still required `cold_sweat:waterskin` / `thermometer` | Quest removed |
| Hardcore True Darkness → v6 | 10 s join timeout kicks slow clients | Moot: rolled back to 4.0.0 (§2.2) |

Every fix: CONFIG_CHANGES "2026-11-01 — Mod-update review: about 80 mods updated, 13 jars removed".

### 2.7 Pre-existing problems found

- **Warp Stone gate bypass:** `waystoneandcreate` ships a Create compacting route (amethyst + 4 pearls + 4 emeralds →
  uncut warpstone → Warp Stone) with no Eye of Ender. `WaystonesGate` overrides it (one emerald →
  `minecraft:ender_eye`). Create Waystones Recipes 2.0.1 also ships a `warp_stone.json` (mechanical crafting, no eye)
  at the **same path** as the pack's override, so the pack's recipe still wins.
- **`kubejs/server_scripts/oc_sanity.js`:** `CONFIG.debug` was left **true**, logging DIAG lines every 2 s per player.
  Set to false; the live log confirms both channels run correctly.

### 2.8 Reviewed, no change needed

Smooth Chunk 4.4, Sophisticated Backpacks/Storage/Core (new stack-upgrade conversion items only), Storage Drawers 12.15
(new Push/Pull upgrades + Connection Key — drawers are the intended early-game storage), The Obsessed 1.5.2 (tuned
values intact; new `Allow Building Structures` / `Horse Stealing` left on — "imitation" is on-theme), JEI 15.56
(config format migrated; the "not a valid config key" errors in the log are the one-time rewrite), Collective / Double
Doors (configs moved to `config/serilum/`), Blood Magic 3.3.8 (sacrificial values intact), AmbientSounds 6.3.11
(`QuietAmbience` override paths unchanged), AE2 15.4.12, Ad Astra 1.15.21, Create addons, Immersive Petroleum 36b,
Iron's Spellbooks 3.16.3, Xaero (tuned keys intact). All 17 OpenLoader packs were checked programmatically: every
override path still exists in the updated jars.

Known, pre-existing log noise (not new): `betterchunkloading` ↔ Canary mixin conflict on `ServerChunkCache`, malformed
JSON in `waystoneandcreate:port_stone` and `create_alexscaves_compat:mixing/marine_snow`, missing IDAS BOP/BYG biome
tags.

### 2.9 Consistency pass (2026-11-01): configs the docs never covered

After the void and Hardcore True Darkness fixes were confirmed, every documented value was re-verified. The findings
are mods the July docs never reviewed, or documented intents that were not in effect. Before → after values and
reasons: CONFIG_CHANGES "2026-11-01 — Lore and config consistency pass".

- **Pale Hound** was timer-summoned every 13–20 min, up to three at once, in every modded biome: its own config's
  blacklist overrode the datapack tag the July pass relied on. Fixed: hour-plus gaps, solitary, the five documented
  biomes (`defaultconfigs/pale_hound-server.toml`).
- **CANTBREATHE** (never reviewed) came every 14–25 min. Fixed: hour-plus gaps (`defaultconfigs/cant_breathe-server.toml`).
- **John Mod Reborn** (weight 6/9/9 everywhere, baby packs of 3–9) and **Creeping Woods** (17 forest spawns, weights
  10–130): weight 2 via `HorrorSpawnRarity`, the same precedent as Rake/TOWW/Wendigo/Whispering Spirits.
- **Xaero's Minimap** (new) radar off; **Jade** no longer identifies horror entities; **Nature's Compass** cannot locate
  Alex's Caves biomes; **Quark** Totem of Holding off (Corpse is the death system); **Serene Seasons** `day_duration`
  36000 to match the 30-minute day.

The pass's open proposals are in §3.6.

### 2.10 Immersive Portals × DimThreads (2026-11-02)

**Symptom:** Immersive Portals 3.0.7, Full Immersion, Ars Two-Way Portals and the Make it Compatible patch were added
on 2026-11-02. The first world load wrote a 1.3 GB log in under three minutes.

**Cause:** Immersive Portals' chunk-ticket flush checks that it is on the server thread, and under DimThreads each
dimension ticks on its own worker, so the check failed once per tick per dimension. Neither project fixes it:
DimThreads 1.20.1 has no build after v1.2.1 and the issue is unanswered; Immersive Portals' repository is archived.

**Fix:** a one-mixin shim built for this pack, `tools/immptl_dimthread_compat/`, lets that one check accept the
DimThreads worker that owns the level. Required on both client and server; Server v2 was built with it (§3.9).

**Verification:** headless, 80,685 errors in 90 s without it, 0 with it, no other change; in game, portals load and
render their far side.

**Status:** DimThreads was disabled on 2026-11-06 (1.2.1 ticks every dimension under one global lock, so it gave no
parallelism), and the shim is `.jar.disabled` with it on the client instance and the VM. Full entries: CONFIG_CHANGES
"2026-11-02 — Immersive Portals × DimThreads: compat shim" and "2026-11-06 — DimThreads disabled".

---

## 3. Proposals

### 3.1 Crash Assistant upload metadata

- **Finding:** Crash Assistant 1.11.12 added `send_uploaded_logs_data_to_kostromdan_dev = true` (sends upload metadata
  to a third-party server) and dropped the privacy-policy prompt.
- **Change:** set it to `false` in `config/crash_assistant/config.toml`.
- **Status:** rejected; see the status table, row 15.

### 3.2 Lore-loot coverage gaps

- **Target:** `kubejs/server_scripts/oc_loot.js`.
- **Change:** a routing pass for loot tables with no lore routing, the same class of gap CONFIG_CHANGES already fixed
  for WDA/YUNG's: `abandoned_watchtower_refurnished:chests/freshloot` (new in 7.0), Spooky Campsite (2 tables), Big
  Lost City (5), IDAS (206 tables), and YUNG's Better Nether Fortresses (skipped in July because TFC blocked the
  Nether; reachable again since TFC's removal).

### 3.3 EntityCulling safe mode

- **Target:** EntityCulling 1.11's new `safeMode = true`.
- **Change:** check in game whether it costs FPS.

### 3.4 Lore and design docs out of step with the mod set

Lore and design docs, left unedited by this review:

- `ftbquest.md` listed "Knocker" under Unidentified Contact.
- `thelore.md` lists From The Fog as an Observer.
- `implore_implementation_plan.md` describes Cold Sweat lore pools.
- `CONFIG_CHANGES.md`'s The Forgotten section is historical.

### 3.5 Quest-book polish

Findings from the FTB review:

- "The Second Civilisation" is visible from the start; the design doc says it "just appears" later, which needs
  dependencies and a chapter-level hide.
- "The Unacceptable Solution" accepts only the fusion controller; the doc says fission *or* fusion.
- `hs_lgtfo_mission_data`, `memory_fragment_distorted`/`forbidden`, `corrupted_map` and the two clarity consumables are
  referenced by no quest.

Applied on 2026-11-08 (CONFIG_CHANGES "2026-11-08 — Quest book: fixes and additions"): People Return and Secure The
Future stay hidden until the opening quests of two of the four paths are done; The Unacceptable Solution completes on
a fission reactor port, a fusion reactor controller or an Extreme Reactors reinforced reactor controller; The Answer lists the HS-LGTFO Mission Data; the
quests Deeper Memories, The Map That Lies and Coping cover the memory fragments, the corrupted map and the clarity
consumables (Sedative Ampoule, Familiar Photograph).

### 3.6 Consistency-pass proposals

From the 2026-11-01 consistency pass; each one's full reasoning is in CONFIG_CHANGES "2026-11-01 — Lore and config
consistency pass" (its proposals list).

| # | Finding | Target | Change |
|---|---|---|---|
| 1 | Singleplayer view and simulation distance are 64 / 64, so the integrated server keeps about 16,600 chunks loaded and ticking; the dedicated server runs 10 / 10 | `options.txt` (per player) | 12–16 render (Farsight fills the horizon beyond the server distance), 8–10 simulation |
| 2 | Artifacts' Night Vision Goggles contradict "darkness is genuinely dark" and are ordinary chest and campsite loot at `artifactRarity` 1.0 | Artifacts gamerules `artifacts.nightVisionGoggles.enabled` / `.strength`, set from a `minecraft:load` function datapack (the `DayLength30` pattern); `artifactRarity` | Goggles off or weaker; `artifactRarity` 1.0 → 2–3 |
| 3 | Iron's Spellbooks Recall / Portal and Ars Nouveau warp scrolls and portals are uncosted, unbounded, cross-dimension teleports, the conflict the Mahou pass resolved | Iron's per-spell config (`/ironsSpellbooks config`); Ars `enableWarpPortals`; a warp scroll recipe override | `enabled = false` or a mana/cooldown increase; `enableWarpPortals = false` |
| 4 | Explorer's Compass locates any structure within 10 km for free | `defaultXpLevels` in both compass configs | 3–5 levels, mirroring "every teleport costs XP" |
| 5 | Nights are the horror window, and a Comforts sleeping bag skips every one | `comforts-server.toml` | `restrictSleeping = true` (one sleep per in-game day) or `sleepingBagBreakChance = 25` |

### 3.7 TFC game rules in older worlds

- **Target:** worlds created while TFC was installed (`saves/New World (2)`, `saves/New World (3)`), where
  `naturalRegeneration`, `doInsomnia`, `doTraderSpawning` and `doPatrolSpawning` are `false` in `level.dat`.
- **Change:** `/gamerule <rule> true` for each of the four, in any of those worlds that is kept.
- **Not affected:** worlds created afterwards. `New World (4)` and Server v2's world have all four `true`.
- Source: CONFIG_CHANGES "2026-11-02 — Mod set changes, 2026-11-01 to 2026-11-02".

### 3.8 Day length with a longer night

- **Target:** `simpledaylengthextender-common.toml`.
- **Change:** optional: `dayLengthMultiplier` 1.25, `nightLengthMultiplier` 1.75, which gives about 33 min with a
  longer night.

### 3.9 Server update checklist

Done on 2026-11-02 by building a new server, `Servers/Server v2`, instead of updating the July server, which is
unchanged (CONFIG_CHANGES "2026-11-02 — Server v2: dedicated server built to match the client"). Server v2 covers
every item below and has a fresh world. Since 2026-11-06 the VM is the live server, deployed by
`scripts/sync_server_mods.py`, and Server v2 is a local test bed. The checklist:

1. **Mods:** `python scripts/sync_server_mods.py` (regenerates `SERVER_MODS.md`), keeping the client-only
   exclusions. The server stays on `chunksending-1.20.1-2.8` and `farsight-1.20.1-5.1` (the July server
   already had them); the newer 4.1 / 5.8 jars in the client's `local/mods-rolled-back-2026-11-01/` are never copied.
2. **Configs:** copy `defaultconfigs/` (minecolonies, plus **pale_hound** and **cant_breathe**, new on 2026-11-01),
   `config/openloader/` (HorrorSpawnRarity also covers John Mod Reborn and Creeping Woods),
   `config/serversidehorror.json`, `config/weeping_angels*`, `config/doorknocker-common.toml`,
   `config/simpledaylengthextender-common.toml`, `config/quark-common.toml`, `config/naturescompass-common.toml`,
   `config/sereneseasons/`, `config/ftbquests/` and `kubejs/`. The Jade and Xaero minimap changes are client-only.
3. **Hardcore True Darkness:** `hardcoretruedarkness-4.0.0.jar` on both sides. v6 is a hard protocol gate and must
   not be on one side only.
4. **Immersive Portals:** the four Immersive Portals jars and `immptl_dimthread_compat-1.0.0.jar` (from `local/`);
   without the shim the server log floods (§2.10). The shim was retired with DimThreads on 2026-11-06.
5. **Serene Seasons:** `SereneSeasons-forge-1.20.1-9.1.0.3.jar`. It is not client-only, the July server had no copy,
   and seasons are computed server-side.
6. **TFC:** no TFC jar or dependants; `level-type=minecraft:normal` in `server.properties`.
7. **Client-only list:** the sync script's exclusion list is sufficient: a dedicated server built from the client's
   `mods/` minus that list (281 jars, shim included) started cleanly on 2026-11-02.

---

## 4. Sources

- **World data:** `saves/New World (2)/region/r.-1.0.mca`; `saves/*/level.dat`; `simplebackups/*.zip`;
  `xaero/world-map/New World (2)/null/cache_1/`.
- **Logs:** `logs/latest.log` (2026-11-01: `oc_sanity.js` DIAG lines, Chunk Sending debug lines,
  `Failed to load function spooky_daylen:tick`); `2026-07-12-6.log.gz`.
- **Jars:** the updated jars in `mods/`; the July server's `Servers/spookydookie/mods/`; TFC 3.2.28
  (`ClientForgeEventHandler.onScreenOpen`, `data/tfc/worldgen/world_preset/overworld.json`); Farsight 5.8
  (`ClientPacketListener.updateLevelChunk` hook).
- **Change log:** CONFIG_CHANGES "2026-11-01 — Mod-update review: about 80 mods updated, 13 jars removed",
  "2026-11-01 — Lore and config consistency pass", "2026-11-02 — Mod set changes, 2026-11-01 to 2026-11-02",
  "2026-11-02 — Immersive Portals × DimThreads: compat shim", "2026-11-02 — Server v2: dedicated server built to
  match the client".
