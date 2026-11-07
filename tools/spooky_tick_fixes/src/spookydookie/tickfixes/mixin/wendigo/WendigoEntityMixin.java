package spookydookie.tickfixes.mixin.wendigo;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import spookydookie.tickfixes.TickFixesConfig;
import spookydookie.tickfixes.WendigoKeepAwayGoal;
import spookydookie.tickfixes.WendigoTemper;

/**
 * Antlers' Wendigo (night form): WendigoTemper runs from its baseTick, it gets WendigoKeepAwayGoal at goal priority 1
 * (its own goals use 2-11), and it may despawn like any monster. The mod's removeWhenFarAway always answers false
 * (MCreator's "despawn when idle" unticked), so every Wendigo ever spawned stayed for good, re-created at each dawn/dusk swap.
 */
@Mixin(targets = "net.mcreator.antlers.entity.WendigoEntity", remap = false)
public abstract class WendigoEntityMixin {
    @Inject(method = "m_6075_()V", at = @At("HEAD"), remap = false)
    private void spookyTickFixes$temper(CallbackInfo ci) {
        WendigoTemper.tick((Mob) (Object) this);
    }

    @Inject(method = "m_8099_()V", at = @At("TAIL"), remap = false)
    private void spookyTickFixes$keepAway(CallbackInfo ci) {
        PathfinderMob self = (PathfinderMob) (Object) this;
        self.f_21345_.m_25352_(1, new WendigoKeepAwayGoal(self));
    }

    @Inject(method = "m_6785_(D)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private void spookyTickFixes$despawnNormally(double distanceSqr, CallbackInfoReturnable<Boolean> cir) {
        if (TickFixesConfig.wendigoDespawnNormally) cir.setReturnValue(true);
    }
}
