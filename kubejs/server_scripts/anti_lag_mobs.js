// priority: 100
// Anti-lag: cull runaway HOSTILE mobs (safety net for entity accumulation).
// Companion to anti_lag_items.js, which only handles @e[type=item]. This handles mobs —
// e.g. a loaded dungeon spawning faster than mobs despawn (see Bruh chunk [-4,66], 2026-07-09
// oversized entity chunk in docs/CONFIG_CHANGES.md).
//
// SAFE BY DESIGN:
//  * Whitelist only — culls entity types in #antilag:cullable_hostiles (common vanilla hostiles).
//    Anything NOT on that list is never touched: pets, villagers, animals, minecarts (incl. Lootr/
//    TNT dungeon loot), item frames, armor stands, paintings, boats, bosses, and every modded/horror
//    mob are all excluded by omission.
//  * Persistence-protected — nbt=!{PersistenceRequired:1b} spares name-tagged mobs, mobs that picked
//    up gear, spawner-persistent mobs, and raid mobs (all of which set PersistenceRequired).
//  * Warn-then-clear, throttled, and only above a high threshold — normal play never triggers it.
//    (This pack heavily suppresses natural hostile spawns, so cullable-hostile counts are normally low.)
//
// Tunable knobs:
const MOB_THRESHOLD    = 200   // cullable hostiles in a player's dimension before acting
const MOB_WARN_TICKS   = 200   // warning lead time before the cull (200 ticks = 10s)
const MOB_CHECK_INTERVAL = 300 // how often to count (300 ticks = ~15s); light on the tick
// selector shared by count + cull so they always agree; runs in the ticking player's dimension
const CULL_SELECTOR = '@e[type=#antilag:cullable_hostiles,nbt=!{PersistenceRequired:1b}]'

// module-level mutable state (`var` avoids the Rhino block-scope quirk seen in oc_interactions.js)
var oc_am_lastCheck = -1
var oc_am_clearAt = -1

PlayerEvents.tick(event => {
  var player = event.player
  if (!player) return
  var now
  try { now = Number(player.server.overworld().getGameTime()) } catch (e) { return }

  // 1) a cull is scheduled and now due -> do it
  if (oc_am_clearAt >= 0 && now >= oc_am_clearAt) {
    oc_am_clearAt = -1
    try {
      player.runCommandSilent('kill ' + CULL_SELECTOR)
      player.runCommandSilent('tellraw @a ' + JSON.stringify(
        { text: '[Cleanup] Culled excess hostile mobs to reduce lag.', color: 'gray', italic: true }))
    } catch (e) { console.error('[AntiLag] mob cull failed: ' + e) }
    return
  }
  if (oc_am_clearAt >= 0) return // waiting out the warning window

  // 2) throttled count check
  if (oc_am_lastCheck >= 0 && (now - oc_am_lastCheck) < MOB_CHECK_INTERVAL) return
  oc_am_lastCheck = now

  var count = 0
  try { count = player.runCommandSilent('execute if entity ' + CULL_SELECTOR) } catch (e) { return }

  // 3) over threshold -> warn now, schedule the cull
  if (count > MOB_THRESHOLD) {
    oc_am_clearAt = now + MOB_WARN_TICKS
    try {
      player.runCommandSilent('tellraw @a ' + JSON.stringify(
        { text: '[Cleanup] ' + count + ' hostile mobs crowding this dimension — culling in '
                + Math.round(MOB_WARN_TICKS / 20) + 's to reduce lag. (Named / tamed / persistent mobs are safe.)',
          color: 'yellow' }))
    } catch (e) {}
  }
})
