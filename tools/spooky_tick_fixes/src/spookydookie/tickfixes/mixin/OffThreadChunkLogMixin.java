package spookydookie.tickfixes.mixin;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import spookydookie.tickfixes.ChunkGuards;

/**
 * Diagnostic: logs who asks for chunks from threads other than the server thread (see
 * ChunkGuards.offThreadChunkRequest). It only observes. Since 1.3.4 the hook sits on the off-thread branch of
 * getChunk only, so server-thread lookups don't pay for it. With Canary (which overwrites getChunk, hence priority 1500)
 * that branch is its getChunkOffThread call; in vanilla it is the supplyAsync call. Exactly one of the two exists.
 */
@Mixin(value = ServerChunkCache.class, priority = 1500, remap = false)
public abstract class OffThreadChunkLogMixin {
    @Inject(method = "m_7587_(IILnet/minecraft/world/level/chunk/ChunkStatus;Z)Lnet/minecraft/world/level/chunk/ChunkAccess;",
            at = {
                @At(value = "INVOKE", remap = false,
                    target = "Lnet/minecraft/server/level/ServerChunkCache;getChunkOffThread(IILnet/minecraft/world/level/chunk/ChunkStatus;Z)Lnet/minecraft/world/level/chunk/ChunkAccess;"),
                @At(value = "INVOKE", remap = false,
                    target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;")
            },
            require = 1, remap = false)
    private void spookyTickFixes$logOffThread(int x, int z, ChunkStatus status, boolean load, CallbackInfoReturnable<ChunkAccess> cir) {
        ChunkGuards.offThreadChunkRequest(x, z, status, load);
    }
}
