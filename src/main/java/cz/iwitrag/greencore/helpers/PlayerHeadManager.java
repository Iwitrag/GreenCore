package cz.iwitrag.greencore.helpers;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Objects;
import java.util.Optional;

// Warning - getting OfflinePlayer of Skull state doesn't work when player is offline !!! SkinsRestorer assigns skin as texture and customizes NBT

public class PlayerHeadManager {

    public static ItemStack getPlayerHead(OfflinePlayer player) {
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta playerHeadMeta = (SkullMeta)Objects.requireNonNull(playerHead.getItemMeta());
        playerHeadMeta.setOwningPlayer(player);
        playerHeadMeta.setDisplayName("§eHlava hráče " + player.getName());
        playerHead.setItemMeta(playerHeadMeta);
        return playerHead;
    }

    public static ItemStack getPlayerHead(Block block) {
        if (block.getType().equals(Material.PLAYER_HEAD) || block.getType().equals(Material.PLAYER_WALL_HEAD)) {
            OfflinePlayer owner = ((Skull) block.getState()).getOwningPlayer();
            Optional<ItemStack> optional = block.getDrops().stream().findFirst();
            if (owner != null && owner.getName() != null && optional.isPresent()) {
                ItemStack drop = optional.get();
                SkullMeta dropMeta = (SkullMeta) Objects.requireNonNull(drop.getItemMeta());
                dropMeta.setDisplayName("§eHlava hráče " + owner.getName());
                drop.setItemMeta(dropMeta);
                return drop;
            }
        }

        return new ItemStack(Material.PLAYER_HEAD);
    }

    // TODO - command to get player head
    // Maybe when player joins save his head into yaml configuration? This way NBT could be correctly saved! (but wait till SkinsRestorer gets his skin)

}
