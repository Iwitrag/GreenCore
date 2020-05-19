package cz.iwitrag.greencore.gameplay.commands.contexts;

import co.aikar.commands.PaperCommandManager;
import cz.iwitrag.greencore.helpers.DependenciesProvider;

public abstract class AbstractContext {

    protected PaperCommandManager paperCommandManager;

    public AbstractContext() {
        paperCommandManager = DependenciesProvider.getInstance().getPaperCommandManager();
    }

    public abstract void registerCommandContext();
}
