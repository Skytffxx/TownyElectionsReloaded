package me.skyttffxx.townyelectionsreloaded.commands.elections;

import me.skyttffxx.townyelectionsreloaded.TownyElections;
import me.skyttffxx.townyelectionsreloaded.commands.SubCommand;
import me.skyttffxx.townyelectionsreloaded.elections.ElectionManager;
import me.skyttffxx.townyelectionsreloaded.gui.TownResultsGui;
import me.skyttffxx.townyelectionsreloaded.gui.NationResultsGui;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;

public class ElectionsResultsSubCommand extends SubCommand {

    public ElectionsResultsSubCommand() {
        super("results", 1);
    }

    @Override
    public boolean execute(Player player, String[] args) {

        switch (args[0].toLowerCase()) {

            case "town": {
                Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
                if (resident == null) {
                    player.sendMessage(TownyElections.getMessage("not-in-a-town"));
                    return true;
                }
                Town t;
                try {
                    t = resident.getTown();
                } catch (NotRegisteredException e) {
                    player.sendMessage(TownyElections.getMessage("not-in-a-town"));
                    return true;
                }
                ElectionManager.ElectionResult result = electionManager.getLastTownResult(t.getUUID());
                if (result == null) {
                    player.sendMessage(TownyElections.getMessage("not-active-election"));
                    return true;
                }
                TownResultsGui.open(player, result);
                return true;
            }

            case "nation": {
                Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
                if (resident == null) {
                    player.sendMessage(TownyElections.getMessage("not-in-a-nation"));
                    return true;
                }
                Nation n;
                try {
                    n = resident.getTown().getNation();
                } catch (NotRegisteredException e) {
                    player.sendMessage(TownyElections.getMessage("not-in-a-nation"));
                    return true;
                }
                ElectionManager.ElectionResult result = electionManager.getLastNationResult(n.getUUID());
                if (result == null) {
                    player.sendMessage(TownyElections.getMessage("not-active-election-nation"));
                    return true;
                }
                NationResultsGui.open(player, result);
                return true;
            }

            default:
                return false;
        }
    }

}
