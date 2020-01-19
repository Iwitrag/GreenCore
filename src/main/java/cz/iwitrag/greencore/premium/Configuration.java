package cz.iwitrag.greencore.premium;

import cz.iwitrag.greencore.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class Configuration {

    private Set<PremiumService> services;

    private Configuration(Set<PremiumService> services) {
        this.services = services;
    }

    Set<PremiumService> getServices() {
        return new HashSet<>(services);
    }

    PremiumService getServiceByInternalName(String internalName) {
        for (PremiumService service : services) {
            if (service.getInternalName().equalsIgnoreCase(internalName))
                return service;
        }
        return null;
    }

    static Configuration loadConfiguration(String url) throws IOException, InvalidConfigurationException {
        URL address = new URL(url);
        URLConnection connection = address.openConnection();
        connection.setUseCaches(false);
        InputStream inputStream = connection.getInputStream();
        Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        FileConfiguration fileConfiguration = new YamlConfiguration();
        fileConfiguration.load(reader);
        reader.close();

        Main.getInstance().getLogger().info("Premium services file loaded, parsing YML structure...");

        Set<PremiumService> services = new HashSet<>();
        ConfigurationSection itemsConfiguration = fileConfiguration.getConfigurationSection("items");
        if (itemsConfiguration != null) {
            for (String serviceName : itemsConfiguration.getKeys(false)) {
                ConfigurationSection serviceConfiguration = itemsConfiguration.getConfigurationSection(serviceName);
                if (serviceConfiguration != null && serviceConfiguration.contains("variants")) {
                    ConfigurationSection variantsConfiguration = serviceConfiguration.getConfigurationSection("variants");
                    if (variantsConfiguration != null) {
                        Set<PremiumVariant> variants = new HashSet<>();
                        for (String variantKeyword : variantsConfiguration.getKeys(false)) {
                            ConfigurationSection variantConfiguration = variantsConfiguration.getConfigurationSection(variantKeyword);
                            if (variantConfiguration != null) {
                                String friendlyName = variantConfiguration.getString("friendly name");
                                if (friendlyName == null) {
                                    friendlyName = "Neznámá varianta";
                                    Main.getInstance().getLogger().warning("Variant " + variantKeyword + " of premium service " + serviceName + " has no friendly name defined!");
                                }
                                String description = variantConfiguration.getString("description");
                                if (description == null) {
                                    description = "Žádný popis varianty";
                                    Main.getInstance().getLogger().warning("Variant " + variantKeyword + " of premium service " + serviceName + " has no description defined!");
                                }
                                int price = variantConfiguration.getInt("price");
                                if (price == 0) {
                                    Main.getInstance().getLogger().warning("Variant " + variantKeyword + " of premium service " + serviceName + " had no or zero price defined! Skipped!");
                                    continue;
                                }
                                String params = variantConfiguration.getString("params");
                                List<String> paramsList;
                                if (params != null && params.length() > 0)
                                    paramsList = Arrays.asList(params.trim().replaceAll(" +", " ").split(" "));
                                else
                                    paramsList = new ArrayList<>();
                                variants.add(new PremiumVariant(variantKeyword, friendlyName, description, price, paramsList));
                            }
                        }
                        if (!variants.isEmpty()) {
                            String friendlyName = serviceConfiguration.getString("friendly name");
                            if (friendlyName == null) {
                                friendlyName = "Neznámá služba";
                                Main.getInstance().getLogger().warning("Premium service " + serviceName + " has no friendly name defined!");
                            }
                            String description = serviceConfiguration.getString("description");
                            if (description == null) {
                                description = "Žádný popis služby";
                                Main.getInstance().getLogger().warning("Premium service " + serviceName + " has no description defined!");
                            }
                            services.add(new PremiumService(serviceName, friendlyName, serviceConfiguration.getBoolean("active"), description, variants));
                            Main.getInstance().getLogger().info(serviceName + " active: " + serviceConfiguration.getBoolean("active"));
                        }
                    }
                }
            }
        }

        if (services.size() == 0)
            Main.getInstance().getLogger().warning("No premium services have been found!");
        else {
            StringBuilder message = new StringBuilder("Loaded " + services.size() + " premium services: ");
            for (PremiumService service : services) {
                message.append(service.getInternalName()).append("(").append(service.getVariants().size()).append("), ");
            }
            message.setLength(message.length() - 2);
            message.append(".");
            Main.getInstance().getLogger().info(message.toString());
        }

        return new Configuration(services);
    }

}
