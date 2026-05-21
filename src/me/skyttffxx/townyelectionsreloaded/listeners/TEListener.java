package me.skyttffxx.townyelectionsreloaded.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.skyttffxx.townyelectionsreloaded.TownyElections;
import me.skyttffxx.townyelectionsreloaded.elections.NationElection;
import me.skyttffxx.townyelectionsreloaded.elections.TownElection;
import me.skyttffxx.townyelectionsreloaded.parties.NationParty;
import me.skyttffxx.townyelectionsreloaded.parties.TownParty;
import com.palmergames.bukkit.towny.event.TownRemoveResidentEvent;

import java.util.UUID;

public class TEListener implements Listener {
        
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent e) {
                if (e.getPlayer().hasPermission("townyelections.vote.town")) {
                        TownElection election;
                        election = TownyElections.getInstance().getElectionManager().getTownElection(e.getPlayer());
                        if (election == null) return;
                        e.getPlayer().sendTitle(" ", TownyElections.getMessage("active-election"), 20, 60, 20);
                }
        }
        
        @SuppressWarnings("deprecation")
        @EventHandler
        public void onPlayerTownLeave(TownRemoveResidentEvent e) {
                UUID residentUuid = e.getResident().getUUID();

                TownParty townParty = TownyElections.getInstance().getPartyManager().getPlayerTownParty(residentUuid);
                if (townParty != null) {
                        townParty.removeMember(residentUuid);
                }
                
                NationParty nationParty = TownyElections.getInstance().getPartyManager().getPlayerNationParty(residentUuid);
                if (nationParty != null) {
                        nationParty.removeMember(residentUuid);
                }

                Player player = Bukkit.getPlayer(residentUuid);
                if (player == null) return;

                TownElection townElection = TownyElections.getInstance().getElectionManager().getTownElection(player);
                if (townElection != null) {
                        townElection.removeVote(residentUuid);
                }
                
                NationElection nationElection = TownyElections.getInstance().getElectionManager().getNationElection(player);
                if (nationElection != null) {
                        nationElection.removeVote(residentUuid);
                }
        }

}
