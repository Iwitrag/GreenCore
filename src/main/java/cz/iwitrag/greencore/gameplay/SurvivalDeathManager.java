package cz.iwitrag.greencore.gameplay;

import cz.iwitrag.greencore.helpers.LuckPermsHelper;
import cz.iwitrag.greencore.helpers.PlayerHeadManager;
import cz.iwitrag.greencore.helpers.StringHelper;
import cz.iwitrag.greencore.helpers.TaskChainHelper;
import cz.iwitrag.greencore.helpers.WorldManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class SurvivalDeathManager implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        World playerWorld = player.getWorld();
        if (WorldManager.isSurvivalWorld(playerWorld)) {
            Location deathLoc = player.getLocation();
            player.sendMessage("§cUmřel/a si :( §2Souřadnice §7[ " + StringHelper.locationToString(deathLoc, true, "§2", "§a") + " §7]§2.");
            TaskChainHelper.newChain()
            .asyncFirst(() -> LuckPermsHelper.playerHasPermission(player.getName(), "drophead.bypass"))
            .abortIf(true)
            .sync(() -> {
                ItemStack playerHead = PlayerHeadManager.getPlayerHead(player);
                if (playerHead != null) {
                    playerWorld.dropItem(deathLoc, playerHead);
                }
            })
            .execute();
        }
    }
}
