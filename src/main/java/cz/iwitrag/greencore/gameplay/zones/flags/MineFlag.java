package cz.iwitrag.greencore.gameplay.zones.flags;

import cz.iwitrag.greencore.gameplay.zones.Zone;
import cz.iwitrag.greencore.gameplay.zones.ZoneException;
import cz.iwitrag.greencore.helpers.percentdistribution.PDException;
import cz.iwitrag.greencore.helpers.percentdistribution.PDItem;
import cz.iwitrag.greencore.helpers.percentdistribution.PDParser;
import org.bukkit.Material;

import java.util.HashSet;
import java.util.Set;

public class MineFlag implements Flag {

    private Zone zone;
    private Set<MineBlock> blocks;
    private int regenTime;

    public MineFlag(Zone zone) {
        this.zone = zone;
        this.blocks = new HashSet<>();
        this.regenTime = 300;
    }

    public Zone getZone() {
        return this.zone;
    }

    public Set<MineBlock> getBlocks() {
        return new HashSet<>(blocks);
    }

    public void setBlocksFromString(String blocksString) throws ZoneException {
        Set<PDItem> parsedblocks;
        try {
            parsedblocks = new PDParser().parse(blocksString);
        } catch (PDException e) {
            throw new ZoneException(e.getMessage());
        }
        Set<MineBlock> convertedBlocks = new HashSet<>();
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
        this.blocks.clear();
        this.blocks = convertedBlocks;
    }

    public String getBlocksAsString() {
        if (blocks.isEmpty())
            return "---";
        StringBuilder builder = new StringBuilder();
        for (MineBlock mineBlock : blocks) {
            String chance = String.valueOf(mineBlock.chance);
            // Strip trailing zeros
            chance = chance.contains(".") ? chance.replaceAll("0*$","").replaceAll("\\.$","") : chance;
            builder.append(chance);
            builder.append("%:");
            builder.append(mineBlock.getBlockType().name());
            builder.append(",");
        }
        return builder.substring(0, builder.length()-1);
    }

    public int getRegenTime() {
        return regenTime;
    }

    public void setRegenTime(int regenTime) {
        this.regenTime = regenTime;
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
