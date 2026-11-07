package spookydookie.tickfixes.mixin.wendigo;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import spookydookie.tickfixes.TickFixesConfig;

/**
 * WendigoEntity$3 is its NearestAttackableTargetGoal&lt;Player&gt; (target priority 7, night only): any survival player
 * it can see within its 60-block follow range. With {@code [antlersWendigo] enabled} it never runs; a provoked Wendigo
 * gets its target from WendigoTemper, and HurtByTargetGoal still answers a hit.
 */
@Mixin(targets = "net.mcreator.antlers.entity.WendigoEntity$3", remap = false)
public abstract class WendigoPlayerTargetGoalMixin {
    @Inject(method = "m_8036_()Z", at = @At("HEAD"), cancellable = true, remap = false)
    private void spookyTickFixes$noUnprovokedTarget(CallbackInfoReturnable<Boolean> cir) {
        if (TickFixesConfig.wendigoProvokedOnly) cir.setReturnValue(false);
    }

    @Inject(method = "m_8045_()Z", at = @At("HEAD"), cancellable = true, remap = false)
    private void spookyTickFixes$noUnprovokedTargetContinue(CallbackInfoReturnable<Boolean> cir) {
        if (TickFixesConfig.wendigoProvokedOnly) cir.setReturnValue(false);
    }
}
