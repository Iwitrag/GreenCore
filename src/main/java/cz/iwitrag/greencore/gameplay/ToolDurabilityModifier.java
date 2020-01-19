package cz.iwitrag.greencore.gameplay;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

import static cz.iwitrag.greencore.helpers.Utils.chance;

public class ToolDurabilityModifier implements Listener {

    @EventHandler
    public void onToolDamage(PlayerItemDamageEvent event) {
        ItemMeta meta = event.getItem().getItemMeta();
        if (meta instanceof Damageable && !event.getItem().getType().equals(Material.ELYTRA)) { // Elytra cannot break
            Damageable damageable = (Damageable) meta;
            damageable.setDamage(damageable.getDamage() + event.getDamage());

            if (chance(50.00))
                damageable.setDamage(damageable.getDamage() + event.getDamage());

            if (Arrays.asList("sword", "bow", "shield", "trident")
                    .contains(event.getItem().getType().toString()))
                damageable.setDamage(damageable.getDamage() + event.getDamage());

            if (Arrays.asList("helmet", "chestplate", "leggings", "boots")
                    .contains(event.getItem().getType().toString()))
                damageable.setDamage(damageable.getDamage() + event.getDamage()*2);

            event.getItem().setItemMeta((ItemMeta)damageable);
        }
    }

    // TODO - change player's pickaxe durability usage based on his mining level
}
