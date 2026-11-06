package spookydookie.tickfixes.mixin;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spookydookie.tickfixes.SmallGates;

/**
 * The One Who Watches: WorldVariables.syncData marks the data dirty, then sends it to the players in that level.
 * Its world-tick procedure calls it every tick for every level; for a level with no players only the first step
 * is kept.
 */
@Mixin(targets = "net.mcreator.theonewhowatches.network.TheOneWhoWatchesModVariables$WorldVariables", remap = false)
public abstract class TowwWorldSyncMixin {
    @Inject(method = "syncData(Lnet/minecraft/world/level/LevelAccessor;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void spookyTickFixes$skipEmptyLevel(LevelAccessor world, CallbackInfo ci) {
        if (SmallGates.noPlayersIn(world)) {
            ((SavedData) (Object) this).m_77762_(); // setDirty, as the original does first
            ci.cancel();
        }
    }
}
