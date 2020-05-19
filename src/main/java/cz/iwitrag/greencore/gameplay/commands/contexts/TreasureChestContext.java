package cz.iwitrag.greencore.gameplay.commands.contexts;

import co.aikar.commands.InvalidCommandArgument;
import cz.iwitrag.greencore.gameplay.chat.ChatUtils;
import cz.iwitrag.greencore.gameplay.treasurechests.TreasureChest;
import cz.iwitrag.greencore.gameplay.treasurechests.TreasureChestManager;
import cz.iwitrag.greencore.helpers.LuckPermsHelper;
import net.luckperms.api.model.group.Group;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TreasureChestContext extends AbstractContext {
    @Override
    public void registerCommandContext() {
        // Treasure Chest actually selected by player
        paperCommandManager.getCommandContexts().registerIssuerOnlyContext(TreasureChest.class, (c) -> {
            if (c.hasFlag("selected") && c.getSender() instanceof Player) {
                Player player = c.getPlayer();
                TreasureChest chest = TreasureChestManager.getInstance().getSelectedChest(player.getName());

                if (chest == null && !c.isOptional()) {
                    player.sendMessage(new ComponentBuilder("")
                            .appendLegacy("§cNemáš vybránu treasure chestku, vyber ji nejdřív příkazem §4")
                            .append(ChatUtils.getPlainTextWithCommand("/tchest select", "/tchest select"))
                            .create()
                    );
                    throw new InvalidCommandArgument(false);
                }
                else
                    return chest;
            }
            else {
                if (c.isOptional())
                    return null;
                else
                    throw new InvalidCommandArgument("§cNepodařilo se určit treasure chestku", false);
            }
        });

        // Completion for players with active cooldown
        paperCommandManager.getCommandCompletions().registerCompletion("tchest_players_cooldown", (c) -> {
            TreasureChest chest = c.getContextValue(TreasureChest.class);
            if (chest == null)
                return new ArrayList<>();
            return new ArrayList<>(chest.getPlayerCooldowns().keySet());
        });

        // Completion for item ids
        // TCHEST TODO - nefunguje tchest_item_id ani tchest_players_cooldown completion
        paperCommandManager.getCommandCompletions().registerCompletion("tchest_item_ids", (c) -> {
            TreasureChest chest = c.getContextValue(TreasureChest.class);
            if (chest == null)
                return new ArrayList<>();
            List<String> completions = new ArrayList<>();
            int number = chest.getPossibleRewardsAmount();
            int rangeLength = String.valueOf(number).length(); // For zero padding
            for (int i = 0; i < number; i++) {
                completions.add(String.format("%0"+rangeLength+"d", i));
            }
            return completions;
        });

        // Completion for various permission groups
        paperCommandManager.getCommandCompletions().registerAsyncCompletion("tchest_groupperms", (c) -> {
            List<Group> groups = LuckPermsHelper.getGroups(true);
            List<String> result = new ArrayList<>();
            for (Group group : groups)
                result.add("tchest." + group.getName());
            return result;
        });
    }
}
