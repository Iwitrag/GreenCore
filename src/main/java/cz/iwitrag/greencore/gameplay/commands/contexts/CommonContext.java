package cz.iwitrag.greencore.gameplay.commands.contexts;

import co.aikar.commands.InvalidCommandArgument;
import cz.iwitrag.greencore.helpers.Percent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommonContext extends AbstractContext {
    @Override
    public void registerCommandContext() {
        // Register numeric range with increments range:5-100(10)
        paperCommandManager.getCommandCompletions().registerAsyncCompletion("range", (c) -> {
            String config = c.getConfig();
            if (config == null)
                return Collections.emptyList();
            config = config.replaceAll("[^0-9]+", " ");
            config = config.trim();
            int start = 0;
            int end = 10;
            int increment = 1;
            if (!config.isEmpty()) {
                String[] split = config.split(" ");
                if (split.length >= 1)
                    start = Integer.parseInt(split[0]);
                if (split.length >= 2)
                    end = Integer.parseInt(split[1]);
                if (split.length >= 3)
                    increment = Integer.parseInt(split[2]);
            }
            List<String> completions = new ArrayList<>();
            int rangeLength = String.valueOf(end).length(); // For zero padding
            for (int i = start; i <= end; i+=increment) {
                completions.add(String.format("%0"+rangeLength+"d", i));
            }
            return completions;
        });

        // Register chance
        paperCommandManager.getCommandCompletions().registerAsyncCompletion("chance", (c) -> Arrays.asList("001%", "010%", "020%", "030%", "040%", "050%", "060%", "070%", "080%", "090%", "100%"));

        // Register held ItemStack in main hand
        paperCommandManager.getCommandContexts().registerIssuerOnlyContext(ItemStack.class, (c) -> {
            if (c.hasFlag("main_hand") && c.getSender() instanceof Player) {
                ItemStack item = c.getPlayer().getInventory().getItemInMainHand();

                if (item.getType() == Material.AIR) {
                    if (c.isOptional())
                        return null;
                    else
                        throw new InvalidCommandArgument("§cMusíš mít item v ruce", false);
                }
                else
                    return item;
            }
            else {
                if (c.isOptional())
                    return null;
                else
                    throw new InvalidCommandArgument("§cNepodařilo se určit item", false);
            }
        });

        // Percents
        paperCommandManager.getCommandContexts().registerContext(Percent.class, (c) -> {
            String arg = c.popFirstArg();
            Percent percent;
            try {
                percent = new Percent(arg);
            } catch (IllegalArgumentException ex) {
                throw new InvalidCommandArgument("§cProcentuální hodnota §4" + arg + " §cje neplatná", false);
            }
            return percent;
        });
    }
}
