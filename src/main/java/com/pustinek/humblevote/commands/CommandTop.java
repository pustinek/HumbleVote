package com.pustinek.humblevote.commands;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.gui.GUIManager;
import com.pustinek.humblevote.utils.Permissions;
import com.pustinek.humblevote.voteStatistics.constants.TOP_VOTES_STATS_TYPE;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class CommandTop extends CommandDefault {


    CommandTop(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommandStart() {
        return "humblevote top";
    }

    @Override
    public String getHelp(CommandSender target) {
        return "help-top";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.VOTING)){
            Main.message(sender, "permission-insufficient");
            return;
        }

        if(!(sender instanceof Player)) {
            Main.message(sender, "cmd-onlyByPlayer");
            return;
        }

        Player player = (Player) sender;

        TOP_VOTES_STATS_TYPE voteStats = TOP_VOTES_STATS_TYPE.TOTAL;

        YearMonth yearMonth;

        if (args.length > 1) {
            try {
                yearMonth = YearMonth.parse(args[1]);
                voteStats = TOP_VOTES_STATS_TYPE.MONTH;
            } catch (DateTimeParseException ex) {
                yearMonth = Main.getTimeManager().getYearMonth();
                Main.message(player, "top-monthYearFailParse", args[1]);
            }
        } else {
            yearMonth = Main.getTimeManager().getYearMonth();
        }

        GUIManager.displayTopVotersGUI(voteStats, yearMonth).open(player);
    }

    @Override
    public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
        List<String> results = new ArrayList<>();
        if (toComplete == 2) {
            results.add("[year-month]");
        }
        return results;
    }
}
