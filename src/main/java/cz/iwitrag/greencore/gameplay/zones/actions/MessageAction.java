package cz.iwitrag.greencore.gameplay.zones.actions;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("msg")
public class MessageAction extends Action {

    @Column(name = "msg_message")
    private String message;

    public MessageAction() {}

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
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    @Override
    public Action copy() {
        return new MessageAction(this.message);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
        validateParameters();
    }

    protected void validateParameters() {
        if (message == null)
            message = "";
    }
}
