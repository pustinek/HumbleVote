package com.pustinek.humblevote.voteSites;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.utils.ChatUtils;
import com.pustinek.humblevote.utils.Utils;
import com.pustinek.humblevote.voteStatistics.PlayerVoteSiteHistory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

public class VoteSite {

    private boolean enabled;
    private String service_site;
    private String voteURL;
    private Long voteCooldown;
    private GUIItem guiItem;
    private boolean enabledGUI;

    VoteSite(boolean enabled, String service_site, String voteURL, Long voteCooldown, GUIItem gui, boolean enabledGUI) {
        this.enabled = enabled;
        this.service_site = service_site;
        this.voteCooldown = voteCooldown;
        this.guiItem = gui;
        this.voteURL = voteURL;
        this.enabledGUI = enabledGUI;
    }


    public boolean isEnabled() {
        return enabled;
    }

    public String getService_site() {
        return service_site;
    }

    public String getVoteURL() {
        return voteURL;
    }

    public long getVoteCooldown() {
        return voteCooldown;
    }

    public boolean isEnabledGUI() {
        return enabledGUI;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }


    public ItemStack buildGUI(Player player) {
        Material enabledMaterial = Material.getMaterial(guiItem.enabledMaterial);
        Material disabledMaterial = Material.getMaterial(guiItem.disabledMaterial);
        if (enabledMaterial == null || disabledMaterial == null) {
            Main.warning("failed to build VoteSite GUI - invalid materials for voteSite " + guiItem.name);
            return null;
        }
        ItemStack itemStack;

        long timeElapsed = 99999;
        long voteCooldown = getVoteCooldown();
        PlayerVoteSiteHistory playerVoteSiteHistory = Main.getVoteStatisticsManager().getPlayerVoteSiteHistory(player.getUniqueId(), service_site);


        if (playerVoteSiteHistory != null) {
            String voteTimestamp = playerVoteSiteHistory.getTimestamp();
            long voteTimestampAsLong = Long.parseLong(voteTimestamp);

            Instant start = Instant.ofEpochSecond(voteTimestampAsLong);
            Instant finish = Instant.now();

            timeElapsed = Duration.between(start, finish).toMinutes();
        }

        long timeLeft = voteCooldown - timeElapsed;
        if (timeLeft < 0) timeLeft = 0;
        if (timeLeft > 0) {
            itemStack = new ItemStack(disabledMaterial);
        }else {
            itemStack = new ItemStack(enabledMaterial);
        }

        ArrayList<String> lore = new ArrayList<>();

        long hours = timeLeft / 60; //since both are ints, you get an int
        long minutes = timeLeft % 60;


        for (String s : guiItem.lore) {
            String x = s.replace("{cooldown}", (hours + "h " + minutes + "min"));
            lore.add(x);
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setLore(ChatUtils.chatColor(lore));
            itemMeta.setDisplayName(ChatUtils.chatColor(guiItem.name));
            itemStack.setItemMeta(itemMeta);
        }


        return itemStack;
    }

    public void sendMessage(Player player) {
        ArrayList<String> message = Main.getVoteSitesManager().getOnClickMessage();
        HashMap<String, String> replaceMap = new HashMap<>();
        replaceMap.put("{vote_url}", voteURL);
        ArrayList<String> msg = Utils.replaceStuffInString(replaceMap, message);
        msg.forEach(player::sendMessage);
    }

    static class GUIItem {

        private String name;
        private ArrayList<String> lore;
        private String enabledMaterial;
        private String disabledMaterial;

        GUIItem(String name, ArrayList<String> lore, String enabledMaterial, String disabledMaterial) {
            this.name = name;
            this.lore = lore;
            this.enabledMaterial = enabledMaterial;
            this.disabledMaterial = disabledMaterial;
        }
    }


}
