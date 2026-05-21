package me.skyttffxx.townyelectionsreloaded;

import me.skyttffxx.townyelectionsreloaded.commands.GovernmentCommandHandler;
import me.skyttffxx.townyelectionsreloaded.government.GovernmentManager;
import me.skyttffxx.townyelectionsreloaded.integrations.PlaceholderHook;
import me.skyttffxx.townyelectionsreloaded.integrations.VaultHook;
import me.skyttffxx.townyelectionsreloaded.listeners.TEListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.skyttffxx.townyelectionsreloaded.commands.ElectionsCommandHandler;
import me.skyttffxx.townyelectionsreloaded.commands.PartyCommandHandler;
import me.skyttffxx.townyelectionsreloaded.commands.TElectCommandHandler;
import me.skyttffxx.townyelectionsreloaded.elections.ElectionManager;
import me.skyttffxx.townyelectionsreloaded.listeners.TElectTabCompleter;
import me.skyttffxx.townyelectionsreloaded.metrics.TEMetrics;
import me.skyttffxx.townyelectionsreloaded.parties.PartyManager;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

import fr.minuskube.inv.InventoryManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TownyElections extends JavaPlugin {

        private static TownyElections instance;
        private ElectionManager electionManager;
        private PartyManager partyManager;
        private InventoryManager inventoryManager;
        private LanguageData languageData;
        private GovernmentManager governmentManager;

        @Override
        public void onEnable() {
                instance = this;

                new TEMetrics(instance);

                setupConfig();
                languageData = new LanguageData();
                languageData.load();

                inventoryManager = new InventoryManager(this);
                inventoryManager.init();

                electionManager = new ElectionManager();
                partyManager = new PartyManager();
                governmentManager = new GovernmentManager();

                electionManager.loadData();
                partyManager.loadData();
                governmentManager.loadData();

                Bukkit.getPluginManager().registerEvents(new TEListener(), this);

                getCommand("townyelections").setExecutor(new TElectCommandHandler(instance));
                getCommand("townyelections").setTabCompleter(new TElectTabCompleter());
                getCommand("elections").setExecutor(new ElectionsCommandHandler());
                getCommand("party").setExecutor(new PartyCommandHandler());
                getCommand("government").setExecutor(new GovernmentCommandHandler());

                // Optional integrations
                if (VaultHook.setup()) {
                        getLogger().info("[TownyElectionsReloaded] Vault economy hooked successfully.");
                }

                if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                        new PlaceholderHook(this).register();
                        getLogger().info("[TownyElectionsReloaded] PlaceholderAPI expansion registered.");
                }
        }

        @Override
        public void onDisable() {
                electionManager.saveData();
                partyManager.saveData();
                governmentManager.saveData();
                languageData.save();
                saveConfig();
        }

        private void setupConfig() {
                // Core
                getConfig().addDefault("language", "en-US");
                getConfig().addDefault("max-duration", 10080);
                getConfig().addDefault("min-duration", 60);
                getConfig().addDefault("debug-mode", false);
                // New settings
                getConfig().addDefault("election-cooldown", 0);
                getConfig().addDefault("quorum-percentage", 0);
                getConfig().addDefault("term-limits", 0);
                getConfig().addDefault("campaign-duration", 0);
                getConfig().addDefault("reminders", Arrays.asList(60, 10));
                getConfig().addDefault("anonymous-voting", false);
                getConfig().addDefault("election-fee", 0.0);
                getConfig().addDefault("discord-webhook-url", "");

                getConfig().options().copyDefaults(true);
                saveConfig();

                // Load into static Configuration
                Configuration.DEBUG_ENABLED      = getConfig().getBoolean("debug-mode");
                Configuration.MAX_DURATION        = getConfig().getInt("max-duration");
                Configuration.MIN_DURATION        = getConfig().getInt("min-duration");
                Configuration.ELECTION_COOLDOWN   = getConfig().getInt("election-cooldown");
                Configuration.QUORUM_PERCENTAGE   = getConfig().getInt("quorum-percentage");
                Configuration.TERM_LIMITS         = getConfig().getInt("term-limits");
                Configuration.CAMPAIGN_DURATION   = getConfig().getInt("campaign-duration");
                Configuration.ANONYMOUS_VOTING    = getConfig().getBoolean("anonymous-voting");
                Configuration.ELECTION_FEE        = getConfig().getDouble("election-fee");
                Configuration.DISCORD_WEBHOOK_URL = getConfig().getString("discord-webhook-url", "");

                List<?> rawReminders = getConfig().getList("reminders", Collections.emptyList());
                Configuration.REMINDERS = rawReminders.stream()
                                .filter(o -> o instanceof Number)
                                .map(o -> ((Number) o).intValue())
                                .collect(Collectors.toList());

                validateConfig();
        }

        private void validateConfig() {
                if (Configuration.MIN_DURATION <= 0) {
                        getLogger().warning("[Config] min-duration must be > 0. Resetting to 60.");
                        Configuration.MIN_DURATION = 60;
                }
                if (Configuration.MAX_DURATION <= Configuration.MIN_DURATION) {
                        getLogger().warning("[Config] max-duration must be greater than min-duration. Resetting to 10080.");
                        Configuration.MAX_DURATION = 10080;
                }
                if (Configuration.QUORUM_PERCENTAGE < 0 || Configuration.QUORUM_PERCENTAGE > 100) {
                        getLogger().warning("[Config] quorum-percentage must be 0–100. Resetting to 0 (disabled).");
                        Configuration.QUORUM_PERCENTAGE = 0;
                }
                if (Configuration.ELECTION_FEE < 0) {
                        getLogger().warning("[Config] election-fee must be >= 0. Resetting to 0.");
                        Configuration.ELECTION_FEE = 0.0;
                }
                if (Configuration.CAMPAIGN_DURATION > 0 && Configuration.CAMPAIGN_DURATION >= Configuration.MIN_DURATION) {
                        getLogger().warning("[Config] campaign-duration should be shorter than min-duration. Adjusting to " + (Configuration.MIN_DURATION - 1) + " min.");
                        Configuration.CAMPAIGN_DURATION = Configuration.MIN_DURATION - 1;
                }
                if (Configuration.ELECTION_COOLDOWN < 0) {
                        getLogger().warning("[Config] election-cooldown must be >= 0. Resetting to 0.");
                        Configuration.ELECTION_COOLDOWN = 0;
                }
                if (Configuration.TERM_LIMITS < 0) {
                        getLogger().warning("[Config] term-limits must be >= 0. Resetting to 0.");
                        Configuration.TERM_LIMITS = 0;
                }
        }

        public static class Configuration {
                public static int     MAX_DURATION        = 10080;
                public static int     MIN_DURATION        = 60;
                public static boolean DEBUG_ENABLED       = false;
                public static int     ELECTION_COOLDOWN   = 0;
                public static int     QUORUM_PERCENTAGE   = 0;
                public static int     TERM_LIMITS         = 0;
                public static int     CAMPAIGN_DURATION   = 0;
                public static List<Integer> REMINDERS     = Arrays.asList(60, 10);
                public static boolean ANONYMOUS_VOTING    = false;
                public static double  ELECTION_FEE        = 0.0;
                public static String  DISCORD_WEBHOOK_URL = "";
        }

        public static String getMessage(String key) {
                return ChatColor.translateAlternateColorCodes('&',
                                instance.getLanguageData().getString("prefix") +
                                instance.getLanguageData().getString(key));
        }

        public static void sendTownMessage(Town n, String message) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                        Resident r = TownyUniverse.getInstance().getResident(p.getUniqueId());
                        if (r != null && n.hasResident(r)) p.sendMessage(message);
                }
        }

        @SuppressWarnings("deprecation")
        public static void sendTownSubtitle(Town n, String message) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                        Resident r = TownyUniverse.getInstance().getResident(p.getUniqueId());
                        if (r != null && n.hasResident(r)) p.sendTitle(" ", message, 20, 60, 20);
                }
        }

        public static void sendNationMessage(Nation n, String message) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                        Resident r = TownyUniverse.getInstance().getResident(p.getUniqueId());
                        if (r != null && n.hasResident(r)) p.sendMessage(message);
                }
        }

        @SuppressWarnings("deprecation")
        public static void sendNationSubtitle(Nation n, String message) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                        Resident r = TownyUniverse.getInstance().getResident(p.getUniqueId());
                        if (r != null && n.hasResident(r)) p.sendTitle(" ", message, 20, 60, 20);
                }
        }

        public static boolean hasPerms(Player p, String perm) {
                if (!p.hasPermission(perm)) {
                        p.sendMessage(TownyElections.getInstance().getLanguageData().getString("no-permission"));
                        return false;
                }
                return true;
        }

        public static class Permissions {
                // Elections
                public static final String TOWN_VOTE         = "townyelections.elections.vote.town";
                public static final String TOWN_CONVOKE      = "townyelections.elections.convoke.town";
                public static final String TOWN_LIST         = "townyelections.elections.list.town";
                public static final String TOWN_STOP         = "townyelections.elections.stop.town";
                public static final String TOWN_UNVOTE       = "townyelections.elections.unvote.town";
                public static final String NATION_VOTE       = "townyelections.elections.vote.nation";
                public static final String NATION_CONVOKE    = "townyelections.elections.convoke.nation";
                public static final String NATION_LIST       = "townyelections.elections.list.nation";
                public static final String NATION_STOP       = "townyelections.elections.stop.nation";
                public static final String NATION_UNVOTE     = "townyelections.elections.unvote.nation";
                // Town party
                public static final String TOWNPARTY_CREATE    = "townyelections.party.create.town";
                public static final String TOWNPARTY_LEAVE     = "townyelections.party.leave.town";
                public static final String TOWNPARTY_ADD       = "townyelections.party.add.town";
                public static final String TOWNPARTY_ACCEPT    = "townyelections.party.accept.town";
                public static final String TOWNPARTY_INVITES   = "townyelections.party.invites.town";
                public static final String TOWNPARTY_SETLEADER = "townyelections.party.setleader.town";
                public static final String TOWNPARTY_PROMOTE   = "townyelections.party.promote.town";
                public static final String TOWNPARTY_DEMOTE    = "townyelections.party.demote.town";
                public static final String TOWNPARTY_INFO      = "townyelections.party.info.town";
                public static final String TOWNPARTY_DISBAND   = "townyelections.party.disband.town";
                public static final String TOWNPARTY_SETMOTTO  = "townyelections.party.setmotto.town";
                // Nation party
                public static final String NATIONPARTY_CREATE    = "townyelections.party.create.nation";
                public static final String NATIONPARTY_LEAVE     = "townyelections.party.leave.nation";
                public static final String NATIONPARTY_ADD       = "townyelections.party.add.nation";
                public static final String NATIONPARTY_ACCEPT    = "townyelections.party.accept.nation";
                public static final String NATIONPARTY_INVITES   = "townyelections.party.invites.nation";
                public static final String NATIONPARTY_SETLEADER = "townyelections.party.setleader.nation";
                public static final String NATIONPARTY_PROMOTE   = "townyelections.party.promote.nation";
                public static final String NATIONPARTY_DEMOTE    = "townyelections.party.demote.nation";
                public static final String NATIONPARTY_INFO      = "townyelections.party.info.nation";
                public static final String NATIONPARTY_DISBAND   = "townyelections.party.disband.nation";
                public static final String NATIONPARTY_SETMOTTO  = "townyelections.party.setmotto.nation";
                // Admin
                public static final String ADMIN_FORCEEND = "townyelections.admin.forceend";
        }

        public static class Text {
                public static final String ELECTIONS_HELP_MESSAGE =
                        "&8&m----- &6&lTownyElections Reloaded&r &8&m-----\n\n" +
                        "  &7- &f/elections &evote [town/nation] &8- &eVote for a candidate\n" +
                        "  &7- &f/elections &econvoke [town/nation] [minutes] &8- &eStart an election\n" +
                        "  &7- &f/elections &elist [town/nation] &8- &eCandidates and vote counts\n" +
                        "  &7- &f/elections &estatus [town/nation] &8- &eElection time & votes\n" +
                        "  &7- &f/elections &eresults [town/nation] &8- &eLast election results\n" +
                        "  &7- &f/elections &estop [town/nation] &8- &eStop the current election\n" +
                        "  &7- &f/elections &eunvote [town/nation] &8- &eRemove your vote\n" +
                        "  &7- &f/elections &eforceend [town/nation] [name] &8- &e[Admin] Force-end\n" +
                        "\n&8---------------------------------";

                public static final String PARTY_HELP_MESSAGE =
                        "&8&m----- &6&lTownyElections Reloaded&r &8&m-----\n\n" +
                        "  &7- &f/party &ecreate [town/nation] [name] &8- &eCreate a party\n" +
                        "  &7- &f/party &eleave [town/nation] &8- &eLeave your party\n" +
                        "  &7- &f/party &edisband [town/nation] &8- &eDisband your party\n" +
                        "  &7- &f/party &esetmotto [town/nation] [text] &8- &eSet party motto\n" +
                        "  &7- &f/party &eadd [town/nation] [player] &8- &eInvite a player\n" +
                        "  &7- &f/party &eaccept [town/nation] &8- &eAccept an invite\n" +
                        "  &7- &f/party &einvites [town/nation] &8- &eSee your invites\n" +
                        "  &7- &f/party &esetleader [town/nation] [player] &8- &eTransfer leadership\n" +
                        "  &7- &f/party &epromote/demote [town/nation] [player] &8- &eManage ranks\n" +
                        "  &7- &f/party &einfo [town/nation] &8- &eParty info\n" +
                        "  &7- &f/party &emanage [town/nation] &8- &eParty management GUI\n" +
                        "\n&8---------------------------------";

                public static final String HELP_MESSAGE =
                        "&8&m----- &6&lTownyElections Reloaded&r &8&m-----\n\n" +
                        "  &7Use &f/elections&e or &f/party&e for full command lists.\n\n" +
                        "  &7- &f/elections &econvoke [town/nation] [minutes]\n" +
                        "  &7- &f/elections &evote / list / status / results / stop / unvote\n" +
                        "  &7- &f/party &ecreate / setmotto / disband / manage\n\n" +
                        "&8---------------------------------";

                public static final String INFO_MESSAGE =
                        "&8--- &6&lTownyElections Reloaded&r &8---\n\n" +
                        "&fVersion: &e%version%\n" +
                        "&fMaintained by: &e%author%\n" +
                        "&7Original plugin by: &7aurgiyalgo\n\n" +
                        "&8---------------------------------";
        }

        public ElectionManager getElectionManager()   { return electionManager; }
        public PartyManager    getPartyManager()      { return partyManager; }
        public InventoryManager getInventoryManager() { return inventoryManager; }
        public LanguageData    getLanguageData()      { return languageData; }
        public GovernmentManager getGovernmentManager() { return governmentManager; }
        public static TownyElections getInstance()    { return instance; }
}
