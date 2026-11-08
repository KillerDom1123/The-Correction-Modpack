package spookydookie.tickfixes.mixin.cantbreathe;

import de.cadentem.cant_breathe.util.Timer;
import de.cadentem.cant_breathe.util.Utils;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spookydookie.tickfixes.SanityHorror;

/**
 * CANTBREATHE's per-dimension spawn logic (sanity-bound horror, see SanityHorror). Its timer still advances while any
 * survival player is in the dimension (isRelevantPlayer is untouched), but:
 * <ul>
 *   <li>handleLogic's victim pick ({@code players.get(random)}, once in the method) takes a critical player when there
 *       is one;</li>
 *   <li>its spawn attempt ({@code Utils.trySpawnMob}, once) fails for a victim who isn't critical; the mod then clears
 *       the victim and picks again next tick;</li>
 *   <li>its cave noise (playCaveSoundToSpelunkers) is skipped for a victim who isn't critical (the noise timer is still
 *       reset), and its per-player send ({@code List.forEach}, once) only reaches critical players.</li>
 * </ul>
 */
@Mixin(targets = "de.cadentem.cant_breathe.CantBreathe", remap = false)
public abstract class CantBreatheLogicMixin {
    private static final String HANDLE_LOGIC = "handleLogic(Lnet/minecraft/server/level/ServerLevel;)V";
    private static final String NOISE = "playCaveSoundToSpelunkers(Ljava/util/List;Lde/cadentem/cant_breathe/util/Timer;)V";

    @Redirect(method = HANDLE_LOGIC, remap = false,
              at = @At(value = "INVOKE", remap = false, target = "Ljava/util/List;get(I)Ljava/lang/Object;"))
    private Object spookyTickFixes$pickVictim(List<?> players, int index) {
        return SanityHorror.cbPickVictim(players, index);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Redirect(method = HANDLE_LOGIC, remap = false,
              at = @At(value = "INVOKE", remap = false,
                       target = "Lde/cadentem/cant_breathe/util/Utils;trySpawnMob(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;IIILnet/minecraft/util/SpawnUtil$Strategy;)Ljava/util/Optional;"))
    private Optional spookyTickFixes$spawnForCriticalOnly(Entity victim, EntityType type, MobSpawnType reason, ServerLevel level, BlockPos pos,
                                                          int attempts, int range, int vertical, SpawnUtil.Strategy strategy) {
        if (!SanityHorror.cbVictimAllowed(victim)) {
            SanityHorror.cbAttemptRefused();
            return Optional.empty();
        }
        return Utils.trySpawnMob(victim, type, reason, level, pos, attempts, range, vertical, strategy);
    }

    @Inject(method = NOISE, at = @At("HEAD"), cancellable = true, remap = false)
    private void spookyTickFixes$noiseForCriticalOnly(List<ServerPlayer> players, Timer timer, CallbackInfo ci) {
        if (timer.currentVictim != null && !SanityHorror.cbVictimAllowed(timer.currentVictim)) {
            timer.resetNoiseTimer();
            SanityHorror.cbNoiseMuted();
            ci.cancel();
        }
    }

    @Redirect(method = NOISE, remap = false,
              at = @At(value = "INVOKE", remap = false, target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V"))
    private void spookyTickFixes$noiseListeners(List<ServerPlayer> players, Consumer<? super ServerPlayer> send) {
        SanityHorror.cbNoise(players, send);
    }
}
