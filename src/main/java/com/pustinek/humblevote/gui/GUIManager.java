package com.pustinek.humblevote.gui;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.utils.Manager;
import fr.minuskube.inv.SmartInventory;

public class GUIManager extends Manager {

    private final Main plugin;

    public GUIManager(Main plugin) {
        this.plugin = plugin;
    }


    public static SmartInventory displayVotingGUI() {
        return SmartInventory.builder()
                .manager(Main.getInventoryManager())
                .provider(new VotingGUI())
                .size(5, 9)
                .title("Voting sites")
                .build();
    }


}
