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

Not in this pass (see the audit doc): Full Immersion portal-AI gating, Alex's Caves magnet gating, Slice & Dice,
Citadel, activation range.

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
and the target mod jars. No Gradle, no network. Output: `build/spooky_tick_fixes-1.0.0.jar`; `--install` copies it
to `mods/` and `local/`. Then run `scripts/sync_server_mods.py` (or upload it to the VM's `mods/`).

Rebuild after updating any target mod. If a target changed, startup fails with a clear mixin error naming the patch.
Set that patch's switch to false, or remove it from `spooky_tick_fixes.mixins.json`.

**Revert:** delete the jar. Nothing is stored in worlds. The config file can stay.
