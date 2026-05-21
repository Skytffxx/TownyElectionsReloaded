package me.skyttffxx.townyelectionsreloaded.commands.elections;

import me.skyttffxx.townyelectionsreloaded.TownyElections;
import me.skyttffxx.townyelectionsreloaded.commands.SubCommand;
import me.skyttffxx.townyelectionsreloaded.elections.NationElection;
import me.skyttffxx.townyelectionsreloaded.elections.TownElection;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ElectionsStatusSubCommand extends SubCommand {

    public ElectionsStatusSubCommand() {
        super("status", 1);
    }

    @Override
    public boolean execute(Player player, String[] args) {

        switch (args[0].toLowerCase()) {

            case "town": {
                TownElection e = electionManager.getTownElection(player);
                if (e == null) {
                    player.sendMessage(TownyElections.getMessage("not-active-election"));
                    return true;
                }
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        TownyElections.getMessage("election-status-header")));
                player.sendMessage(TownyElections.getMessage("election-status-town")
                        .replace("%territory%", e.getTown().getName()));
                if (!e.isVotingOpen()) {
                    player.sendMessage(TownyElections.getMessage("election-status-campaign")
                            .replace("%minutes%", String.valueOf(e.getCampaignMinutesRemaining())));
                } else {
                    player.sendMessage(TownyElections.getMessage("election-status-time")
                            .replace("%minutes%", String.valueOf(e.getMinutesRemaining())));
                    player.sendMessage(TownyElections.getMessage("election-status-votes")
                            .replace("%votes%", String.valueOf(e.getVotesCount())));
                }
                return true;
            }

            case "nation": {
                NationElection e = electionManager.getNationElection(player);
                if (e == null) {
                    player.sendMessage(TownyElections.getMessage("not-active-election-nation"));
                    return true;
                }
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        TownyElections.getMessage("election-status-header")));
                player.sendMessage(TownyElections.getMessage("election-status-nation")
                        .replace("%territory%", e.getNation().getName()));
                if (!e.isVotingOpen()) {
                    player.sendMessage(TownyElections.getMessage("election-status-campaign")
                            .replace("%minutes%", String.valueOf(e.getCampaignMinutesRemaining())));
                } else {
                    player.sendMessage(TownyElections.getMessage("election-status-time")
                            .replace("%minutes%", String.valueOf(e.getMinutesRemaining())));
                    player.sendMessage(TownyElections.getMessage("election-status-votes")
                            .replace("%votes%", String.valueOf(e.getVotesCount())));
                }
                return true;
            }

            default:
                return false;
        }
    }

}
