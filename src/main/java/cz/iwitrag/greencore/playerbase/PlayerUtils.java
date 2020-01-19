package cz.iwitrag.greencore.playerbase;

import com.earth2me.essentials.Essentials;
import cz.iwitrag.greencore.helpers.DependenciesProvider;
import org.bukkit.Bukkit;

public class PlayerUtils {

    private PlayerUtils() {}

    public static int getPlayersOnlineExceptVanished() {
        Essentials essentials = DependenciesProvider.getInstance().getEssentials();
        if (essentials == null)
            return Bukkit.getOnlinePlayers().size();
        else
            return Bukkit.getOnlinePlayers().size() - essentials.getVanishedPlayers().size();
    }

}
