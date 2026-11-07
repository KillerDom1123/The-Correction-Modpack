package spookydookie.pregen;

import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;

/** /pregen [status|next|hold|resume|skip|reload], operators only (level 2). */
final class PregenCommand {
    static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.m_82127_("pregen")
                .requires(src -> src.m_6761_(2))
                .executes(c -> reply(c.getSource(), Scheduler::status))
                .then(Commands.m_82127_("status").executes(c -> reply(c.getSource(), Scheduler::status)))
                .then(Commands.m_82127_("next").executes(c -> reply(c.getSource(), Scheduler::previewNext)))
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
