package me.skyttffxx.townyelectionsreloaded.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import me.skyttffxx.townyelectionsreloaded.TownyElections;
import me.skyttffxx.townyelectionsreloaded.elections.ElectionManager;
import me.skyttffxx.townyelectionsreloaded.elections.TownElection;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class TownConfirmVoteGui implements InventoryProvider {

        private static SmartInventory INVENTORY;

        public static SmartInventory getInventory() {
                if (INVENTORY == null) {
                        INVENTORY = SmartInventory.builder()
                                        .manager(TownyElections.getInstance().getInventoryManager())
                                        .id("townconfirmvotegui")
                                        .provider(new TownConfirmVoteGui())
                                        .size(3, 9)
                                        .title(ChatColor.GREEN + "" + ChatColor.BOLD + "Confirm your vote?")
                                        .build();
                }
                return INVENTORY;
        }

        public static void open(Player player) {
                getInventory().open(player);
        }

        @Override
        public void init(Player player, InventoryContents contents) {
                ElectionManager em = TownyElections.getInstance().getElectionManager();
                ElectionManager.PendingVote pending = em.getPendingVote(player.getUniqueId());

                if (pending == null || !pending.isTown) {
                        player.closeInventory();
                        return;
                }

                String partyName = pending.partyName;

                // Center info item
                ItemStack infoItem = new ItemStack(Material.PAPER);
                ItemMeta infoMeta = infoItem.getItemMeta();
                infoMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + partyName);
                infoMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to confirm or cancel your vote."));
                infoItem.setItemMeta(infoMeta);
                contents.set(0, 4, ClickableItem.empty(infoItem));

                // Confirm button
                ItemStack confirmItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                ItemMeta confirmMeta = confirmItem.getItemMeta();
                confirmMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "✔ Confirm Vote");
                confirmItem.setItemMeta(confirmMeta);
                contents.set(1, 2, ClickableItem.of(confirmItem, e -> {
                        TownElection election = em.getTownElection(player);
                        if (election == null) {
                                player.sendMessage(TownyElections.getMessage("not-active-election"));
                                em.clearPendingVote(player.getUniqueId());
                                player.closeInventory();
                                return;
                        }
                        election.addVote(player.getUniqueId(), partyName);
                        em.clearPendingVote(player.getUniqueId());
                        player.closeInventory();
                        player.sendMessage(TownyElections.getMessage("vote-confirmed").replace("%party%", partyName));
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 2);
                }));

                // Cancel button
                ItemStack cancelItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                ItemMeta cancelMeta = cancelItem.getItemMeta();
                cancelMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "✖ Cancel");
                cancelItem.setItemMeta(cancelMeta);
                contents.set(1, 6, ClickableItem.of(cancelItem, e -> {
                        em.clearPendingVote(player.getUniqueId());
                        player.closeInventory();
                        player.sendMessage(TownyElections.getMessage("vote-cancelled"));
                }));
        }

        @Override
        public void update(Player player, InventoryContents contents) {}

}
