package spookydookie.tickfixes;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.RegisterCommandsEvent;

/** /tickfixes stats — operator-only status line for the activation range and the per-level counters. */
final class TickFixesCommand {
    static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.m_82127_("tickfixes")
                .requires(src -> src.m_6761_(2))
                .then(Commands.m_82127_("stats").executes(ctx -> {
                    CommandSourceStack src = ctx.getSource();
                    ServerLevel level = src.m_81372_();
                    String line = ActivationRange.stats() + " | " + (level == null ? "" : EntityCounters.summary(level))
                            + " | avg tick " + String.format("%.1f", src.m_81377_().m_129903_()) + " ms | " + RayGuard.stats()
                            + " | " + ChunkGuards.stats() + " | " + ChunkSafeTicking.stats();
                    src.m_288197_(() -> Component.m_237113_(line), false);
                    return 1;
                }))
                .then(Commands.m_82127_("probe")
                        .then(Commands.m_82129_("x", IntegerArgumentType.integer())
                                .then(Commands.m_82129_("z", IntegerArgumentType.integer()).executes(ctx -> {
                                    CommandSourceStack src = ctx.getSource();
                                    BlockPos pos = new BlockPos(IntegerArgumentType.getInteger(ctx, "x"), 64, IntegerArgumentType.getInteger(ctx, "z"));
                                    ChunkSafeTicking.Probe.request(src.m_81372_(), pos, msg -> src.m_288197_(() -> Component.m_237113_(msg), false));
                                    src.m_288197_(() -> Component.m_237113_("probe queued for the next tick of " + src.m_81372_().m_46472_().m_135782_()), false);
                                    return 1;
                                })))));
    }

    private TickFixesCommand() {}
}
