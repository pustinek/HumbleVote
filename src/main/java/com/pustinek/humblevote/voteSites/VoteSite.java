package com.pustinek.humblevote.voteSites;

import com.pustinek.humblevote.voteStatistics.PlayerVoteStats;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class VoteSite {

    private boolean enabled;
    private String service_site;
    private ArrayList<String> vote_url;
    private Integer vote_delay;
    private List<String> voteRewards;
    private GUIItem guiItem;

    public VoteSite(boolean enabled, String service_site,ArrayList<String> vote_url,  Integer vote_delay, List<String> voteRewards, GUIItem gui) {
        this.enabled = enabled;
        this.service_site = service_site;
        this.vote_delay = vote_delay;
        this.voteRewards = voteRewards;
        this.guiItem = gui;
        this.vote_url = vote_url;
    }



    public boolean isEnabled() {
        return enabled;
    }

    public String getService_site() {
        return service_site;
    }

    public ArrayList<String> getVote_url() {
        return vote_url;
    }

    public Integer getVote_delay() {
        return vote_delay;
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

        //String timeVotes = playerVoteStats.getVoteSiteLastVote(service_site);

        ItemStack itemStack = new ItemStack(enabledMaterial);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(guiItem.lore);
        itemMeta.setDisplayName(guiItem.name);

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public static class GUIItem {

        private String name;
        private ArrayList<String> lore;
        private String enabledMaterial;
        private String disabledMaterial;
        private ArrayList<String> urlDisplay;

        public GUIItem(String name, ArrayList<String> lore, String enabledMaterial, String disabledMaterial) {
            this.name = name;
            this.lore = lore;
            this.enabledMaterial = enabledMaterial;
            this.disabledMaterial = disabledMaterial;
        }




    }


}
