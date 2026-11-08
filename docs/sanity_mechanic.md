# The Sanity Mechanic — Design & Technical Specification

> **Status:** Implemented in `kubejs/server_scripts/oc_sanity.js`, plus the `OldCivSanity` datapack (`config/openloader/data/OldCivSanity/`) and the items in `kubejs/startup_scripts/oc_items.js` (verified live 2026-11-01: both channels load and the tick loop evaluates, per the DIAG lines in `logs/latest.log`). The 2026-11-08 recovery rework (§5.2) is on the client instance only: VM not applied, not tested in game. Targets KubeJS 6 (`kubejs-forge-2001.6.5`) on Forge 1.20.1. This spec matches the build as of 2026-11-08; behaviour the log lines don't show is marked **Not verified**, and parts of the design that were never built are listed in §16.
> **Related:** `docs/thelore.md` (the hidden truth), `docs/implore.md` and `docs/implore_implementation_plan.md` (the Old Civilisation item system), `docs/CONFIG_CHANGES.md` (Baseline "KubeJS — Sanity mechanic"; "2026-11-08 — Quest book: fixes and additions"; "2026-11-08 — spooky_tick_fixes 1.5.0: The Obsessed and CANTBREATHE sanity-bound"; "2026-11-08 — Sanity: slower recovery"), `tools/spooky_tick_fixes/README.md` (`[sanityHorror]`).

---

## 1. What this is (in one paragraph)

**Sanity** is a hidden, per-player resource that rises and falls with how safe the player's surroundings are. It is never shown on screen. The player only ever perceives it through *consequences* that grow as it falls. Its entire job is to make **the world feel like it is reacting to the player's exposure to it**, which is the exact premise of the pack's lore: reality is damaged, the horrors are symptoms, and staring into the dark for too long makes the damage notice you.

Sanity is a **suppression field, not a threat generator.** It has no failure state (you cannot "die of insanity"), and it *spawns nothing* — it only ever makes the world **quieter and safer as it rises**. Full sanity actively holds the horror *back*; losing sanity lets the horror the pack already ships come *forward*, up to — never past — its authored base rate. It drives two channels (detailed in §8):

- **Channel A — ambient events.** Sounds, footsteps behind the player, brief hidden screen effects, hallucinated mob cues. Zero at full sanity; more frequent and intense as it drops.
- **Channel B — natural-spawn suppression.** At high sanity, natural horror-mob spawns are cancelled before they happen; as sanity falls, that suppression lifts until, at zero, the pack's tuned base spawn rates pass through untouched.

The mental model for the player: **full sanity = the world leaves you alone; empty sanity = the world is exactly as dangerous as it was built to be.** Sanity can only ever *reduce* danger below the authored baseline — it can never manufacture more.

---

## 2. Why the pack wants this (design fit)

Four established pillars of this pack all point at the same mechanic, and all four constrain how it must be built:

| Pillar | Source | Consequence for sanity |
|---|---|---|
| **Reality is wrong; horrors are symptoms, not enemies.** | `thelore.md` ("The Observers", "The Correction", *"why are they looking at us?"*) | Sanity is the *player's* coupling to that damaged reality. Low sanity = reality misbehaving *around* the player. This is the mechanical expression of the whole story. |
| **Rare > frequent. Quiet dread, not jump-scares.** | `CONFIG_CHANGES.md` (Serverside Horror tuning, horror-spawn rarity, Man From The Fog — disabled 2026-11-06) | Sanity is **suppression-only**: it may reduce horror *below* the authored base rate but never raise it *above*. The tuned base rate is a hard ceiling reached only at zero sanity, so the mechanic can never undo the pack's rarity work — at worst it does nothing, at best it makes the world safer. It never rewrites another mod's config (§11). |
| **Minimal HUD.** | `CONFIG_CHANGES.md` (TFC health/hunger/thirst bars reverted to vanilla; thirst removed; TFC itself disabled 2026-11-01) | **No bar, no number.** A "SANITY 62%" readout would be a foreign video-game UI element in a pack that deliberately stripped its HUD to vanilla. Sanity is *invisible* and diegetic. |
| **The server thread is already under pressure.** | `CONFIG_CHANGES.md` (worldgen cascade, `Bruh [-4,66]` entity pile, Whispering Spirits per-tick cost, two anti-lag scripts) | The mechanic must be **cheap** and must **never spawn entities**. Its consequences are sounds and effects only. This is not a nice-to-have; it is why the "atmosphere-only" consequence model was chosen. |

The mechanic is deliberately scoped to the **safest, most on-theme** point in the design space: **invisible state, atmosphere-only consequences, multiple soft drains, multiple recoveries.** Every other option (visible bar, real-threat escalation, driving the other mods) was considered and rejected for one of the reasons above.

---

## 3. The state model

### 3.1 Storage

Sanity is a single per-player double, `oc_sanity`, held in `player.persistentData`. That is KubeJS's own tag (saved with the player as `KubeJSPersistentData`), not Forge's `ForgeData`. It is the same store `oc_interactions.js` uses for `oc_discoveries`. Source: `tools/spooky_tick_fixes/README.md` (1.5.0), which reads it through `kjs$getPersistentData()`.

| Key | Type | Holds |
|---|---|---|
| `oc_sanity` | double | Sanity, `0.0 … 100.0` |
| `oc_sanity_init` | boolean | Set on first evaluation; gates the default |
| `oc_sanity_band` | int | The stored band: 0 Calm, 1 Unease, 2 Dread, 3 Breakdown (§7) |
| `oc_sanity_last_drain` | long | Game time of the last acute drain; starts the settle delay (§5.2) |
| `oc_sanity_last_event` | long | Game time of the last Channel A event (§8.1) |
| `oc_photo_cd` | long | Game time of the last Familiar Photograph use (§10) |

- **Range:** `0.0 … 100.0`. Clamped on every write.
- **Baseline:** `100.0` (fully sane at world entry).
- **Initialisation:** `persistentData.getDouble('oc_sanity')` returns `0.0` when unset, which is indistinguishable from "genuinely at zero." Initialisation is therefore gated on a boolean flag:
  ```
  if (!pd.getBoolean('oc_sanity_init')) {
      pd.putDouble('oc_sanity', CONFIG.baseline)
      pd.putBoolean('oc_sanity_init', true)
      pd.putInt('oc_sanity_band', 0)
  }
  ```
  This is the standard idiom for "a persisted numeric with a nonzero default." Readers that don't initialise (Channel B, `/sanity`) treat an uninitialised player as baseline (`getSanity`).

### 3.2 Persistence semantics

