package cz.iwitrag.greencore.gameplay.zones.actions;

import org.bukkit.entity.Player;

public class NothingAction extends Action {
    @Override
    public String getDescription() {
        return "Žádná akce";
    }

    @Override
    public void execute(Player player) {

    }

    @Override
    public Action copy() {
        return new NothingAction();
    }
}
