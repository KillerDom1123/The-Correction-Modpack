Old Civilisation Lore Implementation Specification
KubeJS + Existing Mod Integration
Purpose

This document defines the storytelling layer to be added to the modpack.

The goal is not to create a linear adventure map. The world remains a sandbox.

The player should slowly uncover:

The existence of The Old Civilisation
The existence of The Correction
The HS-LGTFO Mission
The relationship between technology, magic, and the horrors
The truth that the disaster was not caused by one faction, but by an attempt to understand something beyond reality

The story should be conveyed through:

items
environmental discoveries
existing structures
machine progression
exploration
rare encounters

The player should never receive a complete explanation.

Every discovery should answer one question while creating two more.

The biggest danger with lore items is that players find one and immediately understand the plot. You want a world where players collect dozens of fragments and only later realise they form a picture.

Three categories:

1. **Common atmospheric evidence** (players constantly encounter these)
2. **Faction/perspective records** (different groups interpreted the event differently)
3. **Anomalous objects** (things that imply the universe itself is wrong)

The important rule:

> No single item explains the story. A player should need 5-10 discoveries before they understand a concept.

---

# CATEGORY 1 — Everyday Old Civilisation Artefacts

These make the world feel inhabited before the collapse.

---

# 1. Personal Data Chip

## ID

```
oldcivilisation:personal_data_chip
```

## Purpose

Small personal storage devices.

Unlike Archive Disks, these are **ordinary people's memories**.

---

## Found In

Common loot:

* villages
* abandoned structures
* dungeons
* mansions

Chance:

5-10%

---

## Appearance

Small translucent chip.

---

## Contents

Randomised personal messages:

Example:

> "Reminder: pick up supplies after work."

Then:

> "They cancelled transport again today."

Then:

> "Nobody is saying why."

---

## Lore Function

Shows:

The apocalypse happened slowly.

People were living normal lives.

---

# 2. Old Civilisation ID Badge

## ID

```
oldcivilisation:id_badge
```

---

## Found In

* strongholds
* dungeons
* industrial loot

---

## Names

Random:

* Research Division
* Infrastructure Division
* Energy Division
* Atmospheric Studies

---

## Tooltip

> "Identity verified: deceased."

---

## Lore

Small human details.

Example:

```
Name:
[REDACTED]

Department:
Reality Stabilisation Research
```

---

# 3. Damaged Photograph

Since you have Camerapture, this fits perfectly.

## ID

```
oldcivilisation:damaged_photo
```

---

## Found In

* villages
* abandoned homes
* ruins

---

## Contents

Images:

* families
* buildings
* landscapes

But occasionally:

Something impossible.

---

Example:

Normal photo:

> A group standing outside a facility.

Description:

> "Three people visible."

Later:

> "Four shadows detected."

---

# CATEGORY 2 — Scientific Records

These expand the engineering side.

---

# 4. Incident Report Fragment

## ID

```
oldcivilisation:incident_report
```

---

## Found In

Industrial areas.

---

## Text Examples

Fragment 1:

> "The chamber remained stable."

Fragment 2:

> "The chamber remained stable after external interference."

Fragment 3:

> "External interference was not identified."

---

## Purpose

Slowly introduces:

The experiment was not the cause.

---

# 5. Research Audio Log

## ID

```
oldcivilisation:audio_log
```

---

## Obtained

Rare loot.

---

## Implementation

Could be:

* renamed Goat Horn
* music disc style item
* CC:Tweaked playback

---

## Contents

Audio recordings.

Example:

> "Day 42. We have confirmed the signal is responding."

---

## Why Audio Works

Text feels like a book.

Audio feels like a discovered recording.

Much more horror-oriented.

---

# 6. Containment Label

## ID

```
oldcivilisation:containment_label
```

---

## Found

Very rare.

---

## Appearance

Small warning sticker.

---

## Text:

Examples:

```
CONTAINMENT AREA 04

DO NOT OPEN.

DO NOT OBSERVE.

DO NOT COMMUNICATE.
```

---

## Purpose

The player asks:

"Containment of what?"

---

# CATEGORY 3 — Magic Artefacts

Technology records events.

Magic remembers them.

---

# 7. Memory Fragment Variants

Do not have one Memory Fragment.

Have multiple.

---

## Types:

### Faded Memory

Common.

Shows normal life.

---

### Distorted Memory

Rare.

Shows altered reality.

---

### Forbidden Memory

Very rare.

Shows The Correction.

---

IDs:

```
memory_fragment_faded
memory_fragment_distorted
memory_fragment_forbidden
```

---

## Example:

### Faded

> "A classroom."

> "A lecture about magic."

---

### Distorted

> "The same classroom."

> "Everyone is facing the wrong direction."

---

### Forbidden

> "The sky opens."

> "Something notices."

---

# 8. Arcane Research Notes

