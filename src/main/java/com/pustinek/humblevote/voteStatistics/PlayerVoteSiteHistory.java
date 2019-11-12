package com.pustinek.humblevote.voteStatistics;

import java.util.UUID;

/**
 * == PlayerVoteSiteHistory ==
 * Object represents the database entry for playerVoteSite history
 */

public class PlayerVoteSiteHistory {
    private int id;
    private boolean requireDBSync;
    private String voteSite;
    private String playerName;
    private UUID playerUUID;
    private String timestamp;


    public PlayerVoteSiteHistory(int id, String voteSite, String playerName, UUID playerUUID, String timestamp, boolean requireDBSync) {
        this.id = id;
        this.voteSite = voteSite;
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.timestamp = timestamp;
        this.requireDBSync = requireDBSync;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVoteSite() {
        return voteSite;
    }

    public String getPlayerName() {
        return playerName;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getTimestamp() {
        return timestamp;
    }

    boolean isRequireDBSync() {
        return requireDBSync;
    }

    public void setRequireDBSync(boolean requireDBSync) {
        this.requireDBSync = requireDBSync;
    }

    public boolean hasId() {
        return id != -1;
    }


}
