package com.pustinek.humblevote.gui;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.voteSites.VoteSite;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class VotingGUI implements InventoryProvider {

    @Override
    public void init(Player player, InventoryContents inventoryContents) {

        ArrayList<VoteSite> voteSites = Main.getVoteSitesManager().getVoteSites();
        ArrayList<VoteSite> filteredList = voteSites.stream().filter(VoteSite::isEnabledGUI).collect(Collectors.toCollection(ArrayList::new));

        ClickableItem[] items = new ClickableItem[voteSites.size()];

        for (int i = 0; i < filteredList.size(); i++) {
            VoteSite voteSite = filteredList.get(i);
            ItemStack is = voteSite.buildGUI(player);
            items[i] = ClickableItem.of(is, e -> {
                if(e.isLeftClick()) {
                    voteSite.sendMessage(player);
                    player.closeInventory();
                }
            });
            inventoryContents.set(0,i, items[i]);
        }

    }

    @Override
    public void update(Player player, InventoryContents inventoryContents) {

    }
}
