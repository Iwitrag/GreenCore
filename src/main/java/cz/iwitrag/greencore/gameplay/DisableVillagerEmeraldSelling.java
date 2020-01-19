package cz.iwitrag.greencore.gameplay;

import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;

public class DisableVillagerEmeraldSelling implements Listener {

    @EventHandler
    public void onPlayerMerchantOpen(InventoryOpenEvent event) {
        if (event.getInventory() instanceof MerchantInventory) {
            Merchant merchant = ((MerchantInventory) event.getInventory()).getMerchant();
            List<MerchantRecipe> recipes = new ArrayList<>(merchant.getRecipes());
            int sizeBefore = recipes.size();
            recipes.removeIf(recipe -> recipe.getResult().getType().equals(Material.EMERALD));
            merchant.setRecipes(recipes);

            // Avoid opening empty trade window, better to just not open it and next time villager will turn his head
            if (recipes.size() != sizeBefore && recipes.size() == 0)
                event.setCancelled(true);

            // If no recipes are present, disable villager's profession to allow reuse
            if (recipes.size() == 0 && merchant instanceof Villager)
                restartVillager((Villager) merchant);
        }
    }

    @EventHandler
    public void onPlayerVillagerInteract(PlayerInteractEntityEvent event) {
        // If no recipes are present, disable villager's profession to allow reuse
        if (event.getRightClicked() instanceof Villager) {
            Villager villager = (Villager) event.getRightClicked();
            if (villager.getRecipeCount() == 0)
                restartVillager(villager);
        }
    }

    private void restartVillager(Villager villager) {
        villager.setProfession(Villager.Profession.NONE);
        villager.setVillagerExperience(0);
        villager.setVillagerLevel(1);
    }
}
