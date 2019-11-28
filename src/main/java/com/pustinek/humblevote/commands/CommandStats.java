package com.pustinek.humblevote.commands;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.utils.Permissions;
import com.pustinek.humblevote.voteStatistics.PlayerVoteStats;
import com.pustinek.humblevote.voteStatistics.constants.TOP_VOTES_STATS_TYPE;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandStats  extends CommandDefault{


    CommandStats(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommandStart() {
        return "humblevote stats";
    }

    @Override
    public String getHelp(CommandSender target) {
        return "help-stats";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.VOTING)){
            Main.message(sender, "permission-insufficient");
            return;
        }

        if(!(sender instanceof Player)) {
            Main.message(sender, "cmd-onlyPlayer");
            return;
        }

        Player player = (Player) sender;
        PlayerVoteStats playerVoteStats = null;
        String titleName = "";


        if(args.length == 2) {
            if(args[1].equalsIgnoreCase("server")) {
                titleName = "server";
                Main.messageNoPrefix(sender,
                        "stats",
                        titleName,
                        Main.getVoteStatisticsManager().getServerTotalVotes(TOP_VOTES_STATS_TYPE.TOTAL),
                        Main.getVoteStatisticsManager().getServerTotalVotes(TOP_VOTES_STATS_TYPE.MONTH),
                        "probably a lot",
                        Main.getTimeManager().getReadableTimeFormat());
                return;
            }
        }

        if(args.length == 1) {
            playerVoteStats = Main.getVoteStatisticsManager().getPlayerVoteStats(player.getUniqueId());
            titleName = "your";
        }else if(args.length > 2) {
            String value = args[2];
            playerVoteStats = Main.getVoteStatisticsManager().getPlayerVoteStats(value);
            titleName = value;
        }else {
            Main.messageNoPrefix(sender, "stats-help");
            return;
        }



        if(playerVoteStats == null) {
            Main.messageNoPrefix(sender, "stats-noStats", titleName);
            return;
        }
        Main.messageNoPrefix(sender,
                "stats",
                titleName,
                playerVoteStats.getTotalVoteCount(),
                playerVoteStats.getMonthlyVoteCount(Main.getTimeManager().getYearMonth()),
                playerVoteStats.getVotingPoints(),
                Main.getTimeManager().getReadableTimeFormat()
        );
    }

    @Override
    public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
        ArrayList<String> results = new ArrayList<>();

        if(toComplete == 2) {
            results.add("server");
            results.add("player");
        }

        if(toComplete == 3 && start.length == 3 && start[2].equalsIgnoreCase("player")) {
            results.addAll(Main.getVoteStatisticsManager().getPlayerNamesThatHaveVoteStats());
        }


        return results;


    }
}
