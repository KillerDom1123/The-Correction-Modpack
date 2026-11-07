package spookydookie.pregen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * config/spooky_pregen.json. Plain JSON, read leniently, so // comments are allowed. Read once at server start, and
 * again on /pregen reload (Forge's file watcher doesn't fire on the VM).
 */
final class PregenConfig {
    /** Master switch. It also only ever runs on a dedicated server. */
    boolean enabled = false;
    /** How often to look for new work while the server is empty (minutes). Work then chains without waiting. */
    double checkIntervalMinutes = 5;
    /** The server must have been empty this long before anything starts or resumes (minutes). */
    double emptyGraceMinutes = 5;
    /** Never generate while the disk holding the world has less free space than this (GB). */
    double minFreeDiskGb = 2.0;
    /** Optional cap on how much the world folder may grow per calendar day (MB). 0 = no cap. */
    long maxDiskGrowthMbPerDay = 0;
    /** Chunky shape and pattern for every task. */
    String shape = "square";
    String pattern = "region";

    /** Worked through in order. Each is generated at radius, then radius + step, ... up to maxRadius. */
    List<Job> jobs = new ArrayList<>();
    /** When every job is done: grow the generated area outward from its edge, nearest to the centre first. */
    List<Edge> edges = new ArrayList<>();

    static final class Job {
        String name;
        String dimension = "minecraft:overworld";
        int x;
        int z;
        /** First radius in blocks (Chunky radius: half the side of the square). */
        int radius = 512;
        /** Last radius. 0 = just the one radius. */
        int maxRadius = 0;
        /** Growth per run, in blocks. */
        int step = 512;

        int max() { return maxRadius > 0 ? Math.max(maxRadius, radius) : radius; }
        int stepOrDefault() { return step > 0 ? step : 512; }
    }

    static final class Edge {
        boolean enabled = true;
        String dimension = "minecraft:overworld";
        /**
         * "blob" (1.1.0): pick the edge chunk nearest to where players are, grow a square there from blobStartRadius to
         * blobMaxRadius in blobStep steps, then pick again on the new edge.
         * "nearest" (1.0.0): fixed windowRadius squares at the edge chunk nearest to the centre.
         */
        String mode = "blob";
        /** Spawn anchor: the fallback when no player anchor is known, and the reference point for "nearest" mode. */
        int centerX;
        int centerZ;
        /** Edges further than this from the centre, in blocks on either axis (a square), are left alone. 0 = no limit. */
        int maxDistance = 0;
        /** "nearest" mode: radius of the square generated around each chosen edge chunk, in blocks. */
        int windowRadius = 256;

        /** "blob" mode: first and last radius of each blob, and the growth per run, in blocks. */
        int blobStartRadius = 256;
        int blobMaxRadius = 1024;
        int blobStep = 256;
        /** Islands of generated chunks smaller than this are specks (crumbs from fast travel): their edges are ignored,
         *  unless an anchor is within 16 chunks of them. */
        int minIslandChunks = 64;
        /** Use players' last logout positions and beds as anchors (bootstrapped from world/playerdata). */
        boolean playerAnchors = true;
        /** Anchors older than this are ignored (days). 0 = keep them all. */
        int anchorMaxAgeDays = 30;
        /** Extra fixed anchors, e.g. bases: [{"name": "base", "x": 100, "z": -200}]. */
        List<Point> extraAnchors = new ArrayList<>();

        boolean blob() { return !"nearest".equalsIgnoreCase(mode); }
    }

    static final class Point {
        String name = "point";
        int x;
        int z;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    static PregenConfig load(Path file) throws IOException {
        if (!Files.exists(file)) {
            PregenConfig defaults = new PregenConfig();
            Files.writeString(file, GSON.toJson(defaults), StandardCharsets.UTF_8);
            return defaults;
        }
        try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonReader jr = new JsonReader(r);
            jr.setLenient(true);
            PregenConfig c = GSON.fromJson(jr, PregenConfig.class);
            if (c == null) throw new IOException("empty file");
            if (c.jobs == null) c.jobs = new ArrayList<>();
            if (c.edges == null) c.edges = new ArrayList<>();
            for (Job j : c.jobs) if (j.name == null || j.name.isBlank()) throw new IOException("every job needs a \"name\"");
            return c;
        } catch (RuntimeException e) {
            throw new IOException(e.getMessage(), e);
        }
    }
}
