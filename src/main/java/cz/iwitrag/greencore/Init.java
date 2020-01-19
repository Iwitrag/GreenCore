package cz.iwitrag.greencore;

import co.aikar.commands.PaperCommandManager;
import cz.iwitrag.greencore.auth.JoinTwice;
import cz.iwitrag.greencore.events.EventItemRemover;
import cz.iwitrag.greencore.gameplay.*;
import cz.iwitrag.greencore.gameplay.commands.RtpCommand;
import cz.iwitrag.greencore.gameplay.commands.VipCommand;
import cz.iwitrag.greencore.gameplay.lottery.LotteryCommand;
import cz.iwitrag.greencore.gameplay.oregen.MiningOreGeneration;
import cz.iwitrag.greencore.gameplay.playerskills.SkillsListener;
import cz.iwitrag.greencore.gameplay.zones.ZoneCommands;
import cz.iwitrag.greencore.gameplay.zones.ZoneExecutor;
import cz.iwitrag.greencore.helpers.DependenciesProvider;
import cz.iwitrag.greencore.helpers.WorldManager;
import cz.iwitrag.greencore.playerbase.GPlayerListener;
import cz.iwitrag.greencore.playerbase.GPlayersManager;
import cz.iwitrag.greencore.votifier.VoteCommand;
import cz.iwitrag.greencore.votifier.VoteProcessersManager;
import cz.iwitrag.greencore.votifier.VotifierListener;
import cz.iwitrag.greencore.votifier.processers.CzechCraftVoteProcesser;
import cz.iwitrag.greencore.votifier.processers.VoteProcesser;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public class Init {
    
    private JavaPlugin plugin;
    
    public Init(JavaPlugin plugin) {

        this.plugin = plugin;
        registerCommands(DependenciesProvider.getInstance().getPaperCommandManager());
        registerListeners(Bukkit.getPluginManager());
        createInstances();
        prepareWorlds();
        listenToCzechCraftVotes();

        /*try {
            factory = new AnnotationConfiguration()
                    .configure().
                    addAnnotatedClass(GPlayer.class).
                    buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Failed to create sessionFactory object." + ex);
            throw new ExceptionInInitializerError(ex);
        }*/

    }

    /*public static SessionFactory getSessionFactory() {
        return factory;
    }*/

    private void registerCommands(PaperCommandManager paperCommandManager) {
        paperCommandManager.enableUnstableAPI("help");
        paperCommandManager.getLocales().setDefaultLocale(new Locale("cs", "CZ"));
        paperCommandManager.registerCommand(new LotteryCommand());
        paperCommandManager.registerCommand(new RtpCommand());
        paperCommandManager.registerCommand(new VipCommand());
        paperCommandManager.registerCommand(new VoteCommand());
        paperCommandManager.registerCommand(new ZoneCommands());
    }

    private void registerListeners(PluginManager pluginManager) {
        pluginManager.registerEvents(new AfkPreventer(), plugin);
        pluginManager.registerEvents(new AntiAfkFishing(), plugin);
        pluginManager.registerEvents(new DisableJoinQuitMessages(), plugin);
        pluginManager.registerEvents(new DisablePhantoms(), plugin);
        pluginManager.registerEvents(new DisableVillagerEmeraldSelling(), plugin);
        pluginManager.registerEvents(new EventItemRemover(), plugin);
        pluginManager.registerEvents(GPlayersManager.getInstance(), plugin);
        pluginManager.registerEvents(new GPlayerListener(), plugin);
        pluginManager.registerEvents(new JoinTwice(), plugin);
        pluginManager.registerEvents(new MiningOreGeneration(), plugin);
        pluginManager.registerEvents(new MobGriefDisabler(), plugin);
        pluginManager.registerEvents(new PistonPlantGrowFixer(), plugin);
        pluginManager.registerEvents(new PlayerListFormatter(), plugin);
        pluginManager.registerEvents(new PlayerHeadListener(), plugin);
        pluginManager.registerEvents(new PushPaper(), plugin);
        pluginManager.registerEvents(new PVPWorldLastLocDisabler(), plugin);
        pluginManager.registerEvents(new RconListener(), plugin);
        pluginManager.registerEvents(new RecipeRevealer(), plugin);
        pluginManager.registerEvents(new ScoreboardUnifier(), plugin);
        pluginManager.registerEvents(new SkillsListener(), plugin);
        pluginManager.registerEvents(new SlimeSpawnLocator(), plugin);
        pluginManager.registerEvents(new SnowManSnowGenerator(), plugin);
        pluginManager.registerEvents(new SurvivalDeathManager(), plugin);
        pluginManager.registerEvents(new ToolDurabilityModifier(), plugin);
        pluginManager.registerEvents(new VotifierListener(), plugin);
    }

    private void createInstances() {
        new TntDupeFixer();
        ZoneExecutor.getInstance();
    }

    private void prepareWorlds() {
        new WorldManager("world", 8000);
        new WorldManager("world_nether", 1000);
        new WorldManager("world_the_end", 8000);
        WorldManager pvpWorld = new WorldManager("pvp", 1000);
        pvpWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        pvpWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        pvpWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        pvpWorld.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
        pvpWorld.setTime(6000); // 6 hours (zero point) + 6 hours
    }

    private void listenToCzechCraftVotes() {
        VoteProcesser czechCraftReminder = new CzechCraftVoteProcesser();
        czechCraftReminder.startReminding();
        VoteProcessersManager.registerVoteProcesser("Czech-Craft.eu", czechCraftReminder);
    }
    
}
