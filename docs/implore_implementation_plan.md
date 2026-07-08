# Old Civilisation — Story Item Implementation Plan
### KubeJS 6 + LootJS + FTB Quests integration for the modpack pack

> Companion to `implore.md` (item spec), `thelore.md` (hidden truth), and `ftbquest.md` (quest-book design).

## ✅ BUILD STATUS — implemented (all phases, first pass)

Decisions taken: **full build**, **quest wiring + collection chapter**, **placeholder-first textures**, namespace **`oldcivilisation:`** (KubeJS 6 custom-namespace confirmed via `RegistryInfo`).

Files created:
- `kubejs/startup_scripts/oc_items.js` — 26 items registered.
- `kubejs/client_scripts/oc_tooltips.js` — NBT-variant + staged dynamic tooltips (5 voices).
- `kubejs/server_scripts/oc_loot.js` — LootJS injection (vanilla + Ad Astra More Structures + born_in_chaos/foes mob regex).
- `kubejs/server_scripts/oc_interactions.js` — variant stamping, discovery counter, staged anomalies, signal/audio right-click, endgame Correction Fragment grant, corrupted-map recipe.
- `kubejs/assets/oldcivilisation/textures/item/*.png` — 26 category-coded placeholders.
- FTB Quests: wired 4 endgame tasks (Missing Years → incident reports; Unidentified Contact → biological sample; Departure Records → 4 black-box fragments; The Answer → Correction Fragment) + new optional chapter `recovered_evidence.snbt` (order_index 5; later chapters bumped to 6/7/8).

Validated statically: SNBT balanced, 440 unique quest IDs, no dangling deps; all 4 JS files pass `node --check`; item IDs consistent across scripts + quests.

**Must be verified in-game (I could not run the client). Watch `logs/kubejs/`:**
1. Custom namespace registration — confirm the 26 items exist (`/give @s oldcivilisation:personal_data_chip`).
2. `player.inventory` `getItem/setItem` scan + `.nbt =` assignment (variant stamping / staged tooltips).
3. LootJS regex table modifier for `born_in_chaos_v1` / `foes` entities (wrapped in try/catch; falls back gracefully).
4. `.rarity('...')` / `.glow(true)` builder methods on this KubeJS build.

### Follow-up pass (configcheck.md refinements + quest expansion)

**4 refinements implemented:**
- **#7 horror = evidence:** born_in_chaos/foes sample rates lowered (0.05→0.02 etc.); added 4 marquee-horror **residue items** (`distorted_replica`, `deep_echo`, `fog_residue`, `temporal_residue`) that drop near-guaranteed from the named horrors via `EntityEvents.death` (they have no JSON loot tables). Now 31 KubeJS items.
- **#2 AE2 lore tooltips:** `ae2:controller` / `inscriber` / `drive` get "reconstructing, not inventing" tooltips.
- **#5 Cold Sweat lore:** climate variants folded into `personal_data_chip` + `incident_report` text pools.
- **#4 HS-LGTFO Mission Data:** craftable from the 4 flight recorders + colony manifest + navigation fragment (flavour; does **not** gate rockets — sandbox preserved).

