package com.pustinek.humblevote.voteStatistics;

import com.grack.nanojson.JsonWriter;

import java.util.HashMap;
import java.util.UUID;

public class PlayerVoteStats {

    private boolean needsDatabaseSync = false;
    private UUID playerId; // UUID of the player
    private Integer id;
    private String playerLastUsername; // username that the player used lastly
    private Integer totalVoteCount = 0;
    private Integer monthlyVoteCount = 0;
    private HashMap<String, String> voteSiteStatistics = new HashMap<>(); // vote site statistics (vote-site / timestamp )
    private HashMap<Integer, Integer> monthlyVoteStatistics = new HashMap<>();

    public PlayerVoteStats(String jsonString) {
        if(jsonString == null) return;
    }


    public void incrementPlayerTotalVote(Integer incrementValue) {
        final Integer toIncrement = incrementValue != null ? incrementValue : 1;
        totalVoteCount += toIncrement;
        needsDatabaseSync = true;
    }


    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
    public boolean hasId(){
        return (id != null);
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getPlayerLastUsername() {
        return playerLastUsername;
    }

    public Integer getTotalVoteCount() {
        return totalVoteCount;
    }

    public Integer getMonthlyVoteCount() {
        return monthlyVoteCount;
    }

    public boolean isNeedsDatabaseSync() {
        return needsDatabaseSync;
    }

    public HashMap<String, String> getVoteSiteStatistics() {
        return voteSiteStatistics;
    }

    public HashMap<Integer, Integer> getMonthlyVoteStatistics() {
        return monthlyVoteStatistics;
    }

    public String toJson() {
        String json = JsonWriter.string().object()
                .value(totalVoteCount)
                .value(monthlyVoteCount)
                .array("monthlyVoteStatistics")
                .value(monthlyVoteStatistics)
                .end().done();
        return json;
    }



}
