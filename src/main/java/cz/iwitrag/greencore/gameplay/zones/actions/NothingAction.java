package cz.iwitrag.greencore.gameplay.zones.actions;

import org.bukkit.entity.Player;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("not")
public class NothingAction extends Action {

    public NothingAction() { }

    @Override
    public String getDescription() {
        return "Žádná akce";
    }

    @Override
    public void execute(Player player) { }

    @Override
    public Action copy() {
        return new NothingAction();
    }
}
