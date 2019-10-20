package com.pustinek.humblevote.voting;

public class QueuedVote {
    private Integer databaseID;
    private String serviceName;
    private String username;
    private String address;
    private String timestamp;
    private boolean isFake;
    private Integer cacheIndex = -1;

    public QueuedVote(String address, String serviceName, String username, String timestamp, boolean isFake) {
        this.address = address;
        this.serviceName = serviceName;
        this.username = username;
        this.timestamp = timestamp;
        this.isFake = isFake;
    }

    public String  getServiceName() {
        return serviceName;
    }

    public String getUsername() {
        return username;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getAddress() {
        return address;
    }

    public Integer getDatabaseID() {
        return databaseID;
    }

    public boolean isFake() {
        return isFake;
    }

    public void setCacheIndex(Integer cacheIndex) {
        this.cacheIndex = cacheIndex;
    }

    public Integer getCacheIndex() {
        return cacheIndex;
    }

    public void setDatabaseID(Integer databaseID) {
        this.databaseID = databaseID;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        QueuedVote qv = (QueuedVote) o;

        return  address.equals(qv.address) &&
                serviceName.equals(qv.serviceName) &&
                timestamp.equals(qv.timestamp) &&
                username.equals(qv.username) &&
                cacheIndex.equals(qv.cacheIndex);
    }
}
