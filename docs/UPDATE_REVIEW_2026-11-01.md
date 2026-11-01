# Mod-update review — 2026-11-01

Scope: the CurseForge update of 2026-11-01 (≈80 mods updated, 13 jars removed), checked against the
docs in this folder (source of truth), the pre-update git state, the pre-update dedicated server at
`Servers/spookydookie`, and the older pack exports under `CurseForge/mc/`. Client instance only — the
server was **not** touched (two accidental edits to its `defaultconfigs/tfc-server.toml` were reverted).

Everything listed under "Applied" is also recorded per-mod in `CONFIG_CHANGES.md` (section
"2026-11-01 — Mod-update review").

---

## 1. The "void" world — what the evidence says

The new world (`saves/New World (2)`, created 19:51) is **fine on the server side**:

| Evidence | Where |
|---|---|
| Spawn chunk `[-15, 19]` is `minecraft:full`, 24 sections, grass/dirt/stone/ores, Terralith cave biomes | `region/r.-1.0.mca` (parsed) |
| 1018 generated chunks in that region file after a 4-minute session | `region/r.-1.0.mca` |
| Player stood at y = 71 for 30 s with sky light 15 (not falling) | `oc_sanity.js` DIAG lines in `logs/latest.log` |
| **Xaero's World Map wrote ~800 KB of map cache for this world at 19:56** | `xaero/world-map/New World (2)/null/cache_1/*.xwmc.outdated` |

The last line is decisive: Xaero's map writer only runs on chunks the **client** has received and
decoded. Chunks reached the client; the world simply isn't being *drawn*. So Chunk Sending 4.1,
Farsight 5.8, Smooth Chunk and the worldgen stack are not the cause.

What changed on the render side in this update:

1. **Hardcore True Darkness 4.0.0 → "v6"** — a different mod entirely (new mod id
   `hardcore_true_darkness`, package `com.hyrrx…`). It locks gamma to 0, darkens the lightmap, draws a
   full-screen "final darkness" post-process shader, and if that pass can't load it renders a
   **fallback blackout**. It is now **server-enforced**: clients without the matching v6 jar are
   disconnected, and the server kicks a client that doesn't report "ready" within
   `statusTimeoutTicks` (default 200 ticks = 10 s). The log shows it detected Oculus
   (`client readiness report: Oculus`) and ran its darkness path during the session.
2. Oculus + **Hysteria Shaders** (a deliberately very dark horror shader) were active — the
   shaderpack settings file was (re)written at 19:50:01.
3. Farsight 5.1 → 5.8, EntityCulling 1.10 → 1.11, Mo' Bends 5 → 6, NotEnoughAnimations — none of these
   can blank the whole world on their own.

**Diagnosis order (cheapest first):**

1. In the world press **F3**. If the "C:" counter shows rendered sections > 0 and the F3 world/biome
   text is populated, the world is loaded and it is a darkness/shader overlay. (It will be.)
2. Toggle shaders off (Oculus default key **K**, or `enableShaders=false` in
   `config/oculus.properties`). If terrain appears, the HTD v6 pass + Hysteria combination is the
   cause.
3. If still black with shaders off, move `mods/hardcore_true_darkness-6.jar` out of `mods/`. The
   pre-update jar is still available as a drop-in rollback at
   `Servers/spookydookie/mods/hardcoretruedarkness-4.0.0.jar` (the version `SERVER_MODS.md`
   documents, and the one the not-yet-updated server still runs — so rolling back also restores
   client/server parity). If you roll back, lock the mod in CurseForge so it isn't re-updated.
4. If you keep v6: the server must get the identical jar when it is updated, every player needs it,
   and `defaultconfigs/hardcore_true_darkness-server.toml` (added, `statusTimeoutTicks = 1200`)
   should be copied to the server so slow joins aren't kicked at 10 s.

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

Chunk Sending 4.1 (config migrated, `maxChunksPerTick = 5` kept), Smooth Chunk 4.4, Farsight 5.8
(chunk preview still off), Sophisticated Backpacks/Storage/Core (new stack-upgrade conversion items
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

1. **Remove TFC** — see §2.
2. **HTD v6: keep or roll back** — see §1. Rolling back is the only option that lets the current
   server accept this client.
3. **Crash Assistant 1.11.12** added `send_uploaded_logs_data_to_kostromdan_dev = true` (sends upload
   metadata to a third-party server) and dropped the privacy-policy prompt. Set it to `false` if you
   don't want that.
4. **Lore-loot coverage gaps** in `oc_loot.js` (same class of gap CONFIG_CHANGES already fixed for
   WDA/YUNG's): `abandoned_watchtower_refurnished:chests/freshloot` (new in 7.0), Spooky Campsite (2
   tables), Big Lost City (5), and IDAS (206 tables). Worth a routing pass.
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

1. `python scripts/sync_server_mods.py` (regenerates `SERVER_MODS.md`; keep the client-only
   exclusions).
2. Copy `defaultconfigs/` (minecolonies, tfc, hardcore_true_darkness), `config/openloader/`,
   `config/serversidehorror.json`, `config/weeping_angels*`, `config/doorknocker-common.toml`,
   `config/simpledaylengthextender-common.toml`, `config/ftbquests/`, `kubejs/`.
3. Apply the four `tfc-server.toml` toggles from §2 to the server's `defaultconfigs/tfc-server.toml`
   (not done — server out of scope this pass).
4. HTD v6 is a hard protocol gate: server and every client must run the same jar.
