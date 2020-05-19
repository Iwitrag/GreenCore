package cz.iwitrag.greencore.gameplay.commands.artificial;

public class ArtificialCommand {

    private int id;
    private Runnable runnable;

    public ArtificialCommand (int id, Runnable runnable) {
        this.id = id;
        this.runnable = runnable;
    }

    public int getId() {
        return id;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public String getCommandText() {
        return ArtificialCommandsManager.ARTIFICIAL_CMD + " " + id;
    }
}
