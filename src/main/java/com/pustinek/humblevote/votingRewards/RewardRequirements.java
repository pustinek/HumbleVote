package com.pustinek.humblevote.votingRewards;

class RewardRequirements {

    private int monthlyVotes;
    private int totalVotes;
    private int serverMonthlyVotes;
    private int serverTotalVotes;
    private int votingPoints;


    RewardRequirements(int monthlyVotes, int totalVotes, int serverMonthlyVotes, int serverTotalVotes, int votingPoints) {
        this.monthlyVotes = monthlyVotes;
        this.totalVotes = totalVotes;
        this.serverMonthlyVotes = serverMonthlyVotes;
        this.serverTotalVotes = serverTotalVotes;
        this.votingPoints =votingPoints;
    }

    int getMonthlyVotes() {
        return monthlyVotes;
    }

    int getTotalVotes() {
        return totalVotes;
    }

    int getServerMonthlyVotes() {
        return serverMonthlyVotes;
    }

    int getServerTotalVotes() {
        return serverTotalVotes;
    }

    int getVotingPoints() {
        return votingPoints;
    }
}
