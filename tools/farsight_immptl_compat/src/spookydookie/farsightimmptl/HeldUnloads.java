package spookydookie.farsightimmptl;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.world.level.ChunkPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import qouteall.imm_ptl.core.ClientWorldLoader;
import qouteall.imm_ptl.core.chunk_loading.ImmPtlClientChunkMap;
import qouteall.imm_ptl.core.ducks.IEClientPlayNetworkHandler;

/**
 * Farsight's delayed unload, rebuilt for Immersive Portals' client chunk map.
 *
 * Farsight keeps chunks the server has forgotten by holding back the server's
 * {@code ClientboundForgetLevelChunkPacket} until the chunk is out of render distance. That logic
 * lives in Farsight's own chunk manager, which Immersive Portals replaces with
 * {@code ImmPtlClientChunkMap}. Immersive Portals' map already stores chunks at any distance, so
 * only the hold-and-release part is missing; this class supplies it.
 *
 * Only the dimension the player stands in is held. Portal views into other dimensions unload as
 * before. All methods run on the client main thread.
 */
public final class HeldUnloads {
    private static final Logger LOGGER = LogManager.getLogger(FarsightImmPtlCompat.MOD_ID);

    /** Upper bound on the hold radius whatever the render distance, to bound client memory. */
    static final int MAX_HOLD_RADIUS = Integer.getInteger("farsight_immptl_compat.maxHoldRadius", 32);
    /** One ring past the render distance, so the outermost visible sections still have neighbours to mesh against. */
    private static final int HOLD_MARGIN = 1;
    /** Hysteresis: a held chunk is only released once it is this many chunks past the hold radius. */
    private static final int RELEASE_MARGIN = 3;
    private static final int RELEASE_INTERVAL_TICKS = 10;
    private static final boolean DEBUG = Boolean.getBoolean("farsight_immptl_compat.debug");

    private static final Map<ClientLevel, Map<Long, ClientboundForgetLevelChunkPacket>> HELD = new IdentityHashMap<>();
    private static boolean replaying;
    private static int ticksSinceRelease;
    private static int ticksSinceReport;
    private static long heldTotal;
    private static long releasedTotal;
    private static int releaseFailures;

    private HeldUnloads() {
    }

    /** Called at the head of the forget-chunk handler. Returns true when the unload was held and must be skipped. */
    public static boolean tryHold(Minecraft mc, ClientLevel level, ClientboundForgetLevelChunkPacket packet) {
        if (replaying || level == null || !(level.m_7726_() instanceof ImmPtlClientChunkMap)) {
            return false;
        }
        LocalPlayer player = mc.f_91074_;
        if (player == null || player.m_9236_() != level) {
            return false; // another dimension, e.g. the far side of a portal
        }
        int x = packet.m_132149_();
        int z = packet.m_132152_();
        if (distance(player.m_146902_(), x, z) > holdRadius(mc) + HOLD_MARGIN) {
            return false;
        }
        HELD.computeIfAbsent(level, k -> new HashMap<>()).put(ChunkPos.m_45589_(x, z), packet);
        heldTotal++;
        return true;
    }

    /** The server sent the chunk again, so it is tracked once more and the held unload must not fire. */
    public static void cancelHold(ClientLevel level, int x, int z) {
        Map<Long, ClientboundForgetLevelChunkPacket> held = HELD.get(level);
        if (held != null) {
            held.remove(ChunkPos.m_45589_(x, z));
        }
    }

    /** End of every client tick: release held unloads that are now out of range or in a dimension the player left. */
    public static void tick(Minecraft mc) {
        if (DEBUG && ++ticksSinceReport >= 600) {
            ticksSinceReport = 0;
            int now = HELD.values().stream().mapToInt(Map::size).sum();
            LOGGER.info("[{}] held now {}, held total {}, released total {}", FarsightImmPtlCompat.MOD_ID, now, heldTotal, releasedTotal);
        }
        if (HELD.isEmpty() || ++ticksSinceRelease < RELEASE_INTERVAL_TICKS) {
            return;
        }
        ticksSinceRelease = 0;

        LocalPlayer player = mc.f_91074_;
        ClientPacketListener connection = mc.m_91403_();
        if (player == null || connection == null) {
            HELD.clear(); // disconnected: the levels and their chunks are gone with the connection
            return;
        }
        ChunkPos centre = player.m_146902_();
        int keepRadius = holdRadius(mc) + HOLD_MARGIN + RELEASE_MARGIN;

        Iterator<Map.Entry<ClientLevel, Map<Long, ClientboundForgetLevelChunkPacket>>> levels = HELD.entrySet().iterator();
        while (levels.hasNext()) {
            Map.Entry<ClientLevel, Map<Long, ClientboundForgetLevelChunkPacket>> entry = levels.next();
            ClientLevel level = entry.getKey();
            if (level != mc.f_91073_ && !ClientWorldLoader.getClientWorlds().contains(level)) {
                levels.remove(); // Immersive Portals discarded this client world, chunks included
                continue;
            }
            boolean playerHere = player.m_9236_() == level;
            Iterator<ClientboundForgetLevelChunkPacket> packets = entry.getValue().values().iterator();
            while (packets.hasNext()) {
                ClientboundForgetLevelChunkPacket packet = packets.next();
                if (playerHere && distance(centre, packet.m_132149_(), packet.m_132152_()) <= keepRadius) {
                    continue;
                }
                packets.remove();
                release(mc, connection, level, packet);
            }
            if (entry.getValue().isEmpty()) {
                levels.remove();
            }
        }
    }

    /** Run the held packet through the normal handler, so every mod hooking it sees the unload when it really happens. */
    private static void release(Minecraft mc, ClientPacketListener connection, ClientLevel level, ClientboundForgetLevelChunkPacket packet) {
        replaying = true;
        try {
            if (level == mc.f_91073_) {
                connection.m_5729_(packet);
            } else {
                // The player has left this dimension. Point the listener at it the way Immersive Portals'
                // own packet redirection does, handle the unload there, then point it back.
                IEClientPlayNetworkHandler listener = (IEClientPlayNetworkHandler) connection;
                ClientLevel original = mc.f_91073_;
                listener.ip_setWorld(level);
                try {
                    connection.m_5729_(packet);
                } finally {
                    listener.ip_setWorld(original);
                }
            }
            releasedTotal++;
        } catch (RuntimeException e) {
            if (releaseFailures++ < 5) {
                LOGGER.warn("[{}] could not release held unload of chunk {}, {}", FarsightImmPtlCompat.MOD_ID,
                        packet.m_132149_(), packet.m_132152_(), e);
            }
        } finally {
            replaying = false;
        }
    }

    private static int holdRadius(Minecraft mc) {
        return Math.min(mc.f_91066_.m_231984_().m_231551_(), MAX_HOLD_RADIUS);
    }

    private static int distance(ChunkPos centre, int x, int z) {
        return Math.max(Math.abs(x - centre.f_45578_), Math.abs(z - centre.f_45579_));
    }
}
