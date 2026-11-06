package spookydookie.tickfixes.mixin.ray;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spookydookie.tickfixes.RayGuard;

/**
 * The Obsessed: its per-player tick reads single blocks (isEmptyBlock / getBlockState) at random offsets of up to
 * ~20 blocks around the player. A read in a chunk that is still loading or generating blocks the server thread
 * until the chunk is done; while a player was in fresh terrain this cost 19 s in one minute (2026-11-06 22:17).
 * Such a read can't be answered without the chunk, so instead the whole procedure waits: it is skipped for that
 * player on any tick where a chunk within the configured radius is not finished yet.
 */
@Mixin(targets = "net.theobsessed.procedures.PlayerTickProcedure", remap = false)
public abstract class ObsessedChunkWaitMixin {
    @Inject(method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void spookyTickFixes$waitForChunks(Event event, LevelAccessor world, double x, double y, double z, Entity entity,
                                                      CallbackInfo ci) {
        if (world instanceof ServerLevel level && RayGuard.obsessedMustWait(level, entity, x, z)) ci.cancel();
    }
}
