package me.skyttffxx.townyelectionsreloaded.commands.elections;

import me.skyttffxx.townyelectionsreloaded.TownyElections;
import me.skyttffxx.townyelectionsreloaded.commands.SubCommand;
import me.skyttffxx.townyelectionsreloaded.elections.NationElection;
import me.skyttffxx.townyelectionsreloaded.elections.TownElection;
import me.skyttffxx.townyelectionsreloaded.gui.NationStopGui;
import me.skyttffxx.townyelectionsreloaded.gui.TownStopGui;
import org.bukkit.entity.Player;

public class ElectionsStopSubCommand extends SubCommand {

    public ElectionsStopSubCommand() {
        super("stop", 1);
    }

    @Override
    public boolean execute(Player player, String[] args) {
        switch (args[0].toLowerCase()) {

            case "town": {
                if (!TownyElections.hasPerms(player, TownyElections.Permissions.TOWN_STOP)) return true;

                TownElection election = TownyElections.getInstance().getElectionManager().getTownElection(player);
                if (election == null) {
                    player.sendMessage(TownyElections.getMessage("not-active-election"));
                    return true;
                }
                TownStopGui.INVENTORY.open(player);
            } break;

            case "nation": {
                if (!TownyElections.hasPerms(player, TownyElections.Permissions.NATION_STOP)) return true;

                NationElection election = TownyElections.getInstance().getElectionManager().getNationElection(player);
                if (election == null) {
                    player.sendMessage(TownyElections.getMessage("not-active-election-nation"));
                    return true;
                }
                NationStopGui.INVENTORY.open(player);
            } break;

            default:
                return false;

        }
        return true;
    }

}
