package com.pustinek.humblevote.votingRewards;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.utils.Callback;
import com.pustinek.humblevote.utils.ChatUtils;
import com.pustinek.humblevote.utils.Manager;
import com.pustinek.humblevote.utils.Utils;
import com.pustinek.humblevote.voteStatistics.PlayerVoteStats;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

public class RewardManager extends Manager{


    private final Main plugin;
    private static ArrayList<Reward> rewardArrayList = new ArrayList<>();
    private static HashMap<UUID, ArrayList<PlayerRewardRecord>> votingRewardRecords = new HashMap<>();

    public RewardManager(Main plugin) {

        this.plugin = plugin;
        loadRewardsConfig();
    }


    /**
     * Give reward to player
     *
     * @param player player that will receive the reward
     * @param reward reward that will be given
     **/
    private void giveRewardToPlayer(Player player, Reward reward) {
        HashMap<String, String> toReplace = new HashMap<>();
        toReplace.put("{player}", player.getName());
        reward.getCommands().forEach(cmd -> {

            if(cmd.startsWith("money")) {
                int i = Utils.findFirstNumberSequenceInString(cmd);
                if(i < 0) {Main.warning("failed to parse " + reward.getId() + " <money> reward, skipping reward. "); return;}
                Economy economy = Main.getEconomy();
                if(economy != null)
                    economy.depositPlayer(player, i);
                return;
            }

            final String finalCMD = Utils.replaceStuffInString(toReplace, cmd);
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), finalCMD);
        });
    }

    /**
     * Claim the reward for player
     *
     * @param player player that will claim the reward
     * @param rewardID ID of the reward
     **/
    public boolean playerClaimReward(Player player, String rewardID) {


        Reward reward = rewardArrayList.stream().filter(r -> r.getId().equals(rewardID)).findAny().orElse(null);
        if(reward == null) return false; // Reward doesn't exist

        // On-vote reward check
        if(reward.getRewardType() == RewardType.ONVOTE || !reward.isClaimable())   {
            Main.message(player,"rewards-notClaimable");
            return false;
        }

        if((reward.getRewardType().equals(RewardType.MONTHLY) ||
                reward.getRewardType().equals(RewardType.SERVER_MONTHLY))
                && reward.isPlayerAlreadyClaimed(player)
        ) {
            Main.message(player,"rewards-monthAlreadyClaimed");
            return false;
        }
        if((reward.getRewardType().equals(RewardType.ONETIME) ||
                reward.getRewardType().equals(RewardType.SERVER_ONETIME))
                && reward.isPlayerAlreadyClaimed(player)
        ) {
            Main.message(player,"rewards-onetimeAlreadyClaimed");
            return false;
        }

        if(reward.isPlayerMeetsRequirements(player)) {
            giveRewardToPlayer(player, reward);
            rewardGivenSuccessfully(player,rewardID, Main.getTimeManager().getTimeInstant());
            return true;
        }

        Main.message(player,"rewards-requirementsNotMeet");
        return false;

    }

    public void processPlayerVoteReward(Player player) {
        rewardArrayList.forEach(vr -> {
            if(vr.getRewardType().equals(RewardType.ONVOTE)){
                giveRewardToPlayer(player,vr);
            }
        });
    }


    /**
     * Check if player is a candidate for any of the rewards
     *
     * @param player player that will be checked for rewards
     **/

    private void processPlayerReward(Player player) {
        //TODO: processs on VoteReward separably
        ArrayList<PlayerRewardRecord> playerRewardRecords = votingRewardRecords.get(player.getUniqueId());
        PlayerVoteStats playerVoteStats = Main.getVoteStatisticsManager().getPlayerVoteStats(player.getUniqueId());
        YearMonth currentYearMonth = Main.getTimeManager().getYearMonth();
        rewardArrayList.forEach(vr -> {
            if(
                    vr.getRewardType().equals(RewardType.ONETIME) ||
                            vr.getRewardType().equals(RewardType.MONTHLY))
            {
                if(vr.isClaimable()) return; //Reward is claimable, so ignore it
                if(playerRewardRecords.stream().anyMatch(o -> o.getRewardID().equals(vr.getId()))) return;


                // If reward == monthly -> check that he hasn't gotten it this month
                if(vr.getRewardType().equals(RewardType.MONTHLY)) {
                    if(playerRewardRecords.stream().anyMatch(o -> o.getRewardID().equals(vr.getId()) &&
                            LocalDateTime.ofInstant(o.getTimestamp(), ZoneOffset.UTC).getMonth().equals(YearMonth.now().getMonth()))) return;
                }else if(vr.getRewardType().equals(RewardType.ONETIME)) {
                    //  reward == onetime && was already given to the player so skip this loop
                    if(playerRewardRecords.stream().anyMatch(o -> o.getRewardID().equals(vr.getId()))) return;
                }

                // Check if requirements are meet
                if((playerVoteStats.getMonthlyVoteCount(currentYearMonth)) >= vr.getReqMonthlyVotes() &&  (playerVoteStats.getTotalVoteCount()) >= vr.getReqTotalVotes()) {
                    giveRewardToPlayer(player,vr);

                    player.sendMessage(ChatUtils.chatColor("&5You successfully recieved reward with ID: "+ vr.getId()));
                    rewardGivenSuccessfully(player, vr.getId(), null);
                }

            }
        });

    }

    /**
     * Check if player reward-history is loaded
     *
     * @param player player
     **/
    //TODO: find a better way to do this
    public void checkPlayerRewardEligibility(Player player) {

        if(votingRewardRecords.get(player.getUniqueId()) == null) {
            Main.getDatabaseSQLite().getPlayerRewardRecords(player.getUniqueId(), new Callback<ArrayList<PlayerRewardRecord>>(plugin) {
                @Override
                public void onResult(ArrayList<PlayerRewardRecord> result) {
                    if(result != null && result.size() > 0) {
                        votingRewardRecords.put(player.getUniqueId(), result);
                        processPlayerReward(player);
                    }else{
                        votingRewardRecords.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());
                        processPlayerReward(player);
                    }
                    super.onResult(result);
                }

                @Override
                public void onError(Throwable throwable) {
                    super.onError(throwable);
                }
            });
        }else{
            processPlayerReward(player);
        }
    }
    /**
     * Add player reward received entry to memory and Database
     *
     * @param player player that received the reward
     * @param rewardID ID of the received reward
     **/
    private void rewardGivenSuccessfully(Player player, String rewardID, Instant date) {
        PlayerRewardRecord pvr = new PlayerRewardRecord(player.getUniqueId(),player.getName(), date != null ? date : Instant.now(), rewardID);
        votingRewardRecords.computeIfAbsent(player.getUniqueId(), pr -> new ArrayList<>()).add(pvr);
        Main.message(player, "rewards-successfullyReceived", rewardID);
        Main.getDatabaseSQLite().savePlayerVotingReward(pvr);
    }


    /*
     *
     * (Re)Load configuration file for voting sites (voteSite.yml)
     *
     * */
    public void loadRewardsConfig() {

        // Clean up array lists !
        rewardArrayList = new ArrayList<>();

        File customConfigFile;
        FileConfiguration customConfig;
        customConfigFile = new File(plugin.getDataFolder(), "rewards.yml");

        //create file if it doesn't exist
        if(!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            plugin.saveResource("rewards.yml", false);
        }

        customConfig= new YamlConfiguration();
        try {
            customConfig.load(customConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        // === Start loading data ===

        ConfigurationSection rewardsCS = customConfig.getConfigurationSection("rewards");

        if(rewardsCS == null) return;
        for(String key : rewardsCS.getKeys(false)){


            // Parse requirements
            List<String> requirements = rewardsCS.getStringList(key + ".requirements");
            int req_totalVotes = 0;
            int req_monthlyVotes = 0;

            for (String req : requirements) {
                if (req.contains("{vote.total}")) {
                    Integer foundInt = Utils.findFirstNumberSequenceInString(req);
                    req_totalVotes = foundInt != -1 ? foundInt : 0;

                } else if (req.contains("{vote.month")) {
                    Integer foundInt = Utils.findFirstNumberSequenceInString(req);
                    req_monthlyVotes = foundInt != -1 ? foundInt : 0;
                }
            }
            Main.debug("[reward - " + key + "] req_monthly: " + req_monthlyVotes + ", req_total: " + req_totalVotes );

            ConfigurationSection rewardGUICS = rewardsCS.getConfigurationSection(key + ".gui");

            if(rewardGUICS == null) {
                Main.warning("Failed to create reward " + key + " - missing gui section");
                return;
            }

            Reward reward = new Reward(
                    key,
                    RewardType.valueOf(rewardsCS.getString(key + ".type", "ONETIME")),
                    req_totalVotes,
                    req_monthlyVotes,
                    rewardsCS.getBoolean(key + ".claimable", false),
                    rewardsCS.getStringList(key + ".commands"),
                    rewardGUICS.getString("name"),
                    rewardGUICS.getStringList("lore"),
                    Material.getMaterial(Objects.requireNonNull(rewardGUICS.getString("material", "BARRIER")))
                    );
            rewardArrayList.add(reward);
        }
    }

    public ArrayList<Reward> getVotingRewardArrayList() {
        return new ArrayList<>(rewardArrayList);
    }

    public ArrayList<PlayerRewardRecord> getPlayerRewardRecords(UUID playerID) {
        return votingRewardRecords.get(playerID);
    }

    public PlayerRewardRecord getPlayerRewardRecord(UUID playerID, String rewardID) {
        return votingRewardRecords.get(playerID).stream().filter(playerRewardRecord -> playerRewardRecord.getRewardID().equals(rewardID)).findAny().orElse(null);
    }
    public ArrayList<PlayerRewardRecord> getPlayerRewardRecords(UUID playerID, String rewardID) {
        return votingRewardRecords.get(playerID).stream().filter(prr -> prr.getRewardID().equals(rewardID)).collect(Collectors.toCollection(ArrayList::new));
    }


}

