package com.pustinek.humblevote.configs;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.sql.DatabaseConfig;
import com.pustinek.humblevote.utils.Manager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager extends Manager {
    private final Main plugin;
    private FileConfiguration config;
    public static boolean isDebug = true;

    private String pluginMessagePrefix = "[HumbleVote] ";
    private boolean proccessFakeVotes = false;

    private DatabaseConfig databaseConfig;

    //Config variables
    private String configVersion;

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
        proccessFakeVotes = section.getBoolean("fake_votes", false);
    }



    private void loadRewards() {
        File customConfigFile;
        FileConfiguration customConfig;
        customConfigFile = new File(plugin.getDataFolder(), "rewards.yml");

        //create file if it doesn't exist
        if(!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();

        }
        // save data from resource to the players plugin one
        plugin.saveResource("rewards.yml", false);
        customConfig= new YamlConfiguration();
        try {
            customConfig.load(customConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        // === Start loading data ===
    }


    public String getPluginMessagePrefix() {
        return pluginMessagePrefix;
    }

    public DatabaseConfig getDatabaseConfig() {
        return databaseConfig;
    }

    public boolean isProccessFakeVotes() {
        return proccessFakeVotes;
    }
}
