package com.pustinek.humblevote.voting;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.utils.Callback;
import com.pustinek.humblevote.utils.Manager;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class VoteManager extends Manager {

    public static AtomicInteger receivedVotes = new AtomicInteger(0);
    private final Main plugin;
    private final static Map<String, ArrayList<QueuedVote>> queuedVotesHashMap = new ConcurrentHashMap<>(32, 0.75f, 2); // Username, List<QueuedVote>

    public VoteManager(Main plugin) {
        this.plugin = plugin;
    }


    /**
     * Process single player vote
     *
     * @param player online player
     */
    public boolean processPlayerVote(QueuedVote vote, Player player) {
        if(player == null || !player.isOnline()) return false;


        player.sendMessage("You voted on website" + vote.getServiceName() + " !");
        Main.debug("processing vote from player" + player.getName());

        Main.getNotificationManager().processNotification(player, vote);
        Main.getRewardManager().checkPlayerRewardEligibility(player);
        Main.getRewardManager().processPlayerVoteReward(player);
        Main.getVoteStatisticsManager().processVoteForStatistics(vote, player);

        return true;
    }



    /**
     * Process all queued player votes that might be in the Database or cache
     *
     * @param player online player
     */
    public void resolvePlayerQueuedVotes(Player player) {

        ArrayList<QueuedVote> cacheVotes = queuedVotesHashMap.get(player.getName());

        if(cacheVotes == null) {
            cacheVotes = new ArrayList<>();
        }

        for (Iterator<QueuedVote> it = cacheVotes.iterator(); it.hasNext();) {
            boolean remove = processPlayerVote(it.next(),player);
            if(remove){
                it.remove();
            }
        }


        new BukkitRunnable() {
            @Override
            public void run() {
                Main.getDatabaseSQLite().getPlayerQueuedVotes(player.getName(), new Callback<ArrayList<QueuedVote>>(plugin) {
                    @Override
                    public void onResult(ArrayList<QueuedVote> databaseVotes) {

                        ArrayList<QueuedVote> processedDatabaseVotes = new ArrayList<>();

                        for (QueuedVote queuedVote: databaseVotes) {
                            boolean success = processPlayerVote(queuedVote,player);
                            if(success) {
                                processedDatabaseVotes.add(queuedVote);
                            }
                        }

                        if(processedDatabaseVotes.size() > 0) {
                            Main.getDatabaseSQLite().deletePlayerQueuedVotes(player.getName(), processedDatabaseVotes, new Callback<Integer>(plugin) {
                                @Override
                                public void onResult(Integer result) {

                                }
                                @Override
                                public void onError(Throwable throwable) {
                                    Main.error(throwable);
                                }
                            });
                        }




                    }
                });

            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Add queued vote to cache/queue
     *
     * @param vote vote to be added to queue/cache
     */
    public synchronized void addVoteToCache(QueuedVote vote) {
        String username = vote.getUsername();
        queuedVotesHashMap.computeIfAbsent(username, k -> new ArrayList<>()).add(vote);
    }

    /**
     * Save user votes from cache to database
     *
     * @param async should the operation to database be async ?
     */
    public void saveAllQueuedVotesToDatabase(boolean async) {

        final Collection<ArrayList<QueuedVote>> values = new ArrayList<>(queuedVotesHashMap.values());
        final List<QueuedVote> collect = values.stream().flatMap(Collection::stream).collect(Collectors.toList());

        Main.getDatabaseSQLite().saveVoteQueuesPublic(collect, async, new Callback<Integer>(plugin) {
            @Override
            public void onResult(Integer result) {
                queuedVotesHashMap.values().forEach(qhm -> {
                    for (QueuedVote collectVote : collect) {
                        qhm.removeIf(queuedVote1 -> queuedVote1.equals(collectVote));
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {
                Main.error(throwable);
                super.onError(throwable);
            }
        });
    }
}
