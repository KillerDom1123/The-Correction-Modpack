package spookydookie.tickfixes.mixin.anim;

import net.minecraftforge.event.entity.living.LivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spookydookie.tickfixes.AnimationGate;

/** The Obsessed: skip the animation-sync handler for mobs that are not from this mod (see AnimationGate). */
@Mixin(targets = "net.theobsessed.init.EntityAnimationFactory", remap = false)
public abstract class ObsessedAnimationGateMixin {
    @Inject(method = "onEntityTick(Lnet/minecraftforge/event/entity/living/LivingEvent$LivingTickEvent;)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void spookyTickFixes$gate(LivingEvent.LivingTickEvent event, CallbackInfo ci) {
        if (AnimationGate.OBSESSED.skip(event)) ci.cancel();
    }
}
