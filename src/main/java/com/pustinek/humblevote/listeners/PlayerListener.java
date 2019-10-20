package com.pustinek.humblevote.listeners;

import com.pustinek.humblevote.Main;
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
        Main.getVoteStatisticsManager().checkCreatePlayerVoteStatistic(event.getPlayer());
        Main.getVoteManager().processAllPlayerQueuedVotes(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuitListener(PlayerQuitEvent event) {

    }

}
