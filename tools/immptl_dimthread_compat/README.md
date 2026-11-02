# immptl_dimthread_compat

A one-mixin Forge 1.20.1 mod that lets **Immersive Portals 3.0.7** and **DimThreads (Dimensional
Threading Reforked) 1.2.1** run together.

**Problem.** Immersive Portals re-flushes its chunk-ticket throttle at the end of vanilla's
`DistanceManager.runAllUpdates`. Under DimThreads that code runs on the worker thread ticking the
level, so Immersive Portals' `MinecraftServer.isSameThread()` guard in
`ImmPtlChunkTickets.flushThrottling` fails, logs `Called in a non-server-main thread.` with a stack
trace on every call (≈1.3 GB of log in three minutes at a 64-chunk simulation distance) and returns
without flushing.

**Fix.** `@Redirect` that one `isSameThread()` call so it also accepts the DimThreads worker that
currently owns the level (`DimThread.owns(thread)` and the level's `IMutableMainThread` main thread
is the current thread). The flush only touches that level's own distance manager, from inside that
level's own update loop, so the owning worker is as safe a caller as the server thread. Everything
else is unchanged; off a DimThreads worker the original refusal still applies.

**Build.** `python tools/immptl_dimthread_compat/build.py --install` (needs the JDK 17 at
`E:\Program Files\Java\jdk-17` and the CurseForge install's libraries; no Gradle, no network).
Output: `build/immptl_dimthread_compat-1.0.0.jar`, copied to `mods/` and `local/`.

**Deploy.** Required on client *and* server (it is a normal common mod). Rebuild if DimThreads or
Immersive Portals change the symbols it uses; the mixin is `required: true`, so a mismatch fails
loudly at startup rather than silently losing the fix.

**Revert.** Delete the jar from `mods/`. Nothing is persisted in worlds.

**Verified** 2026-11-02 on a headless dedicated server with the pack's 281 server-safe mods: without the shim, 80,685
error lines in 90 s (one per tick per dimension); with it, 0, the mixin applied, and no other error or lag change.
Confirmed in-game the same day: portals load and render their far side with DimThreads active.

Rationale and evidence: `docs/CONFIG_CHANGES.md`, "Immersive Portals × DimThreads" entry.
