package spookydookie.pregen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Progress, kept in the world folder (spooky_pregen_state.json), so it travels with the world and survives restarts. */
final class PregenState {
    /** Next radius to generate, per job name. */
    Map<String, Integer> jobRadius = new LinkedHashMap<>();
    Set<String> doneJobs = new LinkedHashSet<>();
    /** The Chunky task this mod started, if any. */
    Task task;
    /** Set by /pregen hold (or after an outside /chunky cancel); cleared by /pregen resume. */
    boolean held;
    /** For maxDiskGrowthMbPerDay. */
    String day;
    long dayStartBytes;
    /** The blob being grown (blob mode), so a restart carries on with it. */
    Blob blob;
    /** Where players were: key "<uuid>/logout" or "<uuid>/bed". */
    Map<String, Anchor> anchors = new LinkedHashMap<>();
    boolean anchorsBootstrapped;

    static final class Blob {
        String dimension;
        int x;
        int z;
        int radius;   // next radius to generate
        int max;
        int step;
        String why;   // what picked it, for status
    }

    static final class Anchor {
        String name;
        String dimension;
        int x;
        int z;
        long at;      // ms since epoch
    }

    static final class Task {
        String kind;       // "job" or "edge"
        String label;      // job name, or "edge"
        String dimension;
        int x;
        int z;
        int radius;        // the radius given to Chunky (after clamping to the world border)
        int requested;     // the job radius this run stands for
        boolean paused;
        long startedAt;

        String describe() {
            return kind + " '" + label + "' r=" + radius + " at (" + x + ", " + z + ") in " + dimension;
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    static PregenState load(Path file) {
        try {
            if (Files.exists(file)) {
                PregenState s = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), PregenState.class);
                if (s != null) {
                    if (s.jobRadius == null) s.jobRadius = new LinkedHashMap<>();
                    if (s.doneJobs == null) s.doneJobs = new LinkedHashSet<>();
                    if (s.anchors == null) s.anchors = new LinkedHashMap<>();
                    return s;
                }
            }
        } catch (Exception e) {
            SpookyPregen.LOGGER.warn("[spooky_pregen] could not read {}, starting fresh: {}", file, e.toString());
        }
        return new PregenState();
    }

    void save(Path file) {
        try {
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(tmp, GSON.toJson(this), StandardCharsets.UTF_8);
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            SpookyPregen.LOGGER.warn("[spooky_pregen] could not save {}: {}", file, e.toString());
        }
    }
}
