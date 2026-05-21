package me.skyttffxx.townyelectionsreloaded.elections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.skyttffxx.townyelectionsreloaded.TownyElections;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import me.skyttffxx.townyelectionsreloaded.data.DataHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

public class ElectionManager {

        private final List<TownElection> townElections;
        private final List<NationElection> nationElections;
        private final DataHandler dataHandler;
        private final Gson gson;

        // ── Cooldowns ─────────────────────────────────────────────────────────────
        private final Map<UUID, Long> townCooldowns = new HashMap<>();
        private final Map<UUID, Long> nationCooldowns = new HashMap<>();

        // ── Term counts: territory UUID string → party name → consecutive wins ───
        private final Map<String, Map<String, Integer>> townTermCounts = new HashMap<>();
        private final Map<String, Map<String, Integer>> nationTermCounts = new HashMap<>();

        // ── Pending vote confirmations (runtime only) ─────────────────────────────
        private final Map<UUID, PendingVote> pendingVotes = new HashMap<>();

        // ── Last results (ephemeral — cleared on restart) ─────────────────────────
        private final Map<UUID, ElectionResult> lastTownResults = new HashMap<>();
        private final Map<UUID, ElectionResult> lastNationResults = new HashMap<>();

        // ─────────────────────────────────────────────────────────────────────────
        // Inner classes
        // ─────────────────────────────────────────────────────────────────────────

        public static class PendingVote {
                public final String partyName;
                public final boolean isTown;
                public PendingVote(String partyName, boolean isTown) {
                        this.partyName = partyName;
                        this.isTown = isTown;
                }
        }

        public static class ElectionResult {
                public final String territoryName;
                public final String winner;
                public final Map<String, Integer> partyCounts;
                public final int totalResidents;
                public final long timestamp;

                public ElectionResult(String territoryName, String winner,
                                Map<String, Integer> partyCounts, int totalResidents, long timestamp) {
                        this.territoryName = territoryName;
                        this.winner = winner;
                        this.partyCounts = new HashMap<>(partyCounts);
                        this.totalResidents = totalResidents;
                        this.timestamp = timestamp;
                }

                public int getTotalVotes() {
                        return partyCounts.values().stream().mapToInt(Integer::intValue).sum();
                }
        }

        // ─────────────────────────────────────────────────────────────────────────
        // Constructor
        // ─────────────────────────────────────────────────────────────────────────

        public ElectionManager() {
                townElections = new ArrayList<>();
                nationElections = new ArrayList<>();
                dataHandler = new DataHandler(TownyElections.getInstance().getDataFolder(), "elections.json");
                gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

                new BukkitRunnable() {
                        @Override
                        public void run() {
                                List<Integer> reminders = TownyElections.Configuration.REMINDERS;

                                Iterator<TownElection> it1 = townElections.iterator();
                                while (it1.hasNext()) {
                                        TownElection e = it1.next();
                                        processReminders(e, e.getTown() != null ? e.getTown().getName() : "?", "town", reminders);
                                        if (System.currentTimeMillis() >= e.getEndTime()) {
                                                e.finishElection();
                                                setTownCooldown(e.getTown() != null ? e.getTown().getUUID() : null);
                                                it1.remove();
                                        }
                                }

                                Iterator<NationElection> it2 = nationElections.iterator();
                                while (it2.hasNext()) {
                                        NationElection e = it2.next();
                                        processReminders(e, e.getNation() != null ? e.getNation().getName() : "?", "nation", reminders);
                                        if (System.currentTimeMillis() >= e.getEndTime()) {
                                                e.finishElection();
                                                setNationCooldown(e.getNation() != null ? e.getNation().getUUID() : null);
                                                it2.remove();
                                        }
                                }
                        }
                }.runTaskTimer(TownyElections.getInstance(), 0, 100);
        }

