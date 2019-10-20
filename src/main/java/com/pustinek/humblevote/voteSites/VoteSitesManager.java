package com.pustinek.humblevote.voteSites;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.utils.Manager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class VoteSitesManager extends Manager {

    private final Main plugin;

    private ArrayList<VoteSite> voteSitesArrayList = new ArrayList<>();

    public VoteSitesManager(Main plugin) {
        this.plugin = plugin;
        loadVotingSites();
    }


    /*
    *
    * (Re)Load configuration file for voting sites (voteSite.yml)
    *
    * */
    public void loadVotingSites() {

        // Clean up array lists !
        voteSitesArrayList = new ArrayList<>();

        File customConfigFile;
        FileConfiguration customConfig;
        customConfigFile = new File(plugin.getDataFolder(), "voteSites.yml");

        //create file if it doesn't exist
        if(!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();

        }
        plugin.saveResource("voteSites.yml", false);
        customConfig= new YamlConfiguration();
        try {
            customConfig.load(customConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        // === Start loading data ===

        ConfigurationSection voteSites = customConfig.getConfigurationSection("vote_sites");
        if(voteSites == null) return;
        for(String key : voteSites.getKeys(false)){
            Main.debug("key:" + key);

            Main.debug("enabled: " + voteSites.getString(key + ".enabled"));

            ConfigurationSection guiSection = voteSites.getConfigurationSection(key + ".gui");

            if(guiSection == null) {
                Main.error("No GUI configuration section found in VoteSites");
                return;
            }

            VoteSite voteSite = new VoteSite(
                    voteSites.getBoolean(key + ".enabled", true),
                    voteSites.getString(key + ".service_site"),
                    new ArrayList<>(voteSites.getStringList(key + ".vote_url")),
                    voteSites.getInt(key + ".vote_cooldown"),
                    voteSites.getStringList(key + ".rewards"),
                    new VoteSite.GUIItem(
                            guiSection.getString("name", "default"),
                            new ArrayList<>(guiSection.getStringList("lore")),
                            guiSection.getString("enabled_icon", "STONE"),
                            guiSection.getString("disabled_icon", "STONE")
                    ));

            Main.debug("loading VoteSite --> service-site: " +voteSite.getService_site());

            voteSitesArrayList.add(voteSite);
        }
    }


    public ArrayList<VoteSite> getVoteSites() {
        return new ArrayList<>(voteSitesArrayList);
    }



}
