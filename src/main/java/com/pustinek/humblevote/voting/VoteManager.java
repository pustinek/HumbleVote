package com.pustinek.humblevote.voting;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.utils.Callback;
import com.pustinek.humblevote.utils.Manager;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VoteManager extends Manager {

    private final Main plugin;
    private final static Map<String, ArrayList<QueuedVote>> queuedVotesHashMap = new ConcurrentHashMap<>(32, 0.75f, 2); // Username, List<QueuedVote>
    public static Integer cacheCounter = 0;
    public static Integer receivedVotes = 0;

    public static Integer DB_send = 0;
    public static Integer DB_ones = 0;
    public static Integer DB_all = 0;
    public static Integer SAVE_TO_DB_LENGTH = 0;


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




        Main.getRewardManager().checkPlayerRewardEligibility(player);


        return true;
    }
    /**
     * Process all player votes that might be in the Database queued and cache
     *
     * @param player online player
     */
    public void processAllPlayerQueuedVotes(Player player) {
        processPlayerQueuedCacheVotes(player, new Callback<Integer>(plugin) {
            @Override
            public void onResult(Integer result) {
                Main.debug("processed votes from cache = " + result);
                Main.getVoteStatisticsManager().incrementPlayerTotalVotes(player, result);
                super.onResult(result);
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
            }
        });
        processPlayerQueuedDBVotes(player, new Callback<Integer>(plugin) {
            @Override
            public void onResult(Integer result) {
                Main.debug("processed votes from Database = " + result);
                Main.getVoteStatisticsManager().incrementPlayerTotalVotes(player, result);
                super.onResult(result);
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
            }
        });
    }



    



    /**
     * Process all player votes that might be in the Database queued
     *
     * @param player online player
     */
    private void processPlayerQueuedDBVotes(Player player, Callback<Integer> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Main.getDatabaseSQLite().getPlayerQueuedVotes(player.getName(), new Callback<ArrayList<QueuedVote>>(plugin) {
                    @Override
                    public void onResult(ArrayList<QueuedVote> databaseVotes) {

                        ArrayList<QueuedVote> proccessedDatabaseVotes = new ArrayList<>();

                        for (QueuedVote queuedVote: databaseVotes) {
                            boolean success = processPlayerVote(queuedVote,player);
                            if(success) {
                                proccessedDatabaseVotes.add(queuedVote);
                            }
                        }

                        if(proccessedDatabaseVotes.size() > 0) {
                            Main.getDatabaseSQLite().deletePlayerQueuedVotes(player.getName(), proccessedDatabaseVotes, new Callback<Integer>(plugin) {
                                @Override
                                public void onResult(Integer result) {
                                    if(callback != null) callback.callSyncResult(proccessedDatabaseVotes.size());
                                }
                                @Override
                                public void onError(Throwable throwable) {
                                    Main.error(throwable);
                                }
                            });
                        }else{
                            if(callback != null) callback.callSyncResult(0);
                        }
                    }
                });

            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Process all player votes that might be in the Database queued
     *
     * @param player online player
     * @param callback return the number of successful votes that were proceeded
     */
    private void processPlayerQueuedCacheVotes(Player player, Callback<Integer> callback) {
        ArrayList<QueuedVote> cacheVotes = queuedVotesHashMap.get(player.getName());

        if(cacheVotes == null) {
            cacheVotes = new ArrayList<>();
        }
        Integer successfullyProccesedVotes = 0;
        for (Iterator<QueuedVote> it = cacheVotes.iterator(); it.hasNext();) {
            boolean remove = processPlayerVote(it.next(),player);
            if(remove){
                it.remove();
                successfullyProccesedVotes++;
            }

        }
        if(callback != null) callback.callSyncResult(successfullyProccesedVotes);
    }

    /**
     * Process all player votes that might be in the Database queued
     *
     * @param username username of the player that you wish to get all the qued
     */
    @Deprecated
    public void getAllPlayerQueuedVotes(String username) {
        Main.getDatabaseSQLite().getPlayerQueuedVotes(username, new Callback<ArrayList<QueuedVote>>(plugin) {

            @Override
            public void onResult(ArrayList<QueuedVote> databaseVotes) {

                ArrayList<QueuedVote> cacheVotes = queuedVotesHashMap.get(username);

                if(cacheVotes == null) cacheVotes = new ArrayList<>();

                List<QueuedVote> allVotes = Stream.of(cacheVotes, databaseVotes).flatMap(Collection::stream).collect(Collectors.toList());

                Main.debug("There seem to be a total of " + allVotes.size() + "(db: " + databaseVotes.size() + ", cache: " + cacheVotes. size()+ "[received:"+receivedVotes+"|cached:"+cacheCounter+"|db:"+"] for player " + username);
                Main.debug("Proccessed votes in database ->" + DB_send +"/"+ DB_ones + "/" + DB_all);
                Main.debug("saveVotesToDB LENGTH = " + SAVE_TO_DB_LENGTH);


            }
        });
    }

    /**
     * Add queued vote to cache/queue
     *
     * @param vote vote to be added to queue/cache
     */
    public synchronized void addVoteToCache(QueuedVote vote) {
        String username = vote.getUsername();
        queuedVotesHashMap.computeIfAbsent(username, k -> new ArrayList<>()).add(vote);
        cacheCounter++;

    }


    /**
     * Reload votes / connect to database
     */
    public void reloadVotes() {
        // Clean up cache every 3 minutes
        new BukkitRunnable(){

            @Override
            public void run() {
                saveAllQueuedVotesToDatabase(true);
            }
        }.runTaskTimer(plugin, 1200, 1200);

    }

    /**
     * Save user votes from cache to database
     *
     * @param async should the operation to database be async ?
     */
    public void saveAllQueuedVotesToDatabase(boolean async) {

        final Collection<ArrayList<QueuedVote>> values = new ArrayList<>(queuedVotesHashMap.values());
        final List<QueuedVote> collect = values.stream().flatMap(Collection::stream).collect(Collectors.toList());

        SAVE_TO_DB_LENGTH += collect.size();

        Main.getDatabaseSQLite().saveVoteQueuesPublic(collect, async, new Callback<Integer>(plugin) {
            @Override
            public void onResult(Integer result) {
                Main.debug("success saving votes to database");

                queuedVotesHashMap.values().forEach(qhm -> {
                    for (QueuedVote collectVote : collect) {
                        qhm.removeIf(queuedVote1 -> queuedVote1.equals(collectVote));
                    }
                });
                //collect.forEach(e -> queuedVotesHashMap.entrySet().removeIf(entry -> entry.getValue().contains(e)));
            }

            @Override
            public void onError(Throwable throwable) {
                Main.error(throwable);
                super.onError(throwable);
            }
        });
    }


    /**
     * Save user votes from cache to database
     *
     * @param username username of the user that should be save to DB
     */
    @Deprecated
    public void votesFromCacheToDB(String username) {

        ArrayList<QueuedVote> votes = new ArrayList<>(queuedVotesHashMap.get(username));

        if(votes == null || votes.size() == 0) {
            Main.debug("No votes in cache for user " + username );
            return;
        }

        Main.debug("ready to save " + votes.size() + " to database");

        Main.getDatabaseSQLite().saveVoteQueuesPublic(votes, true,  new Callback<Integer>(plugin) {
            @Override
            public void onResult(Integer result) {
                Main.debug("success saving votes to database");
                queuedVotesHashMap.get(username).removeIf(votes::contains);
                super.onResult(result);
            }

            @Override
            public void onError(Throwable throwable) {
                Main.error(throwable);
                super.onError(throwable);
            }
        });
    }
}
