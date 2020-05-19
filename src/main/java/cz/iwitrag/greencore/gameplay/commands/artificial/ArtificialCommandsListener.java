package cz.iwitrag.greencore.gameplay.commands.artificial;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ArtificialCommandsListener implements Listener {

    @EventHandler
    public void onArtificialCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();
        if (command.charAt(0) != '/')
            command = "/" + command;

        String[] split = command.split(" ");
        if (split.length > 1 && split[0].equalsIgnoreCase(ArtificialCommandsManager.ARTIFICIAL_CMD)) {
            int number = -1;
            try {
                number = Integer.parseInt(split[1]);
            } catch (NumberFormatException ignored) { }
            if (number != -1) {
                ArtificialCommand cmd = ArtificialCommandsManager.getInstance().getCommand(number);
                if (cmd != null) {
                    event.setCancelled(true);
                    cmd.getRunnable().run();
                }
            }
        }
    }

}
