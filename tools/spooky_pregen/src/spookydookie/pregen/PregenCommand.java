package spookydookie.pregen;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;

/**
 * {@code /pregen}: operators only (permission level 2), from chat or the server console. Only {@code radius} takes an
 * argument.
 * <ul>
 *   <li>{@code /pregen radius}: shows the blob size in force (the largest radius a blob grows to) and where it comes
 *       from. {@code /pregen radius <blocks>} (16 to 8192) sets it at once, for the blob in progress too: if that blob
 *       is already larger, its running square is cancelled and a new blob is picked. It's kept across restarts.
 *       {@code /pregen radius reset} returns to the config's blobMaxRadius.</li>
 *   <li>{@code /pregen} or {@code /pregen status}: enabled or not, player count, how long the server has been empty
 *       (and the grace time), hold, guards, free disk, the current Chunky task and its progress (percent, chunks, rate,
 *       ETA), the current blob and why it was picked, the anchors (count and newest), and each job's next radius.</li>
 *   <li>{@code /pregen next}: a dry run of the next pick (job, blob or edge square) and why.</li>
 *   <li>{@code /pregen start}: force-start. Begins (or resumes) work immediately, without waiting for an empty server
 *       or the check interval, alongside any players already online. It clears a hold. The next player to join pauses
 *       the task and ends force mode, as do {@code /pregen stop}, {@code /pregen hold} and a restart. The disk guards
 *       still apply.</li>
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
                .then(Commands.m_82127_("reload").executes(c -> reply(c.getSource(), Scheduler::reloadConfig)))
                // radius [<blocks> | reset]: show or set the blob size (the largest radius a blob grows to)
                .then(Commands.m_82127_("radius").executes(c -> reply(c.getSource(), Scheduler::radius))
                        .then(Commands.m_82127_("reset").executes(c -> reply(c.getSource(), () -> Scheduler.setRadius(null))))
                        .then(Commands.m_82129_("blocks", IntegerArgumentType.integer(16, 8192))
                                .executes(c -> reply(c.getSource(), () -> Scheduler.setRadius(IntegerArgumentType.getInteger(c, "blocks")))))));
    }

    private static int reply(CommandSourceStack src, Supplier<String> action) {
        String text = action.get();
        src.m_288197_(() -> Component.m_237113_(text), false);
        return 1;
    }

    private PregenCommand() {}
}
