package com.pustinek.humblevote.sql;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.utils.Callback;
import com.pustinek.humblevote.voteStatistics.PlayerVoteStats;
import com.pustinek.humblevote.voting.QueuedVote;
import com.pustinek.humblevote.voting.VoteManager;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.*;

public abstract class Database {
    Main plugin;
    HikariDataSource dataSource;

    final String tableVoteStatistics = "voteStatistics";
    final String tableQueuedVotes = "queuedVotes";


    abstract HikariDataSource getDataSource();

    abstract String getQueryCreateTableVoteStatistics();

    abstract String getQueryCreateTableQueuedVotes();


    protected Database(Main plugin) {
        this.plugin = plugin;
    }




    public void connect(final Callback<Integer> callback) {

        Main.debug(tableQueuedVotes);
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


                    try (Statement s = con.createStatement()) {
                        ResultSet rs = s.executeQuery("SELECT COUNT(id) FROM " + tableQueuedVotes);
                        if (rs.next()) {
                            int count = rs.getInt(1);

                            Main.debug("Initialized database with " + count + " queued votes");

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
                    Main.debug("Failed to initialize or connect to database");
                    Main.error(e);
                }
            }
        }.runTaskAsynchronously(plugin);
    }



    public void saveVoteQueuesPublic(final List<QueuedVote> playerQueuedVotes, final boolean async, final Callback<Integer> callback ) {
        Main.debug("saving votes to database (async = "+async+")");
        if(async) {
            new BukkitRunnable(){
                @Override
                public void run() {
                    saveVoteQueues(playerQueuedVotes,callback);
                }
            }.runTaskAsynchronously(plugin);
        }else {
            saveVoteQueues(playerQueuedVotes, null);
        }
    }

    private void saveVoteQueues(final List<QueuedVote> playerQueuedVotes, final Callback<Integer> callback ) {
                final String query = "INSERT INTO " + tableQueuedVotes + "(address,serviceName, username, timestamp) VALUES (?,?,?,?)";
                 Main.debug("(DB)Saving vote quest to Database !!");

                 VoteManager.DB_send += playerQueuedVotes.size();


                try (Connection con = dataSource.getConnection();
                     PreparedStatement ps = con.prepareStatement(query)
                ) {
                    int i = 0;
                    for (QueuedVote queuedVote : playerQueuedVotes) {
                        ps.setString(i+1, queuedVote.getAddress());
                        ps.setString(i+2, queuedVote.getServiceName());
                        ps.setString(i+3, queuedVote.getUsername());
                        ps.setString(i+4, queuedVote.getTimestamp());
                        ps.addBatch();
                    }
                    int[] x = ps.executeBatch();

                    Main.debug("executed update to database -> " + Arrays.toString(x));

                    for (int value : x) {
                        if (value == 1) VoteManager.DB_ones++;
                        VoteManager.DB_all++;
                    }

                    if(callback != null) {
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
        final String query = "SELECT * FROM " + tableQueuedVotes + " WHERE username = ?";

        Main.debug("getPlayerQuedVotes from DB");

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection con = dataSource.getConnection();
                     PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, username);

                    ResultSet rs = ps.executeQuery();

                    ArrayList<QueuedVote> queuedVotes = new ArrayList<>();

                    while(rs.next()) {
                        String username = rs.getString("username");
                        String address = rs.getString("address");
                        String serviceName = rs.getString("serviceName");
                        String timestamp = rs.getString("timestamp");

                        QueuedVote queuedVote = new QueuedVote(address,serviceName, username, timestamp, false);

                        ResultSet keyRs = ps.getGeneratedKeys();

                        int id = -1;
                        if (keyRs.next()) {
                            id = rs.getInt(1);
                        }

                        queuedVote.setDatabaseID(id);
                        queuedVotes.add(queuedVote);
                    }

                    if(callback != null) {
                        callback.callSyncResult(queuedVotes);
                    }
                } catch (SQLException ex) {
                    if(callback != null) {
                        callback.callSyncError(ex);
                    }
                    Main.error("Failed to get player queued voters database");
                    Main.error(ex);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public void deletePlayerQueuedVotes(String userName, ArrayList<QueuedVote> playerQueuedVotes, Callback<Integer> callback) {
        final String query = "DELETE FROM " + tableQueuedVotes + " WHERE id = ? AND username = ?";

        new BukkitRunnable() {

            @Override
            public void run() {
                try (Connection con = dataSource.getConnection();
                     PreparedStatement ps = con.prepareStatement(query)) {

                    int i = 0;

                    for (QueuedVote queuedVote : playerQueuedVotes) {
                        ps.setInt(i+1, queuedVote.getDatabaseID());
                        ps.setString(i+2, queuedVote.getUsername());
                        ps.addBatch();
                    }
                    ps.executeBatch();


                    if(callback != null) {
                        callback.callSyncResult(1);
                    }

                } catch (SQLException ex) {
                    if(callback != null) {
                        callback.callSyncError(ex);
                    }
                    Main.error("Failed to get player queued voters database");
                    Main.error(ex);
                }


            }
        }.runTaskAsynchronously(plugin);
    }

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
                       String playerId = rs.getString("playerId");
                       String lastUsername = rs.getString("lastUsername");
                       Integer totalVotes = rs.getInt("total");
                       String statsJSON = rs.getString("statistics");

                        PlayerVoteStats playerVoteStats = new PlayerVoteStats(statsJSON);
                        playerVoteStatsHashMap.put(UUID.fromString(playerId), playerVoteStats);
                   }

                   if(callback != null) {
                       callback.callSyncResult(playerVoteStatsHashMap);
                   }

               }catch (SQLException ex) {
                   if(callback != null) {
                       callback.callSyncError(ex);
                   }

                   Main.error(ex);
               }
           }
       }.runTaskAsynchronously(plugin);

   }

   public void savePlayerStatistics(PlayerVoteStats playerVoteStats, Callback<Integer> callback) {

       final String query = "REPLACE INTO " + tableVoteStatistics + "(id, playerId, lastUsername, total, statistics) VALUES (?,?,?,?,?)";

       final String queryNoId = "REPLACE INTO " + tableVoteStatistics + "(playerId, lastUsername, total, statistics) VALUES (?,?,?,?)";
       final String queryWithId = "REPLACE INTO " + tableVoteStatistics + "(id, playerId, lastUsername, total, statistics) VALUES (?,?,?,?,?)";

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

                   ps.setString(i+ 1, playerVoteStats.getPlayerId().toString());
                   ps.setString(i+ 2, playerVoteStats.getPlayerLastUsername());
                   ps.setInt(i+ 3, playerVoteStats.getTotalVoteCount());
                   ps.setString(i+ 4, playerVoteStats.toJson());

                   ps.executeUpdate();

                   if(!playerVoteStats.hasId()) {
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
               }catch (SQLException ex) {
                   if (callback != null) {
                       callback.callSyncError(ex);
                   }
                   Main.error(ex);
               }


           }
       }.runTaskAsynchronously(plugin);





   }





    public void disconnect() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }

}
