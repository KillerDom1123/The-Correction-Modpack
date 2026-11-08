package spookydookie.tickfixes.mixin.cantbreathe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import spookydookie.tickfixes.TickFixesConfig;

/**
 * CANTBREATHE's "target too close" goal: once visible, any survival player within 15 blocks made it chase them, seen or
 * not. With {@code cantBreatheLookToProvoke} it never starts. What still provokes it: looking at it (its own "target
 * sees me" goal: the nearest player within 128 blocks, view within ~8 degrees and a clear line of sight, then a roll of
 * chase / stare / stare / flee) and hitting it.
 */
@Mixin(targets = "de.cadentem.cant_breathe.entities.goals.CantBreatheTargetTooCloseGoal", remap = false)
public abstract class CantBreatheTooCloseMixin {
    @Inject(method = "m_8036_()Z", at = @At("HEAD"), cancellable = true, remap = false)
    private void spookyTickFixes$lookToProvoke(CallbackInfoReturnable<Boolean> cir) {
        if (TickFixesConfig.cantBreatheLookToProvoke) cir.setReturnValue(false);
    }
}
