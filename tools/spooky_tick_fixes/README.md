# spooky_tick_fixes

A small Forge 1.20.1 mod of behaviour-preserving server-tick fixes for the Spooky Dookie pack. Each patch skips work
that provably cannot change the outcome; none changes what a mod does. Evidence, measurements and the full proposal
list: `docs/SERVER_TICK_AUDIT_2026-11-06.md` (§5.3). Applied changes: `docs/CONFIG_CHANGES.md`.

## Patches (first pass)

| Switch (`config/spooky_tick_fixes-common.toml`) | Target | What it skips | Why it's safe |
|---|---|---|---|
| `mimicSleep`, `mimicWakeRange` (24) | `artifacts.entity.MimicEntity#tick` | The whole tick of a **dormant** mimic with no player within 24 blocks | A dormant mimic wakes only by targeting a player within its 16-block follow range, by being hurt (outside the tick) or by being in water. It never despawns |
| `watcherFollowRange`, `watcherFollowRangeCap` (32) | `EntityAttributeModificationEvent` + `EntityJoinLevelEvent` | Follow range 2048 → 32 for TOWW `hanging`, `staring`, `crawling`, `hallucination`, `delusion`, `looked_at`; the default is lowered at type level and saved watchers are clamped on join | Path searches may visit follow_range × 16 nodes. Their look-at-player goal has its own 1000-block range. Chase variants are untouched |
| `obsessedSyncCoalesce` | The Obsessed `TheObsessedModVariables$PlayerVariables#syncPlayerVariables` | Repeated full sends; one send per player at the end of the server tick | The client replaces its whole copy on each sync, so only the final state counts. Calls made during the flush and client-side calls pass through unchanged |
| `spiritLookGate` | Whispering Spirits `PlayerLookAtSpiritProcedure#execute` | The 100-step look ray (3 raycasts and a spirit search per step) when no spirit is loaded in the dimension | Every other action in the method follows a non-empty spirit search |
| `fragileTorchesPrecheck` | Fragile Torches `FragTorchEvent#onEntityUpdate` | The per-mob list parse and chance roll when no fragile torch is at the mob's feet or head | Those two blocks are the only ones it can break |
| `farmCharmSyncOnChange`, `farmCharmRefreshTicks` (100) | Farm & Charm `PacketHandler#sendSaturationSync` | Unchanged sends; re-sent on change, every 100 ticks, and when a player starts tracking the animal | The values change only on a 20-tick check or a long decay roll |
| `animationGates` | `EntityAnimationFactory#onEntityTick` of Born in Chaos, The Obsessed, Creeping Woods, Foes, TOWW, Rake, Antlers | The handler, for mobs from other mods | Bytecode check: every `instanceof` target is in the mod's own entity package. Decided per class hierarchy and cached |
| `adAstraExtraGate` | Ad Astra Extra `EntityTickProcedure#onEntityTick` | The procedure, for mobs that are neither "gassed" nor in `adastraextra:jupiter` | Those are its only two branches. Persistent data is read without being created |

## Patches added in 1.1.0

| Switch | Target | What it skips | Why it's safe |
|---|---|---|---|
| `fullImmersionGate` | Full Immersion `PortalAiContext#begin`/`#finishAi`, `PortalDespawnCompatibility#shouldSuppressDespawn` | The per-mob portal-AI wrapper and the portal despawn check in a dimension with no Immersive Portals portal loaded | Projections and despawn suppression both need a `Portal` entity in the mob's level, and the mod indexes every one that joins. begin/finishAi are skipped as a pair (a per-mob flag), so its per-thread state can't go stale |
| `alexsCavesMovingBlockGate` | Alex's Caves `MagnetUtil#getMovingBlockCollisions`, `#isEntityOnMovingMetal` | The per-movement entity search for moving magnetic blocks in a dimension with none loaded | Both only look for `AbstractMovingBlockEntity` (and its subclass), so they return their empty answer |
| `citadelTrackerCache` | Citadel `ServerTickRateTracker#getForServer` | The saved-data lookup on every entity tick; the tracker is cached per server | The data and tracker are the same objects for the server's lifetime. The cached path still marks the data dirty, as the original does |

The portal and moving-block counts (`EntityCounters`) are kept from entity join/leave events. They can only err towards
"present": joins are counted first, and the count never goes below zero. So a miscount turns a gate off; it never
wrongly turns one on.

## Patches added in 1.2.0

These come from the post-fix profile of 2026-11-06 19:06–19:20, a 14-minute all-threads profile of the VM. All ~45 levels
tick in series, because DimThreads 1.2.1 holds one global lock around each level tick, so a per-level cost is paid about
45 times per tick.

