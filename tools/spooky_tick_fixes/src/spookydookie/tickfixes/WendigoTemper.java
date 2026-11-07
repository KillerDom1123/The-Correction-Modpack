package spookydookie.tickfixes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Antlers' Wendigo as a provoked creature that otherwise keeps away from people (1.4.0, {@code [antlersWendigo]}).
 * Changes behaviour.
 *
 * <p>The mod's Wendigo hunts by itself at night in two ways, both turned off by the mixins in {@code mixin.wendigo}:
 * its tick procedure targets the nearest player within ~300 blocks with no line of sight (then sprints, with Speed III
 * within ~150), and its player target goal picks any survival player it can see within its 60-block follow range.
 * Its stalking roar ({@code playsound ... @p ~ ~ ~ 100}: the nearest player, up to 1,600 blocks) is turned off too.
 *
 * <p><b>Calm</b> (WendigoKeepAwayGoal): it moves away from any player it can see within {@code keepAwayRange}.
 *
 * <p><b>Provoked</b> (it then chases only that player):
 * <ul>
 *   <li>hitting it (melee, or a projectile the player shot), day or night;</li>
 *   <li>at night, being within {@code provokeRange} blocks of it where it can see you ({@code provokeRangeSneaking}
 *       while sneaking): you surprised it, cornered it, or ran it down;</li>
 *   <li>at night, staring at it for {@code stareSeconds} from up to {@code stareRange} blocks: your crosshair on its body,
 *       a clear line of sight both ways, and it is not walking away from someone at the time.</li>
 * </ul>
 * Setting the target fires the mod's own "I see you" step when the player is within ~12 blocks: the 12.5 s freeze,
 * scream and heartbeat. While provoked it sprints (the mod then adds Speed III) and roars every 15-30 s, heard within
 * {@code soundRange}.
 *
 * <p><b>Calming down</b>: its provoker leaves, dies, switches to creative/spectator, changes dimension or gets further
 * than {@code calmRange}. With {@code vanishAfterCalm} it then retreats from every player within 48 blocks and vanishes
 * once no player is within 24 blocks and none within 64 can see it (or after 60 s, once none is within 16). While
 * retreating only a hit provokes it again. The mod's own rules still apply: after its scream it removes itself once no
 * player is within ~30 blocks, and at dawn it turns into its daytime form (which runs from players, fights back only).
 *
 * <p>State is kept in memory only (nothing is written to the world); a reloaded Wendigo starts calm.
 * Everything here runs on the server thread.
 */
public final class WendigoTemper {
    private static final ResourceLocation CALLS_OUT = new ResourceLocation("antlers", "wendigo_calls_out");
    /** SetstargetProcedure only screams when a player is inside a 25-block box around the Wendigo. */
    private static final double SCREAM_REACH = 12.5D;
    /** Its scream freezes it (Slowness 101) for 250 ticks; the first chase roar waits for that. */
    private static final long SCREAM_FREEZE_TICKS = 250L;
    /** After calming down: keep away from players this close (seen or not)... */
    private static final double RETREAT_RANGE = 48.0D;
    /** ...and vanish once none is this close and none within VANISH_SEEN can see it... */
    private static final double VANISH_NEAR = 24.0D;
    private static final double VANISH_SEEN = 64.0D;
    /** ...or, after this long, once none is within VANISH_LATE_NEAR. */
    private static final long VANISH_TIMEOUT_TICKS = 1200L;
    private static final double VANISH_LATE_NEAR = 16.0D;

    private static final class State {
        UUID provoker;
        String provokerName = "";
        String reason = "";
        int hurtStamp = Integer.MIN_VALUE;
        long nextRoar;
        boolean fleeing;
        boolean retreating;
        long retreatSince;
        final Map<UUID, Integer> stares = new HashMap<>();
    }

    private static final Map<Mob, State> STATES = new WeakHashMap<>();
    private static long provocations;
    private static long calms;
    private static long vanished;

