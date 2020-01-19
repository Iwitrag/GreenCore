package cz.iwitrag.greencore.gameplay.playerskills;

import java.util.HashMap;
import java.util.Map;

public class SkillsExpTable {

    private static Map<Integer, Double> expTable = new HashMap<>();

    private SkillsExpTable() {}

    public static double getExpForNextLevel(int currentLevel) {
        if (currentLevel < 1) return Double.MAX_VALUE;

        Double neededExp = expTable.get(currentLevel);
        if (neededExp == null) {
            if (currentLevel == 1)
                neededExp = 100.00;
            else
                neededExp = getExpForNextLevel(currentLevel-1)+(25*Math.pow(currentLevel-1, 2.00));
            expTable.put(currentLevel, neededExp);
        }

        return neededExp;
    }

    public static boolean nextLevelReached(int currentLevel, double currentExp) {
        return currentExp > getExpForNextLevel(currentLevel);
    }

}
