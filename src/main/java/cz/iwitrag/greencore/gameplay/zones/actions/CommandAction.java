package cz.iwitrag.greencore.gameplay.zones.actions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Entity
@DiscriminatorValue("cmd")
public class CommandAction extends Action {

    @Column(name = "cmd_command")
    private String command;

    public CommandAction() {}

    public CommandAction(String command) {
        this.command = command;
        validateParameters();
    }

    @Override
    public String getDescription() {
        char c = command.charAt(0);
        if (c == '#')
            return "Konzole spustí příkaz /" + command.substring(1);
        else if (c == '@')
            return "Hráč jako OP spustí příkaz /" + command.substring(1);
        else
            return "Hráč spustí příkaz " + command;
    }

    @Override
    public void execute(Player player) {
        char c = command.charAt(0);
        String modifiedCommand = command;
        if (c == '#') {
            List<String> toReplace = Arrays.asList(
                    "{PLAYER}", "{HRAC}", "{HRÁČ}", "{NICK}", "{NAME}", "{NICKNAME}",
                    "%PLAYER%", "%HRAC%", "%HRÁČ%", "%NICK%", "%NAME%", "%NICKNAME%",
                    "[PLAYER]", "[HRAC]", "[HRÁČ]", "[NICK]", "[NAME]", "[NICKNAME]"
                    );
            for (String pattern : toReplace) {
                // (?i) makes it case insensitive
                modifiedCommand = modifiedCommand.replaceAll("(?i)" + Pattern.quote(pattern), player.getName());
            }
            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), modifiedCommand.substring(1));
        }
        else if (c == '@') {
            player.setOp(true);
            try {
                player.performCommand(command.substring(1));
            } finally {
                player.setOp(false);
            }
        }
        else {
            player.performCommand(command.substring(1));
        }
    }

    @Override
    public Action copy() {
        return new CommandAction(this.command);
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
        validateParameters();
    }

    protected void validateParameters() {
        if (command == null || command.isEmpty())
            command = "/";
        else {
            char c = command.charAt(0);
            if (c != '/' && c != '#' && c != '@')
                command = "/" + command;
        }
    }

}
