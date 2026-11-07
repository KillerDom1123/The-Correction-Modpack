package spookydookie.tickfixes;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.RegisterCommandsEvent;

/**
 * {@code /tickfixes}: operator-only diagnostics (permission level 2), from chat or the server console. The console runs
 * in the overworld.
 *
 * <ul>
 *   <li>{@code /tickfixes stats}: no arguments. Replies with one status line; every count runs from server start.
 *     <ul>
 *       <li>activation range: whether it's enabled and active, the adaptive thresholds, the range, the interval, and
 *           the far mobs skipped last tick;</li>
 *       <li>for the sender's dimension: loaded Immersive Portals portals and Alex's Caves moving blocks;</li>
 *       <li>the server's average tick time;</li>
 *       <li>horror ray casts: guarded, duplicates reused, stopped at unloaded chunks, and Obsessed ticks deferred;</li>
 *       <li>chunk guards: villager bed reads and Obsessed block reads answered without loading, and off-thread chunk
 *           requests with the top callers;</li>
 *       <li>chunk-safe ticking: reads answered with a barrier and their top callers, disk loads, memory loads, and disk
 *           checks that timed out.</li>
 *     </ul></li>
 *   <li>{@code /tickfixes wendigo}: no arguments. Lists every loaded Antlers Wendigo (night form) with its position, its
 *       distance from the sender, and whether it is calm or provoked (by whom, and how), plus the provoke/calm counts
 *       since server start (see WendigoTemper).</li>
 *   <li>{@code /tickfixes probe <x> <z>}: tests what chunk-safe ticking does at a position.
 *     <ul>
 *       <li><b>x</b>, <b>z</b>: whole-number block coordinates in the sender's dimension; y is always 64.</li>
 *       <li>One {@code Level.getBlockState} read is queued for the start of that dimension's next tick, inside the
 *           guarded window. The reply gives the block found, the path that answered it (fast path / loaded from memory /
 *           loaded from disk / barrier, with the reason / exempt caller), and how long the read took.</li>
 *       <li>Probing terrain that doesn't exist yet doesn't generate it. Probing a generated but unloaded chunk loads it,
 *           as any read would.</li>
 *       <li>Only one probe is pending at a time; a new probe replaces one that hasn't run yet.</li>
 *     </ul></li>
 * </ul>
 */
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
                .then(Commands.m_82127_("wendigo").executes(ctx -> {
                    CommandSourceStack src = ctx.getSource();
                    for (String line : WendigoTemper.describe(src.m_230896_())) src.m_288197_(() -> Component.m_237113_(line), false);
                    return 1;
                }))
                // probe <x> <z>: block coordinates in the sender's dimension (y = 64); runs at the start of its next tick
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
