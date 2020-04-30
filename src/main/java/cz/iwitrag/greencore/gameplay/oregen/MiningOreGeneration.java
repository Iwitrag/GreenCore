package cz.iwitrag.greencore.gameplay.oregen;

import cz.iwitrag.greencore.gameplay.zones.Zone;
import cz.iwitrag.greencore.gameplay.zones.ZoneManager;
import cz.iwitrag.greencore.gameplay.zones.flags.MineFlag;
import cz.iwitrag.greencore.helpers.Utils;
import javafx.util.Pair;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MiningOreGeneration implements Listener {

    private static final double ORE_MULTIPLIER = 1.50;

    @EventHandler
    public void onPlayerMining(BlockBreakEvent event) {
        Block brokenBlock = event.getBlock();

        // Don't spawn ores outside survival overworld
        if (!brokenBlock.getWorld().getName().equalsIgnoreCase("world"))
            return;

        // Don't spawn ores inside mines
        for (Zone zone : ZoneManager.getInstance().getZonesPlayerIsInside(event.getPlayer())) {
            if (zone.hasFlag(MineFlag.class)) {
                return;
            }
        }

        for (Block revealedBlock : getRevealedBlocks(brokenBlock)) {
            if (revealedBlock.getType() != Material.STONE) // Because it could be changed by previous iteration
                continue;

            OreSettings chosenOreSettings = chooseOreToGenerate(revealedBlock.getLocation());
            if (chosenOreSettings != null) { // Player was lucky, revealed block is ore vein
                Set<Block> blocksToSet = new HashSet<>();
                blocksToSet.add(revealedBlock);
                int veinSize = determineVeinSize(chosenOreSettings);

                while (blocksToSet.size() < veinSize) {
                    boolean foundSpaceForOrePiece = false;
                    for (int i = 0; i < 10; i++) {
                        Block centerBlock = Utils.pickRandomElement(blocksToSet);
                        Block anotherBlock;
                        if (chosenOreSettings.isDirectlyConnectedOnly())
                            anotherBlock = Utils.pickRandomElement(Utils.getBlockDirectNeighbours(centerBlock));
                        else
                            anotherBlock = Utils.pickRandomElement(getAllNeighboursExceptFarCorners(centerBlock));

                        if (centerBlock == null || anotherBlock == null)
                            continue;

                        // Make ore vein shape more bulky (if chosen block is too far away or not direct, it has less chance of being chosen)
                        if (Utils.chance(15.00 * anotherBlock.getLocation().distance(revealedBlock.getLocation())) ||
                                (anotherBlock.getLocation().distance(centerBlock.getLocation()) != 1.00 && Utils.chance(50.00))) {
                            i -= 1;
                            continue;
                        }

                        if (!anotherBlock.equals(brokenBlock) && !blocksToSet.contains(anotherBlock) && anotherBlock.getType().equals(Material.STONE) && !isBlockExposed(anotherBlock, brokenBlock)) {
                            blocksToSet.add(anotherBlock);
                            foundSpaceForOrePiece = true;
                            break;
                        }
                    }
                    // If we failed to find place for another ore piece, there is probably no space left
                    if (!foundSpaceForOrePiece) {
                        break;
                    }
                }

                if (blocksToSet.size() >= chosenOreSettings.getMinOrePieces()) {
                    for (Block block : blocksToSet) {
                        block.setType(chosenOreSettings.getOre());
                    }
                }
            }
        }
    }

    private Set<Block> getRevealedBlocks(Block brokenBlock) {
        Set<Block> revealedBlocks = Utils.getBlockDirectNeighbours(brokenBlock);
        revealedBlocks.removeIf(revealedBlock -> revealedBlock.getType() != Material.STONE || isBlockExposed(revealedBlock, brokenBlock));
        return revealedBlocks;
    }

    private OreSettings chooseOreToGenerate(Location location) {
        Map<Material, OreSettings> settings = getOreSettings(location);
        int random = (int) (Math.random()*(25000.0 / ORE_MULTIPLIER));
        int stackedChances = 0;
        for (Material material : settings.keySet()) {
            OreSettings oreSettings = settings.get(material);
            stackedChances += calculateChanceForOre(location.getBlockY(), oreSettings.getDistribution()) * 100;
            if (random < stackedChances)
                return oreSettings;
        }
        return null;
    }

    private int determineVeinSize(OreSettings oreSettings) {
        Map<Integer, Integer> distribution = new HashMap<>(); // key = vein size, value = chance
        int maximum = 0; // maximum of stacked chances to choose from
        for (int i = oreSettings.getMinOrePieces(); i <= oreSettings.getMaxOrePieces(); i++) {
            int chance = Math.max(1, oreSettings.getMaxOrePieces()-((int)(2.5*Math.abs(i-oreSettings.getMedOrePieces()))));
            distribution.put(i, chance);
            maximum += chance;
        }
        int random = (int) (Math.random()*maximum);
        int stackedChance = 0;
        for (Integer veinSize : distribution.keySet()) {
            stackedChance += distribution.get(veinSize);
            if (random < stackedChance)
                return veinSize;
        }
        return 0;
    }

    private Map<Material, OreSettings> getOreSettings(Location blockLocation) {
        int y = blockLocation.getBlockY();
        if (y <= 0 || y >= 256)
            return new HashMap<>();
        World world = Objects.requireNonNull(blockLocation.getWorld());
        Biome biome = world.getBiome(blockLocation.getBlockX(), blockLocation.getBlockZ());
        Map<Material, OreSettings> settings = new HashMap<>();
        OresManager oresManager = new OresManager();

        settings.put(Material.COAL_ORE, oresManager.getCoalOreSettings());
        settings.put(Material.IRON_ORE, oresManager.getIronOreSettings());
        settings.put(Material.GOLD_ORE, oresManager.getGoldOreSettings(biome.name().toLowerCase().contains("badlands")));
        settings.put(Material.REDSTONE_ORE, oresManager.getRedstoneOreSettings());
        settings.put(Material.LAPIS_ORE, oresManager.getLapisOreSettings());
        settings.put(Material.DIAMOND_ORE, oresManager.getDiamondOreSettings());
        settings.put(Material.EMERALD_ORE, oresManager.getEmeraldOreSettings(biome.name().toLowerCase().contains("mountain")));

        return settings;
    }

    private double calculateChanceForOre(int y, List<Pair<Integer, Double>> ysAndChances) {
        for (int i = 0; i < ysAndChances.size()-1; i++) {
            int y1 = ysAndChances.get(i).getKey();
            int y2 = ysAndChances.get(i+1).getKey();
            int yDelta = Math.abs(y1-y2);
            double ch1 = ysAndChances.get(i).getValue();
            double ch2 = ysAndChances.get(i+1).getValue();
            double chDelta = Math.abs(ch1-ch2);
            if (y > y1 && y <= y2) {
                if (ch1 > ch2) {
                    return ch1-((chDelta/yDelta)*(y-y1));
                } else if (ch1 < ch2) {
                    return ch1+((chDelta/yDelta)*(y-y1));
                } else {
                    return ch1;
                }
            }
        }
        return 0;
    }

    private boolean isBlockExposed(Block blockToCheck, Block neighbourToIgnore) {
        List<Material> transparentTypes = Arrays.asList(Material.WATER, Material.AIR, Material.CAVE_AIR, Material.LAVA,
                Material.TORCH, Material.WALL_TORCH, Material.REDSTONE_TORCH, Material.REDSTONE_WALL_TORCH);
        for (Block neighbour : Utils.getBlockDirectNeighbours(blockToCheck)) {
            if (!neighbour.equals(neighbourToIgnore) && transparentTypes.contains(neighbour.getType()))
                return true;
        }
        return false;
    }




    private Set<Block> getAllNeighboursExceptFarCorners(Block block) {
        Set<Block> set = new HashSet<>();
        if (block != null) {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        // at least one coordinate must not be zero (to avoid center block itself)
                        // and at least one coordinate must be zero (to avoid far corners)
                        if ((x != 0 || y != 0 || z != 0) && (x == 0 || y == 0 || z == 0)) {
                            set.add(block.getRelative(x, y, z));
                        }
                    }
                }
            }
        }
        return set;
    }
}
