package cz.iwitrag.greencore.helpers;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import cz.iwitrag.greencore.Main;

public class TaskChainHelper {

    private static TaskChainFactory taskChainFactory = null;

    private TaskChainHelper(){}

    public static <T> TaskChain<T> newChain() {
        if (taskChainFactory == null)
            taskChainFactory = BukkitTaskChainFactory.create(Main.getInstance());
        return taskChainFactory.newChain();
    }
    public static <T> TaskChain<T> newSharedChain(String name) {
        if (taskChainFactory == null)
            taskChainFactory = BukkitTaskChainFactory.create(Main.getInstance());
        return taskChainFactory.newSharedChain(name);
    }
}
