# Writing documentation

How documentation in this repository is written and maintained. It covers every hand-written Markdown file: the root
`README.md`, everything under `docs/`, and each `tools/<mod>/README.md`. It applies to anyone editing those files,
person or tool.

---

## 1. Principles

1. **Document the project, not the conversation.** Every sentence is about the pack: a mod, a config file, a script,
   the server, a behaviour players see. Nothing records who asked for a change, what the writer could or could not do,
   or the order a working session ran in. Git history already records who changed what.
2. **State facts.** What changed, why, how, and how it was verified. No narration and no first person. A
   recommendation is labelled as one.
3. **Use absolute dates.** `2026-11-08`, never "today", "now", "recently", "shortly after" or "the same day".
4. **Record end states.** A change that was made and undone before it reached a player or the server is not an event.
   Record what is true afterwards.
5. **Give each fact one home.** Other documents link to it instead of restating it.

---

## 2. Document types

| Type | Files | Contains | How it is updated |
|---|---|---|---|
| Change log | `docs/CONFIG_CHANGES.md` | One dated entry per change (§4) | Append only. Afterwards only an entry's **Status** line and a *Superseded* note may change |
| Pack reference | `README.md` (from "Multiplayer" down) | Hosting, commands, repository layout | Edited in place to match the released pack and the live server |
| Component reference | `tools/<mod>/README.md` | What a pack-built mod does: behaviour, config, commands, build, limits, version history (§5) | Edited in place to match the source, with a status line for what is installed |
| Design spec | `docs/sanity_mechanic.md`, `docs/ftbquest.md`, `docs/implore.md`, `docs/implore_implementation_plan.md` | Intended behaviour and the reasons for it (§6) | Edited in place; the Status header is kept current |
| Report | `docs/<TOPIC>_<YYYY-MM-DD>.md` | A point-in-time review, audit or investigation (§7) | Frozen after its date, except its Status table and dated corrections |
| Lore and pitch | `docs/thelore.md`, `docs/blurb.md`, `README.md` (top half) | The in-world story and the player-facing pitch | In-world voice; §3 does not apply |
| Generated | `modlist.md`, `docs/SERVER_MODS.md` | Output of `tools/generate_modlist.py` and `tools/generate_server_modlist.py` | Never edited by hand. Hand-written notes live in the generator scripts and follow §3 |
| Third-party | `kubejs/README.txt` | KubeJS's own boilerplate | Not edited |

A new document gets one of these types before it is written. If none fits, the content probably belongs in an
existing document. Every document starts with an `#` title. A chat reply is never saved as a document; its findings
are rewritten into the right type first.

---

## 3. Voice and wording

### 3.1 Rules

- **Subject.** The subject of a sentence is the thing documented: "The Wendigo keeps away from players", "`timer`
  changed from 120 to 360". It is never "the user", "I" or "we".
- **Reasons.** Give the actual reason: the problem observed, or the design goal. When a change is a design preference
  with no further rationale, state the design itself ("Design: Waystone warping costs no XP."). Do not invent a
  rationale, and do not attribute the change to a person.
- **People.** Refer to roles: "players", "the server operator", "operators (permission level 2)". Do not name
  individuals or write "the user".
- **Tense.** In the change log, events are past tense or terse before → after fragments. Reference docs, specs and
  READMEs describe current behaviour in the present tense.
- **Time.** Absolute dates only. A state claim that will go stale carries its date: "Live on the VM (2026-11-08)."
- **Uncertainty.** State it once, as a fact: "Cause not determined." Do not hedge every sentence.
- **Evidence.** One line naming the source: `Source: PlayerWaystoneManager#getExperienceLevelCost, Waystones 14.1.21.`
  Do not narrate how it was found.
- **What was not done.** Record it as a state ("Not boot-tested.", "VM: not applied."), not as a story about why the
  session didn't do it.
- **Numbers.** Exact values with units and before → after. Exact mod versions and jar names.
- **Second person.** "You" is fine in player-facing text and in instructions to an operator ("run `/pregen hold`").
  It is never used to address the project owner ("your call").
- **Failed attempts.** Record only the constraint they revealed: "`map_writing_distance` rejects 192 (log); left at
  -1." Not "Attempted 192, but Xaero rejected it … reverted."
- **Files.** Refer only to files in the repository, or mark the others: "`alexsmobs.toml.bak` (local only; `*.bak` is
  git-ignored)."

### 3.2 Names

Use these names, and never "this instance", "this PC" or "here":

