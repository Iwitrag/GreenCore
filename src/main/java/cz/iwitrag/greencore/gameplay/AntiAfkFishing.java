package cz.iwitrag.greencore.gameplay;

import cz.iwitrag.greencore.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// Prevents AFK fishing - https://www.youtube.com/watch?v=hTAHK2XnpQs
public class AntiAfkFishing implements Listener {

    private static final int MAX_CLICKS_PER_10_SECONDS = 10;

    private Map<Player, Double> clickPoints = new HashMap<>();

    public AntiAfkFishing() {
        if (MAX_CLICKS_PER_10_SECONDS > 0) {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), this::reduceClickPoints, 20, 20);
        }
    }

    @EventHandler
    public void onRightClickWithFishingRod(PlayerInteractEvent event) {
        if (event.getItem() != null && event.getItem().getType().equals(Material.FISHING_ROD) &&
                Arrays.asList(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK).contains(event.getAction())) {
            double currentClickPoints = clickPoints.getOrDefault(event.getPlayer(), 0.0) + 1.0;
            if (currentClickPoints > MAX_CLICKS_PER_10_SECONDS) {
                event.getPlayer().sendMessage("§7Používáš prut příliš rychle, zkus počkat " + (int)((currentClickPoints / (MAX_CLICKS_PER_10_SECONDS/10.0))-10) + " s.");
                event.setCancelled(true);
            }
            if (currentClickPoints < (MAX_CLICKS_PER_10_SECONDS * 2))
                clickPoints.put(event.getPlayer(), currentClickPoints);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        clickPoints.remove(event.getPlayer());
    }

    private void reduceClickPoints() {
        for (Iterator<Player> iterator = clickPoints.keySet().iterator(); iterator.hasNext(); ) {
            Player player = iterator.next();
            Double currentClickPoints = clickPoints.get(player);
            Double decrement = MAX_CLICKS_PER_10_SECONDS/10.0;
            if (currentClickPoints <= decrement)
                iterator.remove();
            else
                clickPoints.put(player, currentClickPoints-decrement);
        }
    }

}
