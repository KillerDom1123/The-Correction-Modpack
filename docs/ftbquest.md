# Quest book design

> **Status:** Implemented in `config/ftbquests/quests/chapters/`: 9 chapters and 133 quests, including the
> `wda_questline` mod's When Dungeons Arise chapter. Checks: `kubejs/server_scripts/oc_quest_checks.js`,
> `kubejs/startup_scripts/oc_quest_spellcast.js`, `kubejs/server_scripts/oc_encounters.js` and the OpenLoader pack
> `QuestChecks`. Live on the VM since the 2026-11-08 01:14 restart (log: *Loaded 9 chapters, 133 quests*).
> **Related:** `docs/implore_implementation_plan.md` (story-item wiring and the Recovered Evidence chapter),
> `docs/thelore.md`, `docs/CONFIG_CHANGES.md` ("2026-11-08 — Quest book: real checks instead of click-to-complete",
> "2026-11-08 — Quest book: fixes and additions")
> **Not tested in game:** anything that needs a player, such as the survive-an-encounter payout and the quest screens.
> The `oc_encounters.js` fix (`getUuid()`) was uploaded to the VM on 2026-11-08 and takes effect at the next restart
> or `/reload`. One mirrored tag, `oc_q_evidence_magic`, was granted live at 01:24 on 2026-11-08.

The quest book is **not a progression guide**.

It is an **investigation framework**.

A player can ignore half of it and still play. The quests exist to:

1. Give players direction when they ask "what should I do next?"
2. Teach the major mods naturally.
3. Create reasons to explore.
4. Slowly build the mystery.
5. Make every playstyle contribute to the final discovery.

The chapters below are therefore **parallel investigations**, not a campaign order.

| Order | Chapter | File |
|--:|---|---|
| 0 | The Broken World | `broken_world.snbt` |
| 1 | The Machines That Remain | `machines_that_remain.snbt` |
| 2 | The Other Science | `the_other_science.snbt` |
| 3 | Recovered Data | `recovered_data.snbt` |
| 4 | Field Reports | `field_reports.snbt` |
| 5 | Recovered Evidence (optional) | `recovered_evidence.snbt` |
| 6 | The Second Civilisation | `second_civilisation.snbt` |
| 7 | The Final Question | `the_final_question.snbt` |
| 8 | When Dungeons Arise | `when_dungeons_arise.snbt` |

---

## How the quest book checks progress (2026-11-08)

Every quest checks something real; none can be completed by clicking a checkmark. The requirements below are the
built checks. The task-by-task record is in `CONFIG_CHANGES.md` ("2026-11-08 — Quest book: real checks instead of
click-to-complete").

- **Built-in tasks:** items, advancements, dimensions, biomes, structures and (two optional reports) kills. Biome and
  structure tasks also use tags from the OpenLoader pack `QuestChecks`: `#oldcivilisation:abandoned_places`,
  `#oldcivilisation:creeping_woods`, `#oldcivilisation:dense_forests` and `#oldcivilisation:nether_strongholds`.
- **Script-checked tasks:** anything FTB Quests can't express (counting nights, "any one of" several mods, casting a
  spell, sanity, evidence counts) is a *stage* task. Without a stage mod, FTB Quests reads stages from vanilla player
  tags, so `kubejs/server_scripts/oc_quest_checks.js` and `kubejs/startup_scripts/oc_quest_spellcast.js` add an
  `oc_q_*` tag when the player has done it.
- **Residues without a kill:** the residue quests in Field Reports also pay out when a player lives through an
  encounter (`kubejs/server_scripts/oc_encounters.js`). Observation, not killing, stays the way through that chapter.
- **Item tasks consume nothing** (`default_consume_items: false`).
- A **whole night** means seen at or before 14000 and at or after 22000 (night is 13000–23000), alive, and in the
  Overworld throughout. Players get a short line after each counted night ("Night 2 of 5…").

---

# QUESTBOOK ROOT

# "The Broken World"

**Mandatory opening chapter**

Available immediately.

Purpose:

* Establish tone.
* Encourage leaving spawn.
* Introduce the four major philosophies.

---

## Quest: "A World Left Behind"

### Type

Structure

### Requirements

```pseudo
Stand inside any structure in #oldcivilisation:abandoned_places
(179 ruins, dungeons and abandoned buildings from 21 mods and vanilla)
```

The tag includes the design's suggested structures:

* Abandoned Watchtowers
* When Dungeons Arise
* YUNG structures
* Lost Architects

### Reward

