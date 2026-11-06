package spookydookie.tickfixes.mixin;

import com.github.alexthe666.citadel.server.tick.ServerTickRateTracker;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import spookydookie.tickfixes.CitadelCache;

/** Citadel: return the per-server cached tick-rate tracker (see CitadelCache). */
@Mixin(value = ServerTickRateTracker.class, remap = false)
public abstract class CitadelTrackerCacheMixin {
    @Inject(method = "getForServer(Lnet/minecraft/server/MinecraftServer;)Lcom/github/alexthe666/citadel/server/tick/ServerTickRateTracker;",
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void spookyTickFixes$cached(MinecraftServer server, CallbackInfoReturnable<ServerTickRateTracker> cir) {
        ServerTickRateTracker tracker = CitadelCache.get(server);
        if (tracker != null) cir.setReturnValue(tracker);
    }
}
