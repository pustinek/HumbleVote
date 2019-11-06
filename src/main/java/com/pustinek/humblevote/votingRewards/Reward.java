package com.pustinek.humblevote.votingRewards;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.utils.ChatUtils;
import com.pustinek.humblevote.utils.Utils;
import com.pustinek.humblevote.voteStatistics.PlayerVoteStats;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Reward {

    private String id;
    private int reqTotalVotes = 0;
    private int reqMonthlyVotes = 0;
    private boolean claimable;
    private List<String> commands;
    private RewardType rewardType;
    private RewardRequirements requirements;

    private String GUIName;
    private List<String> GUILore;
    private Material GUIMaterial;

    public Reward(String id, RewardType rewardType, Integer reqTotalVotes, Integer reqMonthlyVotes, boolean claimable, List<String > commands, String GUIName, List<String> GUILore, Material GUIMaterial) {
        this.id = id;
        this.reqTotalVotes = reqTotalVotes;
        this.reqMonthlyVotes = reqMonthlyVotes;
        this.claimable = claimable;
        this.commands = commands;
        this.rewardType = rewardType;
        this.GUIName = GUIName;
        this.GUILore = GUILore;
        this.GUIMaterial = GUIMaterial;
    }
    
    public ItemStack buildGUIItem(Player player) {
        YearMonth yearMonth = Main.getTimeManager().getYearMonth();
        ItemStack itemStack = new ItemStack(GUIMaterial);
        ItemMeta itemMeta = itemStack.getItemMeta();

        if(itemMeta == null) {
            Main.warning("Failed to build GUI item for reward " + getId());
            return new ItemStack(Material.BARRIER);
        }
        HashMap<String, String> replaceMap = new HashMap<>();

        PlayerVoteStats ps = Main.getVoteStatisticsManager().getPlayerVoteStats(player.getUniqueId());

        ArrayList<PlayerRewardRecord> playerRewardRecords = Main.getRewardManager().getPlayerRewardRecords(player.getUniqueId(), id);
        PlayerRewardRecord playerRewardRecord = null;
        if(rewardType.equals(RewardType.MONTHLY) || rewardType.equals(RewardType.SERVER_MONTHLY)){
            playerRewardRecord = playerRewardRecords.stream().filter(prr -> Main.getTimeManager().InstantToYearMonth(prr.getTimestamp()).equals(yearMonth)).findAny().orElse(null);
        } else{
            playerRewardRecord = playerRewardRecords.stream().findFirst().orElse(null);
        }
        replaceMap.put("{requirements.total}", "" + reqTotalVotes);
        replaceMap.put("{requirements.month}", "" + reqMonthlyVotes);
        replaceMap.put("{votes.month}", ("" + ps.getMonthlyVoteCount(Main.getTimeManager().getYearMonth())));
        replaceMap.put("{votes.total}", String.valueOf(ps.getTotalVoteCount()));
        replaceMap.put("{config.claimable}", String.valueOf(isClaimable()));
        replaceMap.put("{config.type}", rewardType.toString());
        if(playerRewardRecord == null) {
            if(ps.getMonthlyVoteCount(yearMonth) >= reqMonthlyVotes && ps.getTotalVoteCount() >= reqTotalVotes) {
                replaceMap.put("{requirements.claimable}", "Reward Can be claimed");
            }else{
                replaceMap.put("{requirements.claimable}", "Requirements not meet");
            }
        }else {
            replaceMap.put("{requirements.claimable}", "&cAlready claimed");
        }


       final ArrayList<String> finalLore = Utils.replaceStuffInString(replaceMap, (ArrayList<String>) getGUILore());
        itemMeta.setDisplayName(ChatUtils.chatColor(GUIName));
        itemMeta.setLore(ChatUtils.chatColor(finalLore));
        itemStack.setItemMeta(itemMeta);
        return itemStack;

    }

    /**
     * Check if player is eligible for the reward
     * (reward req. meet + not claimed)
     *
     * @param player player to check
     * */
    public boolean isPlayerEligible(Player player) {
        if(rewardType.equals(RewardType.ONVOTE)) return true;
        return isPlayerMeetsRequirements(player) && !isPlayerAlreadyClaimed(player);
    }

    /**
     * Check if player meets the reward requirements
     *
     * @param player player to check
     * */
    public boolean isPlayerMeetsRequirements(Player player) {

        PlayerVoteStats playerVoteStats = Main.getVoteStatisticsManager().getPlayerVoteStats(player.getUniqueId());
        YearMonth currentYearMonth = Main.getTimeManager().getYearMonth();
        return (playerVoteStats.getMonthlyVoteCount(currentYearMonth)) >= this.getReqMonthlyVotes() && (playerVoteStats.getTotalVoteCount()) >= this.getReqTotalVotes();
    }

    /**
     * Check if player already claimed the reward
     * Checks are made in depth -> Month is reset every month
     *
     * @param player player to check
     * */
    public boolean isPlayerAlreadyClaimed(Player player) {
        ArrayList<PlayerRewardRecord> playerRewardRecords = Main.getRewardManager().getPlayerRewardRecords(player.getUniqueId());
        ArrayList<PlayerRewardRecord> filteredRewardRecords = playerRewardRecords.stream().filter(prr -> prr.getRewardID().equals(id)).collect(Collectors.toCollection(ArrayList::new));
        YearMonth currentYearMonth = Main.getTimeManager().getYearMonth();
        switch (rewardType) {
        case ONVOTE:
                return false;
        case MONTHLY:
        case SERVER_MONTHLY:
            return filteredRewardRecords.stream().anyMatch(prr ->
                    ( Main.getTimeManager().InstantToYearMonth(prr.getTimestamp()).getYear() == currentYearMonth.getYear() &&
                            Main.getTimeManager().InstantToYearMonth(prr.getTimestamp()).getMonth().equals(currentYearMonth.getMonth()))
            );
        case ONETIME:
        case SERVER_ONETIME:
            return filteredRewardRecords.stream().anyMatch(prr -> prr.getRewardID().equals(id));
        default:
            return true;
        }
    }



    public String getId() {
        return id;
    }

    public RewardType getRewardType() {
        return rewardType;
    }

    public Integer getReqTotalVotes() {
        return reqTotalVotes;
    }

    public Integer getReqMonthlyVotes() {
        return reqMonthlyVotes;
    }

    public boolean isClaimable() {
        return claimable;
    }

    public List<String> getCommands() {
        return commands;
    }

    public List<String> getGUILore() {
        return GUILore;
    }

}
