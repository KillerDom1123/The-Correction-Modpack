# Pack direction review — 2026-07-08

**Scope:** the pack's config philosophy as a whole, as of 2026-07-08: the July balance changes (Lootr and loot
scarcity, the horror configuration, AE2, MineColonies, Cold Sweat, the Ad Astra NASA Workbench gate) read against the
pack's story.
**Summary:** The balance changes already push players toward the behaviour the lore wants: leave the base, explore
dangerous areas, recover knowledge, build infrastructure slowly, and become powerful enough to investigate the truth.
The mechanical foundation is in place; the missing piece is the story layer. The review recommends eleven adjustments,
mostly to reinforce the narrative rather than to change difficulty. Its overall recommendation is to build a KubeJS
lore layer (archive items, loot injection, CC:Tweaked terminals, memory fragments, machine-linked discoveries) instead
of adding mods or rebalancing.
**Status reviewed:** 2026-11-08

| # | Finding | Proposal | Status |
|---|---|---|---|
| 1 | Lootr, rare structures and horror encounters create scarcity | §1 "Recovered Knowledge" loot layer | Implemented in a different form: `kubejs/server_scripts/oc_loot.js` adds the `implore.md` items to vanilla tables (abandoned mineshafts, dungeons, strongholds and ancient cities among them) and to modded structure families, with per-item chances (common 5–10%, rare about 2–3%, very rare about 1%) instead of per-structure ones. The disks, reports, surveys, security logs and spirit echoes named in §1 were not created. `implore_implementation_plan.md` §4; CONFIG_CHANGES "KubeJS — Old Civilisation loot coverage (`oc_loot.js`)" |
| 2 | AE2 progression has no lore framing | §2 ME Controller rename and tooltip | Implemented (tooltip only): `kubejs/client_scripts/oc_tooltips.js` gives `ae2:controller`, `ae2:inscriber` and `ae2:drive` "reconstructing, not inventing" tooltips. The controller is not renamed "Recovered Network Core" |
| 3 | CC:Tweaked is the strongest storytelling opportunity | §3 Archive Terminal | Implemented in a different form: the `archive` program ships in every computer's ROM (`kubejs/data/computercraft/lua/rom/`), with the four record menus. No craftable Archive Terminal or Damaged Archive Disk; the encrypted "anomalous" record opens while any floppy sits in an attached disk drive. Not verified in game. `implore_implementation_plan.md` §5 |
| 4 | The colony is "the first civilisation after The Correction" | §4 Town Hall milestones | Open. Not implemented; the recorded reason is a poor hook. The only town-hall quest is "The Centrepiece" (Town Hall level 5), CONFIG_CHANGES "2026-11-08 — Quest book: fixes and additions" |
| 5 | Cold Sweat makes the environment itself hostile | §5 Climate lore entries | Implemented: both lines are in the `personal_data_chip` and `incident_report` text pools (`oc_tooltips.js`). Cold Sweat was removed in 2026-07. Serene Seasons, re-added 2026-11-01, is the climate layer; its winter settings cite "the winters were never this harsh" (CONFIG_CHANGES "2026-11-01 — Lore and config consistency pass") |
| 6 | The NASA Workbench gate is enough cost | §6 Flight Data Archive | Implemented in a different form: `oldcivilisation:hs_lgtfo_mission_data` is crafted (shapeless, `kubejs/server_scripts/oc_interactions.js`) from the four flight recorders, the colony manifest and the navigation fragment. Rejected part: it does not gate rockets, to keep the sandbox. "The Answer" asks for it (CONFIG_CHANGES "2026-11-08 — Quest book: fixes and additions") |
| 7 | The horror configuration is rare | §7 Encounter evidence | Implemented, as drops: Distorted Replica (the Mimicer), Deep Echo (the Cave Dweller), Fog Residue (The Man From The Fog), plus Temporal Residue (Weeping Angels). They drop on death (90%, `EntityEvents.death` in `oc_interactions.js`) and, since 2026-11-08, when a player survives the encounter (`kubejs/server_scripts/oc_encounters.js`). Fog Residue moved to CANTBREATHE on 2026-11-06 (CONFIG_CHANGES "2026-11-06 — Server tick audit: phases 0 and 1") and was renamed Breathless Residue on 2026-11-08. Born in Chaos and Foes evidence chances were lowered (biological sample 0.05 → 0.02 and similar) |
| 8 | — | §8 Research collection system | Implemented as the optional Recovered Evidence quest chapter (`config/ftbquests/quests/chapters/recovered_evidence.snbt`): catalogue entries for personal, technical, magical, horror, exodus and anomalous evidence and "The Whole Picture" (Correction Fragment); evidence milestones at 4, 10 and 16 kinds (2026-11-08). `implore_implementation_plan.md` §6 |
| 9 | Existing mods can carry environmental storytelling | §9 Environmental storytelling | Open. No custom structures in the pack |
| 10 | Villagers should matter | §10 Easy Villager Trading | No change needed: Easy Villager Trading is not in the pack (`modlist.md`, 2026-11-08) |
| 11 | Rarity makes the horror work | §11 Keep horrors rare, not stronger | Followed: later horror passes make spawns rarer without making the mobs stronger, for example CANTBREATHE (CONFIG_CHANGES "2026-11-01 — Lore and config consistency pass"). The Man From The Fog was disabled on 2026-11-06 |
| — | The story layer is missing | Overall: build the KubeJS lore layer | Implemented: `docs/implore.md` and `docs/implore_implementation_plan.md` (scripts committed 2026-07-08) |

