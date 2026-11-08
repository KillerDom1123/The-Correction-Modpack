package spookydookie.tickfixes;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * What counts as a player's base for the sanity-bound horror rules (1.5.0, {@code [sanityHorror]}):
 * <ul>
 *   <li>within {@code baseRadius} blocks horizontally and {@code baseVerticalRange} blocks vertically of an online
 *       player's respawn point (bed or respawn anchor) in that dimension;</li>
 *   <li>with {@code baseColonies}, inside a MineColonies colony's claimed chunks, within {@code baseVerticalRange} of the
 *       colony centre's height (so mines far below a colony still count as caves).</li>
 * </ul>
 * MineColonies' colony lookup loads the chunk it asks about, so a chunk that isn't loaded counts as "no colony". Nothing
 * can spawn in an unloaded chunk anyway. Offline players' beds are not known, but their colonies still count.
 */
public final class HorrorBases {
    private static boolean colonyWarned;

    private static final class Colonies {
        static final MethodHandle INSTANCE;
        static final MethodHandle AT;
        static final MethodHandle CENTER;

        static {
            MethodHandle instance = null, at = null, center = null;
            try {
                Class<?> manager = Class.forName("com.minecolonies.api.colony.IColonyManager");
                Class<?> colony = Class.forName("com.minecolonies.api.colony.IColony");
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                instance = lookup.findStatic(manager, "getInstance", MethodType.methodType(manager));
                at = lookup.findVirtual(manager, "getColonyByPosFromWorld", MethodType.methodType(colony, Level.class, BlockPos.class));
                center = lookup.findVirtual(colony, "getCenter", MethodType.methodType(BlockPos.class));
            } catch (Throwable t) {
                instance = at = center = null; // MineColonies not installed (or its API changed): beds only
            }
            INSTANCE = instance;
            AT = at;
            CENTER = center;
        }
    }

    /** The respawn point whose base contains pos, or null. */
    static BlockPos bedAt(ServerLevel level, BlockPos pos) {
        int r = TickFixesConfig.baseRadius;
        if (r <= 0) return null;
        long r2 = (long) r * r;
        int v = TickFixesConfig.baseVerticalRange;
        for (ServerPlayer p : level.m_7654_().m_6846_().m_11314_()) {
            BlockPos bed = p.m_8961_();
            if (bed == null || !level.m_46472_().equals(p.m_8963_())) continue;
            long dx = pos.m_123341_() - bed.m_123341_();
            long dz = pos.m_123343_() - bed.m_123343_();
            if (dx * dx + dz * dz <= r2 && Math.abs(pos.m_123342_() - bed.m_123342_()) <= v) return bed;
        }
        return null;
    }

    /** The centre of the colony whose claim contains pos (near its height), or null. Loaded chunks only. */
    static BlockPos colonyAt(ServerLevel level, BlockPos pos) {
        if (!TickFixesConfig.baseColonies || Colonies.AT == null) return null;
        if (level.m_7726_().m_7131_(pos.m_123341_() >> 4, pos.m_123343_() >> 4) == null) return null;
        try {
            Object colony = Colonies.AT.invoke(Colonies.INSTANCE.invoke(), (Level) level, pos);
            if (colony == null) return null;
            BlockPos center = (BlockPos) Colonies.CENTER.invoke(colony);
            return center != null && Math.abs(pos.m_123342_() - center.m_123342_()) <= TickFixesConfig.baseVerticalRange ? center : null;
        } catch (Throwable t) {
            if (!colonyWarned) {
                colonyWarned = true;
                SpookyTickFixes.LOGGER.warn("[{}] MineColonies colony lookup failed ({}); only beds count as bases", SpookyTickFixes.MOD_ID, t.toString());
            }
            return null;
        }
    }

    public static boolean isInBase(ServerLevel level, BlockPos pos) {
        return bedAt(level, pos) != null || colonyAt(level, pos) != null;
    }

    /** For /tickfixes horror. */
    static String describe(ServerLevel level, BlockPos pos) {
        BlockPos bed = bedAt(level, pos);
        if (bed != null) return "in a base (respawn point at " + bed.m_123341_() + " " + bed.m_123342_() + " " + bed.m_123343_() + ")";
        BlockPos colony = colonyAt(level, pos);
        if (colony != null) return "in a base (colony centred at " + colony.m_123341_() + " " + colony.m_123342_() + " " + colony.m_123343_() + ")";
        return "not in a base";
    }

    /**
     * A spot outside every base, in 8-block steps from pos straight away from the base's centre (a respawn point or a
     * colony centre), or away from the player when pos sits on that centre. Gives up after 192 blocks. Returns {x, z}.
     */
    static double[] pushOut(ServerLevel level, BlockPos pos, Entity player) {
        BlockPos center = bedAt(level, pos);
        if (center == null) center = colonyAt(level, pos);
        double cx = center != null ? center.m_123341_() + 0.5D : player.m_20185_();
        double cz = center != null ? center.m_123343_() + 0.5D : player.m_20189_();
        double x = pos.m_123341_() + 0.5D;
        double z = pos.m_123343_() + 0.5D;
        double dx = x - cx;
        double dz = z - cz;
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 1.0D) {
            double a = ThreadLocalRandom.current().nextDouble(Math.PI * 2.0D);
            dx = Math.cos(a);
            dz = Math.sin(a);
            len = 1.0D;
        }
        dx /= len;
        dz /= len;
        for (int i = 0; i < 24; i++) {
            x += dx * 8.0D;
            z += dz * 8.0D;
            if (!isInBase(level, BlockPos.m_274561_(x, pos.m_123342_(), z))) break;
        }
        return new double[] {Math.floor(x), Math.floor(z)};
    }

    private HorrorBases() {}
}
