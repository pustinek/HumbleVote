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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TopVotesGUI implements InventoryProvider {

    private final TOP_VOTES_STATS_TYPE voteStats;
    private final YearMonth yearMonth;

    TopVotesGUI(TOP_VOTES_STATS_TYPE voteStats, YearMonth yearMonth) {
        this.voteStats = voteStats;
        this.yearMonth = yearMonth;
    }

    @Override
    public void init(Player player, InventoryContents inventoryContents) {

        Pagination pagination = inventoryContents.pagination();


        PlayerVoteStatisticsManager vsm = Main.getVoteStatisticsManager();

        List<PlayerVoteStats> playerVoteStats = vsm.getPlayerVoteStatsByTop(50, voteStats, yearMonth);

        ClickableItem[] items = new ClickableItem[playerVoteStats.size()];

        for (int i = 0; i < playerVoteStats.size(); i++) {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta testMeta = (SkullMeta) skull.getItemMeta();

            if (testMeta != null) {
                testMeta.setDisplayName(playerVoteStats.get(i).getPlayerLastUsername());
                testMeta.setLore(Arrays.asList(
                        "total: " + playerVoteStats.get(i).getTotalVoteCount(),
                        "monthly: " + playerVoteStats.get(i).getMonthlyVoteCount(yearMonth)));

                skull.setAmount(i + 1);
                Player topPlayer = Bukkit.getPlayer(playerVoteStats.get(i).getPlayerUUID());
                if (topPlayer != null && topPlayer.isOnline()) {
                    testMeta.setOwningPlayer(player);
                }
                skull.setItemMeta(testMeta);
            }
            items[i] = ClickableItem.empty(skull);
        }

        pagination.setItems(items);
        pagination.setItemsPerPage(36);
        pagination.addToIterator(inventoryContents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0));

        // Navigation background init:
        ItemStack navItemStack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta itemMeta = navItemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName("");
            navItemStack.setItemMeta(itemMeta);
        }
        ClickableItem navBorder = ClickableItem.empty(
                navItemStack
        );

        inventoryContents.fillRow(4, navBorder);

        // Navigation button init:
        inventoryContents.set(4, 4, ClickableItem.of(
                GUIItemGenerator.itemGenerator(voteStats == TOP_VOTES_STATS_TYPE.MONTH ? "&6Monthly votes" : "&6Total votes", Material.SEA_PICKLE, Collections.singletonList(""))
                ,
                e -> GUIManager.displayTopVotersGUI(voteStats == TOP_VOTES_STATS_TYPE.MONTH ? TOP_VOTES_STATS_TYPE.TOTAL : TOP_VOTES_STATS_TYPE.MONTH, yearMonth).open(player)));
        inventoryContents.set(4, 1, ClickableItem.of(
                GUIItemGenerator.itemGenerator("&cprevious page", Material.ARROW, Collections.singletonList(""))
                ,
                e -> GUIManager.displayTopVotersGUI(voteStats, yearMonth).open(player, pagination.previous().getPage())));
        inventoryContents.set(4, 7, ClickableItem.of(
                GUIItemGenerator.itemGenerator("&2next page", Material.ARROW, Collections.singletonList(""))
                ,
                e -> GUIManager.displayTopVotersGUI(voteStats, yearMonth).open(player, pagination.next().getPage())));
    }

    @Override
    public void update(Player player, InventoryContents inventoryContents) {

    }
}
