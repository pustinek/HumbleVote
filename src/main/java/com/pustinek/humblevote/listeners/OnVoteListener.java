package com.pustinek.humblevote.listeners;

import com.pustinek.humblevote.Main;
import com.pustinek.humblevote.voteSites.VoteSite;
import com.pustinek.humblevote.voting.QueuedVote;
import com.pustinek.humblevote.voting.VoteManager;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class OnVoteListener implements Listener {

    private final Main plugin;

    public OnVoteListener(Main plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onVoteEvent(VotifierEvent event) {
        Vote vote = event.getVote();
        final String voteSite = vote.getServiceName();
        final String voteUsername = vote.getUsername().trim();

        if (voteUsername.length() == 0) {
            Main.debug("No specified name from vote on " + voteSite);
            return;
        }

                // Check if the vote sent here is fake / not specified in the voteSites.yml
                VoteSite vs = Main.getVoteSitesManager().getVoteSites().stream().filter(voteSite1 -> voteSite1.getService_site().equalsIgnoreCase(vote.getServiceName())).findAny().orElse(null);
                boolean isFakeVote = false;


                if(vs == null) {
                    Main.debug("VotingSite with the service name (" + vote.getServiceName() + ") not specified, and is being processed as fake VOTE");
                    isFakeVote = true;
                } else if (!vs.isEnabled()) {
                    Main.debug("VotingSite with the service name (" + vote.getServiceName() + ") is disabled in configs, ignoring VOTE");
                    return;
                }

                if(!Main.getConfigManager().isProcessFakeVotes() && isFakeVote) {
                    Main.debug("Vote is being discarded as it's FAKE");
                    return;
                }

                LocalDateTime localTime = LocalDateTime.now();
                long localEpoch = localTime.toEpochSecond(ZoneOffset.UTC);

                QueuedVote queuedVote = new QueuedVote(
                    vote.getAddress(),
                    vote.getServiceName(),
                    vote.getUsername(),
                    vote.getTimeStamp(),
                       Long.toString(localEpoch),
                        isFakeVote
                );

             Main.debug(
                     "Vote was received::: address:  " +
                             queuedVote.getAddress() +
                             ", service_name: " +
                             queuedVote.getServiceName() +
                    ", voted by username " + queuedVote.getUsername() +
                            ", and was processed as " + (isFakeVote ? "FAKE" : "TRUSTWORTHY")
            );



                VoteManager.receivedVotes.getAndIncrement();
                // Set cache index, it's just being used for reference when saving to database
                queuedVote.setCacheIndex(VoteManager.receivedVotes.get());

                Player player = Bukkit.getPlayer(vote.getUsername());

                if(player != null && player.isOnline()) {
                    //Process vote
                    Main.getVoteManager().processPlayerVote(queuedVote, player);

                }else{
                    Main.getVoteManager().addVoteToCache(queuedVote);
                }
    }
}
