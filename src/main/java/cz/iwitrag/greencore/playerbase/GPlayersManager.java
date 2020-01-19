package cz.iwitrag.greencore.playerbase;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class GPlayersManager implements Listener {

    private static GPlayersManager instance;

    private Map<Player, GPlayer> allGPlayers = new HashMap<>();

    private GPlayersManager() {
        instance = this;
    }

    public static GPlayersManager getInstance() {
        if (instance == null)
            instance = new GPlayersManager();
        return instance;
    }

    public void loadGPlayer(Player bukkitPlayer) {
        allGPlayers.put(bukkitPlayer, new GPlayer(bukkitPlayer));
    }

    public void unloadGPlayer(Player bukkitPlayer) {
        allGPlayers.remove(bukkitPlayer);
    }

    public GPlayer getGPlayer(Player bukkitPlayer) {
        return allGPlayers.get(bukkitPlayer);
    }

    public GPlayer getGPlayer(String playerName, boolean ignoreCase) {
        for (Player bukkitPlayer : allGPlayers.keySet()) {
            if (ignoreCase && bukkitPlayer.getName().equalsIgnoreCase(playerName))
                return allGPlayers.get(bukkitPlayer);
            if (!ignoreCase && bukkitPlayer.getName().equals(playerName))
                return allGPlayers.get(bukkitPlayer);
        }
        return null;
    }

    public GPlayer getGPlayer(String playerName) {
        return getGPlayer(playerName, true);
    }
}
