package cz.iwitrag.greencore;

import cz.iwitrag.greencore.premium.PremiumSMSHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.RemoteServerCommandEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RconListener implements Listener {

    private final String prefix = "<RCON> ";

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRconCommand(RemoteServerCommandEvent event) {
        Main.getInstance().getLogger().info(prefix + "Command is: " + event.getCommand());
        List<String> cmdWords = Arrays.asList(event.getCommand().trim().replaceAll(" +", " ").split(" "));

        if (cmdWords.get(0).equalsIgnoreCase("premium")) {
            Main.getInstance().getLogger().info(prefix + "Detected premium SMS, processing...");

            if (cmdWords.size() < 3) {
                Main.getInstance().getLogger().severe(prefix + "Premium command was too short!");
                return;
            }

            List<String> params = new ArrayList<>();
            if (cmdWords.size() > 3)
                params.addAll(cmdWords.subList(3, cmdWords.size()));

            new PremiumSMSHandler().handleSMS(Integer.parseInt(cmdWords.get(1)), cmdWords.get(2), params);
        }

        else { // Security
            Main.getInstance().getLogger().warning(prefix + "Unauthorized access! Command blocked!");
            event.setCancelled(true);
        }
    }

}