---

## Method and limits

A design reading of the pack's config choices against its story. No play-testing or measurement is part of it.

---

## Findings

### Pack identity

The config philosophy gives the pack a clear identity:

> **A post-collapse survival expedition where civilisation must be rebuilt while investigating why the previous
> civilisation disappeared. Technology, magic, and exploration are not conveniences — they are pieces of a forgotten
> puzzle.**

The balance changes push the player toward the behaviour the lore wants:

* **Leave the base**
* **Explore dangerous areas**
* **Recover knowledge**
* **Build infrastructure slowly**
* **Become powerful enough to investigate the truth**

### Existing systems, as of 2026-07-08

- **Loot:** Lootr, rare structures and horror encounters create scarcity.
- **Cold Sweat:** the environment itself is hostile.
- **Ad Astra:** the NASA Workbench gate already supplies the cost of reaching space.
- **Horror:** the design is rare: a rare Mimicer, a rare Cave Dweller, a rare Man From The Fog, rare Angels. That is
  scarier than "every night has five monsters".
- **AE2 and MineColonies:** the July changes stand; the proposals below only add narrative.

### Intended player journey

**Early game:** "I need shelter." Cold, darkness, food, basic survival.

**Mid game:** "This world was inhabited before us." Create, MineColonies, AE2, magic. Finding fragments.

**Late game:** "They were not simply advanced." Mekanism, reactors, advanced automation. Recovering old systems.

**End game:** "They were not trying to conquer the universe." Ad Astra. They were trying to leave.

### Overall

The pack is coherent. The next major improvement is not more mods or balance changes but the **KubeJS lore layer**:

* archive items
* loot injection
* CC:Tweaked terminals
* memory fragments
* machine-linked discoveries

---

## Proposals

### 1. Loot: knowledge over equipment

- **Recommendation:** add no powerful loot. Make exploration reward **information** with a "Recovered Knowledge" loot
  layer.
- **Target:** structure chest loot, through KubeJS loot modifications.
- **Change:**

  | Structure | Chance | Items | Lore |
  |---|---|---|---|
  | Mineshafts | ~5% | Damaged Archive Disk, Mining Report Fragment, Geological Survey | "The Old Civilisation was not mining for resources." |
  | Dungeons | ~5% | Security Logs, Containment Labels, Personal Data Chips | "Something was being kept here." |
  | Strongholds | ~10% | Encrypted Archive Disk, HS-LGTFO fragments, Research Notes | "The final records were intentionally hidden." |
  | Ancient Cities | ~15% | Memory Fragments, Distorted Memories, Spirit Echoes | "The problem existed before the collapse." |

- **Effect:** exploration pays out in knowledge, not equipment, and keeps the scarcity.

### 2. AE2: lore relevance

- **Recommendation:** rename the ME Controller when crafted, and give it a lore tooltip.
- **Change:** name `Recovered Network Core`; tooltip:

  > "A familiar architecture.
  >
  > The original creator is unknown."

- **Effect:** the player should feel "I am not inventing this." They are **reconstructing**.

