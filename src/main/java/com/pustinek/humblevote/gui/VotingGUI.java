package com.pustinek.humblevote.gui;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.utils.ChatUtils;
import com.pustinek.humblevote.voteSites.VoteSite;
import com.pustinek.humblevote.voteStatistics.PlayerVoteStats;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class VotingGUI implements InventoryProvider {



    @Override
    public void init(Player player, InventoryContents inventoryContents) {

        ArrayList<VoteSite> voteSites = Main.getVoteSitesManager().getVoteSites();
        PlayerVoteStats playerVoteStats = Main.getVoteStatisticsManager().getPlayerVoteStats(player.getUniqueId());
        ClickableItem[] items = new ClickableItem[voteSites.size()];

        for(int i = 0; i < voteSites.size(); i ++) {
            VoteSite voteSite = voteSites.get(i);
            ItemStack is = voteSite.buildGUI(player,playerVoteStats);
            items[i] = ClickableItem.of(is, e -> {
                if(e.isLeftClick()) {
                    voteSite.getVote_url().forEach(url -> player.sendMessage(ChatUtils.chatColor(url)));
                }
            });
            inventoryContents.set(0,i, items[i]);
        }



    }

    @Override
    public void update(Player player, InventoryContents inventoryContents) {

    }
}
