package spookydookie.tickfixes.mixin;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import spookydookie.tickfixes.ChunkGuards;

/**
 * Diagnostic: logs who asks for chunks from threads other than the server thread (see
 * ChunkGuards.offThreadChunkRequest). It only observes. Priority 1500 makes it apply after Canary, which overwrites
 * getChunk, so the hook lands in the method that actually runs.
 */
@Mixin(value = ServerChunkCache.class, priority = 1500, remap = false)
public abstract class OffThreadChunkLogMixin {
    @Shadow(remap = false) @Final Thread f_8330_;

    @Inject(method = "m_7587_(IILnet/minecraft/world/level/chunk/ChunkStatus;Z)Lnet/minecraft/world/level/chunk/ChunkAccess;",
            at = @At("HEAD"), remap = false)
    private void spookyTickFixes$logOffThread(int x, int z, ChunkStatus status, boolean load, CallbackInfoReturnable<ChunkAccess> cir) {
        if (Thread.currentThread() != this.f_8330_) ChunkGuards.offThreadChunkRequest(x, z, status, load);
    }
}
