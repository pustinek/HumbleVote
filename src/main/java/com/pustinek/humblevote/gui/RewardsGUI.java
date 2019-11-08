package com.pustinek.humblevote.gui;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.utils.Utils;
import com.pustinek.humblevote.votingRewards.Reward;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class RewardsGUI implements InventoryProvider {
    @Override
    public void init(Player player, InventoryContents provider) {

        ArrayList<Reward> rewards = Main.getRewardManager().getVotingRewardArrayList();

        ClickableItem[] items = new ClickableItem[rewards.size()];

        for (int i = 0; i < rewards.size(); i++) {
            Reward reward = rewards.get(i);
            ItemStack is = reward.buildGUIItem(player);
            items[i] = ClickableItem.of(is, e -> {
                if(e.isLeftClick()) {
                    final boolean b = Main.getRewardManager().playerClaimReward(player, reward.getId());
                    if(b) {
                        player.sendMessage("You successfully claimed the reward.");
                        GUIManager.displayRewardsGUI().open(player);
                    }
                }

            });

            double d = (i / (double)9);

            provider.set(
                    Utils.limitInt((int)Math.floor(d), 8, 0),
                    i % 9,
                    items[i]);
        }

    }

    @Override
    public void update(Player player, InventoryContents provider) {

    }
}
