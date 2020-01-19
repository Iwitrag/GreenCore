package cz.iwitrag.greencore.auth;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.HashSet;
import java.util.Set;

public class JoinTwice implements Listener {

    private Set<String> verifiedPlayers = new HashSet<>();

    public JoinTwice() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            verifiedPlayers.add(player.getName());
        }
    }

    @EventHandler
    public void onPlayerPrelogin(AsyncPlayerPreLoginEvent event) {
        if (!verifiedPlayers.contains(event.getName())) {
            verifiedPlayers.add(event.getName());
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§aPřipoj se ještě jednou, prosím :)");
        }
    }

}
