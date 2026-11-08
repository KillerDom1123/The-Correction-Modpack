# spooky_tick_fixes

A small Forge 1.20.1 mod of behaviour-preserving server-tick fixes for the Spooky Dookie pack. Each patch skips work
that provably cannot change the outcome. Four parts change behaviour on purpose, each with its own switch:
`[activationRange]` (far mobs tick less often while the server is behind), `antlersDayCounterOverworldOnly` (a bug fix),
`[antlersWendigo]` (1.4.0: the Wendigo keeps away and only attacks when provoked) and `[sanityHorror]` (1.5.0: The
Obsessed and CANTBREATHE only come at critical sanity, never appear in a base, and must be looked at to be provoked).
Evidence, measurements and the full proposal
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

`/tickfixes stats` adds the counts: villager bed reads skipped, Obsessed reads stubbed, and off-thread requests with the top callers. The plugin now also logs the two vanilla patches (`-> applying (vanilla)`): **31** applying lines in total at the time
(38 in 1.4.0; see Design).

## 1.3.4 — chunk-safe ticking (`[chunkSafeTicking]`)

**The problem.** Every watchdog crash and multi-second stall of 2026-11-06/07 had the same cause, in a different piece of
code each time:
- the horror look ray casts;
- The Obsessed's spawn search;
- the vanilla villager bed check;
- Immersive Weathering's fluid generators;
- Create Connected's rotation propagation.

Each one, while a level ticked, read a block in a chunk that didn't exist yet. Vanilla generates such a chunk on the spot
and the server thread waits. On the VM, with players exploring, that wait is seconds to minutes; at 120 s the watchdog
stopped the server.

**The fix.** It covers all of them at once, and whatever else does the same:
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
| not generated, half generated, or being generated right now | **a barrier block, no fluid**, at once |

**Why a barrier, not air:** nothing spawns, places blocks or flows into terrain that doesn't exist, and ray casts stop
there.

**Untouched:** player packets, commands, worldgen and other threads, and main-thread chunk tasks (promotion to a full
chunk and its post-processing, via `ChunkTaskSuspendMixin`).

**So the only difference from vanilla:** ungenerated terrain is never generated just because something ticking looked
at it. That is generation the pre-generator (`spooky_pregen`) or a player's own chunk loading would do anyway, off the
server thread.

**Monitoring and escape hatches:**
- **First reads are logged** (`logCallers`): the first barrier read from each caller is logged with its stack (`chunk-safe ticking: … new caller`).
- **`/tickfixes stats`** counts barriers, disk loads and memory loads.
- **`/tickfixes probe <x> <z>`** performs one read at y 64 inside the next tick of your dimension and reports the path it took.
- **`exemptCallers`** (class-name prefixes) lets a mod that really must generate on the spot keep doing so. A prefix
  is matched against the first stack frame outside the JDK, Forge, Mixin, Canary and the level read itself (`Level`,
  `BlockGetter`, `LevelReader`, `LevelAccessor`, `ServerLevel.getBlockState`). Other vanilla classes aren't skipped,
  so that frame can be one such as `FlowingFluid` rather than the mod that started the work.
- **`enabled = false`** restores vanilla.

**Also in 1.3.4:**
- `offThreadChunkLog` now hooks only the off-thread branch of `getChunk` (Canary's `getChunkOffThread`, or vanilla's
  `supplyAsync`), so main-thread chunk lookups don't pay for it.
- The Fragile Torches pre-check uses the non-waiting chunk lookup.

## 1.4.0 — the Wendigo keeps away, and attacks only when provoked (`[antlersWendigo]`)

**Changes gameplay.** Antlers' Wendigo (night form, `antlers:wendigo`) hunted on its own. Two players were attacked
without warning on 2026-11-07, one in their base. It also never despawned.

| Mod behaviour (antlers-1.0.0) | With `enabled = true` |
|---|---|
| Tick procedure, at night: about every 25 ticks, target the **nearest** player within a 600-block box (±300), no line of sight needed, sprint, Speed III within ±150. A player who logs off hands the hunt to the next nearest | Removed. A Wendigo only targets the player who provoked it |
| Player target goal (`WendigoEntity$3`, night): any survival player it can see within its 60-block follow range | Never runs |
| Stalking roar: `playsound antlers:wendigo_calls_out hostile @p ~ ~ ~ 100 1` (the nearest player within ±355, heard up to 1,600 blocks), about once a minute | Removed. It roars only while provoked: every 15–30 s, from where it is, heard within `soundRange` |
| "I see you" (`SetstargetProcedure`): volume 40 on the **Master** channel, everyone within 640 blocks | Capped to `soundRange` (128 blocks) |
| `removeWhenFarAway` always false for the Wendigo and its daytime form | `despawnNormally = true`: despawn like any monster (instantly past 128 blocks from every player, randomly past 32) |

**The idea:** aggressive when provoked, but it keeps away from people.

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

The state is in memory only. Nothing is written to the world, and a Wendigo that unloads comes back calm.
`/tickfixes wendigo` lists the loaded Wendigos and their states: calm, keeping away, provoked by whom and how, or
retreating. The log gets one line per event: `wendigo at x y z provoked by <player>: <reason>`,
`... calmed down: ...; retreating` and `... vanished after calming down`.

**Revert:** `enabled = false` restores the hunting and the sound ranges, and turns off the keep-away and the vanishing.
`despawnNormally = false` restores permanence.

## 1.5.0 — sanity-bound horror: The Obsessed and CANTBREATHE (`[sanityHorror]`)

This changes behaviour, at the user's request (2026-11-08). Three rules, applied to both mods:
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

**The Obsessed** (see `docs/CONFIG_CHANGES.md`, The Obsessed spawn rules, for how the mod schedules things):

