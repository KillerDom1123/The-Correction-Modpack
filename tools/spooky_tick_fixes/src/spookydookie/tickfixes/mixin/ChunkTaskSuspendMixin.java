package spookydookie.tickfixes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spookydookie.tickfixes.ChunkSafeTicking;

/**
 * Chunk-safe ticking is off while the server thread runs a chunk task (promotion to a full chunk, its post-processing,
 * its neighbour shape updates). Those may run nested inside a level's tick, and must read neighbours as vanilla does.
 */
@Mixin(targets = "net.minecraft.server.level.ServerChunkCache$MainThreadExecutor", remap = false)
public abstract class ChunkTaskSuspendMixin {
    @Inject(method = "m_6367_(Ljava/lang/Runnable;)V", at = @At("HEAD"), remap = false)
    private void spookyTickFixes$suspend(Runnable task, CallbackInfo ci) {
        ChunkSafeTicking.suspend();
    }

    @Inject(method = "m_6367_(Ljava/lang/Runnable;)V", at = @At("RETURN"), remap = false)
    private void spookyTickFixes$resume(Runnable task, CallbackInfo ci) {
        ChunkSafeTicking.resume();
    }
}
