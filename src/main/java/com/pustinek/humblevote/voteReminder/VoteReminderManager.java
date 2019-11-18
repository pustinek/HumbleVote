package com.pustinek.humblevote.voteReminder;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.configs.ConfigManager;
import com.pustinek.humblevote.utils.ChatUtils;
import com.pustinek.humblevote.utils.Manager;
import com.pustinek.humblevote.utils.Utils;
import com.pustinek.humblevote.voteSites.VoteSite;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

public class VoteReminderManager extends Manager {

    private static BukkitTask bukkitTask;
    private final Main plugin;
    private static ConfigManager configManager = null;


    public VoteReminderManager(Main plugin) {
        this.plugin = plugin;
        configManager = Main.getConfigManager();
        loadScheduler();
    }

    @Override
    public void shutdown() {
        if(bukkitTask != null) bukkitTask.cancel();
    }

    @Override
    public void reload() {

    }

    public void loadScheduler() {
        bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::remindPlayers,10, configManager.getVoteReminderRepeat() * 20);
    }

    private void remindPlayers() {
        if(!configManager.isVoteReminderEnabled()) return;
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        for(Player player : onlinePlayers) {
            remindPlayer(player);
        }
    }
    public void remindPlayer(Player player) {
        if(!configManager.isVoteReminderEnabled()) return;

        boolean votedOnAllSites = false;
        if(configManager.isDisableOnAllVotes()) {
            ArrayList<VoteSite> voteSites = Main.getVoteSitesManager().getVoteSites();
            Predicate<VoteSite> p1 = voteSite -> Utils.getPlayerVoteSiteCooldown(player, voteSite) == 0 && !voteSite.getService_site().equalsIgnoreCase("null");
            votedOnAllSites = voteSites.stream().noneMatch(p1);
        }


        if(!votedOnAllSites)
            player.sendMessage(ChatUtils.chatColor(configManager.getVoteReminderMessage()));
    }

}
