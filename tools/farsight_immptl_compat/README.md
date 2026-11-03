# farsight_immptl_compat

A small client-only Forge 1.20.1 mod that lets **Farsight 1.20.1-5.1** keep chunks the server has
unloaded while **Immersive Portals 3.0.7** is installed.

**Problem.** Farsight has two halves. One tells the client the server allows `maxRenderDistance`
(64) chunks, so the render distance is not capped at the server's `view-distance`. The other is its
own client chunk manager, which holds back the server's `ClientboundForgetLevelChunkPacket` until the
chunk is beyond the client's render distance plus 10. Immersive Portals replaces the client chunk
cache with `ImmPtlClientChunkMap` (both mods inject at `ClientLevel.<init>` RETURN; Immersive Portals
does not check what is there, and its renderer depends on its own map), so Farsight logs
`Farsight not enabled for: ... as other mods changed the chunk source to: class
qouteall.imm_ptl.core.chunk_loading.ImmPtlClientChunkMap` and the server's unloads apply at once. On
a server you then see only as far as its `view-distance`, whatever the client slider says.

`ImmPtlClientChunkMap` already stores chunks at any distance, and nothing else in Immersive Portals
drops client chunks, so only the hold-and-release half is missing.

**Fix.** Two client mixins and one helper class (`HeldUnloads`):
- `ClientPacketListener.handleForgetLevelChunk` (`m_5729_`) HEAD: in the dimension the player stands
  in, when the chunk map is Immersive Portals' and the chunk is within
  `min(render distance, 32) + 1` chunks of the player, the packet is stored and the handler cancelled.
  Other dimensions (the far side of portals) unload as before.
- `ClientPacketListener.handleLevelChunkWithLight` (`m_183388_`) HEAD: when the server sends a chunk
  again, any held unload for it is dropped, so a stale unload can never punch a hole in live terrain.
- `Minecraft.tick` (`m_91398_`) RETURN, every 10 ticks: held unloads more than 3 chunks past the hold
  radius, or in a dimension the player has left, are released by running the stored packet through the
  normal handler (so every other mod hooking it sees the unload). For a dimension the player has left,
  the listener is pointed at that level with Immersive Portals' own `ip_setWorld`, the way its packet
  redirection does. On disconnect, or when Immersive Portals discards a client world, holds are dropped.

**Limits.** Held chunks are snapshots: block changes beyond the server's range do not show until you
come back, and mobs there disappear because the server removes entities separately. Only terrain you
have already received is kept; nothing new is fetched. Farsight's disk preview stays off.

**Tuning.** JVM flags on the client: `-Dfarsight_immptl_compat.maxHoldRadius=N` (default 32) caps the
hold radius to bound memory; `-Dfarsight_immptl_compat.debug=true` logs held and released counts every
30 seconds.

**Build.** `python tools/farsight_immptl_compat/build.py --install` (needs the JDK 17 at
`E:\Program Files\Java\jdk-17`, the CurseForge install's libraries and the Immersive Portals jar in
`mods/`; no Gradle, no network). Output: `build/farsight_immptl_compat-1.0.0.jar`, copied to `mods/`
and `local/`.

**Deploy.** Client only (`clientSideOnly`, `IGNORE_ALL_VERSION`); listed in the client-only prefixes of
`scripts/sync_server_mods.py`, so it is never copied to the server. Players without it behave exactly
as before. Rebuild if Immersive Portals or Farsight change the symbols it uses; the mixin config is
`required: true`, so a mismatch fails loudly at startup.

**Revert.** Delete the jar from `mods/`. Nothing is persisted in worlds.

Rationale and evidence: `docs/CONFIG_CHANGES.md`, "Farsight × Immersive Portals" entry.
