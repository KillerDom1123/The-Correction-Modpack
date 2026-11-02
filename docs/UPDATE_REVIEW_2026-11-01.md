# Mod-update review — 2026-11-01

Scope: the CurseForge update of 2026-11-01 (≈80 mods updated, 13 jars removed), checked against the
docs in this folder (source of truth), the pre-update git state, the pre-update dedicated server at
`Servers/spookydookie`, and the older pack exports under `CurseForge/mc/`. Client instance only — the
server was **not** touched (two accidental edits to its `defaultconfigs/tfc-server.toml` were reverted).

Everything listed under "Applied" is also recorded per-mod in `CONFIG_CHANGES.md` (section
"2026-11-01 — Mod-update review").

**Status as of 2026-11-02:** the void world and the shaders black screen are fixed (§1); TFC was removed (§2);
a consistency pass followed (§8); Immersive Portals was added with a compat shim for DimThreads (§9); a new dedicated server, `Servers/Server v2`,
was built from the client (§7). The
mod-set record is in `CONFIG_CHANGES.md`, "Mod set changes, 2026-11-01 evening → 2026-11-02".

---

## 1. The "void" world — what the evidence says

The new world (`saves/New World (2)`, created 19:51) is **fine on the server side**:

| Evidence | Where |
|---|---|
| Spawn chunk `[-15, 19]` is `minecraft:full`, 24 sections, grass/dirt/stone/ores, Terralith cave biomes | `region/r.-1.0.mca` (parsed) |
| 1018 generated chunks in that region file after a 4-minute session | `region/r.-1.0.mca` |
| Player stood at y = 71 for 30 s with sky light 15 (not falling) | `oc_sanity.js` DIAG lines in `logs/latest.log` |
| Xaero's World Map wrote ~800 KB of map cache for this world at 19:56 | `xaero/world-map/New World (2)/null/cache_1/*.xwmc.outdated` — **not evidence of delivery**: in singleplayer Xaero reads the save's region files directly |

**Resolved 2026-11-01 (evening), by in-game bisection with F3.** F3 showed the client holding **0
loaded chunks** while the integrated server had thousands and climbing, i.e. chunk packets were never
reaching the client. The cause was **Chunk Sending 2.8 → 4.1**: with its debug logging on, its
per-tick line claimed "Sent: 15 packets" every tick, yet its own queue only grew (426 → 1487 in 76 s,
roughly the generation rate) and the client logged zero "Ignoring chunk" drops — the server-side
queue never drained. The 4.1 rewrite moved the send path onto the player tick, which in this pack
runs on DimThreads' `dimthread_server_overworld` thread. Rolling the jar back to **2.8** (July's
known-good pairing with this same DimThreads build) fixed it immediately: queue 392 → 0 within
seconds, terrain visible.

Eliminated along the way: TFC (void persisted with TFC disabled), Hardcore True Darkness v6 (void
persisted with it disabled), Farsight 5.8 (void persisted after rolling it back to 5.1), worldgen
(chunks were always on disk).

State now (client): `chunksending-1.20.1-2.8.jar` and `farsight-1.20.1-5.1.jar` active, byte-identical
to the server's jars (5.8 adds a new cancellable HEAD hook on `ClientPacketListener.updateLevelChunk`;
it was not the cause but is kept on the known-good 5.1 by request). **A `.disabled` rename is not a
pin in a CurseForge-managed instance**: at 21:28 the app re-scanned the profile, flipped
`chunksending-1.20.1-4.1.jar.disabled` back to `.jar`, and Forge loaded 4.1 over 2.8 (void returned).
The 4.1 / 5.8 / darkness-v6 jars are therefore parked outside `mods/` in
`local/mods-rolled-back-2026-11-01/` so only one jar per mod exists; the app adopts the remaining
jar by fingerprint (it already did so for Hardcore True Darkness 4.0.0). Check in the app that
Chunk Sending, Farsight and Hardcore True Darkness show 2.8 / 5.1 / 4.0.0 and use "Change version"
there if any still shows the newer file. Profile auto-update is already off. The server already
runs all three — do not sync the parked jars to it.