    /** From WendigoEntity.baseTick (every tick, both sides; acts on the server only). */
    public static void tick(Mob w) {
        if (!TickFixesConfig.wendigoProvokedOnly) return;
        if (!(w.m_9236_() instanceof ServerLevel level)) return;
        State s = STATES.computeIfAbsent(w, k -> new State());

        // Knowing provocation: a hit from a player, by hand or by a projectile they shot. The newest attacker wins.
        int stamp = w.m_21213_();
        if (stamp != s.hurtStamp) {
            s.hurtStamp = stamp;
            LivingEntity by = w.m_21188_();
            if (by instanceof ServerPlayer p && canProvoke(p) && !p.m_20148_().equals(s.provoker)) {
                provoke(w, s, p, level, "hit it");
            }
        }

        if (s.provoker != null) {
            Player p = level.m_46003_(s.provoker);
            double calm = TickFixesConfig.wendigoCalmRange;
            String why = p == null ? "left the dimension or the game"
                    : !p.m_6084_() ? "died"
                    : !canProvoke(p) ? "switched to creative or spectator"
                    : w.m_20280_(p) > calm * calm ? "got more than " + (int) calm + " blocks away"
                    : null;
            if (why != null) {
                calm(w, s, level, why);
            } else {
                if (w.m_5448_() != p) w.m_6710_(p);
                if (!w.m_20142_()) w.m_6858_(true);
                long now = level.m_46467_();
                if (now >= s.nextRoar) {
                    roar(w, level);
                    s.nextRoar = now + 300L + ThreadLocalRandom.current().nextInt(300);
                }
                return;
            }
        }

        // Calm: never keep a player target (alerted by another Wendigo, or set before this patch loaded).
        if (w.m_5448_() instanceof Player) w.m_6710_(null);
        if (w.m_20142_()) w.m_6858_(false);
        if (s.retreating) {
            if (((w.f_19797_ + w.m_19879_()) & 7) == 0) retreatTick(w, s, level);
            return;
        }
        // Unknowing provocation is a night rule (by day it is the daytime form anyway). Every other tick is enough.
        if (level.m_46461_() || ((w.f_19797_ + w.m_19879_()) & 1) != 0) return;
        checkUnknowing(w, s, level);
    }

    private static void checkUnknowing(Mob w, State s, ServerLevel level) {
        double near = TickFixesConfig.wendigoProvokeRange;
        double sneak = TickFixesConfig.wendigoProvokeRangeSneaking;
        double stare = TickFixesConfig.wendigoStareRange;
        double reach = Math.max(Math.max(near, sneak), stare);
        if (reach <= 0.0D) return;
        int stareNeeded = Math.max(2, (int) Math.round(TickFixesConfig.wendigoStareSeconds * 20.0D));
        Vec3 body = new Vec3(w.m_20185_(), w.m_20186_() + w.m_20206_() * 0.6D, w.m_20189_());
        Set<UUID> staring = null;
        for (ServerPlayer p : level.m_6907_()) {
            if (!canProvoke(p)) continue;
            double d2 = p.m_20280_(w);
            if (d2 > reach * reach) continue;
            double r = p.m_6047_() ? sneak : near;
            if (r > 0.0D && d2 <= r * r && w.m_142582_(p)) {
                provoke(w, s, p, level, "came within " + (int) Math.ceil(Math.sqrt(d2)) + " blocks");
                return;
            }
            // A stare only counts while it is not walking away from someone, and it has to see you too.
            if (stare > 0.0D && d2 <= stare * stare && !s.fleeing && isLookingAt(p, w, body) && w.m_21574_().m_148306_(p)) {
                if (staring == null) staring = new HashSet<>(2);
                staring.add(p.m_20148_());
                int ticks = s.stares.merge(p.m_20148_(), 2, Integer::sum);
                if (ticks >= stareNeeded) {
                    provoke(w, s, p, level, "stared at it from " + (int) Math.round(Math.sqrt(d2)) + " blocks");
                    return;
                }
            }
        }
        if (staring == null) s.stares.clear();
        else s.stares.keySet().retainAll(staring);
    }

