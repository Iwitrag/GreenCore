package cz.iwitrag.greencore.gameplay.commands.contexts;

import cz.iwitrag.greencore.gameplay.itemdb.ItemDB;
import cz.iwitrag.greencore.gameplay.itemdb.ItemDBCommands;
import cz.iwitrag.greencore.helpers.DependenciesProvider;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ItemDBContext extends AbstractContext {
    @Override
    public void registerCommandContext() {
        paperCommandManager.getCommandCompletions().registerAsyncCompletion("itemdb", c -> {
            ItemDB itemDB = DependenciesProvider.getInstance().getDefaultItemDB();
            Set<String> result = new HashSet<>();
            for (Map.Entry<String, ItemStack> entry : itemDB.getItems().entrySet()) {
                result.add(entry.getKey());
            }
            for (Map.Entry<String, ItemStack> entry : itemDB.getItems(ItemDBCommands.GLOBAL_OWNER, null).entrySet()) {
                result.add(entry.getKey().split("\\.")[1]);
            }
            for (Map.Entry<String, ItemStack> entry : itemDB.getItems(c.getSender().getName(), null).entrySet()) {
                result.add(entry.getKey().split("\\.")[1]);
            }
            return result;
        });
    }
}
