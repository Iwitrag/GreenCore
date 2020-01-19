package cz.iwitrag.greencore.gameplay.oregen;

import javafx.util.Pair;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;

public class OresManager {

    public OreSettings getCoalOreSettings() {
        return new OreSettings()
                .setOre(Material.COAL_ORE)
                .setMinOrePieces(7)
                .setMedOrePieces(12)
                .setMaxOrePieces(25)
                .setDistribution(Arrays.asList(new Pair<>(1, 0.00), new Pair<>(6, 1.00), new Pair<>(57, 0.70), new Pair<>(70, 0.30), new Pair<>(130, 0.00)))
                .setDirectlyConnectedOnly(true);
    }

    public OreSettings getIronOreSettings() {
        return new OreSettings()
                .setOre(Material.IRON_ORE)
                .setMinOrePieces(2)
                .setMedOrePieces(5)
                .setMaxOrePieces(12)
                .setDistribution(Arrays.asList(new Pair<>(1, 0.00), new Pair<>(6, 0.73), new Pair<>(57, 0.63), new Pair<>(64, 0.00)))
                .setDirectlyConnectedOnly(true);
    }

    public OreSettings getGoldOreSettings(boolean isBadlands) {
        if (isBadlands)
            return new OreSettings()
                    .setOre(Material.GOLD_ORE)
                    .setMinOrePieces(4)
                    .setMedOrePieces(5)
                    .setMaxOrePieces(10)
                    .setDistribution(Arrays.asList(new Pair<>(1, 0.00), new Pair<>(5, 0.15), new Pair<>(29, 0.15), new Pair<>(32, 0.93), new Pair<>(63, 0.93), new Pair<>(64, 0.00)))
                    .setDirectlyConnectedOnly(true);
        else
            return new OreSettings()
                    .setOre(Material.GOLD_ORE)
                    .setMinOrePieces(4)
                    .setMedOrePieces(5)
                    .setMaxOrePieces(10)
                    .setDistribution(Arrays.asList(new Pair<>(1, 0.00), new Pair<>(5, 0.15), new Pair<>(29, 0.15), new Pair<>(32, 0.00)))
                    .setDirectlyConnectedOnly(true);
    }

    public OreSettings getRedstoneOreSettings() {
        return new OreSettings()
                .setOre(Material.REDSTONE_ORE)
                .setMinOrePieces(3)
                .setMedOrePieces(5)
                .setMaxOrePieces(9)
                .setDistribution(Arrays.asList(new Pair<>(1, 0.00), new Pair<>(7, 0.50), new Pair<>(13, 0.45), new Pair<>(18, 0.00)))
                .setDirectlyConnectedOnly(false);
    }

    public OreSettings getLapisOreSettings() {
        return new OreSettings()
                .setOre(Material.LAPIS_ORE)
                .setMinOrePieces(4)
                .setMedOrePieces(4)
                .setMaxOrePieces(6)
                .setDistribution(Arrays.asList(new Pair<>(1, 0.00), new Pair<>(15, 0.10), new Pair<>(31, 0.00)))
                .setDirectlyConnectedOnly(true);
    }

    public OreSettings getDiamondOreSettings() {
        return new OreSettings()
                .setOre(Material.DIAMOND_ORE)
                .setMinOrePieces(3)
                .setMedOrePieces(6)
                .setMaxOrePieces(8)
                .setDistribution(Arrays.asList(new Pair<>(1, 0.00), new Pair<>(5, 0.13), new Pair<>(13, 0.12), new Pair<>(16, 0.00)))
                .setDirectlyConnectedOnly(false);
    }

    public OreSettings getEmeraldOreSettings(boolean isMountains) {
        if (isMountains)
            return new OreSettings()
                    .setOre(Material.EMERALD_ORE)
                    .setMinOrePieces(1)
                    .setMedOrePieces(1)
                    .setMaxOrePieces(1)
                    .setDistribution(Arrays.asList(new Pair<>(3, 0.00), new Pair<>(5, 0.07), new Pair<>(31, 0.07), new Pair<>(32, 0.00)))
                    .setDirectlyConnectedOnly(false);
        else
            return new OreSettings()
                    .setOre(Material.EMERALD_ORE)
                    .setMinOrePieces(0)
                    .setMedOrePieces(0)
                    .setMaxOrePieces(0)
                    .setDistribution(new ArrayList<>())
                    .setDirectlyConnectedOnly(false);
    }

}
