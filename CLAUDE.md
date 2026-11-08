# CLAUDE.md

Guidance for Claude Code sessions in this repository.

## The project

"The World After The Correction": a horror-survival Minecraft 1.20.1 Forge modpack. This directory is its CurseForge
instance ("Spooky Dookie"). The repository tracks the pack's configuration and custom content (`config/`,
`defaultconfigs/`, `kubejs/`, `docs/`, `tools/`, `scripts/`), not `mods/` or `saves/`. `modlist.md` is the record of
which mods and versions the configuration targets. See [README.md](README.md) ("Repository & Configuration") for the
layout.

**The repository is public on GitHub.** Never commit credentials, IP addresses, hostnames, account or player
usernames, email addresses, real names or home-directory paths, in docs, code or commit messages.

## Documentation

Every documentation edit follows [docs/WRITING_DOCS.md](docs/WRITING_DOCS.md). Read it before writing or changing any
`.md` file. In short:

- Document the project, not the conversation. Never write "the user asked", "at the user's request", "I", "we",
  "our", or what the session could or couldn't do. Give the actual reason, or "Design: …" for a preference.
- Use absolute dates (`2026-11-08`), never "now", "today", "shortly after" or "the same day".
- Every config, datapack, script or mod-set change gets an entry in [docs/CONFIG_CHANGES.md](docs/CONFIG_CHANGES.md),
  under the current month, in the WRITING_DOCS §4.2 template. When a change reverses an earlier one, add a
  *Superseded* line under the earlier entry.
- Record end states. A change made and undone the same day is not logged. Update an entry's **Status** line (dated)
  when the change reaches the VM.
- Keep the other documents in step (WRITING_DOCS §8). `modlist.md` and `docs/SERVER_MODS.md` are generated; never edit
  them by hand.
- Run the WRITING_DOCS §10 grep check before committing documentation.

## Servers

- **The VM** is the live dedicated server, deployed over FTP by `python scripts/sync_server_mods.py`. The script
  shows the changes and asks before touching the VM; `--dry-run` only shows them. Server facts (Java, memory, pinned
  mods) live in the notes of `tools/generate_server_modlist.py`, published in `docs/SERVER_MODS.md`.
- `Servers/Server v2` is a local test bed, not live.
- Deploying to the VM affects players. Confirm with the maintainer before any upload or restart.
- `mods/` and `kubejs/startup_scripts/` must match on the client and the server, or players are refused at login.
  After any jar change, run `python tools/generate_modlist.py` and `python tools/generate_server_modlist.py`.

## Pack-built mods (`tools/<mod>/`)

- Build with `python tools/<mod>/build.py --install`: JDK 17 plus the CurseForge install's libraries, no Gradle. The
  version lives in the mod's `mods.toml` and `MANIFEST.MF`.
- Each mod's `README.md` follows WRITING_DOCS §5 and carries a status line saying which version is installed.
- Never run a Java test harness with the Minecraft client jar from this directory: its `log4j2.xml` truncates the
  running game's `logs/latest.log`. Run harnesses from a scratch directory, with their own log4j configuration.
