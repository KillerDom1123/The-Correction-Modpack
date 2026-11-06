package spookydookie.tickfixes.mixin;

import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import spookydookie.tickfixes.SmallGates;

/**
 * Alex's Caves replaces Entity.collide and, for every moving entity, searches the area for its moving magnetic
 * blocks (AbstractMovingBlockEntity) via MagnetUtil.getMovingBlockCollisions; isEntityOnMovingMetal does the same
 * search for MovingMetalBlockEntity (a subclass). With none loaded in the level both return their empty answers
 * (an empty list / false), so those answers are given without the search.
 */
@Mixin(targets = "com.github.alexmodguy.alexscaves.server.entity.util.MagnetUtil", remap = false)
public abstract class AlexsCavesMagnetMixin {
    @Inject(method = "getMovingBlockCollisions(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;",
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void spookyTickFixes$noMovingBlocks(Entity entity, AABB box, CallbackInfoReturnable<List<VoxelShape>> cir) {
        if (SmallGates.noAlexsCavesMovingBlocks(entity)) cir.setReturnValue(List.of());
    }

    @Inject(method = "isEntityOnMovingMetal(Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private static void spookyTickFixes$notOnMovingMetal(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (SmallGates.noAlexsCavesMovingBlocks(entity)) cir.setReturnValue(Boolean.FALSE);
    }
}