* Explorer's Compass
* Nature's Compass
* A backpack and basic supplies (food, torches)

### Lore

> "The world is not destroyed. It is interrupted."

---

## Quest: "The First Survey"

### Type

Location counter

```pseudo
Discover:
5 biomes: forest, plains, river, mountain, taiga
3 structures: village, mineshaft, ruined portal
1 underground location: any #forge:is_underground biome
```

### Reward

* Travel supplies: warp scrolls, a sleeping bag, a map. No waystone: `WaystonesGate` puts waystones behind an Ender
  Eye, so the waystone comes with "Through the Portal".

### Lore

> "The further you travel, the more evidence you find that this was once a connected world."

---

## Quest: "The Silent Nights"

### Type

Hidden observation quest (appears after "The First Survey")

```pseudo
Stage oc_q_silent_nights:
- Spend 5 whole nights at least 256 blocks from spawn, not necessarily in a row
- Stay awake on each counted night
```

### Reward

A Familiar Photograph, Night Vision Goggles and food.

### Lore

> "Early explorers reported sounds and movements outside their camps."

> "Most assumed exhaustion."

---

This introduces the horror without naming it.

---

## Further quests in this chapter

| Quest | Checks |
|---|---|
| Sustenance | Farmer's Delight cooking pot and cutting board |
| The First Night | Stage `oc_q_first_night`: a whole night survived under a roof for at least 75% of it, or slept through indoors |
| Through the Portal | Enter the Nether, and stand inside `#oldcivilisation:nether_strongholds` (fortress, Better Fortresses' fortress, bastion, Incendium's Forbidden Castle and Sanctum). Gives the waystone |

---

# PATH ONE

# ENGINEERING

## Theme

*"If we understand what they built, we understand what happened."*

Mods:

* Create
* Create Additions
* Steam Rails
* Immersive Engineering
* Immersive Petroleum
* PneumaticCraft
* Mekanism
* Extreme Reactors
* SecurityCraft (in "The Second Civilisation")

---

# Chapter: "The Machines That Remain"

---

## Quest: "Simple Machines"

### Purpose

Teach Create basics.

### Requirements

```pseudo
Craft:
- Water Wheel
- Mechanical Press
- Encased Fan
- Mechanical Mixer
```

### Reward

Create components.

### Lore

> "The earliest machines recovered were not weapons."

> "They were tools."

---

## Quest: "A Moving World"

### Requirements

Create:

```pseudo
- Mechanical Belt
- Andesite Funnel
- Chute
- Mechanical Arm
```

### Reward

Brass casings and backpack upgrades (pickup, magnet).

### Lore

> "Automation was everywhere."

> "The Old Civilisation did not work harder."

> "It made the world work for it."

---

## Quest: "Industrial Foundations"

### Requirements

Immersive Engineering:

```pseudo
- Coke Brick (Coke Oven)
- Blast Brick (Blast Furnace)
- Kinetic Dynamo (generator)
- LV Capacitor
```

### Reward

Engineer's manual, hammer and steel.

### Lore

> "Industrial facilities remain scattered across the world."

> "Almost all were abandoned simultaneously."

---

## Quest: "The Rail Network"

### Requirements

Create:

```pseudo
- Railway Casing (train)
- Track
- Train Station
```

### Reward

Travel supplies.

### Lore

> "The world was connected once."

> "Something made connection unnecessary."

---

## Quest: "The Power Question"

### Requirements

Any one (stage `oc_q_power`):

```pseudo
Create power: a water wheel, windmill or steam engine advancement (each needs real torque)
OR
Immersive Engineering power: windmill, kinetic dynamo or diesel generator
OR
PneumaticCraft pressure: air or liquid compressor
```

### Purpose

No mod is forced.

---

### Lore

> "The Old Civilisation mastered energy."

> "The question is why they needed so much."

---

## Quest: "A Familiar Design"

### Requirements

Mekanism:

```pseudo
- Enrichment Chamber
- Metallurgic Infuser
- Crusher
```

The Digital Miner is endgame under `MekanismProgression`, so it sits in "Distant Hands".

### Reward

Advanced materials.

### Lore

> "Several abandoned facilities contain machines almost identical to these."

---

## Quest: "The Unacceptable Solution"

### Requirements

No task of its own. It completes when any one of three sub-quests does:

```pseudo
The Split Atom: Mekanism fission reactor port
OR
A Star in a Box: Mekanism fusion reactor controller
OR
The Reinforced Core: Extreme Reactors reinforced reactor controller
```

