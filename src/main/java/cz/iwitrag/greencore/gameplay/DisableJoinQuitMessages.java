package cz.iwitrag.greencore.gameplay;

        import org.bukkit.event.EventHandler;
        import org.bukkit.event.Listener;
        import org.bukkit.event.player.PlayerJoinEvent;

public class DisableJoinQuitMessages implements Listener {

    // Because of null warning
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
    }

    // Not needed for now, Authme handles that
    /*
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }
    */
}