        private void processReminders(Election e, String name, String type, List<Integer> thresholds) {
                if (thresholds.isEmpty()) return;
                long minsLeft = e.getMinutesRemaining();
                for (int threshold : thresholds) {
                        if (minsLeft <= threshold && !e.getRemindersSent().contains(threshold)) {
                                e.getRemindersSent().add(threshold);
                                String msg = TownyElections.getMessage("election-reminder")
                                                .replace("%type%", type)
                                                .replace("%territory%", name)
                                                .replace("%minutes%", String.valueOf(minsLeft));
                                if (e instanceof TownElection && ((TownElection) e).getTown() != null) {
                                        TownyElections.sendTownMessage(((TownElection) e).getTown(), msg);
                                } else if (e instanceof NationElection && ((NationElection) e).getNation() != null) {
                                        TownyElections.sendNationMessage(((NationElection) e).getNation(), msg);
                                }
                        }
                }
        }

        // ─────────────────────────────────────────────────────────────────────────
        // Cooldowns
        // ─────────────────────────────────────────────────────────────────────────

        public boolean hasTownCooldown(UUID townUuid) {
                Long exp = townCooldowns.get(townUuid);
                return exp != null && System.currentTimeMillis() < exp;
        }

        public long getTownCooldownMinutes(UUID townUuid) {
                Long exp = townCooldowns.get(townUuid);
                if (exp == null) return 0;
                long ms = exp - System.currentTimeMillis();
                return ms > 0 ? ms / 60_000L : 0;
        }

        public void setTownCooldown(UUID townUuid) {
                if (townUuid == null) return;
                int mins = TownyElections.Configuration.ELECTION_COOLDOWN;
                if (mins > 0) townCooldowns.put(townUuid, System.currentTimeMillis() + (long) mins * 60_000L);
        }

        public boolean hasNationCooldown(UUID nationUuid) {
                Long exp = nationCooldowns.get(nationUuid);
                return exp != null && System.currentTimeMillis() < exp;
        }

        public long getNationCooldownMinutes(UUID nationUuid) {
                Long exp = nationCooldowns.get(nationUuid);
                if (exp == null) return 0;
                long ms = exp - System.currentTimeMillis();
                return ms > 0 ? ms / 60_000L : 0;
        }

        public void setNationCooldown(UUID nationUuid) {
                if (nationUuid == null) return;
                int mins = TownyElections.Configuration.ELECTION_COOLDOWN;
                if (mins > 0) nationCooldowns.put(nationUuid, System.currentTimeMillis() + (long) mins * 60_000L);
        }

        // ─────────────────────────────────────────────────────────────────────────
        // Term counts
        // ─────────────────────────────────────────────────────────────────────────

        public int getTownTermCount(UUID townUuid, String partyName) {
                Map<String, Integer> c = townTermCounts.get(townUuid.toString());
                return c == null ? 0 : c.getOrDefault(partyName, 0);
        }

        public void incrementTownTermCount(UUID townUuid, String partyName) {
                townTermCounts.computeIfAbsent(townUuid.toString(), k -> new HashMap<>())
                                .merge(partyName, 1, Integer::sum);
        }

        public void resetTownTermCount(UUID townUuid, String partyName) {
                Map<String, Integer> c = townTermCounts.get(townUuid.toString());
                if (c != null) c.put(partyName, 0);
        }

        public boolean isTownPartyAtTermLimit(UUID townUuid, String partyName) {
                int limit = TownyElections.Configuration.TERM_LIMITS;
                if (limit <= 0) return false;
                return getTownTermCount(townUuid, partyName) >= limit;
        }

        public int getNationTermCount(UUID nationUuid, String partyName) {
                Map<String, Integer> c = nationTermCounts.get(nationUuid.toString());
                return c == null ? 0 : c.getOrDefault(partyName, 0);
        }

        public void incrementNationTermCount(UUID nationUuid, String partyName) {
                nationTermCounts.computeIfAbsent(nationUuid.toString(), k -> new HashMap<>())
                                .merge(partyName, 1, Integer::sum);
        }

        public void resetNationTermCount(UUID nationUuid, String partyName) {
                Map<String, Integer> c = nationTermCounts.get(nationUuid.toString());
                if (c != null) c.put(partyName, 0);
        }

        public boolean isNationPartyAtTermLimit(UUID nationUuid, String partyName) {
                int limit = TownyElections.Configuration.TERM_LIMITS;
                if (limit <= 0) return false;
                return getNationTermCount(nationUuid, partyName) >= limit;
        }

        // ─────────────────────────────────────────────────────────────────────────
        // Pending votes (confirm GUI)
        // ─────────────────────────────────────────────────────────────────────────

