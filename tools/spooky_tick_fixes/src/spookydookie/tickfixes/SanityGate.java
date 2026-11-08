package spookydookie.tickfixes;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Reads the pack's sanity mechanic (kubejs/server_scripts/oc_sanity.js, docs/sanity_mechanic.md). Read-only.
 *
 * <p>The script keeps its state in the player's KubeJS persistent data. That is KubeJS's own tag
 * ({@code Entity.kjs$persistentData}, saved as {@code KubeJSPersistentData}), not Forge's {@code getPersistentData()},
 * so it is read through KubeJS's {@code kjs$getPersistentData()}, looked up once by reflection. Every 2 s, for
 * survival/adventure players, the script writes {@code oc_sanity} (0-100), {@code oc_sanity_band} (0 Calm, 1 Unease,
 * 2 Dread, 3 Breakdown, with its 5-point hysteresis on the way back up) and {@code oc_sanity_init}.
 *
 * <p>Without KubeJS, or before the script has run for a player, the player reads as sane (band -1), so nothing that
 * this gate guards can happen to them.
 */
public final class SanityGate {
    static final String[] BANDS = {"Calm", "Unease", "Dread", "Breakdown"};

    private static final class Kjs {
        static final MethodHandle GET = resolve();

        private static MethodHandle resolve() {
            try {
                return MethodHandles.lookup().findVirtual(Entity.class, "kjs$getPersistentData", MethodType.methodType(CompoundTag.class));
            } catch (Throwable t) {
                SpookyTickFixes.LOGGER.warn("[{}] KubeJS persistent data not available ({}); sanity-bound horror treats every player as sane",
                        SpookyTickFixes.MOD_ID, t.toString());
                return null;
            }
        }
    }

    private static CompoundTag data(Entity e) {
        MethodHandle get = Kjs.GET;
        if (get == null) return null;
        try {
            return (CompoundTag) get.invokeExact(e);
        } catch (Throwable t) {
            return null;
        }
    }

    /** The band oc_sanity.js last stored for this player (0-3), or -1 when it has never run for them. */
    public static int band(Player p) {
        CompoundTag t = data(p);
        if (t == null || !t.m_128471_("oc_sanity_init")) return -1;
        return t.m_128451_("oc_sanity_band");
    }

    /** True when the player's band is at or below the configured critical band (default 3, Breakdown). */
    public static boolean isCritical(Player p) {
        return band(p) >= TickFixesConfig.sanityCriticalBand;
    }

    static String bandName(int band) {
        return band < 0 ? "no sanity data" : BANDS[Math.min(band, BANDS.length - 1)];
    }

    /** "sanity 12.3 (Breakdown, critical)" for /tickfixes horror. */
    static String describe(Player p) {
        CompoundTag t = data(p);
        int band = band(p);
        if (band < 0 || t == null) return "no sanity data (counts as sane)";
        return String.format("sanity %.1f (%s%s)", t.m_128459_("oc_sanity"), bandName(band), isCritical(p) ? ", critical" : "");
    }

    private SanityGate() {}
}
