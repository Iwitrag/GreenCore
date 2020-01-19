package cz.iwitrag.greencore.gameplay;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PistonPlantGrowFixer implements Listener {

    private Set<Material> checkedSoils = new HashSet<>(Arrays.asList(Material.SAND, Material.GRASS_BLOCK, Material.END_STONE));
    private Set<Material> checkedPlants = new HashSet<>(Arrays.asList(Material.CACTUS, Material.BAMBOO, Material.CHORUS_PLANT));

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        checkPlants(event.getBlocks());
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        checkPlants(event.getBlocks());
    }

    private void checkPlants(List<Block> blocksAffectedByPiston) {
        for (Block movedBlock : blocksAffectedByPiston) {
            if (checkedSoils.contains(movedBlock.getType())) {
                Block blockAbove = movedBlock.getRelative(BlockFace.UP);
                if (checkedPlants.contains(blockAbove.getType())) {
                    blockAbove.getWorld().dropItemNaturally(blockAbove.getLocation(), new ItemStack(blockAbove.getType(), 1));
                    blockAbove.setType(Material.AIR);
                }
            }
        }
    }

}
