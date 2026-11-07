package spookydookie.tickfixes.mixin.wendigo;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import spookydookie.tickfixes.WendigoTemper;

/**
 * Antlers' SetstargetProcedure (a Wendigo targets a survival player within ~12 blocks, once per Wendigo) plays
 * "i_see_you" at volume 40 on the MASTER channel, heard by everyone within 640 blocks, and a heartbeat at volume 3.
 * Both server-side calls get their volume capped to {@code [antlersWendigo] soundRange} / 16 (the heartbeat is below it).
 */
@Mixin(targets = "net.mcreator.antlers.procedures.SetstargetProcedure", remap = false)
public abstract class WendigoScreamRangeMixin {
    @ModifyArg(method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
               remap = false, index = 4,
               at = @At(value = "INVOKE", remap = false,
                        target = "Lnet/minecraft/world/level/Level;m_5594_(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    private static float spookyTickFixes$capScream(float volume) {
        return WendigoTemper.capVolume(volume);
    }
}
