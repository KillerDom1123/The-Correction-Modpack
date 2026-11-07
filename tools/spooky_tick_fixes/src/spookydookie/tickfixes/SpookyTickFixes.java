package spookydookie.tickfixes;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Wires the config and the event listeners that back the mixins. */
@Mod(SpookyTickFixes.MOD_ID)
public final class SpookyTickFixes {
    public static final String MOD_ID = "spooky_tick_fixes";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public SpookyTickFixes() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TickFixesConfig.SPEC);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(EventPriority.NORMAL, false, ModConfigEvent.Loading.class, e -> onConfig(e.getConfig()));
        modBus.addListener(EventPriority.NORMAL, false, ModConfigEvent.Reloading.class, e -> onConfig(e.getConfig()));
        modBus.addListener(EventPriority.LOW, false, EntityAttributeModificationEvent.class, WatcherFollowRange::onAttributeModification);

        IEventBus forge = MinecraftForge.EVENT_BUS;
        forge.addListener(EventPriority.LOWEST, false, EntityJoinLevelEvent.class, WatcherFollowRange::onJoin);
        // Flush after every other END-phase listener, so syncs they trigger are included.
        forge.addListener(EventPriority.LOWEST, false, TickEvent.ServerTickEvent.class, ObsessedSyncCoalescer::onServerTick);
        forge.addListener(EventPriority.NORMAL, false, PlayerEvent.StartTracking.class, SaturationSyncFilter::onStartTracking);
        forge.addListener(EventPriority.NORMAL, false, EntityLeaveLevelEvent.class, SaturationSyncFilter::onLeave);
        // Count joins first, so a join cancelled later over-counts (fail-open) rather than under-counts.
        forge.addListener(EventPriority.HIGHEST, false, EntityJoinLevelEvent.class, EntityCounters::onJoin);
        forge.addListener(EventPriority.NORMAL, false, EntityLeaveLevelEvent.class, EntityCounters::onLeave);
        forge.addListener(EventPriority.HIGHEST, false, TickEvent.ServerTickEvent.class, ActivationRange::onServerTick);
        forge.addListener(EventPriority.NORMAL, false, ServerStoppedEvent.class, e -> { EntityCounters.clear(); CitadelCache.clear(); });
        forge.addListener(EventPriority.NORMAL, false, RegisterCommandsEvent.class, TickFixesCommand::register);
        // Chunk-safe ticking: the guarded window is a server level's tick, from the first START listener to the last END one.
        forge.addListener(EventPriority.HIGHEST, false, TickEvent.LevelTickEvent.class, e -> {
            if (e.phase == TickEvent.Phase.START && e.side == LogicalSide.SERVER && e.level instanceof ServerLevel sl) ChunkSafeTicking.begin(sl);
        });
        forge.addListener(EventPriority.LOWEST, false, TickEvent.LevelTickEvent.class, e -> {
            if (e.phase == TickEvent.Phase.END && e.side == LogicalSide.SERVER) ChunkSafeTicking.end();
        });
        forge.addListener(EventPriority.NORMAL, false, ChunkEvent.Unload.class, e -> ChunkSafeTicking.onChunkUnload());
        forge.addListener(EventPriority.NORMAL, false, ServerStoppedEvent.class, e -> ChunkSafeTicking.clearAll());

        LOGGER.info("[{}] loaded", MOD_ID);
    }

    private static void onConfig(ModConfig config) {
        if (config.getSpec() != TickFixesConfig.SPEC) return;
        TickFixesConfig.refresh();
        LOGGER.info("[{}] config: {}", MOD_ID, TickFixesConfig.summary());
    }
}
