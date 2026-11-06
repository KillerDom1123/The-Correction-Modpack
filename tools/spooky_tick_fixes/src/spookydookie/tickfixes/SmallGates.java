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
import net.minecraft.world.level.block.Block;
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
     */
    public static boolean fragileTorchesCannotAct(LivingEntity entity) {
        if (!TickFixesConfig.fragileTorches || entity == null) return false;
        Level level = entity.m_9236_();
        BlockPos pos = entity.m_20183_();
        if (level.m_8055_(pos).m_204336_(FRAGILE)) return false;
        if (entity.m_20192_() >= 1.0F && level.m_8055_(pos.m_7494_()).m_204336_(FRAGILE)) return false;
        return true;
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

    private SmallGates() {}
}
