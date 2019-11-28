package com.pustinek.humblevote.commands;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.utils.Permissions;
import com.pustinek.humblevote.voteStatistics.PlayerVoteStats;
import com.pustinek.humblevote.voteStatistics.constants.MODIFICATION_TYPE;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class CommandManage extends CommandDefault {
    CommandManage(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommandStart() {
        return "humblevote manage";
    }

    @Override
    public String getHelp(CommandSender target) {
        if (target.hasPermission(Permissions.MANAGE))
            return "help-manage";
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.MANAGE))
            return;

        if(args.length < 5){
            Main.messageNoPrefix(sender,"manage-help");
            return;
        }

        String playerName = args[1];
        String what = args[2];
        String type = args[3];
        String successKey;
        int value = Integer.parseInt(args[4]);
        int prevValue = 0;
        int newValue = 0;



        PlayerVoteStats playerVoteStats = Main.getVoteStatisticsManager().getPlayerVoteStats(playerName);

        if(playerVoteStats == null && !playerName.equalsIgnoreCase("*")) {
            Main.messageNoPrefix(sender, "manage-noPlayerStats", playerName);
            return;
        }

        if(what.equalsIgnoreCase("votes") && playerVoteStats != null){
            prevValue = playerVoteStats.getTotalVoteCount();
        }else if(what.equalsIgnoreCase("points") && playerVoteStats != null){
            prevValue = playerVoteStats.getVotingPoints();
        }

        switch (type.toLowerCase()){
            case "add":
                successKey = "manage-successAdd";
                if(what.equalsIgnoreCase("votes")) {
                    //newValue = playerVoteStats.modifyVoteCount(MODIFICATION_TYPE.ADD, value);
                    newValue = modifyPlayerVoteStats(playerVoteStats, MODIFICATION_TYPE.ADD,'v', value);
                }else if(what.equalsIgnoreCase("points")) {
                    //newValue = playerVoteStats.modifyVotePoints(MODIFICATION_TYPE.ADD, value);
                    newValue = modifyPlayerVoteStats(playerVoteStats, MODIFICATION_TYPE.ADD,'p', value);
                }else {
                    Main.messageNoPrefix(sender, "manage-help");
                    return;
                }
                break;
            case "set":
                successKey = "manage-successSet";
                if(what.equalsIgnoreCase("votes")) {
                    //newValue = playerVoteStats.modifyVoteCount(MODIFICATION_TYPE.SET, value);
                    newValue = modifyPlayerVoteStats(playerVoteStats, MODIFICATION_TYPE.SET,'v', value);

                }else if(what.equalsIgnoreCase("points")) {
                    //newValue = playerVoteStats.modifyVotePoints(MODIFICATION_TYPE.SET, value);
                    newValue = modifyPlayerVoteStats(playerVoteStats, MODIFICATION_TYPE.SET,'p', value);
                }else {
                    Main.messageNoPrefix(sender, "manage-help");
                    return;
                }
                break;
            case "subtract":
                successKey = "manage-successSubtract";
                if(what.equalsIgnoreCase("votes")) {
                    // newValue = playerVoteStats.modifyVoteCount(MODIFICATION_TYPE.SUBTRACT, value);
                    newValue = modifyPlayerVoteStats(playerVoteStats, MODIFICATION_TYPE.SUBTRACT,'v', value);
                }else if(what.equalsIgnoreCase("points")) {
                    // newValue = playerVoteStats.modifyVotePoints(MODIFICATION_TYPE.SUBTRACT, value);
                    newValue = modifyPlayerVoteStats(playerVoteStats, MODIFICATION_TYPE.SUBTRACT,'p', value);
                }else {
                    Main.messageNoPrefix(sender, "manage-help");
                    return;
                }
                break;
            default:
                Main.messageNoPrefix(sender, "manage-help");
                return;
        }

        Main.messageNoPrefix(sender,
                successKey
                , value,
                what.toLowerCase(),
                playerName,
                prevValue,
                newValue);

    }

    private int modifyPlayerVoteStats(PlayerVoteStats playerVoteStats, MODIFICATION_TYPE type,char whatToModify, int value) {
        if(playerVoteStats == null) {
            // All players
            for(PlayerVoteStats pvs : Main.getVoteStatisticsManager().getPlayerVoteStatsConcurrentHashMap().values()) {
                if(whatToModify == 'p'){
                    pvs.modifyVotePoints(type,value);
                }else {
                    pvs.modifyVoteCount(type,value);
                }
            }

        }else{
            if(whatToModify == 'p'){
                return playerVoteStats.modifyVotePoints(type,value);
            }else {
                return playerVoteStats.modifyVoteCount(type,value);
            }
        }

        return -1;
    }


    @Override
    public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
        List<String> results = new ArrayList<>();

        if(toComplete == 2) {
            results.addAll(Main.getVoteStatisticsManager().getPlayerNamesThatHaveVoteStats());
            results.add("*");
        }
        if(toComplete == 3) {
            results.add("points");
            results.add("votes");
        }
        if(toComplete == 4) {
            results.add("set");
            results.add("add");
            results.add("subtract");
        }
        if(toComplete == 5) {
            results.add("<value>");
        }


        return results;
    }
}
