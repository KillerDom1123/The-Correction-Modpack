package spookydookie.tickfixes;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent;
import net.theobsessed.network.TheObsessedModVariables;

/**
 * The Obsessed calls PlayerVariables.syncPlayerVariables(player) after every variable write (83 call sites in
 * PlayerTickProcedure, 19 in PlayerTick2Procedure); each call serialises the whole variable set, including
 * several free-text fields, and sends it. The client simply replaces its copy, so only the last send in a tick
 * matters. Server-side calls are recorded here and sent once per player at the end of the server tick.
 */
public final class ObsessedSyncCoalescer {
    private static final Map<ServerPlayer, TheObsessedModVariables.PlayerVariables> PENDING = new ConcurrentHashMap<>();
    private static final ThreadLocal<Boolean> FLUSHING = ThreadLocal.withInitial(() -> Boolean.FALSE);

    /** @return true if the call was deferred (the caller must cancel the original send). */
    public static boolean intercept(Object variables, Entity entity) {
        if (!TickFixesConfig.obsessedSync || FLUSHING.get()) return false;
        if (!(entity instanceof ServerPlayer player)) return false;
        PENDING.put(player, (TheObsessedModVariables.PlayerVariables) variables);
        return true;
    }

    static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING.isEmpty()) return;
        FLUSHING.set(Boolean.TRUE);
        try {
            Iterator<Map.Entry<ServerPlayer, TheObsessedModVariables.PlayerVariables>> it = PENDING.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<ServerPlayer, TheObsessedModVariables.PlayerVariables> e = it.next();
                it.remove();
                ServerPlayer player = e.getKey();
                if (!player.m_9232_()) { // hasDisconnected
                    try {
                        e.getValue().syncPlayerVariables(player);
                    } catch (RuntimeException ex) {
                        SpookyTickFixes.LOGGER.warn("[spooky_tick_fixes] deferred The Obsessed sync failed: {}", ex.toString());
                    }
                }
            }
        } finally {
            FLUSHING.set(Boolean.FALSE);
        }
    }

    private ObsessedSyncCoalescer() {}
}