### Reward

Nothing significant.

### Lore

> "Records repeatedly mention a final energy project."

> "The details are missing."

---

This is deliberately ominous.

---

## Further quests in this chapter

| Quest | Checks |
|---|---|
| Brass and Precision | Create crushing wheel, mechanical crafter, deployer, brass ingot |
| Captured Lightning | Create Additions electric motor, alternator, accumulator |
| Refined Extraction | Immersive Engineering metal press, crusher, squeezer |
| The Oil Beneath | Immersive Petroleum pumpjack, distillation tower |
| Pressurised | PneumaticCraft air compressor, pressure tube, plastic, pressure gauge |
| Factory Line | Mekanism steel casing, basic energy cube, basic universal cable, basic smelting factory |
| Distant Hands | Mekanism teleporter, QIO dashboard, gas-burning generator, Digital Miner |
| The Split Atom, A Star in a Box, The Reinforced Core | See "The Unacceptable Solution" |
| The Yellow Ore | Extreme Reactors Yellorium ingot, basic reactor controller, basic turbine controller |

---

# PATH TWO

# ARCANE STUDIES

## Theme

*"Perhaps the machines were never the cause."*

Mods:

* Ars Nouveau
* Hexerei
* Occultism
* Forbidden Arcanus
* Iron's Spells
* Blood Magic
* Mahou Tsukai

---

# Chapter: "The Other Science"

---

## Quest: "Old Symbols"

### Requirements

```pseudo
Craft:
- Source Jar
- Novice Spell Book
- Imbuement Chamber
```

### Reward

Spell resources.

### Lore

> "The Old Civilisation had multiple methods of discovery."

---

## Quest: "A Different Kind of Power"

### Requirements

```pseudo
Generate Source: imbue a Source Gem
Cast a spell of your own from a spell book (stage oc_q_cast_spell, from Ars Nouveau's SpellCastEvent)
Create magical tools: Enchanting Apparatus
```

### Reward

Source gems and Iron's Spells starter items.

### Lore

> "Some researchers believed reality was not broken."

> "Only misunderstood."

---

## Quest: "The Invisible Workforce"

### Requirements

Occultism ritual tools:

```pseudo
- Golden Sacrificial Bowl
- White Chalk
```

### Reward

Occultism resources.

### Lore

> "The spirits answered."

> "The records do not explain why."

---

## Quest: "The Old Practices"

### Requirements

Hexerei:

```pseudo
- Mixing Cauldron (brew)
- Book of Shadows Altar
- Book of Shadows
```

### Lore

> "Some knowledge survived because people were afraid to forget it."

---

## Quest: "Forbidden"

### Requirements

Forbidden Arcanus:

```pseudo
Create advanced materials: Arcane Crystal
```

### Lore

> "Some discoveries were abandoned before the world ended."

---

## Further quests in this chapter

| Quest | Checks |
|---|---|
| The Living Workshop | Ars Nouveau ritual brazier, drygmy charm, agronomic sourcelink |
| Words of Power | Iron's Spells inscription table, scroll forge, scroll |
| Demon Labour | Occultism storage controller, satchel, empty magic lamp |
| The Bound Servant | Occultism familiar ring, dimensional matrix, stable wormhole |
| The Broom and the Cauldron | Hexerei willow broom, herb jar, candle dipper |
| The Forge Unbidden | Forbidden Arcanus Hephaestus Forge, clibano core |
| The Price → What the Blood Bought → The Great Work | Blood Magic: dagger, altar, weak orb; soul snare, Hellfire Forge; master ritual stone, apprentice orb |
| Circles on the Floor → The Lesser Grail | Mahou Tsukai: mortar and pestle plus advancement `mahoutsukai:root` (first circle); `mahoutsukai:lesser_grail` |

---

# PATH THREE

# THE ARCHIVE

## Theme

*"The truth exists. Someone removed it."*

Mods:

* Applied Energistics 2
* Applied Mekanistics
* CC:Tweaked

---

# Chapter:

"Recovered Data"

---

## Quest:

"Storage Is Not Enough"

### Requirements

AE2:

```pseudo
- Inscriber
- ME Controller
- Terminal
```

### Reward

Storage cells.

### Lore

> "The old systems were designed to preserve information."

---

## Quest:

"Networks"

### Requirements

```pseudo
Create:
- Wireless terminal
- Import bus
- Export bus
- Drive
```

### Lore

> "This was not storage."

> "This was memory."

---

## Quest:

"Automation"

### Requirements

