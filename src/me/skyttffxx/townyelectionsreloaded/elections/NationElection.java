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
import me.skyttffxx.townyelectionsreloaded.parties.NationParty;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.google.gson.annotations.Expose;

public class NationElection extends Election {

        @Expose
        private UUID nationUuid;

        private Nation nation;

        public NationElection(Nation nation, long endTime) {
                super(endTime);
                this.nationUuid = nation.getUUID();
                territoryUuid = nation.getUUID();
                setup();
        }

        @Override
        public void setup() {
                UUID id = nationUuid != null ? nationUuid : territoryUuid;
                nation = TownyUniverse.getInstance().getNation(id);
        }

        public void addVote(UUID player, String candidate) {
                if (!isVotingOpen()) return;
                if (votes.containsKey(player)) return;

                ElectionManager em = instance.getElectionManager();
                if (nation != null && em.isNationPartyAtTermLimit(nation.getUUID(), candidate)) return;

                if (instance.getPartyManager().getPartiesForNation(nation.getName()).stream()
                                .noneMatch(party -> party.getName().equals(candidate)))
                        return;
                votes.put(player, candidate);
        }

        public void removeVote(UUID player) {
                votes.remove(player);
        }

        public String finishElection() {
                if (nation == null) return null;

                ElectionManager em = instance.getElectionManager();

                Map<String, AtomicInteger> voteCount = new HashMap<>();
                for (Map.Entry<UUID, String> entry : votes.entrySet()) {
                        voteCount.computeIfAbsent(entry.getValue(), k -> new AtomicInteger(0)).incrementAndGet();
                }
                Map<String, Integer> voteCountSimple = new HashMap<>();
                voteCount.forEach((k, v) -> voteCountSimple.put(k, v.get()));

                int totalResidents = nation.getNumResidents();
                int totalVotes = votes.size();

                // Quorum check
                int quorumPct = TownyElections.Configuration.QUORUM_PERCENTAGE;
                if (quorumPct > 0 && totalResidents > 0) {
                        double turnout = (totalVotes * 100.0) / totalResidents;
                        if (turnout < quorumPct) {
                                TownyElections.sendNationMessage(nation, TownyElections.getMessage("quorum-not-met"));
                                finalise(em, null, voteCountSimple, totalResidents, totalVotes);
                                return null;
                        }
                }

                if (votes.isEmpty()) {
                        TownyElections.sendNationMessage(nation, instance.getLanguageData().getString("no-winner"));
                        finalise(em, null, voteCountSimple, totalResidents, 0);
                        return null;
                }

                // 2-pass winner detection
                int maxVotes = voteCount.values().stream().mapToInt(AtomicInteger::get).max().orElse(0);
                long topCount = voteCount.values().stream().mapToInt(AtomicInteger::get).filter(v -> v == maxVotes).count();
                if (topCount > 1) {
                        TownyElections.sendNationMessage(nation, instance.getLanguageData().getString("no-winner"));
                        finalise(em, null, voteCountSimple, totalResidents, totalVotes);
                        return null;
                }

                winner = voteCount.entrySet().stream()
                                .filter(e -> e.getValue().get() == maxVotes)
                                .findFirst().get().getKey();

                List<NationParty> allParties = instance.getPartyManager().getPartiesForNation(nation.getName());
                for (NationParty p : allParties) {
                        if (!p.getName().equalsIgnoreCase(winner)) {
                                em.resetNationTermCount(nation.getUUID(), p.getName());
                        }
                }
                em.incrementNationTermCount(nation.getUUID(), winner);

                finalise(em, winner, voteCountSimple, totalResidents, totalVotes);

                try {
                        NationParty party = allParties.stream()
                                        .filter(t -> t.getName().equalsIgnoreCase(winner))
                                        .collect(Collectors.toList()).get(0);
                        Resident leaderResident = TownyUniverse.getInstance().getResident(party.getLeader());
                        if (leaderResident == null) throw new Exception("Leader resident not found");
                        nation.setCapital(leaderResident.getTown());
                        String msg = TownyElections.getMessage("election-won").replace("%party%", party.getName());
                        TownyElections.sendNationSubtitle(nation, msg);
                } catch (Exception e) {
                        e.printStackTrace();
                }
                return winner;
        }

        private void finalise(ElectionManager em, String winnerName,
                        Map<String, Integer> counts, int residents, int totalVotes) {
                em.storeNationResult(nation.getUUID(),
                                new ElectionManager.ElectionResult(nation.getName(), winnerName, counts, residents,
                                                System.currentTimeMillis()));
                HistoryHandler.logResult("NATION", nation.getName(), winnerName, counts, residents);
                DiscordWebhook.sendElectionResult("nation", nation.getName(), winnerName,
                                winnerName != null ? counts.getOrDefault(winnerName, 0) : totalVotes);
        }

        public Nation getNation() {
                return nation;
        }

}
