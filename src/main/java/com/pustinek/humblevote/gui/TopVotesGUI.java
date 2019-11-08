package com.pustinek.humblevote.gui;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.utils.GUIItemGenerator;
import com.pustinek.humblevote.voteStatistics.PlayerVoteStatisticsManager;
import com.pustinek.humblevote.voteStatistics.PlayerVoteStats;
import com.pustinek.humblevote.voteStatistics.constants.TOP_VOTES_STATS_TYPE;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TopVotesGUI implements InventoryProvider {


    protected final TOP_VOTES_STATS_TYPE voteStats;

    public TopVotesGUI(TOP_VOTES_STATS_TYPE voteStats) {
        this.voteStats = voteStats;
    }


    @Override
    public void init(Player player, InventoryContents inventoryContents) {

        Pagination pagination = inventoryContents.pagination();


        PlayerVoteStatisticsManager vsm = Main.getVoteStatisticsManager();

        List<PlayerVoteStats> playerVoteStats =  vsm.getPlayerVoteStatsByTop(50, voteStats);

        ClickableItem[] items = new ClickableItem[playerVoteStats.size()];

        for (int i = 0; i < playerVoteStats.size(); i++) {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta testMeta = (SkullMeta) skull.getItemMeta();

            if (testMeta != null) {
                testMeta.setDisplayName(playerVoteStats.get(i).getPlayerLastUsername());
                testMeta.setLore(Arrays.asList(
                        "total: " + playerVoteStats.get(i).getTotalVoteCount(),
                        "monthly: " + playerVoteStats.get(i).getMonthlyVoteCount(Main.getTimeManager().getYearMonth())));
                skull.setItemMeta(testMeta);
                skull.setAmount(i + 1);
            }
            items[i] = ClickableItem.empty(skull);
        }

        pagination.setItems(items);
        pagination.setItemsPerPage(36);
        pagination.addToIterator(inventoryContents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0));


        ClickableItem navBorder = ClickableItem.empty(
                new ItemStack(Material.GRAY_STAINED_GLASS_PANE)
        );

        inventoryContents.fillRow(4, navBorder);

        inventoryContents.set(4, 0, ClickableItem.of(
                GUIItemGenerator.itemGenerator(voteStats == TOP_VOTES_STATS_TYPE.MONTH ? "&4Top Monthly Votes" : "&4Top Total Votes", Material.SEA_PICKLE, Collections.singletonList(""))
                ,
                e -> GUIManager.displayTopVotersGUI(voteStats == TOP_VOTES_STATS_TYPE.MONTH ? TOP_VOTES_STATS_TYPE.TOTAL : TOP_VOTES_STATS_TYPE.MONTH).open(player)));
        inventoryContents.set(4, 6, ClickableItem.of(
                GUIItemGenerator.itemGenerator("&2previous page", Material.ARROW, Collections.singletonList(""))
                ,
                e -> GUIManager.displayTopVotersGUI(voteStats).open(player, pagination.previous().getPage())));
        inventoryContents.set(4, 8, ClickableItem.of(
                GUIItemGenerator.itemGenerator("&cnext page", Material.ARROW, Collections.singletonList(""))
                ,
                e -> GUIManager.displayTopVotersGUI(voteStats).open(player, pagination.next().getPage())));



    }

    @Override
    public void update(Player player, InventoryContents inventoryContents) {

    }
}
