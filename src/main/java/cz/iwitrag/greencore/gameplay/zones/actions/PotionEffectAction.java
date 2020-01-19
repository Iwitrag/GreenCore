package cz.iwitrag.greencore.gameplay.zones.actions;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionEffectAction extends Action {

    private PotionEffect potionEffect;

    public PotionEffectAction(PotionEffectType type, int amplifier, int duration) {
        this.potionEffect = new PotionEffect(type, duration, amplifier, false, false);
        validateParameters();
    }

    @Override
    public String getDescription() {
        if (potionEffect.getAmplifier() == -1 || potionEffect.getDuration() == 0)
            return "Odstraní efekt lektvaru " + potionEffect.getType();
        else
            return "Nastaví efekt lektvaru " + potionEffect.getType() + " " + potionEffect.getAmplifier()+1 + " na dobu " + potionEffect.getDuration() + " sek.";
    }

    @Override
    public void execute(Player player) {
        PotionEffect currentPotionEffect = player.getPotionEffect(potionEffect.getType());
        if (potionEffect.getAmplifier() == -1 || potionEffect.getDuration() == 0) {
            player.removePotionEffect(potionEffect.getType());
        }
        else if (currentPotionEffect == null ||
                currentPotionEffect.getAmplifier() < potionEffect.getAmplifier() ||
                (currentPotionEffect.getAmplifier() == potionEffect.getAmplifier() &&
                        currentPotionEffect.getDuration() < potionEffect.getDuration())) {
            player.removePotionEffect(potionEffect.getType());
            player.addPotionEffect(potionEffect, true);
        }
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

    private void validateParameters() {
        if (potionEffect.getAmplifier() < -1)
            potionEffect = potionEffect.withAmplifier(-1);
        if (potionEffect.getDuration() < 0)
            potionEffect = potionEffect.withDuration(0);
    }
}