### 3. CC:Tweaked: the main lore delivery system

- **Recommendation:** an Archive Terminal.
- **Change:** crafting:

  ```
  Computer
  +
  Disk Drive
  +
  Damaged Archive Disk
  =
  Archive Terminal
  ```

  The terminal runs:

  ```
  archive
  ```

  Output:

  ```
  OLD CIVILISATION ARCHIVE SYSTEM

  Recovered media detected.

  Available records:

  - personnel
  - research
  - incident
  - evacuation
  ```

  Disks are not readable as books; players interact with the terminal.
- **Effect:** a computer terminal in a survival horror pack is much more memorable than "You found Lore Book #12."

### 4. MineColonies: the "Humanity Continues" system

- **Recommendation:** narrative reinforcement only. The colony is not just a village; it is "The first civilisation
  after The Correction."
- **Change:** town-hall level milestones, for example:
  - Town Hall II: "The settlement grows beyond survival."
  - Town Hall V: "For the first time in generations, people are building something permanent."
  - Town Hall VIII: "Old records suggest this resembles structures built before the collapse."
- **Constraint:** citizens never explain lore. They only know myths.

### 5. Cold Sweat: part of the story

- **Recommendation:** lean into the hostile environment with lore entries.
- **Change:**
  - Survivor Journal, for example: "The winters were never this harsh."
  - Research Note, for example: "Atmospheric readings continue to change."
- **Effect:** ties Cold Sweat into "The world itself was altered."

### 6. Ad Astra: more mysterious

- **Recommendation:** add no further cost. Before rockets are allowed, require recovered documents.
- **Change:** a Flight Data Archive recipe:

  ```
  Encrypted Archive Disk
  +
  AE2 Storage Component
  +
  Advanced Machine Component
  ```

  Result:

  ```
  HS-LGTFO Mission Data
  ```

- **Effect:** the player does not simply decide "Let's go to space." They discover "They tried this before."

### 7. Horror: evidence, not loot

- **Recommendation:** avoid mob drops; the horror should feel supernatural. Use rare encounter rewards instead.
- **Change:**
  - **The Mimicer:** after surviving, `Distorted Replica`. Tooltip: "It copied something. / We do not know what."
  - **Cave Dweller:** `Deep Echo`. Tooltip: "Recording origin: / Below known geological limits."
  - **Man From The Fog:** `Fog Residue`. Tooltip: "Composition unknown."

### 8. A "Research" progression layer

- **Recommendation:** a collection system, not gameplay progression.
- **Change:** categories:

  ```
  Old Civilisation Archive

  [Engineering]
  [Magic]
  [Human Records]
  [Anomalies]
  [The Correction]
  [HS-LGTFO]
  ```

- **Effect:** the player slowly fills gaps. It never completes early.

### 9. Environmental storytelling through existing mods

- **Recommendation:** use the installed mods for environmental storytelling:
  - **SecurityCraft:** ancient locked doors, encrypted rooms, old security systems.
  - **Create:** old machines, abandoned mechanisms, incomplete factories.
  - **Immersive Engineering:** old power systems, damaged infrastructure.
  - **Occultism:** spirits remembering things humans forgot.
  - **Ars Nouveau:** magical memory.

### 10. Config: no Easy Villager Trading

- **Recommendation:** if Easy Villager Trading is enabled anywhere, disable it.
- **Effect:** villagers matter. They are descendants, not vending machines.

### 11. Not to change: horror strength

- **Recommendation:** do not make the horrors stronger. The rare design (Mimicer, Cave Dweller, Man From The Fog,
  Angels) is much scarier than "Every night has five monsters."
- **Effect:** players tell stories ("Remember when we found that thing in the forest?"), not complaints ("The forest
  is annoying.").

---

## Sources

- Status review (2026-11-08): `docs/implore_implementation_plan.md`, `docs/CONFIG_CHANGES.md`,
  `kubejs/server_scripts/oc_loot.js`, `kubejs/server_scripts/oc_interactions.js`,
  `kubejs/server_scripts/oc_encounters.js`, `kubejs/client_scripts/oc_tooltips.js`,
  `kubejs/data/computercraft/lua/rom/programs/archive.lua`, `config/ftbquests/quests/chapters/`, `modlist.md`.
