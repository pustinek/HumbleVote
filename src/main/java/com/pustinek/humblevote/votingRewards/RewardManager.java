package com.pustinek.humblevote.votingRewards;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.utils.Manager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RewardManager extends Manager {


    private final Main plugin;

    public RewardManager(Main plugin) {
        this.plugin = plugin;
    }

    public void checkPlayerRewardEligibility(Player player) {


        ItemStack is = new ItemStack(Material.DIAMOND);
        player.getInventory().addItem(is);
        player.updateInventory();
    }

    public void parseRewards() {



    }
}
