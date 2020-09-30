package cz.iwitrag.greencore;

import co.aikar.commands.PaperCommandManager;
import cz.iwitrag.greencore.auth.JoinTwice;
import cz.iwitrag.greencore.gameplay.*;
import cz.iwitrag.greencore.gameplay.commands.RtpCommand;
import cz.iwitrag.greencore.gameplay.commands.VipCommand;
import cz.iwitrag.greencore.gameplay.commands.artificial.ArtificialCommandsListener;
import cz.iwitrag.greencore.gameplay.commands.contexts.*;
import cz.iwitrag.greencore.gameplay.itemdb.ItemDBCommands;
import cz.iwitrag.greencore.gameplay.lottery.LotteryCommand;
import cz.iwitrag.greencore.gameplay.oregen.MiningOreGeneration;
import cz.iwitrag.greencore.gameplay.playerskills.SkillsListener;
import cz.iwitrag.greencore.gameplay.treasurechests.TreasureChestCommands;
import cz.iwitrag.greencore.gameplay.treasurechests.TreasureChestListener;
import cz.iwitrag.greencore.gameplay.zones.ZoneCommands;
import cz.iwitrag.greencore.gameplay.zones.ZoneExecutor;
import cz.iwitrag.greencore.gameplay.zones.ZoneFlagsExecutor;
import cz.iwitrag.greencore.helpers.ConfigurationManager;
import cz.iwitrag.greencore.helpers.DependenciesProvider;
import cz.iwitrag.greencore.helpers.LoggerManager;
import cz.iwitrag.greencore.playerbase.GPlayerListener;
import cz.iwitrag.greencore.playerbase.GPlayersManager;
import cz.iwitrag.greencore.storage.PersistenceManager;
import cz.iwitrag.greencore.votifier.VoteCommand;
import cz.iwitrag.greencore.votifier.VoteProcessersManager;
import cz.iwitrag.greencore.votifier.VotifierListener;
import cz.iwitrag.greencore.votifier.processers.CzechCraftVoteProcesser;
import cz.iwitrag.greencore.votifier.processers.VoteProcesser;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.hibernate.HibernateException;
import org.ipvp.canvas.MenuFunctionListener;

import java.util.Locale;

// Init is separated from Main because of errors which were shown BEFORE injecting libs into classpath

public class Init {

    private JavaPlugin plugin;
    
    public Init(JavaPlugin plugin) {
        this.plugin = plugin;

        configureLocale();
        configureLogger();
        registerCommands(DependenciesProvider.getInstance().getPaperCommandManager());
        registerListeners(Bukkit.getPluginManager());
        createInstances();
        listenToCzechCraftVotes();

        // DB
        setupDatabaseConnection();
        loadDataFromDatabase();
        enableAutoUpdater(20*60*3);
    }

    private void configureLocale() {
        Locale.setDefault(Locale.ENGLISH);
    }

    private void configureLogger() {
        LoggerManager loggerManager = LoggerManager.getInstance();

//        /* Log everything in hibernate */
//        loggerManager.addLogger("org.hibernate", Level.ALL);
//        /* Log all SQL statements */
//        loggerManager.addLogger("org.hibernate.SQL", Level.ALL);
//        loggerManager.addLogger("org.hibernate.type.descriptor.sql", Level.ALL);
//        /* Log all schema changes */
//        loggerManager.addLogger("org.hibernate.tool.hbm2ddl", Level.ALL);
    }

    private void registerCommands(PaperCommandManager paperCommandManager) {
        paperCommandManager.enableUnstableAPI("help");
        // Czech lang in ACF temporarily disabled until Aikar builds changes
        //paperCommandManager.getLocales().setDefaultLocale(new Locale("cs", "CZ"));

        new ColorContext().registerCommandContext();
        new CommonContext().registerCommandContext();
        new ItemDBContext().registerCommandContext();
        new ParticleContext().registerCommandContext();
        new TreasureChestContext().registerCommandContext();
        new ZoneContext().registerCommandContext();

        paperCommandManager.registerCommand(new ItemDBCommands());
        paperCommandManager.registerCommand(new LotteryCommand());
        paperCommandManager.registerCommand(new RtpCommand());
        paperCommandManager.registerCommand(new TreasureChestCommands());
        paperCommandManager.registerCommand(new VipCommand());
        paperCommandManager.registerCommand(new VoteCommand());
        paperCommandManager.registerCommand(new ZoneCommands());

    }

    private void registerListeners(PluginManager pluginManager) {
        pluginManager.registerEvents(new AfkPreventer(), plugin);
        pluginManager.registerEvents(new AntiAfkFishing(), plugin);
        pluginManager.registerEvents(new ArtificialCommandsListener(), plugin);
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
        pluginManager.registerEvents(new TreasureChestListener(), plugin);
        pluginManager.registerEvents(new VotifierListener(), plugin);
        pluginManager.registerEvents(ZoneFlagsExecutor.getInstance(), plugin);
        pluginManager.registerEvents(new MenuFunctionListener(), plugin);
    }

    private void createInstances() {
        new TntDupeFixer();
        ZoneExecutor.getInstance();
    }

    private void listenToCzechCraftVotes() {
        VoteProcesser czechCraftReminder = new CzechCraftVoteProcesser();
        czechCraftReminder.startReminding();
        VoteProcessersManager.registerVoteProcesser("Czech-Craft.eu", czechCraftReminder);
    }

    private void setupDatabaseConnection() {
        FileConfiguration configuration = ConfigurationManager.getInstance().getConfig("database.yml");
        String host = configuration.getString("host");
        if (host == null)
            configuration.set("host", "---");
        String port = configuration.getString("port");
        if (port == null) {
            configuration.set("port", "3306");
            port = "3306";
        }
        String database = configuration.getString("database");
        if (database == null)
            configuration.set("database", "---");
        String username = configuration.getString("username");
        if (username == null)
            configuration.set("username", "---");
        String password = configuration.getString("password");
        if (password == null)
            configuration.set("password", "---");
        ConfigurationManager.getInstance().saveConfig("database.yml");
        PersistenceManager.getInstance().setup(host, port, database, username, password);

        try {
            PersistenceManager.getInstance().getHibernateSessionFactory();
        } catch (HibernateException e) {
            e.printStackTrace();
        }
    }

    private void loadDataFromDatabase() {
        PersistenceManager pm = PersistenceManager.getInstance();
        pm.runHibernateAsyncTask((pm::loadAllData), true);
    }

    private void enableAutoUpdater(int intervalTicks) {
        // Database
        PersistenceManager pm = PersistenceManager.getInstance();
        pm.repeatHibernateAsyncTaskLater((pm::updateAllData), true, intervalTicks, intervalTicks);

        // Config files
        ConfigurationManager cm = ConfigurationManager.getInstance();
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                cm.saveAllConfigs();
            }
        };
        runnable.runTaskTimerAsynchronously(Main.getInstance(), intervalTicks, intervalTicks);
    }

}
