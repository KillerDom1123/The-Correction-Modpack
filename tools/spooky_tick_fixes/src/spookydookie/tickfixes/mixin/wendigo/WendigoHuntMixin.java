package spookydookie.tickfixes.mixin.wendigo;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import spookydookie.tickfixes.TickFixesConfig;

/**
 * Antlers' WendigoOnEntityTickUpdateProcedure (runs for every living entity; its body only acts on Wendigos):
 * <ul>
 *   <li>the night hunt: about every 25 ticks, {@code setTarget} the nearest player within a 600-block box (no line of
 *       sight) and {@code setSprinting(true)}; Speed III follows from the sprint flag;</li>
 *   <li>the stalking roar: {@code playsound antlers:wendigo_calls_out hostile @p ~ ~ ~ 100 1}, the nearest player within
 *       ~355 blocks, about once a minute.</li>
 * </ul>
 * With {@code [antlersWendigo] enabled} these do nothing; WendigoTemper sets the target, the sprint and the roar for a
 * provoked Wendigo instead. Each call appears once in the method (checked against antlers-1.0.0).
 */
@Mixin(targets = "net.mcreator.antlers.procedures.WendigoOnEntityTickUpdateProcedure", remap = false)
public abstract class WendigoHuntMixin {
    private static final String EXECUTE =
            "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V";

    @Redirect(method = EXECUTE, remap = false,
              at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/world/entity/Mob;m_6710_(Lnet/minecraft/world/entity/LivingEntity;)V"))
    private static void spookyTickFixes$noHuntTarget(Mob mob, LivingEntity nearest) {
        if (!TickFixesConfig.wendigoProvokedOnly) mob.m_6710_(nearest);
    }

    @Redirect(method = EXECUTE, remap = false,
              at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/world/entity/Entity;m_6858_(Z)V"))
    private static void spookyTickFixes$noHuntSprint(Entity entity, boolean sprinting) {
        if (!TickFixesConfig.wendigoProvokedOnly) entity.m_6858_(sprinting);
    }

    @Redirect(method = EXECUTE, remap = false,
              at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/commands/Commands;m_230957_(Lnet/minecraft/commands/CommandSourceStack;Ljava/lang/String;)I"))
    private static int spookyTickFixes$noStalkingRoar(Commands commands, CommandSourceStack source, String command) {
        return TickFixesConfig.wendigoProvokedOnly ? 0 : commands.m_230957_(source, command);
    }
}
