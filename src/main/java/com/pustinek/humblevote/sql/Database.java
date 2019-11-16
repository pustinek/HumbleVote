package com.pustinek.humblevote.sql;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.utils.Callback;
import com.pustinek.humblevote.voteStatistics.PlayerVoteSiteHistory;
import com.pustinek.humblevote.voteStatistics.PlayerVoteStats;
import com.pustinek.humblevote.voting.QueuedVote;
import com.pustinek.humblevote.votingRewards.PlayerRewardRecord;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.time.Instant;
import java.util.*;

public abstract class Database {
    String tableVoteStatistics;
    String tableQueuedVotes;
    String tableRewards;
    String tableVoteSiteHistory;
    Main plugin;
    HikariDataSource dataSource;


    Database(Main plugin) {
        this.plugin = plugin;
    }

    abstract HikariDataSource getDataSource();

    abstract String getQueryCreateTableVoteStatistics();

    abstract String getQueryCreateTableQueuedVotes();

    abstract String getQueryCreateTableRewards();

    abstract String getTableCreateTableVoteSiteHistory();

    public void connect(final Callback<Integer> callback) {

        this.tableRewards = Main.getConfigManager().getDatabaseConfig().getTablePrefix() + "rewards";
        this.tableQueuedVotes = Main.getConfigManager().getDatabaseConfig().getTablePrefix() + "queued_votes";
        this.tableVoteStatistics = Main.getConfigManager().getDatabaseConfig().getTablePrefix() + "statistics";
        this.tableVoteSiteHistory = Main.getConfigManager().getDatabaseConfig().getTablePrefix() + "vote_sites";

        new BukkitRunnable() {

            @Override
            public void run() {
                disconnect();

                try {
                    dataSource = getDataSource();
                } catch (Exception e) {
                    callback.onError(e);
                    Main.error(e);
                    return;
                }

                if (dataSource == null) {
                    Exception e = new IllegalStateException("Data source is null");
                    callback.onError(e);
                    Main.error(e);
                    return;
                }

                try (Connection con = dataSource.getConnection()) {
                    //Create queued-votes table
                    try (Statement s = con.createStatement()) {
                        s.executeUpdate(getQueryCreateTableQueuedVotes());
                    }
                    //Create votes-statistics table
                    try (Statement s = con.createStatement()) {
                        s.executeUpdate(getQueryCreateTableVoteStatistics());
                    }

                    //Create rewards-record table
                    try (Statement s = con.createStatement()) {
                        s.executeUpdate(getQueryCreateTableRewards());
                    }

                    //Create vote-site-history table
                    try (Statement s = con.createStatement()) {
                        s.executeUpdate(getTableCreateTableVoteSiteHistory());
                    }

                    try (Statement s = con.createStatement()) {
                        ResultSet rs = s.executeQuery("SELECT COUNT(id) FROM " + tableQueuedVotes);
                        if (rs.next()) {
                            int count = rs.getInt(1);

                            if (callback != null) {
                                callback.callSyncResult(count);
                            }
                        } else {
                            throw new SQLException("Count result has no entries");

                        }
                    }
                } catch (SQLException e) {
                    if (callback != null) {
                        callback.callSyncError(e);
                    }

                    Main.error("Failed to initialize or connect to database");
                    Main.error(e);
                }
            }
        }.runTaskAsynchronously(plugin);
    }



    /*===================================
     *          QUEUED VOTING
     *===================================*/

