package me.skyttffxx.townyelectionsreloaded.gui;

import me.skyttffxx.townyelectionsreloaded.TownyElections;
import me.skyttffxx.townyelectionsreloaded.elections.ElectionManager;
import me.skyttffxx.townyelectionsreloaded.elections.NationElection;
import me.skyttffxx.townyelectionsreloaded.parties.NationParty;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NationVoteGui extends VoteGui {

    public static SmartInventory INVENTORY = SmartInventory.builder()
            .manager(TownyElections.getInstance().getInventoryManager())
            .id("nationvotegui")
            .provider(new NationVoteGui())
            .size(4, 9)
            .title(ChatColor.BLUE + "Vote in your nation election!")
            .build();

    @Override
    public void setItems(Player player, InventoryContents contents) {
        ElectionManager em = TownyElections.getInstance().getElectionManager();

        Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
        if (resident == null) {
            showError(contents, "Could not find your Towny profile.");
            return;
        }
        Nation n;
        try {
            n = resident.getTown().getNation();
        } catch (NotRegisteredException e1) {
            showError(contents, "Your town is not part of a nation.");
            return;
        }

        NationElection election = em.getNationElection(player);
        if (election == null) {
            showError(contents, "No active election in your nation.");
            return;
        }

        // Campaign period — voting not open yet
        if (!election.isVotingOpen()) {
            ItemStack clockItem = new ItemStack(Material.CLOCK);
            ItemMeta clockMeta = clockItem.getItemMeta();
            clockMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Campaign Period Active");
            List<String> clockLore = new ArrayList<>();
            clockLore.add(ChatColor.GRAY + "Voting opens in " + ChatColor.WHITE
                    + election.getCampaignMinutesRemaining() + " minutes.");
            clockMeta.setLore(clockLore);
            clockItem.setItemMeta(clockMeta);
            contents.set(1, 4, ClickableItem.empty(clockItem));
            return;
        }

        // Filter out term-limited parties
        List<NationParty> partyList = TownyElections.getInstance().getPartyManager()
                .getPartiesForNation(n.getName()).stream()
                .filter(p -> !em.isNationPartyAtTermLimit(n.getUUID(), p.getName()))
                .collect(Collectors.toList());

        if (partyList.isEmpty()) {
            showError(contents, "No eligible candidates.");
            return;
        }

        Pagination pagination = contents.pagination();
        ClickableItem[] items = new ClickableItem[partyList.size()];

        for (int i = 0; i < items.length; i++) {
            NationParty party = partyList.get(i);
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + party.getName());
            List<String> lore = new ArrayList<>();
            if (!party.getMotto().isEmpty()) {
                lore.add(ChatColor.ITALIC + "" + ChatColor.GRAY + "\"" + party.getMotto() + "\"");
            }
            lore.add(ChatColor.WHITE + "Members: " + ChatColor.AQUA + party.getMembers().size());
            lore.add("");
            lore.add(ChatColor.GREEN + "Click to vote for this party");
            meta.setLore(lore);
            item.setItemMeta(meta);
            final int it = i;
            items[i] = ClickableItem.of(item, e -> {
                if (election.hasVoted(player.getUniqueId())) {
                    player.sendMessage(TownyElections.getMessage("already-voted"));
                    return;
                }
                em.addPendingVote(player.getUniqueId(), partyList.get(it).getName(), false);
                player.closeInventory();
                NationConfirmVoteGui.open(player);
            });
        }

        pagination.setItems(items);
        pagination.setItemsPerPage(18);
        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0));
    }

    private void showError(InventoryContents contents, String message) {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + message);
        item.setItemMeta(meta);
        contents.set(1, 4, ClickableItem.empty(item));
    }

    @Override
    public SmartInventory getInventory() {
        return INVENTORY;
    }
}
