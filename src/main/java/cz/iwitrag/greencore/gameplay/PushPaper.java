package cz.iwitrag.greencore.gameplay;

import cz.iwitrag.greencore.helpers.LuckPermsHelper;
import cz.iwitrag.greencore.helpers.StringHelper;
import cz.iwitrag.greencore.helpers.TaskChainHelper;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PushPaper implements Listener {

    // TODO - pushpaper target, type of entities, radius, power

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() != null && event.getItem().getType().equals(Material.PAPER) && event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            if (event.getItem().hasItemMeta() && StringHelper.anyStringContains(event.getItem().getItemMeta().getLore(), "odhazovatko", true)) {
                TaskChainHelper.newChain()
                .asyncFirst(() -> LuckPermsHelper.playerHasPermission(event.getPlayer().getName(), "odhazovatko"))
                .abortIf(false)
                .sync(() -> {
                    for (Entity entity : event.getPlayer().getNearbyEntities(20.0, 20.0, 20.0)) {
                        if (entity instanceof Player && !entity.equals(event.getPlayer())) {
                            TaskChainHelper.newChain()
                            .asyncFirst(() -> LuckPermsHelper.playerHasPermission(entity.getName(), "odhazovatko.bypass"))
                            .abortIf(true)
                            .sync(() -> entity.setVelocity(event.getPlayer().getLocation().getDirection().multiply(2)))
                            .execute();
                        }
                    }
                })
                .execute();
            }
        }
    }

}
