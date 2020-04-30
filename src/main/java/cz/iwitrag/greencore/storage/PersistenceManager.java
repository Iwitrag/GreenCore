package cz.iwitrag.greencore.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import cz.iwitrag.greencore.Main;
import cz.iwitrag.greencore.gameplay.zones.Zone;
import cz.iwitrag.greencore.gameplay.zones.ZoneException;
import cz.iwitrag.greencore.gameplay.zones.ZoneManager;
import cz.iwitrag.greencore.gameplay.zones.actions.Action;
import cz.iwitrag.greencore.gameplay.zones.actions.CommandAction;
import cz.iwitrag.greencore.gameplay.zones.actions.DamageAction;
import cz.iwitrag.greencore.gameplay.zones.actions.MessageAction;
import cz.iwitrag.greencore.gameplay.zones.actions.NothingAction;
import cz.iwitrag.greencore.gameplay.zones.actions.PotionEffectAction;
import cz.iwitrag.greencore.gameplay.zones.actions.TeleportAction;
import cz.iwitrag.greencore.gameplay.zones.flags.BlockedCommandsFlag;
import cz.iwitrag.greencore.gameplay.zones.flags.DisconnectPenaltyFlag;
import cz.iwitrag.greencore.gameplay.zones.flags.EnderPortalFlag;
import cz.iwitrag.greencore.gameplay.zones.flags.Flag;
import cz.iwitrag.greencore.gameplay.zones.flags.MineFlag;
import cz.iwitrag.greencore.gameplay.zones.flags.ParticlesFlag;
import cz.iwitrag.greencore.gameplay.zones.flags.TpFlag;
import cz.iwitrag.greencore.storage.converters.ColorConverter;
import cz.iwitrag.greencore.storage.converters.LocationConverter;
import cz.iwitrag.greencore.storage.converters.MineFlagBlocksConverter;
import cz.iwitrag.greencore.storage.converters.PotionEffectConverter;
import cz.iwitrag.greencore.storage.converters.WorldConverter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import javax.persistence.PersistenceException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class PersistenceManager {

    // Maximum time that a client will wait for a connection from the pool
    // If this time is exceeded without a connection becoming available, a SQLException will be thrown
    private static final int HIKARI_CONNECTION_TIMEOUT = 1000 * 10;

    // Maximum time that a connection is allowed to sit idle in the pool
    private static final int HIKARI_IDLE_TIMEOUT = 1000 * 60 * 5;

    // Minimum number of idle connections that HikariCP tries to maintain in the pool
    private static final int HIKARI_MINIMUM_IDLE = 3;

    // Maximum number of actual connection in the pool
    private static final int HIKARI_MAXIMUM_POOL_SIZE = 6;

    // JDBC connection options
    private static final String jdbcOptions = "?useSSL=false" +
            "&useUnicode=true" +
            "&characterEncoding=UTF-8" +
            "&connectionCollation=utf8_general_ci";


    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private HikariDataSource hikariDataSource;
    private SessionFactory hibernateSessionFactory;

    private static PersistenceManager instance;

    private PersistenceManager() {}

    public static PersistenceManager getInstance() {
        if (instance == null)
            instance = new PersistenceManager();
        return instance;
    }

    public void close() {
        if (hikariDataSource != null && !hikariDataSource.isClosed()) {
            hikariDataSource.close();
            hikariDataSource = null;
        }
        if (hibernateSessionFactory != null && !hibernateSessionFactory.isClosed()) {
            hibernateSessionFactory.close();
            hibernateSessionFactory = null;
        }
    }

    public void setup(String host, String port, String database, String username, String password) {
        close();
        this.host = host;
        this.port = Integer.parseInt(port);
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public void setup(String host, String database, String username, String password) {
        setup(host, "3306", database, username, password);
    }

    public Connection getHikariConnection() throws SQLException {
        if (hikariDataSource == null) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + jdbcOptions);
            config.setUsername(username);
            config.setPassword(password);
            config.addDataSourceProperty("cachePrepStmts" , "true");
            config.addDataSourceProperty("useServerPrepStmts" , "true");
            config.addDataSourceProperty("prepStmtCacheSize" , "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit" , "2048");
            config.addDataSourceProperty("connectionTimeout", Integer.toString(HIKARI_CONNECTION_TIMEOUT));
            config.addDataSourceProperty("idleTimeout", Integer.toString(HIKARI_IDLE_TIMEOUT));
            config.addDataSourceProperty("minimumIdle", Integer.toString(HIKARI_MINIMUM_IDLE));
            config.addDataSourceProperty("maximumPoolSize", Integer.toString(HIKARI_MAXIMUM_POOL_SIZE));
            HikariDataSource newHikariDataSource = new HikariDataSource(config);
            Connection connection = newHikariDataSource.getConnection();
            hikariDataSource = newHikariDataSource;
            return connection;
        }
        else {
            return hikariDataSource.getConnection();
        }
    }

    public SessionFactory getHibernateSessionFactory() throws HibernateException {
        if (hibernateSessionFactory == null) {
            Configuration configuration = new Configuration();

            configuration.setProperty("hibernate.dialect", "cz.iwitrag.greencore.storage.ImprovedMySQL5InnoDBDialect");
            configuration.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
            configuration.setPhysicalNamingStrategy(new PrefixedTableNamingStrategy());

            configuration.setProperty("hibernate.connection.url", "jdbc:mysql://" + host + ":" + port + "/" + database + jdbcOptions);
            configuration.setProperty("hibernate.connection.username", username);
            configuration.setProperty("hibernate.connection.password", password);

            configuration.setProperty("hibernate.connection.useUnicode", "true");
            configuration.setProperty("hibernate.connection.CharSet", "utf8");
            configuration.setProperty("hibernate.connection.characterEncoding", "utf8");

            configuration.setProperty("hibernate.hikari.connectionTimeout", Integer.toString(HIKARI_CONNECTION_TIMEOUT));
            configuration.setProperty("hibernate.hikari.idleTimeout", Integer.toString(HIKARI_IDLE_TIMEOUT));
            configuration.setProperty("hibernate.hikari.minimumIdle", Integer.toString(HIKARI_MINIMUM_IDLE));
            configuration.setProperty("hibernate.hikari.maximumPoolSize", Integer.toString(HIKARI_MAXIMUM_POOL_SIZE));

            configuration.setProperty("hibernate.show_sql", "false");
            configuration.setProperty("hibernate.format_sql", "false");
            configuration.setProperty("hibernate.use_sql_comments", "true");

            configuration.setProperty("hibernate.hbm2ddl.auto", "update");

            // CONVERTERS
            configuration.addAnnotatedClass(ColorConverter.class);
            configuration.addAnnotatedClass(ItemStack.class);
            configuration.addAnnotatedClass(LocationConverter.class);
            configuration.addAnnotatedClass(MineFlagBlocksConverter.class);
            configuration.addAnnotatedClass(PotionEffectConverter.class);
            configuration.addAnnotatedClass(WorldConverter.class);

            // ZONES
            configuration.addAnnotatedClass(Zone.class);

            // ZONES - ACTIONS
            configuration.addAnnotatedClass(Action.class);
            configuration.addAnnotatedClass(CommandAction.class);
            configuration.addAnnotatedClass(DamageAction.class);
            configuration.addAnnotatedClass(MessageAction.class);
            configuration.addAnnotatedClass(NothingAction.class);
            configuration.addAnnotatedClass(PotionEffectAction.class);
            configuration.addAnnotatedClass(TeleportAction.class);

            // ZONES - FLAGS
            configuration.addAnnotatedClass(Flag.class);
            configuration.addAnnotatedClass(BlockedCommandsFlag.class);
            configuration.addAnnotatedClass(DisconnectPenaltyFlag.class);
            configuration.addAnnotatedClass(EnderPortalFlag.class);
            configuration.addAnnotatedClass(MineFlag.class);
            configuration.addAnnotatedClass(ParticlesFlag.class);
            configuration.addAnnotatedClass(TpFlag.class);

            Thread.currentThread().setContextClassLoader(Main.class.getClassLoader());

            hibernateSessionFactory = configuration.buildSessionFactory();
        }
        return hibernateSessionFactory;
    }

    public void runHibernateTask(HibernateTask task, boolean autoTransaction, boolean async, Long delay, Long period) {
        BukkitRunnable bukkitRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    runHibernateTaskNoRunnable(task, autoTransaction);
                } catch (PersistenceException ignored) { }
            }
        };

        if (delay == null || delay <= 0)
            delay = 0L;
        if (period != null && period <= 0)
            period = null;

        if (async) {
            if (period == null)
                bukkitRunnable.runTaskLaterAsynchronously(Main.getInstance(), delay);
            else
                bukkitRunnable.runTaskTimerAsynchronously(Main.getInstance(), delay, period);
        } else {
            if (period == null)
                bukkitRunnable.runTaskLater(Main.getInstance(), delay);
            else
                bukkitRunnable.runTaskTimer(Main.getInstance(), delay, period);
        }
    }

    public void runHibernateTaskNoRunnable(HibernateTask task, boolean autoTransaction) throws PersistenceException {
        Transaction transaction = null;
        try (Session session = getHibernateSessionFactory().openSession()) {
            if (autoTransaction)
                transaction = session.beginTransaction();
            task.run(session);
            if (autoTransaction && transaction != null && transaction.isActive())
                session.getTransaction().commit();
        } catch (PersistenceException ex) {
            if (autoTransaction && transaction != null && transaction.isActive())
                transaction.rollback();
            throw ex;
        }
    }

    public void runHibernateAsyncTask(HibernateTask task, boolean autoTransaction) { runHibernateTask(task, autoTransaction, true, null, null); }
    public void runHibernateSyncTask(HibernateTask task, boolean autoTransaction) { runHibernateTask(task, autoTransaction, false, null, null); }
    public void repeatHibernateAsyncTask(HibernateTask task,boolean autoTransaction, long period) { runHibernateTask(task, autoTransaction, true, null, period); }
    public void repeatHibernateSyncTask(HibernateTask task, boolean autoTransaction, long period) { runHibernateTask(task, autoTransaction, false, null, period); }
    public void runHibernateAsyncTaskLater(HibernateTask task, boolean autoTransaction, long delay) { runHibernateTask(task, autoTransaction, true, delay, null); }
    public void runHibernateSyncTaskLater(HibernateTask task, boolean autoTransaction, long delay) { runHibernateTask(task, autoTransaction, false, delay, null); }
    public void repeatHibernateAsyncTaskLater(HibernateTask task, boolean autoTransaction, long period, long delay) { runHibernateTask(task, autoTransaction, true, delay, period); }
    public void repeatHibernateSyncTaskLater(HibernateTask task, boolean autoTransaction, long period, long delay) { runHibernateTask(task, autoTransaction, false, delay, period); }

    @SuppressWarnings("unchecked")
    public void loadAllData(Session session) {
        Main.getInstance().getLogger().info("Loading data from DB...");

        // ZONES

        ZoneManager manager = ZoneManager.getInstance();
        List<Zone> zones = session.createQuery("from Zone").list();
        for (Zone zone : zones) {
            try {
                manager.addZone(zone, false);
            } catch (ZoneException e) {
                Main.getInstance().getLogger().severe("Failed to add Zone from DB " + zone.getName() + " to ZoneManager, reason: " + e.getMessage());
            }
        }

        // ADD OTHER STUFF


    }

    public void updateZoneData(Session session) {
        Main.getInstance().getLogger().info("Updating zone data in DB...");
        for (Zone zone : ZoneManager.getInstance().getZones()){
            session.update(zone);
        }
    }

    public void updateAllData(Session session) {

        updateZoneData(session);

        // ADD OTHER STUFF


    }
}
