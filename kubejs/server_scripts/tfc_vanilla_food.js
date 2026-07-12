// === OldCiv: vanilla-equivalent TFC food definitions for ALL non-TFC foods ===
//
// WHY: TFC replaces the player's FoodData with TFCFoodData, whose eat() calls
// FoodCapability.get(stack) and RETURNS EARLY if the item has no TFC food
// definition (verified in 3.2.23 bytecode). So every modded/vanilla food
// restores ZERO hunger unless something gives it a definition.
//
// The old OpenLoader shim (TFCModdedFood) tried this with a catchall tag, but
// the tag referenced #forge:foods which no mod defines -> the whole tag failed
// to load (TagLoader ERROR in server log) -> the shim matched nothing. Deleted.
//
// THIS script instead iterates the item registry at datapack-load time and
// emits one TFC food def per edible item, using the item's own vanilla values:
//   hunger     = FoodProperties.getNutrition()
//   saturation = nutrition * saturationModifier * 2   (vanilla saturation gain)
// TFC applies defs as delegate.eat(hunger, saturation / (2 * hunger)), which
// adds exactly `hunger` food and `saturation` saturation -> vanilla-identical.
//
// TFC's own foods (tfc:*) keep their tuned defs (incl. the TFCFoodHunger tier
// pack); minecraft:pumpkin_pie + minecraft:rotten_flesh are skipped because
// TFC ships defs for those two. Runs on /reload as well as boot.

ServerEvents.highPriorityData(event => {
  var ForgeRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
  // items TFC already defines (bundled data/tfc/tfc/food_items) — do not shadow
  var SKIP = {
    'minecraft:pumpkin_pie': true,
    'minecraft:rotten_flesh': true
  }
  var count = 0
  var registry = ForgeRegistries.ITEMS
  registry.getValues().forEach(item => {
    try {
      var id = '' + registry.getKey(item)
      if (id.indexOf('tfc:') === 0) return // TFC foods: keep TFC's tuned defs
      if (SKIP[id]) return
      if (!item.isEdible()) return
      var fp = item.getFoodProperties()
      if (!fp) return
      var hunger = fp.getNutrition()
      var sat = Math.round(hunger * fp.getSaturationModifier() * 2 * 100) / 100
      event.addJson('tfc:tfc/food_items/kjs_' + id.replace(/[^a-z0-9_]/g, '_'), {
        ingredient: { item: id },
        hunger: hunger,
        saturation: sat
      })
      count++
    } catch (e) {
      console.warn('[OldCiv/Food] skipped item due to error: ' + e)
    }
  })
  console.info('[OldCiv/Food] generated vanilla-equivalent TFC food defs for ' + count + ' edible items')
})
