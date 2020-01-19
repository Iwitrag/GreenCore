package cz.iwitrag.greencore.helpers.percentdistribution;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PDParser {

    public Set<PDItem> parse(String input) throws PDException {
        Map<String, Double> itemsWithPercent = new HashMap<>();
        Map<String, Integer> itemsWithoutPercent = new HashMap<>();
        String[] inputSplit = input.split(",");
        for (String inputItem : inputSplit) {
            if (inputItem.contains("%")) {
                double percent;
                String[] inputItemSplit = inputItem.split("%");
                try {
                    percent = NumberFormat.getInstance().parse(inputItemSplit[0]).doubleValue();
                } catch (ParseException e) {
                    throw new PDException("§cČást §4" + inputItem + " §cobsahuje neplatné číslo");
                }
                itemsWithPercent.put(inputItemSplit[1], percent + itemsWithPercent.getOrDefault(inputItemSplit[1], 0.0));
            }
            else {
                itemsWithoutPercent.put(inputItem, 1 + itemsWithoutPercent.getOrDefault(inputItem, 0));
            }
        }
        // ZONE TODO - PDParser
    }
}
