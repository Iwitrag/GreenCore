package cz.iwitrag.greencore.votifier;

import com.vexsoftware.votifier.model.VotifierEvent;
import cz.iwitrag.greencore.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class VotifierListener implements Listener {

    // TODO - czech-craft vote counting, probably into GPlayer
    // TODO - czech-craft vote rewards

    @EventHandler
    public void onVote(VotifierEvent event) {
        String playerWhoVotedName = event.getVote().getUsername();
        String serviceName = event.getVote().getServiceName();

        Main.getInstance().getLogger().info("Player " + playerWhoVotedName + " voted for server on service " + serviceName);
        VoteProcessersManager.getRegisteredVoteProcesser(serviceName).playerVoted(playerWhoVotedName);
    }
}
