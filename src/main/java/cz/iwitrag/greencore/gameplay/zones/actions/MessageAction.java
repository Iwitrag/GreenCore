package cz.iwitrag.greencore.gameplay.zones.actions;

import org.bukkit.entity.Player;

public class MessageAction extends Action {

    private String message;

    public MessageAction(String message) {
        this.message = message;
        validateParameters();
    }

    @Override
    public String getDescription() {
        return "Vypíše zprávu §f'" + message + "§f'";
    }

    @Override
    public void execute(Player player) {
        player.sendMessage(message);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
        validateParameters();
    }

    private void validateParameters() {
        if (message == null)
            message = "";
    }
}
