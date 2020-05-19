package cz.iwitrag.greencore.gameplay.commands.contexts;

import co.aikar.commands.InvalidCommandArgument;
import cz.iwitrag.greencore.gameplay.zones.Zone;
import cz.iwitrag.greencore.gameplay.zones.ZoneCommands;
import cz.iwitrag.greencore.gameplay.zones.ZoneManager;
import cz.iwitrag.greencore.gameplay.zones.actions.Action;
import cz.iwitrag.greencore.gameplay.zones.flags.Flag;
import org.apache.commons.lang.math.NumberUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ZoneContext extends AbstractContext {

    @Override
    public void registerCommandContext() {
        // Register Zone as context
        paperCommandManager.getCommandContexts().registerContext(Zone.class, c -> {
            String arg = c.popFirstArg();
            Zone zone = ZoneManager.getInstance().getZone(arg);
            if (zone == null)
                throw new InvalidCommandArgument("§cZóna §4" + arg + " §cneexistuje", false);
            return zone;
        });

        // Register Zone as completion
        paperCommandManager.getCommandCompletions().registerAsyncCompletion("zones", c -> {
            List<String> completions = new ArrayList<>();
            for (Zone zone : ZoneManager.getInstance().getZones())
                completions.add(zone.getName());
            return completions;
        });

        // Register ids of actions of specific Zone
        paperCommandManager.getCommandCompletions().registerCompletion("zone_actions_ids", (c) -> {
            Zone zone;
            try {
                zone = c.getContextValue(Zone.class, NumberUtils.createInteger(c.getConfig()));
            } catch (InvalidCommandArgument e) {
                zone = null;
            }
            if (zone == null)
                return Collections.emptyList();

            List<String> completions = new ArrayList<>();
            for (int i = 0; i < zone.getActionsAmount(); i++) {
                if (zone.getAction(i) != null)
                    completions.add(String.valueOf(i));
            }
            return completions;
        });

        // Register action types (all)
        paperCommandManager.getCommandCompletions().registerCompletion("zone_actions_types", (c) -> {
            List<String> completions = new ArrayList<>();
            Map<Class<? extends Action>, List<String>> keywords = ZoneCommands.getActionKeywords();
            for (Class<? extends Action> key : keywords.keySet()) {
                completions.add(keywords.get(key).get(0));
            }
            return completions;
        });

        // Register flag types (all or of specific Zone)
        paperCommandManager.getCommandCompletions().registerAsyncCompletion("zone_flags_types", (c) -> {
            Zone zone;
            try {
                zone = c.getContextValue(Zone.class, NumberUtils.createInteger(c.getConfig()));
            } catch (InvalidCommandArgument e) {
                zone = null;
            }
            List<String> completions = new ArrayList<>();
            Map<Class<? extends Flag>, List<String>> keywords = ZoneCommands.getFlagKeywords();
            for (Class<? extends Flag> key : keywords.keySet()) {
                if (zone == null || zone.hasFlag(key))
                    completions.add(keywords.get(key).get(0));
            }
            return completions;
        });

        // Register dynamic action params
        paperCommandManager.getCommandCompletions().registerAsyncCompletion("zone_actions_param", (c) -> {
            int paramNumber = NumberUtils.toInt(c.getConfig(), -1);
            if (paramNumber != 1 && paramNumber != 2 && paramNumber != 3)
                return Collections.emptyList();

            return Collections.emptyList();
            // ZONE TODO - command completion for action params (action add + action edit commands)
        });

        // Register dynamic flag params
        paperCommandManager.getCommandCompletions().registerAsyncCompletion("zone_flags_param", (c) -> {
            int paramNumber = NumberUtils.toInt(c.getConfig(), -1);
            if (paramNumber != 1 && paramNumber != 2 && paramNumber != 3)
                return Collections.emptyList();

            return Collections.emptyList();
            // ZONE TODO - command completion for flag params (flag set command)
        });
    }
}
