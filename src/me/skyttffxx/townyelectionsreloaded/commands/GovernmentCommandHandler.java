package me.skyttffxx.townyelectionsreloaded.commands;

import me.skyttffxx.townyelectionsreloaded.commands.government.GovernmentListSubCommand;
import org.bukkit.command.CommandSender;

public class GovernmentCommandHandler extends CommandHandler {

    public GovernmentCommandHandler() {
        addSubCommand(new GovernmentListSubCommand());
    }

    @Override
    protected boolean executeHelp(CommandSender sender) {
        return false;
    }

}