```pseudo
AE2:
- Pattern Encoding Terminal
- Molecular Assembler
- Pattern Provider (autocrafting)
```

### Reward

An Incident Report Fragment (archive fragment).

---

## Quest:

"The Missing Years"

### Hidden

Requirements:

```pseudo
AE2 advanced system: appears after "Automation"
+
exploration discoveries: 3 Incident Report Fragments
```

### Lore

> "The final records were not lost."

> "They were deliberately removed."

---

## Further quests in this chapter

| Quest | Checks |
|---|---|
| Crafting on Demand | AE2 crafting unit, 1k crafting storage, crafting accelerator, storage bus |
| One Mind, Many Hands | AE2 16k item storage cell, ME P2P tunnel, level emitter |
| Beyond the Network | AE2 quantum ring, quantum link, spatial IO port |
| The Machines That Think | CC:Tweaked computer, disk drive, monitor, wireless modem. Its text points to the `archive` program |
| Chemical Memory | Applied Mekanistics chemical storage cell 1k, chemical P2P tunnel |

---

# PATH FOUR

# HORROR INVESTIGATION

## Theme

*"Something survived."*

This chapter is the most important.

---

No kill quests on the main line. (Two optional side reports, the Wendigo and the Nightmare Stalker, are kills.)

No mob lists.

No "defeat the monster."

---

# Chapter:

"Field Reports"

---

## Quest:

"Into The Dark"

### Requirements

```pseudo
Visit:
- Deep caves: any #forge:is_underground biome
- Ancient structures: #oldcivilisation:abandoned_places
- Dense forests: #oldcivilisation:dense_forests (dark forest, old-growth taiga, jungle)
```

### Reward

Camera / exploration supplies.

---

## Quest:

"Observation"

### Requirements

Camerapture:

```pseudo
Hold 3 photographs (Camerapture pictures)
```

---

### Lore

> "The Old Civilisation documented everything."

> "Perhaps they were trying to understand."

---

## Quest:

"The Forest Does Not Forget"

### Requirements

```pseudo
Visit:
Creeping Woods: any of its 24 sites (#oldcivilisation:creeping_woods)
```

### Lore

> "Several expedition notes describe the same phenomenon."

> "The environment seemed aware."

---

## Quest:

"Below"

### Requirements

```pseudo
Explore:
an Alex's Caves biome
the Deep Dark
```

### Lore

> "The oldest warnings concern what exists beneath the surface."

---

## Quest:

"Unidentified Contact"

### Hidden

Appears after "Below".

Requirements (stage `oc_q_contact`):

```pseudo
Hold:
- a Biological Sample
OR
- an Impossible Bone Fragment
```

The four encounters (CANTBREATHE, the Mimicer, the Cave Dweller, a Weeping Angel) each have an optional residue report
below.

### Reward

A Containment Label (archive fragment).

### Lore

Only:

> "Confirmed."

---

No explanation.

---

## Quest:

"The Watchers"

### Requirements

Survive (stage `oc_q_watchers`):

```pseudo
10 whole nights away from home:
256+ blocks from spawn and from your bed, outside any MineColonies colony
```

The pack has no chunk-claim mod, so distance from spawn, bed and colonies stands in for "away from claimed chunks".
The quest text reads "Nowhere near a bed, a colony…".

### Lore

> "Something has noticed the return of civilisation."

---

## Residue reports (optional)

Objective text: "Survive an encounter, or end one." A kill drops the residue (90%); so does surviving the encounter
(`oc_encounters.js`).

| Quest | Item | Horror |
|---|---|---|
| It Wears Faces | Distorted Replica | The Mimicer |
| The One Below | Deep Echo | The Cave Dweller |
| The Breathless | Breathless Residue | CANTBREATHE |
| It Was Not There Before | Temporal Residue | Weeping Angels |

## Further quests in this chapter

| Quest | Checks |
|---|---|
| Below the Bedrock | Deeper and Darker: enter the Otherside; find an Ancient Temple |
| Fixation (optional) | Advancement `the_obsessed:first_impressions` |
| The Figure (optional) | Creeping Woods advancement `demons_of_the_forest` (encounter the Figure) |
| Hunger (optional) | Kill a Wendigo |
| Day Three (optional) | Born in Chaos advancement `fighting_nightmares` (kill the Nightmare Stalker) |
| Breakdown (optional, invisible until done) | Stage `oc_q_breakdown`: sanity reaches Breakdown. Gives 4 Sedative Ampoules |

---

