# spooky_pregen

A Forge 1.20.1 mod for the Spooky Dookie pack that pre-generates the world with **Chunky** while the dedicated server is
empty, and pauses the moment a player logs in. It only ever acts on a dedicated server. On clients and in singleplayer
it loads and does nothing. Chunky (1.3.x) is a required dependency, driven through its API.

## Why

World generation in 1.20.1 is mostly single-threaded per dimension (see `docs/CONFIG_CHANGES.md`, 2026-11-07). On the
VM's E5-2650 v2, Chunky manages about 3–7 new chunks per second, and a player in ungenerated terrain makes the server
generate on demand. That caused the stalls and watchdog crashes of 2026-11-06/07. Pre-generating while nobody is
online uses time the server would otherwise spend idle.

## What it does

Once a second, on the server thread:

- **Players online:** if its Chunky task is running, it's paused (also instantly on login, before other listeners).
- **Empty for `emptyGraceMinutes`:** a paused task is resumed. If there is none, every `checkIntervalMinutes` it picks
  the next one and starts it. A finished task is followed straight away by the next while the server stays empty.
- **Work order:**
  1. **`jobs`**, in order. Each job is a centre and a radius. It's generated at `radius`, then `radius + step`, … up to
     `maxRadius` (a square, so nearby terrain is finished first, and the area is complete whenever it stops), then
     the next job. Progress is kept per job name.
  2. **`edges` in `"mode": "blob"`** (the default since 1.1.0), once every job is done:
     - **Islands:** read which chunks are stored from the region files' location tables, and group them into islands
       (8-connected).
     - **Specks:** islands under `minIslandChunks` (64) are crumbs from fast travel. Their edges are ignored unless an
       anchor is within 16 chunks.
     - **Seed:** of all edge chunks (missing chunks touching a stored one) on the remaining islands, take the one
       **nearest to an anchor**. Anchors are where players are: each player's last logout position and bed, recorded on
       logout and read once from `world/playerdata` at the first start, plus any `extraAnchors`. With no anchors, spawn
       (`centerX`/`centerZ`) is used.
     - **Grow:** a square centred on that edge chunk, from `blobStartRadius` to `blobMaxRadius` in `blobStep` steps. It
       always touches existing land, so no new islands. Then the edge is recomputed and the next seed picked.
     - **Restarts:** the blob in progress is stored in the state file and carries on after a restart.
     - **Example:** on the VM's 2026-11-07 world (32 islands, 27 of them specks) a simulation picks Milton___'s area,
       then ilovepuffles111's, then alternates, with about 7,500–15,000 new chunks per blob (25–50 min each).
  3. **`edges` in `"mode": "nearest"`** (the 1.0.0 behaviour): It reads the 4 KB location table at the start of every region file, which
     gives the exact set of chunks stored on disk without loading any of them. The *edge* is every missing chunk that
     touches a stored one. The edge chunk nearest the rule's centre (within `maxDistance` on either axis, inside the
     world border, and not in a square already done this session) becomes the centre of the next `windowRadius`
     square. So the generated area grows outward evenly, and the gaps and thin trails left by players' journeys get
     filled. Chunks that already exist are never targeted, and Chunky skips any finished chunk it meets (it reads the
     saved status, so half-finished fringe chunks get completed).
- **Guards** (checked once a minute; a tripped guard pauses the task until it clears):
  - free space on the world's disk below `minFreeDiskGb`;
  - the world folder grew more than `maxDiskGrowthMbPerDay` since the first check of the day (0 = off).
- **Restarts:** the task is paused on shutdown (Chunky saves it). State is in `world/spooky_pregen_state.json` (job
  radii, finished jobs, the current task, hold flag, daily-growth baseline). After a restart the task resumes once the
  server has been empty for the grace time. Chunky's own `continueOnRestart` can stay `false`.
