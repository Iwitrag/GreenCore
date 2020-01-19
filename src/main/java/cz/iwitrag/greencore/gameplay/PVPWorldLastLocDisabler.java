package cz.iwitrag.greencore.gameplay;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PVPWorldLastLocDisabler implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer().getWorld().getName().equalsIgnoreCase("pvp")) {
            World world = Bukkit.getWorld("world");
            if (world != null)
                event.getPlayer().teleport(world.getSpawnLocation());
        }
    }

}
