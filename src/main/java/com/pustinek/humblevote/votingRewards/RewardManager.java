package com.pustinek.humblevote.votingRewards;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.utils.Callback;
import com.pustinek.humblevote.utils.ChatUtils;
import com.pustinek.humblevote.utils.Manager;
import com.pustinek.humblevote.utils.Utils;
import com.pustinek.humblevote.voteStatistics.PlayerVoteStats;
import com.pustinek.humblevote.voteStatistics.constants.MODIFICATION_TYPE;
import com.pustinek.humblevote.votingRewards.constants.AWARD_POSSIBILITIES;
import com.pustinek.humblevote.votingRewards.constants.REWARD_TYPE;
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
    private void giveRewardToPlayer(Player player, Reward reward, boolean removePoints) {
        // Replacement for strings
        HashMap<String, String> toReplace = new HashMap<>();
        toReplace.put("{player}", player.getName());
        toReplace.put("{player.uuid}", player.getUniqueId().toString());

        reward.getRewards().forEach(innerReward -> {
            String[] innerRewardSplit = innerReward.split(" ", 2);
            if(innerRewardSplit.length < 2) {
                return;
            }

            String startingWord = innerRewardSplit[0];
            String value = innerRewardSplit[1];

            if(startingWord.equalsIgnoreCase(AWARD_POSSIBILITIES.MONEY.toString())) {
                int i = Utils.findFirstNumberSequenceInString(innerReward);
                if(i < 0) {Main.warning("failed to parse " + reward.getId() + " <money> reward, skipping innerReward. "); return;}
                Economy economy = Main.getEconomy();
                if(economy != null)
                    economy.depositPlayer(player, i);
            }else if(startingWord.equalsIgnoreCase(AWARD_POSSIBILITIES.CMD.toString())) {
                final String finalCMD = Utils.replaceStuffInString(toReplace, value);
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), finalCMD);
            }else if(startingWord.equalsIgnoreCase(AWARD_POSSIBILITIES.MESSAGE.toString())) {
                String[] split = value.split(" ", 2);
                if(split.length < 2) {
                    return;
                }
                if(!split[0].equalsIgnoreCase("{player}")){
                    return;
                }

                HashMap<String, String> replaceMap = new HashMap<>();
                replaceMap.put("{player}", player.getName());
                replaceMap.put("{prefix}", Main.getConfigManager().getPluginMessagePrefix());
                replaceMap.put("{reward.id}", reward.getId());
                replaceMap.put("{reward.name}", reward.getGUIName());
                final String finalMessage = Utils.replaceStuffInString(replaceMap, split[1]);
                player.sendMessage(ChatUtils.chatColor(finalMessage));
            }else if(startingWord.equalsIgnoreCase(AWARD_POSSIBILITIES.BROADCAST.toString())) {
                String[] split = value.split(" ", 2);
                if(split.length < 2) {
                    return;
                }
                HashMap<String, String> replaceMap = new HashMap<>();
                replaceMap.put("{player}", player.getName());
                replaceMap.put("{prefix}", Main.getConfigManager().getPluginMessagePrefix());
                replaceMap.put("{reward.id}", reward.getId());
                replaceMap.put("{reward.name}", reward.getGUIName());

                final String finalMessage = Utils.replaceStuffInString(replaceMap, split[1]);
                Bukkit.broadcastMessage(ChatUtils.chatColor(finalMessage));

            }
        });
        PlayerVoteStats playerVoteStats = Main.getVoteStatisticsManager().getPlayerVoteStats(player.getUniqueId());
        if(reward.getReqVotingPoints() > 0 && playerVoteStats != null) {
            playerVoteStats.modifyVotePoints(MODIFICATION_TYPE.SUBTRACT, reward.getReqVotingPoints());
        }
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
        if(reward.getREWARDTYPE() == REWARD_TYPE.ONVOTE || !reward.isClaimable())   {
            Main.message(player,"rewards-notClaimable");
            return false;
        }

        if((reward.getREWARDTYPE().equals(REWARD_TYPE.MONTHLY)
                && reward.isPlayerAlreadyClaimed(player))
        ) {
            Main.message(player,"rewards-monthAlreadyClaimed");
            return false;
        }
        if((reward.getREWARDTYPE().equals(REWARD_TYPE.ONETIME)
                && reward.isPlayerAlreadyClaimed(player))
        ) {
            Main.message(player,"rewards-onetimeAlreadyClaimed");
            return false;
        }

        if(reward.isPlayerMeetsRequirements(player)) {
            giveRewardToPlayer(player, reward, true);
            rewardGivenSuccessfully(player,rewardID, Main.getTimeManager().getTimeInstant());
            return true;
        }

        Main.message(player,"rewards-requirementsNotMeet");
        return false;

    }

    public void processPlayerVoteReward(Player player) {
        rewardArrayList.forEach(vr -> {
            if(vr.getREWARDTYPE().equals(REWARD_TYPE.ONVOTE)){
                giveRewardToPlayer(player,vr, false);
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
        rewardArrayList.forEach(vr -> {

            if(vr.isClaimable()) return; //Reward is claimable, so ignore it

            if(
                    vr.getREWARDTYPE().equals(REWARD_TYPE.ONETIME) ||
                            vr.getREWARDTYPE().equals(REWARD_TYPE.MONTHLY) ||
                        vr.getREWARDTYPE().equals(REWARD_TYPE.ANYTIME))
            {

                if(playerRewardRecords.stream().anyMatch(o -> o.getRewardID().equals(vr.getId()))) return;


                // If reward == monthly -> check that he hasn't gotten it this month
                if(vr.getREWARDTYPE().equals(REWARD_TYPE.MONTHLY)) {
                    if(playerRewardRecords.stream().anyMatch(o -> o.getRewardID().equals(vr.getId()) &&
                            LocalDateTime.ofInstant(o.getTimestamp(), ZoneOffset.UTC).getMonth().equals(YearMonth.now().getMonth()))) return;
                }else if(vr.getREWARDTYPE().equals(REWARD_TYPE.ONETIME)) {
                    //  reward == onetime && was already given to the player so skip this loop
                    if(playerRewardRecords.stream().anyMatch(o -> o.getRewardID().equals(vr.getId()))) return;
                }

                // Check if requirements are meet
                if(vr.isPlayerMeetsRequirements(player)) {
                    giveRewardToPlayer(player,vr, true);
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

        customConfig = new YamlConfiguration();
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
            List<String> requirementsStringList = rewardsCS.getStringList(key + ".requirements");
            int requiredTotalVotes = 0;
            int requiredMonthlyVotes = 0;
            int requiredServerTotalVotes = 0;
            int requiredServerMonthlyVotes = 0;
            int requiredVotingPoints = 0;

            for (String req : requirementsStringList) {
                if(req.contains("{votes.server.total}")) {
                    int foundInt = Utils.findFirstNumberSequenceInString(req);
                    requiredServerTotalVotes = foundInt != -1 ? foundInt : 0;
                }else if(req.contains("{votes.server.month}")) {
                    int foundInt = Utils.findFirstNumberSequenceInString(req);
                    requiredServerMonthlyVotes = foundInt != -1 ? foundInt : 0;
                }else if (req.contains("{votes.total}")) {
                    int foundInt = Utils.findFirstNumberSequenceInString(req);
                    requiredTotalVotes = foundInt != -1 ? foundInt : 0;

                }else if (req.contains("{votes.month")) {
                    int foundInt = Utils.findFirstNumberSequenceInString(req);
                    requiredMonthlyVotes = foundInt != -1 ? foundInt : 0;
                }
                else if (req.contains("{votes.points")) {
                    int foundInt = Utils.findFirstNumberSequenceInString(req);
                    requiredVotingPoints = foundInt != -1 ? foundInt : 0;
                }
            }



            // Parse rewards
            List<String> innerRewardList = rewardsCS.getStringList(key + ".rewards");
            List<String> validInnerReward = new ArrayList<>();
            List<AWARD_POSSIBILITIES> innerRewardPossibilities = Arrays.asList(AWARD_POSSIBILITIES.values());
            innerRewardList.forEach(award -> {
                if(award.isEmpty()) return;
                String[] awardSplit = award.split(" ", 2);
                if(awardSplit.length < 2) {
                    Main.warning("[reward.yml][" + key + "] failed to parse award `" + award + "` invalid number of arguments.");
                    return;
                }
                String startingWord = awardSplit[0];
                boolean isValid = innerRewardPossibilities.stream().anyMatch(validFirstWord -> validFirstWord.toString().equalsIgnoreCase(startingWord));
                if(!isValid) {
                    Main.warning("[reward.yml][" + key + "] failed to parse award `" + award + "` invalid starting word (" + startingWord + ")." );
                    return;
                }



                validInnerReward.add(award);
            });


            RewardRequirements requirements = new RewardRequirements(requiredMonthlyVotes, requiredTotalVotes, requiredServerMonthlyVotes, requiredServerTotalVotes, requiredVotingPoints);

            // Parse GUI section
            ConfigurationSection rewardGUICS = rewardsCS.getConfigurationSection(key + ".gui");

            if(rewardGUICS == null) {
                Main.warning("Failed to create reward " + key + " - missing gui section");
                return;
            }


            Reward reward = new Reward(
                    key,
                    rewardsCS.getBoolean(key + ".enabled", false),
                    REWARD_TYPE.valueOf(rewardsCS.getString(key + ".type", "ONETIME")),
                    rewardsCS.getBoolean(key + ".claimable", false),
                    requirements,
                    validInnerReward,
                    rewardGUICS.getString("name", "NULL"),
                    rewardGUICS.getStringList("lore"),
                    Material.getMaterial(Objects.requireNonNull(rewardGUICS.getString("material", "BARRIER"))),
                    rewardGUICS.getBoolean("display_in_menu", true)
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

