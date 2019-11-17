package com.pustinek.humblevote;

import com.pustinek.humblevote.commands.CommandManager;
import com.pustinek.humblevote.configs.ConfigManager;
import com.pustinek.humblevote.listeners.OnVoteListener;
import com.pustinek.humblevote.listeners.PlayerListener;
import com.pustinek.humblevote.placeholders.Placeholders;
import com.pustinek.humblevote.sql.Database;
import com.pustinek.humblevote.sql.MySQL;
import com.pustinek.humblevote.sql.SQLite;
import com.pustinek.humblevote.time.TimeManager;
import com.pustinek.humblevote.utils.Callback;
import com.pustinek.humblevote.utils.Manager;
import com.pustinek.humblevote.voteNotifications.VoteNotificationManager;
import com.pustinek.humblevote.voteReminder.VoteReminderManager;
import com.pustinek.humblevote.voteSites.VoteSitesManager;
import com.pustinek.humblevote.voteStatistics.PlayerVoteStatisticsManager;
import com.pustinek.humblevote.voting.VoteManager;
import com.pustinek.humblevote.votingRewards.RewardManager;
import fr.minuskube.inv.InventoryManager;
import me.wiefferink.interactivemessenger.processing.Message;
import me.wiefferink.interactivemessenger.source.LanguageManager;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public final class Main extends JavaPlugin {
    // Private static variables
    private static Logger logger;

    private static Main plugin;
    // Vault economy
    private static Economy econ = null;
    // Managers:
    private static Set<Manager> managers = new HashSet<>();
    private static CommandManager commandManager = null;
    private static ConfigManager configManager = null;
    private static VoteManager voteManager = null;
    private static RewardManager rewardManager = null;
    private static InventoryManager inventoryManager = null;
    private static VoteSitesManager voteSitesManager = null;
    private static PlayerVoteStatisticsManager voteStatisticsManager = null;
    private static VoteNotificationManager notificationManager = null;
    private static TimeManager timeManager = null;
    private static VoteReminderManager voteReminderManager = null;
    private static LanguageManager languageManager = null;

    private static Database databaseMySQL = null;
    private static Database databaseSQLite = null;

    public static CommandManager getCommandManager() {
        return commandManager;
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }

    public static Database getDatabaseMySQL() {
        return databaseMySQL;
    }

    private void initDatabases() {
        logger.info("Init database");

        if(configManager.getDatabaseConfig().getDriver().equalsIgnoreCase("MySQL")) {
            databaseMySQL = new MySQL(this);
            if (configManager.getDatabaseConfig().getPing_interval() > 0) {
                Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
                    @Override
                    public void run() {
                        if (databaseMySQL instanceof MySQL) {
                            ((MySQL) databaseMySQL).ping();
                        }
                    }
                }, configManager.getDatabaseConfig().getPing_interval() * 20L, configManager.getDatabaseConfig().getPing_interval() * 20L);
            }
        }
        logger.info("Using database type: SQLite");
        databaseSQLite = new SQLite(this);

        Main.getDatabaseSQLite().connect(new Callback<Integer>(this) {
            @Override
            public void onResult(Integer result) {
                super.onResult(result);
                Main.debug("Success connection and reloading votes");
                Main.getVoteStatisticsManager().loadAllPlayerVoteStatisticsFromDatabase();

                new BukkitRunnable(){
                    @Override
                    public void run() {
                        Main.getVoteStatisticsManager().saveAllPlayerVoteStatsToDatabase(true);
                        Main.getVoteManager().saveAllQueuedVotesToDatabase(true);
                    }
                }.runTaskTimer(plugin, 1200, 1200);
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
                Main.error(throwable);
            }
        });

    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }

    public static Database getDatabaseSQLite() {
        return databaseSQLite;
    }

    public static InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public static VoteSitesManager getVoteSitesManager() {

        if (voteSitesManager == null) {
            voteSitesManager = new VoteSitesManager(plugin);
            managers.add(voteSitesManager);
        }
        return voteSitesManager;
    }

    public static PlayerVoteStatisticsManager getVoteStatisticsManager() {
        if (voteStatisticsManager == null) {
            voteStatisticsManager = new PlayerVoteStatisticsManager(plugin);
            managers.add(voteStatisticsManager);
        }
        return voteStatisticsManager;
    }

    public static VoteManager getVoteManager() {
        if (voteManager == null) {
            voteManager = new VoteManager(plugin);
            managers.add(voteManager);
        }
        return voteManager;
    }

    public static TimeManager getTimeManager() {
        if (timeManager == null) {
            timeManager = new TimeManager(plugin);
            managers.add(timeManager);
        }
        return timeManager;
    }

    public static VoteNotificationManager getNotificationManager() {
        if (notificationManager == null) {
            notificationManager = new VoteNotificationManager(plugin);
            managers.add(notificationManager);
        }
        return notificationManager;
    }

    public static VoteReminderManager getVoteReminderManager() {
        if (voteReminderManager == null) {
            voteReminderManager = new VoteReminderManager(plugin);
            managers.add(voteReminderManager);
        }
        return voteReminderManager;
    }

    public static RewardManager getRewardManager() {
        if (rewardManager == null) {
            rewardManager = new RewardManager(plugin);
            managers.add(rewardManager);
        }
        return rewardManager;
    }

    public static Database getMainDatabase() {
        if (configManager.getDatabaseConfig().getDriver().equalsIgnoreCase("MySQL")) {
            return databaseMySQL;
        }
        return databaseSQLite;
    }

    public static Economy getEconomy() {
        return econ;
    }

    /**
     * Print a debug msg to the console.
     *
     * @param message The message to print
     */
    public static void debug(String message) {
        if (ConfigManager.isDebug)
            logger.info(message);
    }

    /**
     * Print a warning to the console.
     *
     * @param message The message to print
     */
    public static void warning(String message) {
        logger.warning(message);
    }

    public static void reloadManagers() {
        managers.forEach(Manager::reload);
    }

    @Override
    public void onEnable() {
        // load logger
        logger = this.getLogger();
        plugin = this;
        // Plugin startup logic

        boolean econIsReady = setupEconomy();
        if (econIsReady) {
            logger.info("Successfully hoked into vault - economy ");
        }

        // Load config manager first ->
        if (configManager == null) {
            configManager = new ConfigManager(this);
            managers.add(configManager);
        }

        initDatabases();
        loadManagers();
        registerListeners();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders(this).register();
        }

        languageManager = new LanguageManager(
                this,                                  // The plugin (used to get the languages bundled in the jar file)
                "languages",                           // Folder where the languages are stored
                getConfig().getString("language", "EN"),     // The language to use indicated by the plugin user
                "EN",                                  // The default language, expected to be shipped with the plugin and should be complete, fills in gaps in the user-selected language
                Collections.singletonList(getConfigManager().getPluginMessagePrefix()) // Chat prefix to use with Message#prefix(), could of course come from the config file
        );

    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll();
        managers.forEach(Manager::shutdown);
    }


    /**
     * Send a message to a target without a prefix.
     *
     * @param target       The target to send the message to
     * @param key          The key of the language string
     * @param replacements The replacements to insert in the message
     */
    public static void messageNoPrefix(Object target, String key, Object... replacements) {
        Message.fromKey(key).replacements(replacements).send(target);
    }

    /**
     * Send a message to a target, prefixed by the default chat prefix.
     *
     * @param target       The target to send the message to
     * @param key          The key of the language string
     * @param replacements The replacements to insert in the message
     */
    public static void message(Object target, String key, Object... replacements) {
        Message.fromKey(key).prefix().replacements(replacements).send(target);
    }

    /**
     * Print an error to the console.
     *
     * @param message The message to print
     */
    public static void error(Object... message) {
        logger.severe(StringUtils.join(message, " "));
    }

    private void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new OnVoteListener(this), this);
        pm.registerEvents(new PlayerListener(this), this);
    }

    private void loadManagers() {

        if (commandManager == null) {
            commandManager = new CommandManager(this);
            managers.add(commandManager);
        }
        if (voteManager == null) {
            voteManager = new VoteManager(this);
            managers.add(voteManager);
        }
        if (rewardManager == null) {
            rewardManager = new RewardManager(this);
            managers.add(rewardManager);
        }
        if (voteSitesManager == null) {
            voteSitesManager = new VoteSitesManager(this);
            managers.add(voteSitesManager);
        }
        if (voteSitesManager == null) {
            voteStatisticsManager = new PlayerVoteStatisticsManager(this);
            managers.add(voteStatisticsManager);
        }
        if (notificationManager == null) {
            notificationManager = new VoteNotificationManager(this);
            managers.add(notificationManager);
        }
        if (timeManager == null) {
            timeManager = new TimeManager(this);
            managers.add(timeManager);
        }
        if (voteReminderManager == null) {
            voteReminderManager = new VoteReminderManager(this);
            managers.add(voteReminderManager);
        }

        inventoryManager = new InventoryManager(this);
        inventoryManager.init();
    }

}
