# spooky_tick_fixes

A small Forge mod of behaviour-preserving server-tick fixes for the Spooky Dookie pack (Minecraft 1.20.1, Forge 47 or
later). Each patch skips work that provably cannot change the outcome. Four parts change behaviour on purpose, each
with its own switch: `[activationRange]` (far mobs tick less often while the server is behind),
`antlersDayCounterOverworldOnly` (a bug fix), `[antlersWendigo]` (the Wendigo keeps away and only attacks when
provoked) and `[sanityHorror]` (1.5.0, not installed: The Obsessed and CANTBREATHE only come at critical sanity, never
appear in a base, and must be looked at to be provoked).

- **Targets:** Artifacts, The One Who Watches (TOWW), The Obsessed, Whispering Spirits, Fragile Torches, Farm & Charm,
  Born in Chaos, Creeping Woods, Foes, Rake, Antlers (antlers-1.0.0), Ad Astra Extra, Immersive Portals Full
  Immersion, Alex's Caves, Citadel, John, CANTBREATHE, and vanilla. Each patch applies only when its target mod is
  installed.
- **Side:** server logic. It can sit on the server only; the pack ships it on both sides (see "Patch loading and
  switches").
- **Config:** `config/spooky_tick_fixes-common.toml`.

**Status:** source 1.5.0, built 2026-11-08, not installed. Installed on the client instance and the VM: 1.4.0. 1.5.0 is
not deployed and not boot-tested.

## Why

- **Server-tick cost.** Most patches implement the proposals of the server tick audit,
  `docs/SERVER_TICK_AUDIT_2026-11-06.md` §5.3, which holds the evidence, the measurements and the full proposal list.
  The activation range is its §5.5.1. Applied changes: CONFIG_CHANGES "2026-11-06 — Server tick audit: phases 0 and 1"
  and the entries listed under History.
- **Chunk-loading stalls.** Every watchdog crash and multi-second stall of 2026-11-06/07 had the same cause, in a
  different piece of code each time:
  - the horror look ray casts;
  - The Obsessed's spawn search;
  - the vanilla villager bed check;
  - Immersive Weathering's fluid generators;
  - Create Connected's rotation propagation.

  Each one, while a level ticked, read a block in a chunk that didn't exist yet. Vanilla generates such a chunk on the
  spot and the server thread waits. On the VM, with players exploring, that wait is seconds to minutes; at 120 s the
  watchdog stopped the server. The ray-cast guards, the chunk guards and chunk-safe ticking address this.
- **The Wendigo.** Antlers' Wendigo hunted on its own. Two players were attacked without warning on 2026-11-07, one in
  their base. It also never despawned. Design: aggressive when provoked, but it keeps away from people. CONFIG_CHANGES
  "2026-11-07 — Antlers Wendigo: keeps away, attacks only when provoked (spooky_tick_fixes 1.4.0)".
- **Sanity-bound horror** (1.5.0, not installed). Design: The Obsessed and CANTBREATHE only come for a player whose
  sanity is critical, never appear inside a base, and are provoked by being looked at. CONFIG_CHANGES "2026-11-08 —
  spooky_tick_fixes 1.5.0: The Obsessed and CANTBREATHE sanity-bound".

## How it works

### Patch loading and switches

- **Fail-soft per target mod.** `TickFixesMixinPlugin` applies a patch only when its target mod is installed and logs
  one line per patch and target class (`... -> applying (modid)`, or `(vanilla)` for the 4 vanilla patches;
  `CreepingWoodsRayMixin` has 3 targets). The 3 accessors and the activation-range hook aren't gated and log nothing.
  1.5.0 logs 44 lines (1.4.0: 38). Applied patches keep `defaultRequire: 1`, so a target that changed shape fails
  loudly at startup.
- **Switch per patch.** Each switch is read on the hot path, so a misbehaving patch can be turned off in the config
  without a rebuild. A config reload applies. In practice Forge's file watcher hasn't fired on the VM or the test
  server, so expect config edits to apply on restart. One exception: the watchers' follow-range default (see Limits).
- **No network channels, no registry entries.** `displayTest = "IGNORE_ALL_VERSION"`: it can sit on the server only,
  but the pack ships it on both sides, which also speeds up singleplayer.
- **SRG names** are used directly with `remap = false`, as in `immptl_dimthread_compat`, so no refmap is needed.
- **Own overhead.** The mod's own code was 2.7% of the serial tick in the VM profile of 2026-11-06 19:06–19:20. The
  activation-range immunity check and the Fragile Torches pre-check are built to be cheap for that reason (see their
  sections).

### Per-mob and per-player gates (`[patches]`)

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

**Fragile Torches pre-check:** one chunk lookup for both blocks instead of two `getBlockState` calls, and the lookup
never waits for a chunk. The entity's cached feet state isn't used, because `baseTick` clears it after this event fires.

### Dimension gates: portals, moving blocks, Citadel (`[patches]`)

| Switch | Target | What it skips | Why it's safe |
|---|---|---|---|
| `fullImmersionGate` | Full Immersion `PortalAiContext#begin`/`#finishAi`, `PortalDespawnCompatibility#shouldSuppressDespawn` | The per-mob portal-AI wrapper and the portal despawn check in a dimension with no Immersive Portals portal loaded | Projections and despawn suppression both need a `Portal` entity in the mob's level, and the mod indexes every one that joins. begin/finishAi are skipped as a pair (a per-mob flag), so its per-thread state can't go stale |
| `alexsCavesMovingBlockGate` | Alex's Caves `MagnetUtil#getMovingBlockCollisions`, `#isEntityOnMovingMetal` | The per-movement entity search for moving magnetic blocks in a dimension with none loaded | Both only look for `AbstractMovingBlockEntity` (and its subclass), so they return their empty answer |
| `citadelTrackerCache` | Citadel `ServerTickRateTracker#getForServer` | The saved-data lookup on every entity tick; the tracker is cached per server | The data and tracker are the same objects for the server's lifetime. The cached path still marks the data dirty, as the original does |

The portal and moving-block counts (`EntityCounters`) are kept from entity join/leave events. They can only err towards
"present": joins are counted first, and the count never goes below zero. So a miscount turns a gate off; it never
wrongly turns one on.

### Per-level work (`[patches]`)

Source: the post-fix profile of 2026-11-06 19:06–19:20, a 14-minute all-threads profile of the VM. At that time the VM
ran DimThreads 1.2.1, which held one global lock around each level tick, so all ~45 levels ticked in series and a
per-level cost was paid about 45 times per tick. DimThreads has been disabled since 2026-11-06 (CONFIG_CHANGES
"2026-11-06 — spooky_tick_fixes 1.3.0: horror look-check ray casts"). The costs below are still paid
once for every level that ticks.

| Switch | Target | What it skips | Why it's safe |
|---|---|---|---|
| `antlersDayCounterOverworldOnly` | Antlers `DayCounterProcedure#onWorldTick` | The procedure on every level except the overworld (server side) | **Changes behaviour (a bug fix).** Its counter lives in global saved data, so it counted ~45 ticks per tick and sent a full sync to every player each time (~900 packets/s per player). With the patch it counts one per tick. Its only effect is the one-time `wendigospawn` flag at day 2, which the Wendigo Flute reads (1.6% of the serial tick) |
| `towwEmptyLevelSyncSkip` | TOWW `TheOneWhoWatchesModVariables$WorldVariables#syncData` | Encoding the per-level sync packet for a level with no players. The data is still marked dirty | The packet is addressed to the players in that level, so with none it goes nowhere. The mod also syncs on login and on dimension change (2.3%) |

### Activation range (`[activationRange]`)

Changes behaviour, for far mobs only. Mobs more than `range` (48) blocks from every player in their dimension tick once
every `interval` (4) ticks, staggered by entity id. A skipped tick is skipped entirely, so the mob moves, thinks and
runs its own timers in slow motion. Two things still run on skipped ticks, so their rates are unchanged:
- `AgeableMob` age (growth and breeding cooldowns), switch `ageCatchUp`;
- the vanilla despawn check, switch `despawnChecks`.

**Never throttled:**
- non-mob entities;
- mobs with a target, hurt recently, burning, in lava, leashed, riding or ridden, or in a raid;
- `alwaysTickEntities` (vanilla bosses);
- `alwaysTickNamespaces` (MineColonies, Guard Villagers, the scripted horror mods, Mahou Tsukai).

MineColonies' wiki lists activation-range mods as conflicting, so it must stay in that list.

**Immunity check:** it reads fire ticks directly instead of calling `isOnFire()`, which asks `fireImmune()`, which
Create wraps with a persistent-data read. A fire-immune mob with fire ticks counts as immune, which is more
conservative.

**Adaptive mode** (default on): throttling switches on while the server's average tick is above `enableAboveMspt`
(50) and off again below `disableBelowMspt` (30).

`/tickfixes stats` shows its state (see Commands).

### Horror look-check ray casts (`horrorRayCastLoadedOnly`, `horrorRayCastDedupe`)

**The problem.** Several horror mods ask "is the player looking at its creature?" every tick, for every player, by
casting 15 short rays along the player's view, with 3 identical `Level.clip` calls per step. Vanilla `clip` loads every
chunk the ray touches, synchronously. So a ray reaching the not-yet-loaded chunk ahead of a fast-moving player made the
server thread load that chunk, or generate it beyond the pre-generated area. On 2026-11-06 a player sailing outran
chunk loading, and the server stalled: one tick took 18.9 s, and the Server thread spent 97 s in two minutes blocked.
The worst offender was Creeping Woods' Creature from the Woods (85 s in those two minutes), then Rake and Whispering
Spirits. In normal play these procedures cost about 2.2 ms per tick with 2 players.

| Switch | Targets (`execute` of each) | What it does | Why it's safe |
|---|---|---|---|
| `horrorRayCastLoadedOnly` | Creeping Woods `CreatureFromTheWoodsRaytracingProcedure`, `ShadowFigureDespawnProcedure`, `StatueMovementProcedure`; Rake `RakeRaytracingProcedure`; Whispering Spirits `PlayerLookAtSpiritProcedure`; John `WeepingJohnOnEntityTickUpdateProcedure`; The Obsessed `PlayerTickProcedure` | If any chunk in the ray's x/z span isn't a finished, loaded chunk at that moment (`getChunkNow`, which never waits), it returns vanilla's own "nothing hit" result instead of loading the chunk | Each procedure then looks for its creature near the hit point, and entities exist only in loaded chunks, so the answer can't change |
| `horrorRayCastDedupe` | Same | Within one procedure run, a cast identical to the one just before it reuses that result (same level, end points, block and fluid modes). The memo is cleared at the start of every run. | The three copies are evaluated back to back in one expression, with nothing changing the world in between |

All of it goes through `RayGuard`. It applies to server levels only: client-side calls reach vanilla unchanged.
`/tickfixes stats` reports guarded casts, duplicates reused, and casts stopped at unloaded chunks. Expect about two in
three to be duplicates, and stops only while someone moves faster than chunks load.

**Why `getChunkNow`:** it only returns a chunk whose load has finished. `hasChunk` only tests a chunk's *ticket
level*, so a chunk still loading or generating ahead of a moving player passes it, and vanilla `clip` then blocks on
it. Through that gap (1.3.0) the watchdog crashed the server on 2026-11-06 at 22:05 after a 120 s tick (Rake's ray
cast).

### Chunk guards for single-block reads (`obsessedWaitForChunks`, `obsessedReadsLoadedOnly`, `villagerSleepLoadedOnly`, `offThreadChunkLog`)

**The Obsessed waits for chunks** (`obsessedWaitForChunks`, `obsessedChunkRadius` = 6).
- **The problem:** its `PlayerTickProcedure` also reads single blocks (`isEmptyBlock`, `getBlockState`) at random
  offsets of up to about 20 blocks around the player. A read can't be answered without the chunk, so these blocked on
  chunks still being generated: 19 s in one minute while a player was in fresh terrain (2026-11-06 22:17).
- **The fix:** the whole procedure is skipped for that player on any tick where a chunk within `obsessedChunkRadius`
  chunks isn't finished yet (`getChunkNow`, which never waits). It resumes once the area has loaded. This only happens
  on login or while moving into ungenerated terrain.
- **Radius:** the default is 6 chunks (96 blocks) since 1.3.3; the pack's config has used 6 since 2026-11-07. The
  reads that stalled come from its stalker spawn search, at a scan point that wanders about 37–80 blocks from the
  player (at most 5 chunks), so 6 gives one chunk of margin. A radius of 3 (48 blocks) misses those reads. The check
  costs 169 chunk lookups per player per tick.
- **Monitoring:** `/tickfixes stats` counts the deferred ticks.

| Switch | Target | What it does | Why it's safe |
|---|---|---|---|
| `villagerSleepLoadedOnly` | Vanilla `SleepInBed#checkExtraStartConditions` (`m_6114_`), the `getBlockState` of the villager's home | If the home's chunk isn't a finished, loaded chunk (`getChunkNow`), answer air instead of loading it | Vanilla reads the bed *before* checking that it is within 2 blocks. A ticking villager is in an entity-ticking chunk, and every chunk next to one is loaded, so a home in an unloaded chunk is always further than 2 blocks away, where vanilla returns false anyway. Air also gives false. Crash 2026-11-07 01:44: the watchdog stopped the server after this read waited 120 s for a chunk that still had to be generated |
| `obsessedReadsLoadedOnly` | The Obsessed `PlayerTickProcedure#execute`: all `isEmptyBlock` (33) and `getBlockState` (24) calls | A read in an unloaded chunk answers "not empty" / air | Its stalker spawn search reads at a scan point stored in the player's data. That point can sit near *another* player, or stay at the old location after a teleport, both outside `obsessedChunkRadius`. Every check that acts needs "empty", or a specific block (cobweb, cobweb stone, glass), or comes after an "empty" check on the same column. So the stub sends it down its own "no space here, move on" path, and it only spawns its stalker in loaded terrain |
| `offThreadChunkLog` (diagnostic) | Vanilla `ServerChunkCache#getChunk` (`m_7587_`), priority 1500 (after Canary's overwrite), at the off-thread branch only: Canary's `getChunkOffThread` call, or vanilla's `supplyAsync`. Main-thread chunk lookups don't pay for it | When the caller isn't the server thread, logs the first request from each distinct caller with a 30-frame stack. The rest are counted (sampled 1 in 64 after 2000). Only observes | Vanilla runs those requests on the server thread between ticks. They caused the "Can't keep up! 21 s behind" warnings of 2026-11-06 23:03–23:11 without any long tick, and the server-thread profile can't show who asked. Grep the log for `off-thread chunk request (new caller)`. Turn it off once the caller is known |

`/tickfixes stats` adds the counts: villager bed reads skipped, Obsessed reads stubbed, and off-thread requests with the
top callers.

### Chunk-safe ticking (`[chunkSafeTicking]`)

It covers every chunk-loading stall listed under Why at once, and whatever else does the same:
- **Where:** `LevelReadGuardMixin` redirects the chunk lookup inside `Level.getBlockState` and `Level.getFluidState`.
  Canary overwrites `getBlockState`, so the mixin has priority 1500 to apply after it.
- **When:** while a server level ticks (from the first `LevelTickEvent` START listener to the last END one), on the
  server thread.
- **What happens to a read:**

| The chunk is… | Answer |
|---|---|
| loaded | unchanged, through a 4-entry cache that's cleared every tick and on chunk unload |
| finished, in memory but not accessible | vanilla (it gets a ticket; nothing is generated) |
| finished on disk (`Status` = full, read with the chunk scanner; the region file is checked first) | vanilla (a disk load). The check waits at most `diskCheckMillis` (50); its answer is cached for 60 s (5 s if it timed out). With `allowDiskLoads = false`, a barrier instead, for every chunk that isn't loaded |
| not generated, half generated, or being generated at that moment | **a barrier block, no fluid**, at once |

**Why a barrier, not air:** nothing spawns, places blocks or flows into terrain that doesn't exist, and ray casts stop
there.

**Untouched:** player packets, commands, worldgen and other threads, and main-thread chunk tasks (promotion to a full
chunk and its post-processing, via `ChunkTaskSuspendMixin`).

**So the only difference from vanilla:** ungenerated terrain is never generated just because something ticking looked
at it. That is generation the pre-generator (`spooky_pregen`) or a player's own chunk loading would do anyway, off the
server thread.

**Monitoring and escape hatches:**
- **First reads are logged** (`logCallers`): the first barrier read from each caller is logged with its stack
  (`chunk-safe ticking: … new caller`).
- **`/tickfixes stats`** counts barriers, disk loads and memory loads.
- **`/tickfixes probe <x> <z>`** performs one read at y 64 inside the next tick of your dimension and reports the path
  it took.
- **`exemptCallers`** (class-name prefixes) lets a mod that really must generate on the spot keep doing so. A prefix
  is matched against the first stack frame outside the JDK, Forge, Mixin, Canary and the level read itself (`Level`,
  `BlockGetter`, `LevelReader`, `LevelAccessor`, `ServerLevel.getBlockState`). Other vanilla classes aren't skipped,
  so that frame can be one such as `FlowingFluid` rather than the mod that started the work.
- **`enabled = false`** restores vanilla.

### The Wendigo (`[antlersWendigo]`)

**Changes gameplay.** Applies to Antlers' Wendigo (night form, `antlers:wendigo`) and, for despawning, its daytime form.

| Mod behaviour (antlers-1.0.0) | With `enabled = true` |
|---|---|
| Tick procedure, at night: about every 25 ticks, target the **nearest** player within a 600-block box (±300), no line of sight needed, sprint, Speed III within ±150. A player who logs off hands the hunt to the next nearest | Removed. A Wendigo only targets the player who provoked it |
| Player target goal (`WendigoEntity$3`, night): any survival player it can see within its 60-block follow range | Never runs |
| Stalking roar: `playsound antlers:wendigo_calls_out hostile @p ~ ~ ~ 100 1` (the nearest player within ±355, heard up to 1,600 blocks), about once a minute | Removed. It roars only while provoked: every 15–30 s, from where it is, heard within `soundRange` |
| "I see you" (`SetstargetProcedure`): volume 40 on the **Master** channel, everyone within 640 blocks | Capped to `soundRange` (128 blocks) |
| `removeWhenFarAway` always false for the Wendigo and its daytime form | `despawnNormally = true`: despawn like any monster (instantly past 128 blocks from every player, randomly past 32) |

**Calm:** `WendigoKeepAwayGoal` moves it away from any player it can see within `keepAwayRange` (16). This mirrors its
daytime form, which already runs from players. The goal picks a random spot up to 16 blocks away that is further from
the player, moves at 1.5× (1.9× within 7 blocks), and re-paths once a second while followed. It sits at goal priority 1,
ahead of the mod's stroll, look-at-player and melee goals, and holds MOVE and LOOK, so it doesn't stand watching a
player it is leaving.

**What provokes it** (survival/adventure players only):
- **Hitting it**, by hand or with a projectile the player shot, day or night. The newest attacker becomes its target.
- **At night, being within `provokeRange` (8) blocks** where it can see you; `provokeRangeSneaking` (4) while sneaking.
  Below the keep-away range this only happens when a player surprises it (round a corner, through a door), corners
  it, or runs it down.
- **At night, staring at it** for `stareSeconds` (5, continuous) from up to `stareRange` (24) blocks. Your crosshair has
  to be on its body, the line of sight has to be clear both ways, and it must not be walking away from someone at the
  time. It stops to watch players from afar (the mod's 70-block look goal), so holding its gaze is the provocation.

**Once provoked** it chases only that player. Setting the target runs the mod's own reaction: within ~12 blocks of a
survival player, once per Wendigo, it freezes for 12.5 s (Slowness 101) and screams, then charges. It sprints, so the
mod gives it Speed III. It **calms down** when the provoker leaves the game or the dimension, dies, switches to
creative/spectator, or gets further than `calmRange` (32).

**After calming down** (`vanishAfterCalm`), it retreats from every player within 48 blocks, seen or not. Only a hit
provokes it again. It vanishes once no player is within 24 blocks and none within 64 can see it, or after 60 s once none
is within 16.

The mod's other rules are unchanged:
- after its scream it removes itself once no player is within ~30 blocks (hit and run);
- at dawn it turns into its daytime form, which runs from players and only fights back;
- at dusk it turns back. Each swap creates a fresh, calm mob.

**Hooks:**
- `WendigoHuntMixin`: three `@Redirect`s in `WendigoOnEntityTickUpdateProcedure#execute`, on `Mob.setTarget`,
  `Entity.setSprinting` and `Commands.performPrefixedCommand`. Each call appears once in the method.
- `WendigoPlayerTargetGoalMixin`: `WendigoEntity$3` `canUse` and `canContinueToUse` return false.
- `WendigoEntityMixin`:
  - `baseTick` HEAD runs `WendigoTemper.tick`;
  - `registerGoals` TAIL adds `WendigoKeepAwayGoal` at priority 1;
  - `removeWhenFarAway` returns true.
- `WendigomadEntityMixin`: the same despawn rule for the daytime form.
- `WendigoScreamRangeMixin`: `@ModifyArg` on the two `Level.playSound` volumes in `SetstargetProcedure`.

The state is kept in memory only (see Limits). `/tickfixes wendigo` lists the loaded Wendigos and their states (see
Commands). The log gets one line per event: `wendigo at x y z provoked by <player>: <reason>`,
`... calmed down: ...; retreating` and `... vanished after calming down`.

### Sanity-bound horror (`[sanityHorror]`) (1.5.0, not installed)

Changes behaviour. Three rules, applied to both mods:
1. They only come for a player whose sanity is **critical**.
2. They never **appear inside a base**.
3. They are **provoked by being looked at**, not by seeing you or by you coming near.

**Sanity** comes from the pack's own mechanic (`kubejs/server_scripts/oc_sanity.js`, `docs/sanity_mechanic.md`).
- **Where it is stored:** the script keeps it in the player's KubeJS persistent data. That is KubeJS's own tag (`kjs$persistentData`, saved as `KubeJSPersistentData`), not Forge's persistent data. `SanityGate` reads it through `kjs$getPersistentData()`, looked up once by reflection.
- **What counts as critical:** the band the script stores (`oc_sanity_band`, which already has its 5-point hysteresis) at or past `criticalBand`. The default is 3, Breakdown: you enter it below 15 and leave it at 20.
- **Fallback:** a player the script hasn't run for, or a server without KubeJS, counts as sane.

**A base** (`HorrorBases`) is either of these:
- within `baseRadius` (48) blocks horizontally and `baseVerticalRange` (32) vertically of any online player's respawn point (bed or anchor) in that dimension;
- with `baseColonies`, inside a MineColonies colony's claimed chunks, within `baseVerticalRange` of the colony centre's height.

MineColonies' lookup loads the chunk it asks about, so unloaded chunks count as no colony. Nothing spawns there anyway.

**The Obsessed** (how the mod schedules things: CONFIG_CHANGES "2026-11-08 — spooky_tick_fixes 1.5.0: The Obsessed and
CANTBREATHE sanity-bound"):

| What | The mod | With `[sanityHorror]` |
|---|---|---|
| Targeter (first contact) appears | Per-player timer (3 min in the pack's config), at a dark spot 40–80 blocks away, behind you | Same, but only for a critical player. While you're not critical the timer is held just short of zero, so it comes 10–60 s after a breakdown starts if it was due. Not inside a base |
| Targeter locks on (you become its target) | It has line of sight to the nearest player within ±100 blocks, who isn't sneaking, immune or repellant-protected | That player has to **look at it**: vanilla's Enderman stare test (view within acos(1 − 0.025/d) of its eyes, about 1.7° at 60 blocks) with a clear line of sight. They must also be critical. The mod's other conditions still apply |
| Stalk spawn (the target, every few minutes) | Day or night, anywhere, 37–70 blocks away while you face away | Only while the target is critical; otherwise it's retried every ~20 s (the mod's own 100-point delay). A spot inside a base is moved out of it along the line from the base's centre, then retried. Both checks run before the mod's spawn bookkeeping, so a blocked stalk doesn't advance its mood or timers |
| Behind-you / ceiling-spider / nightmare-spider hallucinations (can follow a stalk) | Behind the player | Not inside a base |
| Block-break surprise (1 in 100, then 53 min cooldown) | Any block you break | Only while critical and outside a base. A blocked one still uses up the cooldown |
| **Lurker** at your windows (within 14 blocks of your respawn point) | Needs an obsession | Unchanged |
| **Sleep-paralysis** hallucination in bed | Needs an obsession | Unchanged |
| Saviour, opportunistic and the other forms | Transformations of an Obsessed that is already there | Unchanged (the one already there needed rules 1 and 2) |

Its intruder form can still walk into a base after spawning outside it (see Limits).

**CANTBREATHE:**
- **Timer:** it still runs on any survival player's time in the Overworld. `isRelevantPlayer` is untouched, so the
  cadence (mostly exactly 1 h in the pack's config) stays the cap.
- **Victim:** picked first among critical players. A spawn attempt for a victim who isn't critical fails, the mod clears
  the victim, and it picks again next tick. When the timer is due, a critical player gets it within about 20 s plus
  the spot search.
- **Cave noise:** skipped for a victim who isn't critical (the noise timer is still reset). It only reaches critical
  players, and it plays at the victim's position (about 32 blocks of range).
- **Spawn spots:** the mod tries 40 spots within 35 blocks of the victim; spots inside a base fail its spawn-rule check.
- **Provocation:** `CantBreatheTargetTooCloseGoal` (any player within 15 blocks made it chase, seen or not) never
  starts. What still provokes it: looking at it (its own goal, the nearest player within 128 blocks, view within about
  8°, clear line of sight, then a roll of chase / stare / stare / flee), its stare goal (looking at it repeatedly), and
  hitting it. Anyone can provoke it this way, not just critical players: it is physically there.
- **Unchanged:** door breaking (1 s), the 1,000-block spotting range, leaving after 650 s or 60 s into a chase.

**Hooks** (all `remap = false`, each target checked against the jars, single call sites unless noted):
- `obsessed.ObsessedSanityTickMixin` (`PlayerTickProcedure#execute`, the private one with the event): HEAD; an
  `@Inject` at the one `GETSTATIC TheObsessedModEntities.IDK_STALKER_SPAWN`, cancellable; an `@Redirect` on all
  12 `EntityType.spawn` calls (`m_262496_`).
- `obsessed.ObsessedTargeterLookMixin` (`IdkStalkerTargeterOnEntityTickUpdateProcedure#execute`): `@Redirect` on
  `LivingEntity.hasLineOfSight`.
- `obsessed.ObsessedSurpriseMixin` (`BlockBrokenProcedure#execute`): HEAD and an `@Redirect` on `EntityType.spawn`.
  The rest of the method (no block breaking in its lair) is untouched.
- `cantbreathe.CantBreatheLogicMixin` (`CantBreathe`): `@Redirect` on `List.get` (the victim pick) and
  `Utils.trySpawnMob` in `handleLogic`; HEAD (cancellable) and an `@Redirect` on `List.forEach` in
  `playCaveSoundToSpelunkers`.
- `cantbreathe.CantBreatheSpawnSpotMixin` (`Utils#trySpawnMob`): `@Redirect` on `Mob.checkSpawnRules`.
- `cantbreathe.CantBreatheTooCloseMixin` (`CantBreatheTargetTooCloseGoal#canUse`): HEAD, returns false.

The PlayerTickProcedure hooks only act on the logical server. In singleplayer the client thread runs the same
procedure, and it must not overwrite the player the server thread is handling.

Nothing is written to the world beyond the timers the mods already keep in player data (`targeter_spawntime`,
`idk_spawn`, `idk_x`/`idk_z`). The counters run from server start. `/tickfixes horror` shows the switches, the counters,
each online player's sanity, base status and Obsessed state, and CANTBREATHE's victim and timers (see Commands).

## Configuration

File: `config/spooky_tick_fixes-common.toml`. **Pack** is the value in the pack's copy of that file (2026-11-08). The
pack's file has no `[sanityHorror]` section: Forge adds it with the defaults on the first 1.5.0 start.

**`[patches]`**

| Key | Default (range) | Pack | Meaning |
|---|---|---|---|
| `mimicSleep` | true | default | A dormant mimic skips its tick while no player is within `mimicWakeRange` |
| `mimicWakeRange` | 24.0 (17–128) | default | Blocks. Must stay above the mimic's 16-block follow range |
| `watcherFollowRange` | true | default | Cap the follow range of the stationary TOWW watchers |
| `watcherFollowRangeCap` | 32.0 (8–2048) | default | Follow range for those watchers |
| `obsessedSyncCoalesce` | true | default | The Obsessed: one player-variable sync per player per tick |
| `spiritLookGate` | true | default | Whispering Spirits: skip the look check with no spirit loaded in the dimension |
| `fragileTorchesPrecheck` | true | default | Fragile Torches: check for a fragile torch before the list parse |
| `farmCharmSyncOnChange` | true | default | Farm & Charm: saturation sync only on change, on refresh and on start-tracking |
| `farmCharmRefreshTicks` | 100 (20–1200) | default | Ticks between forced refreshes of an unchanged value |
| `animationGates` | true | default | MCreator animation handlers skip other mods' mobs |
| `adAstraExtraGate` | true | default | Ad Astra Extra: skip mobs that are neither gassed nor on Jupiter |
| `fullImmersionGate` | true | default | Full Immersion: skip portal AI and the portal despawn check with no portal in the dimension |
| `alexsCavesMovingBlockGate` | true | default | Alex's Caves: skip the moving-block search with none in the dimension |
| `citadelTrackerCache` | true | default | Citadel: cache the tick-rate tracker per server |
| `antlersDayCounterOverworldOnly` | true | default | Antlers: day counter on the overworld's tick only. Changes behaviour |
| `towwEmptyLevelSyncSkip` | true | default | TOWW: skip the world-variable sync for levels with no players |
| `horrorRayCastLoadedOnly` | true | default | Horror look-check rays touching an unloaded chunk return "nothing hit" |
| `horrorRayCastDedupe` | true | default | Reuse an identical ray cast made just before in the same run |
| `obsessedWaitForChunks` | true | default | The Obsessed: skip its player tick while nearby chunks are still loading |
| `obsessedChunkRadius` | 6 (1–8) | default | Chunks around the player that must be loaded |
| `obsessedReadsLoadedOnly` | true | default | The Obsessed: block reads in unloaded chunks answer "not empty" / air |
| `villagerSleepLoadedOnly` | true | default | Villager bed check answers air for a bed in an unloaded chunk |
| `offThreadChunkLog` | true | default | Diagnostic: log the first off-thread chunk request per caller |

**`[chunkSafeTicking]`**

| Key | Default (range) | Pack | Meaning |
|---|---|---|---|
| `enabled` | true | default | Master switch |
| `allowDiskLoads` | true | default | Load chunks fully generated on disk, as vanilla would. false = barrier for every unloaded chunk |
| `diskCheckMillis` | 50 (5–1000) | default | Longest wait for the on-disk status check before answering with a barrier |
| `logCallers` | true | default | Log the first barrier read from each caller, with its stack |
| `exemptCallers` | `[]` | default | Class-name prefixes that may still generate terrain synchronously |

**`[antlersWendigo]`**

| Key | Default (range) | Pack | Meaning |
|---|---|---|---|
| `enabled` | true | default | Master switch. false = the mod's own hunting, stalking roar (1,600 blocks) and scream range (640 blocks) |
| `keepAwayRange` | 16.0 (0–32) | default | While calm it moves away from any player it can see within this range. 0 = off |
| `provokeRange` | 8.0 (0–32) | default | At night, a player this close in its line of sight provokes it. 0 = off. Keep it below `keepAwayRange` |
| `provokeRangeSneaking` | 4.0 (0–32) | default | The same while sneaking. 0 = sneaking players never provoke it by coming close |
| `stareRange` | 24.0 (0–64) | default | At night, staring from this far or closer for `stareSeconds` provokes it. 0 = off |
| `stareSeconds` | 5.0 (0.5–20) | default | Seconds of continuous staring. Looking away resets it |
| `calmRange` | 32.0 (16–256) | default | It calms down when its provoker is further away than this |
| `vanishAfterCalm` | true | default | After calming down it retreats and vanishes. false = it stays, calm |
| `soundRange` | 128.0 (16–640) | default | Range of its roar (only while provoked) and of its "I see you" scream |
| `despawnNormally` | true | default | The Wendigo and its daytime form despawn like other monsters. Works without `enabled` |

**`[sanityHorror]`** (1.5.0, not installed)

| Key | Default (range) | Pack | Meaning |
|---|---|---|---|
| `criticalBand` | 3 (1–3) | not in file | The sanity band that counts as critical, or worse: 1 Unease (70 and below), 2 Dread (40 and below), 3 Breakdown (below 15; a player leaves it at 20) |
| `baseRadius` | 48 (0–256) | not in file | Blocks, horizontally, from an online player's respawn point that count as their base. 0 = beds don't count |
| `baseVerticalRange` | 32 (4–384) | not in file | Blocks above or below the respawn point (or the colony centre) that still count as the base |
| `baseColonies` | true | not in file | A MineColonies colony's claimed chunks also count as a base |
| `obsessedSanityGate` | true | not in file | The Obsessed: targeter, lock-on, stalks and the block-break surprise need a critical player |
| `obsessedLookToProvoke` | true | not in file | The Obsessed: its targeter locks on when the player looks at it |
| `obsessedNoBaseSpawns` | true | not in file | The Obsessed: no stalk, targeter, hallucination or surprise spawns inside a base |
| `cantBreatheSanityGate` | true | not in file | CANTBREATHE: spawns and cave noise only for a critical victim |
| `cantBreatheLookToProvoke` | true | not in file | CANTBREATHE: no chase just because a player came within 15 blocks |
| `cantBreatheNoBaseSpawns` | true | not in file | CANTBREATHE: spawn spots inside a base are rejected |

**`[activationRange]`**

| Key | Default (range) | Pack | Meaning |
|---|---|---|---|
| `enabled` | true | default | Master switch |
| `range` | 48.0 (16–256) | default | Blocks from the nearest player in the same dimension beyond which a mob is far |
| `interval` | 4 (2–20) | default | A far mob ticks once every this many ticks (staggered by entity id) |
| `adaptive` | true | default | Only throttle while the server is behind |
| `enableAboveMspt` | 50.0 (1–1000) | default | Average tick (ms) above which throttling switches on |
| `disableBelowMspt` | 30.0 (0–1000) | default | Average tick (ms) below which throttling switches off |
| `ageCatchUp` | true | default | Advance `AgeableMob` age on skipped ticks |
| `despawnChecks` | true | default | Run the vanilla despawn check on skipped ticks |
| `alwaysTickEntities` | `minecraft:ender_dragon`, `wither`, `warden`, `elder_guardian` | default | Entity ids that always tick at full rate |
| `alwaysTickNamespaces` | `minecolonies`, `guardvillagers`, `the_one_who_watches`, `rake_the_arrival`, `the_obsessed`, `cave_dweller`, `weeping_angels`, `pale_hound`, `cant_breathe`, `themimic_er`, `whispering_spirits`, `antlers`, `john_mod_reborn`, `creepingwoods`, `man`, `mahoutsukai` | default | Mod namespaces whose entities always tick at full rate |

## Commands

Operators only (permission level 2). The server console works too, and acts in the overworld.

| Command | Permission | Effect |
|---|---|---|
| `/tickfixes stats` | 2 | One line of status and counters for every gate and guard |
| `/tickfixes wendigo` | 2 | The `[antlersWendigo]` patch and each loaded Wendigo's state |
| `/tickfixes horror` (1.5.0, not installed) | 2 | The `[sanityHorror]` switches, counters, and each online player's sanity, base and Obsessed state |
| `/tickfixes probe <x> <z>` | 2 | One chunk-safe read at y 64 in the next tick of your dimension, and the path it took |

- **`/tickfixes stats`**: no arguments. Replies with one line; all counts run from server start:
  - **activation range:** enabled, active, the adaptive thresholds, range, interval, and far mobs skipped last tick;
  - **your dimension:** loaded Immersive Portals portals and Alex's Caves moving blocks;
  - the server's **average tick time**;
  - **horror ray casts:** guarded, duplicates reused, stopped at unloaded chunks, and Obsessed ticks deferred;
  - **chunk guards:** villager bed reads and Obsessed block reads answered without loading, and off-thread chunk
    requests with the top callers;
  - **chunk-safe ticking:** reads answered with a barrier and their top callers, disk loads, memory loads, and disk
    checks that timed out.
- **`/tickfixes wendigo`**: no arguments. One line for the patch (on/off, `despawnNormally`, provocations, calm-downs
  and vanishings since start), then one line per loaded Wendigo: dimension and position, distance from you (in your
  own dimension only), and its state: `calm`, `calm, keeping away from a player`, `provoked by <player> (<how>)`, or
  `calmed down, retreating`, with ` (moving away)` while it walks off. With none it says `no Wendigo loaded`. Only
  Wendigos that have ticked with `[antlersWendigo] enabled = true` are listed, so with the patch off the list is always
  empty.
- **`/tickfixes horror`** (1.5.0, not installed): no arguments.
  - **First line:** the critical band, the base definition and the six switches.
  - **Then the counters since start:**
    - The Obsessed: targeter holds, targeters blocked (sane / base), lock-ons refused (sane), stalks held (sane), stalks moved out of a base, stalks allowed, hallucinations blocked (base), surprises blocked (sane / base);
    - CANTBREATHE: critical victims picked, attempts refused (sane victim), spots rejected (base), noises muted.
  - **One line per online player:** their sanity (value, band, `critical`), whether they stand in a base (and whose respawn point or which colony), and their Obsessed state (`Obsessed target, stalk timer N`, or `not targeted, targeter timer N`).
  - **Last line:** CANTBREATHE's overworld victim and its spawn and noise timers (seconds elapsed / target).
- **`/tickfixes probe <x> <z>`**: tests chunk-safe ticking at one position.
  - **`x`, `z`:** whole-number block coordinates in your dimension. y is always 64.
  - **What it does:** queues one `getBlockState` read for the start of your dimension's next tick, inside the guarded
    window. It replies `probe queued…`, then `probe x, 64, z in <dimension>: <block> via <path> (<µs>)`, where the path
    is one of:
    - `fast path (chunk loaded)`;
    - `loaded from memory`;
    - `loaded from disk`;
    - `barrier (chunk is not generated / is being generated / could not be checked in time / is not loaded)`; the
      last only with `allowDiskLoads = false`;
    - `exempt caller …, vanilla`.
  - **Side effects:** probing ungenerated terrain doesn't generate it. Probing a generated but unloaded chunk loads it
    briefly, as any read would.
  - **One at a time:** only one probe is pending; a new one replaces a probe that hasn't run yet.
  - **When `[chunkSafeTicking] enabled = false`:** the reply says so, and the read takes vanilla's path.

## Limits

- **Deliberate behaviour changes** (each has its own switch; see Configuration):
  - `[activationRange]`: far mobs move, think and run their own timers in slow motion while the server is behind.
  - `antlersDayCounterOverworldOnly`: Antlers' day counter counts at the real pace.
  - `[antlersWendigo]`: the Wendigo no longer hunts, keeps away from players, attacks only a player who provoked it, and
    despawns like other monsters.
  - `[sanityHorror]` (1.5.0, not installed): The Obsessed and CANTBREATHE only come for critical players, never spawn
    in a base, and need to be looked at to be provoked.
- **Watcher follow-range default:** the watchers' lower follow-range *default* is set when the game registers entity
  attributes, before the config is read. `watcherFollowRange = false` stops the clamp on saved watchers, but new
  watchers still get 32. Only removing the jar restores 2048.
- **Chunk-safe ticking:** a ticking read of terrain that doesn't exist yet answers a barrier block, so nothing spawns,
  places blocks or flows there until the pre-generator or a player's own chunk loading generates it.
- **The Wendigo's state is in memory only.** Nothing is written to the world, and a Wendigo that unloads comes back
  calm.
- **The Obsessed's intruder form** (1.5.0, not installed) can still walk into a base after spawning outside it: it
  pathfinds to the target's respawn point, opens doors, swaps torches and can take one item from a chest. "No spawn in
  a base" doesn't cover that.
- **Not implemented** (proposals in `docs/SERVER_TICK_AUDIT_2026-11-06.md`): Alex's Caves' per-entity magnetism check,
  Slice & Dice wet air, The Obsessed's `EntityTickProcedure` (§5.3.7), the watcher despawn rule (§5.5.2).

## Build, deploy and revert

`python tools/spooky_tick_fixes/build.py [--install]` builds with JDK 17 against the CurseForge install's libraries
and the target mod jars (shared steps: `tools/_javabuild.py`). No Gradle, no network. The version is set in
`resources/META-INF/mods.toml` and `MANIFEST.MF`, which must agree. Output: `build/spooky_tick_fixes-<version>.jar`;
`--install` copies it to `mods/` and `local/` and removes older versions there. Then run
`python scripts/sync_server_mods.py` to upload it to the VM, and restart the server.

**Installing 1.5.0** (not installed as of 2026-11-08; the jar is `build/spooky_tick_fixes-1.5.0.jar`):
1. With the game closed (a running game holds the old jar open), `build.py --install` replaces 1.4.0 in `mods/` and
   `local/`.
2. Boot a headless server. Any mixin error stops startup with the patch named.
3. `python scripts/sync_server_mods.py`, then restart the VM.

Forge adds the `[sanityHorror]` section with its defaults on the first 1.5.0 start. It is not added to the config file
by hand: a running 1.4.0 could strip unknown keys on a reload. The server can run 1.5.0 alone; clients only gain
singleplayer behaviour.

Rebuild after updating any target mod. If a target changed, startup fails with a clear mixin error naming the patch.
The config switches can't prevent this: they act at runtime, and `TickFixesMixinPlugin.shouldApplyMixin` only checks
whether the target mod is installed. Remove the patch from `spooky_tick_fixes.mixins.json` and rebuild, or remove the
jar.

**Revert:**
- **The whole mod:** delete the jar. Nothing is stored in worlds. The config file can stay.
- **One patch:** set its switch to false.
- **`[chunkSafeTicking]`:** `enabled = false` restores vanilla.
- **`[antlersWendigo]`:** `enabled = false` restores the hunting and the sound ranges, and turns off the keep-away and
  the vanishing. `despawnNormally = false` restores permanence.
- **`[sanityHorror]`** (1.5.0, not installed): each rule has its own switch per mod (`obsessedSanityGate`,
  `obsessedLookToProvoke`, `obsessedNoBaseSpawns`, `cantBreatheSanityGate`, `cantBreatheLookToProvoke`,
  `cantBreatheNoBaseSpawns`), or go back to the 1.4.0 jar.
- **`[activationRange]`:** `enabled = false`.

## Verification

Headless tests ran on a scratch server: the client instance's server-side jars plus this mod, the repo's `config/`,
`defaultconfigs/` and `kubejs/`, a fresh world, Java 17. No player can join it, so anything that needs a player was not
tested there. Full results are in the change-log entries listed under History.

- **Gates** (2026-11-06, headless): every patch applied, with no error from this mod. A dormant mimic summoned 12
  blocks up stayed at y 84.0 while an awake one fell (84 → 73). New hanging and staring watchers got follow range 32,
  saved ones with 2048 were clamped to 32 within the same tick, and `toww_hunting` stayed at 2048. Cow, pig, zombie,
  Born in Chaos and Creeping Woods mobs ticked for 20 s with the gates active. Not tested in game (recorded
  2026-11-06): The Obsessed sync, Whispering Spirits, Farm & Charm, Fragile Torches next to a torch.
- **Effect on the VM** (profile 2026-11-06 19:06–19:20, after 1.1.0): 20 TPS with a 46 ms median, down from
  6.6–7.2 TPS at about 140 ms, with the same population (about 550–600 entities, about 150 mimics).
- **Per-level work** (2026-11-06, headless): Antlers' day counter counted 10,170 times in about 224 ticks (45.4 per
  tick) without the patch, and 227 times with it. With `towwEmptyLevelSyncSkip`, an empty dimension's TOWW `WorldTime`
  still advanced and was saved.
- **Activation range** (2026-11-06, headless, adaptive off): `/tickfixes stats` reported 4–6 far mobs skipped per
  tick. A baby cow kept ageing at exactly 20/s while throttled (−23,900 → −23,700 in 10 s). A throttled pig fell far
  more slowly than an immune (ridden) one: dropped from 30 blocks, the ridden pig died on landing within ~2 s while the
  throttled one was still at y 98.7. Dormant mimics still slept.
- **Ray casts and chunk guards** (2026-11-06 and 2026-11-07, headless): each release booted with every patch applied
  and no injection errors. The guards need players to exercise them; `/tickfixes stats` reports their counters.
- **Chunk-safe ticking** (2026-11-07, headless): 33 patches applied, no mixin errors. `probe 0 0` (loaded): fast path,
  8 µs. `probe 400 8` (generated, unloaded): andesite, loaded from disk in 7–11 ms. `probe 300000 300000` (never
  generated): barrier, nothing generated; 4.3 ms the first time, 86 µs once cached. While `spooky_pregen` generated 29
  squares under the guard, no other read was answered with a barrier. Not measured (recorded 2026-11-07): the overhead
  on the VM under real load; compare a spark profile with the median of about 40 ms before.
- **The Wendigo** (2026-11-07, headless): all five patches log `-> applying (antlers)`, with no injection errors after a
  Wendigo was summoned (that's when its goal classes load). `[antlersWendigo]` was generated with its defaults.
  Summoned Wendigos show as `calm` in `/tickfixes wendigo`, and at daybreak they still turned into the daytime form.
  Three summoned Wendigos built their goals, keep-away included, without errors. Not tested: keeping away, the
  provocation, the calm-down, the retreat and vanishing, and despawning; all need a player.
- **Sanity-bound horror** (1.5.0, not installed; 2026-11-08): compiles (javac, 67 sources); 9 of 9 injection targets
  (owner, name, descriptor and call-site count) match the class files. Not boot-tested; not tested in game. A headless
  boot is required before deploying. To check in game: `/sanity <player>` and `/tickfixes horror` show each player's
  band, base status and Obsessed state. At Breakdown in the dark, a targeter should appear within a minute or so and
  lock on only once the player looks at it. Stalks should then stop when the player recovers, and never appear within
  48 blocks of a bed.

## History

- **1.5.0** (2026-11-08; built, not installed): `[sanityHorror]`: The Obsessed and CANTBREATHE come only at critical
  sanity, never spawn in a base, and are provoked by a look; `/tickfixes horror`. CONFIG_CHANGES "2026-11-08 —
  spooky_tick_fixes 1.5.0: The Obsessed and CANTBREATHE sanity-bound".
- **1.4.0** (2026-11-07): `[antlersWendigo]`: the Wendigo keeps away, attacks only when provoked, and despawns
  normally; `/tickfixes wendigo`. CONFIG_CHANGES "2026-11-07 — Antlers Wendigo: keeps away, attacks only when provoked
  (spooky_tick_fixes 1.4.0)".
- **1.3.4** (2026-11-07): `[chunkSafeTicking]` and `/tickfixes probe`; `offThreadChunkLog` hooks only the off-thread
  branch of `getChunk`; the Fragile Torches pre-check uses the non-waiting chunk lookup. CONFIG_CHANGES "2026-11-07 —
  Watchdog crash 03:08; max-tick-time; spooky_tick_fixes 1.3.4 chunk-safe ticking".
- **1.3.3** (2026-11-07): `villagerSleepLoadedOnly`, `obsessedReadsLoadedOnly`, `offThreadChunkLog` (at the head of
  `getChunk`); `obsessedChunkRadius` default 3 → 6; the plugin also logs the vanilla patches (31 applying lines).
  CONFIG_CHANGES "2026-11-07 — Watchdog crash 01:44 (villager bed); spooky_tick_fixes 1.3.3". The pack's config has
  set the radius to 6 since 2026-11-07: CONFIG_CHANGES "2026-11-07 — The Obsessed: chunk radius 3 → 6".
- **1.3.2** (2026-11-06): `obsessedWaitForChunks`, `obsessedChunkRadius` (default 3). CONFIG_CHANGES "2026-11-06 —
  spooky_tick_fixes 1.3.2: The Obsessed waits for chunks".
- **1.3.1** (2026-11-06): the ray-cast span check uses `getChunkNow` instead of `hasChunk`. CONFIG_CHANGES
  "2026-11-06 — Watchdog crashes 21:56 and 22:05; spooky_tick_fixes 1.3.1".
- **1.3.0** (2026-11-06): horror look-check ray casts go through `RayGuard` (`horrorRayCastLoadedOnly`,
  `horrorRayCastDedupe`). CONFIG_CHANGES "2026-11-06 — spooky_tick_fixes 1.3.0: horror look-check ray casts".
- **1.2.0** (2026-11-06): `antlersDayCounterOverworldOnly`, `towwEmptyLevelSyncSkip`; cheaper activation-range
  immunity check and Fragile Torches pre-check. CONFIG_CHANGES "2026-11-06 — spooky_tick_fixes 1.2.0".
- **1.1.0** (2026-11-06): `fullImmersionGate`, `alexsCavesMovingBlockGate`, `citadelTrackerCache`,
  `[activationRange]`. CONFIG_CHANGES "2026-11-06 — spooky_tick_fixes 1.1.0: second pass and activation range".
- **1.0.0** (2026-11-06): first pass: `mimicSleep`, `watcherFollowRange`, `obsessedSyncCoalesce`, `spiritLookGate`,
  `fragileTorchesPrecheck`, `farmCharmSyncOnChange`, `animationGates`, `adAstraExtraGate`. CONFIG_CHANGES
  "2026-11-06 — spooky_tick_fixes 1.0.0: first tick-fix pass".
