package me.skyttffxx.townyelectionsreloaded.commands.elections;

import me.skyttffxx.townyelectionsreloaded.TownyElections;
import me.skyttffxx.townyelectionsreloaded.commands.SubCommand;
import me.skyttffxx.townyelectionsreloaded.elections.NationElection;
import me.skyttffxx.townyelectionsreloaded.elections.TownElection;
import org.bukkit.entity.Player;

public class ElectionsForceEndSubCommand extends SubCommand {

    public ElectionsForceEndSubCommand() {
        super("forceend", 2);
    }

    @Override
    public boolean execute(Player player, String[] args) {
        if (!TownyElections.hasPerms(player, TownyElections.Permissions.ADMIN_FORCEEND)) return true;

        String territoryName = args[1];

        switch (args[0].toLowerCase()) {

            case "town": {
                TownElection e = electionManager.getTownElectionByName(territoryName);
                if (e == null) {
                    player.sendMessage(TownyElections.getMessage("force-end-not-found")
                            .replace("%territory%", territoryName));
                    return true;
                }
                electionManager.forceEndTownElection(e);
                player.sendMessage(TownyElections.getMessage("force-end-success")
                        .replace("%territory%", territoryName));
                return true;
            }

            case "nation": {
                NationElection e = electionManager.getNationElectionByName(territoryName);
                if (e == null) {
                    player.sendMessage(TownyElections.getMessage("force-end-not-found")
                            .replace("%territory%", territoryName));
                    return true;
                }
                electionManager.forceEndNationElection(e);
                player.sendMessage(TownyElections.getMessage("force-end-success")
                        .replace("%territory%", territoryName));
                return true;
            }

            default:
                return false;
        }
    }

}
