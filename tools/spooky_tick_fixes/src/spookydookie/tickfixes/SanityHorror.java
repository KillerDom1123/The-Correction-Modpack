package spookydookie.tickfixes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Sanity-bound horror (1.5.0, {@code [sanityHorror]}): The Obsessed and CANTBREATHE only come for players whose sanity
 * is critical, never appear inside a base, and are provoked by being looked at. Changes behaviour.
 *
 * <p><b>The Obsessed</b> (mixins in {@code mixin.obsessed}):
 * <ul>
 *   <li><b>Targeting:</b> its per-player targeter timer is held just short of zero while the player isn't critical, so a
 *       targeter can only appear for a critical player (10-60 s after the breakdown starts, if it is due). The targeter
 *       locks on only when that player <i>looks at it</i> (vanilla's Enderman stare test plus a clear line of sight; the
 *       mod used the targeter's line of sight to the player) and is critical. The mod's own conditions still apply
 *       (not sneaking, not immune, no repellant, survival/adventure).</li>
 *   <li><b>Stalking:</b> when its stalk timer is due and the target faces away, the stalk is skipped while the target
 *       isn't critical (retried after the mod's own 100-point delay, about 20 s), and a spot inside a base is moved out
 *       of the base first. The skip happens before the mod's spawn bookkeeping, so its mood and timers don't advance.</li>
 *   <li><b>Kept as they were:</b> the lurker at your windows near your respawn point, and the sleep-paralysis
 *       hallucination in bed. They need an existing obsession but neither critical sanity nor a spot outside a base.</li>
 *   <li><b>Also blocked inside a base:</b> a targeter, and the behind-you/spider hallucinations that can follow a stalk.
 *       The block-break surprise also needs critical sanity and a spot outside a base.</li>
 * </ul>
 *
 * <p><b>CANTBREATHE</b> (mixins in {@code mixin.cantbreathe}): its spawn timer still runs on every survival player's
 * time, but a spawn attempt only goes ahead for a critical victim. Victims are picked among critical players first; a
 * sane victim makes the attempt fail, which makes the mod pick again. Its cave noise only plays for a critical victim and
 * only reaches critical players. Spawn spots inside a base are rejected. It no longer starts a chase because a player
 * came within 15 blocks; looking at it (or hitting it) still provokes it.
 *
 * <p>Everything runs on the server thread. Nothing is stored in the world; the counters run from server start.
 */
public final class SanityHorror {
    private static final String TARGETER_TIMER = "targeter_spawntime";
    private static final String STALK_TIMER = "idk_spawn";
    /** The mod's own "try again" value when the target faces the spot: 100 points, 400 ticks at its usual pace. */
    private static final double STALK_RETRY = 100.0D;

    /** The player whose Obsessed PlayerTickProcedure / BlockBrokenProcedure is running right now. */
    private static Entity obsessedPlayer;
    private static Entity blockBreaker;

    private static long targeterHolds, targetersBlockedSane, targetersBlockedBase, lockOnsRefusedSane;
    private static long stalksHeldSane, stalksMovedOutOfBase, stalksAllowed, hallucinationsBlockedBase;
    private static long surprisesBlockedSane, surprisesBlockedBase;
    private static long cbAttemptsRefusedSane, cbSpotsRejectedBase, cbNoisesMuted, cbCriticalPicks;

    // ---------------------------------------------------------------- The Obsessed

    /** PlayerTickProcedure HEAD: remember the player; hold the targeter timer while they aren't critical. */
    public static void obsessedTickHead(LevelAccessor world, Entity entity) {
        // Logical server only: in singleplayer the client thread runs this procedure too, and must not overwrite the player.
        if (world.m_5776_()) return;
        obsessedPlayer = entity;
        if (!TickFixesConfig.obsessedSanityGate || !(entity instanceof ServerPlayer p)) return;
        if (SanityGate.isCritical(p)) return;
        CompoundTag pd = p.getPersistentData();
        if (pd.m_128459_(TARGETER_TIMER) <= 1.0D) {
            pd.m_128347_(TARGETER_TIMER, 200.0D + ThreadLocalRandom.current().nextInt(1001));
            targeterHolds++;
        }
    }

    /** PlayerTickProcedure, at its stalk spawn: true = skip the spawn (the procedure returns for this tick). */
    public static boolean obsessedStalkBlocked(LevelAccessor world, Entity entity) {
        if (!(entity instanceof ServerPlayer p) || !(world instanceof ServerLevel level)) return false;
        CompoundTag pd = p.getPersistentData();
        if (TickFixesConfig.obsessedSanityGate && !SanityGate.isCritical(p)) {
            pd.m_128347_(STALK_TIMER, STALK_RETRY);
            stalksHeldSane++;
            return true;
        }
        if (TickFixesConfig.obsessedNoBaseSpawns) {
            BlockPos spot = BlockPos.m_274561_(pd.m_128459_("idk_x"), pd.m_128459_("idk_yscan"), pd.m_128459_("idk_z"));
            if (HorrorBases.isInBase(level, spot)) {
                double[] out = HorrorBases.pushOut(level, spot, p);
                pd.m_128347_("idk_x", out[0]);
                pd.m_128347_("idk_z", out[1]);
                pd.m_128347_(STALK_TIMER, STALK_RETRY);
                stalksMovedOutOfBase++;
                return true;
            }
        }
        stalksAllowed++;
        return false;
    }

