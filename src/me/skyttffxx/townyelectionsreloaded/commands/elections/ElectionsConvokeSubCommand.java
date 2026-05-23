package me.skyttffxx.townyelectionsreloaded.commands.elections;

import me.skyttffxx.townyelectionsreloaded.TownyElections;
import me.skyttffxx.townyelectionsreloaded.commands.SubCommand;
import me.skyttffxx.townyelectionsreloaded.elections.NationElection;
import me.skyttffxx.townyelectionsreloaded.elections.TownElection;
import me.skyttffxx.townyelectionsreloaded.integrations.DiscordWebhook;
import me.skyttffxx.townyelectionsreloaded.integrations.VaultHook;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class ElectionsConvokeSubCommand extends SubCommand {

    public ElectionsConvokeSubCommand() {
        super("convoke", 2);
    }

    @Override
    public boolean execute(Player player, String[] args) {
        long finishTime;
        try {
            finishTime = Integer.parseInt(args[1]) * 60L * 1000L;
        } catch (Exception e) {
            player.sendMessage(TownyElections.getMessage("error-input-number"));
            return false;
        }

        switch (args[0].toLowerCase()) {

            case "town": {
                if (!TownyElections.hasPerms(player, TownyElections.Permissions.TOWN_CONVOKE)) return true;

                Town t;
                try {
                    t = TownyUniverse.getInstance().getResident(player.getUniqueId()).getTown();
                } catch (NotRegisteredException e) {
                    player.sendMessage(TownyElections.getMessage("not-in-a-town"));
                    return true;
                }

                if (electionManager.getTownElection(player) != null) {
                    player.sendMessage(TownyElections.getMessage("active-election"));
                    return true;
                }

                if (electionManager.hasTownCooldown(t.getUUID())) {
                    player.sendMessage(TownyElections.getMessage("election-cooldown")
                            .replace("%minutes%", String.valueOf(electionManager.getTownCooldownMinutes(t.getUUID()))));
                    return true;
                }

                if (doChecks(player, args[1])) return true;

                double fee = TownyElections.Configuration.ELECTION_FEE;
                if (fee > 0) {
                    if (!VaultHook.has(player, fee)) {
                        player.sendMessage(TownyElections.getMessage("no-funds-election")
                                .replace("%cost%", VaultHook.format(fee)));
                        return true;
                    }
                    VaultHook.charge(player, fee);
                    player.sendMessage(TownyElections.getMessage("election-fee-charged")
                            .replace("%cost%", VaultHook.format(fee)));
                }

                long endTime = finishTime + System.currentTimeMillis();
                TownElection e = new TownElection(endTime, t);
                electionManager.addTownElection(e);
                TownyElections.sendTownSubtitle(t, TownyElections.getMessage("election-convoked")
                        .replace("%time%", args[1]));
                DiscordWebhook.sendElectionStarted("town", t.getName(), endTime);
            } break;

            case "nation": {
                if (!TownyElections.hasPerms(player, TownyElections.Permissions.NATION_CONVOKE)) return true;

                Nation n;
                try {
                    n = TownyUniverse.getInstance().getResident(player.getUniqueId()).getTown().getNation();
                } catch (NotRegisteredException e) {
                    player.sendMessage(TownyElections.getMessage("not-in-a-nation"));
                    return true;
                }

                if (electionManager.getNationElection(player) != null) {
                    player.sendMessage(TownyElections.getMessage("active-election"));
                    return true;
                }

                if (electionManager.hasNationCooldown(n.getUUID())) {
                    player.sendMessage(TownyElections.getMessage("election-cooldown")
                            .replace("%minutes%", String.valueOf(electionManager.getNationCooldownMinutes(n.getUUID()))));
                    return true;
                }

                if (doChecks(player, args[1])) return true;

                double fee = TownyElections.Configuration.ELECTION_FEE;
                if (fee > 0) {
                    if (!VaultHook.has(player, fee)) {
                        player.sendMessage(TownyElections.getMessage("no-funds-election")
                                .replace("%cost%", VaultHook.format(fee)));
                        return true;
                    }
                    VaultHook.charge(player, fee);
                    player.sendMessage(TownyElections.getMessage("election-fee-charged")
                            .replace("%cost%", VaultHook.format(fee)));
                }

                long endTime = finishTime + System.currentTimeMillis();
                NationElection e = new NationElection(n, endTime);
                electionManager.addNationElection(e);
                TownyElections.sendNationSubtitle(n, TownyElections.getMessage("election-convoked")
                        .replace("%time%", args[1]));
                DiscordWebhook.sendElectionStarted("nation", n.getName(), endTime);
            } break;

            default:
                return false;
        }

        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_FALL, 1, 1);
        player.sendMessage(TownyElections.getMessage("election-convoked").replace("%time%", args[1]));
        return true;
    }

    private boolean doChecks(Player player, String time) {
        long finishTime;
        try {
            finishTime = Long.parseLong(time) * 60L * 1000L;
        } catch (Exception e) {
            player.sendMessage(TownyElections.getMessage("error-input-string"));
            return true;
        }
        if (finishTime / 60_000L < TownyElections.Configuration.MIN_DURATION) {
            player.sendMessage(TownyElections.getMessage("min-duration")
                    .replace("%min%", String.valueOf(TownyElections.Configuration.MIN_DURATION)));
            return true;
        }
        if (finishTime / 60_000L > TownyElections.Configuration.MAX_DURATION) {
            player.sendMessage(TownyElections.getMessage("max-duration")
                    .replace("%max%", String.valueOf(TownyElections.Configuration.MAX_DURATION)));
            return true;
        }
        return false;
    }

}
