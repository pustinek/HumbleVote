package com.pustinek.humblevote.listeners;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.utils.Callback;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final Main plugin;

    public PlayerListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoinListener(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        // Vote reminder on join
        if(Main.getConfigManager().isVoteReminderOnJoin())
            Main.getVoteReminderManager().remindPlayer(player);


        Main.getVoteStatisticsManager().checkCreatePlayerVoteStatistic(player, new Callback<Integer>(plugin) {
            @Override
            public void onResult(Integer result) {
                super.onResult(result);
                // Check for queued player votes after stats for the player have been resolved
                Main.getVoteManager().resolvePlayerQueuedVotes(player);
            }

            @Override
            public void onError(Throwable throwable) {
                Main.error(throwable);
                super.onError(throwable);
            }
        });

        Main.getVoteStatisticsManager().loadPlayerVoteSiteHistoryFromDatabase(event.getPlayer().getUniqueId());

        Main.getRewardManager().checkPlayerRewardEligibility(player);

    }

    @EventHandler
    public void onPlayerQuitListener(PlayerQuitEvent event) {
        Main.getVoteStatisticsManager().savePlayerVoteStatsToDatabase(event.getPlayer().getUniqueId());
        Main.getVoteStatisticsManager().savePlayerVoteSiteHistoryToDatabase(event.getPlayer().getUniqueId(), true);
    }

}
