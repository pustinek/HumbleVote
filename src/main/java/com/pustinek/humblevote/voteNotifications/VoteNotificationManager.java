package com.pustinek.humblevote.voteNotifications;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.utils.ChatUtils;
import com.pustinek.humblevote.utils.Manager;
import com.pustinek.humblevote.utils.Utils;
import com.pustinek.humblevote.voting.QueuedVote;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;


/*
 * VoteNotificationManager - responsible for queueing vote messages into a pool.
 *                           One message for each player is than sent to the whole server
 *                           Implemented to not spam the server with voting messages
 */

public class VoteNotificationManager extends Manager {
    private final Main plugin;
    private static HashMap<UUID, QueuedNotifications> voteNotificationQueue = new HashMap<>();
    private static BukkitTask bukkitTask = null;


    @Override
    public void shutdown() {
        voteNotificationQueue = null;
        if(bukkitTask != null)
            bukkitTask.cancel();
    }



    public VoteNotificationManager(Main plugin) {
        this.plugin = plugin;
        loadScheduler();
    }

    private void loadScheduler() {
        if(Main.getConfigManager().isNotificationBroadcastEnabled()) {
            if(bukkitTask == null)
                bukkitTask = new BukkitRunnable() {
                    @Override
                    public void run() {

                        for (Iterator<QueuedNotifications> it = voteNotificationQueue.values().iterator(); it.hasNext();) {

                            QueuedNotifications qn = it.next();
                            if(qn.getTimeStamp().plusSeconds(Main.getConfigManager().getNotificationWaitTime() / 20).isBefore(Instant.now())) {


                                HashMap<String, String> replaceMap = new HashMap<>();
                                replaceMap.put("{player}", qn.getPlayerName());
                                replaceMap.put("{x}", qn.getVoteCount().toString());

                                String message = Utils.replaceStuffInString(replaceMap, Main.getConfigManager().getNotificationBroadcastMessage());
                                plugin.getServer().broadcastMessage(ChatUtils.chatColor(message));

                                it.remove();
                            }

                        }
                    }
                }.runTaskTimerAsynchronously(plugin, 120, Main.getConfigManager().getNotificationWaitTime() / 2);
        }
    }


    public void processNotification(Player player, QueuedVote queuedVote) {

        if(Main.getConfigManager().isNotificationBroadcastEnabled()) {
            QueuedNotifications queuedNotifications = voteNotificationQueue.get(player.getUniqueId());
            if(queuedNotifications == null) {
                queuedNotifications = new QueuedNotifications(player.getUniqueId(),player.getName(), Instant.now(), 1);
                voteNotificationQueue.put(player.getUniqueId(), queuedNotifications);
            }else {
                queuedNotifications.incrementVoteCount();
            }
        }else {
            HashMap<String, String> replaceMap = new HashMap<>();
            replaceMap.put("{player}", player.getName());
            replaceMap.put("{x}", "1");
            String message = Utils.replaceStuffInString(replaceMap, Main.getConfigManager().getNotificationBroadcastMessage());
            plugin.getServer().broadcastMessage(ChatUtils.chatColor(message));
        }
    }

    static class QueuedNotifications {
        Instant timeStamp;
        Integer voteCount;
        UUID playerID;
        String playerName;

        public QueuedNotifications(UUID playerID,String playerName, Instant timeStamp, Integer voteCount) {
            this.timeStamp = timeStamp;
            this.voteCount = voteCount;
            this.playerID = playerID;
            this.playerName = playerName;
        }

        public String getPlayerName() {
            return playerName;
        }

        public UUID getPlayerID() {
            return playerID;
        }

        public Instant getTimeStamp() {
            return timeStamp;
        }

        public Integer getVoteCount() {
            return voteCount;
        }
        public void incrementVoteCount() {
            voteCount += 1;
            timeStamp = Instant.now();
        }
    }

}

