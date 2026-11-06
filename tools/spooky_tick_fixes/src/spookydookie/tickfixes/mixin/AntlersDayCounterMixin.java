package spookydookie.tickfixes.mixin;

import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spookydookie.tickfixes.SmallGates;

/** Antlers: count the (global) day once per tick, on the overworld's tick, instead of once per loaded level. */
@Mixin(targets = "net.mcreator.antlers.procedures.DayCounterProcedure", remap = false)
public abstract class AntlersDayCounterMixin {
    @Inject(method = "onWorldTick(Lnet/minecraftforge/event/TickEvent$LevelTickEvent;)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void spookyTickFixes$overworldOnly(TickEvent.LevelTickEvent event, CallbackInfo ci) {
        if (SmallGates.antlersDayCounterSkip(event.level)) ci.cancel();
    }
}