        public void addPendingVote(UUID playerUuid, String partyName, boolean isTown) {
                pendingVotes.put(playerUuid, new PendingVote(partyName, isTown));
        }

        public PendingVote getPendingVote(UUID playerUuid) {
                return pendingVotes.get(playerUuid);
        }

        public void clearPendingVote(UUID playerUuid) {
                pendingVotes.remove(playerUuid);
        }

        // ─────────────────────────────────────────────────────────────────────────
        // Results storage
        // ─────────────────────────────────────────────────────────────────────────

        public void storeTownResult(UUID townUuid, ElectionResult result) {
                lastTownResults.put(townUuid, result);
        }

        public void storeNationResult(UUID nationUuid, ElectionResult result) {
                lastNationResults.put(nationUuid, result);
        }

        public ElectionResult getLastTownResult(UUID townUuid) {
                return lastTownResults.get(townUuid);
        }

        public ElectionResult getLastNationResult(UUID nationUuid) {
                return lastNationResults.get(nationUuid);
        }

        // ─────────────────────────────────────────────────────────────────────────
        // Election lookups
        // ─────────────────────────────────────────────────────────────────────────

        public TownElection getTownElection(Player p) {
                Resident resident = TownyUniverse.getInstance().getResident(p.getUniqueId());
                if (resident == null) return null;
                Town t;
                try {
                        t = resident.getTown();
                } catch (NotRegisteredException e) {
                        return null;
                }
                for (TownElection e : townElections) {
                        if (e.getTown() != null && e.getTown().getName().equals(t.getName())) return e;
                }
                return null;
        }

        public NationElection getNationElection(Player p) {
                Resident resident = TownyUniverse.getInstance().getResident(p.getUniqueId());
                if (resident == null) return null;
                Nation n;
                try {
                        n = resident.getTown().getNation();
                } catch (NotRegisteredException e) {
                        return null;
                }
                for (NationElection e : nationElections) {
                        if (e.getNation() != null && e.getNation().getName().equals(n.getName())) return e;
                }
                return null;
        }

        public TownElection getTownElectionByName(String townName) {
                return townElections.stream()
                                .filter(e -> e.getTown() != null && e.getTown().getName().equalsIgnoreCase(townName))
                                .findFirst().orElse(null);
        }

        public NationElection getNationElectionByName(String nationName) {
                return nationElections.stream()
                                .filter(e -> e.getNation() != null && e.getNation().getName().equalsIgnoreCase(nationName))
                                .findFirst().orElse(null);
        }

        // ─────────────────────────────────────────────────────────────────────────
        // Add / Remove / Force-end
        // ─────────────────────────────────────────────────────────────────────────

        public void addTownElection(TownElection e) { townElections.add(e); }
        public void addNationElection(NationElection e) { nationElections.add(e); }

        public void removeTownElection(TownElection e) {
                e.finishElection();
                townElections.remove(e);
        }

        public void removeNationElection(NationElection e) {
                e.finishElection();
                nationElections.remove(e);
        }

        public void forceEndTownElection(TownElection e) {
                e.finishElection();
                townElections.remove(e);
        }

        public void forceEndNationElection(NationElection e) {
                e.finishElection();
                nationElections.remove(e);
        }

        // ─────────────────────────────────────────────────────────────────────────
        // Persistence
        // ─────────────────────────────────────────────────────────────────────────

