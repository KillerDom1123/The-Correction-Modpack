package spookydookie.tickfixes.mixin;

import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import spookydookie.tickfixes.SmallGates;

/**
 * Full Immersion's PortalDespawnCompatibility.shouldSuppressDespawn (called from every mob's checkDespawn) can only
 * return true for a portal in its index, which holds every Portal entity that joined the level. With none loaded,
 * it returns false, so that answer is given directly.
 */
@Mixin(targets = "com.astrologic.fullimmersion.PortalDespawnCompatibility", remap = false)
public abstract class FullImmersionDespawnMixin {
    @Inject(method = "shouldSuppressDespawn(Lnet/minecraft/world/entity/Mob;)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private static void spookyTickFixes$noPortals(Mob mob, CallbackInfoReturnable<Boolean> cir) {
        if (SmallGates.fullImmersionIdle(mob)) cir.setReturnValue(Boolean.FALSE);
    }
}
