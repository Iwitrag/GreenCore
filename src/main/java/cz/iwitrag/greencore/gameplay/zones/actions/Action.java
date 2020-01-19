package cz.iwitrag.greencore.gameplay.zones.actions;

import org.bukkit.entity.Player;

public abstract class Action {

    private int time = 0;

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
        if (this.time < 0)
            this.time = 0;
    }

    public abstract String getDescription();

    public abstract void execute(Player player);
}
