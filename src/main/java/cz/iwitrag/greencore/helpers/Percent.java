package cz.iwitrag.greencore.helpers;

import java.util.Objects;

public final class Percent {

    private final double baseValue; // percents divided by 100

    public Percent(double percent) {
        this.baseValue = percent / 100;
    }

    public Percent(String percent) {
        if (percent == null)
            throw new IllegalArgumentException("Percent cannot be null");
        percent = percent.replaceAll(",", ".");
        percent = percent.replaceAll("%", "");
        percent = percent.replaceAll(" ", "");
        percent = percent.trim();

        try {
            baseValue = Double.parseDouble(percent) / 100;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Failed to get percent from string");
        }
    }

    public double getBaseValue() {
        return baseValue;
    }

    public double getPercentValue() {
        return baseValue * 100;
    }

    public Percent getNormalizedCopy() {
        double newValue = baseValue;
        if (newValue < 0)
            newValue = 0;
        if (newValue > 1)
            newValue = 1;
        return new Percent(newValue*100);
    }

    @Override
    public String toString() {
        return Utils.twoDecimal(baseValue * 100) + " %";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Percent)) return false;
        Percent percent = (Percent) o;
        return Double.compare(percent.baseValue, baseValue) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseValue);
    }
}
