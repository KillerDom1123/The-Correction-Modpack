# farsight_immptl_compat

A small client-only Forge 1.20.1 mod that lets **Farsight 1.20.1-5.1** keep chunks the server has unloaded while
**Immersive Portals 3.0.7** is installed.

**Status:** 1.0.1 installed on the client instance (2026-11-07). Client only; never copied to the VM.

## Why

Farsight has two halves:

- **Radius:** one half tells the client the server allows `maxRenderDistance` (64) chunks, so the render distance is
  not capped at the server's `view-distance`. This half works under Immersive Portals.
- **Hold:** the other half is Farsight's own client chunk manager. It holds back the server's
  `ClientboundForgetLevelChunkPacket` until the chunk is beyond the client's render distance plus 10.

Immersive Portals replaces the client chunk cache with `ImmPtlClientChunkMap`. Both mods inject at
`ClientLevel.<init>` RETURN; Immersive Portals does not check what is already there, and its renderer depends on its
own map. Farsight then logs `Farsight not enabled for: ... as other mods changed the chunk source to: class
qouteall.imm_ptl.core.chunk_loading.ImmPtlClientChunkMap`, and the server's unloads apply at once. On a server, the
view ends at the server's `view-distance`, whatever the client slider says.

`ImmPtlClientChunkMap` already stores chunks at any distance, and nothing else in Immersive Portals drops client
chunks, so only the hold-and-release half is missing.

## How it works

Two client mixins and one helper class (`HeldUnloads`):

- **Hold:** `ClientPacketListener.handleForgetLevelChunk` (`m_5729_`), HEAD. In the dimension the player stands in,
  when the chunk map is Immersive Portals' and the chunk is within `min(render distance, maxHoldRadius) + 1` chunks
  of the player, the packet is stored and the handler cancelled. Other dimensions (the far side of portals) unload as
  before.
- **Drop on resend:** `ClientPacketListener.handleLevelChunkWithLight` (`m_183388_`), HEAD. When the server sends a
  chunk again, any held unload for it is dropped, so a stale unload never punches a hole in live terrain.
- **Release:** `Minecraft.tick` (`m_91398_`), RETURN, every 10 ticks. Held unloads more than 3 chunks past the hold
  radius, or in a dimension the player has left, are released by running the stored packet through the normal
  handler, so every other mod hooking it sees the unload. For a dimension the player has left, the listener is
  pointed at that level with Immersive Portals' own `ip_setWorld`, the way its packet redirection does.
- **Reset:** on disconnect, or when Immersive Portals discards a client world, holds are dropped.

## Configuration

JVM flags on the client:

| Flag | Default | Meaning |
|---|---|---|
| `-Dfarsight_immptl_compat.maxHoldRadius=N` | 64 | Caps the hold radius to bound memory. 64 is Farsight's slider maximum, so the render distance is followed in full |
| `-Dfarsight_immptl_compat.debug=true` | off | Logs held and released counts every 30 seconds |

A render distance of 64 holds about 16,600 chunks, against about 4,200 at 32: four times the chunk memory on the client
heap, and four times the meshes on the GPU. On a client with a small heap, set `maxHoldRadius=32` or lower the
slider.

## Limits

- Held chunks are snapshots. Block changes beyond the server's view distance (8–12 on the VM, set by Dynamic View)
  don't show until the player comes back, and mobs there disappear, because the server removes entities separately.
- Only terrain already received is kept; nothing new is fetched.
- Farsight's disk preview stays off.

## Build, deploy and revert

- **Build:** `python tools/farsight_immptl_compat/build.py --install`. It needs JDK 17 at
  `E:\Program Files\Java\jdk-17`, the CurseForge install's libraries and the Immersive Portals jar in `mods/`; no
  Gradle, no network. Output: `build/farsight_immptl_compat-<version>.jar`, copied to `mods/` and `local/`.
- **Deploy:** client only (`clientSideOnly`, `IGNORE_ALL_VERSION`). The prefix is in the client-only list of
  `scripts/sync_server_mods.py`, so the sync never copies it to the server. Players without it behave exactly as
  before. Players get it with the pack update.
- **Rebuild** if Immersive Portals or Farsight change the symbols it uses. The mixin config is `required: true`, so
  a mismatch stops the client at startup with a clear error.
- **Revert:** delete the jar from `mods/`. Nothing is saved in worlds.

## Verification

- 1.0.0 (2026-11-03): compiles against the Forge 1.20.1 SRG client jar and Immersive Portals 3.0.7. Every injection
  target and shadowed field was checked against the real classes with `javap`. Expected log line on start:
  `[farsight_immptl_compat] loaded: Farsight's delayed chunk unload now runs on Immersive Portals' chunk map`.
- 1.0.1 (2026-11-07): the built jar's default hold radius was checked (64).

## History

- **1.0.1** (2026-11-07): the default hold radius limit is 64 (was 32), so held chunks follow the full render
  distance. CONFIG_CHANGES "2026-11-07 — farsight_immptl_compat 1.0.1: held chunks follow the full render distance".
- **1.0.0** (2026-11-03): first version. CONFIG_CHANGES "2026-11-03 — Farsight × Immersive Portals: client
  chunk-keeping shim".
