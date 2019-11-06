package com.pustinek.humblevote.votingRewards;

public class RewardRequirements {

    public int monthlyVotes;
    public int totalVotes;
    public int serverMonthlyVotes;
    public int serverTotalVotes;


    public RewardRequirements(int monthlyVotes, int totalVotes, int serverMonthlyVotes, int serverTotalVotes) {
        this.monthlyVotes = monthlyVotes;
        this.totalVotes = totalVotes;
        this.serverMonthlyVotes = serverMonthlyVotes;
        this.serverTotalVotes = serverTotalVotes;
    }

    public int getMonthlyVotes() {
        return monthlyVotes;
    }

    public int getTotalVotes() {
        return totalVotes;
    }

    public int getServerMonthlyVotes() {
        return serverMonthlyVotes;
    }

    public int getServerTotalVotes() {
        return serverTotalVotes;
    }
}