## ID

```
oldcivilisation:arcane_notes
```

---

## Found Through

* Ars Nouveau
* Hexerei
* Occultism

---

## Perspective

Magic users.

---

Example:

> "The engineers believe they created the breach."

> "They are mistaken."

---

# 9. Bound Thought Fragment

Occultism-specific.

## ID

```
oldcivilisation:bound_thought
```

---

## Obtained

Rare occultism interactions.

---

## Text:

> "We were not summoned."

> "We were already here."

---

# CATEGORY 4 — Horror Evidence

These should be unsettling.

---

# 10. Biological Sample

## ID

```
oldcivilisation:biological_sample
```

---

## Drops From

* Born in Chaos
* The Pale Hound (if installed)
* horror mobs

---

## Tooltip

> "Cellular structure constantly changes."

---

## Purpose

Suggests the creatures are adapting.

---

# 11. Impossible Bone Fragment

## ID

```
oldcivilisation:impossible_bone
```

---

## Obtained

Rare monster drops.

---

## Tooltip

> "No creature matches this anatomy."

---

## Purpose

Suggests:

The horrors are not native.

---

# 12. Corrupted Map Fragment

## ID

```
oldcivilisation:corrupted_map_fragment
```

---

## Found

Exploration.

---

## Effect

When combined:

Creates a map.

But:

The geography is wrong.

---

Example:

Map shows:

* a continent that does not exist
* a city beneath the ocean
* structures underground

---

# CATEGORY 5 — Exodus / Space

---

# 13. Colony Manifest

## ID

```
oldcivilisation:colony_manifest
```

---

## Found

Ad Astra.

---

## Text:

```
COLONY SHIP:

SUCCESSFULLY DEPLOYED.

COLONY STATUS:

UNKNOWN.
```

---

## Purpose

Shows:

Escape was attempted elsewhere.

---

# 14. Navigation Fragment

## ID

```
oldcivilisation:navigation_fragment
```

---

## Found:

Moon/Mars.

---

## Text:

```
DESTINATION:

HABITABLE WORLD.

UPDATE:

SIGNAL DETECTED THERE.
```

---

Implication:

The Correction followed.

---

# 15. HS-LGTFO Black Box Variants

Do not have one flight recorder.

Have fragments.

---

## Flight Recorder Fragment A

Shows launch.

---

## Fragment B

Shows travel.

---

## Fragment C

Shows arrival.

---

## Fragment D

Shows:

Something was already there.

---

IDs:

```
flight_recorder_launch
flight_recorder_transit
flight_recorder_arrival
flight_recorder_final
```

---

# CATEGORY 6 — Items That Change Over Time

These are extremely powerful.

---

# 16. Unstable Memory Crystal

## ID

```
oldcivilisation:unstable_memory
```

---

## Mechanic

The tooltip changes after discoveries.

Early:

> "A damaged magical crystal."

Later:

> "It remembers you."

---

This creates paranoia.

---

# 17. Unknown Signal Device

## ID

```
oldcivilisation:signal_device
```

---

## Found:

Rare.

---

## Use:

Right-click occasionally:

```
SIGNAL DETECTED.

SOURCE:
UNKNOWN.
```

---

Later:

```
SIGNAL SOURCE:
LOCAL.
```

---

# CATEGORY 7 — Joke / Immersion Items

Small things matter.

---

# 18. Old Currency

## ID

```
oldcivilisation:old_currency
```

---

Found:

everywhere.

---

Tooltip:

> "Value meaningless. History priceless."

---

Useful for:

MineColonies trading.

---

# 19. Employee Handbook

## ID

```
oldcivilisation:employee_handbook
```

---

Found:

offices/libraries.

---

Starts mundane:

> "Safety procedures."

Ends disturbing:

> "If you hear your name from an empty room, do not respond."

---

# 20. Final Item: The Correction Fragment

Do not add this until endgame.

## ID

```
oldcivilisation:correction_fragment
```

---

Obtained:

Only after:

* space
* magic
* engineering

---

Tooltip:

> "A piece of something that removed everything else."

---

# Recommended Total Item Pool

I would aim for:

| Category           | Number |
| ------------------ | -----: |
| Personal artefacts |      5 |
| Technical records  |      8 |
| Magical records    |      6 |
| Horror evidence    |      6 |
| Exodus records     |      6 |
| Anomalous objects  |      5 |

Around **30 items total**.

Not all need complex mechanics.

Many can simply be:

* loot additions
* Patchouli entries
* CC archive text files
* randomised descriptions

---

The biggest improvement over the first design is adding **perspective**.

The player should find:

* an engineer saying "we succeeded"
* a mage saying "we should not have tried"
* a survivor saying "we did not know"
* a spirit saying "you were warned"
* a horror fragment implying "something noticed"

Then the player builds the truth themselves.
