package me.skyttffxx.townyelectionsreloaded.commands.party;

import me.skyttffxx.townyelectionsreloaded.TownyElections;
import me.skyttffxx.townyelectionsreloaded.commands.SubCommand;
import me.skyttffxx.townyelectionsreloaded.parties.Party;
import org.bukkit.entity.Player;

public class PartySetMottoSubCommand extends SubCommand {

    public PartySetMottoSubCommand() {
        super("setmotto", 2);
    }

    @Override
    public boolean execute(Player player, String[] args) {
        Party party;

        switch (args[0].toLowerCase()) {

            case "town": {
                if (!TownyElections.hasPerms(player, TownyElections.Permissions.TOWNPARTY_SETMOTTO)) return true;
                party = partyManager.getPlayerTownParty(player.getUniqueId());
            } break;

            case "nation": {
                if (!TownyElections.hasPerms(player, TownyElections.Permissions.NATIONPARTY_SETMOTTO)) return true;
                party = partyManager.getPlayerNationParty(player.getUniqueId());
            } break;

            default:
                return false;
        }

        if (party == null) {
            player.sendMessage(TownyElections.getMessage("not-in-a-party"));
            return true;
        }

        if (!party.getLeader().equals(player.getUniqueId()) && !party.isAssistant(player.getUniqueId())) {
            player.sendMessage(TownyElections.getMessage("not-leader"));
            return true;
        }

        String motto = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        party.setMotto(motto);
        player.sendMessage(TownyElections.getMessage("motto-set").replace("%motto%", motto));
        return true;
    }

}
