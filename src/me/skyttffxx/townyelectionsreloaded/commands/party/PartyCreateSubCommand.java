package me.skyttffxx.townyelectionsreloaded.commands.party;

import me.skyttffxx.townyelectionsreloaded.TownyElections;
import me.skyttffxx.townyelectionsreloaded.commands.SubCommand;
import me.skyttffxx.townyelectionsreloaded.parties.NationParty;
import me.skyttffxx.townyelectionsreloaded.parties.Party;
import me.skyttffxx.townyelectionsreloaded.parties.TownParty;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;

public class PartyCreateSubCommand extends SubCommand {

    public PartyCreateSubCommand() {
        super("create", 2);
    }

    @Override
    public boolean execute(Player player, String[] args) {
        Party party;

        switch (args[0].toLowerCase()) {
            case "town": {

                if (!player.hasPermission(TownyElections.Permissions.TOWNPARTY_CREATE)) {
                    player.sendMessage(TownyElections.getMessage("no-permission"));
                    return true;
                }

                party = partyManager.getPlayerTownParty(player.getUniqueId());
                if (party != null) {
                    player.sendMessage(TownyElections.getMessage("already-in-a-party"));
                    return true;
                }

                Town town;
                try {
                    town = TownyUniverse.getInstance().getResident(player.getUniqueId()).getTown();
                } catch (NotRegisteredException e) {
                    player.sendMessage(TownyElections.getMessage("not-in-a-town"));
                    return true;
                }

                if (partyManager.getPartiesForTown(town.getName()).stream()
                        .anyMatch(townParty -> townParty.getName().equalsIgnoreCase(args[1]))) {
                    player.sendMessage(TownyElections.getMessage("name-taken"));
                    return true;
                }

                party = new TownParty(args[1], player.getUniqueId(), town.getUUID());
            } break;

            case "nation": {

                if (!player.hasPermission(TownyElections.Permissions.NATIONPARTY_CREATE)) {
                    player.sendMessage(TownyElections.getMessage("no-permission"));
                    return true;
                }

                party = partyManager.getPlayerNationParty(player.getUniqueId());
                if (party != null) {
                    player.sendMessage(TownyElections.getMessage("already-in-a-party"));
                    return true;
                }

                Nation nation;
                try {
                    nation = TownyUniverse.getInstance().getResident(player.getUniqueId()).getTown().getNation();
                } catch (NotRegisteredException e) {
                    player.sendMessage(TownyElections.getMessage("not-in-a-nation"));
                    return true;
                }

                if (partyManager.getPartiesForNation(nation.getName()).stream()
                        .anyMatch(nationParty -> nationParty.getName().equalsIgnoreCase(args[1]))) {
                    player.sendMessage(TownyElections.getMessage("name-taken"));
                    return true;
                }

                party = new NationParty(args[1], player.getUniqueId(), nation.getUUID());
            } break;

            default:
                return false;

        }

        partyManager.addParty(party);
        player.sendMessage(TownyElections.getMessage("party-created"));
        return true;
    }

}
