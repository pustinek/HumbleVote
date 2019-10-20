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

import java.util.Iterator;

public class OnVoteListener implements Listener {

    private final Main plugin;

    public OnVoteListener(Main plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onVoteEvent(VotifierEvent event) {
        Vote vote = event.getVote();
        final String voteSite = vote.getServiceName();
        final String IP = vote.getAddress();
        final String voteUsername = vote.getUsername().trim();

        if (voteUsername.length() == 0) {
            plugin.getLogger().warning("No name from vote on " + voteSite);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {

                // check if vote is valid before processing it further

                    // Check if the vote sent here is fake / not specified in the voteSites.yml
                    Iterator<VoteSite> iterator = Main.getVoteSitesManager().getVoteSites().iterator();
                    boolean isFakeVote = true;
                    for (Iterator<VoteSite> it = iterator; it.hasNext();) {
                        VoteSite vs = it.next();
                        //Main.debug("=== Comparing -> " + vs.getService_site() +" | " + vote.getServiceName());
                        if(vs.isEnabled() && vs.getService_site().equalsIgnoreCase(vote.getServiceName())) isFakeVote = false;
                    }

                    QueuedVote queuedVote = new QueuedVote(
                        vote.getAddress(),
                        vote.getServiceName(),
                        vote.getUsername(),
                        vote.getTimeStamp(),
                            isFakeVote
                );


                    /*Main.debug(
                        "Vote was received from website " +
                                queuedVote.getAddress()+
                        ", voted by username " + queuedVote.getUsername() +
                                ", and was processed as " + (isFakeVote ? "FAKE" : "TRUSTWORTHY")
                );*/

                if(!Main.getConfigManager().isProccessFakeVotes() && isFakeVote) {
                    Main.debug("Vote is being discarded as it's a FAKE vote !" );
                    return;
                }

                VoteManager.receivedVotes++;
                queuedVote.setCacheIndex(VoteManager.receivedVotes);

                Player player = Bukkit.getPlayer(vote.getUsername());
                if(player != null && player.isOnline()) {
                    //Process vote
                    Main.getVoteManager().processPlayerVote(queuedVote, player);
                }else{
                    Main.getVoteManager().addVoteToCache(queuedVote);
                }
            }
        });

    }


}
