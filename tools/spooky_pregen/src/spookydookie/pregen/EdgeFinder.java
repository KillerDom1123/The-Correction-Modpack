package spookydookie.pregen;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.DimensionType;

/**
 * Finds the next window to generate at the edge of the generated area.
 *
 * Which chunks exist comes from the 4 KB location table at the start of every region file (1024 entries, non-zero =
 * the chunk is stored). That is the exact set of chunks on disk, read without loading any of them. The edge is every
 * missing chunk that touches a stored one. The edge chunk closest to the centre (inside maxDistance, the world border
 * and no window already done this session) becomes the centre of the next Chunky square.
 *
 * A stored chunk may be only partly generated (the fringe around finished terrain). Chunky checks the saved status
 * and finishes those, and the window reaches back over them.
 */
final class EdgeFinder {
    /** A square already completed this session, in chunk coordinates (inclusive). */
    record Window(int minX, int minZ, int maxX, int maxZ) {
        boolean contains(int x, int z) { return x >= minX && x <= maxX && z >= minZ && z <= maxZ; }
    }

    record Result(int chunkX, int chunkZ, int storedChunks, int regionFiles, int edgeSides) {}

    static long key(int x, int z) { return ((long) z << 32) | (x & 0xFFFFFFFFL); }

    /** Null if no edge chunk qualifies. */
    static Result find(ServerLevel level, Path worldRoot, PregenConfig.Edge edge, List<Window> done) throws IOException {
        Path dir = DimensionType.m_196975_(level.m_46472_(), worldRoot).resolve("region");
        LongOpenHashSet stored = new LongOpenHashSet();
        int files = readStoredChunks(dir, stored);

        int cx = Math.floorDiv(edge.centerX, 16), cz = Math.floorDiv(edge.centerZ, 16);
        int maxC = edge.maxDistance > 0 ? edge.maxDistance / 16 : Integer.MAX_VALUE;
        long best = Long.MAX_VALUE;
        int bestX = 0, bestZ = 0, edgeCount = 0;

        if (stored.isEmpty()) {
            // Nothing generated yet in this dimension: start at the centre.
            if (Scheduler.borderClamp(level, cx * 16 + 8, cz * 16 + 8, 16) < 16 || inDone(done, cx, cz)) return null;
            return new Result(cx, cz, 0, files, 1);
        }
        int[] dx = {1, -1, 0, 0}, dz = {0, 0, 1, -1};
        LongIterator it = stored.iterator();
        while (it.hasNext()) {
            long k = it.nextLong();
            int x = (int) k, z = (int) (k >> 32);
            for (int i = 0; i < 4; i++) {
                int nx = x + dx[i], nz = z + dz[i];
                if (Math.abs(nx - cx) > maxC || Math.abs(nz - cz) > maxC) continue;
                if (stored.contains(key(nx, nz))) continue;
                edgeCount++; // edge sides in range (a chunk touching several stored chunks counts once per side)
                long d = (long) (nx - cx) * (nx - cx) + (long) (nz - cz) * (nz - cz);
                if (d >= best) continue;
                if (inDone(done, nx, nz)) continue;
                if (Scheduler.borderClamp(level, nx * 16 + 8, nz * 16 + 8, 16) < 16) continue;
                best = d; bestX = nx; bestZ = nz;
            }
        }
        if (best == Long.MAX_VALUE) return null;
        return new Result(bestX, bestZ, stored.size(), files, edgeCount);
    }

    private static boolean inDone(List<Window> done, int x, int z) {
        for (Window w : done) if (w.contains(x, z)) return true;
        return false;
    }

    /** Adds every chunk stored in the region files under dir. Returns the number of region files read. */
    static int readStoredChunks(Path dir, LongOpenHashSet out) throws IOException {
        if (!Files.isDirectory(dir)) return 0;
        int files = 0;
        byte[] header = new byte[4096];
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "r.*.*.mca")) {
            for (Path p : ds) {
                String[] parts = p.getFileName().toString().split("\\.");
                if (parts.length != 4) continue;
                int rx, rz;
                try { rx = Integer.parseInt(parts[1]); rz = Integer.parseInt(parts[2]); } catch (NumberFormatException e) { continue; }
                int n;
                try (InputStream in = Files.newInputStream(p)) { n = in.readNBytes(header, 0, 4096); }
                catch (IOException e) { continue; } // being written or removed right now; next pick sees it
                if (n < 4096) continue;
                files++;
                for (int i = 0; i < 1024; i++) {
                    int o = i * 4;
                    if ((header[o] | header[o + 1] | header[o + 2] | header[o + 3]) != 0) {
                        out.add(key(rx * 32 + (i & 31), rz * 32 + (i >> 5)));
                    }
                }
            }
        }
        return files;
    }

    private EdgeFinder() {}
}
