package me.skyttffxx.townyelectionsreloaded.elections;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import me.skyttffxx.townyelectionsreloaded.TownyElections;
import me.skyttffxx.townyelectionsreloaded.history.HistoryHandler;
import me.skyttffxx.townyelectionsreloaded.integrations.DiscordWebhook;
import me.skyttffxx.townyelectionsreloaded.parties.TownParty;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.google.gson.annotations.Expose;

public class TownElection extends Election {

        @Expose
        private UUID townUuid;

        private Town town;

        public TownElection(long endTime, Town town) {
                super(endTime);
                this.townUuid = town.getUUID();
                territoryUuid = town.getUUID();
                setup();
        }

        @Override
        public void setup() {
                UUID id = townUuid != null ? townUuid : territoryUuid;
                town = TownyUniverse.getInstance().getTown(id);
        }

        public void addVote(UUID player, String candidate) {
                if (!isVotingOpen()) return;
                if (votes.containsKey(player)) return;

                ElectionManager em = instance.getElectionManager();
                if (town != null && em.isTownPartyAtTermLimit(town.getUUID(), candidate)) return;

                if (instance.getPartyManager().getPartiesForTown(town.getName()).stream()
                                .noneMatch(party -> party.getName().equals(candidate)))
                        return;
                votes.put(player, candidate);
        }

        public void removeVote(UUID player) {
                votes.remove(player);
        }

        public String finishElection() {
                if (town == null) return null;

                ElectionManager em = instance.getElectionManager();

                // Build vote count map
                Map<String, AtomicInteger> voteCount = new HashMap<>();
                for (Map.Entry<UUID, String> entry : votes.entrySet()) {
                        voteCount.computeIfAbsent(entry.getValue(), k -> new AtomicInteger(0)).incrementAndGet();
                }
                Map<String, Integer> voteCountSimple = new HashMap<>();
                voteCount.forEach((k, v) -> voteCountSimple.put(k, v.get()));

                int totalResidents = town.getNumResidents();
                int totalVotes = votes.size();

                // Quorum check
                int quorumPct = TownyElections.Configuration.QUORUM_PERCENTAGE;
                if (quorumPct > 0 && totalResidents > 0) {
                        double turnout = (totalVotes * 100.0) / totalResidents;
                        if (turnout < quorumPct) {
                                TownyElections.sendTownMessage(town, TownyElections.getMessage("quorum-not-met"));
                                finalise(em, null, voteCountSimple, totalResidents, totalVotes);
                                return null;
                        }
                }

                if (votes.isEmpty()) {
                        TownyElections.sendTownMessage(town, TownyElections.getMessage("no-winner"));
                        finalise(em, null, voteCountSimple, totalResidents, 0);
                        return null;
                }

                // Find winner (2-pass — avoids false-tie bug with 3+ parties)
                int maxVotes = voteCount.values().stream().mapToInt(AtomicInteger::get).max().orElse(0);
                long topCount = voteCount.values().stream().mapToInt(AtomicInteger::get).filter(v -> v == maxVotes).count();
                if (topCount > 1) {
                        TownyElections.sendTownMessage(town, TownyElections.getMessage("no-winner"));
                        finalise(em, null, voteCountSimple, totalResidents, totalVotes);
                        return null;
                }

                winner = voteCount.entrySet().stream()
                                .filter(e -> e.getValue().get() == maxVotes)
                                .findFirst().get().getKey();

                // Reset losing parties' term counts, increment winner's
                List<TownParty> allParties = instance.getPartyManager().getPartiesForTown(town.getName());
                for (TownParty p : allParties) {
                        if (!p.getName().equalsIgnoreCase(winner)) {
                                em.resetTownTermCount(town.getUUID(), p.getName());
                        }
                }
                em.incrementTownTermCount(town.getUUID(), winner);

                finalise(em, winner, voteCountSimple, totalResidents, totalVotes);

                try {
                        TownParty party = allParties.stream()
                                        .filter(pty -> pty.getName().equalsIgnoreCase(winner))
                                        .collect(Collectors.toList()).get(0);
                        Resident leaderResident = TownyUniverse.getInstance().getResident(party.getLeader());
                        if (leaderResident == null) throw new Exception("Leader resident not found");
                        town.setMayor(leaderResident);
                        String msg = TownyElections.getMessage("election-won").replace("%party%", party.getName());
                        TownyElections.sendTownSubtitle(town, msg);
                } catch (Exception e) {
                        e.printStackTrace();
                }
                return winner;
        }

        private void finalise(ElectionManager em, String winnerName,
                        Map<String, Integer> counts, int residents, int totalVotes) {
                em.storeTownResult(town.getUUID(),
                                new ElectionManager.ElectionResult(town.getName(), winnerName, counts, residents,
                                                System.currentTimeMillis()));
                HistoryHandler.logResult("TOWN", town.getName(), winnerName, counts, residents);
                DiscordWebhook.sendElectionResult("town", town.getName(), winnerName,
                                winnerName != null ? counts.getOrDefault(winnerName, 0) : totalVotes);
        }

        public Town getTown() {
                return town;
        }

}