# OPTIONAL CHAPTER

# "Recovered Evidence"

An index of the story items, one catalogue entry per category, plus evidence milestones at 4, 10 and 16 kinds that
stay invisible until done. It lists and rewards, never explains. Design and quest list:
`implore_implementation_plan.md` §6.

---

# SHARED CHAPTER

# "The Second Civilisation"

Unlocked when players complete several paths.

Not gated.

Just appears: "People Return" and "Secure The Future" stay hidden until the opening quests of two of the four paths
are done.

---

Theme:

The player has recreated what came before.

---

## Quest:

"People Return"

### Requirements

MineColonies:

```pseudo
Population:
25 (advancement minecolonies:minecolonies/colony_population_25)
```

MineColonies has population milestones at 10 and 25, none at 20.

---

## Quest:

"Secure The Future"

### Requirements

SecurityCraft:

```pseudo
- Security Camera
- Keypad Door
- Sentry
```

---

## Quest:

"Knowledge, Power, Shelter"

Requirements:

No task of its own. It completes once its six dependencies do: "People Return", "Secure The Future", and one quest
from each path:

```pseudo
Technology:
Advanced machine: "A Familiar Design"

Knowledge:
AE2 system: "Storage Is Not Enough"

Understanding:
Magic system: "A Different Kind of Power"

Observation:
Horror discovery: "Unidentified Contact"
```

---

## Further quests in this chapter

| Quest | Checks |
|---|---|
| The Centrepiece | MineColonies: Town Hall level 5 |
| The Enchanter's Tower | MineColonies: build an Enchanter's Tower |
| A Village Again | MineColonies: 50 citizens |
| Take Arms | MineColonies: build a Barracks |

---

# FINAL CHAPTER

# "The Final Question"

---

This chapter appears slowly.

Not all at once.

---

Order: The Skyward Project → Following Their Path → The Departure Records → The Answer. The flight recorders are
found in space, so the records come after the flights.

## Quest:

"The Skyward Project"

Requirements:

Ad Astra:

```pseudo
Tier 1 Rocket
Space Suit
Fuel
```

It depends on "The Missing Years", "Unidentified Contact", and the quests behind each NASA Workbench part
(`AdAstraSpaceGate`): "Brass and Precision", "A Familiar Design", "Storage Is Not Enough" and "The Enchanter's Tower"
(teleport scroll). Its text names the four parts.

---

## Quest:

"Following Their Path"

Requirements:

Visit:

```pseudo
Moon
Mars
Earth Orbit (space stations)
```

---

## Quest:

"The Departure Records"

Requirements:

```pseudo
Recover:
the four flight recorder fragments: Launch, Transit, Arrival, Final
```

---

Lore:

> "They did not fight."

> "They prepared."

---

## Quest:

"The Answer"

Final hidden objective. It lists its conditions:

```pseudo
Recover a technical record, a magical record and an exodus record
Hold 12 different kinds of recovered evidence
Assemble the HS-LGTFO Mission Data (the HS-LGTFO remains)
Hold the Correction Fragment
```

The evidence conditions are the conditions of the Correction Fragment grant (`oc_interactions.js`).

## Optional endings (after "The Answer")

| Quest | Checks |
|---|---|
| Follow Them | Reach Glacio |
| Launch Again | A Tier 4 rocket |
| Stay | 75 citizens |

---

# Final Lore

The player discovers:

The mission succeeded.

The Old Civilisation escaped.

But the universe did not become safer.

---

The final message:

> "The Old Civilisation was not destroyed."

> "It was corrected."

---

# End State

The player has:

* rebuilt civilisation
* mastered machines
* recovered knowledge
* explored beyond the world

And discovered:

They followed the exact same path.

The final question is not:

> "Can humanity escape?"

It is:

> "If humanity escapes, what follows?"

---

# "When Dungeons Arise"

The `wda_questline` mod's own chapter, plus one pack quest: "Find a Jungle Tree House" (structure
`dungeons_arise:jungle_tree_house`; WDA has no advancement for it).

---

# Not implemented

- "The Silent Nights": the "sleep in fewer than half of the nights" rule. The built check counts only nights spent
  awake.
- "Observation": what the photographs show. The task counts three Camerapture pictures.
- "Below": Cave Dweller locations.
- Activity checks that became item checks: Basic Glyphs ("Old Symbols", which asks for an Imbuement Chamber instead),
  summoning a first spirit ("The Invisible Workforce", which asks for the ritual tools), using Hexerei rituals ("The
  Old Practices").
