# spooky_pregen

A Forge 1.20.1 mod for the Spooky Dookie pack that pre-generates the world with **Chunky** while the dedicated server is
empty, and pauses the moment a player logs in. It only acts on a dedicated server; on clients and in singleplayer it
loads and does nothing. Chunky (1.3.x) is a required dependency, driven through its API.

**Status:** 1.3.1 installed on the client instance (inert) and live on the VM (2026-11-08).

## Why

World generation in 1.20.1 is mostly single-threaded per dimension. On the VM's E5-2650 v2, Chunky manages about 3–7
new chunks per second, and a player in ungenerated terrain makes the server generate on demand. That caused the stalls
and watchdog crashes of 2026-11-06 and 2026-11-07 (CONFIG_CHANGES "2026-11-07 — Watchdog crash 03:08; max-tick-time;
spooky_tick_fixes 1.3.4 chunk-safe ticking"). Pre-generating while nobody is online uses time the server would
otherwise spend idle.

## How it works

Once a second, on the server thread:

- **Players online:** its Chunky task, if running, is paused. This also happens instantly on login, before other
  listeners, and applies even in force mode (`/pregen start`). `"enabled": false` (after `/pregen reload`) pauses it
  the same way.
- **Empty for `emptyGraceMinutes`:** a paused task is resumed. If there is none, every `checkIntervalMinutes` it picks
  the next one and starts it. A finished task is followed straight away by the next while the server stays empty.
- **Only its own tasks:** a Chunky task someone starts by hand is left alone; if that dimension is busy, it waits. If
  its own task is cancelled or paused with `/chunky`, it holds until `/pregen resume`.

### Work order

1. **`jobs`**, in order. Each job is a centre and a radius. It's generated at `radius`, then `radius + step`, … up to
   `maxRadius`, then the next job. Each step is a square, so nearby terrain is finished first and the area is complete
   whenever it stops. Progress is kept per job name. The pack lists no jobs.
2. **`edges` in `"mode": "blob"`** (the default), once every job is done:
   - **Islands:** it reads which chunks are stored from the region files' location tables, and groups them into
     islands (8-connected).
   - **Specks:** islands under `minIslandChunks` (64) are crumbs from fast travel. Their edges are ignored unless an
     anchor is within 16 chunks.
   - **Seed:** of all edge chunks (missing chunks touching a stored one) on the remaining islands, it takes the one
     **nearest to an anchor**. Anchors are where players are: each player's last logout position and bed, recorded on
     logout and read once from `world/playerdata` at the first start, plus any `extraAnchors`. With no anchors, spawn
     (`centerX`/`centerZ`) is used.
   - **Grow:** a square centred on that edge chunk, from `blobStartRadius` to `blobMaxRadius` in `blobStep` steps. It
     always touches existing land, so it never makes new islands. Then the edge is recomputed and the next seed picked.
   - **Restarts:** the blob in progress is stored in the state file and carries on after a restart.
   - **Example:** on the VM's world as of 2026-11-07 (32 islands, 27 of them specks), a simulation picks the area
     around one player's anchors, then another player's, then alternates. At the pack's blob size (512) a blob is at
     most 64 × 64 = 4,096 chunks, about 10–25 minutes at the VM's rate.
3. **`edges` in `"mode": "nearest"`** (the alternative mode): it reads the 4 KB location table at the start of every
   region file, which gives the exact set of chunks stored on disk without loading any of them. The *edge* is every
   missing chunk that touches a stored one. The edge chunk nearest the rule's centre becomes the centre of the next
   `windowRadius` square, if it is within `maxDistance` on either axis, inside the world border, and not in a square already done since the server started. So the generated area grows outward evenly, and the gaps and thin trails left by
   players' journeys get filled.

Chunks that already exist are never targeted, and Chunky skips any finished chunk it meets. It reads the saved status,
so half-finished fringe chunks get completed.

### Guards

Checked once a minute; a tripped guard pauses the task until it clears.

- Free space on the world's disk below `minFreeDiskGb`.
- The world folder grew more than `maxDiskGrowthMbPerDay` since the first check of the day. 0 turns this off; the
  pack sets 0, so only the free-space floor applies.

### Restarts and state

The task is paused on shutdown, and Chunky saves it. State is in `world/spooky_pregen_state.json`: job radii,
finished jobs, the current task, the blob in progress, the `/pregen radius` size, the player anchors, the hold flag
and the daily-growth baseline. After a restart, the task resumes once the server has been empty for the grace time.
Chunky's own `continueOnRestart` can stay `false`.

The blob size in force is applied at startup, on `/pregen reload` and by `/pregen radius`, through one routine: a
paused or running square larger than the size is cancelled (what it generated stays), and a blob whose next step is
larger than the size is finished, so a new one is picked.

## Configuration: `config/spooky_pregen.json`

Plain JSON, read leniently, so `//` comments are allowed. It's read at server start and by `/pregen reload` (Forge's
file watcher doesn't fire on the VM). A missing file is created with `enabled: false`.

| Key | Default | Meaning |
|---|---|---|
| `enabled` | `false` | Master switch (the pack's file sets it to `true`) |
| `checkIntervalMinutes` | 5 | How often to look for new work while empty |
| `emptyGraceMinutes` | 5 | How long the server must be empty before anything starts or resumes |
| `minFreeDiskGb` | 2.0 | Disk guard |
| `maxDiskGrowthMbPerDay` | 0 | Daily growth cap for the world folder; 0 = off |
| `shape`, `pattern` | `square`, `region` | Passed to Chunky |
| `jobs[]` | — | `name` (required, the progress key), `dimension`, `x`, `z`, `radius`, `maxRadius` (0 = only `radius`), `step` (default 512) |
| `edges[]` | — | `enabled`, `dimension`, `mode` (`blob` or `nearest`), `centerX`, `centerZ` (spawn anchor / nearest-mode centre), `maxDistance` (0 = no limit), `windowRadius` (nearest mode, 256); blob mode: `blobStartRadius` (256), `blobMaxRadius` (1024; the pack sets 512; `/pregen radius` overrides it), `blobStep` (256), `minIslandChunks` (64), `playerAnchors` (true), `anchorMaxAgeDays` (30), `extraAnchors` (`[{name, x, z}]`) |

The pack runs Chunky silent (`"silent": true` in `config/chunky/config.json`): no progress line in the log and no boss
bar. `/pregen status` and `/chunky progress` show progress on demand. spooky_pregen detects completion from Chunky's
API progress event, which fires either way. With `silent` off, Chunky logs a progress line every `updateInterval`
seconds (default 1).

## Commands

Operators (permission level 2) and the server console. Only `radius` takes an argument.

| Command | Effect |
|---|---|
| `/pregen`, `/pregen status` | Enabled, players, how long the server has been empty, hold, guards, free disk, the current task and Chunky's progress (percent, chunks, rate, ETA), the current blob and why it was picked, the anchors (count and newest), and each job's next radius |
| `/pregen next` | What it would start next, including the edge search result. Nothing is generated, but if no blob is chosen yet it picks one and keeps it, so the next start uses that blob |
| `/pregen start` | **Force-start**: begins (or resumes) work immediately, without waiting for an empty server or the check interval, alongside any players already online. It clears a hold. **The next player to join pauses the task and ends force mode**, as do `/pregen stop`, `/pregen hold` and a restart; force mode is never saved. The disk guards still apply. Generation competes with players' chunk loading, so expect extra load |
| `/pregen stop` | Ends force mode and returns to running only while the server is empty. If players are online, the current task pauses at once |
| `/pregen hold`, `/pregen resume` | Stop or allow all work. A hold also ends force mode, and persists across restarts |
| `/pregen skip` | Cancels the current task and moves on. A job or blob goes to its next radius (after its last radius, the blob is finished and a new one is picked); an edge square counts as done until the next restart |
| `/pregen radius` | Shows the blob size, which is the largest radius a blob grows to (the square is twice that across), and whether it comes from the config or the command |
| `/pregen radius <blocks>` | 16–8192. Sets the blob size at once, for the blob in progress too (details below) |
| `/pregen radius reset` | Goes back to the config's `blobMaxRadius`. A config edit plus `/pregen reload` also resizes the blob in progress |
| `/pregen reload` | Re-reads `config/spooky_pregen.json`. If it doesn't parse, the previous settings stay and the reply gives the error |

`/pregen radius <blocks>` in detail:

- The new size applies through the routine described under "Restarts and state". A size between two steps can end a
  blob one step early: with steps 256 → 512, `/pregen radius 384` ends a blob that has only done 256.
- The size is kept in the state file, so it survives restarts, and it overrides `blobMaxRadius` until
  `/pregen radius reset`.
- Example: `/pregen radius 256` gives 512 × 512-block blobs.

## Limits

- A pause stops Chunky handing out more chunks, but chunks already handed out (up to Chunky's 50 in flight) finish
  generating: about 10–15 s on the VM after someone logs in. `-Dchunky.maxWorkingCount=16` in `user_jvm_args.txt`
  would shorten it; generation is serial anyway, so fewer chunks in flight shouldn't cost throughput.
- Square sizes are worth keeping moderate. Chunky checks every chunk in a square. Squares re-checked after a restart
  are skipped quickly, because finished chunks go by at thousands per second.

## Build, deploy and revert

- **Build:** `python tools/spooky_pregen/build.py [--install]`. JDK 17 against the CurseForge install's libraries and
  the Chunky jar; no Gradle, no mixins (shared steps: `tools/_javabuild.py`). The version is in
  `resources/META-INF/mods.toml` and `MANIFEST.MF`.
- **Deploy:** it ships in `mods/` like the other pack-built jars, and `scripts/sync_server_mods.py` copies it to the
  VM. Copy `config/spooky_pregen.json` to the server's `config/` as well.
- **Revert:** delete the jar. Delete `world/spooky_pregen_state.json` to forget progress. Chunky tasks it left saved
  can be cleared with `/chunky cancel`.

## Verification

All on the scratch test server, with tiny squares and short waits:

- **Jobs and edges (1.0.0):** a job grew 32 → 64 → 96; 48 edge windows followed, nearest first, with 0 overlaps across
  54 tasks and two boots. `hold`, `status` and `reload` worked; an unknown dimension was skipped with one warning; a
  task paused at shutdown resumed after a restart and finished.
- **Blob mode (1.1.0):** with a test anchor at (700, 650), the first blob centred at (696, 760), 112 blocks from the
  anchor, and grew 32 → 64 → 96 before a new blob was picked. 15 tasks ran with 0 overlaps.
- **Force mode (1.2.0):** with a 10-minute grace, `/pregen hold` then `/pregen start` cleared the hold and resumed the
  paused blob in the same second; `/pregen stop` ended force mode.
- **Startup resize (1.3.1):** a paused radius-128 square with the config at 96 was cancelled at boot, and seven
  squares then ran back to back. Chunky silent: seven squares completed in a row with no `Task running` lines.
- **Not testable headless:** the pause on player login. It uses the same pause call as `hold` and the empty-server
  rule, at HIGHEST priority on `PlayerLoggedInEvent`.

## History

- **1.3.1** (2026-11-07): a join always pauses, even in force mode; the configured blob size is applied at startup.
  CONFIG_CHANGES "2026-11-07 — spooky_pregen 1.3.1: a join always pauses".
- **1.3.0** (2026-11-07): `/pregen radius`; config edits apply to the blob in progress; the pack's `blobMaxRadius`
  1024 → 512. CONFIG_CHANGES "2026-11-07 — spooky_pregen 1.3.0: blob size command, smaller default".
- **1.2.0** (2026-11-07): `/pregen start` and `/pregen stop`. CONFIG_CHANGES "2026-11-07 — spooky_pregen 1.2.0:
  /pregen start and /pregen stop".
- **1.1.0** (2026-11-07): blob mode, nearest to players, became the default. CONFIG_CHANGES "2026-11-07 —
  spooky_pregen 1.1.0: blob mode".
- **1.0.0** (2026-11-07): first version (jobs and nearest-edge windows). CONFIG_CHANGES "2026-11-07 — spooky_pregen
  1.0.0: pre-generation while the server is empty".
- Config, 2026-11-07: `maxDiskGrowthMbPerDay` 1000 → 0 (off). CONFIG_CHANGES "2026-11-07 — spooky_pregen: no daily
  growth cap".
