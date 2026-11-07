package spookydookie.pregen;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.Logger;
import org.popcraft.chunky.ChunkyProvider;
import org.popcraft.chunky.api.ChunkyAPI;
import org.popcraft.chunky.api.event.task.GenerationProgressEvent;

/**
 * The state machine. Runs on the server thread, checked once a second:
 *   players online           -> our task (if any) is paused
 *   empty for the grace time -> resume our paused task, or every checkIntervalMinutes pick and start the next one
 *   task finished            -> record it and pick the next one straight away while still empty
 * Guards (free disk, optional daily growth cap) are checked once a minute and pause the task while they trip.
 * Only tasks this mod started are touched; a Chunky task someone starts by hand is left alone.
 */
final class Scheduler {
    private static final Logger LOG = SpookyPregen.LOGGER;
    private static final String P = "[spooky_pregen] ";

    private static MinecraftServer server;
    private static boolean dedicated;
    private static ChunkyAPI api;
    private static ChunkyAPI listenersOn;
    private static Path configPath, statePath, worldRoot;

    static PregenConfig cfg = new PregenConfig();
    private static String configError;
    static PregenState state = new PregenState();

    private static final ConcurrentLinkedQueue<String> completions = new ConcurrentLinkedQueue<>();
    private static final Map<String, GenerationProgressEvent> progress = new ConcurrentHashMap<>();
    private static final Map<String, Long> lastProgressAt = new ConcurrentHashMap<>();
    /** No progress event for this long while Chunky has no task in the map = stopped by someone else. Above Chunky's
     *  update interval, and above the time the last in-flight chunks of a task can take on the VM. */
    private static final long STALL_MS = 180_000L;
    private static final Map<String, List<EdgeFinder.Window>> doneWindows = new HashMap<>();
    private static final Set<String> warned = new HashSet<>();

    private static long emptySince = -1, lastPick, lastGuard, lastError;
    /** /pregen start: run now and keep running with players online, until /pregen stop, a hold or a restart. */
    private static boolean forced;
    private static long forcedSince;
    private static String blocked;       // guard reason while a guard trips
    private static String idle = "";     // why nothing runs, for /pregen status (logged on change)
    private static long lastFreeBytes = -1, lastGrowthBytes;

    record Target(String kind, String label, String dimension, int x, int z, int radius, int requested, String note) {}

    // ---- lifecycle ---------------------------------------------------------------------------------------------

    static void onServerStarted(MinecraftServer s) {
        server = s;
        dedicated = s.m_6982_();
        worldRoot = s.m_129843_(LevelResource.f_78182_).toAbsolutePath().normalize();
        configPath = FMLPaths.CONFIGDIR.get().resolve("spooky_pregen.json");
        statePath = worldRoot.resolve("spooky_pregen_state.json");
        if (!dedicated) {
            LOG.info(P + "not a dedicated server, inactive");
            return;
        }
        LOG.info(P + reloadConfig());
        state = PregenState.load(statePath);
        int boot = Anchors.bootstrap(worldRoot, state);
        if (boot > 0) LOG.info(P + "anchors: read the last positions of {} player(s) from playerdata", boot);
        save();
        try {
            api = ChunkyProvider.get().getApi();
        } catch (Throwable t) {
            api = null;
            LOG.error(P + "Chunky's API is not available, inactive: {}", t.toString());
            return;
        }
        if (listenersOn != api) {
            // Completion = a progress event with complete() set: Chunky sets it when the finished-chunk count reaches
            // the total. Its GenerationCompleteEvent is no use here: it fires when the task's loop has handed out the
            // last chunk (up to 50 can still be generating) and also when a task is paused or cancelled.
            api.onGenerationProgress(e -> {
                progress.put(e.world(), e);
                lastProgressAt.put(e.world(), System.currentTimeMillis());
                if (e.complete()) completions.add(e.world());
            });
            listenersOn = api;
        }
        if (state.task != null) {
            state.task.paused = true;
            save();
            LOG.info(P + "will resume once the server has been empty for {} min: {}", cfg.emptyGraceMinutes, state.task.describe());
        }
        emptySince = System.currentTimeMillis();
        forced = false; // force mode never survives a restart
        lastPick = 0;
        lastGuard = 0;
        blocked = null;
        LOG.info(P + "ready: enabled={}, {} job(s), {} edge rule(s), check every {} min, grace {} min, min free disk {} GB",
                cfg.enabled, cfg.jobs.size(), cfg.edges.size(), cfg.checkIntervalMinutes, cfg.emptyGraceMinutes, cfg.minFreeDiskGb);
    }

