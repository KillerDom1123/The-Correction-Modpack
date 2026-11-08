package spookydookie.tickfixes.mixin.obsessed;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spookydookie.tickfixes.SanityHorror;

/**
 * The Obsessed's block-break surprise (a target breaking a block: 1 in 100, then a 64,000-tick cooldown): the spawn
 * (once in the method) needs a critical player and a spot outside a base. The cooldown is set before the spawn, so a
 * blocked surprise still uses it up. The rest of the method (no block breaking in its lair) is untouched.
 */
@Mixin(targets = "net.theobsessed.procedures.BlockBrokenProcedure", remap = false)
public abstract class ObsessedSurpriseMixin {
    private static final String EXECUTE =
            "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V";

    @Inject(method = EXECUTE, at = @At("HEAD"), remap = false)
    private static void spookyTickFixes$breaker(Event event, LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        SanityHorror.blockBreakHead(world, entity);
    }

    @Redirect(method = EXECUTE, remap = false,
              at = @At(value = "INVOKE", remap = false,
                       target = "Lnet/minecraft/world/entity/EntityType;m_262496_(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;)Lnet/minecraft/world/entity/Entity;"))
    private static Entity spookyTickFixes$surprise(EntityType<?> type, ServerLevel level, BlockPos pos, MobSpawnType reason) {
        return SanityHorror.obsessedSurprise(type, level, pos, reason);
    }
}
