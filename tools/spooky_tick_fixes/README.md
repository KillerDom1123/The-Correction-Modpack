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

## Activation range (1.1.0) — `[activationRange]`

This is the one feature that changes behaviour, for far mobs only. Mobs more than `range` (48) blocks from every player
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
  without a rebuild. A config reload applies, except the watcher type default, which is set at startup.
- **No network channels, no registry entries.** `displayTest = "IGNORE_ALL_VERSION"`: it can sit on the server only,
  but the pack ships it on both sides, which also speeds up singleplayer.
- **SRG names** are used directly with `remap = false`, as in `immptl_dimthread_compat`, so no refmap is needed.

## Build / deploy

`python tools/spooky_tick_fixes/build.py [--install]` builds with JDK 17 against the CurseForge install's libraries
and the target mod jars. No Gradle, no network. Output: `build/spooky_tick_fixes-<version>.jar`; `--install` copies
it to `mods/` and `local/` and removes older versions there. Then run `scripts/sync_server_mods.py` (or upload it to the VM's `mods/`).

Rebuild after updating any target mod. If a target changed, startup fails with a clear mixin error naming the patch.
Set that patch's switch to false, or remove it from `spooky_tick_fixes.mixins.json`.

**Revert:** delete the jar. Nothing is stored in worlds. The config file can stay.
