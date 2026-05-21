package me.skyttffxx.townyelectionsreloaded.commands.party;

import me.skyttffxx.townyelectionsreloaded.TownyElections;
import me.skyttffxx.townyelectionsreloaded.commands.SubCommand;
import me.skyttffxx.townyelectionsreloaded.parties.Party;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PartyInfoSubCommand extends SubCommand {

    public PartyInfoSubCommand() {
        super("info", 1);
    }

    @Override
    public boolean execute(Player player, String[] args) {
        Party party;
        switch (args[0].toLowerCase()) {
            case "town": {
                if (!TownyElections.hasPerms(player, TownyElections.Permissions.TOWNPARTY_INFO)) return true;
                party = partyManager.getPlayerTownParty(player.getUniqueId());
            } break;
            case "nation": {
                if (!TownyElections.hasPerms(player, TownyElections.Permissions.NATIONPARTY_INFO)) return true;
                party = partyManager.getPlayerNationParty(player.getUniqueId());
            } break;
            default:
                return false;
        }

        if (party == null) {
            player.sendMessage(TownyElections.getMessage("not-in-a-party"));
            return true;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(ChatColor.GOLD + "" + ChatColor.BOLD + party.getName() + "\n" + ChatColor.RESET);
        builder.append(TownyElections.getMessage("leader"));
        builder.append(Bukkit.getOfflinePlayer(party.getLeader()).getName());
        builder.append("\n");
        builder.append(TownyElections.getMessage("assistants"));
        for (int i = 0; i < party.getAssistants().size(); i++) {
            if (i > 0) builder.append(", ");
            builder.append(Bukkit.getOfflinePlayer(party.getAssistants().get(i)).getName());
        }
        builder.append("\n");
        builder.append(TownyElections.getMessage("members"));
        for (int i = 0; i < party.getMembers().size(); i++) {
            if (i > 0) builder.append(", ");
            builder.append(Bukkit.getOfflinePlayer(party.getMembers().get(i)).getName());
        }
        player.sendMessage(builder.toString());
        return true;
    }

}
