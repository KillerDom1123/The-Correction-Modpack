package spookydookie.farsightimmptl.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spookydookie.farsightimmptl.HeldUnloads;

/** End of each client tick: release held unloads that have gone out of range. */
@Mixin(value = Minecraft.class, remap = false)
public abstract class MinecraftMixin {
    @Inject(method = "m_91398_()V", at = @At("RETURN"), remap = false)
    private void farsightImmptl$releaseHeld(CallbackInfo ci) {
        HeldUnloads.tick((Minecraft) (Object) this);
    }
}
