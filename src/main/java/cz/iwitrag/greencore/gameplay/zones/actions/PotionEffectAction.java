package cz.iwitrag.greencore.gameplay.zones.actions;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("pot")
public class PotionEffectAction extends Action {

    @Column(name = "pot_potionEffect")
    private PotionEffect potionEffect;

    public PotionEffectAction() {}

    public PotionEffectAction(PotionEffectType type, int amplifier, int duration) {
        this.potionEffect = new PotionEffect(type, duration*20, amplifier, false, false);
        validateParameters();
    }

    @Override
    public String getDescription() {
        if (potionEffect.getAmplifier() == -1 || potionEffect.getDuration() == 0)
            return "Odstraní efekt lektvaru " + potionEffect.getType().getName();
        else
            return "Nastaví efekt lektvaru " + potionEffect.getType().getName() + " " + (potionEffect.getAmplifier()+1) + " na dobu " + potionEffect.getDuration()/20 + " sek.";
    }

    @Override
    public void execute(Player player) {
        // remove potion effect
        if (potionEffect.getAmplifier() == -1 || potionEffect.getDuration() == 0) {
            player.removePotionEffect(potionEffect.getType());
            return;
        }

        // add potion effect, duration will be overwritten (if smaller), does nothing if current amplifier is higher
        PotionEffect currentPotionEffect = player.getPotionEffect(potionEffect.getType());
        if (currentPotionEffect == null ||
                currentPotionEffect.getAmplifier() < potionEffect.getAmplifier() ||
                (currentPotionEffect.getAmplifier() == potionEffect.getAmplifier() &&
                        currentPotionEffect.getDuration() < potionEffect.getDuration())) {
            player.removePotionEffect(potionEffect.getType());
            player.addPotionEffect(potionEffect, true);
        }
    }

    @Override
    public Action copy() {
        return new PotionEffectAction(this.potionEffect.getType(), this.potionEffect.getAmplifier(), this.potionEffect.getDuration());
    }

    private PotionEffectType getPotionEffectType() {
        return potionEffect.getType();
    }

    private void setPotionEffectType(PotionEffectType type) {
        potionEffect = potionEffect.withType(type);
        validateParameters();
    }

    private int getAmplifier() {
        return potionEffect.getAmplifier();
    }

    private void setAmplifier(int amplifier) {
        potionEffect = potionEffect.withAmplifier(amplifier);
        validateParameters();
    }

    private int getDuration() {
        return potionEffect.getDuration();
    }

    private void setDuration(int duration) {
        potionEffect.withDuration(duration);
        validateParameters();
    }

    protected void validateParameters() {
        if (potionEffect.getAmplifier() < -1)
            potionEffect = potionEffect.withAmplifier(-1);
        if (potionEffect.getDuration() < 0)
            potionEffect = potionEffect.withDuration(0);
    }
}
