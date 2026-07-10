# The Sanity Mechanic — Design & Technical Specification

> Companion to `thelore.md` (the hidden truth), `implore.md` / `implore_implementation_plan.md` (the Old Civilisation item system), and `CONFIG_CHANGES.md` (the balance record).
> Status: **design spec, pre-implementation.** This document defines *what* the feature is and *why* every choice was made, at enough technical depth to implement directly against KubeJS 6 (`kubejs-forge-2001.6.5`) on Forge 1.20.1. Anything marked **[verify live]** is an API accessor to confirm in-game on first run — the same caution the item-system plan uses.

---

## 1. What this is (in one paragraph)

**Sanity** is a hidden, per-player resource that rises and falls with how safe the player's surroundings are. It is never shown on screen. The player only ever perceives it through *consequences* that grow as it falls. Its entire job is to make **the world feel like it is reacting to the player's exposure to it**, which is the exact premise of the pack's lore: reality is damaged, the horrors are symptoms, and staring into the dark for too long makes the damage notice you.

Sanity is a **suppression field, not a threat generator.** It has no failure state (you cannot "die of insanity"), and it *spawns nothing* — it only ever makes the world **quieter and safer as it rises**. Full sanity actively holds the horror *back*; losing sanity lets the horror the pack already ships come *forward*, up to — never past — its authored base rate. It drives two channels (detailed in §8):

- **Channel A — ambient events.** Sounds, whispers, brief hidden screen effects, hallucinated cues. Zero at full sanity; more frequent and intense as it drops.
- **Channel B — natural-spawn suppression.** At high sanity, natural horror-mob spawns are cancelled before they happen; as sanity falls, that suppression lifts until, at zero, the pack's tuned base spawn rates pass through untouched.

The mental model for the player: **full sanity = the world leaves you alone; empty sanity = the world is exactly as dangerous as it was built to be.** Sanity can only ever *reduce* danger below the authored baseline — it can never manufacture more.

---

## 2. Why the pack wants this (design fit)

Four established pillars of this pack all point at the same mechanic, and all four constrain how it must be built:

| Pillar | Source | Consequence for sanity |
|---|---|---|
| **Reality is wrong; horrors are symptoms, not enemies.** | `thelore.md` ("The Observers", "The Correction", *"why are they looking at us?"*) | Sanity is the *player's* coupling to that damaged reality. Low sanity = reality misbehaving *around* the player. This is the mechanical expression of the whole story. |
| **Rare > frequent. Quiet dread, not jump-scares.** | `CONFIG_CHANGES.md` (Serverside Horror tuning, horror-spawn rarity, Man From The Fog) | Sanity is **suppression-only**: it may reduce horror *below* the authored base rate but never raise it *above*. The tuned base rate is a hard ceiling reached only at zero sanity, so the mechanic can never undo the pack's rarity work — at worst it does nothing, at best it makes the world safer. It never rewrites another mod's config (§11). |
| **Minimal HUD.** | `CONFIG_CHANGES.md` (TFC health/hunger/thirst bars reverted to vanilla; thirst removed) | **No bar, no number.** A "SANITY 62%" readout would be a foreign video-game UI element in a pack that deliberately stripped its HUD to vanilla. Sanity is *invisible* and diegetic. |
| **The server thread is already under pressure.** | `CONFIG_CHANGES.md` (worldgen cascade, `Bruh [-4,66]` entity pile, Whispering Spirits per-tick cost, two anti-lag scripts) | The mechanic must be **cheap** and must **never spawn entities**. Its consequences are sounds/effects/particles only. This is not a nice-to-have; it is why the "atmosphere-only" consequence model was chosen. |

The mechanic was deliberately scoped to the **safest, most on-theme** point in the design space: **invisible state, atmosphere-only consequences, multiple soft drains, multiple recoveries.** Every other option (visible bar, real-threat escalation, driving the other mods) was considered and rejected for one of the reasons above.

---

## 3. The state model

### 3.1 Storage

A single per-player double, `oc_sanity`, held in `player.persistentData` (the Forge/KubeJS entity persistent-data compound — the *same* store `oc_interactions.js` already uses for `oc_discoveries`).

- **Range:** `0.0 … 100.0`. Clamped on every write.
- **Baseline:** `100.0` (fully sane at world entry).
- **Initialisation:** `persistentData.getDouble('oc_sanity')` returns `0.0` when unset, which is indistinguishable from "genuinely at zero." We therefore gate init on a boolean flag:
  ```
  if (!pd.getBoolean('oc_sanity_init')) {
      pd.putDouble('oc_sanity', 100.0)
      pd.putBoolean('oc_sanity_init', true)
  }
  ```
  This is the standard idiom for "a persisted numeric with a nonzero default."

### 3.2 Persistence semantics

