# Server tick audit — 2026-11-06

Scope: why the dedicated server (Server v2 on the Proxmox VM) runs at about 7 TPS with one player online, and every change that would reduce its tick time. The work combined spark profiles, a bytecode scan of all 281 server jars, reading the code of every hook that showed up as costly, and online research into known issues.

**Status: investigation only. Nothing in this document has been applied** except one in-game test command (§3.3). Changes that later get applied should be recorded in `CONFIG_CHANGES.md` as usual.

Related: `CONFIG_CHANGES.md` (sections "Performance — …", "Immersive Portals × DimThreads", "Better Chunk Loading smart post-processing off"), `UPDATE_REVIEW_2026-11-01.md`, `SERVER_MODS.md`.

---

## 1. Summary

**Symptom.** Steady-state tick time is 130–150 ms (TPS 6.6–7.5), with one player online and about 3,700 chunks and 550–620 entities loaded. The target is 50 ms (20 TPS).

**Cause.** DimThreads ticks each dimension on its own worker thread, and the server thread waits until every worker has finished. The overworld worker takes about 115–130 ms per tick, and roughly 84% of that is entity ticking. No single mod accounts for it. The cost comes from the number of living entities multiplied by a high per-entity cost on a slow core:
- every living entity runs about 45 mod event listeners each tick;
- several mods do needlessly expensive work for every mob;
- a few specific bugs (watchers that never despawn and search for paths up to 2048 blocks; dormant mimics running the full mob tick; Farm & Charm sending a packet per animal per tick).

After the overworld finishes, the server thread spends another 15–25 ms on its own work, mostly two horror mods' player-tick code.

**Expected effect of the proposals** (estimates; see §8 for how the numbers were derived):

| Step | Tick time | TPS | TPS gain |
|---|---|---|---|
| Now | ~137 ms | 7.2 | — |
| Tier 1 config only (§5.2) | ~135 ms | 7.4 | +3% |
| + Tier 2 compat shim, first pass (§5.3) | 92–111 ms | 9.0–10.9 | +25–50% |
| + Alex's Caves patch and the optional configs | 85–106 ms | 9.4–11.7 | +30–62% |
| + `simulation-distance` 10 → 8 | 66–92 ms | 10.8–15.2 | +50–110% |
| *or, instead of simulation 8:* + activation range (§5.5.1), keeping simulation 10 | 63–86 ms | 11.6–15.9 | +61–121% |
| Activation range alone, without the shim | 86–96 ms | 10.4–11.6 | +45–61% |

None of these alone reaches 20 TPS on this CPU. The likely last step is host tuning (§5.1.2, §5.5.4) or a faster CPU (§5.5.9).

**The five most valuable actions:**
1. Stop dormant Artifacts mimics from ticking (11–17% of the overworld tick).
2. Fix The One Who Watches permanently: cap the 2048-block follow range, and add the missing spawn overrides that let `toww_hanging` (weight 30, never despawns) pile up.
3. Coalesce The Obsessed's player-variable sync packets (5–11 ms per player per tick on the server thread).
4. Remove the orphaned Data Anchor library, trim Fragile Torches' gentle list, and gate the per-mob hooks of Full Immersion, Fragile Torches and the MCreator animation handlers.
5. Throttle far-away entities with an activation range (§5.5.1), or else lower `simulation-distance` to 8, and fix the JVM memory settings.

---

## 2. Environment (as profiled)

| Item | Value | Note |
|---|---|---|
| Host | Proxmox VM, Ubuntu 26.04 (PVE kernel 7.0.2) | — |
| CPU | Intel Xeon E5-2650 v2 @ 2.6 GHz (Ivy Bridge, 2013), 8 vCPU | Spark reports the real model name, so the VM's CPU type is already `host`. Single-core speed is about 40% of a current desktop CPU. |
| RAM | 32 GB, 8 GB swap | — |
| Disk | 12.6 GB, 6.7 GB used | Small for a world plus Simple Backups. |
| Java | Ubuntu OpenJDK 21.0.12 | The local `run.sh` points at a Windows JDK 17 path; the VM uses its own Java. |
| JVM args (VM) | `-Xms12G -Xmx32G`, no GC flags | `-Xmx` equals the VM's whole RAM (§5.1). The local `Servers/Server v2/user_jvm_args.txt` (6G/12G) is **not** what the VM runs. |
| GC | G1 defaults: young pauses average ~100 ms, one about every 25 s | — |
| `server.properties` (VM) | `view-distance=16`, `simulation-distance=10`, `sync-chunk-writes=true`, `max-tick-time=120000`, `entity-broadcast-range-percentage=100` | — |
| Mods | 281 server jars (`SERVER_MODS.md`) | — |
| DimThreads | v1.2.1, 4 threads, ~50 dimensions | Total tick = slowest dimension + server-thread work. |

---

## 3. How the investigation was done

### 3.1 Spark profiles

| Profile | Threads sampled | Length | Used for |
|---|---|---|---|
| https://spark.lucko.me/LnjBFaG1Y8 | Server thread only (spark's default background profiler) | 548 s, 4,332 ticks | Showed the server thread spending 79–90% of each tick waiting in `ThreadPool.awaitCompletion` (DimThreads), with the real work hidden on the workers. Gave per-minute TPS and the server thread's own costs. |
| https://spark.lucko.me/JvMaXZXDpo | none (empty) | 24 s | The thread filter was written in quotes; spark keeps the quote characters, so nothing matched (§3.2). |
| https://spark.lucko.me/ulUxjide6w | all threads | 13 s, 49 ticks | First view of `dimthread_server_overworld`. |
| https://spark.lucko.me/UFcKxbb5UP | all threads | 3.4 s, 15 ticks | After the §3.3 test. |

### 3.2 Profiling DimThreads correctly

DimThreads names its workers `dimthread_server_<dimension>`. Spark's default and background profiles sample only `Server thread`, so they show DimThreads "using" most of the tick while hiding what the overworld is actually doing. Use:

```
/spark profiler start --thread * --timeout 300
```

Do **not** put quotes around spark arguments. Spark joins space-separated values but keeps the quote characters, so `--thread "Server thread|…"` matches no thread and produces an empty profile. Let the profile run the full 5 minutes. Recording every thread with the Java sampler slows the server noticeably while it runs (3.8 TPS during profile ulUxjide6w), so read **percentages**, not absolute milliseconds.

### 3.3 In-game test (applied)

To test whether The One Who Watches' pathfinding was the 15% hotspot:

```
/execute as @e[type=the_one_who_watches:toww_hanging] run attribute @s minecraft:generic.follow_range base set 32
/execute as @e[type=the_one_who_watches:toww_staring] run attribute @s minecraft:generic.follow_range base set 32
```

Result (profile UFcKxbb5UP): melee path searches dropped from 15.1% to 0.6% of the overworld tick. TPS barely moved (6.8 → 7.2 over 1 minute, median 140 → 137 ms, 95th percentile 219 → 157 ms), because 19 more mimics had loaded in the meantime (115 → 134) and used up the saving (§4.2). The command only affected the watchers loaded at that moment. Watchers spawned later still have 2048 (§5.3.2).

