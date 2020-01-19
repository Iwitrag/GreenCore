package cz.iwitrag.greencore.gameplay;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Recipe;

import java.util.Iterator;

public class RecipeRevealer implements Listener {

    public RecipeRevealer() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            revealAllRecipes(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        revealAllRecipes(event.getPlayer());
    }

    public void revealAllRecipes(Player player) {
        Iterator<Recipe> it = Bukkit.getServer().recipeIterator();
        while (it.hasNext()) {
            Recipe recipe = it.next();
            if ((recipe instanceof Keyed)) { // is namespaced, example: "minecraft.torch"
                player.discoverRecipe(((Keyed)recipe).getKey());
            }
        }
    }
}
