package cz.iwitrag.greencore;

import cz.iwitrag.greencore.helpers.ConfigurationManager;
import cz.iwitrag.greencore.storage.PersistenceManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

// TOP PRIORITY
// TODO - web remake, PayPal
// TODO - plot world
// TODO - Parkour system, checkpoints
// TODO - storage system for leftover premium items
// TODO - voting (keys + crates, cumulative rewards)
// TODO - menu framework, server menu
// TODO - premium items, simple special item framework, command to spawn special items, one-use items with identificators in NBT

// AFTERPARTY
// TODO - PvP coloseum (but be careful, event items must be removed! Maybe add NBT of zone name onto event item and if player goes outside zone - items dissapears)
// TODO - friend system with seen support
// TODO - chest protection 2 days if not inside residence, warning when opening chest
// TODO - spawn - residence tutorial
// TODO - skill system, decreasing durability usage
// TODO - quests with integrated daily jobs
// TODO - tree capitator, integrate with lumberjack skill, it will give exp only when sapling is placed afterwards
// TODO - setup residence flags & event arenas

// LOW PRIORITY
// TODO - vote party
// TODO - community voting system, vote for sun, day, night, rain, storm
// TODO - command to reload skin
// TODO - greenlandia website tutorials, cookies info, Spring
// TODO - better shop with items
// TODO - custom /PL command (but it must show red or green plugins based on if they work)
// TODO - after dragon resurrection place dragon egg
// TODO - custom warp system
// TODO - spawn - advertisement signs for warps
// TODO - chat commands, chat delay, delete chat...
// TODO - karma system
// TODO - chat lottery system
// TODO - creative worlds, inventory backup, simple protection system (not residence)
// TODO - pvp world exp saving

// FUTURE
// TODO - RPG items, upgrading, new ores
// TODO - paintball minigame
// TODO - auto-events
// TODO - nether portals link problem
// https://www.spigotmc.org/threads/entering-nether-portal-and-leaving-somewhere-else.383080/

public class Main extends JavaPlugin {

    private static Main instance;

    public Main() {
        instance = this;
    }

    public static Main getInstance() {
        if (instance == null)
            instance = new Main();
        return instance;
    }

    @Override
    public void onEnable() {
        getLogger().info("=== Loading dependencies ===");
        long timeSpent = System.nanoTime();
        int amountOfLoadedDependencies = loadDependencies(false);
        timeSpent = System.nanoTime() - timeSpent;
        getLogger().info("=== Finished loading " + amountOfLoadedDependencies + " dependencies in " + timeSpent/1000000.0 + " ms ===");

        // TODO - auto-upload non-provided dependency files into '/lib/green' folder somehow... maven?

        new Init(this);
    }

    @Override
    public void onDisable() {
        PersistenceManager pm = PersistenceManager.getInstance();
        pm.runHibernateTaskNoRunnable(pm::updateAllData, true);
        pm.close();

        ConfigurationManager cm = ConfigurationManager.getInstance();
        cm.saveAllConfigs();
    }

    private static int loadDependencies(@SuppressWarnings("SameParameterValue") boolean debug) {
        int amountLoaded = 0;
        File libFolder = new File(new File(Bukkit.getWorldContainer(), "lib"), "green");
        boolean tryToLoadLibs = false;
        if (libFolder.mkdirs()) {
            if (debug) getInstance().getLogger().warning("Created new folder '/lib/green' !");
            tryToLoadLibs = true;
        }
        else if (libFolder.isDirectory()) {
            if (debug) getInstance().getLogger().info("Folder '/lib/green' was found, reading files...");
            tryToLoadLibs = true;
        }
        else {
            if (debug) getInstance().getLogger().warning("Folder '/lib/green' was not found and could not be created!");
        }
        if (tryToLoadLibs) {
            File[] files = libFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    try {
                        final URL url = file.toURI().toURL();
                        if (debug) getInstance().getLogger().info("Loading dependency " + url.toString());
                        URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
                        final Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                        method.setAccessible(true);
                        method.invoke(classLoader, url);
                        if (debug) getInstance().getLogger().info("> Dependency successfully loaded!");
                        amountLoaded++;
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return amountLoaded;
    }
}