### 3.4 Static audit of every jar

A purpose-written class-file scanner parsed every class in the 281 server jars, including nested jar-in-jar libraries, without running any of them. It recorded:
- every method that takes a tick-relevant event (Forge `LivingTickEvent`, `PlayerTickEvent`, `LevelTickEvent`, `ServerTickEvent`, `EntityJoinLevelEvent`, `AttachCapabilitiesEvent` and 25 others). This covers both `@SubscribeEvent` methods and `addListener` lambdas. Result: 719 handlers.
- every mixin, with its targets and injection points. Result: 2,913 mixin classes.
- Architectury tick/entity event registrations (18), and Quark/Zeta's own `ZLivingTick`/`ZPlayerTick`/`ZLevelTick` subscribers.

Each hook was then matched against the profiles' per-frame time. Mixin code appears in profiles as `handler$…`/`redirect$…`/`wrapOperation$…` frames inside vanilla classes; those were mapped back to their jars through the scanned handler names. Every hook costing more than about 0.3% had its bytecode read with `javap` to see what it does per call and whether a cheap early exit exists. Dependencies (`mods.toml` plus class references) were graphed to find libraries nothing uses.

### 3.5 Spawn and persistence audit

Every `forge:add_spawns` biome modifier in the horror and hostile mods was compared against the OpenLoader overrides. Every entity class whose `removeWhenFarAway` always returns `false` (mobs that never despawn by distance) was flagged, because naturally spawning mobs of that kind pile up.

### 3.6 Online research

