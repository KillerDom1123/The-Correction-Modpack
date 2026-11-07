package spookydookie.tickfixes;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraftforge.registries.ForgeRegistries;
import net.yourlocalgamedev.whisperingspirits.entity.WhisperingSpiritEntity;
import spookydookie.tickfixes.mixin.EntityPersistentDataAccessor;

/** Small decision helpers called from mixins (kept out of the mixin classes so they hold no static state). */
public final class SmallGates {
    private static final TagKey<Block> FRAGILE = TagKey.m_203882_(ForgeRegistries.Keys.BLOCKS, new ResourceLocation("fragiletorches", "fragile"));
    private static final ResourceLocation JUPITER = new ResourceLocation("adastraextra", "jupiter");
    private static final String GASSED = "adastraextraGassed";

    /**
     * Fragile Torches can only break a tagged block at the mob's feet, or (for mobs at least 1 block tall) the block
     * above. If neither is fragile, the handler cannot do anything, so it is skipped before its list parse.
     * Both blocks are in the same chunk column, so the chunk is looked up once (1.2.0; a chunk lookup is the
     * expensive part, as several mods wrap it). Out-of-height positions read as air either way, which is not fragile.
     * The entity's cached feet state is not used: baseTick clears it after this event fires, so it can be a tick old.
     */
    public static boolean fragileTorchesCannotAct(LivingEntity entity) {
        if (!TickFixesConfig.fragileTorches || entity == null) return false;
        Level level = entity.m_9236_();
        BlockPos pos = entity.m_20183_();
        // Never wait for a chunk here (it showed up in the chunk waits); one that isn't ready can't have a torch to break.
        LevelChunk chunk = level instanceof net.minecraft.server.level.ServerLevel sl
                ? sl.m_7726_().m_7131_(pos.m_123341_() >> 4, pos.m_123343_() >> 4) : level.m_46745_(pos);
        if (chunk == null) return true;
        if (chunk.m_8055_(pos).m_204336_(FRAGILE)) return false;
        if (entity.m_20192_() >= 1.0F && chunk.m_8055_(pos.m_7494_()).m_204336_(FRAGILE)) return false;
        return true;
    }

    /**
     * Antlers' DayCounterProcedure runs on every level's tick, but its counter is global (overworld saved data), so
     * with ~45 levels it counted ~45 game ticks per tick and sent a full sync packet to every player each time.
     * Server side, it now runs for the overworld only: one count and one sync per tick. Changes behaviour: its day
     * counter runs at the real pace (its only effect is the one-time "wendigospawn" flag at day 2, read by the flute).
     */
    public static boolean antlersDayCounterSkip(Level level) {
        if (!TickFixesConfig.antlersDayCounter || level == null || level.m_5776_()) return false;
        return !Level.f_46428_.equals(level.m_46472_());
    }

    /**
     * MCreator per-level saved-data sync (The One Who Watches' WorldVariables) sends only to players in that level.
     * With none there the packet goes nowhere, so encoding it is skipped; the caller still marks the data dirty.
     * Players get the full state on login and on dimension change from the mod's own handlers.
     */
    public static boolean noPlayersIn(LevelAccessor world) {
        return TickFixesConfig.towwSyncGate && world instanceof ServerLevel level && level.m_6907_().isEmpty();
    }

    /** Whispering Spirits' look check can only act when a WhisperingSpiritEntity is loaded in the player's level. */
    public static boolean noSpiritsLoaded(ServerLevel level) {
        if (!TickFixesConfig.spiritLookGate) return false;
        List<WhisperingSpiritEntity> found = new ArrayList<>(1);
        level.m_261178_(EntityTypeTest.m_156916_(WhisperingSpiritEntity.class), e -> true, found, 1);
        return found.isEmpty();
    }

    /**
     * Ad Astra Extra's EntityTickProcedure has two branches: the "gassed" counter in persistent data, and random
     * lightning in adastraextra:jupiter. A mob with neither is skipped. The raw field is read so that mobs without
     * persistent data don't get an empty tag allocated.
     */
    public static boolean adAstraExtraCannotAct(LivingEntity entity) {
        if (!TickFixesConfig.adAstraExtraGate || entity == null) return false;
        CompoundTag data = ((EntityPersistentDataAccessor) entity).spookyTickFixes$getRawPersistentData();
        if (data != null && data.m_128441_(GASSED)) return false;
        return !JUPITER.equals(entity.m_9236_().m_46472_().m_135782_());
    }

    /**
     * Full Immersion's per-mob portal AI (begin/finishAi) and its portal despawn check can only act through an
     * Immersive Portals portal in the mob's own level; with none loaded there, they are skipped.
     */
    public static boolean fullImmersionIdle(net.minecraft.world.entity.Mob mob) {
        if (!TickFixesConfig.fullImmersionGate || mob == null) return false;
        Level level = mob.m_9236_();
        return level instanceof ServerLevel && EntityCounters.noPortals(level);
    }

    /** Alex's Caves' moving-block collision/standing checks can only find AbstractMovingBlockEntity instances. */
    public static boolean noAlexsCavesMovingBlocks(net.minecraft.world.entity.Entity entity) {
        if (!TickFixesConfig.alexsCavesGate || entity == null) return false;
        Level level = entity.m_9236_();
        return !level.f_46443_ && EntityCounters.noMovingBlocks(level);
    }

    private SmallGates() {}
}