    static void onServerStopping() {
        if (active()) {
            PregenState.Task t = state.task;
            if (t != null && !t.paused) pause(t, "server stopping");
        }
        server = null;
    }

    static void onPlayerJoin(String name) {
        emptySince = -1;
        if (!active()) return;
        PregenState.Task t = state.task;
        if (t != null && !t.paused && !forced) pause(t, name + " joined");
    }

    private static boolean active() {
        return server != null && dedicated && api != null;
    }

    // ---- the once-a-second check ----------------------------------------------------------------------------

    static void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !active() || server.m_129921_() % 20 != 0) return;
        try {
            check(System.currentTimeMillis());
        } catch (RuntimeException ex) {
            long now = System.currentTimeMillis();
            if (now - lastError > 60_000L) {
                lastError = now;
                LOG.error(P + "check failed (logged at most once a minute)", ex);
            }
        }
    }

    private static void check(long now) {
        PregenState.Task t = state.task;
        String world;
        while ((world = completions.poll()) != null) {
            if (t != null && world.equals(t.dimension)) {
                complete(t, "finished");
                t = null;
            }
        }
        if (!cfg.enabled) {
            if (t != null && !t.paused) pause(t, "disabled in the config");
            return;
        }
        if (server.m_7416_() > 0) {
            emptySince = -1;
            if (!forced) { // force mode (/pregen start) keeps going with players online
                if (t != null && !t.paused) pause(t, "players online");
                return;
            }
        } else if (emptySince < 0) {
            emptySince = now;
        }
        if (state.held) return;
        if (!forced && now - emptySince < millis(cfg.emptyGraceMinutes)) return;

        if (lastGuard == 0 || now - lastGuard >= 60_000L) {
            lastGuard = now;
            String g = guard();
            if (!Objects.equals(g, blocked)) {
                if (g != null) LOG.info(P + "holding off: {}", g);
                else LOG.info(P + "guards clear again");
            }
            blocked = g;
        }
        if (blocked != null) {
            if (t != null && !t.paused) pause(t, blocked);
            return;
        }

        if (t != null) {
            if (t.paused) {
                resume(t, now);
            } else if (!api.isRunning(t.dimension)
                    && now - Math.max(t.startedAt, lastProgressAt.getOrDefault(t.dimension, 0L)) > STALL_MS) {
                // Not in Chunky's task map (its loop is done or it was stopped) and no progress for minutes, so the
                // last chunks aren't finishing either: someone paused or cancelled it with /chunky.
                LOG.warn(P + "the Chunky task stopped without finishing (cancelled or paused with /chunky?). Holding; /pregen resume to carry on: {}",
                        t.describe());
                t.paused = true;
                state.held = true;
                save();
            }
            return;
        }
        if (lastPick != 0 && now - lastPick < millis(cfg.checkIntervalMinutes)) return;
        lastPick = now;
        startNext(now);
    }

    // ---- task control ---------------------------------------------------------------------------------------

    private static void startNext(long now) {
        Target next;
        try {
            next = pick();
        } catch (IOException e) {
            setIdle("edge search failed: " + e);
            return;
        }
        if (next == null) {
            setIdle("nothing left to do: every job is done and no edge is within range");
            return;
        }
        if (api.isRunning(next.dimension())) {
            setIdle("Chunky is already running a task in " + next.dimension() + " that this mod didn't start; waiting");
            return;
        }
        progress.remove(next.dimension());
        if (!api.startTask(next.dimension(), cfg.shape, next.x(), next.z(), next.radius(), next.radius(), cfg.pattern)) {
            setIdle("Chunky refused to start " + next.kind() + " '" + next.label() + "' in " + next.dimension());
            return;
        }
        PregenState.Task t = new PregenState.Task();
        t.kind = next.kind();
        t.label = next.label();
        t.dimension = next.dimension();
        t.x = next.x();
        t.z = next.z();
        t.radius = next.radius();
        t.requested = next.requested();
        t.startedAt = now;
        state.task = t;
        save();
        setIdle("");
        LOG.info(P + "started {}{}", t.describe(), next.note());
    }

    private static void resume(PregenState.Task t, long now) {
        if (api.isRunning(t.dimension)) {
            setIdle("Chunky is already running a task in " + t.dimension + " that this mod didn't start; waiting");
            return;
        }
        // Chunky saved the task when it was paused; if it has nothing saved (e.g. its task files were cleared),
        // start the same square again. Chunks already done are skipped quickly.
        progress.remove(t.dimension);
        boolean ok = api.continueTask(t.dimension)
                || api.startTask(t.dimension, cfg.shape, t.x, t.z, t.radius, t.radius, cfg.pattern);
        if (!ok) {
            setIdle("Chunky refused to resume " + t.describe());
            return;
        }
        t.paused = false;
        t.startedAt = now;
        save();
        setIdle("");
        LOG.info(P + "resumed {}", t.describe());
    }

    private static void pause(PregenState.Task t, String reason) {
        boolean ok = api.pauseTask(t.dimension);
        t.paused = true;
        save();
        LOG.info(P + "paused ({}){}: {}", reason, ok ? "" : ", Chunky had nothing running", t.describe());
    }

    private static void complete(PregenState.Task t, String how) {
        if ("blob".equals(t.kind)) {
            PregenState.Blob b = state.blob;
            if (b != null && b.dimension.equals(t.dimension) && b.x == t.x && b.z == t.z) {
                if (t.radius < t.requested || t.requested >= b.max) {
                    state.blob = null; // grown to full size (or cut by the border): the next pick finds a new edge
                    LOG.info(P + "blob at ({}, {}) done", t.x, t.z);
                } else {
                    b.radius = Math.min(t.requested + b.step, b.max);
                }
            }
        }
        if ("job".equals(t.kind)) {
            PregenConfig.Job j = job(t.label);
            if (j == null || t.radius < t.requested || t.requested >= j.max()) {
                state.doneJobs.add(t.label);
                state.jobRadius.remove(t.label);
            } else {
                state.jobRadius.put(t.label, Math.min(t.requested + j.stepOrDefault(), j.max()));
            }
        }
        // Every finished square, job or edge, is done; the edge search skips it even before its chunks are saved.
        doneWindows.computeIfAbsent(t.dimension, k -> new ArrayList<>()).add(new EdgeFinder.Window(
                Math.floorDiv(t.x - t.radius, 16), Math.floorDiv(t.z - t.radius, 16),
                Math.floorDiv(t.x + t.radius - 1, 16), Math.floorDiv(t.z + t.radius - 1, 16)));
        LOG.info(P + "{}: {}", how, t.describe());
        progress.remove(t.dimension);
        state.task = null;
        save();
        lastPick = 0; // chain straight into the next task while the server is still empty
        setIdle("");
    }

    // ---- choosing work --------------------------------------------------------------------------------------

    private static Target pick() throws IOException {
        for (PregenConfig.Job j : cfg.jobs) {
            if (state.doneJobs.contains(j.name)) continue;
            ServerLevel level = level(j.dimension);
            if (level == null) continue;
            int r = state.jobRadius.getOrDefault(j.name, j.radius);
            if (r > j.max()) { // maxRadius lowered since
                state.doneJobs.add(j.name);
                save();
                continue;
            }
            int clamped = borderClamp(level, j.x, j.z, r);
            if (clamped < 16) {
                LOG.info(P + "job '{}' is centred outside the world border, marking it done", j.name);
                state.doneJobs.add(j.name);
                save();
                continue;
            }
            String note = " (radius " + r + " of " + j.max() + (clamped < r ? ", cut to " + clamped + " by the world border" : "") + ")";
            return new Target("job", j.name, j.dimension, j.x, j.z, clamped, r, note);
        }
        // a blob whose rule is gone (or switched off) is dropped
        if (state.blob != null && cfg.edges.stream().noneMatch(e -> e.enabled && e.blob() && e.dimension.equals(state.blob.dimension))) {
            state.blob = null;
            save();
        }
        for (PregenConfig.Edge ed : cfg.edges) {
            if (!ed.enabled) continue;
            ServerLevel level = level(ed.dimension);
            if (level == null) continue;
            if (ed.blob()) {
                Target t = blobTarget(level, ed);
                if (t != null) return t;
                continue;
            }
            EdgeFinder.Result res = EdgeFinder.find(level, worldRoot, ed, doneWindows.getOrDefault(ed.dimension, List.of()));
            if (res == null) continue;
            int x = res.chunkX() * 16 + 8, z = res.chunkZ() * 16 + 8;
            int r = borderClamp(level, x, z, Math.max(16, ed.windowRadius));
            String note = String.format(" (nearest edge, %d blocks from (%d, %d); %d chunks stored in %d region files, %d edge sides in range)",
                    Math.max(Math.abs(x - ed.centerX), Math.abs(z - ed.centerZ)), ed.centerX, ed.centerZ,
                    res.storedChunks(), res.regionFiles(), res.edgeSides());
            return new Target("edge", "edge", ed.dimension, x, z, r, r, note);
        }
        return null;
    }

    /** The current blob's next step, or a new blob at the edge chunk nearest to a player (see BlobFinder). */
    private static Target blobTarget(ServerLevel level, PregenConfig.Edge ed) throws IOException {
        PregenState.Blob b = state.blob;
        if (b != null && !b.dimension.equals(ed.dimension)) return null; // another rule's blob comes first
        if (b == null) {
            List<Anchors.Ref> anchors = Anchors.forDimension(state, ed, System.currentTimeMillis());
            BlobFinder.Result r = BlobFinder.find(level, worldRoot, ed, doneWindows.getOrDefault(ed.dimension, List.of()), anchors);
            if (r == null) return null;
            b = new PregenState.Blob();
            b.dimension = ed.dimension;
            b.x = r.chunkX() * 16 + 8;
            b.z = r.chunkZ() * 16 + 8;
            b.radius = Math.max(16, ed.blobStartRadius);
            b.max = Math.max(b.radius, ed.blobMaxRadius);
            b.step = ed.blobStep > 0 ? ed.blobStep : 256;
            b.why = String.format("edge nearest %s, %d blocks from it; %d islands (%d specks ignored), %d edge chunks, %d chunks generated",
                    r.anchor(), r.distanceBlocks(), r.islands(), r.specks(), r.edgeChunks(), r.storedChunks());
            state.blob = b;
            save();
            LOG.info(P + "new blob at ({}, {}) in {}: {}", b.x, b.z, b.dimension, b.why);
        }
        int clamped = borderClamp(level, b.x, b.z, b.radius);
        if (clamped < 16) {
            LOG.info(P + "blob at ({}, {}) is outside the world border, dropping it", b.x, b.z);
            state.blob = null;
            save();
            return null;
        }
        String note = " (radius " + b.radius + " of " + b.max + (clamped < b.radius ? ", cut to " + clamped + " by the world border" : "") + ")";
        return new Target("blob", "blob", b.dimension, b.x, b.z, clamped, b.radius, note);
    }

    static void onPlayerLogout(net.minecraft.server.level.ServerPlayer player) {
        if (!active()) return;
        Anchors.onLogout(player, state);
        save();
    }

    /** The largest radius up to r whose square around (x, z) stays inside the world border; 0 if (x, z) is outside. */
    static int borderClamp(ServerLevel level, int x, int z, int r) {
        WorldBorder b = level.m_6857_();
        double minX = b.m_61955_(), minZ = b.m_61956_(), maxX = b.m_61957_(), maxZ = b.m_61958_();
        if (x < minX || x >= maxX || z < minZ || z >= maxZ) return 0;
        double room = Math.min(Math.min(x - minX, maxX - x), Math.min(z - minZ, maxZ - z));
        return (int) Math.min(r, Math.floor(room));
    }

    private static ServerLevel level(String dimension) {
        ResourceLocation rl = ResourceLocation.m_135820_(dimension);
        ServerLevel level = rl == null ? null : server.m_129880_(ResourceKey.m_135785_(Registries.f_256858_, rl));
        if (level == null && warned.add(dimension)) LOG.warn(P + "unknown dimension '{}', skipping its entries", dimension);
        return level;
    }

    private static PregenConfig.Job job(String name) {
        for (PregenConfig.Job j : cfg.jobs) if (j.name.equals(name)) return j;
        return null;
    }

    // ---- guards ---------------------------------------------------------------------------------------------

    private static String guard() {
        try {
            lastFreeBytes = Files.getFileStore(worldRoot).getUsableSpace();
        } catch (IOException e) {
            return "cannot read the free disk space: " + e.getMessage();
        }
        if (lastFreeBytes < cfg.minFreeDiskGb * 1e9) {
            return String.format("only %.1f GB free on the world's disk (minimum %.1f GB)", lastFreeBytes / 1e9, cfg.minFreeDiskGb);
        }
        if (cfg.maxDiskGrowthMbPerDay > 0) {
            String today = LocalDate.now().toString();
            long size = folderSize(worldRoot);
            if (!today.equals(state.day)) {
                state.day = today;
                state.dayStartBytes = size;
                save();
            }
            lastGrowthBytes = size - state.dayStartBytes;
            if (lastGrowthBytes > cfg.maxDiskGrowthMbPerDay * 1_000_000L) {
                return String.format("the world grew %d MB today (limit %d MB)", lastGrowthBytes / 1_000_000, cfg.maxDiskGrowthMbPerDay);
            }
        }
        return null;
    }

    private static long folderSize(Path root) {
        long[] total = {0};
        try {
            Files.walkFileTree(root, new SimpleFileVisitor<>() {
                @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    total[0] += attrs.size();
                    return FileVisitResult.CONTINUE;
                }
                @Override public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ignored) {
            // partial total is fine for a daily cap
        }
        return total[0];
    }

    // ---- commands -------------------------------------------------------------------------------------------

    static String reloadConfig() {
        if (configPath == null) return "the server hasn't started yet";
        try {
            cfg = PregenConfig.load(configPath);
            configError = null;
            warned.clear();
            lastGuard = 0;
            return "config loaded: enabled=" + cfg.enabled + ", " + cfg.jobs.size() + " job(s), " + cfg.edges.size() + " edge rule(s)";
        } catch (IOException e) {
            configError = e.getMessage();
            LOG.error(P + "{} not loaded, keeping the previous settings: {}", configPath, e.getMessage());
            return "config NOT loaded (" + e.getMessage() + "), keeping the previous settings";
        }
    }

    static String status() {
        StringBuilder sb = new StringBuilder("spooky_pregen: ");
        if (server == null) return sb.append("server not running").toString();
        if (!dedicated) return sb.append("inactive (not a dedicated server)").toString();
        if (api == null) return sb.append("inactive (Chunky API unavailable)").toString();
        sb.append(cfg.enabled ? "enabled" : "DISABLED in config");
        if (forced) sb.append(" | FORCED by /pregen start ").append((System.currentTimeMillis() - forcedSince) / 60_000L)
                      .append(" min ago (runs with players online; /pregen stop to end)");
        if (configError != null) sb.append(" | config error: ").append(configError);
        long now = System.currentTimeMillis();
        int players = server.m_7416_();
        sb.append("\nplayers ").append(players);
        if (players == 0 && emptySince > 0) sb.append(", empty for ").append(minutesSince(emptySince, now)).append(" min");
        sb.append(" (grace ").append(cfg.emptyGraceMinutes).append(" min) | held: ").append(state.held ? "yes (/pregen resume)" : "no")
          .append(" | guards: ").append(blocked == null ? "clear" : blocked);
        if (lastFreeBytes >= 0) sb.append(String.format(" | disk %.1f GB free", lastFreeBytes / 1e9));
        if (cfg.maxDiskGrowthMbPerDay > 0) sb.append(" | today +").append(lastGrowthBytes / 1_000_000).append(" MB of ").append(cfg.maxDiskGrowthMbPerDay);
        PregenState.Task t = state.task;
        if (t == null) {
            sb.append("\ntask: none").append(idle.isEmpty() ? "" : " (" + idle + ")");
        } else {
            sb.append("\ntask: ").append(t.describe()).append(t.paused ? " [paused]" : " [running]");
            GenerationProgressEvent p = progress.get(t.dimension);
            if (p != null) {
                sb.append(String.format("\nprogress: %.1f%%, %d chunks, %.1f chunks/s, ETA %d:%02d:%02d",
                        p.progress(), p.chunks(), p.rate(), p.hours(), p.minutes(), p.seconds()));
            }
        }
        PregenState.Blob b = state.blob;
        if (b != null) {
            sb.append("\nblob: (").append(b.x).append(", ").append(b.z).append(") in ").append(b.dimension)
              .append(", next radius ").append(b.radius).append(" of ").append(b.max).append(" | ").append(b.why);
        }
        if (!state.anchors.isEmpty()) {
            PregenState.Anchor newest = state.anchors.values().stream().max((x, y) -> Long.compare(x.at, y.at)).orElse(null);
            sb.append("\nanchors: ").append(state.anchors.size());
            if (newest != null) {
                sb.append(" | newest: ").append(newest.name).append(" at (").append(newest.x).append(", ").append(newest.z).append(") in ")
                  .append(newest.dimension).append(", ").append((now - newest.at) / 3_600_000L).append(" h ago");
            }
        }
        sb.append("\njobs:");
        if (cfg.jobs.isEmpty()) sb.append(" none");
        for (PregenConfig.Job j : cfg.jobs) {
            sb.append(" ").append(j.name).append(state.doneJobs.contains(j.name)
                    ? "=done" : "=r" + state.jobRadius.getOrDefault(j.name, j.radius) + "/" + j.max());
        }
        sb.append(" | edge rules: ").append(cfg.edges.stream().filter(e -> e.enabled).count())
          .append(" | edge windows done this session: ").append(doneWindows.values().stream().mapToInt(List::size).sum());
        return sb.toString();
    }

    static String previewNext() {
        if (!active()) return status();
        try {
            Target next = pick();
            return next == null ? "next: nothing (every job done, no edge within range)"
                    : "next: " + next.kind() + " '" + next.label() + "' r=" + next.radius() + " at (" + next.x() + ", " + next.z() + ") in "
                      + next.dimension() + next.note();
        } catch (IOException e) {
            return "edge search failed: " + e;
        }
    }

    static String forceStart() {
        if (!active()) return status();
        if (!cfg.enabled) return "not started: spooky_pregen is disabled in config/spooky_pregen.json (set \"enabled\": true, then /pregen reload)";
        boolean wasHeld = state.held;
        state.held = false;
        forced = true;
        forcedSince = System.currentTimeMillis();
        lastPick = 0;   // pick (or resume) on the next check, a second from now
        lastGuard = 0;  // and re-check the guards first
        save();
        LOG.info(P + "force-started by command{}", wasHeld ? " (hold cleared)" : "");
        return "force-started: generating from the next second, even with players online, until /pregen stop, /pregen hold or a restart"
                + (wasHeld ? " (hold cleared)" : "") + ". The disk guards still apply.";
    }

    static String forceStop() {
        if (!active()) return status();
        if (!forced) return "not in force mode; nothing to stop (/pregen hold stops all work)";
        forced = false;
        PregenState.Task t = state.task;
        boolean online = server.m_7416_() > 0;
        if (online && t != null && !t.paused) pause(t, "force mode ended, players online");
        LOG.info(P + "force mode ended by command");
        return "force mode ended: back to running only while the server is empty" + (online && t != null ? " (task paused, players online)" : "");
    }

    static String hold() {
        if (!active()) return status();
        forced = false;
        state.held = true;
        PregenState.Task t = state.task;
        if (t != null && !t.paused) pause(t, "held by command");
        save();
        return "held: nothing will run until /pregen resume";
    }

    static String resume() {
        if (!active()) return status();
        state.held = false;
        lastPick = 0;
        lastGuard = 0;
        save();
        return "resumed: work continues once the server has been empty for " + cfg.emptyGraceMinutes + " min";
    }

    static String skip() {
        if (!active()) return status();
        PregenState.Task t = state.task;
        if (t == null) return "nothing to skip";
        api.cancelTask(t.dimension);
        complete(t, "skipped");
        return "skipped " + t.describe();
    }

    // ---- helpers --------------------------------------------------------------------------------------------

    private static void setIdle(String s) {
        if (!s.equals(idle)) {
            idle = s;
            if (!s.isEmpty()) LOG.info(P + s);
        }
    }

    private static void save() {
        if (statePath != null) state.save(statePath);
    }

    private static long millis(double minutes) {
        return (long) (minutes * 60_000D);
    }

    private static long minutesSince(long t, long now) {
        return (now - t) / 60_000L;
    }

    private Scheduler() {}
}
