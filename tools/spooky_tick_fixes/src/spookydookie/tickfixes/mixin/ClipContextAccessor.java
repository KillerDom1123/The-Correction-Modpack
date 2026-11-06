package spookydookie.tickfixes.mixin;

import net.minecraft.world.level.ClipContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Reads a ClipContext's block and fluid modes, part of RayGuard's duplicate check. */
@Mixin(value = ClipContext.class, remap = false)
public interface ClipContextAccessor {
    @Accessor(value = "f_45684_", remap = false)
    ClipContext.Block spookyTickFixes$block();

    @Accessor(value = "f_45685_", remap = false)
    ClipContext.Fluid spookyTickFixes$fluid();
}
