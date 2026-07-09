// priority: 100
// Anti-lag: automatic dropped-item cleanup.
// A Spark profile of a fresh world showed ~2400 `minecraft:item` entities on the ground
// dominating the server tick (each loose item ticks every tick). This culls them ONLY when
// they pile up past a threshold, after a visible warning, so normal play is never affected.
//
// Tunable knobs:
const ITEM_THRESHOLD   = 400   // only act when loose items in a player's dimension exceed this
const WARN_TICKS       = 200   // warning lead time before the clear (200 ticks = 10s)
const CHECK_INTERVAL   = 300   // how often to count (300 ticks = ~15s); light on the tick
// NBT notes: `execute if entity <selector>` returns the match count; item entities live per-dimension,
// so the count/clear run in the ticking player's dimension (covers wherever players actually are).

// module-level mutable state (loaded once; `var` avoids the Rhino block-scope quirk seen in oc_interactions.js)
var oc_al_lastCheck = -1
var oc_al_clearAt = -1

PlayerEvents.tick(event => {
  var player = event.player
  if (!player) return
  var now
  try { now = Number(player.server.overworld().getGameTime()) } catch (e) { return }

  // 1) a clear is scheduled and now due -> do it
  if (oc_al_clearAt >= 0 && now >= oc_al_clearAt) {
    oc_al_clearAt = -1
    try {
      var removed = player.runCommandSilent('kill @e[type=item]')
      player.runCommandSilent('tellraw @a ' + JSON.stringify(
        { text: '[Cleanup] Cleared dropped items to reduce lag.', color: 'gray', italic: true }))
    } catch (e) { console.error('[AntiLag] clear failed: ' + e) }
    return
  }
  if (oc_al_clearAt >= 0) return // waiting out the warning window

  // 2) throttled count check
  if (oc_al_lastCheck >= 0 && (now - oc_al_lastCheck) < CHECK_INTERVAL) return
  oc_al_lastCheck = now

  var count = 0
  try { count = player.runCommandSilent('execute if entity @e[type=item]') } catch (e) { return }

  // 3) over threshold -> warn now, schedule the clear
  if (count > ITEM_THRESHOLD) {
    oc_al_clearAt = now + WARN_TICKS
    try {
      player.runCommandSilent('tellraw @a ' + JSON.stringify(
        { text: '[Cleanup] ' + count + ' dropped items on the ground — clearing in '
                + Math.round(WARN_TICKS / 20) + 's to reduce lag. Grab anything you want to keep.',
          color: 'yellow' }))
    } catch (e) {}
  }
})
