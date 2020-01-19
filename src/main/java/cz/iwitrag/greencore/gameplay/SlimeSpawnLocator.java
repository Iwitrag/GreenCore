package cz.iwitrag.greencore.gameplay;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class SlimeSpawnLocator implements Listener {

    @EventHandler
    public void onSlimeBallClick(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getItem() != null && event.getItem().getType() == Material.SLIME_BALL) {
                boolean isSwamp = event.getPlayer().getWorld().getBiome(event.getPlayer().getLocation().getBlockX(),
                        event.getPlayer().getLocation().getBlockZ()).name().toLowerCase().contains("swamp");
                boolean isSlimeChunk = event.getPlayer().getLocation().getChunk().isSlimeChunk();
                if (!isSwamp) {
                    if (isSlimeChunk)
                        event.getPlayer().sendMessage("§aTady se slime spawnuje, §7protože to §aje slime chunk§7, ale není to bažina");
                    else
                        event.getPlayer().sendMessage("§7Tady se slime nespawnuje, protože to není slime chunk a ani bažina");
                } else {
                    if (isSlimeChunk)
                        event.getPlayer().sendMessage("§aTady se slime spawnuje, §7protože to §aje bažina §7a zároveň to §aje slime chunk");
                    else
                        event.getPlayer().sendMessage("§aTady se slime spawnuje, §7protože to §aje bažina§7, ale není to slime chunk");
                }
            }
        }
    }
}
