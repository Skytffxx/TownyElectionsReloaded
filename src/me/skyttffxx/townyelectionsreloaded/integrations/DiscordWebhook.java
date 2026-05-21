package me.skyttffxx.townyelectionsreloaded.integrations;

import me.skyttffxx.townyelectionsreloaded.TownyElections;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordWebhook {

        /** Called when a new election is convoked. */
        public static void sendElectionStarted(String type, String territoryName, long endTimeMs) {
                String webhookUrl = TownyElections.Configuration.DISCORD_WEBHOOK_URL;
                if (webhookUrl == null || webhookUrl.isEmpty()) return;

                long minutesLeft = (endTimeMs - System.currentTimeMillis()) / 60_000L;
                String content = "🗳️ **Election started!** A **" + type + "** election has begun in **"
                                + territoryName + "**. Voting closes in **" + minutesLeft + " minutes**.";
                postAsync(webhookUrl, content);
        }

        /** Called when an election finishes. */
        public static void sendElectionResult(String type, String territoryName, String winner, int votes) {
                String webhookUrl = TownyElections.Configuration.DISCORD_WEBHOOK_URL;
                if (webhookUrl == null || webhookUrl.isEmpty()) return;

                String content = winner != null
                                ? "🏆 **Election result!** The **" + winner + "** party won the **" + type
                                                + "** election in **" + territoryName + "** with **" + votes + " vote(s)**!"
                                : "❌ **Election ended** in **" + territoryName + "** (" + type + ") with no winner.";
                postAsync(webhookUrl, content);
        }

        private static void postAsync(String webhookUrl, String content) {
                TownyElections.getInstance().getServer().getScheduler().runTaskAsynchronously(
                                TownyElections.getInstance(), () -> post(webhookUrl, content));
        }

        private static void post(String webhookUrl, String content) {
                try {
                        URL url = new URL(webhookUrl);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json; utf-8");
                        conn.setDoOutput(true);
                        conn.setConnectTimeout(5000);
                        conn.setReadTimeout(5000);

                        String escaped = content
                                        .replace("\\", "\\\\")
                                        .replace("\"", "\\\"")
                                        .replace("\n", "\\n");
                        String json = "{\"content\": \"" + escaped + "\"}";

                        try (OutputStream os = conn.getOutputStream()) {
                                os.write(json.getBytes(StandardCharsets.UTF_8));
                        }
                        int code = conn.getResponseCode();
                        if (TownyElections.Configuration.DEBUG_ENABLED) {
                                TownyElections.getInstance().getLogger().info("[Discord] Webhook response: " + code);
                        }
                        conn.disconnect();
                } catch (Exception e) {
                        TownyElections.getInstance().getLogger()
                                        .warning("[DiscordWebhook] Failed to send message: " + e.getMessage());
                }
        }

}
