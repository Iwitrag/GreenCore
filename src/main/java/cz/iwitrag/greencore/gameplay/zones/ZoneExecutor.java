package cz.iwitrag.greencore.gameplay.zones;

import cz.iwitrag.greencore.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class ZoneExecutor {

    private static ZoneExecutor instance;

    private Set<ZoneExecution> executions = new HashSet<>();

    private ZoneExecutor() {
        // Check every second for players inside zones
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                for (Zone zone : ZoneManager.getInstance().getZones()) {
                    if (player.getWorld().equals(zone.getPoint1().getWorld()) &&
                            player.getLocation().getX() >= zone.getPoint1().getX() &&
                            player.getLocation().getX() <= zone.getPoint2().getX() &&
                            player.getLocation().getY() >= zone.getPoint1().getY() &&
                            player.getLocation().getY() <= zone.getPoint2().getY() &&
                            player.getLocation().getZ() >= zone.getPoint1().getZ() &&
                            player.getLocation().getZ() <= zone.getPoint2().getZ() &&
                            !isZoneBeingExecutedByPlayer(zone, player))
                        executions.add(new ZoneExecution(zone, player));
                }
            }
        }, 20, 20);

        // Process action sequences
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> executions.removeIf(execution -> !execution.doTick()), 1, 1);
    }

    public static ZoneExecutor getInstance() {
        if (instance == null)
            instance = new ZoneExecutor();
        return instance;
    }

    public boolean isZoneBeingExecuted(Zone zone) {
        for (ZoneExecution execution : executions) {
            if (execution.getZone().equals(zone))
                return true;
        }
        return false;
    }

    public boolean isZoneBeingExecutedByPlayer(Zone zone, Player player) {
        for (ZoneExecution execution : executions) {
            if (execution.getZone().equals(zone) && execution.getPlayer().equals(player))
                return true;
        }
        return false;
    }

}
