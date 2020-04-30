package cz.iwitrag.greencore.gameplay;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class EventItemRemover implements Listener {

    // TODO - permission to bypass event items deletion on port
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getTo() == null) return;
        World fromWorld = event.getFrom().getWorld();
        World toWorld = event.getTo().getWorld();
        if (fromWorld != null && fromWorld.getName().equalsIgnoreCase("pvp") && (toWorld != null && !toWorld.getName().equalsIgnoreCase("pvp"))) {
            boolean deletedSomething = false;
            PlayerInventory playerInventory = event.getPlayer().getInventory();

            if (isEventItem(event.getPlayer().getItemOnCursor())) {
                event.getPlayer().setItemOnCursor(null);
                deletedSomething = true;
            }
            if (isEventItem(playerInventory.getItemInOffHand())) {
                playerInventory.setItemInOffHand(null);
                deletedSomething = true;
            }
            if (isEventItem(playerInventory.getHelmet())) {
                playerInventory.setHelmet(null);
                deletedSomething = true;
            }
            if (isEventItem(playerInventory.getChestplate())) {
                playerInventory.setChestplate(null);
                deletedSomething = true;
            }
            if (isEventItem(playerInventory.getLeggings())) {
                playerInventory.setLeggings(null);
                deletedSomething = true;
            }
            if (isEventItem(playerInventory.getBoots())) {
                playerInventory.setBoots(null);
                deletedSomething = true;
            }
            for (ItemStack item : playerInventory.getContents()) {
                if (isEventItem(item)) {
                    playerInventory.remove(item);
                    deletedSomething = true;
                }
            }
            if (deletedSomething) {
                event.getPlayer().updateInventory();
                event.getPlayer().sendMessage("§aVěci z eventu byly odstraněny z tvého inventáře");
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!event.getPlayer().getWorld().getName().equalsIgnoreCase("pvp") && isEventItem(event.getItemDrop().getItemStack())) {
            event.getItemDrop().remove();
        }
    }

    @EventHandler
    public void onEnderChestItemClick(InventoryClickEvent event) {
        if (isEventItem(event.getCurrentItem())) {
            if (event.getInventory().getType() == InventoryType.ENDER_CHEST)
                event.setCancelled(true);
        }
    }

    private boolean isEventItem(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore != null) {
                List<String> eventLores = Arrays.asList(
                        "eventový předmět",
                        "eventovy předmět",
                        "event předmět",
                        "eventový predmet",
                        "eventovy predmet",
                        "event predmet",
                        "eventový item",
                        "eventovy item",
                        "event item");
                for (String str : lore) {
                    str = str.toLowerCase();
                    for (String eventLore : eventLores) {
                        if (str.contains(eventLore))
                            return true;
                    }
                }
            }
        }
        return false;
    }
}
