package com.pustinek.humblevote.utils;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.voteSites.VoteSite;
import com.pustinek.humblevote.voteStatistics.PlayerVoteStats;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private final static Pattern p = Pattern.compile("\\d+");

    public static Timestamp convertStringToTimestamp(String strDate) {
        try {
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            // you can change format of date
            Date date = formatter.parse(strDate);

            return new Timestamp(date.getTime());
        } catch (ParseException e) {
            System.out.println("Exception :" + e);
            return null;
        }
    }

    public static Integer findFirstNumberSequenceInString(String string) {
        Matcher m = p.matcher(string);
        return m.find() ? Integer.parseInt(m.group()) : -1;
    }

    public static String replaceStuffInString(HashMap<String, String> map, String str) {
        Set<String> keys = map.keySet();

        for(String key: keys){
            str = str.replace(key, map.get(key));
        }
        return str;
    }
    public static ArrayList<String> replaceStuffInString(HashMap<String, String> map, ArrayList<String> stringArrayList) {
        Set<String> keys = map.keySet();
        ArrayList<String> finalStringList = new ArrayList<>();
        for(String str : stringArrayList) {
            for(String key: keys){
                str = str.replace(key, map.get(key));
            }
            finalStringList.add(str);
        }
        return finalStringList;
    }
    public static int limitInt(int value, int max, int min) {
            return Math.max(0, Math.min(value, max));
    }



    public static long getPlayerVoteSiteCooldown(Player player, VoteSite voteSite) {

        PlayerVoteStats ps = Main.getVoteStatisticsManager().getPlayerVoteStats(player.getUniqueId());

        long timeElapsed = 99999;
        long voteCooldown = voteSite.getVoteCooldown();
        if(ps != null) {
            String voteTimestamp = ps.getPlayerVoteSiteLastVoteTimestamp(voteSite.getService_site());
            if(voteTimestamp != null) {
                long voteTimestampAsLong = Long.parseLong(voteTimestamp);
                Instant start = Instant.ofEpochMilli(voteTimestampAsLong);
                Instant finish = Instant.now();
                timeElapsed = Duration.between(start, finish).toMinutes();
            }
        }
        long timeLeft = voteCooldown - timeElapsed;
        if(timeLeft < 0) timeLeft = 0;

        return timeLeft;
    }

}
