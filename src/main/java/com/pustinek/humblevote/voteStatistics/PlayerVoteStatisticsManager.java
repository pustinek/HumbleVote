package com.pustinek.humblevote.voteStatistics;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.utils.Callback;
import com.pustinek.humblevote.utils.ChatUtils;
import com.pustinek.humblevote.utils.Manager;
import com.pustinek.humblevote.voting.QueuedVote;
import org.bukkit.entity.Player;

import java.time.YearMonth;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerVoteStatisticsManager extends Manager {
    private final Main plugin;
    private static ConcurrentHashMap<UUID,PlayerVoteStats> playerVoteStatsConcurrentHashMap = new ConcurrentHashMap<>(32, 0.75f, 2); // PlayerUUID, PlayerVoteStatistics
    private static boolean loadedFromDatabase = false;
    private YearMonth currentYearMonth;


    public PlayerVoteStatisticsManager(Main plugin) {
        this.plugin = plugin;
        currentYearMonth = Main.getConfigManager().devEnabled ? YearMonth.of(Main.getConfigManager().devYear, Main.getConfigManager().devMonth) : YearMonth.now();
    }


    public YearMonth getCurrentYearMonth() {
        return currentYearMonth;
    }

    /**
     * Get player votes stats by his UUID
     *
     * @param playerId UUID of the player to check
     */
    public PlayerVoteStats getPlayerVoteStats(UUID playerId) {
        return playerVoteStatsConcurrentHashMap.get(playerId);
    }

    public static boolean isLoadedFromDatabase() {
        return loadedFromDatabase;
    }

    /**
     * Get player votes statistics for TOP-x players by vote
     *
     * @param top number of top voting players to get
     **/
    public List<PlayerVoteStats> getPlayerVoteStatsByTop(Integer top, TopVoteStatsType statsType) {

        ArrayList<PlayerVoteStats> arrayList = new ArrayList<>(playerVoteStatsConcurrentHashMap.values());
        final YearMonth yearMonth = Main.getVoteStatisticsManager().getCurrentYearMonth();
        if(statsType.equals(TopVoteStatsType.MONTH)){
            arrayList.sort((Comparator.comparing(date -> date.getMonthlyVoteCount(yearMonth), Collections.reverseOrder())));
        }else {
            arrayList.sort((Comparator.comparing(PlayerVoteStats::getTotalVoteCount, Collections.reverseOrder())));
        }
        arrayList.forEach(PlayerVoteStats::monthlyStatisticsCheck);
        if(arrayList.size() > top)
            return  arrayList.subList(0,top);
        return arrayList;

    }

    /**
     * Get the total votes of all the players on the server (total | monthly)
     *
     * @param statsType total | month
     **/
    public int getServerTotalVotes(TopVoteStatsType statsType) {
        if(statsType.equals(TopVoteStatsType.MONTH)) {
           return playerVoteStatsConcurrentHashMap.values().stream().map(pvs -> pvs.getMonthlyVoteCount(Main.getTimeManager().getYearMonth())).reduce(0, Integer::sum);
        }else if(statsType.equals(TopVoteStatsType.TOTAL)) {
            return playerVoteStatsConcurrentHashMap.values().stream().map(PlayerVoteStats::getTotalVoteCount).reduce(0, Integer::sum);
        }
        return 0;
    }



    /**
     * Process player vote and determine what the appropriate statistics
     *
     * @param player player object of which to check
     */
    public void processVoteForStatistics(QueuedVote vote, Player player) {
        if(vote == null || player == null) {
            Main.warning("failed to process vote for statistics -- player or QueuedVote is null !");
            return;
        }

        // Add time of vote to our voteSite statistics, that will be used
        PlayerVoteStats playerVoteStats = playerVoteStatsConcurrentHashMap.get(player.getUniqueId());
        playerVoteStats.addVoteSiteStatistic(vote.getServiceName(), vote.getTimestamp());

        // Increment the vote count by one
        playerVoteStats.incrementPlayerVote();

    }

    /**
     * Check if player votes statistics exist, else create them
     *
     * @param player player object of which to check
     */
    public void checkCreatePlayerVoteStatistic(final Player player, final Callback<Integer> callback) {
        // 1. Check if entry is in our HashMap/Memory
        if(playerVoteStatsConcurrentHashMap.get(player.getUniqueId()) != null) {
            callback.callSyncResult(1);
            return;
        }
        // 2. Check if database was already checked, but there was no entry for the user
        if(loadedFromDatabase) {
            PlayerVoteStats playerVoteStats = new PlayerVoteStats(player.getUniqueId(), player.getName(), 0, null);
            playerVoteStats.setNeedsDatabaseSync(true);
            playerVoteStatsConcurrentHashMap.put(player.getUniqueId(), playerVoteStats);
            savePlayerVoteStatsToDatabase(player.getUniqueId());
            if(callback != null) callback.callSyncResult(1);
        }else {
            Main.getMainDatabase().getPlayerVoteStatistics(player.getUniqueId(), new Callback<PlayerVoteStats>(plugin) {
                @Override
                public void onResult(PlayerVoteStats result) {
                    if(result == null) {
                        // In case there is no entry in database, create new vote-stats object and save it to database
                        Main.debug("There was to player vote stats entry found in DB for user "+ player.getName() +" - creating a new one");
                        PlayerVoteStats playerVoteStats = new PlayerVoteStats(player.getUniqueId(), player.getName(), 0, null);
                        playerVoteStats.setNeedsDatabaseSync(true);
                        playerVoteStatsConcurrentHashMap.put(player.getUniqueId(), playerVoteStats);
                        savePlayerVoteStatsToDatabase(player.getUniqueId());
                        if(callback != null) callback.callSyncResult(1);
                    }else {
                        // In case there is already an entry in database just use that
                        playerVoteStatsConcurrentHashMap.put(result.getPlayerId(), result);
                        if(callback != null) callback.callSyncResult(1);
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    if(callback != null) callback.callSyncError(throwable);
                    super.onError(throwable);
                }
            });
        }

    }


    /**
     * Save all player vote statistics to database
     *
     * @param async should the operation be executed async ?
     */
    public void saveAllPlayerVoteStatsToDatabase(boolean async) {

        ArrayList<PlayerVoteStats> playerVoteStats = new ArrayList<>();

       playerVoteStatsConcurrentHashMap.forEach((key, value) -> {
           if(value.isNeedsDatabaseSync()) playerVoteStats.add(value);
       });

       Main.getMainDatabase().savePlayerStatisticsPublic(playerVoteStats,async, new Callback<Integer>(plugin) {
           @Override
           public void onResult(Integer result) {
               if(result > 0)
                    Main.debug(ChatUtils.chatColor("&2Successfully saved " + result + " player vote statistics to Database "));
               super.onResult(result);
           }

           @Override
           public void onError(Throwable throwable) {
               Main.error(throwable);
               super.onError(throwable);
           }
       });
    }

    /**
     * Save Player votes to database - usually called when user disconnects
     *
     * @param playerId UUID of the player
     */
    public void savePlayerVoteStatsToDatabase(UUID playerId) {

        PlayerVoteStats playerVoteStats = playerVoteStatsConcurrentHashMap.get(playerId);
        if(playerVoteStats == null) {
            Main.warning("failed to get player " + playerId + " vote statistics to SAVE");
            return;
        }

        if(!playerVoteStats.isNeedsDatabaseSync()) { return; }

        Main.getMainDatabase().savePlayerStatistics(playerVoteStats, null);
    }

    /**
     * Load all player vote statistics from database and save them to HashMap
     */
    public void loadAllPlayerVoteStatisticsFromDatabase() {

        Main.getMainDatabase().getAllPlayerStatistics(new Callback<HashMap<UUID, PlayerVoteStats>>(plugin) {
            @Override
            public void onResult(HashMap<UUID, PlayerVoteStats> result) {
                playerVoteStatsConcurrentHashMap.putAll(result);
                Main.debug(ChatUtils.chatColor("&2Successfully loaded " + result.size() + " player vote statistics from Database "));
                loadedFromDatabase = true;
            }

            @Override
            public void onError(Throwable throwable) {
                Main.error(throwable);
                super.onError(throwable);
            }
        });
    }

}
