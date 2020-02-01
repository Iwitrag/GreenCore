package cz.iwitrag.greencore.helpers.percentdistribution;

import cz.iwitrag.greencore.helpers.StringHelper;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class PDParser {

    private Set<PDItem> items = new LinkedHashSet<>();

    public Set<PDItem> getItems() {
        Set<PDItem> result = new LinkedHashSet<>();
        for (PDItem item : items) {
            result.add(new PDItem(item.getValue(), item.getChance()));
        }
        return result;
    }

    public String getString() {
        if (items.isEmpty())
            return "";

        StringBuilder builder = new StringBuilder();
        for (PDItem item : items) {
            String chance = StringHelper.doubleStringWithoutTrailingZeros(item.getChance()*100);
            builder.append(chance);
            builder.append("%:");
            builder.append(item.getValue());
            builder.append(",");
        }
        return builder.substring(0, builder.length()-1); // trim last comma
    }

    @Override
    public String toString() {
        return getString();
    }

    public void setupFromItems(Set<PDItem> input) throws PDException {
        if (input == null)
            input = new HashSet<>();
        for (PDItem item : input) {
            if (item.getValue() == null || item.getValue().isEmpty())
                throw new PDException("§cČást nastavená na §4" + StringHelper.doubleStringWithoutTrailingZeros(item.getChance()*100) + "% §cje prázdná.");
            if (item.getChance() < 0)
                throw new PDException("§cČást §4" + item.getValue() + " §cmá záporná procenta.");
        }
        this.items = normalize(input);
    }

    public void setupFromString(String input) throws PDException {
        Set<PDItem> result = new HashSet<>();

        String[] inputSplit = input.split(",");
        for (String inputItem : inputSplit) {
            if (inputItem.contains("%")) {
                String[] inputItemSplit = inputItem.split("%");
                if (inputItemSplit.length == 1 || inputItemSplit[1] == null || inputItemSplit[1].isEmpty())
                    throw new PDException("§cČást nastavená na §4" + inputItemSplit[0] + "% §cje prázdná.");
                double percent;
                try {
                    percent = NumberFormat.getInstance().parse(inputItemSplit[0]).doubleValue();
                } catch (ParseException e) {
                    throw new PDException("§cČást §4" + inputItem + " §cmá neplatná procenta.");
                }
                if (percent < 0)
                    throw new PDException("§cČást §4" + inputItemSplit[1] + " §cmá záporná procenta.");
                result.add(new PDItem(inputItemSplit[1], percent/100));
            }
            else {
                // Items without percentage will be inserted as zero percent and will processed later
                result.add(new PDItem(inputItem, 0.0));
            }
        }

        // Process items without percentage
        for (PDItem item : result) {
            if (item.getChance() == 0.0)
                item.setChance(1.0 / result.size());
        }

        this.items = normalize(result);
    }

    public PDItem pickOneItem() {
        if (items.isEmpty())
            return null;

        double random = Math.random();
        double stackedChance = 0.0;
        for (PDItem item : items) {
            stackedChance += item.getChance();
            if (stackedChance > random)
                return item;
        }
        return items.toArray(new PDItem[0])[items.size()-1]; // last item
    }

    private Set<PDItem> normalize(Set<PDItem> input) {
        // Merge similar items
        Map<String, PDItem> map = new HashMap<>();
        for (Iterator<PDItem> iterator = input.iterator(); iterator.hasNext(); ) {
            PDItem item = iterator.next();
            PDItem itemInMap = map.get(item.getValue());
            if (itemInMap == null)
                map.put(item.getValue(), item);
            else {
                itemInMap.setChance(itemInMap.getChance() + item.getChance());
                iterator.remove();
            }
        }

        // Normalize percentages ( total = 1.00 )
        double totalChance = 0;
        for (PDItem item : input) {
            totalChance += item.getChance();
        }
        for (PDItem item : input) {
            item.setChance(item.getChance() / totalChance);
        }

        // Sort by percentages from highest to lowest
        Set<PDItem> result = new LinkedHashSet<>();
        while (!input.isEmpty()) {
            double highestChance = -1.0;
            PDItem highestItem = null;
            for (PDItem item : input) {
                if (item.getChance() > highestChance) {
                    highestChance = item.getChance();
                    highestItem = item;
                }
            }
            result.add(highestItem);
            input.remove(highestItem);
        }

        return result;
    }
}
