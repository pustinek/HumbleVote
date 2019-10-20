package com.pustinek.humblevote.voteStatistics;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.utils.Callback;
import com.pustinek.humblevote.utils.Manager;
import com.pustinek.humblevote.voteSites.VoteSite;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class VoteStatisticsManager extends Manager {
    private final Main plugin;
    // ArrayList of all the player statistics.
    public static HashMap<UUID,PlayerVoteStats> allPlayerVoteStats = new HashMap<>();

    public VoteStatisticsManager(Main plugin) {
        this.plugin = plugin;
    }


    public PlayerVoteStats getPlayerVoteStats(UUID playerId) {
        return allPlayerVoteStats.get(playerId);
    }

    /**
     * Check if player votes statistics exist, else create them
     * @param player player object of which to check
     */
    public void checkCreatePlayerVoteStatistic(Player player) {
        if(allPlayerVoteStats.get(player.getUniqueId()) != null) return;

        PlayerVoteStats playerVoteStats = new PlayerVoteStats("");
        allPlayerVoteStats.put(player.getUniqueId(), playerVoteStats);

    }

    /**
     * Save Player votes to database - usually called when user disconnects
     * @param playerId UUID of the player
     */
    public void savePlayerVoteStatsToDatabase(UUID playerId) {

        PlayerVoteStats playerVoteStats = allPlayerVoteStats.get(playerId);
        if(playerVoteStats == null) {
            Main.warrning("failed to get player " + playerId + "vote stats to SAVE");
            return;
        }

        if(!playerVoteStats.isNeedsDatabaseSync()) { return; }

        Main.getMainDatabase().savePlayerStatistics(playerVoteStats, new Callback<Integer>(plugin) {
            @Override
            public void onResult(Integer result) {
                super.onResult(result);
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
            }
        });
    }

    public void loadAllPlayerVoteStatisticsFromDatabase() {
        Main.getMainDatabase().getAllPlayerStatistics(new Callback<HashMap<UUID, PlayerVoteStats>>(plugin) {
            @Override
            public void onResult(HashMap<UUID, PlayerVoteStats> result) {
                allPlayerVoteStats = result;
                Main.debug("Successfully loaded " + result.size() + " player vote statistics from Database ");
            }

            @Override
            public void onError(Throwable throwable) {
                Main.error(throwable);
                super.onError(throwable);
            }
        });
    }

    /**
     * Process single player vote
     * @param player the player that should have it's votes incremented
     * @param toIncrement Value that should be incremented by (null -> increment by 1)
     */
    public void incrementPlayerTotalVotes(Player player, Integer toIncrement) {
        allPlayerVoteStats.get(player.getUniqueId()).incrementPlayerTotalVote(toIncrement);
    }

    public void getPlayerVoteSiteLastTimestampOfVote(Player player, VoteSite votesite) {


    }





}