    /** After calming down: gone once nobody is close and nobody can see it (or after a while, once nobody is very close). */
    private static void retreatTick(Mob w, State s, ServerLevel level) {
        boolean near = false;
        boolean lateNear = false;
        boolean seen = false;
        for (ServerPlayer p : level.m_6907_()) {
            if (!p.m_6084_() || p.m_5833_()) continue;
            double d2 = p.m_20280_(w);
            if (d2 < VANISH_NEAR * VANISH_NEAR) near = true;
            if (d2 < VANISH_LATE_NEAR * VANISH_LATE_NEAR) lateNear = true;
            if (!seen && d2 < VANISH_SEEN * VANISH_SEEN && p.m_142582_(w)) seen = true;
        }
        boolean late = level.m_46467_() - s.retreatSince > VANISH_TIMEOUT_TICKS;
        if ((!near && !seen) || (late && !lateNear)) {
            SpookyTickFixes.LOGGER.info("[{}] wendigo at {} {} {} vanished after calming down{}", SpookyTickFixes.MOD_ID,
                    (int) w.m_20185_(), (int) w.m_20186_(), (int) w.m_20189_(), (!near && !seen) ? "" : " (60 s, still watched)");
            vanished++;
            STATES.remove(w);
            w.m_146870_();
        }
    }

    /** The player's crosshair is within ~0.8 blocks of the middle of its body, and nothing solid is in between. */
    private static boolean isLookingAt(ServerPlayer p, Mob w, Vec3 body) {
        Vec3 to = body.m_82546_(p.m_146892_());
        double d = to.m_82553_();
        if (d < 1.0E-4D) return true;
        double cos = p.m_20252_(1.0F).m_82526_(to) / d;
        return cos > d / Math.sqrt(d * d + 0.64D) && p.m_142582_(w);
    }

    private static boolean canProvoke(Player p) {
        return p.m_6084_() && !p.m_5833_() && !p.m_7500_();
    }

    /**
     * WendigoKeepAwayGoal: the nearest player to move away from, or null. While calm: one it can see within keepAwayRange.
     * While retreating after calming down: any player within 48 blocks. Never while provoked or with the patch off.
     */
    static Player keepAwayFrom(Mob w) {
        if (!TickFixesConfig.wendigoProvokedOnly) return null;
        State s = STATES.get(w);
        if (s == null || s.provoker != null || !(w.m_9236_() instanceof ServerLevel level)) return null;
        double range = s.retreating ? RETREAT_RANGE : TickFixesConfig.wendigoKeepAwayRange;
        if (range <= 0.0D) return null;
        Player best = null;
        double bestD2 = range * range;
        for (ServerPlayer p : level.m_6907_()) {
            if (!p.m_6084_() || p.m_5833_()) continue;
            double d2 = p.m_20280_(w);
            if (d2 >= bestD2) continue;
            if (!s.retreating && !w.m_21574_().m_148306_(p)) continue;
            best = p;
            bestD2 = d2;
        }
        return best;
    }

    static boolean stillKeepingAway(Mob w, Player from) {
        if (!TickFixesConfig.wendigoProvokedOnly || !from.m_6084_()) return false;
        State s = STATES.get(w);
        if (s == null || s.provoker != null) return false;
        double range = (s.retreating ? RETREAT_RANGE : TickFixesConfig.wendigoKeepAwayRange) + 4.0D;
        return w.m_20280_(from) < range * range;
    }

    static void setFleeing(Mob w, boolean fleeing) {
        State s = STATES.get(w);
        if (s != null) s.fleeing = fleeing;
    }

