package spookydookie.farsightimmptl.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spookydookie.farsightimmptl.HeldUnloads;

/**
 * Holds the server's chunk unloads in the player's dimension (handleForgetLevelChunk), and drops a
 * hold when the server sends that chunk again (handleLevelChunkWithLight). Both handlers first
 * arrive on the network thread and are re-queued onto the main thread by vanilla; only the
 * main-thread pass is acted on. Under Immersive Portals' packet redirection {@code level} is the
 * packet's target dimension.
 */
@Mixin(value = ClientPacketListener.class, remap = false)
public abstract class ClientPacketListenerMixin {
    @Shadow @Final private Minecraft f_104888_;
    @Shadow private ClientLevel f_104889_;

    @Inject(method = "m_5729_(Lnet/minecraft/network/protocol/game/ClientboundForgetLevelChunkPacket;)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void farsightImmptl$holdUnload(ClientboundForgetLevelChunkPacket packet, CallbackInfo ci) {
        if (f_104888_.m_18695_() && HeldUnloads.tryHold(f_104888_, f_104889_, packet)) {
            ci.cancel();
        }
    }

    @Inject(method = "m_183388_(Lnet/minecraft/network/protocol/game/ClientboundLevelChunkWithLightPacket;)V",
            at = @At("HEAD"), remap = false)
    private void farsightImmptl$chunkResent(ClientboundLevelChunkWithLightPacket packet, CallbackInfo ci) {
        if (f_104888_.m_18695_()) {
            HeldUnloads.cancelHold(f_104889_, packet.m_195717_(), packet.m_195718_());
        }
    }
}
