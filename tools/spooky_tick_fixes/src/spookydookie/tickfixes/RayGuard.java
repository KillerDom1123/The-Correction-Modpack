package spookydookie.tickfixes;

import java.util.concurrent.atomic.LongAdder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import spookydookie.tickfixes.mixin.ClipContextAccessor;

/**
 * Ray casts made by the horror mods' per-player "is the player looking at it?" procedures (Creeping Woods, Rake,
 * Whispering Spirits, John, The Obsessed). Each runs every tick for every player: a loop of short rays along the
 * player's view, with three identical Level.clip calls per step (one per x/y/z coordinate it reads from the hit).
 *
 * 1. Loaded chunks only. Vanilla clip loads every chunk the ray touches, synchronously. A player who outruns chunk
 *    loading (sailing, flying) makes it load or even generate the chunk ahead on the server thread; one such tick
 *    took 18.9 s (2026-11-06). If any chunk the ray spans is not loaded, the guarded call returns vanilla's own
 *    "miss" result instead. The procedures only look for their creature near the hit point, and entities exist only
 *    in loaded chunks, so the answer cannot change.
 * 2. Duplicates. Within one procedure run, a cast identical to the previous one (same level, end points and
 *    block/fluid modes) returns the previous result. Nothing in between changes the world. The memo is cleared at
 *    the start of every guarded procedure run.
 *
 * Server levels only; client-side calls go straight to vanilla.
 */
public final class RayGuard {
    static final LongAdder CASTS = new LongAdder();
    static final LongAdder DEDUPED = new LongAdder();
    static final LongAdder STOPPED = new LongAdder();
    private static final double EDGE = 1.0E-3D; // vanilla nudges the ray ends by 1e-7; look slightly wider

    private static final class Memo {
        Level level;
        Vec3 from, to;
        Object block, fluid;
        BlockHitResult result;
    }

    private static final ThreadLocal<Memo> MEMO = ThreadLocal.withInitial(Memo::new);

    /** Called at the start of each guarded procedure run. */
    public static void begin() {
        MEMO.get().result = null;
    }

    public static BlockHitResult clip(Level level, ClipContext ctx) {
        boolean loadedOnly = TickFixesConfig.rayLoadedOnly, dedupe = TickFixesConfig.rayDedupe;
        if (!(level instanceof ServerLevel) || !(loadedOnly || dedupe)) return level.m_45547_(ctx);
        CASTS.increment();
        Vec3 from = ctx.m_45702_(), to = ctx.m_45693_();
        ClipContextAccessor acc = (ClipContextAccessor) ctx;
        Object block = acc.spookyTickFixes$block(), fluid = acc.spookyTickFixes$fluid();
        Memo memo = MEMO.get();
        if (dedupe && memo.result != null && memo.level == level && memo.block == block && memo.fluid == fluid
                && from.equals(memo.from) && to.equals(memo.to)) {
            DEDUPED.increment();
            return memo.result;
        }
        BlockHitResult result;
        if (loadedOnly && !spanLoaded(level, from, to)) {
            STOPPED.increment();
            result = miss(from, to);
        } else {
            result = level.m_45547_(ctx);
        }
        if (dedupe) {
            memo.level = level; memo.from = from; memo.to = to; memo.block = block; memo.fluid = fluid; memo.result = result;
        }
        return result;
    }

    /** True if every chunk in the ray's x/z bounding box is loaded (hasChunk never loads one). */
    private static boolean spanLoaded(Level level, Vec3 a, Vec3 b) {
        int x0 = (int) Math.floor(Math.min(a.f_82479_, b.f_82479_) - EDGE) >> 4;
        int x1 = (int) Math.floor(Math.max(a.f_82479_, b.f_82479_) + EDGE) >> 4;
        int z0 = (int) Math.floor(Math.min(a.f_82481_, b.f_82481_) - EDGE) >> 4;
        int z1 = (int) Math.floor(Math.max(a.f_82481_, b.f_82481_) + EDGE) >> 4;
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                if (!level.m_7232_(x, z)) return false;
            }
        }
        return true;
    }

    /** Exactly what vanilla BlockGetter.clip returns when the ray hits nothing. */
    private static BlockHitResult miss(Vec3 from, Vec3 to) {
        Vec3 d = from.m_82546_(to);
        return BlockHitResult.m_82426_(to, Direction.m_122366_(d.f_82479_, d.f_82480_, d.f_82481_), BlockPos.m_274446_(to));
    }

    static String stats() {
        return "horror ray casts: " + CASTS.sum() + " guarded, " + DEDUPED.sum() + " duplicates reused, "
                + STOPPED.sum() + " stopped at unloaded chunks (since start)";
    }

    private RayGuard() {}
}
