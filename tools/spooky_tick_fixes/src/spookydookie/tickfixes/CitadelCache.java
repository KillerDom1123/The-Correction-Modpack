package spookydookie.tickfixes;

import com.github.alexthe666.citadel.server.tick.ServerTickRateTracker;
import com.github.alexthe666.citadel.server.world.CitadelServerData;
import net.minecraft.server.MinecraftServer;

/**
 * Citadel's ServerProxy.canEntityTickServer runs for every entity tick and calls ServerTickRateTracker.getForServer,
 * which looks the CitadelServerData saved data up in the overworld's data storage (and marks it dirty) every time.
 * That data, and the tracker it lazily creates, are the same objects for the whole server lifetime, so they are
 * cached per server. The cached path still marks the data dirty, so persistence is unchanged.
 */
public final class CitadelCache {
    private record Entry(MinecraftServer server, CitadelServerData data, ServerTickRateTracker tracker) {}

    private static volatile Entry entry;

    public static ServerTickRateTracker get(MinecraftServer server) {
        if (!TickFixesConfig.citadelCache || server == null) return null;
        Entry e = entry;
        if (e == null || e.server != server) {
            CitadelServerData data = CitadelServerData.get(server);
            ServerTickRateTracker tracker = data.getOrCreateTickRateTracker();
            e = new Entry(server, data, tracker);
            entry = e;
            return tracker;
        }
        e.data.m_77762_(); // setDirty(), exactly as CitadelServerData.get() does on every call
        return e.tracker;
    }

    static void clear() {
        entry = null;
    }

    private CitadelCache() {}
}
