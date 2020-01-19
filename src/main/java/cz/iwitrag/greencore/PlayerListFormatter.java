package cz.iwitrag.greencore;

import cz.iwitrag.greencore.helpers.TaskChainHelper;
import cz.iwitrag.greencore.helpers.LuckPermsHelper;
import cz.iwitrag.greencore.playerbase.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListFormatter implements Listener {

    public PlayerListFormatter() {
        applyTabHeaderAndFooterForAllPlayers();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                setPlayerTabName(player);
            }
        }, 0, 200); // Refresh every 10 seconds
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        applyTabHeaderAndFooterForAllPlayers();
        setPlayerTabName(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        applyTabHeaderAndFooterForAllPlayers();
    }

    // TODO - change Tab Info based on some Vanish listener from Essentials and dont use PlayerCommandPreprocess
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().split(" ")[0].equalsIgnoreCase("/vanish")) {
            applyTabHeaderAndFooterForAllPlayers();
        }
    }

    private void applyTabHeaderAndFooterForAllPlayers() {
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            int amountOfPlayers = PlayerUtils.getPlayersOnlineExceptVanished();
            String playerAmountInfo = "§7Na serveru právě nikdo nehraje.";
            if (amountOfPlayers == 1)
                playerAmountInfo = "§7Na serveru právě hraje §f" + amountOfPlayers + " §7hráč";
            else if (amountOfPlayers >= 2 && amountOfPlayers <= 4)
                playerAmountInfo = "§7Na serveru právě hrají §f" + amountOfPlayers + " §7hráči";
            else if (amountOfPlayers >= 5)
                playerAmountInfo = "§7Na serveru právě hraje §f" + amountOfPlayers + " §7hráčů";

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.setPlayerListHeader("§2----- §aGreenlandia.cz §2-----\n" + playerAmountInfo + "\n "); // last space is important for newline to work correctly);
                player.setPlayerListFooter("\n§7Děkujeme že hraješ na Greenlandii §f:)\n" + "§2Máš nějaký nápad, bug nebo připomínku?\n" +
                                "§2Použij příkaz §a/bug <zpráva>");
            }
        }, 3);
    }

    private void setPlayerTabName(Player player) {
        TaskChainHelper.newChain()
        .asyncFirst(() -> LuckPermsHelper.getPlayerMainPrefix(player.getName()))
        .syncLast((prefix) -> player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', prefix + player.getName())))
        .execute();
    }

}
