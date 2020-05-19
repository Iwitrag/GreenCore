package cz.iwitrag.greencore.gameplay.treasurechests;

import cz.iwitrag.greencore.helpers.Percent;
import org.bukkit.inventory.ItemStack;

public class PossibleItemReward {

    private ItemStack item;
    private Percent chance;

    public PossibleItemReward(ItemStack item, Percent chance) {
        this.item = item;
        this.chance = chance.getNormalizedCopy();
    }

    public PossibleItemReward(PossibleItemReward possibleItemReward) throws IllegalArgumentException {
        if (possibleItemReward == null)
            throw new IllegalArgumentException("Cannot copy null PossibleItemReward");
        this.item = new ItemStack(possibleItemReward.item);
        this.chance = possibleItemReward.chance;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public Percent getChance() {
        return chance;
    }

    public void setChance(Percent chance) {
        this.chance = chance.getNormalizedCopy();
    }

    public boolean isSimilar(PossibleItemReward possibleItemReward) {
        return item.equals(possibleItemReward.item) && chance.equals(possibleItemReward.chance);
    }
}
