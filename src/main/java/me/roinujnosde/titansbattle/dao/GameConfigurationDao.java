package me.roinujnosde.titansbattle.dao;

import me.roinujnosde.titansbattle.types.GameConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameConfigurationDao {

    public static final String GAME_PATH = "game";
    private static GameConfigurationDao instance;
    private static final Map<String, GameConfiguration> GAMES = new ConcurrentHashMap<>();
    private static final Map<String, YamlConfiguration> CONFIG_FILES = new HashMap<>();
    private final @NotNull File gamesFolder;
    private final Logger logger = Logger.getLogger("TitansBattle");

    public static GameConfigurationDao getInstance(@NotNull Plugin plugin) {
        if (instance == null) {
            instance = new GameConfigurationDao(plugin.getDataFolder());
        }
        return instance;
    }

    private GameConfigurationDao(@NotNull File dataFolder) {
        gamesFolder = new File(dataFolder, "games");
        if (!gamesFolder.exists()) {
            if (!gamesFolder.mkdirs()) {
                logger.severe("Error creating the games folder");
            }
        }
        if (GAMES.isEmpty()) {
            loadGameConfigurations();
        }
    }

    public void createGame(@NotNull String game) {
        File file = new File(gamesFolder, game + ".yml");
        try {
            if (!file.createNewFile()) {
                logger.log(Level.SEVERE, String.format("Error creating the game %s's file. Maybe it already exists?",
                        game));
                return;
            }
            GameConfiguration c = new GameConfiguration();
            c.setName(game);
            YamlConfiguration yamlConfiguration = new YamlConfiguration();
            yamlConfiguration.set(GAME_PATH, c);
            yamlConfiguration.save(file);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, String.format("Error creating the game %s's file.", game), ex);
        }
    }

    public void loadGameConfigurations() {
        GAMES.clear();
        CONFIG_FILES.clear();
        File[] files = gamesFolder.listFiles((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
        for (File file : files) {
            YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(file);
            GameConfiguration gc = (GameConfiguration) yamlConfig.get(GAME_PATH);
            if (gc != null) {
                GAMES.put(gc.getName().toLowerCase(), gc);
                CONFIG_FILES.put(file.getName(), yamlConfig);
            }
        }
    }

    public boolean save(@NotNull GameConfiguration config) {
        for (Map.Entry<String, YamlConfiguration> entry : CONFIG_FILES.entrySet()) {
            YamlConfiguration entryConfig = entry.getValue();
            GameConfiguration entryGame = (GameConfiguration) entryConfig.get(GAME_PATH);
            if (entryGame.equals(config)) {
                entryConfig.set(GAME_PATH, config);
                try {
                    entryConfig.save(new File(gamesFolder, entry.getKey()));
                    return true;
                } catch (IOException e) {
                    Logger logger = Logger.getLogger("TitansBattle");
                    logger.log(Level.SEVERE, String.format("Error saving game config %s", config.getName()), e);
                }
            }
        }
        return false;
    }

    public @Nullable GameConfiguration getGameConfiguration(@NotNull String name) {
        return GAMES.get(name.toLowerCase());
    }

    public @Nullable YamlConfiguration getConfigFile(@Nullable GameConfiguration gameConfig) {
        for (YamlConfiguration yamlConfig : CONFIG_FILES.values()) {
            GameConfiguration gameConfiguration = (GameConfiguration) yamlConfig.get(GAME_PATH);
            if (gameConfiguration.equals(gameConfig)) {
                return yamlConfig;
            }
        }
        return null;
    }

    public Map<String, GameConfiguration> getGameConfigurations() {
        return GAMES;
    }
}