**Shaders-on black screen → Hardcore True Darkness rolled back to 4.0.0.** With Oculus + Hysteria
enabled the view at (-49, 109, -34) was pure black; with shaders off it was fine. The only new
component on the shader path since July was Hardcore True Darkness v6, which runs a vanilla
post-process chain after level rendering (`DarknessFloor` 0.018, `DarknessStrength` default 1.0,
`DaylightPreserve` default 0) and under Oculus crushes the whole frame. Applied: `hardcoretruedarkness-4.0.0.jar`
(July's version, from `Servers/spookydookie/mods/`) copied in, `hardcore_true_darkness-6.jar` →
`.disabled`, and the v6-only `defaultconfigs/hardcore_true_darkness-server.toml` removed (4.0.0 has
a different mod id and no server gate). **Pin 4.0.0 in CurseForge.** The server already runs 4.0.0.
Confirmed in-game 2026-11-01 21:40: chunks load and the shaders-on view is no longer black.

---

## 2. TerraFirmaCraft — the real situation

**TFC world generation has never been active in this pack.** Every world on disk uses the vanilla
noise generator with the vanilla/Terralith multi-noise biome source:

| World | Overworld generator | Source |
|---|---|---|
| New World, New World (1), New World (2) | `minecraft:noise` / `minecraft:multi_noise` / `minecraft:overworld` | `saves/*/level.dat` |
| A kinder, Bazinga, Bruh, Boom-era "New World" backups | same | `simplebackups/*.zip` |

TFC's preset (`tfc:overworld` generator + `tfc:overworld` biome source, from
`data/tfc/worldgen/world_preset/overworld.json`) was never chosen, even though `tfc-common.toml`
sets it as the default. The spawn chunk is vanilla stone/deepslate with Terralith cave biomes and
Mekanism/IE/Create ores — not a TFC rock layer anywhere.

That is probably not an accident: TFC's generator replaces the biome source, so Terralith, Alex's
Caves and most YUNG's/Dungeons Arise structures (which key off vanilla biome tags) would not
generate in a TFC world, and vanilla/modded ores would not either. The pack's documented worldgen
set ("TerraFirmaCraft, Terralith, Incendium, Alex's Caves, YUNG's…") only works *because* TFC's
generator is not in use.

So TFC currently provides **none** of the worldgen the pack wants and **all** of the mechanics the
pack keeps working around: the HUD bars, Calendar/Nutrition/Climate tabs, food/thirst/decay,
item sizes/weights, collapses, forced game rules, and — found this pass — two silent defaults that
contradict the docs:

- `enableNetherPortals = false` — the Waystones gate (Eye of Ender), the Occultism ritual costs
  (blaze, ghast tear, wither skull) and three installed nether worldgen mods all assume the nether
  is reachable. **Fixed → true** (instance defaults + live world).
- `enableVanillaMonstersOnSurface = false` — TFC was denying every vanilla monster spawn on the
  surface. CONFIG_CHANGES blamed the empty nights on The Forgotten's limiter; that mod is gone and
  this was the remaining suppressor. **Fixed → true** (instance defaults + live world).

Also set: `enableInfestations = false` (rat infestations on stored food) and
`enableOverburdening = false` (direct toggle for what `TFCLightItems` works around).

### Decision for you: remove TFC or keep it

**Decided 2026-11-01: removed.** TerraFirmaCraft, `mcw_tfc_aio` and TFC Carry On Storage are disabled. Still open:
retire the inert leftovers listed below, and switch TFC's forced game rules back on in worlds created while it was
installed (`CONFIG_CHANGES.md`, "Mod set changes"). The analysis below is kept as the record.

**Recommended: remove TerraFirmaCraft** (plus its dependants `mcw_tfc_aio-0.2.30` and
`TFC_CarryOnStorage1.2.0`). Then:

- Delete/retire `config/openloader/data/{TFCFoodHunger,TFCLightItems,TFCVanillaCraftingTable}`,
  `config/openloader/resources/{TFCNoSizeTooltip,TFCHideInventoryTabs}`,
  `kubejs/server_scripts/tfc_vanilla_food.js`, `defaultconfigs/tfc-server.toml`, and the
  `tfc-*.toml` configs (all become inert).