| Name | Means |
|---|---|
| the client instance | This repository's CurseForge instance ("Spooky Dookie") |
| the VM | The live dedicated server (Proxmox VM, deployed over FTP by `scripts/sync_server_mods.py`) |
| Server v2 | The local test server in `Servers/Server v2`, not live |
| the July server | `Servers/spookydookie`, kept unchanged for reference |
| the pack update | The client pack as released to players |

### 3.3 Replacements

| Don't write | Write instead |
|---|---|
| "at the user's request", "the user asked for X", "requested", "the user wanted" | The reason, or "Design: …" |
| "The user supplied a new `minecolonies-server.toml`" | "`minecolonies-server.toml` replaced with a 1.1.1301-format file." |
| "3 min with our config", "we", "us" | "3 min in the pack's config" |
| "I could not run the client" | "Not tested in game." |
| "The automated upload was refused by the session's permission check" | "VM: not applied." |
| "Not boot-tested: the user's game was running, with 2.7 GB of RAM free" | "Not boot-tested. Required before deploying." |
| "Disabled again the same day… Re-enabled on both sides shortly after…" | "Enabled on the client and the VM (2026-11-08)." |
| "now", "currently", "at the moment", "at some point" | "as of 2026-11-08", or drop the word |
| "Proposals — not applied (your call)" | "Proposals — not applied" |
| "if wanted" | "Optional: …" |
| "the published Claude artifact" | "the static map snapshot" |
| A paragraph on how a cause was tracked down | "Cause: …" plus a `Source:` line |

---

## 4. Change log (`docs/CONFIG_CHANGES.md`)

### 4.1 Layout

```
# Config Changes
(intro and the scope table)

## Baseline (2026-07)            ← the original per-mod rebalance record
### The Mimic …
### Cave Dweller …

## 2026-11                       ← one heading per month, oldest first
### 2026-11-01 — …
### 2026-11-08 — Waystones: warping costs no XP

## Appendix A — OpenLoader packs
```

Entries are `###` under their month, in date order. A new entry goes at the end of the current month. Baseline
sections keep the form they were written in: a **Goal** line, a before → after table, and a **Why** line.

### 4.2 Entry template

```markdown
### YYYY-MM-DD — <Subject>: <what changed>

- **Change:** `<file>`: `<key>` <before> → **<after>**. (Use a table for three or more settings.)
- **Reason:** <the problem observed, or the design goal>.
- **Mechanism:** <optional: how the mod behaves, as far as needed to justify the change>. Source: <class#method and
  mod version, a log line, or a profile link>.
- **Rejected:** <optional: alternatives considered, and why each was not used>.
- **Verification:** <what was tested and how>. Not tested: <what wasn't>.
- **Applies to:** <the client instance | the VM | both>; <on new chunks | to new worlds only | …>.
- **Status:** <Built, not installed | Client instance only | Uploaded to the VM, live after restart | Live on the VM>
  (YYYY-MM-DD).
- **Maintenance:** <optional: what to re-check when a mod updates>.
- **Revert:** <exact steps>.
- **Related:** <optional: report findings, and other entries whose reasoning this change affects>.
```

- **Heading.** Date, subject, and the change in a few words; under about 80 characters. Scope and status go in the
  fields, not the heading.
- **Fields.** Use these labels exactly, in this order. Leave out fields that don't apply; don't invent new ones.
  ("Verified", "Tested" and "Checked" are all **Verification**. "Deployed" and "On the VM" are both **Status**.)
- **One topic per entry.** A release of a pack-built mod is one entry: a summary of three or four bullets that links to
  the mod's README for detail. An unrelated fix found on the same day gets its own entry.
- **Status** is the only line updated after the entry is written, and each update carries its date. "Takes effect at
  the next restart" is a status, so it is replaced once the restart has happened.
- **Superseded entries** are not rewritten. Add one line under the old heading, and write a new entry for the change:
  `> Superseded 2026-11-08 by "Waystones: warping costs no XP".` Before writing an entry, search the log for the mod's
  name. Every earlier entry that the change reverses or replaces gets this line, Baseline sections included.
- **Same-day reversals** are not recorded. Record the state that resulted.
- **Routine regeneration** of `modlist.md` and `docs/SERVER_MODS.md` is not recorded. §8 already requires it.

---

## 5. Component READMEs (`tools/<mod>/README.md`)

