package cz.iwitrag.greencore.helpers;

import com.google.common.base.Charsets;
import cz.iwitrag.greencore.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationManager {

    private static ConfigurationManager instance;
    private Map<File, FileConfiguration> configs = new HashMap<>();

    private ConfigurationManager() {}

    public static ConfigurationManager getInstance() {
        if (instance == null)
            instance = new ConfigurationManager();
        return instance;
    }

    /**
     * Returns the configuration saved in specified file<br />
     * If specified file does not exist, it will be created<br />
     * Null or omitted config will lead to standard 'config.yml' file.
     * @param configFileName Config file with extension (yml) and optional path... it will be relative to plugin data folder
     * @return Configuration object
     */
    public FileConfiguration getConfig(String configFileName) {
        if (configFileName == null)
            configFileName = "config.yml";
        File file = new File(Main.getInstance().getDataFolder(), configFileName);
        if (configs.get(file) == null) {
            reloadConfig(configFileName);
            saveConfig(configFileName);
        }
        return configs.get(file);
    }

    /**
     * Returns the configuration saved in 'config.yml' file<br />
     * If 'config.yml' does not exist, it will be created
     * @return Configuration object
     */
    public FileConfiguration getConfig() {
        return getConfig("config.yml");
    }

    /**
     * Reloads (or loads) config from specified file<br />
     * If specified file does not exist, it will be created<br />
     * Null or omitted config will lead to standard 'config.yml' file.
     * @param configFileName Config file with extension (yml) and optional path... it will be relative to plugin data folder
     */
    public void reloadConfig(String configFileName) {
        if (configFileName == null)
            configFileName = "config.yml";
        File file = new File(Main.getInstance().getDataFolder(), configFileName);
        configs.put(file, YamlConfiguration.loadConfiguration(file));

        final InputStream defConfigStream = Main.getInstance().getResource(configFileName);
        if (defConfigStream == null) {
            return;
        }
        configs.get(file).setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));
    }

    public void saveConfig(String configFileName) {
        File file = new File(Main.getInstance().getDataFolder(), configFileName);
        FileConfiguration config = configs.get(file);
        if (config == null) {
            reloadConfig(configFileName);
            return;
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
            Main.getInstance().getLogger().warning("Failed to save config '" + file.getName() + "' on disk!");
        }
    }

    /** Saves all used config files on the disk */
    public void saveAllConfigs() {
        Main.getInstance().getLogger().info("Saving all config files on disk");
        for (Map.Entry<File, FileConfiguration> entry : configs.entrySet()) {
            saveConfig(entry.getKey().getName());
        }
        Main.getInstance().getLogger().info("Saving of " + configs.size() + " config files successful");
    }

}
