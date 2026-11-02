package spookydookie.immptldimthread.mixin;

import me.srrapero720.dimthread.DimThread;
import me.srrapero720.dimthread.thread.IMutableMainThread;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Immersive Portals re-flushes its chunk-ticket throttle at the end of vanilla's
 * {@code DistanceManager.runAllUpdates}. Under DimThreads that runs on the worker
 * thread ticking the level, so Immersive Portals' {@code MinecraftServer.isSameThread()}
 * guard fails, logs a stack trace every call and returns without flushing.
 *
 * The flush only touches the distance manager and ticket queue of the level passed in,
 * and it is invoked from inside that level's own update loop, so the worker that owns
 * the level is exactly as safe a caller as the server thread. This redirect widens the
 * guard to accept that worker and nothing else.
 *
 * Mirrors the shape of DimThreads v1.2.1's own Tardis Refined fix
 * ("skips thread check on levels when both threads are dimthreads").
 */
@Mixin(targets = "qouteall.imm_ptl.core.chunk_loading.ImmPtlChunkTickets", remap = false)
public abstract class ImmPtlChunkTicketsMixin {

    @Redirect(
        method = "flushThrottling(Lnet/minecraft/server/level/ServerLevel;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;m_18695_()Z"),
        remap = false
    )
    private boolean immptlDimthreadCompat$isSameThreadOrOwningWorker(MinecraftServer server, ServerLevel level) {
        if (server.m_18695_()) {
            return true; // genuine server thread: original behaviour
        }
        Thread current = Thread.currentThread();
        if (!DimThread.owns(current)) {
            return false; // some other thread: keep Immersive Portals' refusal
        }
        // DimThreads records, on the level itself, which worker is ticking it right now.
        if (level instanceof IMutableMainThread owned) {
            return owned.dimThreads$getMainThread() == current;
        }
        return false;
    }
}
