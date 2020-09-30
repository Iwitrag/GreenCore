package cz.iwitrag.greencore.gameplay.treasurechests;

import cz.iwitrag.greencore.helpers.Percent;
import org.bukkit.inventory.ItemStack;

import javax.persistence.*;

@Entity
@Table(name="TChest_reward")
public class PossibleItemReward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(nullable = false, length = 5000)
    private ItemStack item;

    @Column(nullable = false)
    private Percent chance;

    public PossibleItemReward() {}

    public PossibleItemReward(ItemStack item, Percent chance) {
        this.item = item;
        this.chance = chance.getNormalizedCopy();
    }

    public PossibleItemReward(PossibleItemReward possibleItemReward) throws IllegalArgumentException {
        if (possibleItemReward == null)
            throw new IllegalArgumentException("Cannot copy null PossibleItemReward");
        this.id = null;
        this.item = new ItemStack(possibleItemReward.item);
        this.chance = possibleItemReward.chance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
