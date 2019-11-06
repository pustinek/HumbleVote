package com.pustinek.humblevote.voteSites;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.voteStatistics.PlayerVoteStats;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VoteSite {

    private boolean enabled;
    private String service_site;
    private ArrayList<String> voteURL;
    private Long voteCooldown;
    private List<String> voteRewards;
    private GUIItem guiItem;

    public VoteSite(boolean enabled, String service_site,ArrayList<String> voteURL,  Long voteCooldown, List<String> voteRewards, GUIItem gui) {
        this.enabled = enabled;
        this.service_site = service_site;
        this.voteCooldown = voteCooldown;
        this.voteRewards = voteRewards;
        this.guiItem = gui;
        this.voteURL = voteURL;
    }


    public boolean isEnabled() {
        return enabled;
    }

    public String getService_site() {
        return service_site;
    }

    public ArrayList<String> getVoteURL() {
        return voteURL;
    }

    public long getVoteCooldown() {
        return voteCooldown;
    }



    public List<String> getVoteRewards() {
        return voteRewards;
    }


    @Override
    public int hashCode() {
        return super.hashCode();
    }


    public ItemStack buildGUI(Player player, PlayerVoteStats playerVoteStats) {
        Material enabledMaterial = Material.getMaterial(guiItem.enabledMaterial);
        Material disabledMaterial = Material.getMaterial(guiItem.disabledMaterial);
        ItemStack itemStack = null;

        PlayerVoteStats ps = Main.getVoteStatisticsManager().getPlayerVoteStats(player.getUniqueId());
        long timeElapsed = 99999;
        if(ps != null) {
            String voteTimestamp = ps.getPlayerVoteSiteLastVoteTimestamp(service_site);
            if(voteTimestamp != null) {
                long voteTimestampAsLong = Long.parseLong(voteTimestamp);
                Instant start = Instant.ofEpochMilli(voteTimestampAsLong);
                Instant finish = Instant.now();

                timeElapsed = Duration.between(start, finish).toMinutes();


            }
        }
        if(timeElapsed < voteCooldown) {
            itemStack = new ItemStack(disabledMaterial);
        }else {
            itemStack = new ItemStack(enabledMaterial);
        }
        long timeLeft = voteCooldown - timeElapsed;
        if(timeLeft < 0) timeLeft = 0;


        ArrayList<String> lore = new ArrayList<>();

        long finalTimeLeft = timeLeft;

        Date resultdate = new Date(finalTimeLeft);

        long hours = finalTimeLeft / 60; //since both are ints, you get an int
        long minutes = finalTimeLeft % 60;


        for (String s : guiItem.lore) {
            String x = s.replace("{cooldown}", (hours + "h " + minutes + "min"));
            lore.add(x);
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(lore);
        itemMeta.setDisplayName(guiItem.name);
        //String timeVotes = playerVoteStats.getVoteSiteLastVote(service_site);
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    static class GUIItem {

        private String name;
        private ArrayList<String> lore;
        private String enabledMaterial;
        private String disabledMaterial;
        private ArrayList<String> urlDisplay;

        GUIItem(String name, ArrayList<String> lore, String enabledMaterial, String disabledMaterial) {
            this.name = name;
            this.lore = lore;
            this.enabledMaterial = enabledMaterial;
            this.disabledMaterial = disabledMaterial;
        }
    }


}