| What | The mod | Now |
|---|---|---|
| Targeter (first contact) appears | Per-player timer (3 min with our config), at a dark spot 40–80 blocks away, behind you | Same, but only for a critical player. While you're not critical the timer is held just short of zero, so it comes 10–60 s after a breakdown starts if it was due. Not inside a base |
| Targeter locks on (you become its target) | It has line of sight to the nearest player within ±100 blocks, who isn't sneaking, immune or repellant-protected | That player has to **look at it**: vanilla's Enderman stare test (view within acos(1 − 0.025/d) of its eyes, about 1.7° at 60 blocks) with a clear line of sight. They must also be critical. The mod's other conditions still apply |
| Stalk spawn (the target, every few minutes) | Day or night, anywhere, 37–70 blocks away while you face away | Only while the target is critical; otherwise it's retried every ~20 s (the mod's own 100-point delay). A spot inside a base is moved out of it along the line from the base's centre, then retried. Both checks run before the mod's spawn bookkeeping, so a blocked stalk doesn't advance its mood or timers |
| Behind-you / ceiling-spider / nightmare-spider hallucinations (can follow a stalk) | Behind the player | Not inside a base |
| Block-break surprise (1 in 100, then 53 min cooldown) | Any block you break | Only while critical and outside a base. A blocked one still uses up the cooldown |
| **Lurker** at your windows (within 14 blocks of your respawn point) | Needs an obsession | **Unchanged**, as the user asked |
| **Sleep-paralysis** hallucination in bed | Needs an obsession | **Unchanged**, as the user asked |
| Saviour, opportunistic and the other forms | Transformations of an Obsessed that is already there | Unchanged (the one already there needed rules 1 and 2) |

Its **intruder form** can still walk into a base after spawning outside it: it pathfinds to the target's respawn point,
opens doors, swaps torches and can take one item from a chest. "No spawn in a base" doesn't cover that.

**CANTBREATHE:**
- **Timer:** it still runs on any survival player's time in the Overworld. `isRelevantPlayer` is untouched, so the
  cadence (mostly exactly 1 h with our config) stays the cap.
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
and each online player's sanity, base status and Obsessed state, plus CANTBREATHE's victim and timers.

**Revert:** each rule has its own switch per mod (`obsessedSanityGate`, `obsessedLookToProvoke`,
`obsessedNoBaseSpawns`, `cantBreathe…`), or go back to the 1.4.0 jar.

## Activation range (1.1.0) — `[activationRange]`

This changes behaviour, for far mobs only. (The other deliberate behaviour changes are the 1.2.0 Antlers day-counter fix
and the 1.4.0 Wendigo rework.) Mobs more than `range` (48) blocks from every player in their dimension tick once every
`interval` (4) ticks, staggered by entity id. A skipped tick is skipped entirely, so the mob moves, thinks and runs its
own timers in slow motion. Two things still run on skipped ticks, so their rates are unchanged:
- `AgeableMob` age (growth and breeding cooldowns), switch `ageCatchUp`;
- the vanilla despawn check, switch `despawnChecks`.

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

Not yet done (see the audit doc): Alex's Caves' per-entity magnetism check, Slice & Dice wet air, The Obsessed's
`EntityTickProcedure` (§5.3.7), the watcher despawn rule (§5.5.2).

## Commands

Operators only (permission level 2). The server console works too, and acts in the overworld.

- **`/tickfixes stats`**: no arguments. Replies with one line; all counts run from server start:
  - **activation range:** enabled, active, the adaptive thresholds, range, interval, and far mobs skipped last tick;
  - **your dimension:** loaded Immersive Portals portals and Alex's Caves moving blocks;
  - the server's **average tick time**;
  - **horror ray casts:** guarded, duplicates reused, stopped at unloaded chunks, and Obsessed ticks deferred;
  - **chunk guards (1.3.3):** villager bed reads and Obsessed block reads answered without loading, and off-thread
    chunk requests with the top callers;
  - **chunk-safe ticking (1.3.4):** reads answered with a barrier and their top callers, disk loads, memory loads, and
    disk checks that timed out.
- **`/tickfixes wendigo`** (1.4.0): no arguments. One line for the patch (on/off, `despawnNormally`, provocations,
  calm-downs and vanishings since start), then one line per loaded Wendigo: dimension and position, distance from you
  (in your own dimension only), and its state: `calm`, `calm, keeping away from a player`, `provoked by <player>
  (<how>)`, or `calmed down, retreating`, with ` (moving away)` while it walks off. With none it says `no Wendigo
  loaded`. Only Wendigos that have ticked with `[antlersWendigo] enabled = true` are listed, so with the patch off the
  list is always empty.
- **`/tickfixes horror`** (1.5.0): no arguments.
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

## Design

- **Fail-soft per target mod.** `TickFixesMixinPlugin` applies a patch only when its target mod is installed and logs
  one line per patch and target class (`... -> applying (modid)`, or `(vanilla)` for the 4 vanilla patches;
  `CreepingWoodsRayMixin` has 3 targets). The 3 accessors and the activation-range hook aren't gated and log nothing.
  1.5.0 logs 44 lines (1.4.0: 38). Applied patches keep `defaultRequire: 1`, so a target that changed shape fails loudly at startup.
- **Switch per patch.** Each switch is read on the hot path, so a misbehaving patch can be turned off in the config
  without a rebuild. A config reload applies. In practice Forge's file watcher hasn't fired on the VM or the test
  server, so expect config edits to apply on restart.
  - **One exception:** the watchers' lower follow-range *default* is set when the game registers entity attributes,
    before the config is read. `watcherFollowRange = false` stops the clamp on saved watchers, but new watchers still
    get 32. Only removing the jar restores 2048.
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
