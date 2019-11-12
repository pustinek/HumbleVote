package com.pustinek.humblevote.configs;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.sql.DatabaseConfig;
import com.pustinek.humblevote.utils.Manager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.ZoneId;

public class ConfigManager extends Manager {


    private final Main plugin;
    private static FileConfiguration config;
    public static boolean isDebug = true;

    private String pluginMessagePrefix = "[HumbleVote] ";

    //Database-section variables
    private DatabaseConfig databaseConfig;

    //Votes-section variables
    private boolean processFakeVotes = false;
    private String notificationBroadcastMessage = "";
    private Integer notificationWaitTime = 1200;
    private boolean notificationBroadcastEnabled = true;
    private ZoneId zoneId = ZoneId.systemDefault();

    private boolean voteReminderEnabled;
    private boolean voteReminderOnJoin;
    private int voteReminderRepeat;
    private String voteReminderMessage;
    private boolean voteReminderDisableOnAllVotes;

    private boolean messageEnabled;
    private String messageContent;



    //Development-section variables
    public boolean devEnabled = false;
    public int devYear = 2019;
    public int devMonth = 12;
    public int devDay = 1;
    public int devHour = 12;
    public int devMinute = 12;
    public String devZoneOffset = "+00:00";



    public ConfigManager(Main plugin) {

        this.plugin = plugin;
        reloadConfig();
    }

    public void reloadConfig(){
        try{
            plugin.getLogger().info("(re)loading Configs...");
            //Create config file if it doesn't exist
            plugin.saveDefaultConfig();
            plugin.getConfig().options().copyDefaults(true);
            //Reload config
            plugin.reloadConfig();
            config = plugin.getConfig();




            //Start reading from config file
            loadConfig();
        }catch (Exception e){
            Main.debug(e.getMessage());
        }
    }
    private void loadConfig() {
        isDebug = config.getBoolean("debug", false);
        plugin.getLogger().info("debug is " + isDebug);
        pluginMessagePrefix = config.getString("plugin_message_prefix");

        loadSectionVotes(config.getConfigurationSection("votes"));
        loadSectionSQL(config.getConfigurationSection("sql"));
        loadSectionDevelopment(config.getConfigurationSection("development"));
    }

    private void loadSectionSQL(ConfigurationSection section) {

        if (section == null) {
            //TODO: stop the server or use SQL settings
            Main.error("failed to load SQL configuration !");
            return;
        }

        String table_prefix = section.getString("table_prefix");
        if (table_prefix == null || !table_prefix.matches("^([a-zA-Z0-9\\-_]+)?$")) {
            Main.error("Database table prefix contains illegal letters or is missing, using 'groupchat_' prefix.");
            table_prefix = "groupchat_";
        }

        databaseConfig = new DatabaseConfig(
                section.getString("driver"),
                section.getString("address"),
                section.getString("database"),
                section.getString("username"),
                section.getString("password"),
                table_prefix,
                section.getString("options"),
                section.getInt("ping_interval"));
    }

    private void loadSectionVotes(ConfigurationSection section) {
        if (section == null) {
            Main.error("failed to load votes section in config.yml !");
            return;
        }
        processFakeVotes = section.getBoolean("fake_votes", false);
        // ZoneID
        String timezoneIdAsString = section.getString("timezone_id", "UTC");
        zoneId = ZoneId.of(timezoneIdAsString);

        // === vote_message section ===
        ConfigurationSection messageCS = section.getConfigurationSection("vote_message");
        if (messageCS != null) {
            messageEnabled = messageCS.getBoolean("enabled", true);
            messageContent = messageCS.getString("message", "You successfully voted on website {vote_site}.");
        }


        // === vote_notification section ===
        ConfigurationSection broadcastCS = section.getConfigurationSection("vote_broadcast");
        if (broadcastCS == null) {
            Main.warning("vote_broadcast is missing in config.yml file !");
            return;
        }
        notificationBroadcastEnabled = broadcastCS.getBoolean("queued_broadcast", false);
        notificationWaitTime = broadcastCS.getInt("queued_broadcast_wait_time", 1200);
        notificationBroadcastMessage = broadcastCS.getString("broadcast_message", "Player has voted ");

        // === vote_reminder section ===
        ConfigurationSection reminderCS = section.getConfigurationSection("vote_reminder");
        if(reminderCS == null) {
            Main.warning("vote_reminder section is missing in config.yml file !");
            return;
        }

        voteReminderEnabled = reminderCS.getBoolean("enabled", true);
        voteReminderOnJoin = reminderCS.getBoolean("on_join", true);
        voteReminderRepeat = reminderCS.getInt("repeat", 180);
        voteReminderMessage = reminderCS.getString("message", "&8Remember to vote for our server with &6/hvote&8 voting");
        voteReminderDisableOnAllVotes = reminderCS.getBoolean("disable_on_all_votes", true);
    }


    private void loadSectionDevelopment(ConfigurationSection section) {
        if (section == null) {
            Main.error("failed to load development section in config.yml !");
            return;
        }

        devEnabled = section.getBoolean("enabled");
        devYear = section.getInt("year", 2000);
        devMonth = section.getInt("month", 1);
        devDay = section.getInt("day", 1);
        devHour = section.getInt("hour", 0);
        devMinute = section.getInt("minute", 0);
        devZoneOffset = section.getString("zoneOffset", "+00:00");
    }

    /*
    * ==== GETTERS ====
    */
    public String getPluginMessagePrefix() {
        return pluginMessagePrefix;
    }

    public DatabaseConfig getDatabaseConfig() {
        return databaseConfig;
    }

    public boolean isProcessFakeVotes() {
        return processFakeVotes;
    }

    public String getNotificationBroadcastMessage() {
        return notificationBroadcastMessage;
    }

    public Integer getNotificationWaitTime() {
        return notificationWaitTime;
    }

    public boolean isNotificationBroadcastEnabled() {
        return notificationBroadcastEnabled;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public int getVoteReminderRepeat() {
        return voteReminderRepeat;
    }

    public String getVoteReminderMessage() {
        return voteReminderMessage;
    }

    public boolean isVoteReminderEnabled() {
        return voteReminderEnabled;
    }

    public boolean isDisableOnAllVotes() {
        return voteReminderDisableOnAllVotes;
    }

    public boolean isVoteReminderOnJoin() {
        return voteReminderOnJoin;
    }
}
