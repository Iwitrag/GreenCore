package cz.iwitrag.greencore.helpers.percentdistribution;

public class PDItem {

    private String value;
    private double chance;

    public PDItem(String value, double chance) {
        this.value = value;
        this.chance = chance;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }
}
