package cz.iwitrag.greencore.gameplay.commands.contexts;

import co.aikar.commands.InvalidCommandArgument;
import cz.iwitrag.greencore.helpers.Color;

public class ColorContext extends AbstractContext {
    @Override
    public void registerCommandContext() {
        paperCommandManager.getCommandCompletions().registerAsyncCompletion("colors", c -> Color.getNamedColors().keySet());

        paperCommandManager.getCommandContexts().registerContext(Color.class, c -> {
            String arg = c.popFirstArg();
            Color color;
            try {
                color = new Color(arg);
            } catch (IllegalArgumentException ex) {
                throw new InvalidCommandArgument("§c" + ex.getMessage(), false);
            }
            return color;
        });
    }
}
