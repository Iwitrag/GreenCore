package cz.iwitrag.greencore.gameplay.itemdb;

import cz.iwitrag.greencore.Main;
import cz.iwitrag.greencore.helpers.ConfigurationManager;
import cz.iwitrag.greencore.helpers.Utils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.text.Collator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class ItemDB {

    private Map<String, ItemStack> items;
    private FileConfiguration config;

    public ItemDB(String filename) {
        Collator collator = Collator.getInstance(new Locale("cs", "CZ"));
        collator.setStrength(Collator.SECONDARY);
        items = new TreeMap<>(collator);

        config = ConfigurationManager.getInstance().getConfig(filename);

        for (String key : config.getKeys(false)) {
            items.put(key.replaceAll("@", "."), Utils.stringToItemStack(config.getString(key)));
        }
        Main.getInstance().getLogger().info("Loaded " + items.size() + " serialized ItemStacks");
    }

    public Map<String, ItemStack> getItems() {
        Map<String, ItemStack> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        result.putAll(items);
        return result;
    }

    public Map<String, ItemStack> getItems(String owner, String name) {
        Map<String, ItemStack> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        boolean ownerEmpty = owner == null || owner.isEmpty();
        boolean nameEmpty = name == null || name.isEmpty();
        if (ownerEmpty && nameEmpty)
            return result;
        for (Map.Entry<String, ItemStack> entry : items.entrySet()) {
            String itemName = entry.getKey();
            ItemStack itemStack = entry.getValue();
            String[] itemNameSplit = itemName.split("\\.");
            if (ownerEmpty || owner.equalsIgnoreCase(itemNameSplit[0])) {
                if (nameEmpty || itemNameSplit[1].toLowerCase().contains(name.toLowerCase()))
                    result.put(itemName, itemStack);
            }
        }
        return result;
    }

    public Map<String, ItemStack> getItems(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            return getItems(null, null);
        }
        else if (fullName.contains(".")) {
            String[] split = fullName.split("\\.");
            return getItems(split[0], split[1]);
        }
        else {
            return getItems(null, fullName);
        }
    }

    public ItemStack getItemExact(String owner, String name) {
        if (owner == null || owner.isEmpty() || name == null || name.isEmpty())
            return null;
        return items.get(owner + "." + name);
    }

    public ItemStack getItemExact(String fullName) {
        if (fullName == null || fullName.isEmpty())
            return null;
        return items.get(fullName);
    }

    public void addItem(String owner, String name, ItemStack item) {
        if (owner == null || owner.isEmpty() || name == null || name.isEmpty() || item == null)
            return;
        ItemStack newItem = new ItemStack(item);
        newItem.setAmount(1);
        items.put(owner + "." + name, newItem);
        config.set((owner + "@" + name).toLowerCase(), Utils.itemStackToString(newItem));
    }

    public void addItem(String fullName, ItemStack item) {
        if (fullName == null || fullName.isEmpty() || item == null)
            return;
        ItemStack newItem = new ItemStack(item);
        newItem.setAmount(1);
        items.put(fullName, newItem);
        config.set(fullName.toLowerCase(), Utils.itemStackToString(newItem));
    }

    public boolean removeItem(String owner, String name) {
        if (owner == null || owner.isEmpty() || name == null || name.isEmpty())
            return false;
        config.set((owner + "@" + name).toLowerCase(), null);
        return items.remove(owner + "." + name) != null;
    }

    public boolean removeItem(String fullName) {
        if (fullName == null || fullName.isEmpty())
            return false;
        config.set(fullName.toLowerCase(), null);
        return items.remove(fullName) != null;
    }

}
