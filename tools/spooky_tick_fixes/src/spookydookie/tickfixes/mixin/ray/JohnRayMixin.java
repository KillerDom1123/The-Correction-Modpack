package spookydookie.tickfixes.mixin.ray;

import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spookydookie.tickfixes.RayGuard;

/** John Mod Reborn (Weeping John): the per-player look ray casts go through RayGuard (loaded chunks only, duplicates reused). */
@Mixin(targets = {"net.mcreator.johnmod_reborn_nineteen_four_release_beta.procedures.WeepingJohnOnEntityTickUpdateProcedure"}, remap = false)
public abstract class JohnRayMixin {
    @Inject(method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), remap = false)
    private static void spookyTickFixes$rayBegin(CallbackInfo ci) {
        RayGuard.begin();
    }

    @Redirect(method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Entity;)V",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;m_45547_(Lnet/minecraft/world/level/ClipContext;)Lnet/minecraft/world/phys/BlockHitResult;"),
              remap = false)
    private static BlockHitResult spookyTickFixes$guardedClip(Level level, ClipContext ctx) {
        return RayGuard.clip(level, ctx);
    }
}
