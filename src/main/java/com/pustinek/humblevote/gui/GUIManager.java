package com.pustinek.humblevote.gui;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.utils.Manager;
import com.pustinek.humblevote.voteStatistics.constants.TOP_VOTES_STATS_TYPE;
import fr.minuskube.inv.SmartInventory;

import java.time.YearMonth;

public class GUIManager extends Manager {

    private final Main plugin;

    public GUIManager(Main plugin) {
        this.plugin = plugin;
    }


    public static SmartInventory displayVotingGUI() {
        return SmartInventory.builder()
                .manager(Main.getInventoryManager())
                .provider(new VotingGUI())
                .size(2, 9)
                .title("Voting sites")
                .build();
    }

    public static SmartInventory displayTopVotersGUI(TOP_VOTES_STATS_TYPE voteStats, YearMonth yearMonth) {
        return SmartInventory.builder()
                .manager(Main.getInventoryManager())
                .provider(new TopVotesGUI(voteStats, yearMonth))
                .size(5, 9)
                .title(
                        voteStats.equals(TOP_VOTES_STATS_TYPE.MONTH) ? "Monthly top voters (" + yearMonth.toString() + ")" : "Total top voters"
                )
                .build();
    }

    public static SmartInventory displayRewardsGUI() {
        return SmartInventory.builder()
                .manager(Main.getInventoryManager())
                .provider(new RewardsGUI())
                .size(3, 9)
                .title("Voting rewards")
                .build();
    }


}
