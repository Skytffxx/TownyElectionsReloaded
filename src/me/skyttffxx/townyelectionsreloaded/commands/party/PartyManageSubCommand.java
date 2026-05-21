package me.skyttffxx.townyelectionsreloaded.commands.party;

import me.skyttffxx.townyelectionsreloaded.TownyElections;
import me.skyttffxx.townyelectionsreloaded.commands.SubCommand;
import me.skyttffxx.townyelectionsreloaded.gui.PartyManageGui;
import me.skyttffxx.townyelectionsreloaded.parties.NationParty;
import me.skyttffxx.townyelectionsreloaded.parties.TownParty;
import org.bukkit.entity.Player;

public class PartyManageSubCommand extends SubCommand {

    public PartyManageSubCommand() {
        super("manage", 1);
    }

    @Override
    public boolean execute(Player player, String[] args) {

        switch (args[0].toLowerCase()) {

            case "town": {
                if (!TownyElections.hasPerms(player, TownyElections.Permissions.TOWNPARTY_INFO)) return true;
                TownParty party = partyManager.getPlayerTownParty(player.getUniqueId());
                if (party == null) {
                    player.sendMessage(TownyElections.getMessage("not-in-a-party"));
                    return true;
                }
                PartyManageGui.open(player, party);
                return true;
            }

            case "nation": {
                if (!TownyElections.hasPerms(player, TownyElections.Permissions.NATIONPARTY_INFO)) return true;
                NationParty party = partyManager.getPlayerNationParty(player.getUniqueId());
                if (party == null) {
                    player.sendMessage(TownyElections.getMessage("not-in-a-party"));
                    return true;
                }
                PartyManageGui.open(player, party);
                return true;
            }

            default:
                return false;
        }
    }

}