    public void saveVoteQueuesPublic(final List<QueuedVote> playerQueuedVotes, final boolean async, final Callback<Integer> callback) {
        if (async) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    saveVoteQueues(playerQueuedVotes, callback);
                }
            }.runTaskAsynchronously(plugin);
        } else {
            saveVoteQueues(playerQueuedVotes, null);
        }
    }

    public void savePlayerStatisticsPublic(final ArrayList<PlayerVoteStats> playerVoteStats, final boolean async, final Callback<Integer> callback) {
        if (async) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    savePlayerStatistics(playerVoteStats, callback);
                }
            }.runTaskAsynchronously(plugin);
        } else {
            savePlayerStatistics(playerVoteStats, null);
        }
    }

    private void saveVoteQueues(final List<QueuedVote> playerQueuedVotes, final Callback<Integer> callback) {
        final String query = "INSERT INTO " + tableQueuedVotes + "(address,service_name, player_username, local_timestamp, timestamp) VALUES (?,?,?,?,?)";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(query)
        ) {
            int i = 0;
            for (QueuedVote queuedVote : playerQueuedVotes) {
                ps.setString(i + 1, queuedVote.getAddress());
                ps.setString(i + 2, queuedVote.getServiceName());
                ps.setString(i + 3, queuedVote.getUsername());
                ps.setString(i + 4, queuedVote.getLocalTimestamp());
                ps.setString(i + 5, queuedVote.getTimestamp());
                ps.addBatch();
            }
            int[] x = ps.executeBatch();

            if (callback != null) {
                callback.callSyncResult(1);
            }

        } catch (SQLException ex) {
            if (callback != null) {
                callback.callSyncError(ex);
            }

            Main.error("Failed to add ques to database");
            Main.error(ex);
        }
    }

    public void getPlayerQueuedVotes(String username, Callback<ArrayList<QueuedVote>> callback) {
        final String query = "SELECT * FROM " + tableQueuedVotes + " WHERE player_username = ?";

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection con = dataSource.getConnection();
                     PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, username);

                    ResultSet rs = ps.executeQuery();

                    ArrayList<QueuedVote> queuedVotes = new ArrayList<>();

                    while (rs.next()) {
                        String username = rs.getString("player_username");
                        String address = rs.getString("address");
                        String serviceName = rs.getString("service_name");
                        String localTimestamp = rs.getString("local_timestamp");
                        String timestamp = rs.getString("timestamp");

                        QueuedVote queuedVote = new QueuedVote(address, serviceName, username, timestamp, localTimestamp, false);

                        ResultSet keyRs = ps.getGeneratedKeys();

                        int id = -1;
                        if (keyRs.next()) {
                            id = rs.getInt(1);
                        }

                        queuedVote.setDatabaseID(id);
                        queuedVotes.add(queuedVote);
                    }

                    if (callback != null) {
                        callback.callSyncResult(queuedVotes);
                    }
                } catch (SQLException ex) {
                    if (callback != null) {
                        callback.callSyncError(ex);
                    }
                    Main.error("Failed to get player queued voters database");
                    Main.error(ex);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public void deletePlayerQueuedVotes(String userName, ArrayList<QueuedVote> playerQueuedVotes, Callback<Integer> callback) {
        final String query = "DELETE FROM " + tableQueuedVotes + " WHERE id = ? AND player_username = ?";

        new BukkitRunnable() {

            @Override
            public void run() {
                try (Connection con = dataSource.getConnection();
                     PreparedStatement ps = con.prepareStatement(query)) {

                    int i = 0;

                    for (QueuedVote queuedVote : playerQueuedVotes) {
                        ps.setInt(i + 1, queuedVote.getDatabaseID());
                        ps.setString(i + 2, queuedVote.getUsername());
                        ps.addBatch();
                    }
                    ps.executeBatch();


                    if (callback != null) {
                        callback.callSyncResult(1);
                    }

                } catch (SQLException ex) {
                    if (callback != null) {
                        callback.callSyncError(ex);
                    }
                    Main.error("Failed to get player queued voters database");
                    Main.error(ex);
                }


            }
        }.runTaskAsynchronously(plugin);
    }

    /*===================================
     *          PLAYER STATISTICS
     *===================================*/

    public void getAllPlayerStatistics(final Callback<HashMap<UUID, PlayerVoteStats>> callback) {
        final String query = "SELECT * FROM " + tableVoteStatistics + "";

        new BukkitRunnable() {

            @Override
            public void run() {
                try (Connection con = dataSource.getConnection();
                     PreparedStatement ps = con.prepareStatement(query)) {

                    ResultSet rs = ps.executeQuery();

                    HashMap<UUID, PlayerVoteStats> playerVoteStatsHashMap = new HashMap<>();

                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String playerId = rs.getString("player_uuid");
                        String lastUsername = rs.getString("player_username");
                        int totalVotes = rs.getInt("total");
                        int points = rs.getInt("points");
                        String statsJSON = rs.getString("monthly_stats");

                        PlayerVoteStats playerVoteStats = new PlayerVoteStats(UUID.fromString(playerId), lastUsername, totalVotes,points, statsJSON);
                        playerVoteStats.setId(id);
                        playerVoteStatsHashMap.put(UUID.fromString(playerId), playerVoteStats);
                    }

                    if (callback != null) {
                        callback.callSyncResult(playerVoteStatsHashMap);
                    }

                } catch (SQLException ex) {
                    if (callback != null) {
                        callback.callSyncError(ex);
                    }

                    Main.error(ex);
                }
            }
        }.runTaskAsynchronously(plugin);

    }

    public void getPlayerVoteStatistics(final UUID playerID, Callback<PlayerVoteStats> callback) {
        final String query = "SELECT * FROM " + tableVoteStatistics + " WHERE player_uuid = ?";


        new BukkitRunnable() {

            @Override
            public void run() {
                try (Connection con = dataSource.getConnection();
                     PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, playerID.toString());

                    ResultSet rs = ps.executeQuery();


                    PlayerVoteStats playerVoteStats = null;

                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String playerId = rs.getString("player_uuid");
                        String lastUsername = rs.getString("player_username");
                        int totalVotes = rs.getInt("total");
                        int points = rs.getInt("points");
                        String statsJSON = rs.getString("monthly_stats");

                        playerVoteStats = new PlayerVoteStats(UUID.fromString(playerId), lastUsername, totalVotes,points, statsJSON);
                        playerVoteStats.setId(id);
                    }

                    if (callback != null) {
                        callback.callSyncResult(playerVoteStats);
                    }

                } catch (SQLException ex) {
                    if (callback != null) {
                        callback.callSyncError(ex);
                    }
                    Main.error(ex);
                }
            }
        }.runTaskAsynchronously(plugin);

    }

    private void savePlayerStatistics(ArrayList<PlayerVoteStats> playerVoteStatsArrayList, Callback<Integer> callback) {
        final String queryNoId = "REPLACE INTO " + tableVoteStatistics + "(player_uuid, player_username, total,points, monthly_stats) VALUES (?,?,?,?,?)";
        final String queryWithId = "REPLACE INTO " + tableVoteStatistics + "(id, player_uuid, player_username, total,points, monthly_stats) VALUES (?,?,?,?,?,?)";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(queryWithId)) {


            int i = 1;

            for (PlayerVoteStats playerVoteStats : playerVoteStatsArrayList) {
                ps.setInt(1, (playerVoteStats.getId() != null ? playerVoteStats.getId() : -1));
                ps.setString(i + 1, playerVoteStats.getPlayerUUID().toString());
                ps.setString(i + 2, playerVoteStats.getPlayerLastUsername());
                ps.setInt(i + 3, playerVoteStats.getTotalVoteCount());
                ps.setInt(i + 4, playerVoteStats.getVotingPoints());
                ps.setString(i + 5, playerVoteStats.getMonthlyStatisticsAsJSON());
                ps.addBatch();
            }

            ps.executeBatch();


            if (callback != null) {
                callback.callSyncResult(0);
            }
        } catch (SQLException ex) {
            if (callback != null) {
                callback.callSyncError(ex);
            }
            Main.error(ex);
        }
    }

    public void savePlayerStatistics(final PlayerVoteStats playerVoteStats, final Callback<Integer> callback) {

        final String queryNoId = "REPLACE INTO " + tableVoteStatistics + "(player_uuid, player_username, total, points, monthly_stats) VALUES (?,?,?,?,?)";
        final String queryWithId = "REPLACE INTO " + tableVoteStatistics + "(id, player_uuid, player_username, total, points, monthly_stats) VALUES (?,?,?,?,?,?)";

        new BukkitRunnable() {
            @Override
            public void run() {
                String query = playerVoteStats.hasId() ? queryWithId : queryNoId;

                try (Connection con = dataSource.getConnection();
                     PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

                    int i = 0;
                    if (playerVoteStats.hasId()) {
                        i = 1;
                        ps.setInt(1, playerVoteStats.getId());
                    }

                    ps.setString(i + 1, playerVoteStats.getPlayerUUID().toString());
                    ps.setString(i + 2, playerVoteStats.getPlayerLastUsername());
                    ps.setInt(i + 3, playerVoteStats.getTotalVoteCount());
                    ps.setInt(i + 4, playerVoteStats.getVotingPoints());
                    ps.setString(i + 5, playerVoteStats.getMonthlyStatisticsAsJSON());

                    ps.executeUpdate();

                    if (!playerVoteStats.hasId()) {
                        int statId = -1;
                        ResultSet rs = ps.getGeneratedKeys();
                        if (rs.next()) {
                            statId = rs.getInt(1);
                        }
                        playerVoteStats.setId(statId);
                    }


                    if (callback != null) {
                        callback.callSyncResult(playerVoteStats.getId());
                    }
                } catch (SQLException ex) {
                    if (callback != null) {
                        callback.callSyncError(ex);
                    }
                    Main.error(ex);
                }
            }
        }.runTaskAsynchronously(plugin);

    }

    /*===================================
     *          VOTING REWARDS
     *===================================*/

    /**
     * Get rewards history of player
     *
     * @param playerID UUID of the player
     */
    public void getPlayerRewardRecords(UUID playerID, Callback<ArrayList<PlayerRewardRecord>> callback) {
        final String query = "SELECT * FROM " + tableRewards + " WHERE player_uuid = ?";

        new BukkitRunnable() {

            @Override
            public void run() {
                try (Connection con = dataSource.getConnection();
                     PreparedStatement ps = con.prepareStatement(query)) {

                    ps.setString(1, playerID.toString());

                    ResultSet rs = ps.executeQuery();

                    ArrayList<PlayerRewardRecord> playerRewardRecords = new ArrayList<>();


                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String playerName = rs.getString("player_username");
                        String rewardID = rs.getString("reward_id");
                        Instant timestamp = Instant.parse(rs.getString("timestamp"));

                        PlayerRewardRecord playerRewardRecord = new PlayerRewardRecord(playerID, playerName, timestamp, rewardID);
                        playerRewardRecord.setId(id);
                        playerRewardRecords.add(playerRewardRecord);
                    }

                    if(callback != null) {
                        callback.callSyncResult(playerRewardRecords);
                    }

                } catch (SQLException ex) {
                    if (callback != null) {
                        callback.callSyncError(ex);
                    }
                    Main.error(ex);
                }

            }
        }.runTaskAsynchronously(plugin);
    }


    /**
     * Save reward to Database
     *
     * @param rewardRecord Object representing the reward received by the player
     */
    public void savePlayerVotingReward(PlayerRewardRecord rewardRecord) {
        final String queryNoId = "REPLACE INTO " + tableRewards + "(player_uuid, player_username, reward_id, timestamp) VALUES (?,?,?,?)";
        final String queryWithId = "REPLACE INTO " + tableRewards + "(id, player_uuid, player_username, reward_id, timestamp) VALUES (?,?,?,?,?)";

        new BukkitRunnable(){

            @Override
            public void run() {
                try (Connection con = dataSource.getConnection();
                     PreparedStatement ps = con.prepareStatement(rewardRecord.hasID() ? queryWithId : queryNoId)) {

                    int i = 0;
                    if (rewardRecord.hasID()) {
                        i = 1;
                        ps.setInt(1, rewardRecord.getId());
                    }

                    ps.setString(i+ 1, rewardRecord.getPlayerID().toString());
                    ps.setString(i + 2, rewardRecord.getPlayerName());
                    ps.setString(i + 3, rewardRecord.getRewardID());
                    ps.setString(i + 4, rewardRecord.getTimestamp().toString());

                    ps.executeUpdate();

                    if (!rewardRecord.hasID()) {
                        int dbID = -1;
                        ResultSet rs = ps.getGeneratedKeys();
                        if (rs.next()) {
                            dbID = rs.getInt(1);
                        }
                        rewardRecord.setId(dbID);
                    }

                }catch (SQLException ex) {
                    Main.error(ex);
                }
            }
        }.runTaskAsynchronously(plugin);
    }



    /*===================================
     *          VOTE_SITE_HISTORY
     *===================================*/

    public void savePlayerVoteSiteHistory(PlayerVoteSiteHistory history, boolean async, Callback<Integer> callback) {
        if (async) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    savePlayerVoteSiteHistory(history, callback);
                }
            }.runTaskAsynchronously(plugin);
        } else {
            savePlayerVoteSiteHistory(history, callback);
        }
    }

    private void savePlayerVoteSiteHistory(PlayerVoteSiteHistory history, Callback<Integer> callback) {

        final String queryWithId = "UPDATE " + tableVoteSiteHistory + " SET timestamp=? WHERE id=?";
        final String queryNoId = "INSERT INTO " + tableVoteSiteHistory + " (player_uuid, player_username, vote_site, timestamp) VALUES (?,?,?,?)";

                try (Connection con = dataSource.getConnection();
                     PreparedStatement ps = con.prepareStatement(history.hasId() ? queryWithId : queryNoId, Statement.RETURN_GENERATED_KEYS)) {

                    int i = 0;
                    if (history.hasId()) {
                        ps.setString(i + 1, history.getTimestamp());
                        ps.setInt(i + 2, history.getId());
                    } else {
                        ps.setString(i + 1, history.getPlayerUUID().toString());
                        ps.setString(i + 2, history.getPlayerName());
                        ps.setString(i + 3, history.getVoteSite());
                        ps.setString(i + 4, history.getTimestamp());
                    }

                    ps.executeUpdate();

                    if (!history.hasId()) {
                        int dbID = -1;
                        ResultSet rs = ps.getGeneratedKeys();
                        if (rs.next()) {
                            dbID = rs.getInt(1);
                        }
                        history.setId(dbID);
                    }

                    if (callback != null) {
                        callback.callSyncResult(1);
                    }
                } catch (SQLException ex) {
                    Main.error(ex);
                    if (callback != null) {
                        callback.callSyncError(ex);
                    }
                }
    }


    public void savePlayerVoteSiteHistory(ArrayList<PlayerVoteSiteHistory> histories, Callback<int[]> callback) {

        Main.debug("savePlayerVoteSiteHistory called with array size -> :::" + histories.size());
        final String updateQuery = "UPDATE " + tableVoteSiteHistory + " SET timestamp=? WHERE id=?";
        final String insertQuery = "INSERT INTO " + tableVoteSiteHistory + " (player_uuid, player_username, vote_site, timestamp) VALUES (?,?,?,?)";

        try (Connection con = dataSource.getConnection();
             PreparedStatement updatePs = con.prepareStatement(updateQuery, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement insertPs = con.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)
        ) {


            for (PlayerVoteSiteHistory history : histories) {
                if (history.hasId()) {
                    updatePs.setString(1, history.getTimestamp());
                    updatePs.setInt(2, history.getId());
                    updatePs.addBatch();

                } else {
                    insertPs.setString(1, history.getPlayerUUID().toString());
                    insertPs.setString(2, history.getPlayerName());
                    insertPs.setString(3, history.getVoteSite());
                    insertPs.setString(4, history.getTimestamp());
                    insertPs.addBatch();
                }
            }

            updatePs.executeBatch();
            int[] x = insertPs.executeBatch();
            Main.debug("Insert database results :::" + Arrays.toString(x));
            if (callback != null) {
                callback.callSyncResult(x);
            }

        } catch (SQLException ex) {
            Main.error(ex);
            if (callback != null) {
                callback.callSyncError(ex);
            }
        }


    }






    /**
     * Get player vote site history entries
     *
     * @param playerID UUID of the player
     */
    public void getPlayerVoteSiteHistories(UUID playerID, Callback<ArrayList<PlayerVoteSiteHistory>> callback) {
        final String query = "SELECT * FROM " + tableVoteSiteHistory + " WHERE player_uuid = ?";

        new BukkitRunnable() {

            @Override
            public void run() {
                try (Connection con = dataSource.getConnection();
                     PreparedStatement ps = con.prepareStatement(query)) {

                    ps.setString(1, playerID.toString());

                    ResultSet rs = ps.executeQuery();

                    ArrayList<PlayerVoteSiteHistory> playerVoteSiteHistories = new ArrayList<>();

                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String playerName = rs.getString("player_username");
                        String voteSite = rs.getString("vote_site");
                        String timestamp = rs.getString("timestamp");

                        PlayerVoteSiteHistory playerVoteSiteHistory = new PlayerVoteSiteHistory(id, voteSite, playerName, playerID, timestamp, false);
                        playerVoteSiteHistory.setId(id);
                        playerVoteSiteHistories.add(playerVoteSiteHistory);
                    }

                    if (callback != null) {
                        callback.callSyncResult(playerVoteSiteHistories);
                    }

                } catch (SQLException ex) {
                    if (callback != null) {
                        callback.callSyncError(ex);
                    }
                    Main.error(ex);
                }

            }
        }.runTaskAsynchronously(plugin);
    }


    private void disconnect() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }


}
