package spookydookie.tickfixes.mixin;

import net.minecraft.world.entity.Entity;
import net.satisfy.farm_and_charm.core.network.packet.SyncSaturationPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spookydookie.tickfixes.SaturationSyncFilter;

/** Farm & Charm: only send an animal's saturation sync when it changed (plus refresh / start-tracking). */
@Mixin(targets = "net.satisfy.farm_and_charm.core.network.PacketHandler", remap = false)
public abstract class FarmCharmSaturationMixin {
    @Inject(method = "sendSaturationSync(Lnet/satisfy/farm_and_charm/core/network/packet/SyncSaturationPacket;Lnet/minecraft/world/entity/Entity;)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void spookyTickFixes$onlyOnChange(SyncSaturationPacket packet, Entity entity, CallbackInfo ci) {
        if (entity != null && packet != null
                && !SaturationSyncFilter.shouldSend(entity, packet.getLevel(), packet.getFoodCounter())) {
            ci.cancel();
        }
    }
}
