package spookydookie.tickfixes.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spookydookie.tickfixes.ActivationRange;

/** Activation range: far, idle mobs skip some ticks (see ActivationRange). Hooks ServerLevel.tickNonPassenger. */
@Mixin(value = ServerLevel.class, remap = false)
public abstract class ServerLevelActivationRangeMixin {
    @Inject(method = "m_8647_(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void spookyTickFixes$activationRange(Entity entity, CallbackInfo ci) {
        if (ActivationRange.shouldSkip((ServerLevel) (Object) this, entity)) ci.cancel();
    }
}
