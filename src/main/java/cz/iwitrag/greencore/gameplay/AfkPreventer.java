package cz.iwitrag.greencore.gameplay;

import cz.iwitrag.greencore.Main;
import cz.iwitrag.greencore.helpers.TaskChainHelper;
import cz.iwitrag.greencore.helpers.LuckPermsHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class AfkPreventer implements Listener {

    private static final int MAX_MINUTES_AFK = 10;

    private Map<Player, Double> afkScores = new HashMap<>();
    private Map<Player, Location> lastLocs = new HashMap<>();

    public AfkPreventer() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), this::checkAfkPlayers, 200, 200);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        afkScores.remove(event.getPlayer());
        lastLocs.remove(event.getPlayer());
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // Player is not afk when chatting
        afkScores.remove(event.getPlayer());
        lastLocs.remove(event.getPlayer());
    }

    private void checkAfkPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            TaskChainHelper.newChain()
            // Don't check players with permission
            .asyncFirst(() -> LuckPermsHelper.playerHasPermission(player.getName(), "afkpreventer.bypass"))
            .abortIf(true)
            .sync(() -> {
                // Newly checked player
                if (!afkScores.containsKey(player)) {
                    afkScores.put(player, 0.0);
                    lastLocs.put(player, player.getLocation());
                } else {
                    // Player is still afk
                    if (isConsideredAfk(player.getLocation(), lastLocs.get(player))) {
                        double currentScore = afkScores.get(player) + 1.0;
                        // Player is afk for too long, kick
                        if (currentScore >= MAX_MINUTES_AFK*6) {
                            player.kickPlayer("§cByl/a si odpojen/a za neaktivitu delší než " + MAX_MINUTES_AFK + " min.");
                            afkScores.remove(player);
                            lastLocs.remove(player);
                            // Increase afk score
                        } else {
                            afkScores.put(player, currentScore);
                            lastLocs.put(player, player.getLocation());
                        }
                        // Player moved, reset his afk score
                    } else {
                        afkScores.remove(player);
                        lastLocs.remove(player);
                    }
                }
            })
            .execute();
        }
    }

    private boolean isConsideredAfk(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null)
            return false;

        if (loc1.getWorld() != null && loc1.getWorld() != loc2.getWorld())
            return false;

        return (loc1.distance(loc2) < 16.0 && loc1.getYaw() == loc2.getYaw() && loc1.getPitch() == loc2.getPitch());
    }

}
