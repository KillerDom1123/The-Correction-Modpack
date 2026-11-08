package spookydookie.tickfixes.mixin.obsessed;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.eventbus.api.Event;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spookydookie.tickfixes.SanityHorror;

/**
 * The Obsessed's per-player tick (sanity-bound horror, see SanityHorror):
 * <ul>
 *   <li>HEAD: remember the player and hold their targeter timer while they aren't critical;</li>
 *   <li>at the stalk spawn ({@code TheObsessedModEntities.IDK_STALKER_SPAWN}, read once in the method, just before the
 *       spawn and its bookkeeping): skip it while the target isn't critical, or move a spot inside a base out of it;</li>
 *   <li>every {@code EntityType.spawn} (12 in the method): a targeter needs a critical player and a spot outside a base;
 *       the behind-you/spider hallucinations need a spot outside a base; the rest pass through.</li>
 * </ul>
 */
@Mixin(targets = "net.theobsessed.procedures.PlayerTickProcedure", remap = false)
public abstract class ObsessedSanityTickMixin {
    private static final String EXECUTE =
            "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V";

    @Inject(method = EXECUTE, at = @At("HEAD"), remap = false)
    private static void spookyTickFixes$sanityHead(Event event, LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        SanityHorror.obsessedTickHead(world, entity);
    }

    @Inject(method = EXECUTE, cancellable = true, remap = false,
            at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC, remap = false,
                     target = "Lnet/theobsessed/init/TheObsessedModEntities;IDK_STALKER_SPAWN:Lnet/minecraftforge/registries/RegistryObject;"))
    private static void spookyTickFixes$stalkGate(Event event, LevelAccessor world, double x, double y, double z, Entity entity, CallbackInfo ci) {
        if (SanityHorror.obsessedStalkBlocked(world, entity)) ci.cancel();
    }

    @Redirect(method = EXECUTE, remap = false,
              at = @At(value = "INVOKE", remap = false,
                       target = "Lnet/minecraft/world/entity/EntityType;m_262496_(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;)Lnet/minecraft/world/entity/Entity;"))
    private static Entity spookyTickFixes$spawnFilter(EntityType<?> type, ServerLevel level, BlockPos pos, MobSpawnType reason) {
        return SanityHorror.obsessedSpawn(type, level, pos, reason);
    }
}