`##` sections, in this order (leave out any that don't apply):

1. **Title and summary**: what the mod is, the mod versions it targets, which side it runs on, and a status line:
   `**Status:** 1.4.0 installed on the client instance and the VM (2026-11-07).` A retired mod says so here:
   `**Retired 2026-11-06:** DimThreads was removed; this jar is disabled with it.`
2. **Why**: the problem, as a finding plus evidence, linking to the change-log entry or report.
3. **How it works**: present tense, one subsection per feature or config section, **never per version**.
4. **Configuration**: a table of key, default, the pack's value, and meaning.
5. **Commands**: a table of command, permission and effect.
6. **Limits**: side effects, known gaps, behaviour that differs from the unpatched mod.
7. **Build, deploy and revert**: the `build.py` invocation, where the jar goes, version pins, and when to rebuild.
8. **Verification**: what was tested, how and when, and what was not.
9. **History**: one line per version, newest first, each linking to its change-log entry:
   `1.4.0 (2026-11-07): Wendigo keeps away and attacks only when provoked. CONFIG_CHANGES "Antlers Wendigo: …".`

The README describes the source in its folder. When the source is ahead of what is installed, the status line says
so (`**Status:** source 1.5.0, built 2026-11-08, not installed. Installed on the client instance and the VM: 1.4.0.`),
and each feature that isn't installed yet is marked `(1.5.0, not installed)`. Version numbers in examples and paths
use a placeholder (`build/<mod>-<version>.jar`) so they don't go stale.

---

## 6. Design specs

Each spec opens with a status block:

```markdown
> **Status:** Draft | Implemented in `kubejs/server_scripts/oc_sanity.js` (2026-11-08) | Superseded by <doc>
> **Related:** `docs/thelore.md`, `docs/CONFIG_CHANGES.md` ("<entry heading>")
```

- The body describes the design in the present tense, together with the reasons for it.
- Decisions are stated as decisions ("Namespace: `oldcivilisation:`"), not as a record of a discussion ("Decisions
  taken: …", "Open decisions (need your call)").
- Once a feature is built, the spec is edited to match what was built. Parts of the design that were never built are
  listed under **Not implemented**. There is no separate list of differences.
- Proposals that haven't been accepted are listed under **Proposals**, each with a status: proposed, accepted
  (linked to its change-log entry) or rejected (with the reason).
- Build logs, file lists and verification checklists belong in the change log, not in the spec.

---

## 7. Reports (`docs/<TOPIC>_<YYYY-MM-DD>.md`)

A report records an investigation as of its date. It opens with:

```markdown
# <Topic> — YYYY-MM-DD

**Scope:** <what was examined: mods, server, profile links>
**Summary:** <three to five sentences of findings>
**Status reviewed:** YYYY-MM-DD

| # | Finding | Proposal | Status |
|---|---|---|---|
| 1 | … | §5.2 | Applied: CONFIG_CHANGES "<entry heading>" / Open / Open decision: A or B / Rejected: <reason> |
```

Then, in this order:

1. **Method and limits**: how the evidence was gathered and what it can't show. Keep it short.
2. **Findings**: organised by topic, not by the order of the investigation. Present tense, tied to the snapshot
   ("as profiled on 2026-11-06").
3. **Proposals**: one template each: **Target**, **Change**, **Why it is safe**, **Effect**, **Revert**.
4. **Sources**: profile links, jar versions, log files.

Rules:

- After its date, the **Status** column and the "Status reviewed" date are kept current, and nothing else about the
  report grows. New developments go in the change log, or in a new report.
- A statement found to be wrong is corrected in place, with a dated marker: `(Corrected 2026-11-07: …)`. Do not
  stack "Correction" or "Since then" paragraphs at the top while the original text stands unchanged.
- Applied changes are recorded only in the change log. The report links to them by entry heading.
- Reusable procedures (how to take a profile, how to deploy) belong in the relevant tool README, or in the README's
  hosting section, not in a report.

---

## 8. Keeping documents consistent

| When this changes | Update |
|---|---|
| Any config, datapack, script or mod-set change | A change-log entry |
| A pack-built mod's behaviour or version | Its README (behaviour and Changelog), a change-log entry, and the README's repository summary if that changes |
| A jar is added, removed or disabled | Regenerate `modlist.md` and `docs/SERVER_MODS.md` |
| A command is added or changed | The README's command table and the mod's README |
| A change reaches the VM | The **Status** line of its change-log entry |
| A change reverses or replaces an earlier one | A *Superseded* line under each earlier entry (§4.2), and the Status of any report finding that proposed it |
| A spec is implemented | The spec's Status block |
| Server environment (Java, memory, run user, view distance) | The notes in `tools/generate_server_modlist.py`, then regenerate `docs/SERVER_MODS.md`. That is the one home for VM facts |

The root `README.md` describes what is installed and live, not what is built or planned.

---

## 9. Public repository

The repository is public on GitHub. Documents never contain credentials, IP addresses, hostnames, account usernames,
player usernames, email addresses, real names or home-directory paths. Write "the VM", give server paths relative to
the server root (`files/server/config/…`), and write "a player's base", not the player's name or their base's name.

---

## 10. Pre-commit check

```sh
# Requester and session references (review each hit; quotes and lore can match legitimately)
grep -nEi "the user|user's|you asked|your call|requested|at the request|this session|claude|shortly after|the same day|at some point|currently" \
  README.md docs/*.md tools/*/README.md
# First person outside lore
grep -nwE "I|I'm|I'd|I've|we|We|our|Our|us" README.md docs/*.md tools/*/README.md \
  | grep -vE "docs/(thelore|blurb)\.md"
```

---

## 11. Examples

### 11.1 A whole entry (`CONFIG_CHANGES.md`, Waystones, 2026-11-08)

Before (excerpt):

> ### Waystones: no XP cost (2026-11-08) — *client instance; VM upload pending*
> - **Why:** the user asked to remove the XP cost of Waystone warping.
> - …
> - **Side note:** the 2026-07 Mahou Tsukai displacement cap (256 blocks) was justified by long trips going "through
>   the XP-costed Waystones". That reason no longer holds; the cap is left as it is.
> - **VM:** not applied yet. The automated upload was refused by the session's permission check (production deploy).
>   The VM's current file matches the old repo copy. …

After:

> ### 2026-11-08 — Waystones: warping costs no XP
> - **Change:** `config/waystones-common.toml` `[xpCost]`: `minimumBaseXpCost` 1.0 → **0.0**, `maximumBaseXpCost`
>   3.0 → **0.0**, `dimensionalWarpXpCost` 3 → **0**.
> - **Reason:** Design: Waystone warping costs no XP. The Eye of Ender recipe gate (`WaystonesGate`), activation by
>   visiting, and cooldowns are unchanged.
> - **Mechanism:** cost = round((base + leashed animals × `xpCostPerLeashed`) × the warp mode's multiplier). With a
>   base of 0, every warp mode is free; the multipliers (all 1.0) are left as they are. Source:
>   `PlayerWaystoneManager#getExperienceLevelCost`, Waystones 14.1.21.
> - **Applies to:** both; in multiplayer the VM's values apply.
> - **Status:** client instance (2026-11-08). VM: not applied.
> - **Revert:** set the three values back (1.0 / 3.0 / 3).
> - **Related:** the Mahou Tsukai displacement cap (256 blocks, Baseline) cited XP-costed Waystones as its reason. The
>   cap is unchanged.

The Baseline "Waystones" section also gets
`> Superseded 2026-11-08 by "Waystones: warping costs no XP" (XP cost only; the Eye of Ender gate stands).`

### 11.2 Smaller cases

**A reversal told as a story** (`CONFIG_CHANGES.md`, Ultimine shapes, 2026-11-08)

> Before
> - **On the VM (2026-11-08, at the user's request):** uploaded with `scripts/sync_server_mods.py --yes` …
> - **Disabled again the same day, at the user's request, until a client update ships.** …
> - **Re-enabled on both sides shortly after, at the user's request:** …

> After
> - **Applies to:** client instance and VM; takes effect at the next VM restart. Players need the pack update that
>   includes the jar, or shape names from index 6 onward are wrong.
> - **Status:** enabled on the client and the VM (2026-11-08).

**Machine state as a reason** (`CONFIG_CHANGES.md`, `spooky_tick_fixes` 1.5.0)

> Before: "**Not boot-tested:** the user's game was running, with 2.7 GB of RAM free, too little for a headless server
> next to it."

> After: "**Verification:** compiles (javac, 67 sources); 9 of 9 injection targets match the class files. Not
> boot-tested; not tested in game. A headless boot is required before deploying."

**First person** (`CONFIG_CHANGES.md`, The Obsessed)

> Before: "A per-player targeter timer (3 min with our config) puts a 'targeter' 40–80 blocks away"

> After: "A per-player targeter timer (3 min in the pack's config) places a targeter 40–80 blocks away"
