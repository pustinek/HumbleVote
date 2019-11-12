package com.pustinek.humblevote.votingRewards;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.utils.ChatUtils;
import com.pustinek.humblevote.utils.Utils;
import com.pustinek.humblevote.voteStatistics.PlayerVoteStats;
import com.pustinek.humblevote.voteStatistics.constants.TOP_VOTES_STATS_TYPE;
import com.pustinek.humblevote.votingRewards.constants.REWARD_TYPE;
import me.wiefferink.interactivemessenger.processing.Message;
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
    private boolean enabled;
    private boolean claimable;
    private List<String> rewards;
    private REWARD_TYPE REWARDTYPE;
    private RewardRequirements requirements;

    private String GUIName;
    private List<String> GUILore;
    private Material GUIMaterial;
    private boolean displayGUI;

    Reward(String id, boolean enabled, REWARD_TYPE REWARDTYPE, boolean claimable, RewardRequirements requirements, List<String> rewards, String GUIName, List<String> GUILore, Material GUIMaterial, boolean displayGUI) {
        this.id = id;
        this.enabled = enabled;
        this.claimable = claimable;
        this.requirements = requirements;
        this.rewards = rewards;
        this.REWARDTYPE = REWARDTYPE;
        this.GUIName = GUIName;
        this.GUILore = GUILore;
        this.GUIMaterial = GUIMaterial;
        this.displayGUI = displayGUI;
    }


    public ItemStack buildGUIItem(Player player) {
        boolean readyToBeClaimed = false;
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
        PlayerRewardRecord playerRewardRecord;
        if(REWARDTYPE.equals(REWARD_TYPE.MONTHLY)){
            playerRewardRecord = playerRewardRecords.stream().filter(prr -> Main.getTimeManager().InstantToYearMonth(prr.getTimestamp()).equals(yearMonth)).findAny().orElse(null);
        } else{
            playerRewardRecord = playerRewardRecords.stream().findFirst().orElse(null);
        }
        replaceMap.put("{requirements.total}", "" + requirements.getTotalVotes());
        replaceMap.put("{requirements.month}", "" + requirements.getMonthlyVotes());
        replaceMap.put("{requirements.server.total}", "" + requirements.getServerTotalVotes());
        replaceMap.put("{requirements.server.month}", "" + requirements.getServerMonthlyVotes());
        replaceMap.put("{requirements.points}", "" + requirements.getVotingPoints());
        replaceMap.put("{votes.month}", String.valueOf(ps.getMonthlyVoteCount()));
        replaceMap.put("{votes.total}", String.valueOf(ps.getTotalVoteCount()));
        replaceMap.put("{votes.points}", String.valueOf(ps.getVotingPoints()));
        replaceMap.put("{votes.server.total}", String.valueOf(Main.getVoteStatisticsManager().getServerTotalVotes(TOP_VOTES_STATS_TYPE.TOTAL)));
        replaceMap.put("{votes.server.month}", String.valueOf(Main.getVoteStatisticsManager().getServerTotalVotes(TOP_VOTES_STATS_TYPE.MONTH)));
        replaceMap.put("{config.claimable}", String.valueOf(isClaimable()));
        replaceMap.put("{config.type}", REWARDTYPE.toString());
        if (playerRewardRecord == null && !REWARDTYPE.equals(REWARD_TYPE.ONVOTE)) {
            if(ps.getMonthlyVoteCount(yearMonth) >= requirements.getMonthlyVotes() && ps.getTotalVoteCount() >= requirements.getTotalVotes()) {
                replaceMap.put("{requirements.claimable}", Message.fromKey("rewards-placeholder-claim-readyToBeClaimed").getPlain());
            }else{
                replaceMap.put("{requirements.claimable}", Message.fromKey("rewards-placeholder-claim-requirementsNotMeet").getPlain());
            }
        }else {
            replaceMap.put("{requirements.claimable}", Message.fromKey("rewards-placeholder-claim-alreadyClaimed").getPlain());
        }

       final ArrayList<String> finalLore = Utils.replaceStuffInString(replaceMap, (ArrayList<String>) getGUILore());
        itemMeta.setDisplayName(ChatUtils.chatColor(GUIName));
        itemMeta.setLore(ChatUtils.chatColor(finalLore));


        itemStack.setItemMeta(itemMeta);
     /*   if(isPlayerEligible(player) && !(REWARDTYPE.equals(REWARD_TYPE.ONVOTE))) {
            Main.debug("adding enchant to reward Item" + id);
            itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        }*/

        return itemStack;

    }

    /**
     * Check if player is eligible for the reward
     * (reward req. meet + not claimed)
     *
     * @param player player to check
     * */
    public boolean isPlayerEligible(Player player) {
        if(REWARDTYPE.equals(REWARD_TYPE.ONVOTE)) return true;
        return isPlayerMeetsRequirements(player) && !isPlayerAlreadyClaimed(player);
    }

    /**
     * Check if player meets the reward requirements
     *
     * @param player player to check
     * */
    boolean isPlayerMeetsRequirements(Player player) {

        PlayerVoteStats playerVoteStats = Main.getVoteStatisticsManager().getPlayerVoteStats(player.getUniqueId());
        YearMonth currentYearMonth = Main.getTimeManager().getYearMonth();
        return (playerVoteStats.getMonthlyVoteCount(currentYearMonth)) >= this.getReqMonthlyVotes() &&
                (playerVoteStats.getTotalVoteCount()) >= this.getReqTotalVotes() &&
                (playerVoteStats.getVotingPoints() >= this.getReqVotingPoints() &&
                        Main.getVoteStatisticsManager().getServerTotalVotes(TOP_VOTES_STATS_TYPE.TOTAL) >= this.getRequirements().getServerTotalVotes() &&
        Main.getVoteStatisticsManager().getServerTotalVotes(TOP_VOTES_STATS_TYPE.MONTH) >= this.getRequirements().getServerMonthlyVotes()

                );
    }

    /**
     * Check if player already claimed the reward
     * Checks are made in depth -> Month is reset every month
     *
     * @param player player to check
     * */
    boolean isPlayerAlreadyClaimed(Player player) {
        ArrayList<PlayerRewardRecord> playerRewardRecords = Main.getRewardManager().getPlayerRewardRecords(player.getUniqueId());
        ArrayList<PlayerRewardRecord> filteredRewardRecords = playerRewardRecords.stream().filter(prr -> prr.getRewardID().equals(id)).collect(Collectors.toCollection(ArrayList::new));
        YearMonth currentYearMonth = Main.getTimeManager().getYearMonth();
        switch (REWARDTYPE) {
        case ONVOTE:
            case ANYTIME:
                return false;
        case MONTHLY:
            return filteredRewardRecords.stream().anyMatch(prr ->
                    ( Main.getTimeManager().InstantToYearMonth(prr.getTimestamp()).getYear() == currentYearMonth.getYear() &&
                            Main.getTimeManager().InstantToYearMonth(prr.getTimestamp()).getMonth().equals(currentYearMonth.getMonth()))
            );
        case ONETIME:
            return filteredRewardRecords.stream().anyMatch(prr -> prr.getRewardID().equals(id));
        default:
            return true;
        }
    }



    public String getId() {
        return id;
    }

    REWARD_TYPE getREWARDTYPE() {
        return REWARDTYPE;
    }

    private Integer getReqTotalVotes() {
        return requirements.getTotalVotes();
    }

    private Integer getReqMonthlyVotes() {
        return requirements.getMonthlyVotes();
    }

    int getReqVotingPoints() {
        return requirements.getVotingPoints();
    }

    private RewardRequirements getRequirements() {
        return requirements;
    }


    boolean isClaimable() {
        return claimable;
    }

    List<String> getRewards() {
        return rewards;
    }

    private List<String> getGUILore() {
        return GUILore;
    }

    String getGUIName() {
        return GUIName;
    }
}
