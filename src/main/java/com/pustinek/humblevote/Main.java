package com.pustinek.humblevote;

import com.pustinek.humblevote.commands.CommandManager;
import com.pustinek.humblevote.configs.ConfigManager;
import com.pustinek.humblevote.listeners.OnVoteListener;
import com.pustinek.humblevote.listeners.PlayerListener;
import com.pustinek.humblevote.sql.Database;
import com.pustinek.humblevote.sql.MySQL;
import com.pustinek.humblevote.sql.SQLite;
import com.pustinek.humblevote.utils.Callback;
import com.pustinek.humblevote.utils.ChatUtils;
import com.pustinek.humblevote.utils.Manager;
import com.pustinek.humblevote.voteSites.VoteSitesManager;
import com.pustinek.humblevote.voteStatistics.VoteStatisticsManager;
import com.pustinek.humblevote.voting.VoteManager;
import com.pustinek.humblevote.votingRewards.RewardManager;
import fr.minuskube.inv.InventoryManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;


public final class Main extends JavaPlugin {
    // Private static variables
    private static Logger logger;

    private static Main plugin;
    // Managers:
    private static Set<Manager> managers = new HashSet<>();
    private static CommandManager commandManager = null;
    private static ConfigManager configManager = null;
    private static VoteManager voteManager = null;
    private static RewardManager rewardManager = null;
    private static InventoryManager inventoryManager = null;
    private static VoteSitesManager voteSitesManager = null;
    private static VoteStatisticsManager voteStatisticsManager = null;

    private static Database databaseMySQL = null;
    private static Database databaseSQLite = null;
    // General variables:




    @Override
    public void onEnable() {
        // load logger
        logger = this.getLogger();
        plugin = this;
        // Plugin startup logic

        //load managers


        // Load config manager first ->
        configManager = new ConfigManager(this);
        managers.add(configManager);

        initDatabases();
        loadManagers();
        initializeVotes();
        registerListeners();

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Main.debug("On Disable !!");
        Main.getVoteManager().saveAllQueuedVotesToDatabase(false);
        Main.debug("Votes should be saved to Database");
    }

    /**
     * Print a warning to the console.
     * @param message The message to print
     */
    public static void warrning(String message) {
        logger.warning(message);
    }

    /**
     * Print a debug msg to the console.
     * @param message The message to print
     */
    public static void debug(String message) {
        if (ConfigManager.isDebug)
            logger.info(message);
    }

    /**
     * Send message to sender, without plugin prefix
     *
     * @param sender  the one to which send the message
     * @param message message to be sent
     */
    public static void messageNoPrefix(CommandSender sender, String message) {
        sender.sendMessage(ChatUtils.chatColor(message));
    }

    /**
     * Send message to sender with plugin prefix
     *
     * @param sender  the one to which send the message
     * @param message message to be sent
     */
    public static void message(CommandSender sender, String message) {
        sender.sendMessage(ChatUtils.chatColor(configManager.getPluginMessagePrefix()) + ChatUtils.chatColor(message));
    }

    /**
     * Print an error to the console.
     * @param message The message to print
     */
    public static void error(Object... message) {
        logger.severe(StringUtils.join(message, " "));
    }

    public static CommandManager getCommandManager() {
        return commandManager;
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }

    public static Database getDatabaseMySQL() {
        return databaseMySQL;
    }

    public static Database getDatabaseSQLite() {
        return databaseSQLite;
    }

    public static InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public static VoteSitesManager getVoteSitesManager() {

        if(voteSitesManager == null) {
            voteSitesManager = new VoteSitesManager(plugin);
            managers.add(voteSitesManager);
        }
        return voteSitesManager;
    }
    public static VoteStatisticsManager getVoteStatisticsManager() {
        if(voteStatisticsManager == null) {
            voteStatisticsManager = new VoteStatisticsManager(plugin);
            managers.add(voteStatisticsManager);
        }
        return voteStatisticsManager;
    }

    public static Database getMainDatabase() {
        if(configManager.getDatabaseConfig().getDriver().equalsIgnoreCase("MySQL")) {
            return databaseMySQL;
        }
        return databaseSQLite;
    }

    public static VoteManager getVoteManager() {
        if(voteManager == null) {
            voteManager = new VoteManager(plugin);
            managers.add(voteManager);
        }
        return voteManager;
    }

    public static RewardManager getRewardManager() {
        return rewardManager;
    }

    private void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new OnVoteListener(this), this);
        pm.registerEvents(new PlayerListener(this), this);
    }

    private void loadManagers() {

        if(commandManager == null) {
            commandManager = new CommandManager(this);
            managers.add(commandManager);
        }
        if(voteManager == null) {
            voteManager = new VoteManager(this);
            managers.add(voteManager);
        }
        if(rewardManager == null) {
            rewardManager = new RewardManager(this);
            managers.add(rewardManager);
        }
        if(voteSitesManager == null) {
            voteSitesManager = new VoteSitesManager(this);
            managers.add(voteSitesManager);
        }
        if(voteSitesManager == null) {
            voteStatisticsManager = new VoteStatisticsManager(this);
            managers.add(voteStatisticsManager);
        }




        inventoryManager = new InventoryManager(this);
        inventoryManager.init();


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
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
                Main.error(throwable);
            }
        });

    }


    private void initializeVotes() {
        Main.getVoteManager().reloadVotes();
    }
}