| Switch | Target | What it skips | Why it's safe |
|---|---|---|---|
| `antlersDayCounterOverworldOnly` | Antlers `DayCounterProcedure#onWorldTick` | The procedure on every level except the overworld (server side) | **Changes behaviour (a bug fix).** Its counter lives in global saved data, so it counted ~45 ticks per tick and sent a full sync to every player each time (~900 packets/s per player). Now it counts one per tick. Its only effect is the one-time `wendigospawn` flag at day 2, which the Wendigo Flute reads (1.6% of the serial tick) |
| `towwEmptyLevelSyncSkip` | TOWW `TheOneWhoWatchesModVariables$WorldVariables#syncData` | Encoding the per-level sync packet for a level with no players. The data is still marked dirty | The packet is addressed to the players in that level, so with none it goes nowhere. The mod also syncs on login and on dimension change (2.3%) |

The existing gates were also made cheaper. Our own code was 2.7% of the serial tick:
- **Activation range:** the immunity check reads fire ticks directly instead of calling `isOnFire()`, which asks `fireImmune()`, which Create wraps with a persistent-data read. A fire-immune mob with fire ticks now counts as immune, which is more conservative.
- **Fragile Torches pre-check:** one chunk lookup for both blocks instead of two `getBlockState` calls. The entity's cached feet state isn't used, because `baseTick` clears it after this event fires.

## Patches added in 1.3.0 — horror look-check ray casts

**Why.** On 2026-11-06 a player sailing outran chunk loading, and the server stalled: one tick took 18.9 s, and the
Server thread spent 97 s in two minutes blocked. Several horror mods ask "is the player looking at our creature?" every
tick, for every player, by casting 15 short rays along the player's view, with 3 identical `Level.clip` calls per step.
Vanilla `clip` loads every chunk the ray touches, synchronously. So a ray reaching the not-yet-loaded chunk ahead of a
fast-moving player made the server thread load that chunk, or generate it beyond the pre-generated area. The worst
offender was Creeping Woods' Creature from the Woods (85 s in those two minutes), then Rake and Whispering Spirits. In
normal play these procedures cost about 2.2 ms per tick with 2 players.

| Switch | Targets (`execute` of each) | What it does | Why it's safe |
|---|---|---|---|
| `horrorRayCastLoadedOnly` | Creeping Woods `CreatureFromTheWoodsRaytracingProcedure`, `ShadowFigureDespawnProcedure`, `StatueMovementProcedure`; Rake `RakeRaytracingProcedure`; Whispering Spirits `PlayerLookAtSpiritProcedure`; John `WeepingJohnOnEntityTickUpdateProcedure`; The Obsessed `PlayerTickProcedure` | If any chunk in the ray's x/z span isn't a finished, loaded chunk right now (`getChunkNow`, which never waits), it returns vanilla's own "nothing hit" result instead of loading the chunk | Each procedure then looks for its creature near the hit point, and entities exist only in loaded chunks, so the answer can't change |
| `horrorRayCastDedupe` | Same | Within one procedure run, a cast identical to the one just before it reuses that result (same level, end points, block and fluid modes). The memo is cleared at the start of every run. | The three copies are evaluated back to back in one expression, with nothing changing the world in between |

All of it goes through `RayGuard`. It applies to server levels only: client-side calls reach vanilla unchanged.
`/tickfixes stats` reports guarded casts, duplicates reused, and casts stopped at unloaded chunks. Expect about two in
three to be duplicates, and stops only while someone moves faster than chunks load.

