package spookydookie.tickfixes;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.raid.Raider;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Activation range (docs/SERVER_TICK_AUDIT_2026-11-06.md §5.5.1). Mobs further than `range` blocks from every player
 * in their level run their tick only one tick in `interval`, staggered by entity id so each tick handles an even
 * share. A skipped tick is skipped entirely (the mob lives in slow motion, including its own timers), except:
 * AgeableMob age advances as vanilla's aiStep would (growth and breeding cooldowns keep normal pace), and the vanilla
 * despawn check still runs (despawn rates unchanged).
 *
 * Never throttled: non-Mob entities; mobs with a target, recently hurt, burning, in lava, leashed, riding or ridden,
 * or in an active raid; entity types or namespaces listed in the config (MineColonies, guards, scripted horror mobs,
 * bosses). Adaptive mode throttles only while the server's average tick is above enableAboveMspt and stops once it
 * drops below disableBelowMspt.
 */
public final class ActivationRange {
    private static volatile boolean active;
    private static volatile long lastSkipped;
    private static final LongAdder SKIPPED = new LongAdder();
    private static final Map<EntityType<?>, Boolean> ALWAYS = new ConcurrentHashMap<>();
    private static volatile Set<String> alwaysIds = Set.of();
    private static volatile Set<String> alwaysNamespaces = Set.of();

    static void reloadLists(List<? extends String> ids, List<? extends String> namespaces) {
        alwaysIds = new HashSet<>(ids);
        alwaysNamespaces = new HashSet<>(namespaces);
        ALWAYS.clear();
    }

    static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        lastSkipped = SKIPPED.sumThenReset();
        if (!TickFixesConfig.activationEnabled) { active = false; return; }
        if (!TickFixesConfig.activationAdaptive) { active = true; return; }
        MinecraftServer server = event.getServer();
        if (server == null) return;
        float mspt = server.m_129903_(); // average of the last 100 tick times
        if (!active && mspt > TickFixesConfig.activationEnableAboveMspt) active = true;
        else if (active && mspt < TickFixesConfig.activationDisableBelowMspt) active = false;
    }

    /** Called at HEAD of ServerLevel.tickNonPassenger. @return true to skip this tick for the entity. */
    public static boolean shouldSkip(ServerLevel level, Entity entity) {
        if (!active || !(entity instanceof Mob mob)) return false;
        int interval = TickFixesConfig.activationInterval;
        if (Math.floorMod(level.m_46467_() + entity.m_19879_(), interval) == 0) return false; // its turn
        if (nearPlayer(level, entity, TickFixesConfig.activationRangeBlocks)) return false;
        if (immune(mob)) return false;

        if (TickFixesConfig.activationAgeCatchUp && mob instanceof AgeableMob ageable && ageable.m_6084_()) {
            int age = ageable.m_146764_();
            if (age < 0) ageable.m_146762_(age + 1);
            else if (age > 0) ageable.m_146762_(age - 1);
        }
        if (TickFixesConfig.activationDespawnChecks && !mob.m_21532_()) {
            mob.m_6043_(); // vanilla despawn check (may discard the mob, exactly as its own tick would)
        }
        SKIPPED.increment();
        return true;
    }

    private static boolean nearPlayer(ServerLevel level, Entity entity, double range) {
        double r2 = range * range;
        double x = entity.m_20185_(), y = entity.m_20186_(), z = entity.m_20189_();
        for (ServerPlayer p : level.m_7654_().m_6846_().m_11314_()) {
            if (p.m_9236_() == level && p.m_20275_(x, y, z) <= r2) return true;
        }
        return false;
    }

    private static boolean immune(Mob mob) {
        if (mob.m_5448_() != null) return true;                       // has a target
        if (mob.f_20916_ > 0 || mob.m_21188_() != null) return true;  // hurt recently
        if (mob.m_6060_() || mob.m_20077_()) return true;              // burning / in lava
        if (mob.m_21523_() || mob.m_20159_() || mob.m_20160_()) return true; // leashed / riding / ridden
        if (mob instanceof Raider raider && raider.m_37885_() != null) return true; // in a raid
        return alwaysTick(mob.m_6095_());
    }

    private static boolean alwaysTick(EntityType<?> type) {
        Boolean cached = ALWAYS.get(type);
        if (cached != null) return cached;
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(type);
        boolean always = key != null && (alwaysIds.contains(key.toString()) || alwaysNamespaces.contains(key.m_135827_()));
        ALWAYS.put(type, always);
        return always;
    }

    static String stats() {
        return "activation range: enabled=" + TickFixesConfig.activationEnabled + " active=" + active
                + " adaptive=" + TickFixesConfig.activationAdaptive + " (on above " + TickFixesConfig.activationEnableAboveMspt
                + " ms, off below " + TickFixesConfig.activationDisableBelowMspt + " ms) range=" + TickFixesConfig.activationRangeBlocks
                + " interval=" + TickFixesConfig.activationInterval + " | far mobs skipped last tick: " + lastSkipped;
    }

    private ActivationRange() {}
}
