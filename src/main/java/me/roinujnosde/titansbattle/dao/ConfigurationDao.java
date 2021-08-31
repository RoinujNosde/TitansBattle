package me.roinujnosde.titansbattle.dao;

import me.roinujnosde.titansbattle.BaseGameConfiguration;
import me.roinujnosde.titansbattle.challenges.ArenaConfiguration;
import me.roinujnosde.titansbattle.types.GameConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigurationDao {

    private final Logger logger = Logger.getLogger("TitansBattle");
    private final Map<Class<? extends BaseGameConfiguration>, Metadata> metadataMap;
    private final Set<BaseGameConfiguration> configurations;

    public ConfigurationDao(@NotNull File dataFolder) {
        metadataMap = new HashMap<>();
        metadataMap.put(ArenaConfiguration.class, new Metadata(new File(dataFolder, "arenas"), "arena"));
        metadataMap.put(GameConfiguration.class, new Metadata(new File(dataFolder, "games"), "game"));
        configurations = new HashSet<>();

        loadConfigurations();
    }

    public void loadConfigurations() {
        configurations.clear();
        for (Map.Entry<Class<? extends BaseGameConfiguration>, Metadata> entry : metadataMap.entrySet()) {
            File folder = entry.getValue().folder;
            if (!folder.exists() && !folder.mkdirs()) {
                logger.log(Level.SEVERE, "Error creating folder {0}", folder.getAbsolutePath());
                continue;
            }
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
            //noinspection ConstantConditions
            for (File file : files) {
                YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(file);
                BaseGameConfiguration gc = (BaseGameConfiguration) yamlConfig.get(entry.getValue().configKey);
                if (gc != null) {
                    configurations.add(gc);
                    gc.setFile(file);
                    gc.setFileConfiguration(yamlConfig);
                }
            }
        }
    }

    public @NotNull <T extends BaseGameConfiguration> Set<T> getConfigurations(@NotNull Class<T> clazz) {
        Set<T> set = new HashSet<>();
        for (BaseGameConfiguration configuration : configurations) {
            if (clazz.isInstance(configuration)) {
                set.add(clazz.cast(configuration));
            }
        }
        return set;
    }

    public @NotNull <T extends BaseGameConfiguration> Optional<T> getConfiguration(@NotNull String name,
                                                                                   @NotNull Class<T> clazz) {
        Set<T> configurations = getConfigurations(clazz);
        for (T configuration : configurations) {
            if (configuration.getName().equalsIgnoreCase(name)) {
                return Optional.of(configuration);
            }
        }
        return Optional.empty();
    }

    public <T extends BaseGameConfiguration> boolean create(@NotNull String name, @NotNull Class<T> clazz) {
        Metadata metadata = metadataMap.get(clazz);
        if (metadata == null) {
            throw new IllegalArgumentException(String.format("Invalid config class: %s", clazz.getName()));
        }

        File file = new File(metadata.folder, name + ".yml");
        try {
            if (!file.createNewFile()) {
                logger.log(Level.SEVERE, String.format("Error creating the config %s's file. Maybe it already exists?",
                        name));
                return false;
            }
            T config = clazz.getConstructor().newInstance();
            config.setName(name);
            config.setFile(file);

            YamlConfiguration yamlConfiguration = new YamlConfiguration();
            config.setFileConfiguration(yamlConfiguration);
            yamlConfiguration.set(metadata.configKey, config);
            yamlConfiguration.save(file);
            configurations.add(config);
            return true;
        } catch (IOException | ReflectiveOperationException ex) {
            logger.log(Level.SEVERE, String.format("Error creating the config %s", name), ex);
        }
        return false;
    }

    public <T extends BaseGameConfiguration> boolean save(T config) {
        Metadata metadata = metadataMap.get(config.getClass());
        if (metadata == null) {
            logger.log(Level.SEVERE, "Invalid config class {0}", config.getClass().getName());
            return false;
        }
        FileConfiguration fileConfiguration = config.getFileConfiguration();
        fileConfiguration.set(metadata.configKey, config);
        try {
            fileConfiguration.save(config.getFile());
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error saving config", e);
        }
        return false;
    }

    private static class Metadata {
        File folder;
        String configKey;

        public Metadata(File folder, String configKey) {
            this.folder = folder;
            this.configKey = configKey;
        }
    }


}
