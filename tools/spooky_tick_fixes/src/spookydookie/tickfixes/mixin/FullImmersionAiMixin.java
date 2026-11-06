package spookydookie.tickfixes.mixin;

import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spookydookie.tickfixes.MobTickFlags;
import spookydookie.tickfixes.SmallGates;

/**
 * Full Immersion wraps every mob's serverAiStep with PortalAiContext.begin/finishAi (ThreadLocal set/get/remove plus
 * route and candidate caches, and a projection scan every aiScanIntervalTicks). Projections are players seen through
 * an Immersive Portals portal in the mob's level; with no portal loaded there, begin() can only clear its state and
 * finishAi() only clears it again. Both are then skipped, as a pair: finishAi() is skipped only when begin() was
 * skipped for the same mob, so the per-thread state can never be left stale.
 */
@Mixin(targets = "com.astrologic.fullimmersion.PortalAiContext", remap = false)
public abstract class FullImmersionAiMixin {
    @Inject(method = "begin(Lnet/minecraft/world/entity/Mob;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void spookyTickFixes$skipBegin(Mob mob, CallbackInfo ci) {
        boolean skip = SmallGates.fullImmersionIdle(mob);
        ((MobTickFlags) mob).spookyTickFixes$setFiSkipped(skip);
        if (skip) ci.cancel();
    }

    @Inject(method = "finishAi(Lnet/minecraft/world/entity/Mob;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void spookyTickFixes$skipFinish(Mob mob, CallbackInfo ci) {
        MobTickFlags flags = (MobTickFlags) mob;
        if (flags.spookyTickFixes$fiSkipped()) {
            flags.spookyTickFixes$setFiSkipped(false);
            ci.cancel();
        }
    }
}