- Vanilla hunger/regen return by themselves (the docs' end goal). No day-length impact (see §3).
- Nether portals, bonemeal on vanilla crops, wandering traders and pillager patrols come back
  (TFC's `enableForcedTFCGameRules` was switching the last two off).
- Existing worlds lose nothing of value (no TFC blocks were ever generated).

**If you keep it:** the inventory tabs have no config switch (verified in
`ClientForgeEventHandler.onScreenOpen`, TFC 3.2.28). The new `TFCHideInventoryTabs` resource pack
blanks the tab strip in `tfc:textures/gui/icons.png`, so they are no longer visible — but the
buttons still exist invisibly at the top-left of the inventory and can be clicked. The guide-book
tab/links and the Jade hoe overlay *are* configurable and are now off.

---

## 3. Day length — the datapack never worked

Every log since July (checked `2026-07-12-6.log.gz` and today's) contains:

```
Failed to load function spooky_daylen:tick
Couldn't load tag minecraft:tick as it is missing following references: spooky_daylen:tick (from data/DayLength30)
```

TFC replaces the vanilla `/time` command with its own (`add <n> days|ticks|…`), so the datapack's
`time add 1` does not parse, the function fails to compile, and — because a tag with a missing
reference is dropped whole — **the entire `minecraft:tick` function tag is discarded**. That also
killed `wa_buff:tick`, so the documented Weeping Angels stat buff has never applied either.

The day length the pack has actually been running is Simple Day Length Extender's: 2.0×/2.0× with
TFC calendar auto-adjust ≈ **60-minute days** (today's save: `Time` 2937 vs `DayTime` 757 = exactly
SDLE's 3.87× day multiplier).

**Applied:** `DayLength30` retired to `config/openloader/data-disabled/`;
`simpledaylengthextender-common.toml` set to `dayLengthMultiplier = 1.5`,
`nightLengthMultiplier = 1.5`, `autoCalendarAdjustments = false` → 36 000 real ticks per day =
the documented **30 minutes**, sleeping skips the night (SDLE overrides the sleep gamerule check),
and the Weeping Angels buff tick loads again. If you'd rather lean into the horror, 1.25 / 1.75
gives ~33 min with a longer night.

---

## 4. Update breakage found and fixed (client)

| Mod (old → new) | What the update broke | Fix applied |
|---|---|---|
| MineColonies 1.1.1231 → 1301 | Raid keys renamed (`barbarianhordedifficulty`→`raidDifficulty` etc.); the new world seeded the mod default **5**, losing the documented 8 | New keys added to `defaultconfigs/minecolonies-server.toml` with documented values (old keys kept so either server version seeds correctly); live world 5 → 8 |
| MineColonies (research data) | 11 new mounted-combat/barding researches at default cost | Scaled with the documented ×2/×3/×4 rule → `MineColoniesSlowResearch` now 212 files |
| Weeping Angels 46.1.5 → 46.1.6 | `weeping_angels_spawns.json` regenerated; **9 new biomes enabled at weight 8, groups of 1–4** (Incendium nether + Glimmering Weald); new `snow_angels = true` buries angels in every snowy biome; `max_nearby = 6` | 9 biomes disabled (curated 16 untouched); `snow_angels = false`; `max_nearby = 1` |
| Server-Side Horror 4.2 → 5.1 | New `random_starer` event, absent from the json → mod defaults (enabled) | `random_starer_enable = false`, `random_starer_chance = 8000000` (matches documented starer rarity) |
| Door Knocker 1.2.0 → 1.3 | New independent `[trapdoors]` event at the old frequent defaults | Trapdoor timings aligned to the documented door timings (48000/96000 day, 24000/48000 night) |
| TFC 3.2.23 → 3.2.28 | `tfc-client.toml` rewritten (harmless); data files unchanged | Verified all 205 TFC data overrides still match; see §2 for the config changes |
| Cold Sweat removed | Quest "Warmth" (Broken World) still required `cold_sweat:waterskin` / `thermometer` | Quest removed (`broken_world.snbt.bak` kept) |
| HTD → v6 | 10 s join timeout kicks slow clients | `defaultconfigs/hardcore_true_darkness-server.toml`, `statusTimeoutTicks = 1200` |

Also fixed while here:

- **Warp Stone gate bypass** (pre-existing, not update-related): `waystoneandcreate` ships a Create
  compacting route (amethyst + 4 pearls + 4 emeralds → uncut warpstone → Warp Stone) with no Eye of
  Ender. `WaystonesGate` now overrides it (one emerald → `minecraft:ender_eye`). Create Waystones
  Recipes 2.0.1 also ships a `warp_stone.json` (mechanical crafting, no eye) at the **same path** as the
  pack's override, so the pack's recipe still wins.
- `kubejs/server_scripts/oc_sanity.js`: `CONFIG.debug` was left **true** → DIAG spam every 2 s per
  player. Set to false (live log confirms both channels run correctly).
- `modlist.md` / `modlist.json` / README block regenerated (286 enabled, 0 disabled).

## 5. Reviewed, no change needed

Smooth Chunk 4.4, Sophisticated Backpacks/Storage/Core (new stack-upgrade conversion items
only), Storage Drawers 12.15 (new Push/Pull upgrades + Connection Key — drawers are the intended
early-game storage), The Obsessed 1.5.2 (tuned values intact; new `Allow Building Structures` /
`Horse Stealing` left on — "imitation" is on-theme), JEI 15.56 (config format migrated; the
"not a valid config key" errors in the log are the one-time rewrite), Collective / Double Doors
(configs moved to `config/serilum/`), Blood Magic 3.3.8 (sacrificial values intact), AmbientSounds
6.3.11 (`QuietAmbience` override paths unchanged), AE2 15.4.12, Ad Astra 1.15.21, Create addons,
Immersive Petroleum 36b, Iron's Spellbooks 3.16.3, Xaero (tuned keys intact). All 17 OpenLoader
packs were checked programmatically: every override path still exists in the updated jars.

Known, pre-existing log noise (not new): `betterchunkloading` ↔ Canary mixin conflict on
`ServerChunkCache`, malformed JSON in `waystoneandcreate:port_stone` and
`create_alexscaves_compat:mixing/marine_snow`, missing IDAS BOP/BYG biome tags.

## 6. Proposals not applied (your call)

1. ~~**Remove TFC**~~ — done 2026-11-01; leftovers still to retire (§2).
2. ~~**HTD v6: keep or roll back**~~ — rolled back to 4.0.0 on 2026-11-01; it was also what blacked out the
   shaders view (§1).
3. **Crash Assistant 1.11.12** added `send_uploaded_logs_data_to_kostromdan_dev = true` (sends upload
   metadata to a third-party server) and dropped the privacy-policy prompt. Set it to `false` if you
   don't want that.
4. **Lore-loot coverage gaps** in `oc_loot.js` (same class of gap CONFIG_CHANGES already fixed for
   WDA/YUNG's): `abandoned_watchtower_refurnished:chests/freshloot` (new in 7.0), Spooky Campsite (2
   tables), Big Lost City (5), IDAS (206 tables), and YUNG's Better Nether Fortresses (skipped in July because TFC
   blocked the nether; reachable again). Worth a routing pass.
5. **EntityCulling 1.11** added `safeMode = true`; check in-game whether it costs FPS.
6. **Stale docs** (lore/design, left alone): `ftbquest.md` lists "Knocker" under Unidentified
   Contact; `thelore.md` lists From The Fog as an Observer; `implore_implementation_plan.md` still
   describes Cold Sweat lore pools; `CONFIG_CHANGES.md`'s The Forgotten section is historical.
7. **Quest-book polish** from the FTB review: "The Second Civilisation" is visible from the start
   (doc says it "just appears" later — needs deps + chapter-level hide); "The Unacceptable Solution"
   accepts only the fusion controller (doc says fission *or* fusion);
   `hs_lgtfo_mission_data`, `memory_fragment_distorted/forbidden`, `corrupted_map`, the two clarity
   consumables are referenced by no quest.

## 7. Server update checklist (when you do it)

**Done 2026-11-02 by building a new server instead of updating the old one:** `Servers/Server v2` covers every item
below (pinned jars, shim, Serene Seasons, no TFC, `minecraft:normal`, current configs) and has a fresh world;
the July server is untouched. See `CONFIG_CHANGES.md`, "Server v2". The list is kept as the record.

1. `python scripts/sync_server_mods.py` (regenerates `SERVER_MODS.md`; keep the client-only
   exclusions). Keep the server on `chunksending-1.20.1-2.8` and `farsight-1.20.1-5.1` (it already
   has them); the newer 4.1/5.8 jars are parked in the client's `local/mods-rolled-back-2026-11-01/` and must
   not be copied.
2. Copy `defaultconfigs/` (minecolonies, **pale_hound, cant_breathe** — new 2026-11-01 22:00; tfc only if TFC stays),
   `config/openloader/` (HorrorSpawnRarity now also covers John Mod Reborn + Creeping Woods),
   `config/serversidehorror.json`, `config/weeping_angels*`, `config/doorknocker-common.toml`,
   `config/simpledaylengthextender-common.toml`, `config/quark-common.toml`, `config/naturescompass-common.toml`,
   `config/sereneseasons/`, `config/ftbquests/`, `kubejs/`. Jade / Xaero minimap changes are client-only.
3. ~~Apply the four `tfc-server.toml` toggles from §2~~ — moot now that TFC is removed (item 6).
4. Keep Hardcore True Darkness on `hardcoretruedarkness-4.0.0.jar` (the client was rolled back to it; v6 is a hard
   protocol gate and must not be reintroduced on one side only).
5. Copy the four Immersive Portals jars **and** `immptl_dimthread_compat-1.0.0.jar` (from `local/`) to the server; without the
   shim the server log floods the same way (`docs/CONFIG_CHANGES.md`, "Immersive Portals × DimThreads").
6. Add `sereneseasons-forge-1.20.1-9.1.0.3.jar` to the server — it is not client-only and the server has no copy;
   seasons are computed server-side. Remove the server's TFC jar and dependants if TFC stays removed, and set
   `level-type=minecraft:normal` in `server.properties`.
7. The sync script's client-only exclusion list is sufficient as it stands: a dedicated server built from the
   current `mods/` minus that list (281 jars, shim included) started cleanly on 2026-11-02.

## 8. Consistency pass (2026-11-01 22:00) — configs the docs never covered

Done after the void/HTD fixes were confirmed. Every documented value was re-verified; the changes
are all to mods the July docs never reviewed, or to documented intents that were not actually in
effect. Full entries with before/after and the reasoning are in `docs/CONFIG_CHANGES.md`
("2026-11-01 (22:00)"):

- **Pale Hound** was timer-summoned every 13–20 min, up to three at once, in every modded biome — its own
  config's blacklist overrode the datapack tag the July pass relied on. Now `defaultconfigs/pale_hound-server.toml`
  (hour-plus gaps, solitary, the five documented biomes).
- **CANTBREATHE** (never reviewed) every 14–25 min → `defaultconfigs/cant_breathe-server.toml`, hour-plus gaps.
- **John Mod Reborn** (weight 6/9/9 everywhere, baby packs of 3–9) and **Creeping Woods** (17 forest spawns,
  weights 10–130) → weight 2 via `HorrorSpawnRarity`, same precedent as Rake/TOWW/Wendigo/WS.
- **Xaero's Minimap** (new) radar off; **Jade** no longer identifies horror entities; **Nature's Compass** cannot
  locate Alex's Caves biomes; **Quark** Totem of Holding off (Corpse is the death system); **Serene Seasons**
  `day_duration` 36000 to match the 30-minute day.
- Proposals left for you: singleplayer view/simulation distance 64/64 (the integrated server ticks ~16,600 chunks),
  Artifacts night-vision goggles, Iron's/Ars teleports, compass XP cost, Comforts, retiring TFC leftovers.

## 9. Immersive Portals × DimThreads (2026-11-02)

Immersive Portals 3.0.7, Full Immersion, Ars Two-Way Portals and the Make it Compatible patch were added. The
first world load wrote a 1.3 GB log in under three minutes: Immersive Portals' chunk-ticket flush checks that it
is on the server thread, and under DimThreads each dimension ticks on its own worker, so the check failed once
per tick per dimension. Neither project will fix it (DimThreads 1.20.1 has no build after v1.2.1 and the issue is
unanswered; Immersive Portals' repository is archived).

Resolved with a one-mixin shim built for this pack, `tools/immptl_dimthread_compat/`, which lets that one check
accept the DimThreads worker that owns the level. Verified headless (80,685 errors in 90 s without it, 0 with it,
no other change) and in-game (portals load and render their far side). It must also go on the server.
Full entry: `CONFIG_CHANGES.md`, "Immersive Portals × DimThreads".
