package cz.iwitrag.greencore.playerbase;

import cz.iwitrag.greencore.helpers.DependenciesProvider;
import fr.xephi.authme.events.LoginEvent;
import fr.xephi.authme.events.LogoutEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class GPlayerListener implements Listener {

    public GPlayerListener() {
        for (Player bukkitPlayer : Bukkit.getOnlinePlayers()) {
            GPlayersManager.getInstance().loadGPlayer(bukkitPlayer);
        }
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        GPlayersManager.getInstance().loadGPlayer(event.getPlayer());
    }

    @EventHandler
    public void onLogout(LogoutEvent event) { // Covers /logout command only
        GPlayersManager.getInstance().unloadGPlayer(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (DependenciesProvider.getInstance().getAuthMe().isAuthenticated(event.getPlayer())) {
            GPlayersManager.getInstance().unloadGPlayer(event.getPlayer());
        }
    }

}
