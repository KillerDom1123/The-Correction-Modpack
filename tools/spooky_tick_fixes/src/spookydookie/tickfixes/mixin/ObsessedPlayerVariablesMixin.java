package spookydookie.tickfixes.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spookydookie.tickfixes.ObsessedSyncCoalescer;

/** Defers The Obsessed's player-variable sync to one send per player at the end of the server tick. */
@Mixin(targets = "net.theobsessed.network.TheObsessedModVariables$PlayerVariables", remap = false)
public abstract class ObsessedPlayerVariablesMixin {
    @Inject(method = "syncPlayerVariables(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void spookyTickFixes$coalesce(Entity entity, CallbackInfo ci) {
        if (ObsessedSyncCoalescer.intercept(this, entity)) ci.cancel();
    }
}
