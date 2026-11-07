package spookydookie.tickfixes.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import spookydookie.tickfixes.ChunkSafeTicking;

/**
 * Chunk-safe ticking (see ChunkSafeTicking): the chunk lookup inside Level.getBlockState and Level.getFluidState.
 * Priority 1500 applies it after Canary, which overwrites getBlockState; its version still fetches the chunk with
 * this.getChunk(x, z), so the redirect lands in the method that runs.
 */
@Mixin(value = Level.class, priority = 1500, remap = false)
public abstract class LevelReadGuardMixin {
    @Redirect(method = "m_8055_(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;m_6325_(II)Lnet/minecraft/world/level/chunk/LevelChunk;", remap = false),
              remap = false)
    private LevelChunk spookyTickFixes$chunkForState(Level self, int cx, int cz) {
        return ChunkSafeTicking.chunkForRead(self, cx, cz);
    }

    @Redirect(method = "m_6425_(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;m_46745_(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/chunk/LevelChunk;", remap = false),
              remap = false)
    private LevelChunk spookyTickFixes$chunkForFluid(Level self, BlockPos pos) {
        return ChunkSafeTicking.chunkForFluid(self, pos);
    }
}
