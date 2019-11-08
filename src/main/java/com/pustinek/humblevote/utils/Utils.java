package com.pustinek.humblevote.utils;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.voteSites.VoteSite;
import com.pustinek.humblevote.voteStatistics.PlayerVoteStats;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
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

    public static int votePointCalculator(Player player) {
        int votePointsToGive = 1;

        Set<PermissionAttachmentInfo> effectivePermissions = player.getEffectivePermissions();
        Pattern limitPattern = Pattern.compile("(humblevote.votepoints).([+|x])([0-9]+)");
        Iterator<PermissionAttachmentInfo> itr = effectivePermissions.iterator();
        ArrayList<Integer> addValues = new ArrayList<>();
        ArrayList<Integer> multiplyValues = new ArrayList<>();

        while (itr.hasNext()) {
            PermissionAttachmentInfo info = itr.next();
            String permission = info.getPermission();
            Matcher matcher = limitPattern.matcher(permission);
            if (matcher.find()) {
                if(matcher.groupCount() < 3) {
                    Main.debug("found but to short");
                    continue;
                }

                Main.debug("0 -> " + matcher.group(0) );
                Main.debug("1 -> " + matcher.group(1) );
                Main.debug("2 -> " + matcher.group(2) );
                Main.debug("3 -> " + matcher.group(3) );


                String type = matcher.group(2);
                int value = Integer.parseInt(matcher.group(3));
                if(type.equalsIgnoreCase("x")) {
                    multiplyValues.add(value);
                }else if(type.equalsIgnoreCase("+")) {
                    addValues.add(value);
                }

            }
        }
        int toMultiply = multiplyValues.stream().reduce(1, (a,b) -> a * b);
        if(toMultiply < 1) toMultiply = 1;
        votePointsToGive *= toMultiply;
        int toSum = addValues.stream().mapToInt(Integer::intValue).sum();
        votePointsToGive += toSum;

        return votePointsToGive;
    }


}
