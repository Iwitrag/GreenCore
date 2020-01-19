package cz.iwitrag.greencore.gameplay.zones;

import cz.iwitrag.greencore.gameplay.zones.actions.Action;
import org.bukkit.entity.Player;

import java.util.List;

public class ZoneExecution {

    private int currentTick;
    private Zone zone;
    private Player player;

    public ZoneExecution(Zone zone, Player player) {
        this.currentTick = -1;
        this.zone = zone;
        this.player = player;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public Zone getZone() {
        return zone;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean doTick() {
        currentTick++;
        List<Action> actions = zone.getActions();
        int maxPossibleTick = -1;
        for (int i = 0; i < actions.size(); i++) {
            Action action = actions.get(i);
            if (currentTick == action.getTime()) {
                action.execute(player);
            }
            if (action.getTime() > maxPossibleTick)
                maxPossibleTick = action.getTime();
        }
        return currentTick < maxPossibleTick;
    }


}