    /** Every EntityType.spawn in PlayerTickProcedure. Lurker, stalk, saviour and opportunistic forms pass straight through. */
    public static Entity obsessedSpawn(EntityType<?> type, ServerLevel level, BlockPos pos, MobSpawnType reason) {
        String id = path(type);
        if ("idk_stalker_targeter".equals(id)) {
            if (TickFixesConfig.obsessedSanityGate && obsessedPlayer instanceof Player p && !SanityGate.isCritical(p)) {
                targetersBlockedSane++;
                return null;
            }
            if (TickFixesConfig.obsessedNoBaseSpawns && HorrorBases.isInBase(level, pos)) {
                targetersBlockedBase++;
                return null;
            }
        } else if ("idk_stalker_behind".equals(id) || "idk_spider_ceiling".equals(id) || "idk_spider_nightmare".equals(id)) {
            if (TickFixesConfig.obsessedNoBaseSpawns && HorrorBases.isInBase(level, pos)) {
                hallucinationsBlockedBase++;
                return null;
            }
        }
        return type.m_262496_(level, pos, reason);
    }

    /** The targeter's lock-on check (was: the targeter has line of sight to the nearest player). */
    public static boolean obsessedTargeterSees(LivingEntity targeter, Entity seen) {
        boolean sane = TickFixesConfig.obsessedSanityGate && seen instanceof Player p && !SanityGate.isCritical(p);
        if (!TickFixesConfig.obsessedLookToProvoke) {
            if (!targeter.m_142582_(seen)) return false;
            if (sane) lockOnsRefusedSane++;
            return !sane;
        }
        if (!(seen instanceof Player p) || !isLookingAt(p, targeter)) return false;
        if (sane) {
            lockOnsRefusedSane++;
            return false;
        }
        return true;
    }

    /** BlockBrokenProcedure HEAD. */
    public static void blockBreakHead(LevelAccessor world, Entity entity) {
        if (!world.m_5776_()) blockBreaker = entity;
    }

    /** BlockBrokenProcedure's surprise spawn. */
    public static Entity obsessedSurprise(EntityType<?> type, ServerLevel level, BlockPos pos, MobSpawnType reason) {
        if (TickFixesConfig.obsessedSanityGate && blockBreaker instanceof Player p && !SanityGate.isCritical(p)) {
            surprisesBlockedSane++;
            return null;
        }
        if (TickFixesConfig.obsessedNoBaseSpawns && HorrorBases.isInBase(level, pos)) {
            surprisesBlockedBase++;
            return null;
        }
        return type.m_262496_(level, pos, reason);
    }

    /**
     * Vanilla's Enderman stare test (EnderMan.isLookingAtMe without the pumpkin): the view direction within
     * acos(1 - 0.025/d) of its eyes (about 1.7 degrees at 60 blocks, 4 at 10), and a clear line of sight.
     */
    static boolean isLookingAt(Player p, LivingEntity e) {
        Vec3 view = p.m_20252_(1.0F).m_82541_();
        Vec3 to = new Vec3(e.m_20185_() - p.m_20185_(), e.m_20188_() - p.m_20188_(), e.m_20189_() - p.m_20189_());
        double d = to.m_82553_();
        if (d < 1.0E-4D) return true;
        return view.m_82526_(to) / d > 1.0D - 0.025D / d && p.m_142582_(e);
    }

    private static String path(EntityType<?> type) {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(type);
        return key == null ? "" : key.m_135815_();
    }

    // ---------------------------------------------------------------- CANTBREATHE

    /** handleLogic's victim pick (players.get(random)): a random critical player when there is one. */
    public static Object cbPickVictim(List<?> players, int index) {
        if (TickFixesConfig.cantBreatheSanityGate) {
            List<Object> critical = new ArrayList<>(2);
            for (Object o : players) if (o instanceof Player p && SanityGate.isCritical(p)) critical.add(o);
            if (!critical.isEmpty()) {
                cbCriticalPicks++;
                return critical.get(ThreadLocalRandom.current().nextInt(critical.size()));
            }
        }
        return players.get(index);
    }

    /** May a spawn attempt (or a cave noise) go ahead for this victim? */
    public static boolean cbVictimAllowed(Entity victim) {
        return !TickFixesConfig.cantBreatheSanityGate || (victim instanceof Player p && SanityGate.isCritical(p));
    }

    public static void cbAttemptRefused() {
        cbAttemptsRefusedSane++;
    }

    public static void cbNoiseMuted() {
        cbNoisesMuted++;
    }