    private static void provoke(Mob w, State s, ServerPlayer p, ServerLevel level, String reason) {
        s.provoker = p.m_20148_();
        s.provokerName = p.m_6302_();
        s.reason = reason;
        s.retreating = false;
        s.stares.clear();
        // The mod screams (and freezes it) when the target is set with a survival player close by, once per Wendigo.
        boolean screams = w.m_20280_(p) <= SCREAM_REACH * SCREAM_REACH && !w.getPersistentData().m_128471_("seeu");
        s.nextRoar = level.m_46467_() + (screams ? SCREAM_FREEZE_TICKS : 0L);
        w.m_21573_().m_26573_();
        w.m_6710_(p);
        w.m_6858_(true);
        provocations++;
        SpookyTickFixes.LOGGER.info("[{}] wendigo at {} {} {} provoked by {}: {}", SpookyTickFixes.MOD_ID,
                (int) w.m_20185_(), (int) w.m_20186_(), (int) w.m_20189_(), s.provokerName, reason);
    }

    private static void calm(Mob w, State s, ServerLevel level, String why) {
        SpookyTickFixes.LOGGER.info("[{}] wendigo at {} {} {} calmed down: {} {}{}", SpookyTickFixes.MOD_ID,
                (int) w.m_20185_(), (int) w.m_20186_(), (int) w.m_20189_(), s.provokerName, why,
                TickFixesConfig.wendigoVanishAfterCalm ? "; retreating" : "");
        s.provoker = null;
        s.provokerName = "";
        s.reason = "";
        s.retreating = TickFixesConfig.wendigoVanishAfterCalm;
        s.retreatSince = level.m_46467_();
        if (w.m_5448_() instanceof Player) w.m_6710_(null);
        w.m_6858_(false);
        w.m_21573_().m_26573_();
        calms++;
    }

    private static void roar(Mob w, ServerLevel level) {
        SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(CALLS_OUT);
        if (sound == null) return;
        level.m_6263_(null, w.m_20185_(), w.m_20186_(), w.m_20189_(), sound, SoundSource.HOSTILE, soundVolume(), 1.0F);
    }

    /** A sound reaches players within 16 x volume blocks (volume above 1 only widens the range). */
    private static float soundVolume() {
        return (float) (TickFixesConfig.wendigoSoundRange / 16.0D);
    }

    /** SetstargetProcedure's sounds: the scream (volume 40, everyone within 640 blocks) is capped to soundRange. */
    public static float capVolume(float volume) {
        return TickFixesConfig.wendigoProvokedOnly ? Math.min(volume, soundVolume()) : volume;
    }

    /** {@code /tickfixes wendigo}: every loaded Wendigo and its state. */
    static List<String> describe(Player viewer) {
        List<String> out = new ArrayList<>();
        out.add("wendigo patch " + (TickFixesConfig.wendigoProvokedOnly ? "on" : "OFF") + ", despawnNormally="
                + TickFixesConfig.wendigoDespawnNormally + " | since start: provoked " + provocations + "x, calmed " + calms
                + "x, vanished " + vanished + "x");
        for (Map.Entry<Mob, State> e : STATES.entrySet()) {
            Mob w = e.getKey();
            if (w.m_213877_() || !w.m_6084_()) continue;
            State s = e.getValue();
            String where = w.m_9236_().m_46472_().m_135782_() + " " + (int) w.m_20185_() + " " + (int) w.m_20186_() + " " + (int) w.m_20189_();
            String dist = viewer != null && viewer.m_9236_() == w.m_9236_() ? " (" + (int) Math.sqrt(viewer.m_20280_(w)) + " blocks from you)" : "";
            String state = s.provoker != null ? "provoked by " + s.provokerName + " (" + s.reason + ")"
                    : s.retreating ? "calmed down, retreating" + (s.fleeing ? " (moving away)" : "")
                    : s.fleeing ? "calm, keeping away from a player" : "calm";
            out.add("  " + where + dist + ": " + state);
        }
        if (out.size() == 1) out.add("  no Wendigo loaded");
        return out;
    }

    private WendigoTemper() {}
}
