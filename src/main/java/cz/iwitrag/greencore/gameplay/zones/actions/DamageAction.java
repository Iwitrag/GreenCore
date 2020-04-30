package cz.iwitrag.greencore.gameplay.zones.actions;

import org.bukkit.entity.Player;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("dmg")
public class DamageAction extends Action {

    @Column(name = "dmg_damage")
    private int damage;

    public DamageAction() {}

    public DamageAction(int damage) {
        this.damage = damage;
    }

    @Override
    public String getDescription() {
        if (damage >= 0)
            return "Zraní hráče o " + damage + " (" + ((double)damage)/2 + " srdíček)";
        else
            return "Uzdraví hráče o " + -damage + " (" + ((double)-damage)/2 + " srdíček)";
    }

    @Override
    public void execute(Player player) {
        if (damage >= 0)
            player.damage(damage);
        else
            player.setHealth(player.getHealth() - damage);
    }

    @Override
    public Action copy() {
        return new DamageAction(this.damage);
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }
}
