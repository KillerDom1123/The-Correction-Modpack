package spookydookie.tickfixes.mixin.ray;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import spookydookie.tickfixes.ChunkGuards;

/**
 * The Obsessed (player tick): every isEmptyBlock (33 calls) and getBlockState (24 calls) goes through ChunkGuards, so a
 * read in an unloaded chunk never loads it. This covers the stalker spawn search's scan point, which can be anywhere:
 * near another player, or at the old location after a teleport. ObsessedChunkWaitMixin only covers the area around
 * the player.
 */
@Mixin(targets = "net.theobsessed.procedures.PlayerTickProcedure", remap = false)
public abstract class ObsessedBlockReadMixin {
    @Redirect(method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelAccessor;m_46859_(Lnet/minecraft/core/BlockPos;)Z", remap = false),
              remap = false)
    private static boolean spookyTickFixes$isEmptyIfLoaded(LevelAccessor world, BlockPos pos) {
        return ChunkGuards.obsessedIsEmptyBlock(world, pos);
    }

    @Redirect(method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/level/LevelAccessor;m_8055_(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
                       remap = false),
              remap = false)
    private static BlockState spookyTickFixes$stateIfLoaded(LevelAccessor world, BlockPos pos) {
        return ChunkGuards.obsessedBlockState(world, pos);
    }
}
