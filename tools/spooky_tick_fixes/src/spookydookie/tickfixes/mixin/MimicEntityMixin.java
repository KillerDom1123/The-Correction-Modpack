package spookydookie.tickfixes.mixin;

import artifacts.entity.MimicEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spookydookie.tickfixes.TickFixesConfig;

/**
 * A dormant Artifacts mimic (campsite chest) runs the full mob tick: physics, AI and every mod's living-tick
 * listener. It only wakes through setTarget (NearestAttackableTargetGoal, follow range 16), hurt (outside the
 * tick) or being in water, and never despawns. With no player within mimicWakeRange (default 24) none of that can
 * happen, so its tick is skipped. Vanilla's entity loop still advances tickCount.
 */
@Mixin(value = MimicEntity.class, remap = false)
public abstract class MimicEntityMixin {
    @Inject(method = "m_8119_", at = @At("HEAD"), cancellable = true, remap = false)
    private void spookyTickFixes$sleepWhileDormant(CallbackInfo ci) {
        if (!TickFixesConfig.mimicSleep) return;
        MimicEntity self = (MimicEntity) (Object) this;
        if (!self.isDormant) return;
        Level level = self.m_9236_();
        if (level.f_46443_) return; // client side: leave alone
        if (!level.m_45914_(self.m_20185_(), self.m_20186_(), self.m_20189_(), TickFixesConfig.mimicWakeRange)) {
            ci.cancel();
        }
    }
}