- **Across logout / world reload:** `persistentData` is serialised with the player, so sanity survives naturally. Desired.
- **Across death:** whether the KubeJS tag is copied to the respawned player is not verified, and the design doesn't depend on it. **Design: respawn resets sanity to baseline** (a clean slate — "near-death clarity"). `PlayerEvents.respawned` sets `oc_sanity` to `CONFIG.baseline` (100.0), `oc_sanity_init` to true and `oc_sanity_band` to 0. The timestamps (`oc_sanity_last_drain`, `oc_sanity_last_event`, `oc_photo_cd`) are not reset. This makes the behaviour deterministic regardless of how the tag is treated on death, and it is thematically clean. That the respawn handler fires on this build: Not verified.

### 3.3 Who is exempt

The tick loop skips `player.isCreative()` and `player.isSpectator()` entirely each interval (no drain, no events). Creative/spectator players are not "in" the survival fiction. This also keeps the builder/admin experience clean. The exemption covers the tick loop only: Channel B reads the nearest player's stored sanity whatever their game mode (§8.2, §12).

---

## 4. The tick loop and its performance budget

### 4.1 One loop, heavily throttled

All per-player logic lives in a **single** `PlayerEvents.tick` handler, gated to run once every **40 ticks (2 s)**:

```
PlayerEvents.tick(event => {
    var player = event.player
    if (!player) return
    // (debug only: the DIAG heartbeat line, §15.3)
    if (player.age % CONFIG.interval !== 0) return
    // … all sanity logic here …
})
```

