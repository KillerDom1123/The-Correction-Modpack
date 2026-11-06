package spookydookie.tickfixes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Farm & Charm sends a SyncSaturationPacket for every cow, pig, sheep and chicken, every tick, to every player in
 * the dimension. The values (saturation level and food counter) only change on a 20-tick check or a long random
 * decay. Sends are let through when the value changed, when farmCharmRefreshTicks have passed, or after a player
 * started tracking the animal (so newly arrived clients get the current value).
 */
public final class SaturationSyncFilter {
    /** entity id -> {packed state, game time of last send} */
    private static final Map<Integer, long[]> LAST = new ConcurrentHashMap<>();

    public static boolean shouldSend(Entity entity, int level, int foodCounter) {
        if (!TickFixesConfig.farmCharmSync) return true;
        long state = ((long) level << 32) | (foodCounter & 0xFFFFFFFFL);
        long now = entity.m_9236_().m_46467_();
        Integer id = entity.m_19879_();
        long[] prev = LAST.get(id);
        if (prev != null && prev[0] == state && now >= prev[1] && now - prev[1] < TickFixesConfig.farmCharmRefreshTicks) {
            return false;
        }
        LAST.put(id, new long[]{state, now});
        return true;
    }

    static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() != null) LAST.remove(event.getTarget().m_19879_());
    }

    static void onLeave(EntityLeaveLevelEvent event) {
        if (!event.getLevel().f_46443_) LAST.remove(event.getEntity().m_19879_());
    }

    private SaturationSyncFilter() {}
}
