package spookydookie.tickfixes.mixin.wendigo;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import spookydookie.tickfixes.TickFixesConfig;

/** Antlers' Wendigo daytime form ("wendigomad"): may despawn like any monster (the mod always answers false). */
@Mixin(targets = "net.mcreator.antlers.entity.WendigomadEntity", remap = false)
public abstract class WendigomadEntityMixin {
    @Inject(method = "m_6785_(D)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private void spookyTickFixes$despawnNormally(double distanceSqr, CallbackInfoReturnable<Boolean> cir) {
        if (TickFixesConfig.wendigoDespawnNormally) cir.setReturnValue(true);
    }
}
