package com.pustinek.humblevote.voteStatistics;

import com.grack.nanojson.*;
import com.pustinek.humblevote.Main;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class PlayerVoteStats {

    // variables used for database syncing|saving
    private Integer id;
    private boolean needsDatabaseSync = false;


    private UUID playerId; // UUID of the player
    private String playerLastUsername; // username that the player used lastly

    private int totalVoteCount;
    private HashMap<String, String> voteSiteStatistics = new HashMap<>(); // vote site statistics (vote-site / timestamp )
    private ArrayList<MonthlyStats> monthlyVoteStatistics = new ArrayList<>();
    private MonthlyStats currentMonthlyVoteStats = null;

    public PlayerVoteStats(UUID playerId, String username, Integer totalVoteCount, String jsonString) {
        this.playerId = playerId;
        this.playerLastUsername = username;
        this.totalVoteCount = totalVoteCount;
        fromJson(jsonString);
        monthlyStatisticsCheck();
    }

    /*
     * Method to check if the month is valid (current or set in configuration)
     *
     * */
    void monthlyStatisticsCheck() {

        // Check if the current month and year correspond with the right month and year
        YearMonth currentYearMonth = YearMonth.now(ZoneId.of("UTC"));

        if (Main.getConfigManager().devEnabled) {
            currentYearMonth = YearMonth.of(Main.getConfigManager().devYear, Main.getConfigManager().devMonth);
        }
        //Main.debug("[VoteStatistics] using yearMonth: " + currentYearMonth);
        YearMonth finalCurrentYearMonth = currentYearMonth;

        if (currentMonthlyVoteStats != null && currentMonthlyVoteStats.getDate().equals(currentYearMonth)) return;

        monthlyVoteStatistics.forEach(mvs -> {
            if (mvs.getDate().equals(finalCurrentYearMonth)) {
                currentMonthlyVoteStats = mvs;
            }
        });

        if (currentMonthlyVoteStats == null || !currentMonthlyVoteStats.getDate().equals(currentYearMonth)) {
            // If there is a new month, or the data wasn't found create a new statistic
            currentMonthlyVoteStats = new MonthlyStats(0, currentYearMonth);
            monthlyVoteStatistics.add(currentMonthlyVoteStats);
        }
    }

    void incrementPlayerVote() {
        totalVoteCount += 1;
        // Before incrementing check if it's current month
        monthlyStatisticsCheck();
        currentMonthlyVoteStats.incrementVoteCount();
        needsDatabaseSync = true;
    }



    void addVoteSiteStatistic(String voteSiteServiceName, String timestamp) {
        needsDatabaseSync = true;
        voteSiteStatistics.put(voteSiteServiceName, timestamp);
    }

    private void fromJson(String json) {
        if (json == null || json.equals("")) return;

        try {
            JsonObject voteSitesObj = JsonParser.object().from(json).getObject("voteSites");
            JsonArray monthlyStatsObj = JsonParser.object().from(json).getArray("months");


            voteSitesObj.forEach((a, b) -> {
                if (b instanceof String) {
                    voteSiteStatistics.put(a, b.toString());
                } else {
                    Main.error("failed to parse voteSite statistics from database !");
                }
            });
            monthlyStatsObj.forEach(a -> {
                Integer number = null;
                String dateString = null;
                try {
                    number = JsonParser.object().from(a.toString()).getInt("total");
                    dateString = JsonParser.object().from(a.toString()).getString("date");
                    MonthlyStats monthlyStats = new MonthlyStats(number, YearMonth.parse(dateString));
                    monthlyVoteStatistics.add(monthlyStats);
                } catch (JsonParserException e) {
                    e.printStackTrace();
                }

            });

        } catch (JsonParserException e) {
            Main.error(e);
            e.printStackTrace();
        }
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean hasId() {
        return (id != null);
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getPlayerLastUsername() {
        return playerLastUsername;
    }

    public int getTotalVoteCount() {
        return totalVoteCount;
    }


    public int getMonthlyVoteCount(YearMonth date) {
        MonthlyStats stats = monthlyVoteStatistics.stream().filter(monthlyStats -> monthlyStats.getDate().equals(date)).findAny().orElse(null);
        return stats != null ? stats.getVoteCount() : 0;
    }

    public String getPlayerVoteSiteLastVoteTimestamp(String voteSiteServiceName) {
        return voteSiteStatistics.get(voteSiteServiceName);
    }

    public boolean isNeedsDatabaseSync() {
        return needsDatabaseSync;
    }

    public void setNeedsDatabaseSync(boolean needsDatabaseSync) {
        this.needsDatabaseSync = needsDatabaseSync;
    }


    public String toJson() {

        ArrayList<String> monthlyStatsString = new ArrayList<>();
        monthlyVoteStatistics.forEach(monthlyStats -> {
            String json = JsonWriter.string().object()
                    .value("total", monthlyStats.getVoteCount())
                    .value("date", monthlyStats.getDate().toString())
                    .end().done();
            monthlyStatsString.add(json);
        });


        return JsonWriter.string().object()
                .value("totalVoteCount", totalVoteCount)
                .value("voteSites", voteSiteStatistics)
                .array("months", monthlyStatsString)
                .end().done();
    }

    private static class MonthlyStats {

        int voteCount;
        YearMonth date;

        MonthlyStats(Integer voteCount, YearMonth date) {
            this.voteCount = voteCount;
            this.date = date;
        }

        int getVoteCount() {
            return voteCount;
        }

        void incrementVoteCount() {
            voteCount++;
        }

        YearMonth getDate() {
            return date;
        }
    }

}

