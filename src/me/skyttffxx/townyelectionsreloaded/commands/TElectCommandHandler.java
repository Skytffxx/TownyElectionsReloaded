package me.skyttffxx.townyelectionsreloaded.commands;

import me.skyttffxx.townyelectionsreloaded.TownyElections;
import me.skyttffxx.townyelectionsreloaded.elections.NationElection;
import me.skyttffxx.townyelectionsreloaded.elections.TownElection;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TElectCommandHandler implements CommandExecutor {

        private final TownyElections instance;

        public TElectCommandHandler(TownyElections instance) {
                this.instance = instance;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String str, String[] args) {
                if (args.length < 1) return executeHelp(sender);
                switch (args[0].toLowerCase()) {
                        case "info":     return executeInfo(sender);
                        case "reload":   return executeReload(sender);
                        case "forceend": return executeForceEnd(sender, args);
                        default:
                                sender.sendMessage(ChatColor.RED + "Invalid argument! Use /telect for help.");
                }
                return true;
        }

        private boolean executeHelp(CommandSender sender) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', TownyElections.Text.ELECTIONS_HELP_MESSAGE));
                return true;
        }

        private boolean executeInfo(CommandSender sender) {
                String infoMessage = TownyElections.Text.INFO_MESSAGE
                                .replaceAll("%description%", instance.getDescription().getDescription())
                                .replaceAll("%version%", instance.getDescription().getVersion())
                                .replaceAll("%author%", instance.getDescription().getAuthors().get(0));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', infoMessage));
                return true;
        }

        private boolean executeReload(CommandSender sender) {
                if (!(sender instanceof Player)) {
                        instance.getLanguageData().load();
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                        instance.getLanguageData().getString("plugin-reloaded")));
                        return true;
                }
                if (!TownyElections.hasPerms((Player) sender, "townyelections.reload")) return true;
                instance.getLanguageData().load();
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                instance.getLanguageData().getString("plugin-reloaded")));
                return true;
        }

        private boolean executeForceEnd(CommandSender sender, String[] args) {
                if (!(sender instanceof Player)) {
                        sender.sendMessage(TownyElections.getMessage("only-player"));
                        return true;
                }
                Player player = (Player) sender;
                if (!TownyElections.hasPerms(player, TownyElections.Permissions.ADMIN_FORCEEND)) return true;

                if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED + "Usage: /telect forceend <town|nation> <name>");
                        return true;
                }

                String type = args[1].toLowerCase();
                String name = args[2];

                if (type.equals("town")) {
                        TownElection e = instance.getElectionManager().getTownElectionByName(name);
                        if (e == null) {
                                sender.sendMessage(TownyElections.getMessage("force-end-not-found").replace("%territory%", name));
                                return true;
                        }
                        instance.getElectionManager().forceEndTownElection(e);
                        sender.sendMessage(TownyElections.getMessage("force-end-success").replace("%territory%", name));
                } else if (type.equals("nation")) {
                        NationElection e = instance.getElectionManager().getNationElectionByName(name);
                        if (e == null) {
                                sender.sendMessage(TownyElections.getMessage("force-end-not-found").replace("%territory%", name));
                                return true;
                        }
                        instance.getElectionManager().forceEndNationElection(e);
                        sender.sendMessage(TownyElections.getMessage("force-end-success").replace("%territory%", name));
                } else {
                        sender.sendMessage(ChatColor.RED + "Usage: /telect forceend <town|nation> <name>");
                }
                return true;
        }

}
