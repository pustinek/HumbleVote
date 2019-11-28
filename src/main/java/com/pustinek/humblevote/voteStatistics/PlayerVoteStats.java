package com.pustinek.humblevote.voteStatistics;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonWriter;
import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.utils.Utils;
import com.pustinek.humblevote.voteStatistics.constants.MODIFICATION_TYPE;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.UUID;

public class PlayerVoteStats {

    // variables used for database syncing|saving
    private Integer id;
    private boolean needsDatabaseSync = false;


    private UUID playerUUID; // UUID of the player
    private String playerLastUsername; // username that the player used lastly

    private int totalVoteCount;
    private int points;
    //private HashMap<String, String> voteSiteStatistics = new HashMap<>(); // vote site statistics (vote-site / timestamp )
    private ArrayList<MonthlyStats> monthlyVoteStatistics = new ArrayList<>();
    private MonthlyStats currentMonthlyVoteStats = null;

    public PlayerVoteStats(UUID playerId, String username, Integer totalVoteCount, int points, String jsonString) {
        this.playerUUID = playerId;
        this.playerLastUsername = username;
        this.totalVoteCount = totalVoteCount;
        this.points = points;
        parseJSONToMonthlyStats(jsonString);
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

        // Voting point check
        Player player = Bukkit.getPlayer(playerUUID);
        int pointsToGive = 1;
        if(player != null)
           pointsToGive = Utils.votePointCalculator(player);

        points += pointsToGive;

        needsDatabaseSync = true;
    }


    public int modifyVoteCount(MODIFICATION_TYPE type, int value) {
        switch (type) {
            case ADD:
                totalVoteCount += value;
                currentMonthlyVoteStats.setVoteCount(currentMonthlyVoteStats.getVoteCount() + value);
                break;
            case SET:
                totalVoteCount = value;
                currentMonthlyVoteStats.setVoteCount(value);
                break;
            case SUBTRACT:
                totalVoteCount -= value;
                currentMonthlyVoteStats.setVoteCount(currentMonthlyVoteStats.getVoteCount() - value);
                break;
        }
        needsDatabaseSync = true;
        return totalVoteCount;
    }

    public int modifyVotePoints(MODIFICATION_TYPE type, int value) {
        switch (type) {
            case ADD:
                points += value;
                break;
            case SET:
                points = value;
                break;
            case SUBTRACT:
                points -= value;
                break;
        }
        needsDatabaseSync = true;
        return points;
    }


    private void parseJSONToMonthlyStats(String json) {
        if (json == null || json.equals("")) return;

        try {
            JsonArray monthlyStatsObj = JsonParser.object().from(json).getArray("months");

            monthlyStatsObj.forEach(a -> {
                Integer number;
                String dateString;
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

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPlayerLastUsername() {
        return playerLastUsername;
    }

    public int getTotalVoteCount() {
        return totalVoteCount;
    }

    public int getVotingPoints() {
        return points;
    }

    public int getMonthlyVoteCount(YearMonth date) {
        MonthlyStats stats = monthlyVoteStatistics.stream().filter(monthlyStats -> monthlyStats.getDate().equals(date)).findAny().orElse(null);
        return stats != null ? stats.getVoteCount() : 0;
    }

    public int getMonthlyVoteCount() {
        YearMonth yearMonth = Main.getTimeManager().getYearMonth();
        return getMonthlyVoteCount(yearMonth);
    }


    public boolean isNeedsDatabaseSync() {
        return needsDatabaseSync;
    }

    public void setNeedsDatabaseSync(boolean needsDatabaseSync) {
        this.needsDatabaseSync = needsDatabaseSync;
    }


    public String getMonthlyStatisticsAsJSON() {

        ArrayList<String> monthlyStatsString = new ArrayList<>();
        monthlyVoteStatistics.forEach(monthlyStats -> {
            String json = JsonWriter.string().object()
                    .value("total", monthlyStats.getVoteCount())
                    .value("date", monthlyStats.getDate().toString())
                    .end().done();
            monthlyStatsString.add(json);
        });


        return JsonWriter.string().object()
                .array("months", monthlyStatsString)
                .end().done();
    }

    private static class MonthlyStats {

        int voteCount;
        YearMonth date;

        MonthlyStats(int voteCount, YearMonth date) {
            this.voteCount = voteCount;
            this.date = date;
        }

        int getVoteCount() {
            return voteCount;
        }

        void setVoteCount(int value) {
            voteCount = value;
        }

        void incrementVoteCount() {
            voteCount++;
        }

        YearMonth getDate() {
            return date;
        }
    }

}

