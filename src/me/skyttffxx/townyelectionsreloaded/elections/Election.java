package me.skyttffxx.townyelectionsreloaded.elections;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import me.skyttffxx.townyelectionsreloaded.TownyElections;
import com.google.gson.annotations.Expose;

public abstract class Election {

        @Expose
        protected final Map<UUID, String> votes;

        @Expose
        private final long endTime;

        @Expose
        private long startTime;

        @Expose
        private long campaignEndTime;

        @Expose
        protected UUID territoryUuid;

        protected String winner;

        protected TownyElections instance;

        private transient Set<Integer> remindersSent;

        public Election(long endTime) {
                this.instance = TownyElections.getInstance();
                this.endTime = endTime;
                this.startTime = System.currentTimeMillis();
                this.campaignEndTime = computeCampaignEnd();
                votes = new HashMap<>();
                remindersSent = new HashSet<>();
        }

        private long computeCampaignEnd() {
                int campaignMins = TownyElections.Configuration.CAMPAIGN_DURATION;
                if (campaignMins <= 0) return 0L;
                return System.currentTimeMillis() + (long) campaignMins * 60_000L;
        }

        public abstract void setup();

        public abstract void addVote(UUID player, String candidate);

        public abstract void removeVote(UUID player);

        public abstract String finishElection();

        public boolean hasVoted(UUID player) {
                return votes.containsKey(player);
        }

        public boolean isVotingOpen() {
                if (campaignEndTime <= 0) return true;
                return System.currentTimeMillis() >= campaignEndTime;
        }

        public long getCampaignMinutesRemaining() {
                if (campaignEndTime <= 0) return 0;
                long ms = campaignEndTime - System.currentTimeMillis();
                return ms > 0 ? ms / 60_000L : 0;
        }

        public long getMinutesRemaining() {
                long ms = endTime - System.currentTimeMillis();
                return ms > 0 ? ms / 60_000L : 0;
        }

        public int getVotesCount() {
                return votes.size();
        }

        public Map<UUID, String> getVotes() {
                return votes;
        }

        public long getEndTime() {
                return endTime;
        }

        public long getStartTime() {
                return startTime;
        }

        public long getCampaignEndTime() {
                return campaignEndTime;
        }

        public String getWinner() {
                return winner;
        }

        public Set<Integer> getRemindersSent() {
                if (remindersSent == null) remindersSent = new HashSet<>();
                return remindersSent;
        }

}