        @SuppressWarnings("unchecked")
        public void loadData() {
                List<JSONObject> jsonArray;
                jsonArray = dataHandler.getDataList("townElections");
                if (jsonArray != null) {
                        for (JSONObject o : jsonArray) {
                                TownElection election = gson.fromJson(o.toJSONString(), TownElection.class);
                                election.setup();
                                townElections.add(election);
                        }
                }

                jsonArray = dataHandler.getDataList("nationElections");
                if (jsonArray != null) {
                        for (JSONObject o : jsonArray) {
                                NationElection election = gson.fromJson(o.toJSONString(), NationElection.class);
                                election.setup();
                                nationElections.add(election);
                        }
                }

                // Load town cooldowns
                JSONObject tc = dataHandler.getDataObject("townCooldowns");
                if (tc != null) {
                        for (Object key : tc.keySet()) {
                                long expires = ((Number) tc.get(key)).longValue();
                                if (expires > System.currentTimeMillis()) {
                                        townCooldowns.put(UUID.fromString((String) key), expires);
                                }
                        }
                }

                // Load nation cooldowns
                JSONObject nc = dataHandler.getDataObject("nationCooldowns");
                if (nc != null) {
                        for (Object key : nc.keySet()) {
                                long expires = ((Number) nc.get(key)).longValue();
                                if (expires > System.currentTimeMillis()) {
                                        nationCooldowns.put(UUID.fromString((String) key), expires);
                                }
                        }
                }

                // Load town term counts
                JSONObject ttc = dataHandler.getDataObject("townTermCounts");
                if (ttc != null) {
                        for (Object tKey : ttc.keySet()) {
                                JSONObject partyMap = (JSONObject) ttc.get(tKey);
                                Map<String, Integer> counts = new HashMap<>();
                                for (Object pKey : partyMap.keySet()) {
                                        counts.put((String) pKey, ((Number) partyMap.get(pKey)).intValue());
                                }
                                townTermCounts.put((String) tKey, counts);
                        }
                }

                // Load nation term counts
                JSONObject ntc = dataHandler.getDataObject("nationTermCounts");
                if (ntc != null) {
                        for (Object nKey : ntc.keySet()) {
                                JSONObject partyMap = (JSONObject) ntc.get(nKey);
                                Map<String, Integer> counts = new HashMap<>();
                                for (Object pKey : partyMap.keySet()) {
                                        counts.put((String) pKey, ((Number) partyMap.get(pKey)).intValue());
                                }
                                nationTermCounts.put((String) nKey, counts);
                        }
                }
        }

        @SuppressWarnings("unchecked")
        public void saveData() {
                // Save elections
                List<JSONObject> jsonArray = new ArrayList<>();
                for (TownElection w : townElections) {
                        try {
                                jsonArray.add((JSONObject) new JSONParser().parse(gson.toJson(w)));
                        } catch (ParseException e) { e.printStackTrace(); }
                }
                dataHandler.addDataList("townElections", jsonArray);

                jsonArray = new ArrayList<>();
                for (NationElection w : nationElections) {
                        try {
                                jsonArray.add((JSONObject) new JSONParser().parse(gson.toJson(w)));
                        } catch (ParseException e) { e.printStackTrace(); }
                }
                dataHandler.addDataList("nationElections", jsonArray);

                // Save town cooldowns
                JSONObject townCooldownsJson = new JSONObject();
                for (Map.Entry<UUID, Long> entry : townCooldowns.entrySet()) {
                        if (System.currentTimeMillis() < entry.getValue()) {
                                townCooldownsJson.put(entry.getKey().toString(), entry.getValue());
                        }
                }
                dataHandler.addDataObject("townCooldowns", townCooldownsJson);

                // Save nation cooldowns
                JSONObject nationCooldownsJson = new JSONObject();
                for (Map.Entry<UUID, Long> entry : nationCooldowns.entrySet()) {
                        if (System.currentTimeMillis() < entry.getValue()) {
                                nationCooldownsJson.put(entry.getKey().toString(), entry.getValue());
                        }
                }
                dataHandler.addDataObject("nationCooldowns", nationCooldownsJson);

                // Save town term counts
                JSONObject townTermCountsJson = new JSONObject();
                for (Map.Entry<String, Map<String, Integer>> entry : townTermCounts.entrySet()) {
                        JSONObject partyMap = new JSONObject();
                        entry.getValue().forEach(partyMap::put);
                        townTermCountsJson.put(entry.getKey(), partyMap);
                }
                dataHandler.addDataObject("townTermCounts", townTermCountsJson);

                // Save nation term counts
                JSONObject nationTermCountsJson = new JSONObject();
                for (Map.Entry<String, Map<String, Integer>> entry : nationTermCounts.entrySet()) {
                        JSONObject partyMap = new JSONObject();
                        entry.getValue().forEach(partyMap::put);
                        nationTermCountsJson.put(entry.getKey(), partyMap);
                }
                dataHandler.addDataObject("nationTermCounts", nationTermCountsJson);

                dataHandler.saveData();
        }

}
