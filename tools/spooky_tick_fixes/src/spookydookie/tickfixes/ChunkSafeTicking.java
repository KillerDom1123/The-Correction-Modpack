package spookydookie.tickfixes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.visitors.CollectFields;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spookydookie.tickfixes.mixin.ChunkMapAccessor;

/**
 * 1.3.4, "chunk-safe ticking": while a dimension ticks, block and fluid reads on the server thread never make it
 * generate terrain.
 *
 * Every watchdog crash and multi-second stall of 2026-11-06/07 had one cause: code running in a level's tick (a mob's AI,
 * a player-tick procedure, a fluid update, a kinetic network) read a block in a chunk that didn't exist yet. Vanilla then
 * generates that chunk on the spot, which on the VM takes seconds to minutes. Level.getBlockState / getFluidState fetch
 * the chunk through Level.getChunk(x, z) / getChunkAt(pos); LevelReadGuardMixin routes those two calls here.
 *
 * During a level's tick (Forge LevelTickEvent START to END) on the server thread:
 *   - chunk loaded (getChunkNow)                         -> unchanged
 *   - chunk in memory and finished, only not accessible   -> vanilla (it just gets a ticket, nothing is generated)
 *   - chunk finished on disk ("Status" = full)            -> vanilla (a disk load, milliseconds)
 *   - anything else: not generated, half generated, or being generated right now
 *                                                         -> a barrier block / no fluid, at once
 * A barrier rather than air, so nothing spawns, places blocks or flows into terrain that doesn't exist, and ray casts
 * stop there. Outside a level's tick (player packets, commands, worldgen, other threads) and while the server thread is
 * running chunk tasks (chunk post-processing), nothing changes. In practice the only difference from vanilla is that
 * ungenerated terrain is never generated just because something ticking looked at it.
 */
public final class ChunkSafeTicking {
    private static final Logger LOGGER = LogManager.getLogger("spooky_tick_fixes");

    // ---- context (server thread only) ---------------------------------------------------------------------------
    private static Level active;
    private static Thread serverThread;
    private static int suspended;

    // 4-entry cache of loaded chunks for the active level, cleared at every tick start/end and on chunk unload.
    private static final long[] cacheKeys = new long[4];
    private static final LevelChunk[] cacheChunks = new LevelChunk[4];
    private static int cacheNext;

    // ---- counters ------------------------------------------------------------------------------------------------
    static final LongAdder BARRIERS = new LongAdder();
    static final LongAdder MEMORY_LOADS = new LongAdder();
    static final LongAdder DISK_LOADS = new LongAdder();
    static final LongAdder UNKNOWN = new LongAdder();
    private static final Map<String, LongAdder> CALLERS = new ConcurrentHashMap<>();

    // Per level: chunk key -> decision expiry (ms, sign = verdict). Positive = finished on disk, negative = not.
    private static final Map<Level, Long2LongOpenHashMap> DISK_VERDICTS = new WeakHashMap<>();
    private static final long VERDICT_MS = 60_000L, UNKNOWN_MS = 5_000L;
    private static final Map<Level, LevelChunk> BARRIER_CHUNKS = new WeakHashMap<>();
    private static final StackWalker WALKER = StackWalker.getInstance();

    /** What the last guarded read did, for /tickfixes probe. */
    static String lastPath = "";

    // ---- lifecycle -----------------------------------------------------------------------------------------------

    static void begin(ServerLevel level) {
        active = TickFixesConfig.chunkSafeTicking ? level : null;
        serverThread = Thread.currentThread();
        suspended = 0;
        clearCache();
        Probe.runIfPending(level);
    }

    static void end() {
        active = null;
        clearCache();
    }

    static void onChunkUnload() {
        clearCache();
    }

    static void clearAll() {
        active = null;
        clearCache();
        synchronized (DISK_VERDICTS) { DISK_VERDICTS.clear(); }
        synchronized (BARRIER_CHUNKS) { BARRIER_CHUNKS.clear(); }
    }

    private static void clearCache() {
        for (int i = 0; i < 4; i++) { cacheKeys[i] = Long.MIN_VALUE; cacheChunks[i] = null; }
    }

    /** Main-thread chunk tasks (promotion to full, post-processing) run with the guard off. */
    public static void suspend() { if (Thread.currentThread() == serverThread) suspended++; }
    public static void resume() { if (Thread.currentThread() == serverThread && suspended > 0) suspended--; }

    // ---- the hot path --------------------------------------------------------------------------------------------

    /** Replaces the chunk lookup inside Level.getBlockState / getFluidState. */
    public static LevelChunk chunkForRead(Level level, int cx, int cz) {
        if (level != active || Thread.currentThread() != serverThread) return level.m_6325_(cx, cz);
        long key = ChunkPos.m_45589_(cx, cz);
        for (int i = 0; i < 4; i++) if (cacheKeys[i] == key) return cacheChunks[i];
        LevelChunk loaded = ((ServerLevel) level).m_7726_().m_7131_(cx, cz);
        if (loaded != null) {
            cacheKeys[cacheNext] = key;
            cacheChunks[cacheNext] = loaded;
            cacheNext = (cacheNext + 1) & 3;
            return loaded;
        }
        return slow((ServerLevel) level, cx, cz, key);
    }

