package me.skyttffxx.townyelectionsreloaded.commands.party;

import me.skyttffxx.townyelectionsreloaded.TownyElections;
import me.skyttffxx.townyelectionsreloaded.commands.SubCommand;
import me.skyttffxx.townyelectionsreloaded.parties.Party;
import org.bukkit.entity.Player;

public class PartyDisbandSubCommand extends SubCommand {

    public PartyDisbandSubCommand() {
        super("disband", 1);
    }

    @Override
    public boolean execute(Player player, String[] args) {
        Party party;

        switch (args[0].toLowerCase()) {

            case "town": {
                if (!TownyElections.hasPerms(player, TownyElections.Permissions.TOWNPARTY_DISBAND)) return true;
                party = partyManager.getPlayerTownParty(player.getUniqueId());
            } break;

            case "nation": {
                if (!TownyElections.hasPerms(player, TownyElections.Permissions.NATIONPARTY_DISBAND)) return true;
                party = partyManager.getPlayerNationParty(player.getUniqueId());
            } break;

            default:
                return false;
        }

        if (party == null) {
            player.sendMessage(TownyElections.getMessage("not-in-a-party"));
            return true;
        }

        if (!party.getLeader().equals(player.getUniqueId())) {
            player.sendMessage(TownyElections.getMessage("not-leader"));
            return true;
        }

        String partyName = party.getName();
        partyManager.removeParty(party);
        player.sendMessage(TownyElections.getMessage("party-disbanded").replace("%party%", partyName));
        return true;
    }

}