**Why 40 ticks:** sanity is a slow-moving atmospheric quantity, not a combat stat. Sampling once every 2 s is imperceptible to the player and cuts the per-tick cost by 40×. The pack has three cautionary tales about per-tick work (Whispering Spirits' every-tick look scan was the **#1 mod cost** in the Spark profile); this design refuses to add a fourth.

**Why one handler, not one per concern:** event-handler dispatch and the per-player guard cost is paid per registered handler. As of 2026-11-08, six server scripts register a `PlayerEvents.tick`: `anti_lag_items.js`, `anti_lag_mobs.js`, `oc_encounters.js`, `oc_interactions.js`, `oc_quest_checks.js` and `oc_sanity.js`. Sanity adds exactly **one**, and keeps all its per-interval work behind a single throttle. Consolidating the handlers into one loop is out of scope for this feature.

### 4.2 Per-interval cost (the actual budget)

Once every 2 s, per online player, the loop does at most:

1. **One light read** — `player.block.getLight()` (§6.1).
2. **One depth read** — `player.y` (a field access), plus the dimension test (§6.2).
3. **One sky/day check** — `player.block.canSeeSky` (a boolean property in KubeJS, not a method) and the overworld's day time (§5.2).
4. **One sleep read** — `player.isSleeping()`.
5. **One nearby-horror count** — a single `execute if entity @e[type=#oldcivilisation:horror_mobs,distance=..16]` command (see §6.3). One bounded selector evaluation, not an every-tick scan.
6. **Occasionally**, when an event fires: one or two `playsound` / `effect` commands targeting `@s`.

For a typical small server this is a rounding error next to entity ticking, chunk I/O, and worldgen — the things the profiles actually flagged. Crucially, **nothing here scales with world entity count** (unlike the Whispering Spirits procedure, which processed every spirit entity every tick). It scales only with *online player count*.

### 4.3 The Rhino trap (mandatory idiom)

KubeJS runs on Rhino (`rhino-forge-2001.2.3`), which **mis-hoists `let`/`const` declared inside a `try` block in a repeatedly-invoked callback** — the bug that silently killed the entire Old Civilisation interaction system until it was found (`redeclaration of var` × 167 in the logs; see CONFIG_CHANGES "KubeJS — Old Civilisation script bugfix"). **Every variable declared inside the tick/event callbacks in this script uses `var`.** Module-level `const` (the config tables) is fine — those load once.

---

## 5. Drains and restores (the numeric model)

All values below are **per 2 s interval** unless marked otherwise. They live in the single `CONFIG` object at the top of the script (§13), so the whole feel can be retuned in one place without touching logic. The intent is a slow curve measured in **minutes**, not seconds.

### 5.1 Drains

| Trigger | Δ per interval | Detection | Rationale |
|---|---:|---|---|
| **Darkness** — light `< 4` | **−0.6** | `player.block.getLight()` (§6.1) | Darkness is the core driver of sanity loss. Light 4 is the vanilla hostile-spawn threshold, so "dark enough for sanity loss" == "dark enough to be dangerous" — one intuition, two systems. An acute drain: it restarts the settle delay (§5.2). |
| **Depth** — overworld only, `Y < 0`, scaling linearly to `Y −40` | **−0.2 → −0.6** | `player.y`, gated on the dimension (§6.2) | The deep dark is where the pack's apex threats live (Cave Dweller at Y≤0, Deep Dark spawns). Depth pressure makes long sub-zero expeditions psychologically costly, not just mechanically dangerous. Overworld-only because "Y<0" is meaningless in the Nether/End. Below Y −40 the drain stays at −0.6. Not an acute drain. |
| **Horror proximity** — a tagged horror within 16 blocks | **−1.2** | `#oldcivilisation:horror_mobs` count (§6.3); flat, whatever the count | Being *near* one of the marquee horrors should spike dread. This is the spike term; it stacks on top of ambient drains. An acute drain: it restarts the settle delay. |
| **Self-inflicted feedback** — one of the mechanic's own Channel A events just fired | **−2.0** (one-shot) | internal; follows every Channel A event, in any band below Calm | A gentle positive-feedback loop: once the player is below Calm, the events themselves push them a little deeper, so a bad situation *escalates* rather than plateauing — but slowly: one −2.0 per event, and events are minutes apart (§7). |

Drains **stack additively**. Worked example — spelunking in the pitch dark at Y −50 with a Cave Dweller nearby:
`−0.6 (dark) − 0.6 (deep) − 1.2 (proximity) = −2.4 / interval`. From full (100) to the Dread band (40) is 60 points ≈ **25 intervals ≈ 50 s** of sustained exposure. Ambient darkness alone (−0.6, 18 per minute) reaches Dread in ≈ 200 s (~3.3 min). Channel A's −2.0 per event comes on top. Aggressive but deliberate; lower `CONFIG.drain.dark` toward 0 for a gentler pack.

### 5.2 Restores

| Condition | Δ per interval | Detection | Rationale |
|---|---:|---|---|
| **Firelight** — light `≥ 10` | **+0.2**, only up to **70** (`firelightCap`) | `player.block.getLight()` | Lighting your surroundings is the primary, always-available defence. Symmetry with the darkness drain makes "keep the torches up" a real psychological ritual, not just a spawn-proofing chore. Torchlight stops the bleeding, but reaching Calm (75 on the way up) needs daylight, sleep or a clarity item. |
| **Daylight** — daytime *and* sky access | **+0.4** | `player.block.canSeeSky`, and the overworld's `getDayTime() % 24000 < 12000` (the overworld clock, in every dimension) | Daytime above ground is the safe state. The two conditions together prevent "daytime in a cave" from healing. |
| **Sleep** — in bed | **+5.0** | `player.isSleeping()` (that it reads true in bed: Not verified) | Rest is refuge. A night's sleep gives about +12: a strong step back that reinforces base-building and the day/night rhythm the 30-min day establishes, with a morning outside finishing the recovery. |
| **Clarity consumables** | **+30** (Sedative Ampoule) / **+12** (Familiar Photograph), on use | `ItemEvents.rightClicked` (§10) | An active, lootable answer to a bad night — and a lore hook (see §10). |

**How the restores combine:**
- **No stacking.** Passive recovery (firelight, daylight) is the *larger* of the two, never their sum. Daylight (+0.4) exceeds firelight (+0.2), so daylight applies whenever both do.
- **Settle delay.** Passive recovery only starts `settleTicks` (600, 30 s) after the last *acute* drain: darkness, or a horror within 16 blocks. The time is stored in `oc_sanity_last_drain`. Depth pressure doesn't reset the clock, so a well-lit deep cave nets firelight minus depth, from 0 down to −0.4 per interval. Sleep and the clarity items are exempt.
- **One light read.** Darkness and firelight come from the same read, so the player can't be "in darkness" and "in firelight" simultaneously (light 4–9 gives neither).
- **Firelight cap.** A lit base at night holds a recovering player in Unease (firelight stops at 70), which brings a faint ambient cue about every 10 min.

**Why recovery is shaped this way (2026-11-08).** Recovery was too quick. `getLight()` returns the brightest light at the player's position, so open daylight (15) also counts as firelight. With the earlier summed restores (daylight +0.8, firelight +0.5, sleep +8.0, no cap, no delay), standing outside by day gave +1.3 per interval (39 per minute), more than twice the darkness drain (18 per minute), and any torch-lit spot healed, including lit caves above Y 0. A lit cave at Y −40 netted −0.1 per interval (−0.4 since the rework). Recovery times, with the values before 2026-11-08 in brackets:

| Situation | Leaving Breakdown (to 20) | Breakdown → Calm (75) |
|---|---|---|
| Outside by day | ~55 s (~10 s) | ~5.5 min (~1.5 min) |
| Torch-lit | ~80 s (~24 s) | stops at 70 (~4 min) |
| A night's sleep | about +12 (about +20) | — |

The drains, the bands and their hysteresis, and the event pools were unchanged by the rework. The longer Breakdown windows also give `spooky_tick_fixes` `[sanityHorror]` (1.5.0, not installed; §8) time to act: once both are live, The Obsessed and CANTBREATHE are expected to turn up more often during breakdowns. Status of the rework: client instance only (2026-11-08); VM not applied; not tested in game. Revert: CONFIG_CHANGES "2026-11-08 — Sanity: slower recovery".

### 5.3 Clamp and write

After summing the interval's deltas: if the sum is non-zero, `sanity = clamp(sanity + delta, 0, 100)` is written back with `pd.putDouble`. An interval with a zero sum (Calm, no drains) reads state but skips the write, to avoid needless NBT churn. An interval with an acute drain also stamps `oc_sanity_last_drain`. The band is then recomputed (§7), and a Channel A event may fire (§8.1); an event applies its −2.0 and writes again.

---

## 6. Detection details

### 6.1 Light

`player.block` is the block-space the player occupies; the script reads `.getLight()` on it. **Design:** effective light (max of sky and block light, as the spawn engine sees it), because a torch-lit cave at night should read as "safe" and an unlit surface at night should read as "dark." `getLight()` returns the brightest light at the player's position, so open daylight (15) reads as firelight (source: CONFIG_CHANGES "2026-11-08 — Sanity: slower recovery"). That an unlit surface at night reads as dark (the sky component lowered at night): Not verified. The fallback `max(skyLight − darkenAmount, blockLight)` is not in the script (§16): if `getLight()` throws, the darkness drain and the firelight restore are switched off (§15.4).

### 6.2 Depth and dimension

`player.y` gives altitude. The depth term is restricted to the overworld: the script stringifies `player.level.dimension` and checks that it contains `overworld`, a test that works whether KubeJS returns a ResourceKey or a ResourceLocation. If that throws it tries `player.level.dimensionKey`, and if both throw it treats the player as in the overworld. Nether/End get no depth drain (their geometry makes Y meaningless), but they *do* still get darkness and proximity drains — the End especially should feel unsafe.

### 6.3 Nearby horrors — the entity-type tag

Rather than iterate the entity list every interval (the expensive pattern), the proximity drain uses a **datapack entity-type tag** and lets the command engine do the bounded query:

`config/openloader/data/OldCivSanity/data/oldcivilisation/tags/entity_types/horror_mobs.json` (`"replace": false`)

It holds the marquee set the residue system recognises plus the other tuned horrors, 12 entries:

```
rake_the_arrival:rake_stalking, rake_the_arrival:rake_following, rake_the_arrival:rake_spawn_trigger,
the_one_who_watches:toww_staring, the_one_who_watches:toww_stalking,
whispering_spirits:whispering_spirit, antlers:wendigo, weeping_angels:weeping_angel,
themimic_er:mimicer, cave_dweller:cave_dweller,
cant_breathe:cant_breathe (required: false), pale_hound:pale_hound (required: false)
```

Only the CANTBREATHE and Pale Hound entries are `required: false`. The other ten are plain ids, which must resolve: removing one of those mods without editing the tag stops the tag from loading. The Man From The Fog (`man:manfromthefog`, `man:managgresive`) is not in the set: its jar has been disabled since 2026-11-06, and its ids were removed from the tag and from `HORROR_IDS` on 2026-11-08, when `cant_breathe:cant_breathe` was added (CONFIG_CHANGES "2026-11-08 — Quest book: fixes and additions").

Detection is then a single command per interval, run as the player (so `distance` is measured from the player):
`execute if entity @e[type=#oldcivilisation:horror_mobs,distance=..16]`
The command returns the number of matches; any match applies the drain.

This mirrors the established `#antilag:cullable_hostiles` tag pattern (`anti_lag_mobs.js`) — one tag, one selector, cheap and centrally editable. Channel B does not read the tag: it matches spawns against `HORROR_IDS`, a copy of the same 12 ids in `oc_sanity.js`. Adding or removing a horror takes two edits, the tag and `HORROR_IDS`, and no logic change.

---

## 7. The band model (why thresholds, not a linear effect)

Sanity is quantised into four **bands**, stored in `oc_sanity_band`. Events are driven by the band, not the raw number.

| Band | Range | What the player experiences |
|---|---|---|
| **Calm** | > 70 | Nothing. Silence is the reward. |
| **Unease** | 40 – 70 | Sparse, ambiguous cues: quiet cave ambience, a single sculk-sensor click, a footstep behind. ~10 min between events. |
| **Dread** | 15 – 40 | Footsteps *behind* you, a Warden's sound behind you, brief `nausea`, a 2 s `darkness` flash. ~5 min between events. |
| **Breakdown** | ≤ 15 | A `darkness` pulse with a heartbeat, 8 s `nausea`, hallucinated mob sounds (an Enderman's stare, a creeper's fuse, a close Warden) with **no corresponding mob**. ~2 min between events. |

A value exactly on a threshold belongs to the lower band (70 is Unease, 40 Dread, 15 Breakdown). The event pools are in §8.1.

**Why banded:**
- **Qualitative, legible escalation.** A linear "event chance = f(sanity)" produces a mushy ramp the player can't read. Discrete bands give the dread a *shape* — the player can feel themselves cross from "something's off" to "I need to leave."
- **It's how the player *learns the system exists* without a bar** (see §9). Crossing a band boundary is the diegetic signal.
- **Tuning surface.** Each band has its own event pool and cooldown, so Unease can be made rarer or Breakdown nastier independently.

**Hysteresis (anti-flicker).** Band changes are asymmetric to stop oscillation at a boundary from spamming the threshold cue: the player drops into a lower band the instant sanity reaches its threshold, but must climb **5 points above** a threshold to be promoted back up. Dread is entered at ≤ 40 and left at ≥ 45; Breakdown at ≤ 15 and ≥ 20; Unease at ≤ 70 and ≥ 75. This prevents a player hovering at 40 from triggering the "entering Dread" line every few seconds.

---

## 8. The two consequence channels

Sanity has exactly two outputs, both keyed off the band model (§7). Neither ever creates an entity: Channel A emits sounds and effects, Channel B *removes* spawn attempts the game would otherwise have made.

> **Readers of the stored band outside this script (2026-11-08):**
> - `spooky_tick_fixes` `[sanityHorror]` (1.5.0, not installed) reads the stored band (`oc_sanity_band`, from the player's KubeJS persistent data) to gate **The Obsessed** and **CANTBREATHE**. They only come for a player at Breakdown (`criticalBand`), and never appear inside a base. It follows the same rule as Channel B: it only ever holds back spawns those mods would have made, and never adds any. This reaches the timer-summoned horrors that Channel B can't (§8.2 table). A player the script has not run for counts as sane. Details: `tools/spooky_tick_fixes/README.md` (1.5.0) and CONFIG_CHANGES "2026-11-08 — spooky_tick_fixes 1.5.0: The Obsessed and CANTBREATHE sanity-bound".
> - The quest book's hidden "Breakdown" quest: `kubejs/server_scripts/oc_quest_checks.js` grants stage `oc_q_breakdown` when `oc_sanity_band` is 3.
>
> Renaming `oc_sanity_band` or its values would break both.

### 8.1 Channel A — ambient events (atmosphere-only, by construction)

Each band below Calm owns a **pool** of events. On each interval, if the player is in that band **and** the game time since `oc_sanity_last_event` is at least the band's cooldown times a random jitter factor (0.75–1.25, `cooldown.jitter` 0.25), one event is picked from the pool at random and fired. `oc_sanity_last_event` is then stamped with the current game time (the overworld's), and the self-feedback drain (−2.0, §5.1) is applied. The jitter keeps events from feeling metronomic. The stamp is shared by all bands, so a player who drops a band is measured against the new band's cooldown from the last event.

Every event is built from two primitives, **neither of which creates an entity**:

1. **Sound** — `player.runCommandSilent('playsound <id> master @s ~ ~ ~ <volume> <pitch>')`, or placed behind the player with `execute positioned ^ ^ ^-<d> run playsound <id> master @s ~ ~ ~ <volume> <pitch>`. Caret coordinates are relative to the player's facing, so `^ ^ ^-d` is *d* blocks behind. A footstep placed 2–3 blocks behind the player reads as "something is behind me" without anything being there. This is the same illusion Serverside Horror's `fake_steps` uses — the mechanic reproduces the *technique*, under its own control. That the placed sound is heard from behind in game: Not verified.
2. **Effect** — `effect give @s minecraft:nausea <secs> 0 true` / `minecraft:darkness <secs> 0 true`. The trailing `true` hides the potion particles/HUD icon, so the effect is felt (screen warp, vignette) but leaves **no HUD trace** — critical for the "invisible mechanic" rule.

The pools (`POOLS` in the script; sound ids are `minecraft:`, volume in brackets, pitch 1.0 unless given):

| Band | Events (one per roll) |
|---|---|
| **Unease** | `ambient.cave` (0.4) · `block.sculk_sensor.clicking` (0.5, pitch 0.8) · `block.wood.step` 2 blocks behind (0.7, pitch 0.9) |
| **Dread** | `block.gravel.step` 3 and then 2 blocks behind (0.8 each) · `entity.warden.nearby_closest` 3 blocks behind (0.6) · `nausea` 3 s + `ambient.cave` (0.5, pitch 0.8) · `darkness` 2 s · `ambient.crimson_forest.mood` (0.6, pitch 0.8) |
| **Breakdown** | `darkness` 5 s + `entity.warden.heartbeat` (0.8) · `nausea` 8 s · `entity.enderman.stare` 2 blocks behind (0.9) and at the player (0.7) · `entity.creeper.primed` (0.8), a fuse with no creeper · `entity.warden.nearby_closest` 3 blocks behind (1.0, pitch 0.7) + `nausea` 4 s |

Because the strongest primitive is a hidden `nausea`/`darkness` and a positional sound, **the worst case for the server is a few commands**, and the worst case for the *client* is a screen-warp shader that vanilla already ships. There is no path from "low sanity" to "entity spawn," so there is no path from this feature to the entity-pile lag class the pack has been fighting.

### 8.2 Channel B — natural-spawn suppression

This channel implements the core design intent: **high sanity makes the pack's real horror rarer; zero sanity lets it through at its authored base rate.** It does this by *cancelling* natural spawn attempts, never by creating them — so it can only ever subtract from what the game already decided to do.

**Mechanism.** A single `EntityEvents.checkSpawn` handler (Forge's check-spawn path, which KubeJS exposes and which is cancelable). When a horror in `HORROR_IDS` attempts a *natural* spawn, the handler resolves the nearest player's sanity and cancels probabilistically:

```
EntityEvents.checkSpawn(event => {
  var doDeny = false
  try {
    var ent = event.entity
    if (!ent) return
    var id = '' + ent.type
    if (!HORROR_ID_SET[id]) return                        // HORROR_IDS (§6.3)
    if (CONFIG.spawn.naturalOnly) {
      var reason = spawnReason(event)                     // event.type, else event.getType()
      if (reason !== null && reason.toUpperCase().indexOf('NATURAL') < 0) return
    }
    var s = nearestSanity(ent)                            // noPlayerSanity (0 = allow) if the dimension has no players
    var cancelChance = Math.pow(clamp(s, 0, 100) / 100, CONFIG.spawn.gamma)
    if (Math.random() < cancelChance) doDeny = true
  } catch (e) { warnOnce('checkSpawn-body', e); return }
  if (doDeny) event.cancel()                              // outside the try: cancel() throws EventExit
})
```

- **Cancelling.** KubeJS's `event.cancel()` works by throwing `EventExit`, which must reach the event system to deny the spawn. Called inside a `try/catch`, the exit is swallowed and the deny is lost, so the decision is made inside the `try` and the cancel happens outside it.
- **Spawn reason.** `CheckLivingEntitySpawnEventJS` exposes the `MobSpawnType` as the field `type` (or `getType()`). Only types containing `NATURAL` are suppressed; spawner, structure, chunk-generation and other spawn types pass untouched. If the event exposed no type, every spawn type would be treated as natural.
- **Nearest player.** The nearest player (3D distance) in the spawning entity's dimension, at any distance and in any game mode. A player the tick loop has never initialised reads as baseline (100).
- **Verification.** Registration verified live 2026-11-01 (the load line reports Channel B `ENABLED`). That denials take effect in game: Not verified.

**The curve.** `cancelChance = (sanity/100)^gamma`.
- `sanity = 100` → cancelChance `1.0` → the spawn is always denied (`Math.random()` is below 1) ⇒ **full sanity ≈ 0% horror** from this bucket.
- `sanity = 0` → cancelChance `0.0` → never denied ⇒ **the tuned base rate passes through, untouched**.
- `gamma = 1.0` (the pack's value) is linear (50 sanity → 50% suppression). `gamma < 1` front-loads suppression so you have to fall a long way before spawns really return (a more forgiving pack); `gamma > 1` means even mild sanity loss opens the gates fast. One knob, whole feel.

**Why cancel-only (never add).** `checkSpawn` can *veto* an attempt but cannot *manufacture* one. That limitation is the feature: base rate is a hard ceiling (§2), so this channel is structurally incapable of exceeding the pack's authored rarity. It rarifies; it never inflates.

**Which horrors this actually reaches — the two buckets (be honest about this):**

| Bucket | Spawn mechanism | Scalable by Channel B? | Members (per `CONFIG_CHANGES.md`) |
|---|---|:--:|---|
| **Natural / biome-weighted** | Vanilla/Forge spawn pipeline, biome spawn weight | **Yes** — routes through `checkSpawn`, for ids in `HORROR_IDS` | In `HORROR_IDS`, from the `HorrorSpawnRarity` set: **Rake** (`rake_stalking`, `rake_following`, `rake_spawn_trigger`), **The One Who Watches** (`toww_staring`, `toww_stalking`), **Wendigo** (`antlers:wendigo`), **Whispering Spirits**; and **Weeping Angels** (biome spawn config; whether it routes through check-spawn: Not verified). Natural spawns that are **not** in `HORROR_IDS`, so Channel B leaves them alone: John Mod Reborn and Creeping Woods (in `HorrorSpawnRarity` since 2026-11-01), and general vanilla hostiles. |
| **Timer / scheduler self-summon** | The mod's own internal tick timer reads its `*-server.toml` cadence and summons directly | **No** — bypasses `checkSpawn`; KubeJS cannot rewrite a mod's loaded config per-player at runtime | **The Mimic** (`can_spawn_min/max`), **Cave Dweller** (`can_spawn_min/max`), **The Pale Hound** and **CANTBREATHE** (`can_spawn_min/max`; source: CONFIG_CHANGES "2026-11-01 — Lore and config consistency pass"). They are in the tag and `HORROR_IDS` for the proximity drain only. **The Man From The Fog** (`min/max_spawn_rate`) belonged here until its jar was disabled on 2026-11-06. |

**Wendigo caveat (checked 2026-11-07):** only its first, natural spawn goes through `checkSpawn`. Antlers swaps it for its daytime form (`antlers:wendigomad`) at dawn and back at dusk by summoning a fresh mob, which skips the hook. `antlers:wendigomad` is not in the tag or `HORROR_IDS`, so the daytime form doesn't count for the proximity drain either. Without `spooky_tick_fixes` 1.4.0 neither form despawns, so one Wendigo stays in its area for good. With 1.4.0 (installed on the client instance and the VM) both despawn like other monsters, so one doesn't outlast the players' visit, and the next has to pass the gate.

Bucket 2 keeps the static cadence already tuned in `CONFIG_CHANGES.md`; sanity cannot dynamically rarify it. Fighting those schedulers by deleting freshly-summoned entities (`EntityEvents.spawned` post-removal) is possible but **rejected**: it flickers, many MCreator mobs summon outside the normal placement path so it wouldn't catch them anyway, and a scheduler that finds its spawn gone tends to just retry. Not worth the fragility.

**The coupling that covers the gap.** A Bucket-2 horror still craters sanity via the horror-proximity drain (§5.1) the moment it appears. That drop lifts Channel-B suppression on the Bucket-1 stalkers *and* opens Channel A. So the un-scalable timer-horror becomes the **trigger** for a wider bad night, and sanity handles the **aftermath** — the two buckets stay linked even though only one is directly spawn-scaled. `spooky_tick_fixes` `[sanityHorror]` (1.5.0, not installed) gates two Bucket-2 horrors by band (callout at the top of §8).

**Cost.** `checkSpawn` fires only on spawn *attempts*, which the pack already made rare. The per-attempt work is one id lookup in `HORROR_IDS` + a nearest-player pass over the dimension's players + one RNG roll — orders of magnitude cheaper than a per-tick scan, and it scales with spawn attempts (already throttled by the base rates), not with world entity count.

---

## 9. How the player discovers an invisible system

A fully silent hidden stat is a design risk: the player may never realise the world is responding to *them* rather than to random chance. The mechanic solves this **diegetically**, without ever admitting it's a stat:

- **Band-crossing lines.** When (and only when) the player crosses *down* into a new band, a single italic, dark-grey line prints in chat (`player.tell(Text.of(line).darkGray().italic(true))`) — the pack's established "raw voice" styling, the same look the Old Civilisation tooltips use:
  - entering Unease → *"Something about this place sits wrong."*
  - entering Dread → *"Your thoughts feel heavier down here."*
  - entering Breakdown → *"You can't tell if the sound is outside your head."*

  Hysteresis (§7) guarantees these fire at most once per genuine descent.
- **Correlation does the rest.** Because drains map onto *legible* causes (darkness, depth, proximity) and recoveries onto *legible* safety (light, day, bed, the clarity items), an attentive player infers the rule — "the dark is doing this to me" — without ever being told there's a number. That inference *is* the horror.

These lines and the clarity items' use lines (§10) are the only textual surface. They are content-tunable (`BAND_LINES` in the script), and `CONFIG.bandLines = false` turns the band lines off for a purist silent run.

---

## 10. Clarity consumables (the active recovery + lore hook)

Two items, registered through the **existing** Old Civilisation pipeline (`oc_items.js` for registration, `oc_tooltips.js` for the voiced tooltip, `oc_loot.js` for placement), so they arrive as *part of the story*, not a bolted-on potion:

- **Sedative Ampoule** (`oldcivilisation:sedative_ampoule`; uncommon, stacks to 8) *(Technical / Engineer voice)*. Tooltip: *"Field-issue. Dulls what the mind insists on noticing."* / *"Right-click to use."* On right-click: +30 sanity (`CONFIG.restore.clarityItem`), plays `minecraft:entity.generic.drink`, removes one item from the main-hand stack (not in creative), and prints *"The noise recedes. A little."* Consumed.
- **Familiar Photograph** (`oldcivilisation:familiar_photograph`; uncommon, stack of 1) *(Personal / Survivor voice)*. Tooltip: *"A face you are sure you knew."* / *"Right-click to look. It steadies you."* On right-click: +12 sanity (0.4 × `clarityItem`) and *"A face you are sure you knew. It steadies you."* Reusable, with a 2,400-tick (2 min) game-time cooldown stored in `oc_photo_cd`; during the cooldown it prints *"You have already looked. It does not help twice so soon."* and restores nothing. Comfort, not a cure.

Recovery is applied in `ItemEvents.rightClicked` by writing `oc_sanity` directly (the same persistentData write path as the tick loop, initialising the player first if needed). Neither item waits for the settle delay (§5.2). The band catches up at the next evaluation.

Placement goes through LootJS, so their rarity is tuned centrally alongside every other lore item, and "how often can I find relief" is a balance knob, not a constant:

| Item | Chance | Chests |
|---|---:|---|
| Sedative Ampoule ×2 | 5% | simple dungeon, abandoned mineshaft, stronghold corridor and crossing, ancient city, pillager outpost |
| Familiar Photograph ×1 | 4% | the five village house chests, woodland mansion, simple dungeon |

The quest book also gives them: the hidden "Breakdown" quest rewards 4 Sedative Ampoules, and The Silent Nights a Familiar Photograph (CONFIG_CHANGES "2026-11-08 — Quest book: fixes and additions").

---

## 11. What it does and does not do to the other horror mods

The boundary is precise: sanity **never rewrites another mod's config, and never raises any frequency above what that mod was tuned to.** Everything it does is either additive-in-its-own-channel or subtractive-at-runtime.

**It does not:**
- Turn up the *dials* of Serverside Horror, The Forgotten (removed 2026-11-01), The Obsessed, or `horror_element_mod`. Their event frequencies live in `.toml`/`.json` or hardcoded logic (`CONFIG_CHANGES.md` documents that The Forgotten's LAN scare and mob-limiter are *hardcoded with no knob*), and KubeJS cannot reach a running mod's internal roll. Design: the mechanic would not do so even if it could — it would silently undo the pack's rarity tuning and couple two balance surfaces that should stay independent.
- Increase the spawn cadence of the timer-driven marquee horrors (Mimic, Cave Dweller, Pale Hound, CANTBREATHE). Those read their own config; sanity leaves them exactly as tuned.

**It does (all runtime-only, all reducing or reading, never config-editing):**
- **Suppress** natural spawn attempts of biome-weighted horrors via `checkSpawn` (Channel B, §8.2). This *lowers* their effective rate at high sanity and returns them to base at zero — it never pushes past base.
- **Read** the presence of tagged horror mobs for the proximity drain (§6.3) — observe only, never control.
- **Emit** its own self-contained ambient events (Channel A, §8.1), balanced entirely in its own `CONFIG`.

So sanity sits *alongside* the other systems, is retunable and removable without touching a single other mod's settings, and its only outward effect on them is to make some of them **rarer** — never more common. The one outside system that acts on The Obsessed and CANTBREATHE by band, `spooky_tick_fixes` `[sanityHorror]` (1.5.0, not installed), is a separate mod that follows the same hold-back-only rule (§8).

---

## 12. Interactions & edge cases

| Situation | Behaviour | Why |
|---|---|---|
| **Multiplayer** | Fully per-player: per-player state, `@s`-targeted sounds/effects, per-player cooldowns and band lines. One player's breakdown is invisible and inaudible to another. | Sanity is a subjective state. |
| **Creative / spectator players** | Skipped by the tick loop (§3.3). Channel B still uses the nearest player's stored sanity, so a creative player who was never evaluated reads as 100 and, while nearest, suppresses every natural spawn of a tagged horror. | Channel B's nearest-player pass has no game-mode filter. |
| **The Forgotten mob limiter** *(mod removed 2026-11-01 — historical)* | Unaffected. The limiter suppresses *natural* spawns; horror mobs are structure/rare-spawn driven, so proximity detection still works. | Documented in `CONFIG_CHANGES.md`. |
| **Nether access** | The Nether is reachable again (TFC, which disabled nether portals, was disabled 2026-11-01). The depth term is overworld-only by its dimension guard, so being in the Nether never drains via depth. | dimension guard in `oc_sanity.js` |
| **Sleeping through the night** | About +12 from the night itself, then daylight recovery on waking (after the settle delay, if an acute drain came first). Since 2026-11-08 a night's sleep no longer fully resets dread on its own; a morning outside finishes it. | Intended rhythm. |
| **Death** | Sanity, its init flag and its band reset to baseline on respawn (§3.2). | Deterministic regardless of how the tag is treated on death; thematically "a clean slate." |
| **Dimension change** | State persists on the player; drains/restores re-evaluate against the new dimension next interval. The daylight restore keeps using the overworld's clock. | Sanity is a property of the mind, not the place. |
| **Peaceful difficulty** | Events still fire (they aren't mobs). `CONFIG.affectPeaceful` exists, but no code reads it (§16). | Atmosphere isn't difficulty-gated by vanilla. |

---

## 13. Configuration surface

A single `CONFIG` object at the top of `oc_sanity.js` exposes the levers. The values below are the script's; the comments explain each key:

```
const CONFIG = {
  interval: 40,            // ticks between evaluations (2 s) — the throttle
  baseline: 100.0,
  drain:   { dark: 0.6, deepMin: 0.2, deepMax: 0.6, deepStartY: 0, deepFullY: -40,
             horror: 1.2, horrorRadius: 16, selfEvent: 2.0 },
  restore: { firelight: 0.2, daylight: 0.4, sleep: 5.0, clarityItem: 30.0, firelightCap: 70, settleTicks: 600 },
             // 2026-11-08; was 0.5 / 0.8 / 8.0, summed, no cap or delay (§5.2). The Photograph gives 0.4 × clarityItem
  light:   { darkBelow: 4, litAtOrAbove: 10 },
  bands:   { unease: 70, dread: 40, breakdown: 15, hysteresis: 5 },
  cooldown:{ unease: 12000, dread: 6000, breakdown: 2400, jitter: 0.25 }, // ticks
  spawn:   { enabled: true,       // Channel B — natural-spawn suppression (§8.2)
             gamma: 1.0,          // cancelChance = (sanity/100)^gamma; <1 forgiving, >1 punishing
             naturalOnly: true,   // suppress NATURAL spawns only; spawner/structure spawns pass
             noPlayerSanity: 0 }, // sanity assumed when the dimension has no players (0 = allow base rate)
  bandLines: true,         // the diegetic band-crossing lines (§9)
  affectPeaceful: true,    // not read by the script (§16)
  debug: false,            // verified live 2026-11-01 (DIAG lines in logs/latest.log); keep off — true spams the log every 2 s per player (§15.3)
}
```

Everything about the *feel* — how fast dread builds, how forgiving recovery is, how chatty the world gets, and how hard high sanity holds the horror back — is a number here. Set `spawn.enabled=false` to keep the ambient channel but leave spawns entirely to the pack's base rates. No logic edit is needed to retune. Fixed in the script rather than `CONFIG`: the event pools (`POOLS`), the band lines (`BAND_LINES`), the horror list (`HORROR_IDS`), the no-stacking rule for passive recovery, the Photograph's 0.4 multiplier and 2,400-tick cooldown, and the 1-in-20 sampling of spawn debug lines.

---

## 14. Files this feature touches

| File | Role |
|---|---|
| `kubejs/server_scripts/oc_sanity.js` | The whole mechanic: state, tick loop, drains/restores, bands, Channel A event pools, band lines, clarity-item use, Channel B `checkSpawn` suppression, death reset, and the `/sanity` command. |
| `kubejs/startup_scripts/oc_items.js` | Registers `sedative_ampoule` (uncommon, stack 8) and `familiar_photograph` (uncommon, stack 1) with the existing `oc(...)` helper. |
| `kubejs/startup_scripts/oc_creative.js` | Both clarity items in the Old Civilisation creative tab. |
| `kubejs/server_scripts/oc_interactions.js` | `OC_NON_LORE` excludes both clarity items from the lore discovery/variant-stamping scan, so a stackable consumable never gets per-item random NBT. |
| `kubejs/client_scripts/oc_tooltips.js` | Static tooltips for the clarity items (via `tt.add`, not the NBT-variant pool). |
| `kubejs/server_scripts/oc_loot.js` | LootJS placement for the clarity items (§10). |
| `kubejs/assets/oldcivilisation/models/item/{sedative_ampoule,familiar_photograph}.json` | Item models parented to `minecraft:item/glass_bottle` / `minecraft:item/paper` (placeholders, matching the existing OC model-reuse approach). |
| `config/openloader/data/OldCivSanity/` | OpenLoader datapack: `pack.mcmeta` (`pack_format` 15) + the `#oldcivilisation:horror_mobs` entity-type tag (§6.3). Two of its twelve entries use `required: false`. |
| `docs/CONFIG_CHANGES.md` | The Baseline section "KubeJS — Sanity mechanic" records the mechanic, its knobs, and its revert; later entries record changes to it. |

No existing `.toml`, no other mod's config, no worldgen. Entirely additive. Readers outside the feature: `kubejs/server_scripts/oc_quest_checks.js` and `spooky_tick_fixes` `[sanityHorror]` (1.5.0, not installed), both of `oc_sanity_band` (§8).

---

## 15. Logging & diagnostics

Logging here has two non-negotiable jobs, and they pull in opposite directions:
1. **Make every failure loud and located.** This pack has twice been bitten by *silent* failure — the `redeclaration of var` bug killed the entire Old Civilisation system with no in-game symptom until the logs were read (`CONFIG_CHANGES.md`), and the worldgen cascade dumped 15,476 errors before anyone noticed. A new mechanic on hot paths must never fail quietly.
2. **Never become the spam.** The same document records what runaway logging looks like (167 identical errors, 15k cascade lines). This feature's hot paths — the 2 s tick loop and the per-attempt `checkSpawn` handler — could trivially flood `logs/latest.log`. So verbose logging is **gated behind `CONFIG.debug`**, and failures are **logged once, then suppressed**. The DIAG trace lines are not rate-limited (§15.3), so `debug` stays off.

### 15.1 Conventions
- **Prefix** every line `[OldCiv/Sanity]`, consistent with the existing `[OldCiv]` and `[AntiLag]` prefixes — greppable, and it tells you which subsystem spoke.
- **Levels:** `console.info` for the load lines and for debug tracing; `console.warn` for every failure (through `warnOnce`, §15.4). `console.error` and `console.debug` are not used. Verbose tracing goes through a `dlog()` helper gated on `CONFIG.debug`, which routes to `console.info`.
- **Where it lands:** `logs/kubejs/startup.log`, `server.log`, `client.log` (type-specific), plus `logs/latest.log`. Registration logs in `startup.log`; runtime in `server.log`.

### 15.2 Always-on — load-time confirmation (INFO, three lines)
On script load, the script emits a short, bounded block so wiring can be confirmed from the log alone (the same habit as the item plan's "validated: N unique IDs"):

```
[OldCiv/Sanity] loaded. interval=40t  bands(U/D/B)=70/40/15  spawn.enabled=true gamma=1  debug=false
[OldCiv/Sanity] Channel A (ambient): ENABLED.  Channel B (spawn suppression): ENABLED
[OldCiv/Sanity] horror set: 12 entity ids (proximity drain + Channel B). Unresolved ids are inert (datapack tag uses required:false).
```

Instead of `ENABLED`, Channel B reports `FAILED TO REGISTER — spawns fall back to base rate` or `DISABLED by config`. The horror-set count is the length of `HORROR_IDS`, not a check of the tag; its `required:false` note holds for two of the tag's twelve entries (§6.3).

That block is the first diagnostic to read: if Channel B doesn't say `ENABLED`, the log shows it before anyone joins.

### 15.3 Gated verbose — tracing (debug, off by default)
Only emitted when `CONFIG.debug = true`:
- **DIAG heartbeat** — every 60 player-tick callbacks (about 3 s with one player online), independent of the 40-tick throttle: the player's `age`, `age % 40`, whether the throttle passes, and creative/spectator state. A creative or spectator player is skipped by the loop, and this line reveals that.
- **DIAG eval** — every evaluation (every 2 s per player): sanity, light, y, overworld, horror count, `dark`, `lit`, `daylight`, `settled`, `dayOk`, `sleepOk`, the delta and the new sanity. **DIAG write** follows each write with the value read back.
- **Skip** — one line per evaluation for a creative/spectator player.
- **Band transitions** — descents only: `player X: Unease -> Dread (sanity 38.4)`. Hysteresis already prevents boundary flicker, so this is naturally low-volume.
- **Event fires** — `player X: Channel A event (band Dread)`.
- **Clarity use** — `player X: +30 -> 75`.
- **Spawn decisions (the hot one)** — `checkSpawn` runs on every attempt, so its debug line is **sampled**: denials only, 1 in 20. Format: `(sampled) suppress themimic_er:mimicer sanity=72 cancel=0.72`.

**Hard rule (design):** every `dlog()` on a hot path (tick loop, checkSpawn) is either naturally rare (transitions) or explicitly throttled/sampled, so `debug=true` is safe to leave on for a ten-minute diagnostic without flooding the log. The transition and spawn lines meet it. The DIAG lines (heartbeat, eval, write, skip) don't: they print every 2 s per player, which is why `debug` is off (2026-11-01).

### 15.4 Failure handling — log-once, then degrade (WARN)
- Every callback body is wrapped in `try/catch`. A failure goes to `warnOnce(site, e)`, which logs `console.warn('[OldCiv/Sanity] degraded at [<site>]: <e> (further identical messages suppressed)')` with a **site label** so the failing point is obvious. Sites: `tick`, `getLight`, `depth`, `daylight`, `isSleeping`, `proximity`, `event-cmd`, `fireEvent`, `checkSpawn-body`, `checkSpawn-register`, `nearestSanity`, `respawn`, `respawn-register`, `sedative`, `photo`, `sanity-report`, `command-register`.
- **Log-once suppression.** A module-level `var oc_sn_warned = {}` map keyed by site. The first failure at a site logs; later failures there increment its count silently, and no summary line is printed. This is the direct antidote to the 167×-identical-error pattern — a broken accessor on the tick path logs **once**, not on every evaluation.
- **Graceful degradation** — a failing accessor disables *only* its own sub-feature, until the server scripts reload:

  | Failure | Logged (once) | Degrades to |
  |---|---|---|
  | `getLight()` throws | WARN `getLight` | Darkness drain + firelight restore disabled; depth/proximity/day still run |
  | Daylight check throws | WARN `daylight` | Daylight restore disabled; other restores unaffected |
  | `isSleeping()` throws | WARN `isSleeping` | Sleep restore disabled; other restores unaffected |
  | Depth or proximity read throws | WARN `depth` / `proximity` | That term is skipped for the interval and tried again at the next |
  | `checkSpawn` registration fails | WARN `checkSpawn-register`; the load line reads `FAILED TO REGISTER` | Channel B off; spawns fall back to base rates; Channel A unaffected |
  | Nearest-player lookup throws | WARN `nearestSanity` | `noPlayerSanity` (0): the spawn is allowed |
  | Command registration fails | WARN `command-register` | `/sanity` absent |

  The feature never hard-crashes a player tick or a spawn; it narrows and says so.

### 15.5 On-demand introspection — the `/sanity` command
Because the stat is invisible, inspecting it otherwise means reading raw NBT. A KubeJS-registered command exposes it directly (registered in `ServerEvents.commandRegistry`, wrapped in `try/catch` so a command-API mismatch degrades to "command absent" rather than breaking load):

- **`/sanity`** — no arguments; replies to the caller with their own state: `<name> — sanity 38.4/100  [Dread]`. Anyone can run it. From the console (no player) it replies `[OldCiv/Sanity] no target player.`, so give a target there.
- **`/sanity <target>`** — `target` is one player: a name, or a selector that matches exactly one player (`@p`, `@s`). The same report for that player; gated `requires(src => src.hasPermission(2))` so only operators can read someone else's mind.

It is **always available** (not debug-gated) — it's the intended way to check/verify a player's level in-game, for tuning and for admins. It only *reads* state (band is computed from the current value against the player's stored band for hysteresis), never mutates it, so using it can't perturb the mechanic. Player replies go through `player.tell(...)` in grey, the styling already in `signal_device`/`audio_log`; console/command-block callers get `sendSystemMessage`. Also documented in `README.md` (Custom commands).

---

## 16. Not implemented

Parts of the design that were never built:

- **Particle hallucinations** (Channel A's third primitive). The pools use sounds and effects only.
- **Whisper sounds, the item-pickup blip, and a repeating darkness pulse.** The built pools are in §8.1; Breakdown's darkness is a single 5 s effect.
- **Weighted pools.** One event is picked uniformly from the band's pool.
- **Self-feedback limited to Dread and to intervals with other drains.** The −2.0 follows every Channel A event, in any band below Calm.
- **The effective-light fallback** (`max(skyLight − darkenAmount, blockLight)`). A `getLight()` failure switches off the darkness drain and firelight restore instead.
- **The Peaceful toggle.** `CONFIG.affectPeaceful` is not read; events fire on Peaceful.
- **Food-use for the clarity items.** Right-click only.
- **Load-time checks:** the "N clarity items registered" line, per-entry resolution warnings for `#oldcivilisation:horror_mobs`, and a WARN for an unresolved clarity-item id.
- **Detecting an uncancelable `checkSpawn`.** Only a failed registration turns Channel B off.
- **Debug rate limits and detail:** the DIAG lines print every evaluation rather than once per ~10 s per player; transition lines carry no cause and cover descents only; spawn lines carry no roll and cover denials only; event lines carry no event name.
- **The "…(suppressed further N)" summary** for log-once failures.

**Not verified** (the 2026-11-01 log lines don't show these):

- An unlit surface at night reads as dark to `player.block.getLight()` (the sky component lowered at night).
- `player.isSleeping()` reads true while in a bed.
- `PlayerEvents.respawned` fires and resets the values; whether the KubeJS tag survives death without it.
- A caret-positioned `playsound` is heard from behind the player.
- Channel B denials take effect in game (registration is verified).
- Weeping Angels' natural spawns route through `checkSpawn`.
- The 2026-11-08 recovery rework (§5.2): not tested in game.

---

## 17. Revert

Delete `kubejs/server_scripts/oc_sanity.js`; remove the two clarity items from `oc_items.js`, `oc_creative.js`, `oc_tooltips.js` and `oc_loot.js`; remove the `OC_NON_LORE` guard from `oc_interactions.js`; delete the two model JSONs and `config/openloader/data/OldCivSanity/` (which holds the `horror_mobs.json` tag). The quest book references the items (4 Sedative Ampoules for "Breakdown", a Familiar Photograph for The Silent Nights). Existing `oc_sanity*` and `oc_photo_cd` keys left in players' persistent data are inert, except `oc_sanity_band`, which two readers use: the quest book's Breakdown stage (`oc_quest_checks.js`) and `spooky_tick_fixes` `[sanityHorror]` (1.5.0, not installed), which would go on reading each player's last stored band. No world data, worldgen, or other-mod config is affected.

---

## 18. Summary

Sanity is the pack's thesis rendered as a mechanic: **exposure to a broken world costs you something, and the cost is that the world stops leaving you alone.** It is invisible by design, cheap by discipline, and suppression-only by choice. It reaches the player through two channels — its own ambient dread (Channel A) and a runtime veto that holds the pack's *real* horror back while you're sane and returns it, never exceeding its authored base rate, as you break down (Channel B). It spawns nothing, rewrites no other mod's config, raises no frequency above baseline, and leaves the pack's hard-won rarity balance, minimal HUD, and fragile tick budget completely intact. Full sanity, the world leaves you alone; empty sanity, the world is exactly as dangerous as it was built to be — and never more.
