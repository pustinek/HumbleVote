package com.pustinek.humblevote.placeholders;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.voteStatistics.TopVoteStatsType;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.time.YearMonth;

public class Placeholders extends PlaceholderExpansion {

    private final Main plugin;

    public Placeholders(Main plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public boolean canRegister(){
        return true;
    }
    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getIdentifier() {
        return "humblevote";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }


    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        // %humbevote_[identifier]%
        if(identifier.equals("total")){
            return Integer.toString(Main.getVoteStatisticsManager().getPlayerVoteStats(player.getUniqueId()).getTotalVoteCount());
        }
        if(identifier.equals("month")){
            return "" + Main.getVoteStatisticsManager().getPlayerVoteStats(player.getUniqueId()).getMonthlyVoteCount(YearMonth.now());
        }
        if(identifier.equals("server_total")){
            return Integer.toString(Main.getVoteStatisticsManager().getServerTotalVotes(TopVoteStatsType.TOTAL));
        }
        if(identifier.equals("server_month")){
            return Integer.toString(Main.getVoteStatisticsManager().getServerTotalVotes(TopVoteStatsType.MONTH));
        }

        return null;

    }



}
