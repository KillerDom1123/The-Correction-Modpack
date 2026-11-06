package spookydookie.tickfixes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;

/**
 * Per-level counts of entity kinds whose absence makes other mods' per-mob work a no-op:
 * Immersive Portals portals (Full Immersion) and Alex's Caves moving blocks. Classes are matched by name, so
 * there is no compile-time dependency on either mod.
 *
 * The counts only ever err towards "present" (fail-open): joins are counted at HIGHEST priority, so a join that
 * another listener later cancels over-counts, and a count never goes below zero.
 */
public final class EntityCounters {
    private static final int NONE = 0, PORTAL = 1, MOVING_BLOCK = 2;
    private static final Map<Level, AtomicInteger> PORTALS = new ConcurrentHashMap<>();
    private static final Map<Level, AtomicInteger> MOVING_BLOCKS = new ConcurrentHashMap<>();

    private static final ClassValue<Integer> KIND = new ClassValue<>() {
        @Override
        protected Integer computeValue(Class<?> type) {
            for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
                String n = c.getName();
                if (n.equals("qouteall.imm_ptl.core.portal.Portal")) return PORTAL;
                if (n.equals("com.github.alexmodguy.alexscaves.server.entity.item.AbstractMovingBlockEntity")) return MOVING_BLOCK;
            }
            return NONE;
        }
    };

    private static Map<Level, AtomicInteger> mapFor(Entity entity) {
        int kind = KIND.get(entity.getClass());
        return kind == PORTAL ? PORTALS : kind == MOVING_BLOCK ? MOVING_BLOCKS : null;
    }

    static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().f_46443_) return;
        Map<Level, AtomicInteger> map = mapFor(event.getEntity());
        if (map != null) map.computeIfAbsent(event.getLevel(), l -> new AtomicInteger()).incrementAndGet();
    }

    static void onLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel().f_46443_) return;
        Map<Level, AtomicInteger> map = mapFor(event.getEntity());
        if (map == null) return;
        AtomicInteger c = map.get(event.getLevel());
        if (c != null) c.updateAndGet(v -> Math.max(0, v - 1));
    }

    static void clear() {
        PORTALS.clear();
        MOVING_BLOCKS.clear();
    }

    public static boolean noPortals(Level level) {
        AtomicInteger c = PORTALS.get(level);
        return c == null || c.get() <= 0;
    }

    public static boolean noMovingBlocks(Level level) {
        AtomicInteger c = MOVING_BLOCKS.get(level);
        return c == null || c.get() <= 0;
    }

    static String summary(Level level) {
        AtomicInteger p = PORTALS.get(level), m = MOVING_BLOCKS.get(level);
        return "portals=" + (p == null ? 0 : p.get()) + " acMovingBlocks=" + (m == null ? 0 : m.get());
    }

    private EntityCounters() {}
}
