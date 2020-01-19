package cz.iwitrag.greencore.helpers.percentdistribution;

public class PDItem {

    private String value;
    private double chance;

    PDItem(String value, double chance) {
        this.value = value;
        this.chance = chance;
    }

    public String getValue() {
        return value;
    }

    public double getChance() {
        return chance;
    }
}
