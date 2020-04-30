package cz.iwitrag.greencore.gameplay.zones;

import cz.iwitrag.greencore.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ZoneExecutor {

    private static ZoneExecutor instance;

    private Set<ZoneExecution> executions = new HashSet<>();

    // Half second cooldown between executions
    private Map<ZoneExecution, Integer> cooldowns = new HashMap<>();

    private ZoneExecutor() {

        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> {

            // Check for players inside zones
            for (Player player : Bukkit.getOnlinePlayers()) {
                for (Zone zone : ZoneManager.getInstance().getZones()) {
                    if (ZoneManager.getInstance().isPlayerInsideZone(player, zone) && !isZoneBeingExecutedByPlayer(zone, player) && !isCooldown(zone, player))
                        executions.add(new ZoneExecution(zone, player));
                }
            }

            // Decrease cooldowns
            for (Iterator<ZoneExecution> iterator = cooldowns.keySet().iterator(); iterator.hasNext(); ) {
                ZoneExecution execution = iterator.next();
                int remaining = cooldowns.get(execution);
                remaining--;
                if (remaining == 0)
                    iterator.remove();
                else
                    cooldowns.put(execution, remaining);
            }

            // Process action sequences
            for (Iterator<ZoneExecution> iterator = executions.iterator(); iterator.hasNext(); ) {
                ZoneExecution execution = iterator.next();
                if (!execution.doTick()) {
                    iterator.remove();
                    cooldowns.put(execution, 10);
                }
            }
        }, 1, 1);
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

    public boolean isCooldown(Zone zone, Player player) {
        for (ZoneExecution execution : cooldowns.keySet()) {
            if (execution.getZone().equals(zone) && execution.getPlayer().equals(player))
                return true;
        }
        return false;
    }

}
