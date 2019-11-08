package com.pustinek.humblevote.votingRewards;

public class RewardRequirements {

    public int monthlyVotes;
    public int totalVotes;
    public int serverMonthlyVotes;
    public int serverTotalVotes;
    public int votingPoints;


    public RewardRequirements(int monthlyVotes, int totalVotes, int serverMonthlyVotes, int serverTotalVotes, int votingPoints) {
        this.monthlyVotes = monthlyVotes;
        this.totalVotes = totalVotes;
        this.serverMonthlyVotes = serverMonthlyVotes;
        this.serverTotalVotes = serverTotalVotes;
        this.votingPoints =votingPoints;
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

    public int getVotingPoints() {
        return votingPoints;
    }
}