**Quest trees fleshed out (~24 new quests, staged early→mid→late):**
- Broken World +3 survival (Cold Sweat warmth, FarmersDelight cooking, first shelter).
- Machines That Remain +7 (Create brass/precision → Create Additions electricity; IE multiblocks → Immersive Petroleum; PneumaticCraft; Mekanism factory → teleport/QIO).
- The Other Science +6 (Ars automation; Iron's Spellbooks; Occultism labour → familiars; Hexerei; Forbidden Arcanus forge).
- Recovered Data +4 (AE2 crafting CPU → 16k/P2P → quantum/spatial; CC:Tweaked computers).
- Field Reports +4 optional evidence-recovery quests wired to the new residues.

Validated: 9 chapters, 585 unique quest IDs, no dups/dangling deps, no stray chars; all 4 JS pass `node --check`; 205 modded IDs all resolve.

**CC:Tweaked Archive Terminal (#3) — implemented.** Ships as a datapack ROM program via `kubejs/data/computercraft/lua/rom/`:
- `programs/archive.lua` — the `archive` program: boot/integrity sequence, then a browsable menu of recovered records (personnel / research / incident / evacuation), each showing randomised, corrupted, never-complete fragments in the five voices. The **anomalous** record is encrypted and unlocks only when recovered media (any floppy) sits in an attached disk drive — the "recovered media detected" mechanic, without hard-gating.
- `autorun/oc_archive.lua` — subtle one-line boot hint ("type: archive").
- `help/archive.txt` — `help archive` entry.
- Available on **every** computer (ROM merge); the "Machines That Think" quest points players to it. Lua validated (keyword/paren/brace balance) — verify live in-world.

Still not done (deliberate): MineColonies town-hall milestones (#4 configcheck — poor hook), custom structures (#9), authored art/`.ogg`.

---


---

## 0. Guiding constraints (re-confirmed from the source docs)

Everything below is bound by rules that are already written down. Restating them because they drive every technical choice:

| Constraint | Source | How this plan honours it |
|---|---|---|
| "No single item explains the story. A player should need **5–10 discoveries** before they understand a concept." | implore.md | Every concept is split across many low-information fragments + randomised text pools. No item is a summary. |
| "Every discovery should answer one question while creating two more." | implore.md | Tooltip-writing rubric (§6). Text always withholds. |
| "The player should **never receive a complete explanation**." | implore.md | No Patchouli "here's the plot" book. Guidebooks (if any) only *index* finds, never interpret them. |
| Add **perspective** — engineer / mage / survivor / spirit / horror each contradict each other. | implore.md | Every item is tagged to a faction voice (§4 column "Voice"). Contradiction is the point. |
| Quest book is a **modern researcher cataloguing**, not a narrator. | ftbquest.md + your mid-task note | **Tonal split (below).** Items are raw primary sources; quest text stays the present-day annotator. |
| The world stays a **sandbox**; not a linear adventure map. | implore.md | Lore items are loot/drops/encounters. Quest hooks are **optional & reward-only**, never hard gates for core play. |
| The Correction Fragment "do not add until endgame… only after space + magic + engineering." | implore.md | Gated on the convergence already built in `second_civilisation.snbt` / `the_final_question.snbt`. |

### The tonal split (critical — keep these two voices distinct)
- **Story items = raw voices.** First-person, in-world, contradictory, emotional, unlabelled. An ID badge, a panicked data chip, a spirit's taunt. The Old Civilisation and its horrors speaking *directly*.
- **FTB quest text = the modern cataloguer.** Detached, present-day, "recovered document, cause unknown." Already written this way in the seven chapters.

The player is the researcher. The **items are the evidence**; the **quest book is their lab notebook**. Do not let items adopt the cataloguer's detached voice, and do not let the quest book start explaining. This separation is what makes the mystery feel assembled-by-the-player rather than handed over.

---

## 1. Tooling map (what does what)

| Tool | Installed | Role in this feature |
|---|---|---|
| **KubeJS 6** (`kubejs-forge-2001.6.5`) | ✅ | Register the ~30 items (`startup_scripts`), dynamic/NBT tooltips (`client_scripts`), right-click behaviour & state escalation (`server_scripts`). |
| **LootJS** (`lootjs-forge-2.13.1`) | ✅ | Inject items into vanilla + modded loot tables and mob drops. `LootJS.modifiers(...)` with per-drop `modifyLoot` callbacks (confirmed in jar) → lets us roll a random text variant at drop time. |
| **FTB Quests** (`ftb-quests-2001.4.22`) | ✅ | Optional collection quests; wire fragments into the existing endgame chapters. |
| **lootintegrations** (+ WDA / T&T / BornInChaos / integrated addons) | ✅ | Already normalises structure loot; our LootJS modifiers stack on top. Note: test interaction, don't fight it. |
| **Camerapture** | ✅ | *Not* used for the Damaged Photo (see §5, item 3 — pictures need runtime-uploaded images). Kept as flavour reference only. |
| **CC:Tweaked** | ✅ | Optional advanced route for Audio Logs / archive terminals (§7 stretch). |
| **Patchouli / Modonomicon** | ✅ | Optional "evidence index" only — must not explain. Default plan: **skip**. |
| **Curios, ycurrenci, MineColonies** | ✅ | `old_currency` can be made a MineColonies-tradeable / ycurrenci-adjacent trinket (§5 item 18). |

### KubeJS folder layout we will use
```
kubejs/
  startup_scripts/
    oc_items.js            # register all items + properties
  server_scripts/
    oc_loot.js             # LootJS loot/mob injection
    oc_interactions.js     # right-click, state escalation, discovery counter
  client_scripts/
    oc_tooltips.js         # dynamic NBT-driven tooltips + text pools
  assets/oldcivilisation/
    lang/en_us.json        # display names
    textures/item/*.png    # 16x16 art (or placeholders)
    models/item/*.json     # auto-generated if omitted (item/generated)
```

---

## 2. Key technical decisions (recommendations)

1. **Namespace.** KubeJS 6 accepts a custom namespace in `event.create('oldcivilisation:...')`. **Recommendation:** use `oldcivilisation:` to match `implore.md` verbatim. *Fallback if a build rejects it:* `kubejs:oc_<name>`. → **One 2-minute in-game test decides this before we write 30 IDs.**
2. **Randomised text = NBT index + text pool.** One item, many messages. Store `{ oc_variant: <int> }` on the stack; the tooltip script picks `POOL[id][variant]`. Randomisation happens **once, at drop time** via LootJS `modifyLoot` (so a given item never flickers). This is how "Personal Data Chip", "Damaged Photo", "Incident Report", "Arcane Notes", etc. get variety without 200 item registrations.
3. **Multi-item concepts stay multi-item** where the spec says so: Memory Fragments (faded/distorted/forbidden = 3 items) and Black Box (launch/transit/arrival/final = 4 items). Rarity escalates per variant.
4. **Textures.** Start with a small set of shared placeholder textures grouped by "form" (chip, badge, paper, crystal, bone, disc, currency). Missing textures still function (purple/black), so art can lag behind logic. Custom art is a polish pass, not a blocker.
5. **State-changing items** (Unstable Memory Crystal, Signal Device) read a per-player **discovery counter** in `player.persistentData.oc_discoveries`; server-side code stamps the item's NBT stage when thresholds are crossed, so the **client tooltip only reads NBT** (no custom networking needed).
6. **Audio Logs**: default = right-click item that plays an **existing** eerie sound event via `level.playSound` + shows a transcript tooltip. Custom `.ogg` is an optional upgrade (needs `sounds.json` asset work). Avoids a custom sound-registration pipeline on day one.
7. **No hard gating.** Only the *endgame* Correction Fragment and the already-terminal Final chapter may depend on items. Everything else is discoverable and ignorable.

---

## 3. Core code patterns (illustrative — confirm method names on first run)

**Item registration** (`startup_scripts/oc_items.js`):
```js
StartupEvents.registry('item', e => {
  e.create('oldcivilisation:personal_data_chip')
    .displayName('Personal Data Chip')
    .maxStackSize(16).rarity('uncommon').glow(false)
  // ... ~30 items, some with .rarity('rare'/'epic'), .glow(true) for anomalies
})
```

**Dynamic tooltip from NBT** (`client_scripts/oc_tooltips.js`):
```js
const POOLS = {
  'oldcivilisation:personal_data_chip': [
    ['§7"Reminder: pick up supplies after work."'],
    ['§7"They cancelled transport again today."'],
    ['§7"Nobody is saying why."'],
  ],
  // ...
}
ItemEvents.tooltip(t => {
  t.addAdvanced('oldcivilisation:personal_data_chip', (stack, adv, text) => {
    const pool = POOLS[stack.id]; if (!pool) return
    const v = (stack.nbt?.oc_variant ?? 0) % pool.length
    pool[v].forEach(line => text.add(1, Text.of(line)))
  })
})
```

**Loot injection + per-drop random variant** (`server_scripts/oc_loot.js`):
```js
LootJS.modifiers(ev => {
  ev.addTableModifier('minecraft:chests/village/village_house')   // + abandoned/dungeon tables
    .randomChance(0.08)
    .addLoot('oldcivilisation:personal_data_chip')
    .modifyLoot('oldcivilisation:personal_data_chip', (stack, ctx) =>
      stack.withNBT({ oc_variant: Math.floor(Math.random() * 3) }))
})
```

**Mob drop** (horror evidence):
```js
LootJS.modifiers(ev => {
  ev.addEntityModifier('born_in_chaos_v1:*')   // + themimic_er, cave_dweller, foes, etc.
    .randomChance(0.05)
    .addLoot('oldcivilisation:biological_sample')
})
```

**Right-click signal / escalation** (`server_scripts/oc_interactions.js`):
```js
ItemEvents.rightClicked('oldcivilisation:signal_device', e => {
  const n = e.player.persistentData.oc_discoveries | 0
  e.player.tell(n < 10 ? 'SIGNAL DETECTED. SOURCE: UNKNOWN.' : 'SIGNAL SOURCE: LOCAL.')
})
```

---

## 4. The full item catalogue (~30 stacks) — voice, source, mechanic

`Voice` = which faction's perspective (drives contradiction). `Mech` = complexity: **L** loot-only, **T** randomised tooltip, **S** stateful/scripted.

| # | ID (`oldcivilisation:`) | Cat | Voice | Where it comes from | Mech |
|--:|---|---|---|---|:--:|
| 1 | `personal_data_chip` | Personal | Survivor | Villages, abandoned structures, dungeons, mansions (~5–10%) | T |
| 2 | `id_badge` | Personal | Survivor/Eng | Strongholds, dungeons, "industrial" loot | T |
| 3 | `damaged_photo` | Personal | Survivor | Villages, abandoned homes, ruins | T |
| 4 | `old_currency` | Joke/Immersion | Survivor | Everywhere, common; MineColonies/ycurrenci tie-in | L |
| 5 | `employee_handbook` | Joke→Horror | Survivor→? | Offices/libraries (village/stronghold libraries) | T |
| 6 | `incident_report` | Technical | Engineer | Industrial: mineshaft, IE/other tech structures, dungeons | T |
| 7 | `audio_log` | Technical | Engineer | Rare loot | S |
| 8 | `containment_label` | Technical | Engineer | Very rare, deep/industrial | T |
| 9 | `memory_fragment_faded` | Magic | Mage | Ars/Hexerei/Occultism structures & drops (common) | T |
| 10 | `memory_fragment_distorted` | Magic | Mage | as above (rare) | T |
| 11 | `memory_fragment_forbidden` | Magic | Mage/Correction | as above (very rare) | T |
| 12 | `arcane_notes` | Magic | Mage | Magic structures / ritual by-products | T |
| 13 | `bound_thought` | Magic | Spirit | Rare Occultism interactions (ritual output / demon drops) | T |
| 14 | `biological_sample` | Horror | Horror | Born in Chaos, Mimic, Foes, horror mobs | L |
| 15 | `impossible_bone` | Horror | Horror | Rare monster drops | L |
| 16 | `corrupted_map_fragment` | Horror/Anomaly | Correction | Exploration loot; combine → `corrupted_map` | S |
| 17 | `colony_manifest` | Exodus | Engineer | Ad Astra structures | T |
| 18 | `navigation_fragment` | Exodus | Engineer | Moon / Mars loot (dimension-conditioned) | T |
| 19 | `flight_recorder_launch` | Exodus | Engineer | Ad Astra / orbit loot | T |
| 20 | `flight_recorder_transit` | Exodus | Engineer | Moon loot | T |
| 21 | `flight_recorder_arrival` | Exodus | Engineer | Mars loot | T |
| 22 | `flight_recorder_final` | Exodus | Engineer→Horror | Rare, off-world | T |
| 23 | `unstable_memory` | Anomaly | Correction | Rare magic/deep loot; tooltip escalates with discoveries | S |
| 24 | `signal_device` | Anomaly | Correction | Rare; right-click escalating messages | S |
| 25 | `correction_fragment` | Final | Correction | **Endgame only** — after space+magic+engineering | S |
| 26–30 | Variant/atmos filler to reach the spec's per-category counts | mixed | mixed | spread across the above tables to hit implore.md's table (Personal 5 / Technical 8 / Magical 6 / Horror 6 / Exodus 6 / Anomalous 5) | T |

> The spec's target table sums to ~36 "slots" but many are **text variants of the same item** (that's the intent: dozens of finds, one slowly-forming picture). We hit the counts primarily through the NBT text pools, registering ~20–25 distinct items and letting variants do the rest.

### Perspective seeds (the deliberate contradictions)
- **Engineer** (incident report, black box): *"The chamber remained stable. External interference was not identified."* → "we succeeded / it wasn't our fault."
- **Mage** (arcane notes, memory fragments): *"The engineers believe they created the breach. They are mistaken."* → "we should not have tried."
- **Survivor** (data chip, photo, handbook): *"They cancelled transport again. Nobody is saying why."* → "we did not know."
- **Spirit** (bound thought): *"We were not summoned. We were already here."* → "you were warned."
- **Horror/Correction** (impossible bone, forbidden memory, correction fragment): *"No creature matches this anatomy."* / *"Something notices."* → "something is fixing the universe."

Five voices, no narrator. The player reconciles them.

---

## 5. Notable per-item implementation notes

- **3 Damaged Photograph** — implement as a normal item with a randomised tooltip ("Three people visible." → later finds: "Four shadows detected."). *Not* a Camerapture picture: those require a runtime-uploaded image bound in NBT, which we can't prebake into loot. Camerapture stays the player's own tool.
- **7 Audio Log** — default: right-click plays an existing unsettling sound (e.g. an elder-guardian/whisper/cave ambience) + transcript tooltip. Optional upgrade: bundle `.ogg` + `sounds.json` for authored recordings; or a CC:Tweaked disk that prints transcript to a terminal (stretch).
- **16 Corrupted Map Fragment** — fragments combine (shapeless craft) into `corrupted_map` (lore item whose tooltip describes impossible geography: "a continent that does not exist", "a city beneath the ocean"). *Optional* stretch: right-click issues a `/locate` to a real rare modded structure so the "wrong map" points somewhere real-but-strange.
- **18 Old Currency** — cosmetic + soft economy hook. Simplest: just loot everywhere with the fatalistic tooltip. Optional: a KubeJS trade/recipe so MineColonies-era players can sink them, or bridge to `ycurrenci`.
- **23 Unstable Memory / 24 Signal Device** — the only genuinely stateful items. Drive both off `player.persistentData.oc_discoveries`. Ship them in a **later phase** so the discovery counter is battle-tested first.
- **25 Correction Fragment** — obtainable only when the player has cleared the space/magic/engineering convergence (see §6). It is the physical payoff of `The Answer`.

---

## 6. FTB Quests integration (wiring evidence into the seven chapters)

The quests currently reference "fragments" **narratively** and use `checkmark`/dependency placeholders. Now that fragments are real items, we upgrade **a small number of endgame, optional-feeling** tasks to real item collection — without turning the sandbox into a fetch quest.

Concrete, low-risk wiring (quest IDs from the existing chapters):

| Quest (chapter) | ID | Change |
|---|---|---|
| **The Missing Years** (Recovered Data) | `7A04000000000104` | Add optional `item` task: collect e.g. 3× `incident_report`. Keep the checkmark too (either/OR via `dependency`/task tuning). |
| **Unidentified Contact** (Field Reports) | `7A05000000000105` | Task becomes "possess a `biological_sample` **or** `impossible_bone`." Fits "Confirmed." perfectly. |
| **The Departure Records** (Final Question) | `7A07000000000101` | Replace checkmark with `item` tasks for the **4 black-box fragments** (`flight_recorder_launch/transit/arrival/final`). This is the ideal literal realisation of "reassemble the departure records." |
| **The Answer** (Final Question) | `7A07000000000104` | Task = submit/own `correction_fragment`. The fragment only exists post-convergence, so the gate is honest and endgame-only. |

Optional new chapter — **"Recovered Evidence"** (`order_index` after Field Reports): a purely **optional, observation-style checklist** that lists the lore items as `item` tasks with 0-consume, so a player can see *how many* fragments they've catalogued. It must **only list and reward**, never explain — matching "collect dozens, realise later." This gives collectors a home without violating the no-explanation rule.

Everything else (Category 1–3 atmospheric items) stays **off** the quest grid — they're world texture, found by playing, not chased on a checklist.

---

## 7. Phased rollout

Each phase is independently shippable and testable. Recommended order:

- **Phase 0 — Foundation (½ day).** Namespace test; register 3 pilot items (data chip, id badge, incident report); build the NBT-variant tooltip framework + one text pool; one LootJS injection into `chests/simple_dungeon`. *Exit test:* find a chip in a dungeon, see a random message, no flicker.
- **Phase 1 — Personal artefacts (Cat 1).** Items 1–5. Village/abandoned/mansion loot. Pure L/T. Highest "world feels inhabited" payoff, lowest risk.
- **Phase 2 — Technical records (Cat 2).** Items 6–8. Industrial/mineshaft/stronghold loot. Audio log v1 (existing sound). Introduces the *engineer* voice.
- **Phase 3 — Magic records (Cat 3).** Items 9–13, incl. 3 memory variants. Loot from Ars/Hexerei/Occultism structures + ritual/demon drops. Introduces *mage* + *spirit* voices and the first "reality is wrong" hints.
- **Phase 4 — Horror evidence (Cat 4).** Items 14–16. Mob-drop injection across the 11 horror mods (namespaces already identified). Corrupted map combine recipe. Introduces *horror* voice.
- **Phase 5 — Exodus/space (Cat 5).** Items 17–22. Ad Astra structure + Moon/Mars dimension-conditioned loot. Sets up the black-box fragments for the quest wiring.
- **Phase 6 — Anomalous / stateful (Cat 6).** Items 23–24 + the `oc_discoveries` counter. First scripted, synced-via-NBT behaviour. Ship after the counter is proven.
- **Phase 7 — Endgame + quest wiring.** Correction Fragment; upgrade the 4 quest tasks (§6); optional "Recovered Evidence" chapter.
- **Phase 8 — Polish.** Custom textures, authored `.ogg` audio logs, balance loot chances against `lootintegrations`, optional CC:Tweaked archive terminal, optional `/locate` corrupted map.

---

## 8. Risks & mitigations

| Risk | Mitigation |
|---|---|
| Custom namespace rejected by this KubeJS build | 2-min Phase-0 test; fallback to `kubejs:oc_*`. |
| `lootintegrations` overwrites/normalises tables we modify | LootJS runs as a modifier layer; test each target table in-game; prefer `addTableModifier` on specific tables over broad type modifiers where overlap is likely. |
| Loot spam (too many lore items) breaks the "rare, meaningful" feel | Central `CHANCES` table in `oc_loot.js`; tune globally; obey spec percentages (5–10% common, "very rare" ≈ 1–2%). Log-and-review, no silent inflation. |
| Dynamic tooltip flicker / desync | Variant fixed in NBT at drop; never `Math.random()` in the tooltip path. |
| Stateful items (23–25) over-scoped early | Deferred to Phase 6–7; counter proven first. |
| Audio/custom sound pipeline cost | v1 reuses existing sounds; authored `.ogg` is Phase 8 polish. |
| Over-explaining via a guidebook | Patchouli/Modonomicon intentionally **out** of default scope; "Recovered Evidence" chapter is index-only. |

---

## 9. Open decisions (need your call before Phase 0)

1. **Namespace:** `oldcivilisation:` (matches spec, pending the quick support test) vs guaranteed `kubejs:oc_*`. — *Rec: try `oldcivilisation:` first.*
2. **Scope of first build:** ship **Phase 0 pilot** now, or land **Phases 0–1** (all personal artefacts) as the first deliverable? — *Rec: 0–1 together; it's the biggest atmosphere win.*
3. **Quest wiring depth:** just the 4 endgame task upgrades (§6), or also add the optional **"Recovered Evidence"** collection chapter? — *Rec: both, chapter last.*
4. **Textures:** placeholder-first (I generate simple 16×16 art), or hold logic until you supply/commission art? — *Rec: placeholder-first so gameplay isn't blocked.*
5. **Audio logs:** existing-sound v1 acceptable, or is authored `.ogg` a must-have for the horror feel? — *Rec: v1 now, authored later.*