- **Only its own tasks:** a Chunky task someone starts by hand is left alone. If that dimension is busy, it waits. If
  its own task is cancelled or paused with `/chunky`, it holds until `/pregen resume`.

Square sizes are worth keeping moderate. A pause is immediate, but Chunky checks every chunk in a square, and squares
re-checked after a restart are skipped quickly (finished chunks go by at thousands per second).

## Config: `config/spooky_pregen.json`

Plain JSON, read leniently, so `//` comments are allowed. It's read at server start and by `/pregen reload` (Forge's
file watcher doesn't fire on the VM). A missing file is created with `enabled: false`.

| Key | Default | Meaning |
|---|---|---|
| `enabled` | `false` | Master switch (the pack's file sets it to `true`) |
| `checkIntervalMinutes` | 5 | How often to look for new work while empty |
| `emptyGraceMinutes` | 5 | How long the server must be empty before anything starts or resumes |
| `minFreeDiskGb` | 2.0 | Disk guard |
| `maxDiskGrowthMbPerDay` | 0 | Daily growth cap for the world folder, 0 = off |
| `shape`, `pattern` | `square`, `region` | Passed to Chunky |
| `jobs[]` | — | `name` (required, the progress key), `dimension`, `x`, `z`, `radius`, `maxRadius` (0 = only `radius`), `step` (default 512) |
| `edges[]` | — | `enabled`, `dimension`, `mode` (`blob` or `nearest`), `centerX`, `centerZ` (spawn anchor / nearest-mode centre), `maxDistance` (0 = no limit), `windowRadius` (nearest mode, 256); blob mode: `blobStartRadius` (256), `blobMaxRadius` (1024), `blobStep` (256), `minIslandChunks` (64), `playerAnchors` (true), `anchorMaxAgeDays` (30), `extraAnchors` (`[{name, x, z}]`) |

## Commands (operators, level 2)

None of the subcommands takes arguments. The server console works too.


- `/pregen` or `/pregen status`: enabled, players, how long the server has been empty, hold, guards, free disk, the
  current task and Chunky's progress (percent, chunks, rate, ETA), the current blob and why it was picked, the anchors
  (count and newest), and each job's next radius.
- `/pregen next`: what it would start next, including the edge search result. A dry run.
- `/pregen start`: **force-start**. Begins (or resumes) work immediately, without waiting for an empty server or the
  check interval, and **keeps running with players online**. It clears a hold. It lasts until `/pregen stop`,
  `/pregen hold` or a server restart; force mode is never saved. The disk guards still apply. Expect extra load while
  players are on: generation competes with their chunk loading.
- `/pregen stop`: ends force mode and returns to running only while the server is empty. If players are online, the
  current task pauses at once.
- `/pregen hold` / `/pregen resume`: stop or allow all work. A hold also ends force mode, and persists across restarts.
- `/pregen skip`: cancel the current task and move on. A job or blob goes to its next radius (after its last radius,
  the blob is finished and a new one is picked); an edge square counts as done for this session.
- `/pregen reload`: re-read `config/spooky_pregen.json`. If it doesn't parse, the previous settings stay and the reply
  gives the error.

Chunky's progress line goes to the log every `updateInterval` seconds (`config/chunky/config.json`, default 1).
Raise it (e.g. 60) if the log gets noisy.

## Build / deploy

`python tools/spooky_pregen/build.py [--install]`. JDK 17 against the CurseForge install's libraries and the Chunky
jar; no Gradle, no mixins (shared steps: `tools/_javabuild.py`). The version is in `resources/META-INF/mods.toml` and
`MANIFEST.MF`. It ships in `mods/` like the other pack-built jars, and `scripts/sync_server_mods.py` copies it to the VM.
Copy `config/spooky_pregen.json` to the server's `config/` as well.

**Revert:** delete the jar. Delete `world/spooky_pregen_state.json` to forget progress. Chunky tasks it left saved can
be cleared with `/chunky cancel`.
