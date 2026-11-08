package spookydookie.tickfixes.mixin.cantbreathe;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import spookydookie.tickfixes.SanityHorror;

/**
 * CANTBREATHE's spawn search (Utils.trySpawnMob: 40 random spots within 35 blocks of the victim): a spot inside a base
 * fails the mob's spawn-rule check ({@code Mob.checkSpawnRules}, once in the method), so the search moves on.
 */
@Mixin(targets = "de.cadentem.cant_breathe.util.Utils", remap = false)
public abstract class CantBreatheSpawnSpotMixin {
    @Redirect(method = "trySpawnMob(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;IIILnet/minecraft/util/SpawnUtil$Strategy;)Ljava/util/Optional;",
              remap = false,
              at = @At(value = "INVOKE", remap = false,
                       target = "Lnet/minecraft/world/entity/Mob;m_5545_(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/MobSpawnType;)Z"))
    private static boolean spookyTickFixes$notInBase(Mob mob, LevelAccessor level, MobSpawnType reason) {
        return mob.m_5545_(level, reason) && !SanityHorror.cbSpotInBase(level, mob);
    }
}
