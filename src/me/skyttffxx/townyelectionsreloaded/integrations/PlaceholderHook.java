package me.skyttffxx.townyelectionsreloaded.integrations;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.skyttffxx.townyelectionsreloaded.TownyElections;
import me.skyttffxx.townyelectionsreloaded.elections.NationElection;
import me.skyttffxx.townyelectionsreloaded.elections.TownElection;
import me.skyttffxx.townyelectionsreloaded.parties.NationParty;
import me.skyttffxx.townyelectionsreloaded.parties.TownParty;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PlaceholderAPI expansion for TownyElectionsReloaded.
 *
 * Available placeholders:
 *   %townyelectionsreloaded_town_election_active%      — true/false
 *   %townyelectionsreloaded_town_election_time%        — minutes remaining (or 0)
 *   %townyelectionsreloaded_town_election_votes%       — total votes cast
 *   %townyelectionsreloaded_town_party%                — player's town party name (or None)
 *   %townyelectionsreloaded_town_party_motto%          — player's town party motto
 *   %townyelectionsreloaded_nation_election_active%    — true/false
 *   %townyelectionsreloaded_nation_election_time%      — minutes remaining (or 0)
 *   %townyelectionsreloaded_nation_election_votes%     — total votes cast
 *   %townyelectionsreloaded_nation_party%              — player's nation party name (or None)
 *   %townyelectionsreloaded_nation_party_motto%        — player's nation party motto
 *   %townyelectionsreloaded_has_voted_town%            — true/false
 *   %townyelectionsreloaded_has_voted_nation%          — true/false
 */
public class PlaceholderHook extends PlaceholderExpansion {

        private final TownyElections plugin;

        public PlaceholderHook(TownyElections plugin) {
                this.plugin = plugin;
        }

        @Override
        public @NotNull String getIdentifier() {
                return "townyelectionsreloaded";
        }

        @Override
        public @NotNull String getAuthor() {
                return "skyttffxx";
        }

        @Override
        public @NotNull String getVersion() {
                return plugin.getDescription().getVersion();
        }

        @Override
        public boolean persist() {
                return true;
        }

        @Override
        public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
                if (player == null) return "";

                switch (params.toLowerCase()) {

                        case "town_election_active": {
                                return plugin.getElectionManager().getTownElection(player) != null ? "true" : "false";
                        }
                        case "town_election_time": {
                                TownElection e = plugin.getElectionManager().getTownElection(player);
                                return e == null ? "0" : e.getMinutesRemaining() + "m";
                        }
                        case "town_election_votes": {
                                TownElection e = plugin.getElectionManager().getTownElection(player);
                                return e == null ? "0" : String.valueOf(e.getVotesCount());
                        }
                        case "town_party": {
                                TownParty p = plugin.getPartyManager().getPlayerTownParty(player.getUniqueId());
                                return p == null ? "None" : p.getName();
                        }
                        case "town_party_motto": {
                                TownParty p = plugin.getPartyManager().getPlayerTownParty(player.getUniqueId());
                                return p == null ? "" : p.getMotto();
                        }
                        case "nation_election_active": {
                                return plugin.getElectionManager().getNationElection(player) != null ? "true" : "false";
                        }
                        case "nation_election_time": {
                                NationElection e = plugin.getElectionManager().getNationElection(player);
                                return e == null ? "0" : e.getMinutesRemaining() + "m";
                        }
                        case "nation_election_votes": {
                                NationElection e = plugin.getElectionManager().getNationElection(player);
                                return e == null ? "0" : String.valueOf(e.getVotesCount());
                        }
                        case "nation_party": {
                                NationParty p = plugin.getPartyManager().getPlayerNationParty(player.getUniqueId());
                                return p == null ? "None" : p.getName();
                        }
                        case "nation_party_motto": {
                                NationParty p = plugin.getPartyManager().getPlayerNationParty(player.getUniqueId());
                                return p == null ? "" : p.getMotto();
                        }
                        case "has_voted_town": {
                                TownElection e = plugin.getElectionManager().getTownElection(player);
                                return e != null && e.hasVoted(player.getUniqueId()) ? "true" : "false";
                        }
                        case "has_voted_nation": {
                                NationElection e = plugin.getElectionManager().getNationElection(player);
                                return e != null && e.hasVoted(player.getUniqueId()) ? "true" : "false";
                        }
                        default:
                                return null;
                }
        }

}
