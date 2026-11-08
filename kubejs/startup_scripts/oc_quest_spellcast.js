// priority: 0
// Old Civilisation — quest check for The Other Science / "A Different Kind of Power".
// Casting an Ars Nouveau spell adds the player tag oc_q_cast_spell, which the quest's stage task reads
// (see server_scripts/oc_quest_checks.js for how tags act as stages). ForgeEvents only exists in startup scripts.
// SpellCastEvent is a LivingEvent posted on the server when a spell resolves its cast; the caster is getEntity().

ForgeEvents.onEvent('com.hollingsworth.arsnouveau.api.event.SpellCastEvent', event => {
  try {
    var caster = event.getEntity()
    if (!caster || !caster.isPlayer() || caster.level.isClientSide()) return
    if (!caster.getTags().contains('oc_q_cast_spell')) {
      caster.addTag('oc_q_cast_spell')
      console.info('[OldCiv/Quests] ' + caster.username + ' -> oc_q_cast_spell')
    }
  } catch (e) { console.warn('[OldCiv/Quests] spell cast check failed: ' + e) }
})
