package cz.iwitrag.greencore.helpers;

import co.aikar.commands.PaperCommandManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.earth2me.essentials.Essentials;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import cz.iwitrag.greencore.Main;
import cz.iwitrag.greencore.gameplay.itemdb.ItemDB;
import fr.xephi.authme.api.v3.AuthMeApi;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Logger;

public class DependenciesProvider {

    private static DependenciesProvider instance;

    private LuckPerms luckPerms;
    private Essentials essentials;
    private AuthMeApi authme;
    private PaperCommandManager paperCommandManager;
    private ProtocolManager protocolManager;
    private WorldEditPlugin worldEdit;
    private Logger logger;
    private ItemDB itemDB;

    private DependenciesProvider() {
        instance = this;
    }

    public static DependenciesProvider getInstance() {
        if (instance == null)
            instance = new DependenciesProvider();
        return instance;
    }

    public Essentials getEssentials() {
        if (essentials == null)
            essentials = (Essentials)Bukkit.getPluginManager().getPlugin("Essentials");
        return essentials;
    }

    public LuckPerms getLuckPerms() {
        if (luckPerms == null) {
            RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServer().getServicesManager().getRegistration(LuckPerms.class);
            if (provider != null)
                luckPerms = provider.getProvider();
        }
        return luckPerms;
    }

    public AuthMeApi getAuthMe() {
        if (authme == null) {
            authme = AuthMeApi.getInstance();
        }
        return authme;
    }

    public PaperCommandManager getPaperCommandManager() {
        if (paperCommandManager == null) {
            paperCommandManager = new PaperCommandManager(Main.getInstance());
        }
        return paperCommandManager;
    }

    public ProtocolManager getProtocolLib() {
        if (protocolManager == null) {
            protocolManager = ProtocolLibrary.getProtocolManager();
        }
        return protocolManager;
    }

    public WorldEditPlugin getWorldEdit() {
        if (worldEdit == null) {
            worldEdit = (WorldEditPlugin)Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        }
        return worldEdit;
    }

    public Location getWorldEditSelection(String playerName, boolean firstPoint) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null)
            return null;
        Region region;
        try {
            region = getWorldEdit().getSession(player).getSelection(BukkitAdapter.adapt(player.getWorld()));
        } catch (IncompleteRegionException e) {
            return null;
        }
        BlockVector3 point = firstPoint ? region.getMinimumPoint() : region.getMaximumPoint();
        World w = null;
        if (region.getWorld() != null)
            w = BukkitAdapter.adapt(region.getWorld());
        return new Location(w, point.getX()+0.5, point.getY()+0.5, point.getZ()+0.5);
    }

    /** Sets WorldEdit selection for world player is located */
    public void setWorldEditSelection(String playerName, Location p1, Location p2) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null)
            return;
        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(player.getWorld());
        CuboidRegionSelector selector = new CuboidRegionSelector(world, BlockVector3.at(p1.getX(), p1.getY(), p1.getZ()), BlockVector3.at(p2.getX(), p2.getY(), p2.getZ()));
        getWorldEdit().getSession(player).setRegionSelector(world, selector);
    }

    public ItemDB getDefaultItemDB() {
        if (itemDB == null)
            itemDB = new ItemDB("itemDB.yml");
        return itemDB;
    }

}
