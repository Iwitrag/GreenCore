package cz.iwitrag.greencore.helpers;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class LoggerManager {

    private static LoggerManager instance;

    static {
        System.setProperty("org.jboss.logging.provider", "log4j2");
    }

    private Logger logger;

    private LoggerManager() {
        instance = this;
    }

    public static LoggerManager getInstance() {
        if (instance == null)
            instance = new LoggerManager();
        return instance;
    }

    /** Adds Logger with default appenders ("SysOut", "File", "ServerGuiConsole", "TerminalConsole") */
    public void addLogger(String name, Level level) {
        addLogger(name, level, "SysOut", "File", "ServerGuiConsole", "TerminalConsole");
    }

    /** Adds Logger with custom appenders */
    public void addLogger(String name, Level level, String... appenders) {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();

        List<AppenderRef> appenderRefs = new ArrayList<>();
        for (String appenderParam : appenders) {
            appenderRefs.add(AppenderRef.createAppenderRef(appenderParam, null, null));
        }
        final LoggerConfig loggerConfig = LoggerConfig.createLogger(false, level, name, "true", appenderRefs.toArray(new AppenderRef[0]), null, config, null);

        config.addLogger(name, loggerConfig);
        ctx.updateLoggers();
    }

}
