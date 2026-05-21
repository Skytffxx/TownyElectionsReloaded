package me.skyttffxx.townyelectionsreloaded.history;

import me.skyttffxx.townyelectionsreloaded.TownyElections;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class HistoryHandler {

        private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        /**
         * Appends one line to election-history.log in the plugin data folder.
         *
         * @param type           "TOWN" or "NATION"
         * @param territoryName  Display name of the town/nation
         * @param winner         Winning party name, or null if no winner
         * @param partyCounts    Map of partyName → vote count
         * @param totalResidents Total eligible residents at time of finish
         */
        public static void logResult(String type, String territoryName, String winner,
                        Map<String, Integer> partyCounts, int totalResidents) {
                File logFile = new File(TownyElections.getInstance().getDataFolder(), "election-history.log");
                try {
                        if (!logFile.getParentFile().exists()) logFile.getParentFile().mkdirs();

                        int totalVotes = partyCounts.values().stream().mapToInt(Integer::intValue).sum();
                        double pct = totalResidents > 0 ? (totalVotes * 100.0 / totalResidents) : 0.0;

                        StringBuilder sb = new StringBuilder();
                        sb.append(LocalDateTime.now().format(FMT)).append(" | ");
                        sb.append(type).append(" | ");
                        sb.append(territoryName).append(" | ");
                        sb.append(winner != null ? "WINNER:" + winner : "NO_WINNER").append(" | ");
                        sb.append(totalVotes).append("/").append(totalResidents)
                                        .append(" votes (").append(String.format("%.1f", pct)).append("%) | ");
                        partyCounts.forEach((party, count) -> sb.append(party).append(":").append(count).append(" "));

                        try (PrintWriter pw = new PrintWriter(new FileWriter(logFile, true))) {
                                pw.println(sb.toString().trim());
                        }
                } catch (IOException e) {
                        TownyElections.getInstance().getLogger().warning("[HistoryHandler] Failed to write log: " + e.getMessage());
                }
        }

}
