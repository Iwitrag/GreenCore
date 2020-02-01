package cz.iwitrag.greencore.gameplay.zones.flags;

import cz.iwitrag.greencore.gameplay.zones.ZoneException;
import cz.iwitrag.greencore.helpers.percentdistribution.PDException;
import cz.iwitrag.greencore.helpers.percentdistribution.PDItem;
import cz.iwitrag.greencore.helpers.percentdistribution.PDParser;
import org.bukkit.Material;

import java.util.LinkedHashSet;
import java.util.Set;

public class MineFlag implements Flag {

    private Set<MineBlock> blocks = new LinkedHashSet<>();
    private double regenPercentage;

    public MineFlag() {
        this.regenPercentage = 100.00;
    }

    public boolean hasBlocks() {
        return !blocks.isEmpty();
    }

    public Set<MineBlock> getBlocks() {
        return new LinkedHashSet<>(blocks);
    }

    public void setBlocksFromString(String blocksString) throws ZoneException {
        PDParser parser = new PDParser();
        try {
            parser.setupFromString(blocksString);
        } catch (PDException e) {
            throw new ZoneException(e.getMessage());
        }
        Set<PDItem> parsedblocks = parser.getItems();
        Set<MineBlock> convertedBlocks = new LinkedHashSet<>();
        for (PDItem pdItem : parsedblocks) {
            Material material = Material.getMaterial(pdItem.getValue());
            if (material == null) {
                throw new ZoneException("§cTyp bloku s názvem §4" + pdItem.getValue() + " §cnebyl nalezen.");
            }
            if (!material.isBlock()) {
                throw new ZoneException("§cŘetězec §4" + pdItem.getValue() + " §cnení platný blok.");
            }
            convertedBlocks.add(new MineBlock(material, pdItem.getChance()));
        }
        this.blocks = convertedBlocks;
    }

    public String getBlocksAsString() {
        if (blocks.isEmpty())
            return "---";
        Set<PDItem> PDInput = new LinkedHashSet<>();
        for (MineBlock block : blocks) {
            PDInput.add(new PDItem(block.getBlockType().toString(), block.getChance()));
        }
        PDParser parser = new PDParser();
        try {
            parser.setupFromItems(PDInput);
        } catch (PDException e) {
            return "Chyba: " + e.getMessage();
        }
        return parser.toString();
    }

    public double getRegenPercentage() {
        return regenPercentage;
    }

    public void setRegenPercentage(double regenPercentage) {
        if (regenPercentage < 0)
            regenPercentage = 0;
        this.regenPercentage = regenPercentage;
    }

    public Material pickOneBlock() {
        if (blocks.isEmpty())
            return null;

        double random = Math.random();
        double stackedChance = 0.0;
        for (MineBlock block : blocks) {
            stackedChance += block.getChance();
            if (stackedChance > random)
                return block.getBlockType();
        }
        return blocks.toArray(new MineBlock[0])[blocks.size()-1].blockType; // last item
    }

    public class MineBlock {

        private Material blockType;
        private double chance;

        public MineBlock(Material blockType, double chance) {
            this.blockType = blockType;
            this.chance = chance;
        }

        public Material getBlockType() {
            return blockType;
        }

        public double getChance() {
            return chance;
        }
    }
}
