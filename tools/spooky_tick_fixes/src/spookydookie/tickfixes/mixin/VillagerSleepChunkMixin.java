package spookydookie.tickfixes.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.SleepInBed;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import spookydookie.tickfixes.ChunkGuards;

/**
 * Vanilla villager sleep check: don't load the home bed's chunk just to read the bed (see ChunkGuards.villagerBedState).
 * The read comes before the "within 2 blocks" check in checkExtraStartConditions.
 */
@Mixin(value = SleepInBed.class, remap = false)
public abstract class VillagerSleepChunkMixin {
    @Redirect(method = "m_6114_(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)Z",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/server/level/ServerLevel;m_8055_(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
                       remap = false),
              remap = false)
    private BlockState spookyTickFixes$bedStateIfLoaded(ServerLevel level, BlockPos pos) {
        return ChunkGuards.villagerBedState(level, pos);
    }
}