    /** Replaces the chunk lookup inside Level.getFluidState. */
    public static LevelChunk chunkForFluid(Level level, BlockPos pos) {
        if (level != active || Thread.currentThread() != serverThread) return level.m_46745_(pos);
        return chunkForRead(level, pos.m_123341_() >> 4, pos.m_123343_() >> 4);
    }

    private static LevelChunk slow(ServerLevel level, int cx, int cz, long key) {
        if (suspended > 0) return level.m_6325_(cx, cz);
        ChunkMap map = level.m_7726_().f_8325_;
        ChunkHolder holder = ((ChunkMapAccessor) map).spookyTickFixes$getVisibleChunkIfPresent(key);
        if (holder != null) {
            if (holder.m_140088_() == ChunkStatus.f_62326_) { // finished, just not at an accessible level
                MEMORY_LOADS.increment();
                lastPath = "loaded from memory";
                return level.m_6325_(cx, cz);
            }
            return barrier(level, cx, cz, "is being generated");
        }
        if (TickFixesConfig.chunkSafeAllowDiskLoads) {
            int verdict = diskVerdict(level, map, cx, cz, key);
            if (verdict > 0) {
                DISK_LOADS.increment();
                lastPath = "loaded from disk";
                return level.m_6325_(cx, cz);
            }
            if (verdict == 0) UNKNOWN.increment();
            return barrier(level, cx, cz, verdict == 0 ? "could not be checked in time" : "is not generated");
        }
        return barrier(level, cx, cz, "is not loaded");
    }

    // ---- disk check ----------------------------------------------------------------------------------------------

    /** 1 = finished chunk on disk, -1 = not on disk or not finished, 0 = unknown (timed out). Cached. */
    private static int diskVerdict(ServerLevel level, ChunkMap map, int cx, int cz, long key) {
        long now = System.currentTimeMillis();
        Long2LongOpenHashMap verdicts;
        synchronized (DISK_VERDICTS) { verdicts = DISK_VERDICTS.computeIfAbsent(level, l -> new Long2LongOpenHashMap()); }
        long cached = verdicts.get(key);
        if (cached != 0 && Math.abs(cached) > now) return cached > 0 ? 1 : -1;
        int verdict = checkDisk(level, map, cx, cz);
        if (verdict != 0) {
            if (verdicts.size() > 8192) verdicts.clear();
            verdicts.put(key, verdict > 0 ? now + VERDICT_MS : -(now + VERDICT_MS));
        } else {
            verdicts.put(key, -(now + UNKNOWN_MS));
        }
        return verdict;
    }

