package cz.iwitrag.greencore;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

// TOP PRIORITY
// TODO - zones framework, general zones, command zones
// TODO - new spawn
// TODO - setup residence flags & event arenas
// TODO - premium items, simple special item framework, command to spawn special items, one-use items with identificators
// TODO - chest protection 2 days if not inside residence, warning when opening chest

// AFTERPARTY
// TODO - plot world
// TODO - mines
// TODO - PvP arena (but be careful, event items must be removed! Maybe add lore of region name onto event item and if player goes outside region - items dissapears)
// TODO - LP parkour
// TODO - community voting system, vote for sun, day, night, rain, storm
// TODO - friend system with seen support
// TODO - lottery
// TODO - skill system, decreasing durability usage
// TODO - quests with integrated daily jobs
// TODO - tree capitator, integrate with lumberjack skill, it will give exp only when sapling is placed afterwards
// TODO - voting (keys + crates, cumulative rewards, vote party)

// LOW PRIORITY
// TODO - command to reload skin
// TODO - spawn - residence tutorial
// TODO - greenlandia website tutorials, cookies info, Spring
// TODO - better shop with items
// TODO - custom /PL command (but it must show red or green plugins based on if they work)
// TODO - menu framework, server menu
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

        Bukkit.getLogger().info("=== Loading dependencies ===");
        long timeSpent = System.nanoTime();
        int amountOfLoadedDependencies = loadDependencies(false);
        timeSpent = System.nanoTime() - timeSpent;
        Bukkit.getLogger().info("=== Finished loading " + amountOfLoadedDependencies + " dependencies in " + timeSpent/1000000.0 + " ms ===");

        // TODO - auto-upload non-provided dependency files into '/lib/green' folder somehow... maven?

        new Init(this);
    }

    private static int loadDependencies(@SuppressWarnings("SameParameterValue") boolean debug) {
        int amountLoaded = 0;
        File libFolder = new File(new File(Bukkit.getWorldContainer(), "lib"), "green");
        boolean tryToLoadLibs = false;
        if (libFolder.mkdirs()) {
            if (debug) Bukkit.getLogger().warning("Created new folder '/lib/green' !");
            tryToLoadLibs = true;
        }
        else if (libFolder.isDirectory()) {
            if (debug) Bukkit.getLogger().info("Folder '/lib/green' was found, reading files...");
            tryToLoadLibs = true;
        }
        else {
            if (debug) Bukkit.getLogger().warning("Folder '/lib/green' was not found and could not be created!");
        }
        if (tryToLoadLibs) {
            File[] files = libFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    try {
                        final URL url = file.toURI().toURL();
                        if (debug) Bukkit.getLogger().info("Loading dependency " + url.toString());
                        URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
                        final Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                        method.setAccessible(true);
                        method.invoke(classLoader, url);
                        if (debug) Bukkit.getLogger().info("> Dependency successfully loaded!");
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
