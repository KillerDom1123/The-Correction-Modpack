# spooky_ultimine_shapes

A small Forge 1.20.1 mod that adds three mining shapes to **FTB Ultimine 2001.1.8**. Each one is a single layer laid
flat on the face of the block you hit, so it turns with you, like FTB's own **Small Square (3x3)**.

**Status:** 1.0.0 enabled on the client instance (2026-11-08). Uploaded to the VM on 2026-11-08; live there after
the next VM restart. Players need the pack update.

## Why

FTB Ultimine's built-in face-oriented shape is only the 3x3. Ultimine Shapes+ JSON shapes can't fill the gap: their
offsets are fixed to world axes, and its `wall` type always runs along X or Z, never turning to face the side you hit.

## How it works

| Shape (in the shape menu) | Size | Blocks |
|---|---|---|
| Square (2x2) | 2 × 2 | 4 |
| *Small Square (3x3): FTB Ultimine's own* | 3 × 3 | 9 |
| Square (5x5) | 5 × 5 | 25 |
| Rectangle (5x3) | 5 wide × 3 tall | 15 |

Nothing is mined behind the hit block. FTB's **Large Tunnel (3x3)** is different: it keeps going deeper until it
reaches `max_blocks`. The Ultimine Shapes+ **Flatten** shapes are different too: they're always horizontal.

### Layout

- **Width** runs across the face, left to right as you look at it.
- **Height** runs up the face on a wall. On a floor or ceiling it runs forward, away from you.
- **Odd sizes** are centred on the block you hit. A 3-tall shape aimed straight ahead at head height clears from
  your feet to one block above your head.
- **Even sizes** can't be centred, so the extra block goes to your right, or forward on a floor or ceiling. On a wall
  it also goes towards your eye level, so a 2x2 aimed at either the head-height or the foot-height block in front of
  you clears the same 2-tall walkway.
- **Order:** blocks are taken nearest first. If `max_blocks` (81 in the pack) or hunger runs out, the outer edge is
  lost first.
- **What is mined:** the same rules as FTB's shaped modes. A block must pass the shaped matcher (`merge_tags_shaped`,
  default `*`, any block). Air is skipped.

### Shape order: why both sides need it

FTB Ultimine keeps one shape list on each side and links the two by index. The server sends the index of the
selected shape, and the client looks up the name to show. So client and server must register the same shapes in the
same order:

1. FTB Ultimine's built-ins, from its constructor (indices 0–5).
2. These three shapes, from this mod's common-setup handler (indices 6–8). It runs in the parallel part of common
   setup on purpose: after every constructor, and before any `enqueueWork` task.
3. Ultimine Shapes+'s JSON shapes, from its common-setup `enqueueWork` (Flatten 3x3/5x5/9x9 at 9–11).

A scan of every jar in `mods/` found no other mod that registers shapes. The log line
`[spooky_ultimine_shapes] registered 3 shapes at indices 6..8` confirms the order on each side.

## Limits

The mod has no network channel, so a client without it can still join. That client shows the wrong names for every
shape from index 6 onward, including the Flatten shapes. The server and the players' pack should update together.

## Build, deploy and revert

- **Build:** `python tools/spooky_ultimine_shapes/build.py --install`. It needs JDK 17 at
  `E:\Program Files\Java\jdk-17`; no Gradle, no network. It puts `spooky_ultimine_shapes-<version>.jar` in `mods/`
  and `local/`.
- **Deploy:** `python scripts/sync_server_mods.py` copies it to the VM. It takes effect at the next restart. Players
  need the pack update too.
- **Pinned** to FTB Ultimine `[2001.1.8]` in `mods.toml`, because `ShapeRegistry` and `ShapeContext` are outside
  FTB's api package. After an FTB Ultimine update, check those classes, rebuild, and widen the range.
- **Revert:** delete the jar from `mods/` on both sides. Nothing is saved in worlds.

## Verification

- Offline: the shape layout for every wall direction, floor and ceiling, plus head- and foot-height aims for the 2x2.
  14 cases, all pass.
- Not tested in game.

## History

- **1.0.0** (2026-11-08): first version. CONFIG_CHANGES "2026-11-08 — spooky_ultimine_shapes 1.0.0: face-oriented
  2x2, 5x5 and 5x3 shapes".
