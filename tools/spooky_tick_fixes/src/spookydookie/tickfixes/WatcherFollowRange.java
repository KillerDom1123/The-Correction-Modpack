package spookydookie.tickfixes;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * The One Who Watches hardcodes FOLLOW_RANGE 2048 on its watchers. A vanilla path search may visit
 * follow_range x 16 nodes (fixed when the mob is constructed) inside a +/- follow_range region, and the melee
 * goal re-tries it every 20 ticks per watcher. The type default is lowered (so newly constructed watchers get a
 * small node budget) and saved watchers, whose NBT still carries 2048, are clamped when they join a level.
 * Chase variants (hunting, running, stalking, stare) are left alone.
 */
final class WatcherFollowRange {
    private static final String NAMESPACE = "the_one_who_watches";
    private static final String[] IDS = {"toww_hanging", "toww_staring", "toww_crawling", "toww_hallucination", "toww_delusion", "toww_looked_at"};
    private static volatile Set<EntityType<?>> types;

    private static Set<EntityType<?>> types() {
        Set<EntityType<?>> t = types;
        if (t == null) {
            Set<EntityType<?>> s = Collections.newSetFromMap(new IdentityHashMap<>());
            for (String id : IDS) {
                ResourceLocation rl = new ResourceLocation(NAMESPACE, id);
                // ENTITY_TYPES is a defaulted registry (getValue falls back to pig), so check the key first.
                if (ForgeRegistries.ENTITY_TYPES.containsKey(rl)) s.add(ForgeRegistries.ENTITY_TYPES.getValue(rl));
            }
            types = t = s;
        }
        return t;
    }

    @SuppressWarnings("unchecked")
    static void onAttributeModification(EntityAttributeModificationEvent event) {
        // May run before the config file is loaded, so the default cap applies at type level; the join clamp
        // below uses the configured value.
        int n = 0;
        for (EntityType<?> type : types()) {
            event.add((EntityType<? extends LivingEntity>) type, Attributes.f_22277_, TickFixesConfig.watcherFollowRangeCap);
            n++;
        }
        if (n > 0) SpookyTickFixes.LOGGER.info("[spooky_tick_fixes] watcher follow range: default set to {} for {} TOWW entity types", TickFixesConfig.watcherFollowRangeCap, n);
    }

    static void onJoin(EntityJoinLevelEvent event) {
        if (!TickFixesConfig.watcherFollowRange || event.getLevel().f_46443_) return;
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity living) || !types().contains(entity.m_6095_())) return;
        AttributeInstance range = living.m_21051_(Attributes.f_22277_);
        double cap = TickFixesConfig.watcherFollowRangeCap;
        if (range != null && range.m_22115_() > cap) range.m_22100_(cap);
    }

    private WatcherFollowRange() {}
}
