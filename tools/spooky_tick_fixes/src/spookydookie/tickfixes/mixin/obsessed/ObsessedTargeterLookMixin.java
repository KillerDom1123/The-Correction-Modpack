package spookydookie.tickfixes.mixin.obsessed;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import spookydookie.tickfixes.SanityHorror;

/**
 * The Obsessed's targeter (the first-contact form): it locked on to the nearest player within ±100 blocks as soon as
 * it had line of sight to them ({@code targeter.hasLineOfSight(player)}, once in the method). Now that player has to
 * look at it, and be at critical sanity. The mod's later checks (not sneaking, not already a target, not immune, no
 * repellant, survival/adventure) still follow.
 */
@Mixin(targets = "net.theobsessed.procedures.IdkStalkerTargeterOnEntityTickUpdateProcedure", remap = false)
public abstract class ObsessedTargeterLookMixin {
    @Redirect(method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V", remap = false,
              at = @At(value = "INVOKE", remap = false,
                       target = "Lnet/minecraft/world/entity/LivingEntity;m_142582_(Lnet/minecraft/world/entity/Entity;)Z"))
    private static boolean spookyTickFixes$lookedAt(LivingEntity targeter, Entity player) {
        return SanityHorror.obsessedTargeterSees(targeter, player);
    }
}