Two research passes covered: server-side performance mods that exist for Forge 1.20.1, newer versions and known lag issues for the mods involved (Artifacts, Fragile Torches, Incendium, TOWW, The Obsessed, Whispering Spirits, Born in Chaos, DimThreads, MineColonies, Create, AE2, Mekanism, Ad Astra, Alex's Caves/Mobs, Quark, Immersive Weathering, Serene Seasons, Sophisticated, the magic mods, Immersive Portals, Lootr, CC:Tweaked and others), and JVM flags. Findings that were not confirmed locally are marked *(research, unverified)*.

### 3.7 Limits of the data

The overworld percentages come from about 16 seconds of worker samples, so treat each number as ±30%. Mimic and entity counts change as the player moves. Block-entity costs are tiny now (2%) because the world is new. They will grow with bases (§6).

---

## 4. Findings

### 4.1 Thread structure

Each tick runs in this order:
1. The server thread hands every dimension to a DimThreads worker and waits.
2. When all workers finish, the server thread processes player packets and player ticks, then server-tick events.

Tick time is therefore (slowest dimension) + (server thread's own work). Steady state is overworld ≈ 115 ms plus server thread ≈ 21 ms. All other dimensions run in parallel and finish well inside the overworld's time. Extra DimThreads threads don't help when everyone is in the overworld.

### 4.2 Overworld tick breakdown (profiles ulUxjide6w + UFcKxbb5UP)

| Part | Share of overworld tick | Notes |
|---|---|---|
| Entity ticking | 84% | 550–600 entities |
| — Artifacts mimics (115–134 loaded) | 11.5–16.7% | Dormant mimics run the full mob tick (§5.3.1). |
| — Melee path searches (TOWW watchers) | 15.1% → 0.6% after §3.3 | Follow range 2048 means up to 32,768 path nodes per search (§5.3.2). |
| — Forge `LivingTickEvent` dispatch (all mods) | 19.3% | About 5% of this is pure event-bus overhead (§4.3). |
| — Zombie villagers / villagers / creepers / raiders | 4.3 / 3.3 / 3.1 / 3.3% | 27 zombie villagers and a static group of 24 pillagers near spawn. |
| Chunk ticking | 6.8–8% | Spawning ~2%, Immersive Weathering 1.3%, Data Anchor 0.7%. |
| Scheduled functions (Incendium) | 3.7% | §5.2.6 |
| Block entities | ~2% | — |

### 4.3 Per-mob event listeners (`LivingTickEvent`)

There are 49 handlers in the pack, and every one runs for every living entity on every tick. Measured share of the overworld tick:

| Handler | Share | What it does per mob |
|---|---|---|
| Event-bus overhead (ASM handler dispatch, cast filters) | ~5% | Calling about 45 listeners × about 550 mobs |
| Fragile Torches `FragTorchEvent.onEntityUpdate` | 2.42% | Registry lookup plus a 13-entry string-list parse (`String.split`) **before** its 1% chance roll |
| Quark/Zeta `ForgeEventsRemapper` | 1.22% | MethodHandle dispatch to small Quark modules (Parrot Eggs, Pig Litters, Horses Swim, Dragon Scales, Vexes Die, Poison Potato, Pathfinder Maps) |
| Ad Astra Extra `EntityTickProcedure` | 1.00% | `getPersistentData()` (which allocates a tag on first use) and a "gassed" counter lookup, plus a Jupiter-only lightning branch |
| Mahou Tsukai `ServerHandler.livingEvent` | 0.92% | About 15 effect and capability checks |
| Mekanism `RadiationManager.onLivingTick` | 0.81% | Radiation; kept, because fission is a documented milestone |
| The Obsessed `EntityTickProcedure` | 0.81% | `registry key → toString() → startsWith("the_obsessed:")` for every mob, then a scared-mob counter and tamed-wolf checks |
| MCreator `EntityAnimationFactory` × 6 (Born in Chaos 0.75, The Obsessed 0.36, Creeping Woods 0.25, Foes 0.19, TOWW 0.08, plus Rake and Man From The Fog) | ~1.6% | `instanceof` / `String.equals` chains that only ever act on the mod's own mobs |
| Create Hypertube `ModServerEvents.onEntityTick` | 0.67% | Hypertube travel check |
| Artifacts (Kitty Slippers, Umbrella) | 0.67% | Curio-slot lookups for every mob |
| Alex's Mobs, Curios, Sophisticated Backpacks, Alex's Caves, Create Deep Dark, PneumaticCraft, Creeping Woods, The Obsessed climbing, Blood Magic, Antlers ×3, Create, Framework | 0.1–0.6% each | Long tail |

### 4.4 Mixin hooks on hot paths

| Hook (mod) | Share | What it does |
|---|---|---|
| Immersive Portals: Full Immersion — `Mob.serverAiStep` begin/finish, `checkDespawn`, fluid hiding, target restore | ~4.0% | Clears and reads a ThreadLocal around every mob's AI step and runs a portal-despawn check per mob. It runs even with all portal-AI features switched off in config. |
| Alex's Caves — `Entity.tick` (`ac_tick`), `Entity.collide` (`ac_collide`), `calculateEntityAnimation` | ~4% of its own work | `ac_tick`: tests every equipment slot of every entity against the magnetic tag, plus a POI/stream query for magnetic ones. `ac_collide`: an entity search for moving magnetic blocks on every movement. Known upstream: no config option (research). |
| Ars Nouveau — `RewindEntityMixin` (`baseTick`) | 1.17% | Records the position and health history of every entity, every tick, for the Rewind glyph |
| Data Anchor — `ServerLevel.tickChunk`, `Level.tickBlockEntities` | 1.06% | Iterates tracked data per chunk; **no mod uses this library** |
| Create Slice & Dice — `Entity.baseTick` | 1.00% | `getBlockState` at every entity's position, looking for "wet air" |
| Ad Astra — `LivingEntity.travel/tick` | 1.1% | Gravity and planet lookups |
| Farm & Charm — `Animal.tick` (`tickSaturation`) | 0.75% | Sends a `SyncSaturationPacket` **to every player in the dimension, every tick, for every cow, pig, sheep and chicken**, whether or not anything changed (~1,500 packets/s per player with ~75 animals) |
| Serene Seasons (via GlitchCore `LevelTickEvent`) | 1.11% | Melt/snow random-update pass over loaded chunks |
| Immersive Weathering `IW_weatheringTick` | 1.34% | Block-growth lookup on random ticks |
| Ars Nouveau perk `decreaseAirSupply`, Supplementaries rope climb, Citadel tick-rate guard | 0.4–0.5% each | — |
| Canary and Mobtimizations hooks | — | These are the optimizations themselves; keep them |

Capability lookups (`getCapability`) total 1.8% of the overworld tick, mostly Curios and Artifacts checking every mob's curio inventory.

### 4.5 Server thread (profile LnjBFaG1Y8)

| Item | Share of server thread | Notes |
|---|---|---|
| Waiting for DimThreads | 79–90% | The overworld's time |
| The Obsessed `PlayerTickProcedure` (+ `PlayerTick2`) | 4.2–7.8% (+0.5%) | Grew from 0.5 to 11.6 ms per tick within 9 minutes, for one player. 95% of it is `syncPlayerVariables`: **83 call sites** in `PlayerTickProcedure` and **19** in `PlayerTick2Procedure`. Each one serialises the full variable set to NBT, including the free-text fields `store_chats` 1–5, `store_speech`, `message_memory` and `imitation_structure_storage`, and sends it. |
| Whispering Spirits `PlayerLookAtSpiritProcedure` | 1.3–3.4% | Up to **100 ray steps per player per tick**, each doing 3 identical `Level.clip` raycasts plus an entity search |
| Chunk loading from disk (`ChunkSerializer.read`) | up to 12% while moving | Vanilla does this on the main thread |
| Immersive Portals entity sync and chunk tracking | 1.3% | — |
| KubeJS (all scripts) | 0.3–0.6% | Fine |
| TOWW `OnPlayerTickProcedure`, John Mod, Man From The Fog timers | 0.1–0.3% each | — |

### 4.6 Spawn and persistence gaps

`CONFIG_CHANGES.md` documents The One Who Watches, Rake and the other psychological-horror mobs as aligned to weight 2. These spawn entries were missed:

| Spawn | Weight | Biomes | Never despawns? | Overridden? |
|---|---|---|---|---|
| `the_one_who_watches:toww_hanging` | **30** | all | yes | no |
| `the_one_who_watches:toww_hallucination` | 5 | 42 listed | yes | no |
| `the_one_who_watches:toww_delusion` | 3 | all | — | no |
| `man:mftfhang` (Man From The Fog hanging variant) | **10** | all | — | no |
| `rake_the_arrival:ambient_sound` | 15 | 10 forest/taiga | — | no (Creeping Woods' equivalent was set to 2 on 2026-11-01) |

Most TOWW entities never despawn, and almost all have `FOLLOW_RANGE` 2048: hanging, staring, stalking, hunting, crawling, hallucination, delusion and looked-at (running and stare have 1000). The loaded watcher count rose 6 → 7 → 8 across the profiles, which is the pile-up in progress.

Born in Chaos (about 40 `forge:any` hostiles at weights 2–30) and Foes are left at mod weights on purpose (CONFIG_CHANGES, "The Altered"). They share the vanilla monster cap, so they change which hostiles spawn, not how many.

### 4.7 JVM and hardware

- `-Xmx32G` on a 32 GB VM lets the heap grow to the VM's entire RAM, which risks swapping or the OOM killer. Heap in use was 3.6–9.5 GB.
- G1 young-generation pauses average about 100 ms, one every ~25 s. That costs little on average but causes a visible stutter.
- The CPU is the hard limit. The JVM used only 25–30% of the 8 vCPUs, because the overworld is one thread.

---

## 5. Proposed changes

**Where edits go.** The usual flow:
1. Edit this instance (the repo).
2. Copy to `Servers/Server v2` (mods via `scripts/sync_server_mods.py`).
3. Copy the changed files to the VM (e.g. over SFTP).

`defaultconfigs/` only seeds new worlds; for the live world, edit `world/serverconfig/` on the VM. Mods and datapacks need a server restart. Anything in `mods/` must match on clients and server.

### 5.1 Tier 0 — VM and JVM

**5.1.1 JVM memory and GC flags.** In the VM's `user_jvm_args.txt` (or whatever start script the VM uses), replace `-Xms12G -Xmx32G` with Aikar's G1 set:
```
-Xms12G -Xmx12G -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1
```
- *Why:* a fixed 12 GB heap stays well below the VM's RAM and leaves room for the OS and off-heap memory. Aikar's flags are the standard G1 tuning for Minecraft. Current hosting guides recommend G1 up to about 12 GB; generational ZGC costs 10–15% more CPU, which comes straight out of tick time on slow cores (research).
- *Effect:* under 1% of average tick time, but it removes the ~100 ms hitches and the swap risk.

**5.1.2 Host CPU frequency (Proxmox host, not the VM).** Check that turbo is enabled in the BIOS. On the host, `cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor` should report `performance`. If it doesn't, set it with `cpupower frequency-set -g performance` (persist it via the distribution's usual method).
- *Why:* the E5-2650 v2 runs at 2.6 GHz base and up to 3.4 GHz with turbo. The tick is bound to a single thread, so clock speed translates directly into tick time.
- *Effect:* 0–25%, depending on the current state.

**5.1.3 Optional: `sync-chunk-writes=false`** in the VM's `server.properties`. With `true`, chunk saves write synchronously (fsync) on the IO workers. This doesn't change the steady tick, but it reduces save stalls on slow disks. Paper's default is `false`. The trade-off is slightly more risk of chunk loss if the host loses power.

### 5.2 Tier 1 — config and datapack changes (no code)

**5.2.1 Complete the horror spawn overrides** (`config/openloader/data/HorrorSpawnRarity/`). Add these files using the same-path override pattern as the existing ones:

`data/the_one_who_watches/forge/biome_modifier/toww_hanging_biome_modifier.json`
```json
{
  "type": "forge:add_spawns",
  "biomes": { "type": "forge:any" },
  "spawners": { "type": "the_one_who_watches:toww_hanging", "weight": 2, "minCount": 1, "maxCount": 1 }
}
```
`data/the_one_who_watches/forge/biome_modifier/toww_delusion_biome_modifier.json` — the same shape, with `"type": "the_one_who_watches:toww_delusion"`, weight 2.
`data/man/forge/biome_modifier/mftfhang_biome_modifier.json` — the same shape, with `"type": "man:mftfhang"`, weight 2.
`data/the_one_who_watches/forge/biome_modifier/toww_hallucination_biome_modifier.json` and `data/rake_the_arrival/forge/biome_modifier/ambient_sound_biome_modifier.json` — **copy each file from its mod jar** (both use explicit biome lists that must be kept) and change only `weight` (5 → 2, 15 → 2).

- *Why:* puts these spawns at the documented benchmark (weight 2, solitary). It stops `toww_hanging`, which spawns at weight 30 in every biome and never despawns, from piling up across the world. Each loaded watcher also carries the 2048-block pathfinding cost unless §5.3.2 is applied.
- *Effect:* prevents the tick from getting slower over time; it doesn't speed up today's tick. Watchers already in the world remain. Kill the loaded ones with `/kill @e[type=the_one_who_watches:toww_hanging]` if wanted (only loaded chunks are affected).
- Takes effect on world load. Revert by deleting the files.

**5.2.2 Remove Data Anchor** (`Data_Anchor-forge-1.20.1-1.0.0.24.jar`, by Corgi Taco). No jar on either side references its classes or declares it as a dependency; it's an orphan, possibly left behind by a removed mod.
- *How:* remove it in the CurseForge app (so the app doesn't restore it), run `scripts/sync_server_mods.py` (which prunes it from Server v2), and delete it from the VM's `mods/`. It must come off **both** sides.
- *Why:* its mixins on `ServerLevel.tickChunk` and `Level.tickBlockEntities` iterate tracked data for every ticking chunk and block-entity pass, for nothing.
- *Effect:* ~1% of the overworld tick.

**5.2.3 Fragile Torches gentle list** (`config/fragiletorches.toml`, `[fragiletorches.entities.gentle] list`). Delete `minecraft:boat`, `minecraft:ender_pearl`, `minecraft:egg`, `minecraft:item`, `minecraft:experience_orb` and `minecraft:minecart`.
- *Why:* the handler listens to `LivingTickEvent`, which only fires for living entities, so these six entries can never match. But the list is re-split string by string for every mob, every tick, before the chance roll. Removing them changes no behaviour.
- *Effect:* ~0.7–1%. Superseded by the shim patch §5.3.6 if that's built.

**5.2.4 Artifacts campsites** (`config/artifacts/common.json5`, `campsite`). Lower `count` (4 attempts per chunk) and/or `mimicChance` (0.3), for example to `count: 2` and `mimicChance: 0.15`.
- *Why:* 115–134 dormant mimics are loaded at any time, and they're the single biggest entity cost.
- *Effect:* affects newly generated chunks only, so the gain grows as the world is explored. It's a gameplay choice (fewer campsites and fewer mimic surprises). Not needed for performance if §5.3.1 is built.

**5.2.5 `simulation-distance` 10 → 8** (the VM's `server.properties`; `view-distance` can stay at 16).
- *Why:* each player's entity-ticking area falls from 441 to 289 chunks (−34%). Mimics, animals, villagers, minecarts and random ticks in the ring between 8 and 10 chunks stop ticking. Hostile mob numbers don't change, since they only spawn within 128 blocks of players anyway.
- *Effect:* an estimated 15–25% of the remaining overworld tick.
- *Gameplay:* crops and farms more than 128 blocks from every player pause.

**5.2.6 Optional, with gameplay trade-offs:**

| Change | File / key | Effect | Trade-off |
|---|---|---|---|
| Ars Nouveau Rewind off | `config/ars_nouveau/rewind.toml` `enabled = false` | 1.2%. Verified in code: `EffectRewind.shouldRecordData` returns false when the glyph is disabled. A middle option: add entity types to the `ars_nouveau:rewind_blacklist` tag via a datapack. | The Rewind glyph can no longer be used |
| Immersive Weathering moss | `config/immersive_weathering-common.toml` `[mossy_blocks] enabled = false` | Up to 1.3% now. Upstream issue #464 reports about 4% from moss neighbour-update chains on larger worlds (research). | No moss spreading |
| Alex's Mobs Underminer | `config/alexsmobs.toml` `underminerSpawnWeight` 50 → lower | Reported as laggy upstream (research, unverified here) | Fewer Underminers |
| Incendium per-tick clock | override `incendium:clocks/main` via an OpenLoader datapack | 3.7%. Its tag `incendium:mobs_no_player` lists all ~70 vanilla mobs, so it initialises and rescans every mob in every dimension, every tick. Its `@e` selectors have no position limit, so they also read other dimensions' entity lists from the overworld worker. Options: run the init/eid lines on a slower clock, or disable the functions (FTB did this in its own pack; research). | Delays or removes Incendium's scripted mob behaviour; needs a careful datapack |
| Quark small modules | `config/quark-common.toml`: Pig Litters, Parrot Eggs, Horses Swim, Dragon Scales, Vexes Die With Their Masters, Poison Potato Usage | Part of 1.2% | Small gameplay tweaks lost |

**Not recommended:** turning off Mekanism radiation (`radiationEnabled`, 0.8%), because fission is a documented milestone.

### 5.3 Tier 2 — compat shim `spooky_tick_fixes`

A small Forge mod with one job per mixin, built the same way as `tools/immptl_dimthread_compat`:
- **Project:** `tools/spooky_tick_fixes/` with `build.py --install`, compiled with JDK 17 (`E:\Program Files\Java\jdk-17`) against the jars in the CurseForge install. The jar goes in `mods/` and `local/`.
- **Install on both sides.** It changes no registries or network protocol, but keeping it on clients also speeds up singleplayer (The Correction instance) and keeps `mods/` identical. The sync script copies it to Server v2.
- **Fail-soft per target mod.** Use a mixin config plugin (`IMixinConfigPlugin.shouldApplyMixin`) that applies each mixin only if its target mod is loaded, and logs one line per applied patch. Removing a target mod later then doesn't crash the server. Each mixin is `required: true` *when applied*, so a target that changed shape fails loudly at startup (the existing shim's policy).
- **Rule for every patch:** skip work that provably cannot change the result. Never change what the mod does.
- **Verify every target with `javap`** against the shipped jar before building, as was done for the existing shims.

Measured costs are shares of the overworld tick (O) or the server thread (S).

#### 5.3.1 Dormant mimics skip their tick — O 11–17%
- **Target:** `artifacts.entity.MimicEntity#tick` (`m_8119_`), inject at HEAD, cancellable.
- **Change:** on the server side, if `isDormant` is true and no player is within 24 blocks (`level.hasNearbyAlivePlayer(x, y, z, 24)`), cancel the tick.
- **Why it's safe** (from bytecode): a dormant mimic only wakes through `setTarget` (its `NearestAttackableTargetGoal` targets players within its follow range of 16), through `hurt` (which runs outside the tick and calls `setTarget`), or by being in water (checked in its tick). `removeWhenFarAway` returns false, so it never despawns either way. With no player within 24 blocks none of these can happen, except a dormant mimic in water, which then wakes when a player approaches. The vanilla entity loop still increments `tickCount`.
- **What's skipped:** physics and collision (~22% of a mimic's tick), AI (~13%), about 20 mods' living-tick listeners (~35%), and the base tick (~21%).

#### 5.3.2 The One Who Watches follow range — O 15% when active
- **Target:** Forge `EntityAttributeModificationEvent` (mod bus) plus `EntityJoinLevelEvent`.
- **Change:** set the default `generic.follow_range` to 32 for the non-chasing variants: `toww_hanging`, `toww_staring`, `toww_crawling`, `toww_hallucination`, `toww_delusion` and `toww_looked_at`. On join, clamp any saved base value above 32 down to 32, because saved entities carry 2048 in their NBT.
- **Leave the chase variants** (`toww_hunting`, `toww_running`, `toww_stalking`) at their values, unless profiling shows them pathing constantly.
- **Why it's safe:** a vanilla path search may visit `follow_range × 16` nodes and builds a search region of ± follow range. At 2048, every melee goal re-check (once every 20 ticks per watcher) could visit 32,768 nodes. They keep watching the player, because their `LookAtPlayerGoal` uses its own 1000-block range. They stop pathing from across the map, which also matches the lore's Observers ("they do not behave like predators").
- **Why at type level:** the path-node limit is computed once, at construction. Changing the default fixes it for every newly created watcher; the in-game command only clamped the search distance on existing ones.

#### 5.3.3 The Obsessed: one sync per player per tick — S 5–11 ms per player
- **Target:** `net.theobsessed.network.TheObsessedModVariables$PlayerVariables#syncPlayerVariables(Entity)`, inject at HEAD, cancellable.
- **Change:** if the entity is a `ServerPlayer` and no flush is in progress, add the player to a dirty set and cancel. At `ServerTickEvent` END, call the original method once for each dirty player who is still online (with a re-entrancy flag set so it isn't intercepted again).
- **Why it's safe:** each call serialises the whole variable set as it is *at that moment*, and the client simply replaces its copy. Sending once at the end of the tick delivers the final state, which is what the client would have after the up-to-102 sends anyway.
- **Why it helps:** the cost is almost entirely NBT and UTF encoding of the large string fields, repeated per call. Its growth over a session suggests those strings get longer over time.

#### 5.3.4 Whispering Spirits: skip the look check when no spirit is near — S 1.3–3.4%
- **Target:** `net.yourlocalgamedev.whisperingspirits.procedures.PlayerLookAtSpiritProcedure#execute(Event, LevelAccessor, Entity)`, inject at HEAD, cancellable.
- **Change:** keep a per-level count of loaded `WhisperingSpiritEntity` instances (incremented and decremented on `EntityJoinLevelEvent`/`EntityLeaveLevelEvent`). If the player's level has none within the ray's maximum reach (100 steps, so a 101-block radius), cancel.
- **Why it's safe:** the procedure only acts when one of its ray steps finds a spirit in a 1×1×1 box along the player's view, and that's impossible with no spirit in range. Spirits are weight 2, so the check skips nearly every tick.
- **Optional:** also cache the three identical `clip` results within each ray step.

#### 5.3.5 Full Immersion: skip per-mob portal hooks in dimensions with no portal — O ~4%
- **Targets:** `com.astrologic.fullimmersion.PortalAiContext#begin` and `#finishAi`, and `PortalDespawnCompatibility#shouldSuppressDespawn`, at HEAD.
- **Change:** return early (with the neutral result) when the mob's level has no Immersive Portals portal entity. Use the mod's own `PortalDespawnCompatibility.sourcePortals(level)` index or an equivalent per-level count.
- **Why it's safe:** with no portal in the dimension, no projection, pursuit or despawn exception can apply.
- **Verify before building:** that the index covers every portal type the pack creates (nether portals converted by Immersive Portals, Ars Two-Way Portals, global portals), and that `begin()` leaves no state `finishAi()` needs.
- **Note:** the config switches (`mobPortalPursuit` etc.) don't avoid this cost; `begin()` still clears the ThreadLocal when all features are off.

#### 5.3.6 Fragile Torches: check for a torch first — O ~2.3% (supersedes §5.2.3)
- **Target:** `com.lothrazar.fragiletorches.FragTorchEvent#onEntityUpdate`, inject at HEAD, cancellable.
- **Change:** if neither the block at the entity's position nor (for entities at least 1 block tall) the block above is in the `fragiletorches:fragile` tag, cancel.
- **Why it's safe:** the handler can only break a tagged block at exactly those two positions; every other path returns without doing anything. Two `getBlockState` calls replace the list parse and the random roll for almost every mob.

#### 5.3.7 Per-mob MCreator handlers — O ~2.5–3%
- **`EntityAnimationFactory.onEntityTick`** for Born in Chaos, The Obsessed, Creeping Woods, Foes, TOWW, Rake and Man From The Fog: at HEAD, return unless the entity's type belongs to that mod. Cache the answer per `EntityType` in an identity map.
  - *Why it's safe:* these handlers only sync animation state for their own entity classes.
  - *Verify:* per jar, that each handler touches nothing else.
- **Ad Astra Extra `EntityTickProcedure.onEntityTick`:** return early when the entity's Forge persistent-data tag is absent or lacks `adastraextraGassed`, **and** the level isn't `adastraextra:jupiter`.
  - *Why it's safe:* those are the procedure's only two branches (gas damage and countdown; random lightning on Jupiter).
  - *How:* read the `persistentData` field through an accessor, so the check doesn't allocate tags on mobs.
- **The Obsessed `EntityTickProcedure`:** replace only its `getKey(type).toString().startsWith("the_obsessed:")` test with a cached per-`EntityType` flag (`@WrapOperation`/`@Redirect` on that call chain).
  - *Why not a full gate:* the procedure also runs a scared-mob counter (`limit_per_mob_scare`) and tamed-wolf logic for other mods' mobs, so those must keep running.

#### 5.3.8 Farm & Charm: send saturation only on change — O 0.75%, plus network
- **Target:** `net.satisfy.farm_and_charm.core.network.PacketHandler#sendSaturationSync(SyncSaturationPacket, Entity)`, inject at HEAD, cancellable.
- **Change:** remember the last `(level, foodCounter)` sent per entity (weak map). Cancel when unchanged, but resend every 100 ticks, and when a player starts tracking the animal (`PlayerEvent.StartTracking`), so clients that come into range get the current value.
- **Why it helps:** today it sends one packet per animal per tick to every player in the dimension, near or far.

#### 5.3.9 Smaller patches — O ~1.4% combined
- **Create Slice & Dice** (`EntityMixin.baseTick`, wet-air check): gate on whether the level has any loaded sprinkler, tracked through the sprinkler block entity's load and unload.
- **Citadel** (`ServerProxy.canEntityTickServer` → `CitadelServerData.get`): cache the tick-rate tracker for the current tick instead of doing a saved-data lookup per entity.

Both need their code read in full before building.

#### 5.3.10 Second pass: Alex's Caves magnetism — O ~2–3%
- **Targets:** the `ac_tick` path (`MagnetUtil.isPulledByMagnets` → `isDynamicallyMagnetic`, which tests every equipment slot against the magnetic tag) and the `ac_collide` path (an entity search for moving metal blocks on every movement).
- **Gate:** per-level knowledge of whether any magnet POI or moving-metal entity exists.
- **Why second:** it's the most involved patch. Upstream confirms there's no config option, and an open issue (#1724) reports large spikes near Magnetic Caves (research).

### 5.4 Tier 3 — structural options (test on a copy first)

**ServerCore (Forge) 1.5.2+1.20.1** *(research)*
- **What:** Paper-style entity activation range (distant, idle entities tick only occasionally), dynamic view/simulation distance and mob caps by MSPT, villager lobotomisation. Its behaviour-changing features are off by default. This is the biggest remaining lever, because it shrinks the entity count that dominates the profile.
- **Caveats:**
  - It's the last Forge build; the author has dropped Forge.
  - MineColonies' wiki lists ServerCore as a conflicting mod, because it culls citizen ticks. If used, exclude `minecolonies:citizen` and the other MineColonies entities by exact ID (no wildcards; ServerCore issue #101).
  - Also exclude the horror suite's scripted entities, bosses and anything that must act far from players.
  - Its compatibility with DimThreads and Immersive Portals is unknown.
- **Alternatives:** Tick Tweaks, NoTick. Use only one.

**Dynamic View (someaddon) 4.0** *(research)*
- **What:** adjusts simulation and view distance to hold a target MSPT. It's lower-risk than ServerCore (it only changes distances) and comes from the author of Chunk Sending, Smooth Chunk, Better Chunk Loading and others already in the pack. It needs Cupboard, which is already installed. DimThreads 1.2.1 fixed an earlier Cupboard incompatibility.
- **Caveat:** don't combine it with ServerCore's dynamic distance.

**Hardware.** A current desktop-class CPU is about 2.5× faster per core than the E5-2650 v2. That alone would put today's tick near the 50 ms budget.

### 5.5 Additional options (added after the audit)

#### 5.5.1 Activation range: tick far-away entities less often — O 35–44% alone

**The measurement.** Spark's world stats record entity counts per chunk, including the chunk the player stands in. In profiles ulUxjide6w and UFcKxbb5UP, about 310 entities were in entity-ticking chunks (within 10 chunks of the player, plus the radius-3 spawn area):

| Distance from the player | Profile ulUxjide6w | Profile UFcKxbb5UP |
|---|---|---|
| ≤ 48 blocks | 58 (19%) | 50 (16%) |
| 48–96 blocks | 108 (35%) | 111 (35%) |
| 96–128 blocks | 60 (20%) | 72 (23%) |
| > 128 blocks (corners of the 21×21-chunk simulation square) | 80 (26%) | 85 (27%) |

Another 230–270 entities, including 63–84 mimics, sit in chunks that are loaded (view distance 16) but don't tick at all.

So more than 80% of the entities that tick are more than 48 blocks from the player. Nobody can see them in detail, yet each costs a full tick.

**The change.** Mobs more than 48 blocks from every player in their dimension run their tick only one tick in four. Entities are staggered by ID (`(gameTime + id) % 4 == 0`), so each tick handles about a quarter of the far mobs instead of all of them. A skipped tick is skipped entirely, so the mob lives in slow motion, including its own timers. Two exceptions:
- `AgeableMob` age gets the skipped ticks added back, so growth and breeding cooldowns keep normal pace;
- the immunity rules below always override the throttle.

**Never throttled:**
- **Non-mobs:** items, projectiles, minecarts, boats, falling blocks, markers, item frames, armor stands, and Create and Immersive Portals entities. These keep vanilla behaviour.
- **Mobs in an active state:** anything with a target, hurt in the last 5 s, burning, leashed, riding or being ridden, or taking part in a raid.
- **Bosses.**
- **Anything in the datapack tag `#spooky_tick_fixes:always_tick`.** Seed it with:
  - MineColonies citizens and visitors (MineColonies officially lists activation-range mods as conflicting, because citizens work away from players);
  - Guard Villagers guards;
  - the horror suite's scripted mobs (TOWW chase variants, Rake, Man From The Fog, Cave Dweller, Weeping Angels, Pale Hound, CANTBREATHE, The Mimicer);
  - Mahou Tsukai familiars.

  Editing the tag needs no rebuild.
- **Optional adaptive mode:** throttle only while the last-minute average tick is above 50 ms, so the world runs at full fidelity whenever the server keeps up.

**Gameplay effect:**
- Mobs, animals and villagers more than 48 blocks away move, path and think at quarter rate. They're too far to be seen in detail.
- Animal growth and breeding stay normal (the age catch-up).
- Far villagers work and breed more slowly. Add `minecraft:villager` to the tag if that matters.
- Far hostiles despawn more slowly, since their despawn check runs less often. The mob cap then stays fuller, so fewer new spawns happen. That's neutral to slightly fewer mobs.
- Known visual edge: mobs seen through an Immersive Portals portal more than 48 blocks away can look slow-motion. If that shows, treat entities within range of a portal near a player as near.

**Estimated gain:**
- Entities are 84% of the overworld tick, and about 55–70% of that cost belongs to far, non-immune mobs (the remainder is near mobs, immune mobs and non-mobs).
- Skipping three ticks in four saves about 35–44% of the overworld tick. On its own that's 86–96 ms per tick, +45–61% TPS.
- It overlaps with §5.3.1, since most mimics are far. Combined with the Tier 2 shim, the estimate is 63–86 ms (§1).
- It replaces the need for §5.2.5: far things keep running slowly instead of freezing beyond 128 blocks.

**How to get it, either way:**
- **Built into `spooky_tick_fixes` (recommended).** One cancellable inject at HEAD of `ServerLevel.tickNonPassenger` (`m_8647_`), plus the immunity checks, the age catch-up, the tag and a config file holding range, interval and adaptive mode. That's about 150 lines.
  - *Why recommended:* only the listed behaviour changes, the exclusion list is under the pack's control, and it's tested against this pack's mods.
  - *Add a `/tickfixes stats` command* showing how many entities were throttled last tick, to confirm it's working.
- **ServerCore (§5.4).** Its activation range is ported from Paper and configurable per entity type. It's the last Forge build, untested with DimThreads and Immersive Portals, and needs the same MineColonies exclusions.

**Why not two threads (near and far)?** It was considered. The split is lopsided (about 18% near, 82% far), so the tick would still wait for the far thread processing 82% of the work: a ceiling of about 20% gain. It also carries every risk in §5.5.10. Skipping three of four far ticks removes more work and needs no threading.

#### 5.5.2 Let non-despawning horror mobs despawn when far away

Most TOWW entities, and several other MCreator horror mobs, return `false` from `removeWhenFarAway`, so once spawned they stay forever and pile up (§4.6). A shim rule can make the stationary watchers (`toww_hanging`, `toww_staring`, `toww_hallucination`, `toww_looked_at`, `toww_crawling`) despawn like vanilla monsters: instantly when more than 128 blocks from every player, randomly between 32 and 128.
- *Why:* together with §5.2.1 this keeps their number bounded. Each watcher costs a mob tick, and before the follow-range fix (§5.3.2) also a 32,768-node path search every 20 ticks.
- *Lore fit:* the Observers "appear" and "watch". Something that is gone when you come back fits them better than a permanent ornament.

#### 5.5.3 One-off cleanup of what has already piled up

These are gameplay decisions; check what's there before deleting anything.
- **The raider group** that sat near spawn for 30+ minutes in every profile (24 pillagers plus vindicators and witches, chunks 6–7 / 3–5, blocks x 96–127, z 48–95):
  `/kill @e[type=#minecraft:raiders,x=96,y=-64,z=48,dx=32,dy=384,dz=48]`
- **Loaded watchers:** `/kill @e[type=the_one_who_watches:toww_hanging]` (only loaded chunks are affected).
- **27 zombie villagers around the village.** Probably converted villagers, which never despawn. Cure them, or kill them, as preferred.

#### 5.5.4 Proxmox host tuning (adds to §5.1.2)

The E5-2650 v2 normally sits in dual-socket boards. Settings that each tend to be worth a few percent:
- **Keep the VM on one socket.** Check the layout on the host with `lscpu -e` (the SOCKET, NODE and CORE columns) or `numactl --hardware`. Enable NUMA for the VM (`qm set <vmid> --numa 1`). Pin its vCPUs to cores on one socket (`qm set <vmid> --affinity <core list>`, Proxmox VE 7.3+). That way memory reads don't cross sockets.
- **Prefer physical cores over hyperthread pairs** for the vCPU list: two hyperthreads of one core share it, and the tick thread is the one that suffers.
- **Power settings:** BIOS power profile on "Maximum Performance", deep C-states off, and turbo on. With the `intel_pstate` driver, `cat /sys/devices/system/cpu/intel_pstate/no_turbo` should print `0`. Then the `performance` governor (§5.1.2).
- **Check for contention:** run `vmstat 1` inside the VM and watch the `st` (steal) column while the server runs. If it isn't near 0, other VMs are taking this VM's CPU time.

Together with §5.1.2 this could be 10–25% if turbo or power saving is currently holding the CPU back, and close to nothing if it's already tuned.

#### 5.5.5 JVM extras (Linux)

- **`-XX:+UseTransparentHugePages`** added to the §5.1.1 line lets the JVM back its heap with 2 MB pages, which cuts memory-translation overhead. It's a few percent at low risk. Requires `/sys/kernel/mm/transparent_hugepage/enabled` in the VM to read `[madvise]` or `[always]`.
- **Oracle GraalVM for JDK 21** in place of the Ubuntu OpenJDK. Same flags; its JIT compiler is often reported 5–15% faster on modded servers *(community benchmarks, unverified here)*. It's just a different Java install, so it's easy to try and revert. Test on a copy first, like any JVM change.

#### 5.5.6 Pregenerate the world with Chunky

While exploring, chunk loading took up to 12% of the server thread (§4.5), and new terrain also costs generation work.
- **What to run:** `/chunky center 0 0`, `/chunky radius <n>`, `/chunky start`, at a time nobody is playing.
- **What it fixes:** the generation hitches while exploring, not the steady tick.
- **Already in place:** Better Chunk Loading's smart post-processing is already off, which prevents the Chunky deadlock (CONFIG_CHANGES 2026-11-03).
- **Disk space is the limit.** The VM has about 6 GB free, and modded region files grow fast. Start with a small radius (e.g. 1000 blocks, about 15,600 chunks) and check usage before going further.

#### 5.5.7 Lower mob caps

Fewer hostiles near players means less AI and path searching. Vanilla has no setting for this; it needs ServerCore's dynamic mob caps (§5.4) or a spawn-control mod such as In Control!. It's a gameplay trade-off: the pack wants "a world that fights back".

#### 5.5.8 Remove heavy mods (last resort)

| Mod | Measured cost | What would be lost |
|---|---|---|
| Immersive Portals: Full Immersion | ~4% (until §5.3.5) | Mobs following, fighting and seeing through portals, plus its portal fluid and despawn compatibility |
| Incendium datapack functions | 3.7% | Incendium's scripted mobs, bosses and items (biomes and structures stay with the Incendium Biomes Only add-on) |
| Alex's Caves magnetism | ~4% (until §5.3.10) | Part of Alex's Caves' core content; removal is not realistic |
| The Obsessed | server thread 4–8% (until §5.3.3) | A whole horror mod |

#### 5.5.9 Faster CPU

A current desktop-class CPU is about 2.5× faster per core than the E5-2650 v2. Moving only this VM onto such a machine would put today's tick (about 137 ms) near the 50 ms budget before any other change. Combined with the shim, it would leave headroom for more players and bigger bases.

#### 5.5.10 Considered and rejected

- **Parallel entity ticking within a dimension.** The Async mod's Forge 1.20.1 ports (HariMultiThread, HariEntity, kingniu4/Async-1.20.1) and MCMT (original for 1.15/1.16; newer forks) split a dimension's entity list across a thread pool. Rejected:
  - entity ticks aren't independent (pushing, damage, block changes, spawning and removing entities, the world's shared random number generator);
  - several mods here assume one thread per dimension (Full Immersion records a single "projection thread"; The Obsessed sends packets from mob ticks; Canary keeps single-threaded collision and entity caches; Alex's Caves, Citadel, Farm & Charm and Incendium keep shared tables);
  - DimThreads has already hit this class of bug three times in this pack (Immersive Portals' thread check, Chunk Sending 4.1's void world, the Better Chunk Loading deadlock);
  - the mods call themselves experimental alpha and expect a `synchronizedEntities` list, which here would be long and would shrink the gain.
  
  If ever tried: last, on a copy, with backups.
- **Splitting by chunk or region.** Entities affect things well beyond 16 blocks within one tick (targeting 16–64, path searches up to the follow range, arrows, teleports up to 150 blocks, `@e` commands across the whole dimension). Block updates also cross chunk borders. Checkerboard scheduling only fixes contact interactions. Folia (Paper) does region threading with a buffer between regions, but:
  - it gains only with players far apart (one player means one region);
  - it rewrote all global systems;
  - it runs no Forge mods.
  
  This is the same reason multi-node sharding was ruled out.
- **Two threads, near and far.** See §5.5.1: about a 20% ceiling, plus the risks above.
- **Calculating the next tick in advance.** Tick N+1 starts from exactly what tick N produces, plus the player input that arrives during tick N. Guesses would almost always be wrong, and there's no idle time between ticks while the server is behind. The safe forms of "precompute" are already covered: pregeneration (§5.5.6), predictive chunk loading (Better Chunk Loading, installed), path searching on worker threads (MineColonies, Alex's Mobs, Citadel), and skipping work whose answer can't have changed (§5.3).

---

## 6. Watch list — costs that will grow with bases

| Area | What to watch | Lever *(research unless stated)* |
|---|---|---|
| MineColonies | `colonyloadstrictness = 1` with `loadtime = 40` keeps every claimed chunk ticking for 40 minutes after the owner leaves (stated in CONFIG_CHANGES 2026-11-04) | Raise strictness, lower loadtime |
| Mekanism | Logistical transporters with item backlogs re-path every tick (#8602, open) | Avoid long backed-up transporter lines |
| AE2 | Storage buses polling external inventories | Fewer or narrower storage buses |
| Create | Filtered brass funnels/tunnels pulling from large vaults (#5448); contraption assembly spikes | `maxBlocksMoved` in `create-server.toml` |
| Ad Astra | Oxygen distributors expanding to their maximum volume | `maxDistributionBlocks`, `distributionRefreshRate` |
| Lootr / maps | The first opening of a chest whose loot includes an explorer map runs the structure search on the main thread (vanilla bug MC-249136) | Remove maps from loot if long stalls appear |
| Iron's Spells | Priest AI (`NeutralWizard.aiStep`) stalls, open on exactly 3.16.3 (#1269) | Kill offending priests |
| Immersive Portals | Each active portal loads up to 169 far-side chunks (`portalChunkLoadingRadiusCap = 6`). A reported hang involves cross-dimension load/save while another thread touches the world (relevant to DimThreads) | Watch TPS with several portals in range |
| Hexerei | Mixing cauldrons are expensive per block | Don't chunk-load banks of them |

## 7. Checked and ruled out

- **KubeJS scripts** (`anti_lag_*`, `oc_*`, `oc_sanity` `checkSpawn`): 0.3–0.6% of the server thread and none measurable on the overworld.
- **Observable** (`observable$track` wraps every entity tick): no measurable self-time.
- **Guard Villagers:** patrols are already off ("Have guards patrol the village regularly?" = false).
- **Orphan check:** besides Data Anchor, the jars nothing depends on are client libraries (Iceberg, Prism, Searchables), real feature mods, or infrastructure (Kotlin for Forge, the mixinextras fix, FerriteCore, CreativeCore). CreativeCore only serves the client-only AmbientSounds, but removing it from the server alone risks a network-channel mismatch, for no measurable gain.
- **Async Locator:** conflicts with the already-installed Structure Essentials *(research)*.
- **Raid near spawn:** 24 pillagers, plus vindicators and witches, sat static in chunks (6–7, 3–5) across all profiles, but cost only ~3%.

## 8. How the estimates were made, and how to verify

**Baseline:** about 137 ms per tick, made up of the overworld ≈ 116 ms and the server thread ≈ 21 ms. These come from profile UFcKxbb5UP's per-thread time scaled to its real 1-minute median.

**Each patch's saving** is its measured share of the overworld or server thread, multiplied by an expected removal fraction:
- mimic patch: 80–95%;
- Full Immersion: 50–100%, depending on portals;
- MCreator gates: 60–80% (the event-bus dispatch remains);
- Fragile Torches: about 90%;
- Obsessed and Whispering Spirits: about 90–95%.

Savings were summed as if independent, which overstates slightly where they overlap. The simulation-distance estimate assumes non-hostile entities are spread evenly.

**Activation range (§5.5.1):**
- Take the per-chunk entity counts in profiles ulUxjide6w and UFcKxbb5UP and count the entities in entity-ticking chunks: within 10 chunks of the player's chunk (Chebyshev distance), plus the radius-3 spawn area.
- Split them by straight-line distance from the player's chunk.
- 81–84% are more than 48 blocks away. Assume 55–70% of the entity-tick cost belongs to far mobs that would not be immune.
- Skipping three ticks in four then saves 75% of that: 35–44% of the overworld tick.
- In combination with the shim, the overlap (mainly mimics) was removed before adding the two together.

**To verify each step:**
1. Before and after each change, let the server settle with the player in the same place. Note `/spark tps`, and the 1-minute median and 95th percentile from `/spark health`.
2. Take a worker profile with `/spark profiler start --thread * --timeout 300` and compare the shares in §4.
3. For the shim, the log line each patch prints on load confirms it applied. Spark should show the targeted frames shrinking: `MimicEntity.m_8119_`, `PortalAiContext`, `FragTorchEvent`, `syncPlayerVariables`, `PlayerLookAtSpiritProcedure`, `sendSaturationSync`.

## 9. Sources

Profiles: https://spark.lucko.me/LnjBFaG1Y8 · https://spark.lucko.me/ulUxjide6w · https://spark.lucko.me/UFcKxbb5UP

Research references (not re-verified locally):
- DimThreads README (total MSPT = slowest dimension): https://github.com/SrRapero720/DimensionalThreading-Reforked
- ServerCore Forge: https://modrinth.com/mod/servercore/version/rx1c7m6q · MineColonies exclusion issue: https://github.com/Wesley1808/ServerCore/issues/101
- MineColonies config and conflicting mods: https://minecolonies.com/wiki/misc/configfile/
- Dynamic View: https://www.curseforge.com/minecraft/mc-mods/dynamic-view/files/5345889
- Tick Tweaks: https://modrinth.com/mod/tick-tweaks
- Aikar's flags: https://docs.papermc.io/paper/aikars-flags
- Artifacts campsite config: https://raw.githubusercontent.com/ochotonida/artifacts/1.20.1/common/src/main/java/artifacts/config/ModConfig.java
- Fragile Torches source: https://raw.githubusercontent.com/Lothrazar/FragileTorches/trunk/1.20.1/src/main/java/com/lothrazar/fragiletorches/FragTorchEvent.java
- Incendium functions: https://support.modrinth.com/en/articles/14030919-datapacks-on-modrinth-hosting · https://github.com/FTBTeam/FTB-Modpack-Issues/issues/13670 · https://github.com/Stardust-Labs-MC/Incendium/issues/159
- Immersive Weathering moss: https://github.com/Silversmith-Mods/Immersive-Weathering/issues/464
- Alex's Caves magnet spikes: https://github.com/AlexModGuy/AlexsCaves/issues/1724
- Iron's Spells priest stall: https://github.com/iron431/irons-spells-n-spellbooks/issues/1269
- Mekanism transporters: https://github.com/mekanism/Mekanism/issues/8602
- Lootr saves: https://github.com/LootrMinecraft/Lootr/issues/804
- Immersive Portals config: https://qouteall.fun/immptl/wiki/Config-Options.html
- Parallel entity ticking:
  - Async: https://modrinth.com/mod/async
  - Forge 1.20.1 ports: https://www.curseforge.com/minecraft/mc-mods/harimt · https://www.curseforge.com/minecraft/mc-mods/harientity · https://github.com/kingniu4/Async-1.20.1
  - MCMT: https://github.com/jediminer543/JMT-MCMT · https://github.com/ben001109/JMT-MCMT
- Region threading (Folia): https://github.com/PaperMC/Folia

Local evidence: `javap` of the shipped jars in `Servers/Server v2/mods/` (classes and methods named in §4–§5), and the configs under `Servers/Server v2/config/`.
