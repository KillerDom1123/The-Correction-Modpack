package spookydookie.tickfixes;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Single-block reads that would otherwise make the server thread load, or even generate, a chunk (1.3.3).
 * Also a diagnostic log of chunk requests made from other threads, which vanilla runs on the server thread
 * between ticks.
 *
 * "Loaded" here means ServerChunkCache.getChunkNow returns a chunk: a finished chunk, read without waiting.
 */
public final class ChunkGuards {
    private static final Logger LOGGER = LogManager.getLogger("spooky_tick_fixes");

    static final LongAdder VILLAGER_SLEEP_SKIPS = new LongAdder();
    static final LongAdder OBSESSED_READS_STUBBED = new LongAdder();

    static boolean loaded(ServerLevel level, BlockPos pos) {
        return level.m_7726_().m_7131_(pos.m_123341_() >> 4, pos.m_123343_() >> 4) != null;
    }

    /**
     * Vanilla SleepInBed.checkExtraStartConditions reads the block at the villager's home before checking that the
     * home is within 2 blocks. A villager only ticks in an entity-ticking chunk, and every chunk next to one is loaded,
     * so a home in an unloaded chunk is always further than 2 blocks away and the check returns false anyway. Air
     * gives the same false without loading the chunk. On 2026-11-07 at 01:44 this read waited 120 s for a chunk that
     * still had to be generated, and the watchdog stopped the server.
     */
    public static BlockState villagerBedState(ServerLevel level, BlockPos pos) {
        if (TickFixesConfig.villagerSleepLoadedOnly && !loaded(level, pos)) {
            VILLAGER_SLEEP_SKIPS.increment();
            return Blocks.f_50016_.m_49966_();
        }
        return level.m_8055_(pos);
    }

    /**
     * The Obsessed's player tick: isEmptyBlock in an unloaded chunk answers "not empty". Its stalker spawn search
     * reads at a stored scan point 37 to 80 blocks away, or further after a teleport. Every check that acts on the
     * answer needs "empty" (space to spawn), or comes after such a check on the same column. So "not empty" sends it
     * down its own "not enough space, move on" path.
     */
    public static boolean obsessedIsEmptyBlock(LevelAccessor world, BlockPos pos) {
        if (TickFixesConfig.obsessedReadsLoadedOnly && world instanceof ServerLevel level && !loaded(level, pos)) {
            OBSESSED_READS_STUBBED.increment();
            return false;
        }
        return world.m_46859_(pos);
    }

    /** Same, for getBlockState. Air: every check on the result looks for a specific block (cobweb, cobweb stone, glass). */
    public static BlockState obsessedBlockState(LevelAccessor world, BlockPos pos) {
        if (TickFixesConfig.obsessedReadsLoadedOnly && world instanceof ServerLevel level && !loaded(level, pos)) {
            OBSESSED_READS_STUBBED.increment();
            return Blocks.f_50016_.m_49966_();
        }
        return world.m_8055_(pos);
    }

    // ---- off-thread chunk requests (diagnostic) ------------------------------------------------------------------

    private static final AtomicLong OFF_THREAD_CALLS = new AtomicLong();
    private static final Map<String, LongAdder> OFF_THREAD_BY_CALLER = new ConcurrentHashMap<>();
    private static final int MAX_CALLERS = 64;
    private static final int LOGGED_FRAMES = 30;
    private static final StackWalker WALKER = StackWalker.getInstance();
    private static final String[] INFRA = {
            "java.", "jdk.", "sun.", "net.minecraft.", "com.mojang.", "net.minecraftforge.", "cpw.", "org.spongepowered.",
            "it.unimi.", "com.google.", "io.netty.", "spookydookie.tickfixes.", "com.abdelaziz.canary.", "org.embeddedt.modernfix."};

    /**
     * Called at the head of ServerChunkCache.getChunk when the calling thread isn't the server thread. Vanilla then
     * runs the request on the server thread, in the gap between ticks, and the calling thread waits. A request for a
     * chunk that still has to be generated holds up the next tick for seconds. The first request from each distinct
     * caller is logged with its stack; the rest are only counted. After the first 2000 requests, 1 in 64 is sampled.
     */
    public static void offThreadChunkRequest(int x, int z, ChunkStatus status, boolean load) {
        if (!TickFixesConfig.offThreadChunkLog) return;
        long n = OFF_THREAD_CALLS.incrementAndGet();
        if (n > 2000 && (n & 63) != 0) return;
        List<StackWalker.StackFrame> frames = WALKER.walk(s -> s.limit(64).collect(Collectors.toList()));
        String key = Thread.currentThread().getName().replaceAll("[0-9]+", "#") + " <- " + caller(frames);
        LongAdder count = OFF_THREAD_BY_CALLER.get(key);
        if (count == null) {
            if (OFF_THREAD_BY_CALLER.size() >= MAX_CALLERS) return;
            LongAdder fresh = new LongAdder();
            count = OFF_THREAD_BY_CALLER.putIfAbsent(key, fresh);
            if (count == null) {
                count = fresh;
                String stack = frames.stream().limit(LOGGED_FRAMES).map(f -> f.toStackTraceElement().toString())
                        .collect(Collectors.joining("\n\tat "));
                LOGGER.warn("[spooky_tick_fixes] off-thread chunk request (new caller): thread '{}' asked for chunk [{}, {}] status {} load={}; caller {}\n\tat {}",
                        Thread.currentThread().getName(), x, z, status, load, key, stack);
            }
        }
        count.increment();
    }

    /** The first frame outside Minecraft, Forge, the JDK and the chunk-system mods, or a mixin handler merged into a vanilla class. */
    private static String caller(List<StackWalker.StackFrame> frames) {
        for (StackWalker.StackFrame f : frames) {
            String cls = f.getClassName(), m = f.getMethodName();
            if (cls.startsWith("spookydookie.tickfixes.")) continue;
            boolean merged = m.startsWith("handler$") || m.startsWith("redirect$") || m.startsWith("wrapOperation$")
                    || m.startsWith("modify$") || m.startsWith("localvar$");
            if (merged && !m.contains("spookyTickFixes")) return cls + "." + m;
            boolean infra = false;
            for (String p : INFRA) if (cls.startsWith(p)) { infra = true; break; }
            if (!infra) return cls + "." + m;
        }
        return "<vanilla only> " + (frames.size() > 4 ? frames.get(4).getClassName() + "." + frames.get(4).getMethodName() : "?");
    }

    static String stats() {
        String callers = OFF_THREAD_BY_CALLER.entrySet().stream()
                .sorted(Comparator.comparingLong((Map.Entry<String, LongAdder> e) -> e.getValue().sum()).reversed())
                .limit(3).map(e -> e.getKey() + "=" + e.getValue().sum()).collect(Collectors.joining(", "));
        return "villager bed reads in unloaded chunks: " + VILLAGER_SLEEP_SKIPS.sum()
                + "; Obsessed block reads in unloaded chunks: " + OBSESSED_READS_STUBBED.sum()
                + "; off-thread chunk requests: " + OFF_THREAD_CALLS.get()
                + (callers.isEmpty() ? "" : " (top callers, sampled: " + callers + ")");
    }

    private ChunkGuards() {}
}
