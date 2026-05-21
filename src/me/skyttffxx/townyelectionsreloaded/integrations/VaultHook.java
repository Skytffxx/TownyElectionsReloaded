package me.skyttffxx.townyelectionsreloaded.integrations;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {

        private static Economy economy = null;

        /**
         * Called during onEnable(). Returns true if Vault + an economy plugin are present.
         */
        public static boolean setup() {
                if (Bukkit.getPluginManager().getPlugin("Vault") == null) return false;
                RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
                if (rsp == null) return false;
                economy = rsp.getProvider();
                return economy != null;
        }

        public static boolean isEnabled() {
                return economy != null;
        }

        /**
         * Returns true if the player has at least {@code amount} currency.
         * Always returns true when Vault is not present.
         */
        public static boolean has(Player player, double amount) {
                if (!isEnabled() || amount <= 0) return true;
                return economy.has(player, amount);
        }

        /**
         * Deducts {@code amount} from the player and returns true on success.
         * Always returns true when Vault is not present or amount <= 0.
         */
        public static boolean charge(Player player, double amount) {
                if (!isEnabled() || amount <= 0) return true;
                if (!economy.has(player, amount)) return false;
                economy.withdrawPlayer(player, amount);
                return true;
        }

        /** Returns a formatted currency string (e.g. "$5.00"). Falls back to plain number. */
        public static String format(double amount) {
                if (!isEnabled()) return String.valueOf(amount);
                return economy.format(amount);
        }

}
