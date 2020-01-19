package cz.iwitrag.greencore.gameplay;

import cz.iwitrag.greencore.Main;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class DisablePhantoms implements Listener {

    // Every Minecraft day - TIME_SINCE_REST is reset to zero for everyone to prevent Phantom spawn
    public DisablePhantoms() {
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.setStatistic(Statistic.TIME_SINCE_REST, 0);
            }
        }, 0, 24000);
    }

    // When player joins, his TIME_SINCE_REST is reset to zero - because he can miss BukkitRunnable above
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().setStatistic(Statistic.TIME_SINCE_REST, 0);
    }

    // Anyway - if any phantom spawns naturally, it will block it, but this should not happen
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof Phantom && event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
            Main.getInstance().getLogger().warning("Phantom spawned naturally!");
            event.setCancelled(true);
        }
    }

}
