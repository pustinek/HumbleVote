package com.pustinek.humblevote.voteStatistics;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.utils.Callback;
import com.pustinek.humblevote.utils.ChatUtils;
import com.pustinek.humblevote.utils.Manager;
import com.pustinek.humblevote.voteStatistics.constants.TOP_VOTES_STATS_TYPE;
import com.pustinek.humblevote.voting.QueuedVote;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.YearMonth;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PlayerVoteStatisticsManager extends Manager {
    private final Main plugin;
    private static ConcurrentHashMap<UUID,PlayerVoteStats> playerVoteStatsConcurrentHashMap = new ConcurrentHashMap<>(32, 0.75f, 2); // PlayerUUID, PlayerVoteStatistics
    private static ConcurrentHashMap<UUID, ArrayList<PlayerVoteSiteHistory>> playerVoteSiteHistoryConcurrentHashMap = new ConcurrentHashMap<>(32, 0.75f, 2); // PlayerUUID, PlayerVoteSiteHistory
    private static boolean loadedFromDatabase = false;


    public PlayerVoteStatisticsManager(Main plugin) {
        this.plugin = plugin;
    }


    @Override
    public void shutdown() {
        // Shutdown cleanup
        saveAllPlayerVoteStatsToDatabase(false);
        playerVoteStatsConcurrentHashMap.clear();
        playerVoteStatsConcurrentHashMap = null;

        ArrayList<PlayerVoteSiteHistory> playerVoteSiteHistories = new ArrayList<>();
        playerVoteSiteHistoryConcurrentHashMap.values().forEach(playerVoteSiteHistories::addAll);

        Main.getMainDatabase().savePlayerVoteSiteHistory(playerVoteSiteHistories, null);

        //playerVoteSiteHistoryConcurrentHashMap.forEach((x, y) -> savePlayerVoteSiteHistoryToDatabase(x, false));
    }

    /**
     * Get player votes stats by his UUID
     *
     * @param playerId UUID of the player to check
     */
    public PlayerVoteStats getPlayerVoteStats(UUID playerId) {
        return playerVoteStatsConcurrentHashMap.get(playerId);
    }

    /**
     * Get player votes stats by his Username
     *
     * @param username username of the player to get
     */
    public PlayerVoteStats getPlayerVoteStats(String username) {
        return playerVoteStatsConcurrentHashMap.values().stream().filter(playerVoteStats -> playerVoteStats.getPlayerLastUsername().equalsIgnoreCase(username)).findAny().orElse(null);
    }


    public ConcurrentHashMap<UUID, PlayerVoteStats> getPlayerVoteStatsConcurrentHashMap() {
        return playerVoteStatsConcurrentHashMap;
    }

    /**
     * Get all players names that have a voteStat entry
     **/
    public List<String> getPlayerNamesThatHaveVoteStats() {
        return playerVoteStatsConcurrentHashMap.values().stream().map(PlayerVoteStats::getPlayerLastUsername).collect(Collectors.toList());
    }











    /**
     * Get player votes statistics for TOP-x players by vote
     *
     * @param top number of top voting players to get
     * @param statsType type of statistics to get (Monthly | Total)
     * @param yearMonth year-month of the statistics to get
     **/
    public List<PlayerVoteStats> getPlayerVoteStatsByTop(final int top, final TOP_VOTES_STATS_TYPE statsType, final YearMonth yearMonth) {

        ArrayList<PlayerVoteStats> arrayList = new ArrayList<>(playerVoteStatsConcurrentHashMap.values());

        if(statsType.equals(TOP_VOTES_STATS_TYPE.MONTH)){
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
     * Get player votes statistics for TOP-x players by vote for current year-month
     *
     * @param top       number of top voting players to get
     * @param statsType type of statistics to get (Monthly | Total)
     **/
    public List<PlayerVoteStats> getPlayerVoteStatsByTop(int top, TOP_VOTES_STATS_TYPE statsType) {
        return getPlayerVoteStatsByTop(top, statsType, Main.getTimeManager().getYearMonth());
    }



    /**
     * Get the total votes of all the players on the server (total | monthly)
     *
     * @param statsType total | month
     **/
    public int getServerTotalVotes(TOP_VOTES_STATS_TYPE statsType) {
        if(statsType.equals(TOP_VOTES_STATS_TYPE.MONTH)) {
            return playerVoteStatsConcurrentHashMap.values().stream().map(pvs -> pvs.getMonthlyVoteCount(Main.getTimeManager().getYearMonth())).reduce(0, Integer::sum);
        }else if(statsType.equals(TOP_VOTES_STATS_TYPE.TOTAL)) {
            return playerVoteStatsConcurrentHashMap.values().stream().map(PlayerVoteStats::getTotalVoteCount).reduce(0, Integer::sum);
        }
        return 0;
    }


    /**
     * Process player vote for statistics (monthly, total)
     *
     * @param player player object of which to check
     */
    public void processVoteForStatistics(@NotNull QueuedVote vote, @NotNull Player player) {

        // Add time of vote to our voteSite statistics, that will be used
        PlayerVoteStats playerVoteStats = playerVoteStatsConcurrentHashMap.get(player.getUniqueId());

        PlayerVoteSiteHistory playerVoteSiteHistory = new PlayerVoteSiteHistory(-1, vote.getServiceName(), player.getName(), player.getUniqueId(), vote.getLocalTimestamp(), true);
        addVoteSiteHistory(playerVoteSiteHistory);

        // Increment the vote count by one
        playerVoteStats.incrementTotalPlayerVoteCount();

        // Monthly increment
        Instant instant = Instant.ofEpochMilli(Long.parseLong(vote.getTimestamp()));
        YearMonth yearMonth = Main.getTimeManager().InstantToYearMonth(instant);
        playerVoteStats.incrementMonthlyPlayerVoteCount(yearMonth);

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
            PlayerVoteStats playerVoteStats = new PlayerVoteStats(player.getUniqueId(), player.getName(), 0,0, null);
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
                        PlayerVoteStats playerVoteStats = new PlayerVoteStats(player.getUniqueId(), player.getName(), 0,0, null);
                        playerVoteStats.setNeedsDatabaseSync(true);
                        playerVoteStatsConcurrentHashMap.put(player.getUniqueId(), playerVoteStats);
                        savePlayerVoteStatsToDatabase(player.getUniqueId());
                        if(callback != null) callback.callSyncResult(1);
                    }else {
                        // In case there is already an entry in database just use that
                        playerVoteStatsConcurrentHashMap.put(result.getPlayerUUID(), result);
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
     * @param async should the operation be executed async
     */
    public void saveAllPlayerVoteStatsToDatabase(boolean async) {

        ArrayList<PlayerVoteStats> playerVoteStats = new ArrayList<>();

        playerVoteStatsConcurrentHashMap.forEach((key, value) -> {
            if (value.isNeedsDatabaseSync()) playerVoteStats.add(value);
        });

        Main.getMainDatabase().savePlayerStatisticsPublic(playerVoteStats, async, new Callback<Integer>(plugin) {
            @Override
            public void onResult(Integer result) {
                if (result > 0)
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


    /* ====================================
     *           VOTE-SITE HISTORY
     * =====================================*/

    /**
     * add VoteSite history
     *
     * @param playerVoteSiteHistory playerVoteSiteHistory
     */
    private void addVoteSiteHistory(PlayerVoteSiteHistory playerVoteSiteHistory) {

        ArrayList<PlayerVoteSiteHistory> x = playerVoteSiteHistoryConcurrentHashMap.get(playerVoteSiteHistory.getPlayerUUID());
        if (x == null) {
            playerVoteSiteHistoryConcurrentHashMap.computeIfAbsent(playerVoteSiteHistory.getPlayerUUID(), k -> new ArrayList<>()).add(playerVoteSiteHistory);
            return;
        }

        ListIterator<PlayerVoteSiteHistory> iterator = x.listIterator();
        while (iterator.hasNext()) {
            PlayerVoteSiteHistory historyNext = iterator.next();
            if (historyNext.getVoteSite().equalsIgnoreCase(playerVoteSiteHistory.getVoteSite())) {
                int databaseId;
                if (historyNext.hasId()) {
                    databaseId = historyNext.getId();
                    playerVoteSiteHistory.setId(databaseId);
                }
                iterator.set(playerVoteSiteHistory);
                return;
            }
        }
        playerVoteSiteHistoryConcurrentHashMap.computeIfAbsent(playerVoteSiteHistory.getPlayerUUID(), k -> new ArrayList<>()).add(playerVoteSiteHistory);
    }


    /**
     * get player voteSite history object
     *
     * @param playerUUID player UUID
     * @param voteSite   voteSite service name
     */
    @Nullable
    public PlayerVoteSiteHistory getPlayerVoteSiteHistory(UUID playerUUID, String voteSite) {
        if (playerVoteSiteHistoryConcurrentHashMap.get(playerUUID) == null) return null;
        return playerVoteSiteHistoryConcurrentHashMap.get(playerUUID).stream().filter(x -> x.getVoteSite().equalsIgnoreCase(voteSite)).findAny().orElse(null);
    }

    /**
     * Save player vote site history to database
     *
     * @param playerUUID player UUID
     */
    public void savePlayerVoteSiteHistoryToDatabase(UUID playerUUID, boolean async) {
        ArrayList<PlayerVoteSiteHistory> playerVoteSiteHistories = playerVoteSiteHistoryConcurrentHashMap.get(playerUUID);
        if (playerVoteSiteHistories == null) return;
        playerVoteSiteHistories.forEach(playerVoteSiteHistory -> {
            if (playerVoteSiteHistory.isRequireDBSync()) {
                Main.getMainDatabase().savePlayerVoteSiteHistory(playerVoteSiteHistory, async, null);
            }
        });
    }

    /**
     * Load player vote site history from database and save to memory
     *
     * @param playerUUID player UUID
     */
    public void loadPlayerVoteSiteHistoryFromDatabase(UUID playerUUID) {
        Main.getMainDatabase().getPlayerVoteSiteHistories(playerUUID, new Callback<ArrayList<PlayerVoteSiteHistory>>(plugin) {
            @Override
            public void onResult(ArrayList<PlayerVoteSiteHistory> result) {
                result.forEach(x -> addVoteSiteHistory(x));
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
            }
        });
    }


}
