package me.skyttffxx.townyelectionsreloaded.commands;

import me.skyttffxx.townyelectionsreloaded.TownyElections;
import me.skyttffxx.townyelectionsreloaded.commands.elections.*;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ElectionsCommandHandler extends CommandHandler {

        public ElectionsCommandHandler() {
                addSubCommand(new ElectionsVoteSubCommand());
                addSubCommand(new ElectionsConvokeSubCommand());
                addSubCommand(new ElectionsListSubCommand());
                addSubCommand(new ElectionsStopSubCommand());
                addSubCommand(new ElectionsUnvoteSubCommand());
                addSubCommand(new ElectionsStatusSubCommand());
                addSubCommand(new ElectionsResultsSubCommand());
                addSubCommand(new ElectionsForceEndSubCommand());
        }

        @Override
        protected boolean executeHelp(CommandSender sender) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', TownyElections.Text.ELECTIONS_HELP_MESSAGE));
                return true;
        }

}
