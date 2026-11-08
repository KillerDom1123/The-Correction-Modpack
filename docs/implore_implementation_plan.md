# Old Civilisation story items — implementation

How the story items specified in `implore.md` are built with KubeJS 6, LootJS and FTB Quests.

> **Status:** Implemented. Scripts committed 2026-07-08; loot routing for modded structures added 2026-07-09; quest
> wiring revised 2026-11-08. On the VM, all KubeJS startup and server scripts loaded with 0 errors after the
> 2026-11-08 01:14 restart.
> - Items: `kubejs/startup_scripts/oc_items.js` (33 items: 31 story items and the two sanity clarity consumables),
>   `kubejs/startup_scripts/oc_creative.js` (creative tab), `kubejs/assets/oldcivilisation/models/item/`.
> - Tooltips: `kubejs/client_scripts/oc_tooltips.js`.
> - Loot: `kubejs/server_scripts/oc_loot.js`.
> - Variant stamping, discovery tracking, stateful items, right-click items, residue drops, recipes and the endgame
>   grant: `kubejs/server_scripts/oc_interactions.js`. Residues for surviving an encounter:
>   `kubejs/server_scripts/oc_encounters.js`.
> - Archive terminal: `kubejs/data/computercraft/lua/rom/`.
> - Quests: `config/ftbquests/quests/chapters/`, with stage tags from `kubejs/server_scripts/oc_quest_checks.js`.
>
> **Related:** `docs/implore.md` (item spec), `docs/thelore.md` (hidden truth), `docs/ftbquest.md` (quest-book
> design), `docs/sanity_mechanic.md` (clarity consumables), `docs/PACK_DIRECTION_REVIEW_2026-07-08.md` (proposals #2, #3, #5, #6 and #7, §5),
> `docs/CONFIG_CHANGES.md` ("KubeJS — Old Civilisation script bugfix (`oc_interactions.js`)", "KubeJS — Old
> Civilisation loot coverage (`oc_loot.js`)", "2026-11-08 — Quest book: fixes and additions").
>
> **Not verified in game:**
> 1. Variant stamping (`inv.setItem` with a new stack carrying `oc_variant` / `oc_stage` NBT) and the tooltips that
>    read it. Test: find a data chip in a dungeon, see a random message, no flicker. The same inventory scan's
>    discovery tracking does run: `oc_q_evidence_magic`, mirrored from the scan's `oc_flag_magic`, was granted on the
>    VM at 01:24 on 2026-11-08.
> 2. Born in Chaos and Foes mob drops. The regex table modifiers load without logging a failure (CONFIG_CHANGES
>    "KubeJS — Old Civilisation loot coverage (`oc_loot.js`)").
> 3. The `archive` program. The Lua was checked for keyword, parenthesis and brace balance only.
> 4. The survive-an-encounter residue payout. The `oc_encounters.js` fix (`getUuid()`) was uploaded to the VM on
>    2026-11-08 and takes effect at the next VM restart or `/reload` (last recorded state, 2026-11-08).
>
> Watch `logs/kubejs/` for `[OldCiv]` lines.

---

## 1. Guiding constraints

These rules come from the source docs and drive every technical choice:

| Constraint | Source | How the implementation honours it |
|---|---|---|
| "No single item explains the story. A player should need **5–10 discoveries** before they understand a concept." | implore.md | Every concept is split across many low-information fragments and randomised text pools. No item is a summary. |
| "Every discovery should answer one question while creating two more." | implore.md | The rule heading `oc_tooltips.js`: no single line explains the plot. Text always withholds. |
| "The player should **never receive a complete explanation**." | implore.md | No "here's the plot" book. Patchouli and Modonomicon are installed but not used. |
| Add **perspective**: engineer, mage, survivor, spirit and horror each contradict the others. | implore.md | Every item is tagged to a faction voice (§4, column "Voice"). Contradiction is the point. |
| The quest book is a **modern researcher cataloguing**, not a narrator. | ftbquest.md; design | **Tonal split** (below). Items are raw primary sources; quest text stays the present-day annotator. |
| The world stays a **sandbox**, not a linear adventure map. | implore.md | Lore items come from loot, drops and encounters. Quest hooks are **optional and reward-only**, never hard gates for core play. |
| The Correction Fragment: "do not add until endgame… only after space + magic + engineering." | implore.md | Granted by `oc_interactions.js` only after a technical, a magical and an exodus record and 12 distinct kinds of evidence (§5). |

### The tonal split

- **Story items are raw voices.** First-person, in-world, contradictory, emotional, unlabelled. An ID badge, a
  panicked data chip, a spirit's taunt. The Old Civilisation and its horrors speak *directly*.
- **FTB quest text is the modern cataloguer.** Detached, present-day: "recovered document, cause unknown." The pack's
  quest chapters are written this way.

The player is the researcher. The **items are the evidence**; the **quest book is the lab notebook**. Items never adopt
the cataloguer's detached voice, and the quest book never starts explaining. This separation makes the mystery feel
assembled by the player rather than handed over.

---

## 2. Tooling

| Tool | Version | Role |
|---|---|---|
| **KubeJS 6** | `kubejs-forge-2001.6.5` | Item registration (`startup_scripts`), NBT-driven tooltips (`client_scripts`), right-click behaviour, state escalation and recipes (`server_scripts`). |
| **LootJS** | `lootjs-forge-1.20.1-2.13.1` | Adds the items to vanilla and modded chest loot tables and to mob loot tables. |
| **FTB Quests** | `ftb-quests-forge-2001.4.22` | Optional collection quests; evidence wired into the endgame chapters (§6). |
| **lootintegrations** (+ WDA, Towns and Towers, Born in Chaos and integrated addons) | 4.7 | Normalises structure loot. The LootJS modifiers stack on top of it. |
| **Camerapture** | 1.10.12 | Not used for the Damaged Photograph (§5): a picture needs a runtime-uploaded image bound in NBT. |
| **CC:Tweaked** | 1.113.1 | The `archive` program (§5). |
| **Patchouli / Modonomicon** | installed | Not used. An "evidence index" book would be the only acceptable use, and it must not explain. |
| **Curios, ycurrenci, MineColonies** | installed | Planned `old_currency` trade tie-in; not implemented. |

### File layout

```
kubejs/
  startup_scripts/
    oc_items.js            # registers the 33 items
    oc_creative.js         # puts them in a creative tab (KubeJS 6 adds custom items to no tab)
  server_scripts/
    oc_loot.js             # LootJS chest and mob-table injection
    oc_interactions.js     # variant stamping, discovery tracking, stateful items, right-click, residues, recipes, endgame grant
    oc_encounters.js       # residue when a player survives an encounter
  client_scripts/
    oc_tooltips.js         # tooltips from text pools, picked by NBT
  assets/oldcivilisation/
    models/item/*.json     # each item parents an existing vanilla or Create item model
    textures/item/*.png    # 31 category-coded placeholders, unreferenced
  data/computercraft/lua/rom/
    programs/archive.lua   # the archive program
    autorun/oc_archive.lua # one-line boot hint
    help/archive.txt       # `help archive`
```

Display names are set in `oc_items.js`; there is no lang file.

---

## 3. Technical design

1. **Namespace: `oldcivilisation:`**, matching `implore.md`. KubeJS 6 accepts a custom namespace in
   `event.create('oldcivilisation:…')` (confirmed via `RegistryInfo`).
2. **Randomised text = NBT index + text pool.** One item, many messages. A stack carries `{ oc_variant: <int> }`; the
   tooltip shows `pool[oc_variant % pool.length]`. `oc_interactions.js` stamps a random variant from 0 to 11
   (`OC_VARIANT_ITEMS_MAX` 12) the first time the item appears in a player's inventory; the scan runs every 60 ticks.
   Stamping happens on acquisition because LootJS can't randomise NBT at drop time (`oc_interactions.js` header). The
   variant is fixed once stamped, and
   the tooltip path never calls `Math.random()`, so a given item never flickers. This gives the Personal Data Chip,
   Damaged Photograph, Incident Report, Arcane Notes and the rest their variety without hundreds of registrations.
3. **Multi-item concepts stay multi-item** where the spec says so: Memory Fragments (faded, distorted, forbidden: 3
   items) and the Black Box (launch, transit, arrival, final: 4 items). Rarity escalates per variant.
4. **Icons.** Each item's model parents an existing vanilla or Create item model (for example
   `create:item/electron_tube` for the data chip, `minecraft:item/music_disc_13` for the audio log). Missing textures
   would still function (purple and black), so art never blocks logic. Authored art is not implemented.
5. **State lives in `player.persistentData`:** `oc_discoveries` (count of distinct lore item IDs seen), `oc_seen`
   (the IDs, as `[id]` strings), the path flags `oc_flag_tech`, `oc_flag_magic` and `oc_flag_space`, and
   `oc_correction_granted`. Server code stamps a stateful item's `oc_stage` when a threshold is crossed, so the
   **client tooltip only reads NBT** and no custom networking is needed. Thresholds: Unstable Memory stage 1/2/3 at
   4/9/16 discoveries; Signal Device stage 1 at 10.
6. **Audio logs** are right-click items: they play an existing sound (`minecraft:entity.elder_guardian.curse`) and
   print one of three transcript lines, chosen by discovery count. No custom sound registration.
7. **No hard gating.** Only the endgame Correction Fragment and the final chapter depend on items. The HS-LGTFO
   Mission Data does not gate rockets.
8. **Clarity consumables** (`sedative_ampoule`, `familiar_photograph`, specified in `sanity_mechanic.md`) use the same
   registration, tooltip and loot pipeline, but `OC_NON_LORE` excludes them from discovery tracking and variant
   stamping.
9. **Rhino rule:** declarations inside event callbacks use `var`, not `const`/`let`. This KubeJS build's Rhino engine
   mis-hoists `const`/`let` declared inside a `try` block in a repeatedly invoked callback (CONFIG_CHANGES "KubeJS —
   Old Civilisation script bugfix (`oc_interactions.js`)"). `Entity#getUUID` is exposed as `getUuid()`.

### Code patterns (from the built scripts)

**Registration** (`oc_items.js`):
```js
const b = event.create('oldcivilisation:' + id).displayName(name)
b.maxStackSize(opts.stack !== undefined ? opts.stack : 16)
if (opts.rarity) b.rarity(opts.rarity)
if (opts.glow) b.glow(true)
```

**Tooltip from NBT** (`oc_tooltips.js`):
```js
tt.addAdvanced(id, (item, advanced, text) => {
  const v = ((ocInt(item, 'oc_variant') % pool.length) + pool.length) % pool.length
  ocRender(text, pool[v])
})
```
A leading `»` marks a corrupted or redacted line and `~` an anomalous one (both dark grey italic); other lines are grey.

**Loot injection** (`oc_loot.js`):
```js
event.addLootTableModifier(tables).randomChance(chance).addLoot(item)
event.addLootTableModifier(/born_in_chaos_v1:entities\/.*/).randomChance(0.02).addLoot('oldcivilisation:biological_sample')
```

**Right-click escalation** (`oc_interactions.js`):
```js
ItemEvents.rightClicked('oldcivilisation:signal_device', event => {
  var d = event.player.persistentData.getInt('oc_discoveries') | 0
  // d < 10: "SIGNAL DETECTED." / "SOURCE: UNKNOWN."   d >= 10: "SIGNAL DETECTED." / "SOURCE: LOCAL."
})
```
The Signal Device also plays `minecraft:block.sculk_sensor.clicking`.

---

## 4. Item catalogue

`Voice` is the faction perspective (it drives contradiction). `Mech` is complexity: **L** loot only (one fixed
tooltip), **T** randomised tooltip, **S** stateful or scripted. Chances are per chest roll. Vanilla tables follow
`implore.md` (common 5–10%, rare about 2–3%, very rare about 1%); modded structure families run slightly lower.

| # | ID (`oldcivilisation:`) | Category | Voice | Source | Mech |
|--:|---|---|---|---|:--:|
| 1 | `personal_data_chip` | Personal | Survivor | Village houses, dungeons, mineshafts, mansions 8%; WDA settlements, More Underground Structures, Lost Architects brawler's guild 5% | T |
| 2 | `id_badge` | Personal | Survivor/Engineer | Strongholds, dungeons, mineshafts, pillager outposts 6%; WDA keeps and towers, Better Strongholds 4% | T |
| 3 | `damaged_photo` | Personal | Survivor | Village houses, mansions, dungeons 6%; WDA settlements 4% | T |
| 4 | `old_currency` (×3) | Joke/Immersion | Survivor | Village houses and workshops, dungeons, mineshafts, ocean chests 15%; WDA settlements and keeps, YUNG temples and ocean monuments, brawler's guild 10% | L |
| 5 | `employee_handbook` | Joke→Horror | Survivor→? | Stronghold libraries, mansions, village temples and cartographers 5%; WDA pub, camp and bandit village, Better Strongholds, brawler's guild 3% | T |
| 6 | `incident_report` | Technical | Engineer | Dungeons, mineshafts, mansions, ancient cities 7%; WDA keeps and industrial sites, Better Strongholds, More Underground Structures, artificer's workshop 4% | T |
| 7 | `audio_log` | Technical | Engineer | Mansions, stronghold libraries, ancient cities 2%; WDA industrial sites, artificer's workshop 1.2% | S |
| 8 | `containment_label` | Technical | Engineer | Stronghold libraries, mansions, ancient cities 1.5%; WDA foundry and mechanical nest 1% | T |
| 9 | `memory_fragment_faded` | Magic | Mage | Village houses, dungeons, mineshafts 6%; WDA mushroom sites and tree house, YUNG ocean monuments and witch huts, More Underground Structures 4% | T |
| 10 | `memory_fragment_distorted` | Magic | Mage | Mansions, strongholds, desert and jungle temples 3%; WDA plague, infested and abandoned sites and monastery/heavenly sites, Better Strongholds, YUNG temples 2% | T |
| 11 | `memory_fragment_forbidden` | Magic | Mage/Correction | Ancient cities, mansions 1.2% | T |
| 12 | `arcane_notes` | Magic | Mage | Mansions, stronghold libraries, ancient cities, temples 5%; WDA monastery, heavenly and mythic-beast sites, Better Strongholds, YUNG temples and witch huts, Lost Architects 3% | T |
| 13 | `bound_thought` | Magic | Spirit | Ancient cities, bastions 2%; WDA Typhon, Ceryneian Hind and Heavenly Conqueror 1.2% | T |
| 14 | `biological_sample` | Horror | Horror | Ancient cities, mansions 3%; Born in Chaos mobs 2%, Foes mobs 2.5%; WDA undead and plague sites, YUNG witch huts, More Underground Structures 2% | T |
| 15 | `impossible_bone` | Horror | Horror | Ancient cities 2%; Born in Chaos mobs 1%, Foes mobs 1.2%; WDA plague, infested and abandoned sites 1.2% | T |
| 16 | `corrupted_map_fragment` | Horror/Anomaly | Correction | Mansions, ocean chests, ancient cities 5%; WDA pirate ship and abandoned temple, YUNG temples and ocean monuments 3%. Four combine (shapeless) into `corrupted_map` | S |
| 17 | `corrupted_map` | Horror/Anomaly | Correction | Crafted from 4 fragments. Tooltip: impossible geography | T |
| 18 | `distorted_replica` | Horror residue | Horror | The Mimicer (§5) | L |
| 19 | `deep_echo` | Horror residue | Horror | The Cave Dweller | L |
| 20 | `fog_residue` ("Breathless Residue") | Horror residue | Horror | CANTBREATHE. The ID is kept from The Man From The Fog, disabled 2026-11-06 | L |
| 21 | `temporal_residue` | Horror residue | Horror | Weeping Angels | L |
| 22 | `colony_manifest` | Exodus | Engineer | Moon dungeon; Ad Astra More Structures rare Mars, Mercury, Venus, Glacio and "other" chests 10% | T |
| 23 | `navigation_fragment` | Exodus | Engineer | Moon dungeon and village chests, Ad Astra More Structures common Mars 12% | T |
| 24 | `flight_recorder_launch` | Exodus | Engineer | Moon dungeon chest, moon village house and blacksmith 10% | T |
| 25 | `flight_recorder_transit` | Exodus | Engineer | Moon dungeon chest, common Mars 10% | T |
| 26 | `flight_recorder_arrival` | Exodus | Engineer | Common and rare Mars 10% | T |
| 27 | `flight_recorder_final` | Exodus | Engineer→Horror | Rare Mars, Venus, Mercury 6% | T |
| 28 | `hs_lgtfo_mission_data` | Exodus | Engineer | Crafted (shapeless) from #22–27. Flavour; does not gate rockets | T |
| 29 | `unstable_memory` | Anomaly | Correction | Ancient cities, mansions, stronghold libraries, moon dungeon, rare Mars 2%; WDA Typhon, Heavenly Conqueror, monastery 1.2%. Tooltip escalates with discoveries | S |
| 30 | `signal_device` | Anomaly | Correction | Ancient cities, rare "other" and Venus chests 2%; WDA mechanical nest, blimp and lighthouse, artificer's workshop 1.2%. Right-click message escalates | S |
| 31 | `correction_fragment` | Final | Correction | **Endgame only.** Granted, never looted (§5) | S |

Also registered: `sedative_ampoule` (×2; dungeons, mineshafts, strongholds, ancient cities, outposts 5%) and
`familiar_photograph` (village houses, mansions, dungeons 4%), the sanity clarity consumables.

`implore.md`'s target table sums to about 36 slots (Personal 5, Technical 8, Magical 6, Horror 6, Exodus 6,
Anomalous 5). Many of those are **text variants of the same item** (dozens of finds, one slowly forming picture), so
31 story items are registered and the NBT text pools supply the rest.

Loot limits:
- Off-world loot goes only into tables that a structure in the pack places (checked 2026-11-08 against every jar's
  structure NBT). Ad Astra More Structures' `common_moon`, `common_other`, `rare_moon`, `common_mercury`,
  `common_venus` and `common_glacio` are never placed.
- YUNG's Better Dungeons and Better Mineshafts reuse the vanilla tables, so they are covered. YUNG's Better Nether
  Fortresses chests are not routed.
- Loot is baked when a chest is first rolled, so changes reach only newly generated or unopened chests. Lootr is
  compatible: LootJS modifies the table, which Lootr then rolls per player.

### Perspective seeds (the deliberate contradictions)

- **Engineer** (incident report, black box): *"The chamber remained stable. External interference was not
  identified."* → "we succeeded / it wasn't our fault."
- **Mage** (arcane notes, memory fragments): *"The engineers believe they created the breach. They are mistaken."* →
  "we should not have tried."
- **Survivor** (data chip, photo, handbook): *"They cancelled transport again. Nobody is saying why."* → "we did not
  know."
- **Spirit** (bound thought): *"We were not summoned. We were already here."* → "you were warned."
- **Horror/Correction** (impossible bone, forbidden memory, correction fragment): *"No creature matches this
  anatomy."* / *"Something notices."* → "something is fixing the universe."

Five voices, no narrator. The player reconciles them.

---

## 5. Per-item notes

- **3 Damaged Photograph:** a normal item with a randomised tooltip ("Three people visible." → later finds: "Four
  shadows detected."). Not a Camerapture picture: those need a runtime-uploaded image bound in NBT, which can't be
  prebaked into loot. Camerapture stays the player's own tool.
- **4 Old Currency:** loot everywhere with the fatalistic tooltip; also a reward (8) in the Recovered Evidence chapter.
- **7 Audio Log:** right-click plays `minecraft:entity.elder_guardian.curse` and prints a transcript line ("Day 42…",
  "Day 61…", "Day 90…").
- **16–17 Corrupted Map:** four fragments combine (shapeless) into `corrupted_map`, a lore item whose tooltip
  describes impossible geography ("A continent no survey records.", "A city beneath the ocean.").
- **18–21 Horror residues** (`PACK_DIRECTION_REVIEW_2026-07-08.md` #7). Each marquee horror leaves a unique residue instead of generic
  loot:
  - on death: 90% chance, summoned as an item at the death location by `EntityEvents.death`, because these mobs have
    no JSON loot tables;
  - on survival (`oc_encounters.js`): an encounter starts when one of the four comes within 24 blocks of a survival
    player. It pays out if the horror then disappears without being killed and the player hasn't died meanwhile. The
    player gets the residue and a one-line message. Open encounters are kept in memory only, so a restart forgets
    them.
  - The common swarm mobs stay rare evidence so horror is not a farm: Born in Chaos and Foes sample chances were
    lowered (0.05 → 0.02 and similar) when the residues were added.
  - `fog_residue` moved from The Man From The Fog to CANTBREATHE on 2026-11-06 (CONFIG_CHANGES "2026-11-06 — Server
    tick audit: phases 0 and 1") and was renamed Breathless Residue on 2026-11-08.
- **29–30 Unstable Memory / Signal Device:** the only genuinely stateful items, both driven by `oc_discoveries`
  (thresholds in §3).
- **28 HS-LGTFO Mission Data** (`PACK_DIRECTION_REVIEW_2026-07-08.md` #6, labelled #4 in the `oc_interactions.js` comment): crafted from
  the four flight recorders, the colony manifest and the navigation fragment. Flavour only: rockets are not gated, so
  the sandbox is preserved. "The Answer" asks for it.
- **31 Correction Fragment:** `oc_interactions.js` gives it once, when `oc_flag_tech`, `oc_flag_magic` and
  `oc_flag_space` are all set and `oc_discoveries` is at least 12, with the line *"Something settles into your pack.
  You did not pick it up."* It is the physical payoff of "The Answer".
- **AE2 lore tooltips** (`PACK_DIRECTION_REVIEW_2026-07-08.md` #2): `ae2:controller` "A familiar architecture. The original creator is
  unknown.", `ae2:inscriber` "The pattern was already here. We only pressed it again.", `ae2:drive` "Recovered storage
  protocol. It remembers formats we never wrote." (reconstructing, not inventing).
- **Climate lines** (`PACK_DIRECTION_REVIEW_2026-07-08.md` #5): written for Cold Sweat and folded into the `personal_data_chip` ("The
  winters were never this harsh.") and `incident_report` ("Atmospheric readings continue to change.") text pools.
  Cold Sweat was removed in 2026-07; Serene Seasons is the climate layer since 2026-11-01.
- **Archive terminal** (`PACK_DIRECTION_REVIEW_2026-07-08.md` #3): the `archive` program ships as a datapack ROM file through
  `kubejs/data/computercraft/lua/rom/`, so it is on **every** computer (ROM merge). It runs a boot and integrity
  sequence, then a browsable menu of recovered records (personnel, research, incident, evacuation), each showing
  randomised, corrupted, never-complete fragments in the five voices. The **anomalous** record is encrypted and unlocks
  only while recovered media (any floppy) sits in an attached disk drive: the "recovered media detected" mechanic,
  without hard gating. `autorun/oc_archive.lua` prints a one-line boot hint ("type: archive"); `help/archive.txt` is
  the `help archive` entry. The "The Machines That Think" quest points players to it.

---

## 6. FTB Quests integration

Quest text refers to fragments narratively. A small number of endgame, optional-feeling tasks require the real items,
so the sandbox doesn't turn into a fetch quest. Every task checks something real; none is a checkmark (`ftbquest.md`).

| Quest (chapter) | ID | Task |
|---|---|---|
| **The Missing Years** (Recovered Data) | `7A04000000000104` | Item: 3× `incident_report`. Hidden until "Automation" is done |
| **Unidentified Contact** (Field Reports) | `7A05000000000105` | Stage `oc_q_contact`: a `biological_sample` **or** an `impossible_bone` in `oc_seen`. Fits "Confirmed." |
| **The Departure Records** (The Final Question) | `7A07000000000101` | Items: the **4 black-box fragments** (`flight_recorder_launch/transit/arrival/final`), the literal "reassemble the departure records". Comes after "Following Their Path", because the recorders are space loot |
| **The Answer** (The Final Question) | `7A07000000000104` | Stages `oc_q_evidence_tech`, `oc_q_evidence_magic`, `oc_q_evidence_space`, `oc_q_discoveries_12` (the grant conditions), and items `hs_lgtfo_mission_data` and `correction_fragment`. The fragment exists only after the convergence, so the gate is honest and endgame-only |
| **It Wears Faces / The One Below / The Breathless / It Was Not There Before** (Field Reports, optional) | `7A05000000000107`–`10A` | Items: the four residues |

**Recovered Evidence** (`recovered_evidence.snbt`, `order_index` 5; The Second Civilisation, The Final Question and
When Dungeons Arise follow at 6, 7 and 8) is a purely **optional, observation-style catalogue**. It lists the lore
items as `item` tasks that consume nothing (`default_consume_items: false`), so a player can see how many fragments
they have catalogued. It **only lists and rewards**, never explains, matching "collect dozens, realise later". Quests:
Recovered Evidence (the data chip), Personal Effects, Technical Records, Magical Records, Horror Evidence, Exodus
Records, Anomalous Objects, The Whole Picture (the Correction Fragment), Deeper Memories, The Map That Lies, Coping (the
two clarity consumables), and Four, Ten and Sixteen Entries (invisible until done; stages `oc_q_discoveries_4`, `_10`,
`_16`).

The atmospheric items (categories 1–3 of `implore.md`) stay **off** the main quest grid: they're world texture, found
by playing, not chased on a checklist.

---

## 7. Risks and limits

| Risk | Handling |
|---|---|
| `lootintegrations` overwrites or normalises tables the scripts modify | LootJS runs as a modifier layer on top. Specific tables or regex families are targeted rather than broad type modifiers. An in-game check of each target table is not recorded. |
| Loot spam breaks the "rare, meaningful" feel | Chances follow the spec (5–10% common, "very rare" about 1–2%). Each line's `randomChance` in `oc_loot.js` is the tuning point. |
| Tooltip flicker or desync | The variant is fixed in NBT on acquisition; the tooltip path never calls `Math.random()`. |
| Script errors silently disable the system | Every handler is wrapped in try/catch and logs an `[OldCiv]` line. Rhino `var` rule in §3. |
| Over-explaining via a guidebook | Patchouli and Modonomicon are out of scope; the Recovered Evidence chapter is index-only. |

---

## Not implemented

- Authored item art (the models reuse vanilla and Create item art) and authored `.ogg` audio logs (`sounds.json`).
- Transcripts printed by a CC:Tweaked disk.
- A right-click `/locate` on the Corrupted Map pointing to a real rare modded structure.
- An Old Currency sink: a KubeJS trade or recipe for MineColonies-era players, or a bridge to ycurrenci.
- A Patchouli or Modonomicon evidence index.
- Mob-drop injection across all 11 horror mods. Built: the Born in Chaos and Foes loot tables, plus residues from four
  marquee horrors.
- MineColonies town-hall milestones (`PACK_DIRECTION_REVIEW_2026-07-08.md` #4, a poor hook) and custom structures (`PACK_DIRECTION_REVIEW_2026-07-08.md` #9).

---

## Implementation

The whole design was built in one pass (all phases), with quest wiring, the Recovered Evidence chapter and
placeholder-first art. The same July build added about 24 quests, staged early → mid → late, across The Broken World,
The Machines That Remain, The Other Science, Recovered Data and Field Reports (listed per chapter in `ftbquest.md`).
Changes since then are in `docs/CONFIG_CHANGES.md`:

- July build validation: SNBT balanced, no duplicate quest IDs, dangling dependencies or stray characters, 205 modded
  IDs resolved; all JS files passed `node --check`; item IDs consistent across scripts and quests; the Lua
  balance-checked.
- "KubeJS — Old Civilisation script bugfix (`oc_interactions.js`)": the Rhino `const`/`let` errors (167 in the log)
  had stopped the whole discovery, stateful-item, endgame-grant and residue system; fixed and verified 2026-07-09.
- "KubeJS — Old Civilisation loot coverage (`oc_loot.js`)": routing for When Dungeons Arise, YUNG's structures, More
  Underground Structures and Lost Architects (2026-07-09).
- "2026-11-06 — Fog Residue: drop moved from The Man From The Fog to CANTBREATHE".
- "2026-11-08 — Quest book: real checks instead of click-to-complete" and "2026-11-08 — Quest book: fixes and
  additions": stage tasks, `oc_encounters.js`, the flight-recorder loot moved to placed moon tables, The Final Question
  reordered, the Breathless Residue rename, and the evidence milestones. The quest book has 9 chapters and 133 quests
  (VM log, 2026-11-08).
