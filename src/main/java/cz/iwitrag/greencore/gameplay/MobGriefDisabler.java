package cz.iwitrag.greencore.gameplay;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Arrays;

public class MobGriefDisabler implements Listener {

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (Arrays.asList(EntityType.CREEPER, EntityType.WITHER_SKULL, EntityType.FIREBALL, EntityType.DRAGON_FIREBALL,
                EntityType.ENDER_DRAGON, EntityType.ENDER_CRYSTAL)
                .contains(event.getEntityType())) {
            event.blockList().clear();
        }
    }


    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (Arrays.asList(EntityType.ENDER_DRAGON, EntityType.ENDERMAN)
                .contains(event.getEntityType())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSnowForm(EntityBlockFormEvent event) {
        if (event.getEntity().getType().equals(EntityType.SNOWMAN)) {
            event.setCancelled(true);
        }
    }

}
