package spookydookie.pregen;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.DimensionType;

/**
 * Blob mode: chooses where the next blob grows.
 *
 * Generated chunks come from the region files' location tables (EdgeFinder.readStoredChunks). They fall into islands
 * (8-connected groups): the main area around spawn, plus the separate areas players reached by boat or on foot. Islands
 * smaller than minIslandChunks are specks (a few chunks left along a fast trip) and are ignored, unless an anchor is
 * within 16 chunks of them. The edge is every missing chunk that touches a chunk of a remaining island. The edge chunk
 * closest to any anchor (where a player logged out or sleeps) wins; with no anchors, the one closest to the spawn centre.
 * Edge chunks outside the world border, beyond maxDistance, or inside a square already finished this session are
 * skipped.
 */
final class BlobFinder {
    record Result(int chunkX, int chunkZ, String anchor, int distanceBlocks, int islands, int specks, int edgeChunks, int storedChunks) {}

    private static final int[] DX4 = {1, -1, 0, 0}, DZ4 = {0, 0, 1, -1};

    static Result find(ServerLevel level, Path worldRoot, PregenConfig.Edge rule, List<EdgeFinder.Window> done,
                       List<Anchors.Ref> anchors) throws IOException {
        Path dir = DimensionType.m_196975_(level.m_46472_(), worldRoot).resolve("region");
        LongOpenHashSet stored = new LongOpenHashSet();
        EdgeFinder.readStoredChunks(dir, stored);

        // anchor points in chunk coordinates; the spawn centre when there are none
        List<Anchors.Ref> refs = new ArrayList<>(anchors);
        if (refs.isEmpty()) refs.add(new Anchors.Ref("spawn", rule.centerX, rule.centerZ, 0));
        int[] ax = new int[refs.size()], az = new int[refs.size()];
        for (int i = 0; i < refs.size(); i++) { ax[i] = Math.floorDiv(refs.get(i).x(), 16); az[i] = Math.floorDiv(refs.get(i).z(), 16); }

        if (stored.isEmpty()) { // nothing generated in this dimension yet: start at the first anchor
            int cx = ax[0], cz = az[0];
            if (Scheduler.borderClamp(level, cx * 16 + 8, cz * 16 + 8, 16) < 16 || inDone(done, cx, cz)) return null;
            return new Result(cx, cz, refs.get(0).name(), 0, 0, 0, 1, 0);
        }

        // islands (8-connected)
        Long2IntOpenHashMap island = new Long2IntOpenHashMap(stored.size());
        island.defaultReturnValue(-1);
        List<int[]> boxes = new ArrayList<>();   // per island: size, minX, minZ, maxX, maxZ
        LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
        LongIterator it = stored.iterator();
        while (it.hasNext()) {
            long start = it.nextLong();
            if (island.get(start) >= 0) continue;
            int id = boxes.size();
            int[] box = {0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
            island.put(start, id);
            queue.enqueue(start);
            while (!queue.isEmpty()) {
                long k = queue.dequeueLong();
                int x = (int) k, z = (int) (k >> 32);
                box[0]++;
                box[1] = Math.min(box[1], x); box[2] = Math.min(box[2], z);
                box[3] = Math.max(box[3], x); box[4] = Math.max(box[4], z);
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        long nk = EdgeFinder.key(x + dx, z + dz);
                        if (stored.contains(nk) && island.get(nk) < 0) { island.put(nk, id); queue.enqueue(nk); }
                    }
                }
            }
            boxes.add(box);
        }
        boolean[] speck = new boolean[boxes.size()];
        int specks = 0;
        for (int i = 0; i < boxes.size(); i++) {
            int[] b = boxes.get(i);
            if (b[0] >= Math.max(1, rule.minIslandChunks)) continue;
            boolean nearAnchor = false;
            for (int a = 0; a < ax.length && !nearAnchor; a++) {
                nearAnchor = ax[a] >= b[1] - 16 && ax[a] <= b[3] + 16 && az[a] >= b[2] - 16 && az[a] <= b[4] + 16;
            }
            if (!nearAnchor) { speck[i] = true; specks++; }
        }

        // edge chunks of the remaining islands, scored by distance to the nearest anchor
        int cx0 = Math.floorDiv(rule.centerX, 16), cz0 = Math.floorDiv(rule.centerZ, 16);
        int maxC = rule.maxDistance > 0 ? rule.maxDistance / 16 : Integer.MAX_VALUE;
        long best = Long.MAX_VALUE;
        int bestX = 0, bestZ = 0, bestAnchor = 0, edges = 0;
        LongOpenHashSet seen = new LongOpenHashSet();
        it = stored.iterator();
        while (it.hasNext()) {
            long k = it.nextLong();
            if (speck[island.get(k)]) continue;
            int x = (int) k, z = (int) (k >> 32);
            for (int i = 0; i < 4; i++) {
                int nx = x + DX4[i], nz = z + DZ4[i];
                long nk = EdgeFinder.key(nx, nz);
                if (stored.contains(nk) || !seen.add(nk)) continue;
                if (maxC != Integer.MAX_VALUE && (Math.abs(nx - cx0) > maxC || Math.abs(nz - cz0) > maxC)) continue;
                edges++;
                long d = Long.MAX_VALUE;
                int which = 0;
                for (int a = 0; a < ax.length; a++) {
                    long da = (long) (nx - ax[a]) * (nx - ax[a]) + (long) (nz - az[a]) * (nz - az[a]);
                    if (da < d) { d = da; which = a; }
                }
                if (d >= best) continue;
                if (inDone(done, nx, nz)) continue;
                if (Scheduler.borderClamp(level, nx * 16 + 8, nz * 16 + 8, 16) < 16) continue;
                best = d; bestX = nx; bestZ = nz; bestAnchor = which;
            }
        }
        if (best == Long.MAX_VALUE) return null;
        return new Result(bestX, bestZ, refs.get(bestAnchor).name(), (int) (Math.sqrt((double) best) * 16), boxes.size(), specks, edges, stored.size());
    }

    private static boolean inDone(List<EdgeFinder.Window> done, int x, int z) {
        for (EdgeFinder.Window w : done) if (w.contains(x, z)) return true;
        return false;
    }

    private BlobFinder() {}
}