- **Across logout / world reload:** `persistentData` is serialised with the player, so sanity survives naturally. Desired.
- **Across death:** Forge's copy-on-respawn behaviour for entity persistent data is version- and configuration-sensitive **[verify live]**. Rather than depend on it, we make a *design* choice: **respawn resets sanity toward baseline** (a clean slate — "near-death clarity"). Concretely, `PlayerEvents.respawned` sets `oc_sanity` to `100.0` (or a chosen "post-death" value). This makes the behaviour deterministic regardless of how Forge treats the tag, and it is thematically clean.

### 3.3 Who is exempt

Skipped entirely each tick (no drain, no events): `player.isCreative()`, `player.isSpectator()`. Creative/spectator players are not "in" the survival fiction. This also keeps the builder/admin experience clean.

---

## 4. The tick loop and its performance budget

### 4.1 One loop, heavily throttled

All logic lives in a **single** `PlayerEvents.tick` handler, gated to run once every **40 ticks (2 s)**:

```
PlayerEvents.tick(event => {
    var player = event.player
    if (!player || player.age % 40 !== 0) return
    // … all sanity logic here …
})
```

**Why 40 ticks:** sanity is a slow-moving atmospheric quantity, not a combat stat. Sampling twice a second is imperceptible to the player and cuts the per-tick cost by 40×. The pack has three cautionary tales about per-tick work (Whispering Spirits' every-tick look scan was the **#1 mod cost** in the Spark profile); this design refuses to add a fourth.

**Why one handler, not one per concern:** `oc_interactions.js` and `anti_lag_mobs.js` already each register a `PlayerEvents.tick`. Event-handler dispatch and the per-player guard cost is paid per registered handler. Sanity adds exactly **one** more, and keeps all its per-interval work behind a single throttle. (A later cleanup could consolidate all three into one loop; out of scope for this feature.)

### 4.2 Per-interval cost (the actual budget)

Once every 2 s, per online player, the loop does at most:

1. **One light read** — `player.block.getLight()` **[verify live]** (a single array lookup in the light engine).
2. **One depth read** — `player.y` (a field access).
3. **One sky/day check** — `player.block.canSeeSky()` + `player.level.isDay()` **[verify live]**.
4. **One nearby-horror count** — a single `execute if entity @e[type=#oldcivilisation:horror_mobs,distance=..16]` command (see §6.3). One bounded selector evaluation, not an every-tick scan.
5. **Occasionally**, when an event fires: a handful of `playsound` / `effect` commands targeting `@s`.

For a typical small server this is a rounding error next to entity ticking, chunk I/O, and worldgen — the things the profiles actually flagged. Crucially, **nothing here scales with world entity count** (unlike the Whispering Spirits procedure, which processed every spirit entity every tick). It scales only with *online player count*.

### 4.3 The Rhino trap (mandatory idiom)

KubeJS runs on Rhino (`rhino-forge-2001.2.3`), which **mis-hoists `let`/`const` declared inside a `try` block in a repeatedly-invoked callback** — the bug that silently killed the entire Old Civilisation interaction system until it was found (`redeclaration of var` × 167 in the logs; see `CONFIG_CHANGES.md` → *KubeJS — Old Civilisation script bugfix*). **Every variable declared inside the tick/event callbacks in this script uses `var`.** Module-level `const` (the config tables) is fine — those load once.

---

## 5. Drains and restores (the numeric model)

All values below are **per 2 s interval** unless marked otherwise, and are **starting values** — they live in a single `CONFIG` object at the top of the script so the whole feel can be retuned in one place without touching logic. The intent is a slow curve measured in **minutes**, not seconds.

### 5.1 Drains

| Trigger | Δ per interval | Detection | Rationale |
|---|---:|---|---|
| **Darkness** — effective light `< 4` | **−0.6** | `player.block.getLight()` **[verify live]** | The player's stated core driver. Light 4 is the vanilla hostile-spawn threshold, so "dark enough for sanity loss" == "dark enough to be dangerous" — one intuition, two systems. |
| **Depth** — overworld only, `Y < 0`, scaling to `Y < −40` | **−0.2 → −0.6** | `player.y`, gated on `player.level.dimension` | The deep dark is where the pack's apex threats live (Cave Dweller at Y≤0, Deep Dark spawns). Depth pressure makes long sub-zero expeditions psychologically costly, not just mechanically dangerous. Overworld-only because "Y<0" is meaningless in the Nether/End. |
| **Horror proximity** — a horror mob within ~16 blocks | **−1.2** | `#oldcivilisation:horror_mobs` count (§6.3) | Being *near* one of the marquee horrors should spike dread. This is the spike term; it stacks on top of ambient drains. |
| **Self-inflicted feedback** — one of our own dread events just fired | **−2.0** (one-shot) | internal | A gentle positive-feedback loop: once you're in Dread, the events themselves push you a little deeper, so a bad situation *escalates* rather than plateauing — but slowly, and only while other drains persist. |

Drains **stack additively**. Worked example — spelunking in the pitch dark at Y −50 with a Cave Dweller nearby:
`−0.6 (dark) − 0.6 (deep) − 1.2 (proximity) = −2.4 / interval`. From full (100) to the Dread band (40) is 60 points ≈ **25 intervals ≈ 50 s** of sustained exposure. Ambient darkness alone (−0.6) reaches Dread in ≈ 200 s (~3.3 min). Aggressive but deliberate; tune `CONFIG.dark` upward toward 0 for a gentler pack.

### 5.2 Restores

| Condition | Δ per interval | Detection | Rationale |
|---|---:|---|---|
| **Firelight** — effective light `≥ 10` | **+0.5** | `player.block.getLight()` | Lighting your surroundings is the primary, always-available defence. Symmetry with the darkness drain makes "keep the torches up" a real psychological ritual, not just a spawn-proofing chore. |
| **Daylight** — daytime *and* sky access | **+0.8** | `isDay()` + `canSeeSky()` | Daytime above ground is the safe state. The two conditions together prevent "daytime in a cave" from healing. |
| **Sleep** — currently in bed | **+8.0** | `player.isSleeping()` **[verify live]** | Rest is refuge. Rapid recovery (full in ~2 in-game "sleep intervals") makes a bed the reset button — reinforcing base-building and the day/night rhythm the 30-min day already establishes. |
| **Clarity consumable** | **+30 (flat, on use)** | `ItemEvents` (§8) | An active, lootable/craftable answer to a bad night — and a lore hook (see §8). |

Restores are only applied when the corresponding drain is **absent** (you can't be "in darkness" and "in firelight" simultaneously; the light read resolves to one band). Daylight and firelight can co-apply.

### 5.3 Clamp and write

After summing the interval's deltas: `sanity = clamp(sanity + delta, 0, 100)`, written back with `pd.putDouble`. A no-op interval (Calm, no drains) still reads state but may skip the write if unchanged, to avoid needless NBT churn.

---

## 6. Detection details

### 6.1 Light

`player.block` returns the block-space the player occupies; `.getLight()` **[verify live]** yields the *effective* light level (max of sky and block light, as the spawn engine sees it). We want effective light because a torch-lit cave at night should read as "safe" and an unlit surface at night should read as "dark." If only `getSkyLight()`/`getBlockLight()` are exposed on this build, effective light is `max(skyLight − darkenAmount, blockLight)`; the fallback is documented in the script.

### 6.2 Depth and dimension

`player.y` for altitude; `player.level.dimension` (or `player.level.dimensionKey`) **[verify live]** to restrict the depth term to `minecraft:overworld`. Nether/End get no depth drain (their geometry makes Y meaningless), but they *do* still get darkness and proximity drains — the End especially should feel unsafe.

### 6.3 Nearby horrors — the entity-type tag

Rather than iterate the entity list every interval (the expensive pattern), we define a **datapack entity-type tag** and let the command engine do the bounded query:

`config/openloader/data/<Pack>/data/oldcivilisation/tags/entity_types/horror_mobs.json`

seeded with the same marquee set the residue system already recognises, plus the other tuned horrors:

```
themimic_er:mimicer, cave_dweller:cave_dweller, man:manfromthefog, man:managgresive,
weeping_angels:weeping_angel, + the HorrorSpawnRarity set (rake, TOWW, wendigo,
whispering_spirit, pale_hound) as desired
```

Detection is then a single command per interval:
`execute positioned as @s if entity @e[type=#oldcivilisation:horror_mobs,distance=..16]`

This mirrors the established `#antilag:cullable_hostiles` tag pattern (`anti_lag_mobs.js`) — one tag, one selector, cheap and centrally editable. Adding/removing a horror from the sanity system is a one-line tag edit, no code change.

---

## 7. The band model (why thresholds, not a linear effect)

Sanity is quantised into four **bands**. Events are driven by the band, not the raw number.

| Band | Range | What the player experiences |
|---|---|---|
| **Calm** | > 70 | Nothing. Silence is the reward. |
| **Unease** | 40 – 70 | Sparse, ambiguous cues: a distant cave ambience, a single `sculk_sensor` click. ~10 min between events. |
| **Dread** | 15 – 40 | Whispers, footsteps *behind* you, brief `nausea`, occasional `darkness` flash. ~5 min between events. |
| **Breakdown** | < 15 | Frequent whispers, `darkness` pulses, sustained `nausea`, hallucinated fake mob sounds. ~2 min between events. |

**Why banded:**
- **Qualitative, legible escalation.** A linear "event chance = f(sanity)" produces a mushy ramp the player can't read. Discrete bands give the dread a *shape* — the player can feel themselves cross from "something's off" to "I need to leave."
- **It's how the player *learns the system exists* without a bar** (see §9). Crossing a band boundary is the diegetic signal.
- **Tuning surface.** Each band has its own event pool and cooldown, so you can make Unease rarer or Breakdown nastier independently.

**Hysteresis (anti-flicker).** Band changes are asymmetric to stop oscillation at a boundary from spamming the threshold cue: you drop into a lower band the instant you cross its ceiling, but you must climb **5 points above** a threshold to be promoted back up. So the Dread/Unease boundary is `< 40` to enter Dread, `≥ 45` to leave it. This prevents a player hovering at 40 from triggering the "entering Dread" whisper every few seconds.

---

## 8. The two consequence channels

Sanity has exactly two outputs, both keyed off the band model (§7). Neither ever creates an entity: Channel A emits sounds/effects/particles, Channel B *removes* spawn attempts the game would otherwise have made.

### 8.1 Channel A — ambient events (atmosphere-only, by construction)

Each band above Calm owns a **weighted pool** of events. On each interval, if the player is in that band **and** past the band's per-player cooldown, roll the pool once and fire one event, then stamp `oc_sanity_last_event = now` and shorten nothing — the cooldown is the band's fixed minimum gap plus a random jitter so events never feel metronomic.

Every event is built from exactly three primitives, **none of which create entities**:

1. **Sound** — `player.runCommandSilent('playsound <id> <source> @s ...')`, optionally *positioned* behind the player. A footstep or whisper placed a few blocks behind the player's facing vector (`@s` position + offset derived from `player.yaw`) reads as "something is behind me" without anything being there. This is the same illusion Serverside Horror's `fake_steps` uses — we reproduce the *technique*, under our own control.
2. **Effect** — `effect give @s minecraft:nausea <secs> 0 true` / `minecraft:darkness <secs> 0 true`. The trailing `true` hides the potion particles/HUD icon, so the effect is felt (screen warp, vignette) but leaves **no HUD trace** — critical for the "invisible mechanic" rule.
3. **Particle** — `particle` at or around the player for the rare visual hallucination, kept subtle.

Illustrative pools (final text/sounds are a content pass):

- **Unease:** `ambient.cave` at low volume · single `block.sculk_sensor.clicking` · a lone `entity.item.pickup`-pitched blip with no item.
- **Dread:** whisper sound behind the player · 2–3 footstep sounds behind the player · `nausea` 3 s · one `darkness` 2 s flash.
- **Breakdown:** layered whispers · `darkness` pulse (5 s, repeating) · `nausea` 8 s · a hallucinated hostile sound (`entity.creeper.primed`, `entity.enderman.stare`, `entity.warden.nearby_closest`) with **no corresponding mob**.

Because the strongest primitive is a hidden `nausea`/`darkness` and a positional sound, **the worst case for the server is a few commands**, and the worst case for the *client* is a screen-warp shader that vanilla already ships. There is no path from "low sanity" to "entity spawn," so there is no path from this feature to the entity-pile lag class the pack has been fighting.

### 8.2 Channel B — natural-spawn suppression

This channel implements the core design intent: **high sanity makes the pack's real horror rarer; zero sanity lets it through at its authored base rate.** It does this by *cancelling* natural spawn attempts, never by creating them — so it can only ever subtract from what the game already decided to do.

**Mechanism.** A single `EntityEvents.checkSpawn` handler **[verify live]** (Forge's `MobSpawnEvent` / check-spawn path, which KubeJS exposes and which is cancelable). When a tagged horror attempts a *natural* spawn, we resolve the nearest player's sanity and cancel probabilistically:

```
EntityEvents.checkSpawn(event => {
  var e = event.entity
  if (!e || !isHorror(e.type)) return                 // #oldcivilisation:horror_mobs (§6.3)
  if (event.spawnType && event.spawnType != 'NATURAL') return  // [verify live] leave spawner/structure spawns alone
  var s = nearestPlayerSanity(event.level, e.x, e.y, e.z)      // default 0 (=allow) if no player near
  var cancelChance = clamp(Math.pow(s / 100, CONFIG.spawn.gamma), 0, 1)
  if (Math.random() < cancelChance) event.cancel()    // or event.setResult('deny') on this build
})
```

**The curve.** `cancelChance = (sanity/100)^gamma`.
- `sanity = 100` → cancelChance `1.0` → the spawn is (almost) always denied ⇒ **full sanity ≈ 0% horror**.
- `sanity = 0` → cancelChance `0.0` → never denied ⇒ **the tuned base rate passes through, untouched**.
- `gamma = 1.0` is linear (50 sanity → 50% suppression). `gamma < 1` front-loads suppression so you have to fall a long way before spawns really return (a more forgiving pack); `gamma > 1` means even mild sanity loss opens the gates fast. One knob, whole feel.

**Why cancel-only (never add).** `checkSpawn` can *veto* an attempt but cannot *manufacture* one. That limitation is the feature: base rate is a hard ceiling (§2), so this channel is structurally incapable of exceeding the pack's authored rarity. It rarifies; it never inflates.

**Which horrors this actually reaches — the two buckets (be honest about this):**

| Bucket | Spawn mechanism | Scalable by Channel B? | Members (per `CONFIG_CHANGES.md`) |
|---|---|:--:|---|
| **Natural / biome-weighted** | Vanilla/Forge spawn pipeline, biome spawn weight | **Yes** — routes through `checkSpawn` | The `HorrorSpawnRarity` set: **Rake, The One Who Watches, Wendigo, Whispering Spirits, Pale Hound**; general vanilla hostiles; **Weeping Angels** (biome spawn config — **[verify]** it routes through check-spawn) |
| **Timer / scheduler self-summon** | The mod's own internal tick timer reads its `*-server.toml` cadence and summons directly | **No** — bypasses `checkSpawn`; KubeJS cannot rewrite a mod's loaded config per-player at runtime | **The Mimic** (`can_spawn_min/max`), **Cave Dweller** (`can_spawn_min/max`), **The Man From The Fog** (`min/max_spawn_rate`) |

Bucket 2 keeps the static cadence already tuned in `CONFIG_CHANGES.md`; sanity cannot dynamically rarify it. Attempting to fight those schedulers by deleting freshly-summoned entities (`EntityEvents.spawned` post-removal) is possible but **rejected**: it flickers, many MCreator mobs summon outside the normal placement path so it wouldn't catch them anyway, and a scheduler that finds its spawn gone tends to just retry. Not worth the fragility.

**The coupling that covers the gap.** A Bucket-2 horror still craters sanity via the horror-proximity drain (§5.1) the moment it appears. That drop lifts Channel-B suppression on the Bucket-1 stalkers *and* opens Channel A. So the un-scalable timer-horror becomes the **trigger** for a wider bad night, and sanity handles the **aftermath** — the two buckets stay linked even though only one is directly spawn-scaled.

**Cost.** `checkSpawn` fires only on spawn *attempts*, which the pack already made rare. The per-attempt work is one tag test + one nearest-player lookup + one RNG roll — orders of magnitude cheaper than a per-tick scan, and it scales with spawn attempts (already throttled by the base rates), not with world entity count.

---

## 9. How the player discovers an invisible system

A fully silent hidden stat is a design risk: the player may never realise the world is responding to *them* rather than to random chance. The mechanic solves this **diegetically**, without ever admitting it's a stat:

- **Band-crossing lines.** When (and only when) the player crosses *down* into a new band, a single italic, dark-grey line prints in chat — the pack's established "raw voice" styling (`.darkGray().italic(true)`), the same look the Old Civilisation tooltips use. Examples: entering Dread → *"Your thoughts feel heavier down here."*; entering Breakdown → *"You can't tell if the sound is outside your head."* Hysteresis (§7) guarantees these fire at most once per genuine descent.
- **Correlation does the rest.** Because drains map onto *legible* causes (darkness, depth, proximity) and recoveries onto *legible* safety (light, day, bed, the clarity item), an attentive player infers the rule — "the dark is doing this to me" — without ever being told there's a number. That inference *is* the horror.

These lines are the only textual surface, they are content-tunable, and they can be disabled with a single `CONFIG` flag for a purist silent run.

---

## 10. Clarity consumables (the active recovery + lore hook)

One or two new items, registered through the **existing** Old Civilisation pipeline (`oc_items.js` for registration, `oc_tooltips.js` for the voiced tooltip, `oc_loot.js` for placement), so they arrive as *part of the story*, not a bolted-on potion:

- **Sedative Ampoule** *(Technical / Engineer voice)* — *"Field-issue. Dulls what the mind insists on noticing."* Flat +30 sanity on use; consumed.
- **Familiar Photograph** *(Personal / Survivor voice)* — *"A face you're sure you knew."* Smaller restore, reusable with a cooldown — comfort, not a cure.

Recovery is applied in `ItemEvents.rightClicked` / food-use by writing `oc_sanity` directly (same persistentData write path as the tick loop). Because they route through LootJS, their rarity is tuned centrally alongside every other lore item, so "how often can I find relief" is a balance knob, not a constant.

---

## 11. What it does and does not do to the other horror mods

The boundary is precise: sanity **never rewrites another mod's config, and never raises any frequency above what that mod was tuned to.** Everything it does is either additive-in-its-own-channel or subtractive-at-runtime.

**It does not:**
- Turn up the *dials* of Serverside Horror, The Forgotten, The Obsessed, or `horror_element_mod`. Their event frequencies live in `.toml`/`.json` or hardcoded logic (`CONFIG_CHANGES.md` documents that The Forgotten's LAN scare and mob-limiter are *hardcoded with no knob*), and KubeJS cannot reach a running mod's internal roll. We wouldn't even if we could — it would silently undo the pack's rarity tuning and couple two balance surfaces that should stay independent.
- Increase the spawn cadence of the timer-driven marquee horrors (Mimic, Cave Dweller, Man From The Fog). Those read their own config; sanity leaves them exactly as tuned.

**It does (all runtime-only, all reducing or reading, never config-editing):**
- **Suppress** natural spawn attempts of biome-weighted horrors via `checkSpawn` (Channel B, §8.2). This *lowers* their effective rate at high sanity and returns them to base at zero — it never pushes past base.
- **Read** the presence of tagged horror mobs for the proximity drain (§6.3) — observe only, never control.
- **Emit** its own self-contained ambient events (Channel A, §8.1), balanced entirely in its own `CONFIG`.

So sanity sits *alongside* the other systems, is retunable and removable without touching a single other mod's settings, and its only outward effect on them is to make some of them **rarer** — never more common.

---

## 12. Interactions & edge cases

| Situation | Behaviour | Why |
|---|---|---|
| **Multiplayer** | Fully per-player: per-player state, `@s`-targeted sounds/effects, per-player cooldowns and band lines. One player's breakdown is invisible and inaudible to another. | Sanity is a subjective state. |
| **The Forgotten mob limiter** | Unaffected. The limiter suppresses *natural* spawns; horror mobs are structure/rare-spawn driven, so proximity detection still works. | Documented in `CONFIG_CHANGES.md`. |
| **Nether portals disabled (TFC)** | Depth term never triggers in the Nether because players effectively can't reach it; the dimension guard makes it moot regardless. | `enableNetherPortals=false`. |
| **Sleeping through the night** | Rapid recovery, then daylight recovery on waking. A safe base fully resets dread overnight. | Intended rhythm. |
| **Death** | Sanity reset to baseline on respawn (§3.2). | Deterministic regardless of Forge's copy-on-death behaviour; thematically "a clean slate." |
| **Dimension change** | State persists on the player; drains/restores re-evaluate against the new dimension next interval. | Sanity is a property of the mind, not the place. |
| **Peaceful difficulty** | Events still fire (they aren't mobs); consider a `CONFIG` toggle to soften on Peaceful if desired. | Atmosphere isn't difficulty-gated by vanilla. |

---

## 13. Configuration surface

A single `CONFIG` object at the top of `oc_sanity.js` exposes every lever:

```
const CONFIG = {
  interval: 40,            // ticks between evaluations
  baseline: 100.0,
  drain:   { dark: 0.6, deepMin: 0.2, deepMax: 0.6, deepStartY: 0, deepFullY: -40,
             horror: 1.2, horrorRadius: 16, selfEvent: 2.0 },
  restore: { firelight: 0.5, daylight: 0.8, sleep: 8.0, clarityItem: 30.0 },
  light:   { darkBelow: 4, litAtOrAbove: 10 },
  bands:   { unease: 70, dread: 40, breakdown: 15, hysteresis: 5 },
  cooldown:{ unease: 12000, dread: 6000, breakdown: 2400, jitter: 0.25 }, // ticks
  spawn:   { enabled: true,  // Channel B — natural-spawn suppression (§8.2)
             gamma: 1.0,     // cancelChance = (sanity/100)^gamma; <1 forgiving, >1 punishing
             naturalOnly: true,   // ignore spawner/structure spawns
             noPlayerSanity: 0 }, // sanity assumed when no player is near a spawn (0 = allow base rate)
  bandLines: true,         // the diegetic band-crossing whispers
  affectPeaceful: true,
  debug: false,            // gate all verbose per-player / per-spawn logging (§15)
}
```

Everything about the *feel* — how fast dread builds, how forgiving recovery is, how chatty the world gets, and how hard high sanity holds the horror back — is a number here. Set `spawn.enabled=false` to keep the ambient channel but leave spawns entirely to the pack's base rates. No logic edit is needed to retune.

---

## 14. Files this feature touches

| File | Change |
|---|---|
| `kubejs/server_scripts/oc_sanity.js` | **New.** The whole mechanic: state, tick loop, drains/restores, bands, Channel A event pools, band lines, clarity-item use, Channel B `checkSpawn` suppression, death reset, and the `/sanity` command. |
| `kubejs/startup_scripts/oc_items.js` | Register `sedative_ampoule` + `familiar_photograph` (existing `oc(...)` helper). |
| `kubejs/startup_scripts/oc_creative.js` | Add both clarity items to the Old Civilisation creative tab. |
| `kubejs/server_scripts/oc_interactions.js` | Exclude the two clarity items from the lore discovery/variant-stamping scan (`OC_NON_LORE`) so a stackable consumable never gets per-item random NBT. |
| `kubejs/client_scripts/oc_tooltips.js` | Static tooltips for the clarity items (via `tt.add`, not the NBT-variant pool). |
| `kubejs/server_scripts/oc_loot.js` | LootJS placement for the clarity items. |
| `kubejs/assets/oldcivilisation/models/item/{sedative_ampoule,familiar_photograph}.json` | **New** item models (parented to `glass_bottle` / `paper` — placeholder, matching the existing OC model-reuse approach). |
| `config/openloader/data/OldCivSanity/` | **New** OpenLoader datapack: `pack.mcmeta` + the `#oldcivilisation:horror_mobs` entity-type tag (§6.3). Modded ids use `required:false` so an unresolved id never breaks the tag. |
| `docs/CONFIG_CHANGES.md` | A section recording the mechanic, its knobs, and its revert. |

No existing `.toml`, no other mod's config, no worldgen. Entirely additive.

---

## 15. Logging & diagnostics

Logging here has two non-negotiable jobs, and they pull in opposite directions:
1. **Make every failure loud and located.** This pack has twice been bitten by *silent* failure — the `redeclaration of var` bug killed the entire Old Civilisation system with no in-game symptom until the logs were read (`CONFIG_CHANGES.md`), and the worldgen cascade dumped 15,476 errors before anyone noticed. A new mechanic on hot paths must never fail quietly.
2. **Never become the spam.** The same document records what runaway logging looks like (167 identical errors, 15k cascade lines). This feature's hot paths — the 2 s tick loop and the per-attempt `checkSpawn` handler — could trivially flood `logs/latest.log`. So verbose logging is **gated and rate-limited by construction**, and failures are **logged once, then suppressed**.

### 15.1 Conventions
- **Prefix** every line `[OldCiv/Sanity]`, consistent with the existing `[OldCiv]` and `[AntiLag]` prefixes — greppable, and it tells you which subsystem spoke.
- **Levels:** `console.info` (lifecycle, load-time, low volume), `console.warn` (recoverable / degraded mode), `console.error` (caught exceptions). Verbose per-player/per-spawn tracing goes through a single `dlog()` helper gated on `CONFIG.debug`. **[verify live]** which `console.*` methods this KubeJS build exposes (`info`/`warn`/`error` are certain; if `console.debug` is absent, `dlog` routes to `console.info`).
- **Where it lands:** `logs/kubejs/startup.log`, `server.log`, `client.log` (type-specific), plus `logs/latest.log`. Registration logs in `startup.log`; runtime in `server.log`.

### 15.2 Always-on — load-time confirmation (INFO, a handful of lines total)
On script load, emit a short, bounded block so you can confirm wiring from the log alone (the same habit as the item plan's "validated: N unique IDs"):
- Script loaded + a one-line CONFIG summary (`interval`, band thresholds, `spawn.enabled`, `gamma`, `debug`).
- `N clarity items registered` and their ids.
- `#oldcivilisation:horror_mobs resolved to N entity types` — and **warn** for any listed type that does not resolve to a loaded entity (a typo'd or removed mod id).
- One explicit line per channel: `Channel A (ambient): ENABLED`; `Channel B (spawn suppression): ENABLED (gamma 1.0)` **or** `DISABLED (checkSpawn unavailable — see §15.4)`.

That block is the diagnostic you read first: if a channel says DISABLED or the tag count is wrong, you know before you ever load a world.

### 15.3 Gated verbose — tracing (DEBUG, off by default)
Only emitted when `CONFIG.debug = true`, all through `dlog()`:
- **Band transitions** — `player X: Unease → Dread (sanity 38.4, cause: dark+deep)`. One line per genuine transition (hysteresis already prevents boundary flicker), so this is naturally low-volume even on.
- **Sanity snapshots** — throttled to **once per ~10 s per player** (not per interval), so a debug session shows the curve without 30 lines/minute/player.
- **Event fires** — `player X: Channel A fired 'whisper_behind' (band Dread)`.
- **Spawn decisions (the hot one)** — `checkSpawn` runs on every attempt, so its debug line is **sampled**: log at most 1 in `N` decisions (or once per mob-type per 10 s), never one line per attempt. Format: `suppress mimicer? sanity=72 cancel=0.72 roll=0.31 → DENIED`.

**Hard rule:** every `dlog()` on a hot path (tick loop, checkSpawn) is either naturally rare (transitions) or explicitly throttled/sampled. `debug=true` must be safe to leave on for a ten-minute diagnostic without flooding the log. This is enforced in code, not left to the operator.

### 15.4 Failure handling — log-once, then degrade (WARN / ERROR)
- Every callback body is wrapped in `try/catch`, logging `console.error('[OldCiv/Sanity] <site>: ' + e)` with a **site label** (`tick`, `checkSpawn`, `clarity-use`, `respawn`) so the stack point is obvious.
- **Log-once suppression.** A module-level `var oc_sn_warned = {}` map keyed by site. The first failure at a site logs; subsequent identical failures increment silently (optionally emitting a single "…(suppressed further N)" summary). This is the direct antidote to the 167×-identical-error pattern — a broken accessor on the tick path logs **once**, not forty times a second.
- **Graceful degradation** — a failing accessor disables *only* its own sub-feature and logs one WARN naming what was turned off:

  | Failure | Logged (once) | Degrades to |
  |---|---|---|
  | `getLight()` throws | WARN | Darkness drain + firelight restore disabled; depth/proximity/day still run |
  | `isSleeping()` throws | WARN | Sleep restore disabled; other restores unaffected |
  | `checkSpawn` absent / uncancelable | WARN at load | Channel B off (`spawn.enabled=false`); Channel A unaffected |
  | Clarity item id unresolved | WARN at load | That item skipped; mechanic otherwise intact |

  The feature never hard-crashes a player tick or a spawn; it narrows and says so.

### 15.5 On-demand introspection — the `/sanity` command
Because the stat is invisible, inspecting it otherwise means reading raw NBT. A KubeJS-registered command exposes it directly (registered in `ServerEvents.commandRegistry`, wrapped in `try/catch` so a command-API mismatch degrades to "command absent" rather than breaking load):

- **`/sanity`** — replies to the caller with their own state: `<name> — sanity 38.4/100  [Dread]`.
- **`/sanity <target>`** — the same for another player; gated `requires(src => src.hasPermission(2))` so only operators can read someone else's mind.

It is **always available** (not debug-gated) — it's the intended way to check/verify a player's level in-game, for tuning and for admins. It only *reads* state (band is computed from the current value against the player's stored band for hysteresis), never mutates it, so using it can't perturb the mechanic. Player replies use the `player.tell(...)` styling already in `signal_device`/`audio_log`; console/command-block callers get `sendSystemMessage`.

---

## 16. Verify-live checklist (first in-game run)

The pack's convention is to flag KubeJS accessors that must be confirmed on the running build rather than assumed. For this feature:

1. **`player.block.getLight()`** returns effective light (else fall back to `max(skyLight, blockLight)` accessors).
2. **`player.isSleeping()`** exists and reads true while in a bed.
3. **`player.level.isDay()`** / **`canSeeSky()`** on `player.block`.
4. **`player.level.dimension` / `dimensionKey`** shape for the overworld-only depth guard.
5. **`PlayerEvents.respawned`** fires and exposes `event.player` for the death reset.
6. **`persistentData.getDouble` / `putDouble`** round-trip a float (the item system uses `getInt`/`getString`; double is the same API family but confirm).
7. Positional `playsound` behind the player computes sane coordinates from `player.yaw`.
8. **`EntityEvents.checkSpawn`** exists, exposes `event.entity` + `event.level` + spawn coordinates, and is **cancelable** (`event.cancel()` or `event.setResult('deny')`). Confirm whether it also exposes the spawn reason (`event.spawnType`) for the natural-only gate; if not, drop `naturalOnly` and accept that spawner/structure spawns are also suppressed. **Fallback if `checkSpawn` is absent/uncancelable on this build:** Channel B is disabled (`spawn.enabled=false`) and spawns fall back to base rates — Channel A still delivers the full experience.

Watch `logs/kubejs/` on load; every callback is wrapped in `try/catch` with a `console.error('[OldCiv/Sanity] …')` prefix, matching the existing scripts, so any accessor mismatch surfaces as a logged line rather than a silent failure.

---

## 17. Revert

Delete `kubejs/server_scripts/oc_sanity.js`, remove the clarity-item lines from `oc_items.js` / `oc_tooltips.js` / `oc_loot.js`, and delete the `horror_mobs.json` tag. Existing `oc_sanity*` keys left in players' `persistentData` are inert and harmless. No world data, worldgen, or other-mod config is affected.

---

## 18. Summary

Sanity is the pack's thesis rendered as a mechanic: **exposure to a broken world costs you something, and the cost is that the world stops leaving you alone.** It is invisible by design, cheap by discipline, and suppression-only by choice. It reaches the player through two channels — its own ambient dread (Channel A) and a runtime veto that holds the pack's *real* horror back while you're sane and returns it, never exceeding its authored base rate, as you break down (Channel B). It spawns nothing, rewrites no other mod's config, raises no frequency above baseline, and leaves the pack's hard-won rarity balance, minimal HUD, and fragile tick budget completely intact. Full sanity, the world leaves you alone; empty sanity, the world is exactly as dangerous as it was built to be — and never more.
