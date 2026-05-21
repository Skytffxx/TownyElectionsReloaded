package me.skyttffxx.townyelectionsreloaded.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import me.skyttffxx.townyelectionsreloaded.TownyElections;
import me.skyttffxx.townyelectionsreloaded.elections.ElectionManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TownResultsGui implements InventoryProvider {

        private static SmartInventory INVENTORY;
        private static final Map<UUID, ElectionManager.ElectionResult> pendingResults = new HashMap<>();

        public static SmartInventory getInventory() {
                if (INVENTORY == null) {
                        INVENTORY = SmartInventory.builder()
                                        .manager(TownyElections.getInstance().getInventoryManager())
                                        .id("townresultsgui")
                                        .provider(new TownResultsGui())
                                        .size(4, 9)
                                        .title(ChatColor.GOLD + "" + ChatColor.BOLD + "Town Election Results")
                                        .build();
                }
                return INVENTORY;
        }

        public static void open(Player player, ElectionManager.ElectionResult result) {
                pendingResults.put(player.getUniqueId(), result);
                getInventory().open(player);
        }

        @Override
        public void init(Player player, InventoryContents contents) {
                ElectionManager.ElectionResult result = pendingResults.get(player.getUniqueId());
                if (result == null) {
                        player.closeInventory();
                        return;
                }

                // Header: territory name + winner
                ItemStack headerItem = new ItemStack(result.winner != null ? Material.GOLD_BLOCK : Material.BARRIER);
                ItemMeta headerMeta = headerItem.getItemMeta();
                headerMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + result.territoryName);
                List<String> headerLore = new ArrayList<>();
                headerLore.add(result.winner != null
                                ? ChatColor.GREEN + "Winner: " + ChatColor.BOLD + result.winner
                                : ChatColor.RED + "No winner");
                int totalVotes = result.getTotalVotes();
                double pct = result.totalResidents > 0 ? (totalVotes * 100.0 / result.totalResidents) : 0;
                headerLore.add(ChatColor.GRAY + "Turnout: " + ChatColor.WHITE + totalVotes + "/" + result.totalResidents
                                + " (" + String.format("%.1f", pct) + "%)");
                headerMeta.setLore(headerLore);
                headerItem.setItemMeta(headerMeta);
                contents.set(0, 4, ClickableItem.empty(headerItem));

                // Party results
                List<Map.Entry<String, Integer>> sorted = new ArrayList<>(result.partyCounts.entrySet());
                sorted.sort((a, b) -> b.getValue() - a.getValue());

                int col = 1;
                int row = 1;
                for (Map.Entry<String, Integer> entry : sorted) {
                        boolean isWinner = entry.getKey().equals(result.winner);
                        ItemStack item = new ItemStack(isWinner ? Material.LIME_CONCRETE : Material.PAPER);
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName((isWinner ? ChatColor.GREEN : ChatColor.YELLOW) + entry.getKey());
                        List<String> lore = new ArrayList<>();
                        lore.add(ChatColor.WHITE + "Votes: " + ChatColor.AQUA + entry.getValue());
                        if (result.totalResidents > 0) {
                                double share = (entry.getValue() * 100.0 / result.totalResidents);
                                lore.add(ChatColor.GRAY + "Share: " + String.format("%.1f", share) + "%");
                        }
                        if (isWinner) lore.add(ChatColor.GREEN + "" + ChatColor.BOLD + "★ WINNER");
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                        contents.set(row, col, ClickableItem.empty(item));
                        col++;
                        if (col > 7) { col = 1; row++; }
                        if (row > 2) break;
                }

                // Close button
                ItemStack closeItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                ItemMeta closeMeta = closeItem.getItemMeta();
                closeMeta.setDisplayName(ChatColor.RED + "Close");
                closeItem.setItemMeta(closeMeta);
                contents.set(3, 4, ClickableItem.of(closeItem, e -> player.closeInventory()));
        }

        @Override
        public void update(Player player, InventoryContents contents) {}

}
