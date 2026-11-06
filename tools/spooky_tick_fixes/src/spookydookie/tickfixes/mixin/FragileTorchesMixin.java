package spookydookie.tickfixes.mixin;

import net.minecraftforge.event.entity.living.LivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spookydookie.tickfixes.SmallGates;

/** Fragile Torches: check for a fragile torch at the mob first; skip the per-mob list parse when there is none. */
@Mixin(targets = "com.lothrazar.fragiletorches.FragTorchEvent", remap = false)
public abstract class FragileTorchesMixin {
    @Inject(method = "onEntityUpdate(Lnet/minecraftforge/event/entity/living/LivingEvent$LivingTickEvent;)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void spookyTickFixes$torchFirst(LivingEvent.LivingTickEvent event, CallbackInfo ci) {
        if (SmallGates.fragileTorchesCannotAct(event.getEntity())) ci.cancel();
    }
}
