package me.skyttffxx.townyelectionsreloaded.commands.elections;

import me.skyttffxx.townyelectionsreloaded.TownyElections;
import me.skyttffxx.townyelectionsreloaded.commands.SubCommand;
import me.skyttffxx.townyelectionsreloaded.elections.Election;
import me.skyttffxx.townyelectionsreloaded.elections.NationElection;
import me.skyttffxx.townyelectionsreloaded.elections.TownElection;
import me.skyttffxx.townyelectionsreloaded.parties.Party;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ElectionsListSubCommand extends SubCommand {

    public ElectionsListSubCommand() {
        super("list", 1);
    }

    @Override
    public boolean execute(Player player, String[] args) {
        Election election;
        List<Party> parties = new ArrayList<>();
        boolean anonymous = TownyElections.Configuration.ANONYMOUS_VOTING;
        UUID territoryUuid = null;
        boolean isTown = false;

        switch (args[0].toLowerCase()) {

            case "town": {
                if (!TownyElections.hasPerms(player, TownyElections.Permissions.TOWN_LIST)) return true;
                election = electionManager.getTownElection(player);
                if (election == null) {
                    player.sendMessage(TownyElections.getMessage("not-active-election"));
                    return true;
                }
                TownElection te = (TownElection) election;
                territoryUuid = te.getTown().getUUID();
                isTown = true;
                parties.addAll(partyManager.getPartiesForTown(te.getTown().getName()));
            } break;

            case "nation": {
                if (!TownyElections.hasPerms(player, TownyElections.Permissions.NATION_LIST)) return true;
                election = electionManager.getNationElection(player);
                if (election == null) {
                    player.sendMessage(TownyElections.getMessage("not-active-election-nation"));
                    return true;
                }
                NationElection ne = (NationElection) election;
                territoryUuid = ne.getNation().getUUID();
                parties.addAll(partyManager.getPartiesForNation(ne.getNation().getName()));
            } break;

            default:
                return false;
        }

        Map<UUID, String> votes = election.getVotes();

        StringBuilder builder = new StringBuilder();
        builder.append(ChatColor.GOLD).append("Candidates:");

        if (parties.isEmpty()) {
            builder.append(ChatColor.RED).append(" There are no candidates");
            player.sendMessage(builder.toString());
            return true;
        }

        for (Party party : parties) {
            builder.append("\n ").append(ChatColor.YELLOW).append(party.getName());

            if (!party.getMotto().isEmpty()) {
                builder.append(ChatColor.GRAY).append(" \"").append(party.getMotto()).append("\"");
            }

            builder.append(ChatColor.WHITE).append(" [").append(party.getMembers().size()).append(" members]");

            if (!anonymous && territoryUuid != null) {
                final UUID tid = territoryUuid;
                long partyVotes = votes.values().stream().filter(v -> v.equals(party.getName())).count();
                builder.append(ChatColor.GREEN).append(" (").append(partyVotes).append(" votes)");
            }
        }

        player.sendMessage(builder.toString());
        return true;
    }

}
