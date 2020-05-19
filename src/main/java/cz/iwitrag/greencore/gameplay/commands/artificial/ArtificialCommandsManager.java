package cz.iwitrag.greencore.gameplay.commands.artificial;

import java.util.HashMap;
import java.util.Map;

public class ArtificialCommandsManager {

    public static final String ARTIFICIAL_CMD = "/artificial_cmd";

    private static ArtificialCommandsManager instance;

    private Map<Integer, ArtificialCommand> commands = new HashMap<>();
    private int freeId = 0;

    private ArtificialCommandsManager() {}

    public static ArtificialCommandsManager getInstance() {
        if (instance == null)
            instance = new ArtificialCommandsManager();
        return instance;
    }

    public ArtificialCommand getCommand(int id) {
        return commands.get(id);
    }

    public boolean hasCommand(int id) {
        return commands.containsKey(id);
    }

    public ArtificialCommand registerCommand(Runnable runnable) {
        ArtificialCommand command = new ArtificialCommand(freeId, runnable);
        commands.put(freeId, command);
        freeId++;
        return command;
    }

}
