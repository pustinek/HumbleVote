package com.pustinek.humblevote.votingRewards;

import java.time.Instant;
import java.util.UUID;

public class PlayerRewardRecord {
    private Integer id;
    private UUID playerID;
    private String playerName;
    private Instant timestamp;
    private String rewardID;


    public PlayerRewardRecord(UUID playerID, String playerName, Instant timestamp, String rewardID) {
        this.playerID = playerID;
        this.playerName = playerName;
        this.timestamp = timestamp;
        this.rewardID = rewardID;
    }

    public int getId() {
        return id;
    }
    public boolean hasID() {
        return id != null;
    }

    public UUID getPlayerID() {
        return playerID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Instant getTimestamp() {
        return timestamp;
    }


    public String getRewardID() {
        return rewardID;
    }

    public void setId(int id) {
        this.id = id;
    }
}
