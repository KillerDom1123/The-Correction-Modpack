# immptl_dimthread_compat

A one-mixin Forge 1.20.1 mod that lets **Immersive Portals 3.0.7** and **DimThreads (Dimensional Threading
Reforked) 1.2.1** run together.

**Status: retired 2026-11-06.** DimThreads was disabled on the client instance and the VM, because 1.2.1 ticks every
dimension under one global lock and gives no parallelism. This jar is `.jar.disabled` on both sides with it: its
required mixin references DimThreads classes. The source stays here in case DimThreads returns.

## Why

Immersive Portals re-flushes its chunk-ticket throttle at the end of vanilla's `DistanceManager.runAllUpdates`. Under
DimThreads that code runs on the worker thread ticking the level, so Immersive Portals' `MinecraftServer.isSameThread()`
guard in `ImmPtlChunkTickets.flushThrottling` fails. It logs `Called in a non-server-main thread.` with a stack trace
on every call (about 1.3 GB of log in three minutes at a 64-chunk simulation distance) and returns without flushing.

## How it works

A `@Redirect` on that one `isSameThread()` call also accepts the DimThreads worker that owns the level: the thread
passes `DimThread.owns(thread)`, and the level's `IMutableMainThread` main thread is the current thread. The flush only
touches that level's own distance manager, from inside that level's own update loop, so the owning worker is as safe a
caller as the server thread. Everything else is unchanged; off a DimThreads worker, the original refusal still
applies.

## Build, deploy and revert

- **Build:** `python tools/immptl_dimthread_compat/build.py --install`. It needs JDK 17 at
  `E:\Program Files\Java\jdk-17` and the CurseForge install's libraries; no Gradle, no network. Output:
  `build/immptl_dimthread_compat-<version>.jar`, copied to `mods/` and `local/`.
- **Deploy:** required on the client and the server together with DimThreads (it is a normal common mod).
- **Rebuild** if DimThreads or Immersive Portals change the symbols it uses. The mixin is `required: true`, so a
  mismatch stops startup with a clear error rather than silently losing the fix.
- **Revert:** delete the jar from `mods/`. Nothing is saved in worlds.

## Verification

2026-11-02, on a headless dedicated server with the pack's 281 server-safe mods: without the shim, 80,685 error lines
in 90 s (one per tick per dimension); with it, 0. The mixin applied, with no other error and no change in lag. In game on 2026-11-02, portals loaded and rendered their far side with DimThreads active.

## History

- **Retired** (2026-11-06): disabled with DimThreads. CONFIG_CHANGES "2026-11-06 — DimThreads disabled".
- **1.0.0** (2026-11-02): first version. CONFIG_CHANGES "2026-11-02 — Immersive Portals × DimThreads: compat shim".