    /** The cave noise goes to critical players only (it plays at the victim's position, audible ~32 blocks). */
    public static <T> void cbNoise(List<T> players, Consumer<? super T> send) {
        if (!TickFixesConfig.cantBreatheSanityGate) {
            players.forEach(send);
            return;
        }
        for (T o : players) if (o instanceof Player p && SanityGate.isCritical(p)) send.accept(o);
    }

    /** Utils.trySpawnMob: reject a candidate spot inside a base. */
    public static boolean cbSpotInBase(LevelAccessor level, Mob mob) {
        if (!TickFixesConfig.cantBreatheNoBaseSpawns || !(level instanceof ServerLevel sl)) return false;
        if (!HorrorBases.isInBase(sl, mob.m_20183_())) return false;
        cbSpotsRejectedBase++;
        return true;
    }

    // ---------------------------------------------------------------- /tickfixes horror

    static List<String> describe(MinecraftServer server) {
        List<String> out = new ArrayList<>();
        out.add("sanity-bound horror: critical = " + SanityGate.bandName(TickFixesConfig.sanityCriticalBand) + " or worse, base = "
                + TickFixesConfig.baseRadius + " blocks (±" + TickFixesConfig.baseVerticalRange + " y) of a respawn point"
                + (TickFixesConfig.baseColonies ? " or a colony" : "")
                + " | Obsessed sanity/look/noBase = " + TickFixesConfig.obsessedSanityGate + "/" + TickFixesConfig.obsessedLookToProvoke
                + "/" + TickFixesConfig.obsessedNoBaseSpawns + " | CANTBREATHE sanity/look/noBase = " + TickFixesConfig.cantBreatheSanityGate
                + "/" + TickFixesConfig.cantBreatheLookToProvoke + "/" + TickFixesConfig.cantBreatheNoBaseSpawns);
        out.add("  Obsessed since start: targeter holds " + targeterHolds + ", targeters blocked (sane " + targetersBlockedSane + ", base "
                + targetersBlockedBase + "), lock-ons refused (sane) " + lockOnsRefusedSane + ", stalks held (sane) " + stalksHeldSane
                + ", stalks moved out of a base " + stalksMovedOutOfBase + ", stalks allowed " + stalksAllowed
                + ", hallucinations blocked (base) " + hallucinationsBlockedBase + ", surprises blocked (sane " + surprisesBlockedSane
                + ", base " + surprisesBlockedBase + ")");
        out.add("  CANTBREATHE since start: critical victims picked " + cbCriticalPicks + ", attempts refused (sane victim) "
                + cbAttemptsRefusedSane + ", spots rejected (base) " + cbSpotsRejectedBase + ", noises muted " + cbNoisesMuted);
        boolean obsessed = ModList.get().isLoaded("the_obsessed");
        for (ServerPlayer p : server.m_6846_().m_11314_()) {
            StringBuilder line = new StringBuilder("  ").append(p.m_6302_()).append(": ").append(SanityGate.describe(p)).append("; ")
                    .append(HorrorBases.describe(p.m_284548_(), p.m_20183_()));
            if (obsessed) line.append("; ").append(ObsessedView.describe(p));
            out.add(line.toString());
        }
        if (ModList.get().isLoaded("cant_breathe")) out.add("  " + CantBreatheView.describe());
        return out;
    }

    /** Kept apart so the_obsessed classes are only touched when it is installed. */
    private static final class ObsessedView {
        static String describe(ServerPlayer p) {
            boolean target = p.getCapability(net.theobsessed.network.TheObsessedModVariables.PLAYER_VARIABLES_CAPABILITY)
                    .map(v -> v.idktarget).orElse(false);
            CompoundTag pd = p.getPersistentData();
            return target ? String.format("Obsessed target, stalk timer %.1f", pd.m_128459_(STALK_TIMER))
                    : String.format("not targeted, targeter timer %.0f", pd.m_128459_(TARGETER_TIMER));
        }
    }

    /** Kept apart so cant_breathe classes are only touched when it is installed. */
    private static final class CantBreatheView {
        static String describe() {
            String dim = "minecraft:overworld";
            Entity victim = de.cadentem.cant_breathe.CantBreathe.getCurrentVictim(dim);
            var spawn = de.cadentem.cant_breathe.CantBreathe.getTimer(dim, "spawn");
            var noise = de.cadentem.cant_breathe.CantBreathe.getTimer(dim, "noise");
            return "CANTBREATHE (overworld): victim " + (victim instanceof Player p ? p.m_6302_() + " (" + SanityGate.describe(p) + ")"
                            : victim == null ? "none" : String.valueOf(victim))
                    + ", spawn timer " + seconds(spawn.getFirst()) + "/" + seconds(spawn.getSecond())
                    + " s, noise timer " + seconds(noise.getFirst()) + "/" + seconds(noise.getSecond()) + " s";
        }

        private static String seconds(Integer ticks) {
            return ticks == null || ticks < 0 ? "?" : String.valueOf(ticks / 20);
        }
    }

    private SanityHorror() {}
}
