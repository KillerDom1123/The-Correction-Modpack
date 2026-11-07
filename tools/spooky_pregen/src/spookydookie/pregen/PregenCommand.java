package spookydookie.pregen;

import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;

/**
 * {@code /pregen}: operators only (permission level 2), from chat or the server console. None of the subcommands takes
 * arguments.
 * <ul>
 *   <li>{@code /pregen} or {@code /pregen status}: enabled or not, player count, how long the server has been empty
 *       (and the grace time), hold, guards, free disk, the current Chunky task and its progress (percent, chunks, rate,
 *       ETA), the current blob and why it was picked, the anchors (count and newest), and each job's next radius.</li>
 *   <li>{@code /pregen next}: a dry run of the next pick (job, blob or edge square) and why.</li>
 *   <li>{@code /pregen start}: force-start. Begins (or resumes) work immediately, without waiting for an empty server
 *       or the check interval, and keeps running with players online. It clears a hold. Lasts until
 *       {@code /pregen stop}, {@code /pregen hold} or a restart. The disk guards still apply.</li>
 *   <li>{@code /pregen stop}: ends force mode and returns to running only while the server is empty. With players
 *       online, the current task pauses at once.</li>
 *   <li>{@code /pregen hold}: stops all work (also ending force mode) and pauses the current task. It persists across
 *       restarts.</li>
 *   <li>{@code /pregen resume}: allows work again. It resumes once the server has been empty for the grace time.</li>
 *   <li>{@code /pregen skip}: cancels the current task and moves on. A job or blob goes to its next radius (after its
 *       last radius the blob is finished); an edge square counts as done for this session.</li>
 *   <li>{@code /pregen reload}: re-reads config/spooky_pregen.json. If the file doesn't parse, the previous settings
 *       stay and the reply gives the error.</li>
 * </ul>
 * Documented for players in README.md (Custom commands) and in tools/spooky_pregen/README.md (Commands).
 */
final class PregenCommand {
    static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.m_82127_("pregen")
                .requires(src -> src.m_6761_(2))
                .executes(c -> reply(c.getSource(), Scheduler::status))
                .then(Commands.m_82127_("status").executes(c -> reply(c.getSource(), Scheduler::status)))
                .then(Commands.m_82127_("next").executes(c -> reply(c.getSource(), Scheduler::previewNext)))
                .then(Commands.m_82127_("start").executes(c -> reply(c.getSource(), Scheduler::forceStart)))
                .then(Commands.m_82127_("stop").executes(c -> reply(c.getSource(), Scheduler::forceStop)))
                .then(Commands.m_82127_("hold").executes(c -> reply(c.getSource(), Scheduler::hold)))
                .then(Commands.m_82127_("resume").executes(c -> reply(c.getSource(), Scheduler::resume)))
                .then(Commands.m_82127_("skip").executes(c -> reply(c.getSource(), Scheduler::skip)))
                .then(Commands.m_82127_("reload").executes(c -> reply(c.getSource(), Scheduler::reloadConfig))));
    }

    private static int reply(CommandSourceStack src, Supplier<String> action) {
        String text = action.get();
        src.m_288197_(() -> Component.m_237113_(text), false);
        return 1;
    }

    private PregenCommand() {}
}
