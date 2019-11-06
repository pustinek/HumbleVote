package com.pustinek.humblevote.commands;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.utils.Permissions;
import com.pustinek.humblevote.voteStatistics.PlayerVoteStats;
import com.pustinek.humblevote.voteStatistics.TopVoteStatsType;
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

        if(args.length > 1) {
            if(args[1].equalsIgnoreCase("server")) {
                Main.messageNoPrefix(sender, "stats","server", Main.getVoteStatisticsManager().getServerTotalVotes(TopVoteStatsType.TOTAL),Main.getVoteStatisticsManager().getServerTotalVotes(TopVoteStatsType.MONTH) , Main.getTimeManager().getReadableTimeFormat());
                return;
            }
        }


        Player player = (Player) sender;

        PlayerVoteStats pvs = Main.getVoteStatisticsManager().getPlayerVoteStats(player.getUniqueId());

        Main.messageNoPrefix(sender, "stats","your", pvs.getTotalVoteCount(), pvs.getMonthlyVoteCount(Main.getTimeManager().getYearMonth()), Main.getTimeManager().getReadableTimeFormat());
    }

    @Override
    public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
        ArrayList<String> results = new ArrayList<>();

        if(toComplete == 2) {
            results.add("server");
        }

        return results;


    }
}
