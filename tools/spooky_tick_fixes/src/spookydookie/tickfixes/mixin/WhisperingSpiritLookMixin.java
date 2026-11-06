package spookydookie.tickfixes.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spookydookie.tickfixes.SmallGates;

/**
 * PlayerLookAtSpiritProcedure walks 100 ray steps per player per tick (3 identical raycasts plus a
 * WhisperingSpiritEntity search each). Every other action in the method happens only after that search finds a
 * spirit (bytecode: an empty result jumps straight to the next step), so with no spirit loaded in the player's
 * level the whole procedure is a no-op and is skipped.
 */
@Mixin(targets = "net.yourlocalgamedev.whisperingspirits.procedures.PlayerLookAtSpiritProcedure", remap = false)
public abstract class WhisperingSpiritLookMixin {
    @Inject(method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void spookyTickFixes$skipWithoutSpirits(Event event, LevelAccessor world, Entity entity, CallbackInfo ci) {
        if (world instanceof ServerLevel level && SmallGates.noSpiritsLoaded(level)) ci.cancel();
    }
}