**1.3.1 fix.** 1.3.0 checked with `hasChunk`, but that only tests a chunk's *ticket level*. A chunk still loading or
generating ahead of a moving player passed the check, vanilla `clip` then blocked on it, and on 2026-11-06 at 22:05
the watchdog crashed the server after a 120 s tick (Rake's ray cast). 1.3.1 uses `getChunkNow`, which only returns a
chunk whose load has finished.

**1.3.2: The Obsessed waits for chunks** (`obsessedWaitForChunks`, `obsessedChunkRadius` = 3).
- **The problem:** its `PlayerTickProcedure` also reads single blocks (`isEmptyBlock`, `getBlockState`) at random offsets of up to about 20 blocks around the player. A read can't be answered without the chunk, so these blocked on chunks still being generated: 19 s in one minute while a player was in fresh terrain (22:17).
- **The fix:** the whole procedure is skipped for that player on any tick where a chunk within 3 chunks (48 blocks) isn't finished yet (`getChunkNow`, which never waits). It resumes once the area has loaded. This only happens on login or while moving into ungenerated terrain.
- **Monitoring:** `/tickfixes stats` counts the deferred ticks.
- **Radius 6 (2026-11-07; the default from 1.3.3).** 3 turned out too small. The reads that stalled come from its stalker spawn search, at a scan point that wanders about 37–80 blocks from the player (at most 5 chunks).

## Patches added in 1.3.3 — single-block reads that loaded chunks

| Switch | Target | What it does | Why it's safe |
|---|---|---|---|
| `villagerSleepLoadedOnly` | Vanilla `SleepInBed#checkExtraStartConditions` (`m_6114_`), the `getBlockState` of the villager's home | If the home's chunk isn't a finished, loaded chunk (`getChunkNow`), answer air instead of loading it | Vanilla reads the bed *before* checking that it is within 2 blocks. A ticking villager is in an entity-ticking chunk, and every chunk next to one is loaded, so a home in an unloaded chunk is always further than 2 blocks away, where vanilla returns false anyway. Air also gives false. Crash 2026-11-07 01:44: the watchdog stopped the server after this read waited 120 s for a chunk that still had to be generated |
| `obsessedReadsLoadedOnly` | The Obsessed `PlayerTickProcedure#execute`: all `isEmptyBlock` (33) and `getBlockState` (24) calls | A read in an unloaded chunk answers "not empty" / air | Its stalker spawn search reads at a scan point stored in the player's data. That point can sit near *another* player, or stay at the old location after a teleport, both outside `obsessedChunkRadius`. Every check that acts needs "empty", or a specific block (cobweb, cobweb stone, glass), or comes after an "empty" check on the same column. So the stub sends it down its own "no space here, move on" path, and it only spawns its stalker in loaded terrain |
| `offThreadChunkLog` (diagnostic) | Vanilla `ServerChunkCache#getChunk` (`m_7587_`), HEAD, priority 1500 (after Canary's overwrite) | When the caller isn't the server thread, logs the first request from each distinct caller with a 30-frame stack. The rest are counted (sampled 1 in 64 after 2000). Only observes | Vanilla runs those requests on the server thread between ticks. They caused the "Can't keep up! 21 s behind" warnings of 2026-11-06 23:03–23:11 without any long tick, and the server-thread profile can't show who asked. Grep the log for `off-thread chunk request (new caller)`. Turn it off once the caller is known |

`/tickfixes stats` adds the counts: villager bed reads skipped, Obsessed reads stubbed, and off-thread requests with the top callers. The plugin now also logs the two vanilla patches (`-> applying (vanilla)`): **31** applying lines in total.

## Activation range (1.1.0) — `[activationRange]`

This changes behaviour, for far mobs only. (The other behaviour change is the 1.2.0 Antlers day-counter fix.) Mobs more than `range` (48) blocks from every player
in their dimension tick once every `interval` (4) ticks, staggered by entity id. A skipped tick is skipped entirely, so
the mob moves, thinks and runs its own timers in slow motion. Two things still run on skipped ticks, so their rates
are unchanged:
- `AgeableMob` age (growth and breeding cooldowns);
- the vanilla despawn check.

**Never throttled:**
- non-mob entities;
- mobs with a target, hurt recently, burning, in lava, leashed, riding or ridden, or in a raid;
- `alwaysTickEntities` (vanilla bosses);
- `alwaysTickNamespaces` (MineColonies, Guard Villagers, the scripted horror mods, Mahou Tsukai).

MineColonies' wiki lists activation-range mods as conflicting, so it must stay in that list.

**Adaptive mode** (default on): throttling switches on while the server's average tick is above `enableAboveMspt`
(50) and off again below `disableBelowMspt` (30).

`/tickfixes stats` (op) prints whether it is active, how many far mobs were skipped last tick, the portal and
moving-block counts for your dimension, and the average tick time.

Verified headless: a baby cow kept ageing at exactly 20/s while throttled; a throttled pig fell far more slowly than
an immune (ridden) one; dormant mimics still slept.

Not yet done (see the audit doc): Alex's Caves' per-entity magnetism check, Slice & Dice wet air, the watcher despawn
rule (§5.5.2).

## Design

- **Fail-soft per target mod.** `TickFixesMixinPlugin` applies a patch only when its target mod is installed and logs
  one line per patch (`... -> applying (modid)`). Applied patches keep `defaultRequire: 1`, so a target that changed
  shape fails loudly at startup.
- **Switch per patch.** Each switch is read on the hot path, so a misbehaving patch can be turned off in the config
  without a rebuild. A config reload applies, except the watcher type default, which is set at startup. In practice Forge's file watcher hasn't fired on the VM or the test server, so expect config edits to apply on restart.
- **No network channels, no registry entries.** `displayTest = "IGNORE_ALL_VERSION"`: it can sit on the server only,
  but the pack ships it on both sides, which also speeds up singleplayer.
- **SRG names** are used directly with `remap = false`, as in `immptl_dimthread_compat`, so no refmap is needed.

## Build / deploy

`python tools/spooky_tick_fixes/build.py [--install]` builds with JDK 17 against the CurseForge install's libraries
and the target mod jars (shared steps: `tools/_javabuild.py`). No Gradle, no network. The version is set in
`resources/META-INF/mods.toml` and `MANIFEST.MF`, which must agree. Output: `build/spooky_tick_fixes-<version>.jar`;
`--install` copies it to `mods/` and `local/` and removes older versions there. Then run
`python scripts/sync_server_mods.py` to upload it to the VM, and restart the server.

Rebuild after updating any target mod. If a target changed, startup fails with a clear mixin error naming the patch.
Set that patch's switch to false, or remove it from `spooky_tick_fixes.mixins.json`.

**Revert:** delete the jar. Nothing is stored in worlds. The config file can stay.
