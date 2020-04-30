package cz.iwitrag.greencore.gameplay.zones.flags;

import cz.iwitrag.greencore.gameplay.zones.ZoneException;
import cz.iwitrag.greencore.helpers.percentdistribution.PDException;
import cz.iwitrag.greencore.helpers.percentdistribution.PDItem;
import cz.iwitrag.greencore.helpers.percentdistribution.PDParser;
import cz.iwitrag.greencore.storage.converters.MineFlagBlocksConverter;
import org.bukkit.Material;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("mine")
public class MineFlag extends Flag {

    @Column(name = "mine_blocks", length = 1000)
    @Convert(converter = MineFlagBlocksConverter.class)
    private Set<MineBlock> blocks = new LinkedHashSet<>();

    @Column(name = "mine_regenPercentage")
    private float regenPercentage;

    public MineFlag() {
        this.regenPercentage = 100.00f;
    }

    public boolean hasBlocks() {
        return !blocks.isEmpty();
    }

    public Set<MineBlock> getBlocks() {
        return new LinkedHashSet<>(blocks);
    }

    /**
     * Converts String into MineBlocks
     * @param input Input String
     * @return Converted Set with MineBlocks, if input String is empty, returned Set will be empty as well
     * @throws ZoneException Error with conversion
     */
    public static Set<MineBlock> makeBlocksFromString(String input) throws ZoneException {
        Set<MineBlock> convertedBlocks = new LinkedHashSet<>();
        if (input.isEmpty())
            return convertedBlocks;

        PDParser parser = new PDParser();
        try {
            parser.setupFromString(input);
        } catch (PDException e) {
            throw new ZoneException(e.getMessage());
        }
        Set<PDItem> parsedBlocks = parser.getItems();
        for (PDItem pdItem : parsedBlocks) {
            Material material = Material.getMaterial(pdItem.getValue());
            if (material == null) {
                throw new ZoneException("§cTyp bloku s názvem §4" + pdItem.getValue() + " §cnebyl nalezen.");
            }
            if (!material.isBlock()) {
                throw new ZoneException("§cŘetězec §4" + pdItem.getValue() + " §cnení platný blok.");
            }
            convertedBlocks.add(new MineBlock(material, pdItem.getChance()));
        }
        return convertedBlocks;
    }

    /**
     * Converts MineBlocks to String
     * @param input Input MineBlocks
     * @return Converted String, if input Set is empty, returned String will be empty as well
     * @throws Error with conversion
     */
    public static String makeStringFromBlocks(Set<MineBlock> input) throws ZoneException {
        if (input.isEmpty())
            return "";
        Set<PDItem> PDInput = new LinkedHashSet<>();
        for (MineBlock block : input) {
            PDInput.add(new PDItem(block.getBlockType().toString(), block.getChance()));
        }
        PDParser parser = new PDParser();
        try {
            parser.setupFromItems(PDInput);
        } catch (PDException e) {
            throw new ZoneException(e.getMessage());
        }
        return parser.toString();
    }

    public void setBlocksFromString(String blocksString) throws ZoneException {
        this.blocks = makeBlocksFromString(blocksString);
    }


    public String getBlocksAsString() throws ZoneException {
        return makeStringFromBlocks(this.blocks);
    }

    public double getRegenPercentage() {
        return regenPercentage;
    }

    public void setRegenPercentage(float regenPercentage) {
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

    @Override
    public Flag copy() {
        MineFlag flag = new MineFlag();
        flag.setRegenPercentage(regenPercentage);
        try {
            flag.setBlocksFromString(getBlocksAsString());
        } catch (ZoneException e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static class MineBlock {

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
