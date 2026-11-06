package spookydookie.tickfixes.mixin;

import net.minecraftforge.event.entity.living.LivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spookydookie.tickfixes.SmallGates;

/** Ad Astra Extra: skip its per-mob procedure for mobs that are neither gassed nor on Jupiter. */
@Mixin(targets = "net.bullfighter.adastraextra.procedures.EntityTickProcedure", remap = false)
public abstract class AdAstraExtraEntityTickMixin {
    @Inject(method = "onEntityTick(Lnet/minecraftforge/event/entity/living/LivingEvent$LivingTickEvent;)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void spookyTickFixes$gate(LivingEvent.LivingTickEvent event, CallbackInfo ci) {
        if (SmallGates.adAstraExtraCannotAct(event.getEntity())) ci.cancel();
    }
}
