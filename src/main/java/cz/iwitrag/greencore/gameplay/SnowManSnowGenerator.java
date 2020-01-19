package cz.iwitrag.greencore.gameplay;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class SnowManSnowGenerator implements Listener {

    @EventHandler
    public void onSnowManClick(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        Entity clickedEntity = event.getRightClicked();
        if (clickedEntity.getType().equals(EntityType.SNOWMAN) &&
                event.getHand().equals(EquipmentSlot.HAND) &&
                itemInMainHand.getType().toString().toLowerCase().contains("shovel") &&
                itemInMainHand.getItemMeta() instanceof Damageable) {
            player.playSound(player.getLocation(), Sound.BLOCK_SNOW_BREAK, SoundCategory.BLOCKS, (float)1.0, (float)1.0);
            for (int i = 0; i < 10; i++)
                clickedEntity.getWorld().spawnParticle(Particle.SNOW_SHOVEL,
                        clickedEntity.getLocation(), 1, -0.5+Math.random(), Math.random()*2, -0.5+Math.random());
            player.getWorld().dropItemNaturally(clickedEntity.getLocation(), new ItemStack(Material.SNOWBALL, 1));
            Damageable damageable = (Damageable)itemInMainHand.getItemMeta();
            damageable.setDamage(damageable.getDamage()+1);
            itemInMainHand.setItemMeta((ItemMeta)damageable);
            if (damageable.getDamage() >= itemInMainHand.getType().getMaxDurability()) {
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, SoundCategory.BLOCKS, (float)1.0, (float)1.0);
                player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            }
        }
    }

}
