package spookydookie.pregen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerPlayer;

/**
 * Where players are: each player's last logout position and bed (respawn point). These are the anchors that blob mode
 * grows towards, so the terrain ahead of where people will walk next is generated first. Recorded on every logout;
 * bootstrapped once from world/playerdata/*.dat (position, dimension and respawn point) so it works from the first run.
 */
final class Anchors {
    record Ref(String name, int x, int z, long at) {}

    static void onLogout(ServerPlayer p, PregenState state) {
        long now = System.currentTimeMillis();
        String uuid = p.m_20148_().toString();
        String name = p.m_6302_();
        put(state, uuid + "/logout", name, p.m_284548_().m_46472_().m_135782_().toString(), (int) p.m_20185_(), (int) p.m_20189_(), now);
        BlockPos bed = p.m_8961_();
        if (bed != null) {
            put(state, uuid + "/bed", name + " (bed)", p.m_8963_().m_135782_().toString(), bed.m_123341_(), bed.m_123343_(), now);
        }
    }

    /** Fills the anchors from world/playerdata once, using each file's modification time as "when". */
    static int bootstrap(Path worldRoot, PregenState state) {
        if (state.anchorsBootstrapped) return 0;
        state.anchorsBootstrapped = true;
        Map<String, String> names = userCache(worldRoot.getParent() != null ? worldRoot.getParent().resolve("usercache.json") : null);
        Path dir = worldRoot.resolve("playerdata");
        int n = 0;
        if (!Files.isDirectory(dir)) return 0;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.dat")) {
            for (Path f : ds) {
                String uuid = f.getFileName().toString().replace(".dat", "");
                if (state.anchors.containsKey(uuid + "/logout")) continue;
                try {
                    CompoundTag tag = NbtIo.m_128937_(f.toFile());
                    long at = Files.getLastModifiedTime(f).toMillis();
                    String name = names.getOrDefault(uuid, uuid.substring(0, Math.min(8, uuid.length())));
                    ListTag pos = tag.m_128437_("Pos", 6);
                    if (pos.size() == 3 && tag.m_128441_("Dimension")) {
                        put(state, uuid + "/logout", name, tag.m_128461_("Dimension"), (int) pos.m_128772_(0), (int) pos.m_128772_(2), at);
                        n++;
                    }
                    if (tag.m_128441_("SpawnX") && tag.m_128441_("SpawnZ")) {
                        String dim = tag.m_128441_("SpawnDimension") ? tag.m_128461_("SpawnDimension") : "minecraft:overworld";
                        put(state, uuid + "/bed", name + " (bed)", dim, tag.m_128451_("SpawnX"), tag.m_128451_("SpawnZ"), at);
                    }
                } catch (Exception e) {
                    SpookyPregen.LOGGER.warn("[spooky_pregen] could not read player data {}: {}", f.getFileName(), e.toString());
                }
            }
        } catch (Exception e) {
            SpookyPregen.LOGGER.warn("[spooky_pregen] could not list {}: {}", dir, e.toString());
        }
        return n;
    }

    /** Anchors in this dimension, newer than maxAgeDays (0 = all), plus the rule's extra anchors. */
    static List<Ref> forDimension(PregenState state, PregenConfig.Edge rule, long now) {
        List<Ref> out = new ArrayList<>();
        if (rule.playerAnchors) {
            long minAt = rule.anchorMaxAgeDays > 0 ? now - rule.anchorMaxAgeDays * 86_400_000L : Long.MIN_VALUE;
            for (PregenState.Anchor a : state.anchors.values()) {
                if (rule.dimension.equals(a.dimension) && a.at >= minAt) out.add(new Ref(a.name, a.x, a.z, a.at));
            }
        }
        for (PregenConfig.Point p : rule.extraAnchors) out.add(new Ref(p.name, p.x, p.z, now));
        return out;
    }

    private static void put(PregenState state, String key, String name, String dimension, int x, int z, long at) {
        PregenState.Anchor a = new PregenState.Anchor();
        a.name = name;
        a.dimension = dimension;
        a.x = x;
        a.z = z;
        a.at = at;
        state.anchors.put(key, a);
    }

    private static Map<String, String> userCache(Path file) {
        Map<String, String> out = new HashMap<>();
        if (file == null || !Files.isRegularFile(file)) return out;
        try {
            JsonElement root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8));
            if (root instanceof JsonArray arr) {
                for (JsonElement e : arr) {
                    if (e instanceof JsonObject o && o.has("uuid") && o.has("name")) out.put(o.get("uuid").getAsString(), o.get("name").getAsString());
                }
            }
        } catch (Exception ignored) {
            // names are only cosmetic
        }
        return out;
    }

    private Anchors() {}
}
