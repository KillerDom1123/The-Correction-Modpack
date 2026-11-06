package spookydookie.tickfixes.mixin;

import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import spookydookie.tickfixes.MobTickFlags;

/** Adds a per-mob flag pairing Full Immersion's begin()/finishAi() skips (see FullImmersionAiMixin). */
@Mixin(value = Mob.class, remap = false)
public abstract class MobFlagsMixin implements MobTickFlags {
    @Unique
    private boolean spookyTickFixes$fiSkipped;

    @Override
    public boolean spookyTickFixes$fiSkipped() {
        return this.spookyTickFixes$fiSkipped;
    }

    @Override
    public void spookyTickFixes$setFiSkipped(boolean skipped) {
        this.spookyTickFixes$fiSkipped = skipped;
    }
}