    private static int checkDisk(ServerLevel level, ChunkMap map, int cx, int cz) {
        // No region file (or an empty one) means nothing stored. Checking first also avoids the empty file that
        // opening a missing region creates.
        Path region = DimensionType.m_196975_(level.m_46472_(), level.m_7654_().m_129843_(LevelResource.f_78182_))
                .resolve("region").resolve("r." + (cx >> 5) + "." + (cz >> 5) + ".mca");
        try {
            if (!Files.isRegularFile(region) || Files.size(region) < 8192) return -1;
        } catch (Exception e) {
            return 0;
        }
        CollectFields fields = new CollectFields(new FieldSelector(StringTag.f_129288_, "Status"));
        CompletableFuture<Void> scan = map.m_196922_().m_196358_(new ChunkPos(cx, cz), fields);
        try {
            scan.get(TickFixesConfig.chunkSafeDiskCheckMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            return 0;
        } catch (Exception e) {
            return 0;
        }
        Tag result = fields.m_197713_();
        if (!(result instanceof CompoundTag tag)) return -1;
        String status = tag.m_128461_("Status");
        return status.equals("minecraft:full") || status.equals("full") ? 1 : -1;
    }

    // ---- the barrier ---------------------------------------------------------------------------------------------

    private static LevelChunk barrier(ServerLevel level, int cx, int cz, String why) {
        if (!TickFixesConfig.chunkSafeExempt.isEmpty() || TickFixesConfig.chunkSafeLog) {
            String caller = caller();
            if (isExempt(caller)) {
                lastPath = "exempt caller " + caller + ", vanilla";
                return level.m_6325_(cx, cz);
            }
            if (TickFixesConfig.chunkSafeLog) note(caller, level, cx, cz, why);
        }
        BARRIERS.increment();
        lastPath = "barrier (chunk " + why + ")";
        return barrierChunk(level);
    }

    private static boolean isExempt(String caller) {
        for (String p : TickFixesConfig.chunkSafeExempt) if (!p.isEmpty() && caller.startsWith(p)) return true;
        return false;
    }

    private static void note(String caller, ServerLevel level, int cx, int cz, String why) {
        LongAdder n = CALLERS.get(caller);
        if (n == null) {
            if (CALLERS.size() >= 128) return;
            LongAdder fresh = new LongAdder();
            n = CALLERS.putIfAbsent(caller, fresh);
            if (n == null) {
                n = fresh;
                String stack = WALKER.walk(s -> s.limit(30).map(f -> f.toStackTraceElement().toString())
                        .collect(Collectors.joining("\n\tat ")));
                LOGGER.warn("[spooky_tick_fixes] chunk-safe ticking: {} read chunk [{}, {}] in {}, which {}; answered with a barrier instead of generating it (new caller; later reads from it are only counted)\n\tat {}",
                        caller, cx, cz, level.m_46472_().m_135782_(), why, stack);
            }
        }
        n.increment();
    }

    /** The first frame outside Minecraft, Forge, Mixin, this mod and the chunk-system mods (a merged mixin handler counts). */
    private static String caller() {
        return WALKER.walk(s -> s.map(f -> f.getClassName() + "." + f.getMethodName())
                .filter(n -> !isInfra(n)).findFirst().orElse("<vanilla>"));
    }

    private static final String[] INFRA = {
            "java.", "jdk.", "sun.", "com.mojang.", "net.minecraftforge.", "cpw.", "org.spongepowered.", "it.unimi.",
            "spookydookie.tickfixes.ChunkSafeTicking", "net.minecraft.world.level.Level.", "net.minecraft.world.level.BlockGetter",
            "net.minecraft.world.level.LevelReader", "net.minecraft.world.level.LevelAccessor", "net.minecraft.server.level.ServerLevel.m_8055_",
            "com.abdelaziz.canary."};

    private static boolean isInfra(String frame) {
        for (String p : INFRA) if (frame.startsWith(p)) return true;
        return false;
    }

    /** A chunk object whose every block is a barrier and that holds no fluid. Only ever used for one read. */
    private static LevelChunk barrierChunk(ServerLevel level) {
        synchronized (BARRIER_CHUNKS) {
            return BARRIER_CHUNKS.computeIfAbsent(level, BarrierChunk::new);
        }
    }

    static final class BarrierChunk extends EmptyLevelChunk {
        private static final BlockState BARRIER = Blocks.f_50375_.m_49966_();

        BarrierChunk(Level level) {
            super(level, new ChunkPos(0, 0), anyBiome(level));
            LevelChunkSection[] sections = this.m_7103_();
            for (int i = 0; i < sections.length; i++) {
                PalettedContainer<BlockState> states = new PalettedContainer<>(Block.f_49791_, BARRIER, PalettedContainer.Strategy.f_188137_);
                sections[i] = new LevelChunkSection(states, sections[i].m_187996_());
            }
        }

        private static Holder<Biome> anyBiome(Level level) {
            return level.m_9598_().m_175515_(Registries.f_256952_).m_203300_(0).orElseThrow();
        }

        @Override public BlockState m_8055_(BlockPos pos) { return BARRIER; }
        @Override public FluidState m_6425_(BlockPos pos) { return Fluids.f_76191_.m_76145_(); }
    }

    // ---- /tickfixes probe --------------------------------------------------------------------------------------

    /** A read performed at the start of the requested level's next tick, inside the guarded window. */
    static final class Probe {
        private static volatile ServerLevel level;
        private static volatile BlockPos pos;
        private static volatile java.util.function.Consumer<String> reply;

        static void request(ServerLevel l, BlockPos p, java.util.function.Consumer<String> r) { level = l; pos = p; reply = r; }

        static void runIfPending(ServerLevel l) {
            if (level != l || pos == null) return;
            BlockPos p = pos;
            java.util.function.Consumer<String> r = reply;
            level = null; pos = null; reply = null;
            lastPath = "fast path (chunk loaded)";
            long t0 = System.nanoTime();
            BlockState state = l.m_8055_(p);
            long us = (System.nanoTime() - t0) / 1000;
            r.accept("probe " + p.m_123344_() + " in " + l.m_46472_().m_135782_() + ": " + state + " via " + lastPath + " (" + us + " us)"
                    + (TickFixesConfig.chunkSafeTicking ? "" : " [chunkSafeTicking is off]"));
        }
    }

    static String stats() {
        String callers = CALLERS.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().sum(), a.getValue().sum()))
                .limit(3).map(e -> e.getKey() + "=" + e.getValue().sum()).collect(Collectors.joining(", "));
        return "chunk-safe ticking " + (TickFixesConfig.chunkSafeTicking ? "on" : "OFF") + ": " + BARRIERS.sum()
                + " reads answered with a barrier (" + CALLERS.size() + " callers" + (callers.isEmpty() ? "" : ": " + callers) + "), "
                + DISK_LOADS.sum() + " disk loads, " + MEMORY_LOADS.sum() + " memory loads, " + UNKNOWN.sum() + " disk checks timed out";
    }

    static Set<String> callers() { return CALLERS.keySet(); }

    private ChunkSafeTicking() {}
}
