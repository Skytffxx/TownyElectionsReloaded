package me.skyttffxx.townyelectionsreloaded.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import me.skyttffxx.townyelectionsreloaded.TownyElections;
import me.skyttffxx.townyelectionsreloaded.parties.Party;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PartyManageGui implements InventoryProvider {

        private static SmartInventory INVENTORY;
        private static final Map<UUID, Party> pendingParties = new HashMap<>();

        public static SmartInventory getInventory() {
                if (INVENTORY == null) {
                        INVENTORY = SmartInventory.builder()
                                        .manager(TownyElections.getInstance().getInventoryManager())
                                        .id("partymanagegui")
                                        .provider(new PartyManageGui())
                                        .size(5, 9)
                                        .title(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Party Management")
                                        .build();
                }
                return INVENTORY;
        }

        public static void open(Player player, Party party) {
                pendingParties.put(player.getUniqueId(), party);
                getInventory().open(player);
        }

        @Override
        public void init(Player player, InventoryContents contents) {
                Party party = pendingParties.get(player.getUniqueId());
                if (party == null) {
                        player.closeInventory();
                        return;
                }

                boolean isLeader = party.getLeader().equals(player.getUniqueId());

                // Header: party name + motto
                ItemStack headerItem = new ItemStack(Material.BOOK);
                ItemMeta headerMeta = headerItem.getItemMeta();
                headerMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + party.getName());
                List<String> headerLore = new ArrayList<>();
                if (!party.getMotto().isEmpty()) {
                        headerLore.add(ChatColor.ITALIC + "" + ChatColor.GRAY + "\"" + party.getMotto() + "\"");
                }
                headerLore.add(ChatColor.WHITE + "Members: " + ChatColor.AQUA + party.getMembers().size());
                if (isLeader) {
                        headerLore.add("");
                        headerLore.add(ChatColor.YELLOW + "LEFT-CLICK member: Promote/Demote");
                        headerLore.add(ChatColor.RED + "RIGHT-CLICK member: Kick");
                }
                headerMeta.setLore(headerLore);
                headerItem.setItemMeta(headerMeta);
                contents.set(0, 4, ClickableItem.empty(headerItem));

                // Member list (rows 1-3, cols 1-7 = up to 21 members)
                List<UUID> members = party.getMembers();
                int col = 1;
                int row = 1;
                for (UUID memberUuid : members) {
                        if (row > 3) break;
                        UUID leaderUuid = party.getLeader();
                        boolean isThisLeader = memberUuid.equals(leaderUuid);
                        boolean isAssistant = party.isAssistant(memberUuid);

                        Material mat = isThisLeader ? Material.GOLD_NUGGET
                                        : isAssistant ? Material.IRON_NUGGET : Material.PAPER;
                        String roleColor = isThisLeader ? ChatColor.GOLD.toString()
                                        : isAssistant ? ChatColor.GRAY.toString() : ChatColor.WHITE.toString();
                        String role = isThisLeader ? "[Leader]" : isAssistant ? "[Assistant]" : "[Member]";

                        String playerName = Bukkit.getOfflinePlayer(memberUuid).getName();
                        if (playerName == null) playerName = memberUuid.toString().substring(0, 8);

                        ItemStack item = new ItemStack(mat);
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName(roleColor + role + " " + ChatColor.WHITE + playerName);
                        List<String> lore = new ArrayList<>();
                        if (isLeader && !isThisLeader) {
                                lore.add(ChatColor.YELLOW + "Left-click: " + (isAssistant ? "Demote" : "Promote"));
                                lore.add(ChatColor.RED + "Right-click: Kick");
                        }
                        meta.setLore(lore);
                        item.setItemMeta(meta);

                        final UUID targetUuid = memberUuid;
                        contents.set(row, col, ClickableItem.of(item, e -> {
                                if (!isLeader || isThisLeader) return;
                                if (e.getClick() == ClickType.RIGHT) {
                                        party.removeMember(targetUuid);
                                        player.sendMessage(TownyElections.getMessage("not-a-member")
                                                .replace("That player is not a member of the party",
                                                         Bukkit.getOfflinePlayer(targetUuid).getName() + " was kicked."));
                                } else {
                                        if (party.isAssistant(targetUuid)) {
                                                party.removeAssistant(targetUuid);
                                                player.sendMessage(TownyElections.getMessage("player-was-demoted")
                                                        .replace("%player%", Bukkit.getOfflinePlayer(targetUuid).getName()));
                                        } else {
                                                party.addAssistant(targetUuid);
                                                player.sendMessage(TownyElections.getMessage("player-promoted")
                                                        .replace("%player%", Bukkit.getOfflinePlayer(targetUuid).getName()));
                                        }
                                }
                                // Refresh the GUI
                                open(player, party);
                        }));

                        col++;
                        if (col > 7) { col = 1; row++; }
                }

                // Close button
                ItemStack closeItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                ItemMeta closeMeta = closeItem.getItemMeta();
                closeMeta.setDisplayName(ChatColor.RED + "Close");
                closeItem.setItemMeta(closeMeta);
                contents.set(4, 4, ClickableItem.of(closeItem, e -> player.closeInventory()));
        }

        @Override
        public void update(Player player, InventoryContents contents) {}

}
