package spookydookie.pregen;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Pre-generates the world with Chunky while the dedicated server is empty, and pauses the moment a player logs in.
 * Does nothing on a client or in singleplayer. See tools/spooky_pregen/README.md.
 */
@Mod(SpookyPregen.MOD_ID)
public final class SpookyPregen {
    public static final String MOD_ID = "spooky_pregen";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public SpookyPregen() {
        IEventBus forge = MinecraftForge.EVENT_BUS;
        forge.addListener(EventPriority.LOW, false, ServerStartedEvent.class, e -> Scheduler.onServerStarted(e.getServer()));
        forge.addListener(EventPriority.HIGH, false, ServerStoppingEvent.class, e -> Scheduler.onServerStopping());
        forge.addListener(EventPriority.NORMAL, false, TickEvent.ServerTickEvent.class, Scheduler::onServerTick);
        // Pause before anything else reacts to the join.
        forge.addListener(EventPriority.HIGHEST, false, PlayerEvent.PlayerLoggedInEvent.class,
                e -> Scheduler.onPlayerJoin(e.getEntity().m_6302_()));
        forge.addListener(EventPriority.NORMAL, false, RegisterCommandsEvent.class, PregenCommand::register);
        forge.addListener(EventPriority.NORMAL, false, PlayerEvent.PlayerLoggedOutEvent.class, e -> {
            if (e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p) Scheduler.onPlayerLogout(p);
        });
        LOGGER.info("[{}] loaded", MOD_ID);
    }
}
