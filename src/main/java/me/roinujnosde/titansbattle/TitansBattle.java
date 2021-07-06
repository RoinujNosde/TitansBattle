/* *****************************************************************************
 * Copyright (C) 2017 Edson Passos - edsonpassosjr@outlook.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ***************************************************************************** */
package me.roinujnosde.titansbattle;

import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import me.roinujnosde.titansbattle.challenges.ArenaConfiguration;
import me.roinujnosde.titansbattle.commands.TBCommands;
import me.roinujnosde.titansbattle.dao.GameConfigurationDao;
import me.roinujnosde.titansbattle.games.Game;
import me.roinujnosde.titansbattle.listeners.*;
import me.roinujnosde.titansbattle.managers.*;
import me.roinujnosde.titansbattle.types.GameConfiguration;
import me.roinujnosde.titansbattle.types.Kit;
import me.roinujnosde.titansbattle.types.Prizes;
import me.roinujnosde.titansbattle.types.Winners;
import me.roinujnosde.titansbattle.utils.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author RoinujNosde
 *
 */
public final class TitansBattle extends JavaPlugin {

    private static TitansBattle instance;
    private PaperCommandManager pcm;
    private GameManager gameManager;
    private ConfigManager configManager;
    private TaskManager taskManager;
    private LanguageManager languageManager;
    private DatabaseManager databaseManager;
    private @Nullable GroupManager groupManager;
    private GameConfigurationDao gameConfigurationDao;

    @Override
    public void onEnable() {
        saveDefaultConfig(); // TODO Copy defaults
        registerSerializationClasses();
        instance = this;
        gameManager = new GameManager();
        configManager = new ConfigManager();
        taskManager = new TaskManager();
        languageManager = new LanguageManager();
        databaseManager = new DatabaseManager();
        gameConfigurationDao = GameConfigurationDao.getInstance(this);

        configManager.load();
        languageManager.setup();
        databaseManager.setup();

        loadGroupsPlugin();

        pcm = new PaperCommandManager(this);
        pcm.enableUnstableAPI("help");
        configureCommands();
        registerEvents();
        databaseManager.loadDataToMemory();
        gameManager.startOrSchedule();
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new TBExpansion(this).register();
        }
    }

    private void registerSerializationClasses() {
        ConfigurationSerialization.registerClass(ArenaConfiguration.class);
        ConfigurationSerialization.registerClass(GameConfiguration.Prize.class);
        ConfigurationSerialization.registerClass(GameConfiguration.class);
        ConfigurationSerialization.registerClass(Kit.class);
        ConfigurationSerialization.registerClass(Prizes.class);
    }

    private void configureCommands() {
        setDefaultLocale();
        registerDependencies();
        registerCompletions();
        registerContexts();
        registerReplacements();
        registerConditions();
        registerCommands();
    }

    private void setDefaultLocale() {
        String[] s = configManager.getLanguage().split("_");
        pcm.getLocales().setDefaultLocale(new Locale(s[0]));
    }

    private void registerReplacements() {
        ConfigurationSection commandsSection = getConfig().getConfigurationSection("commands");
        if (commandsSection == null) {
            return;
        }
        Set<String> commands = commandsSection.getKeys(false);
        for (String command : commands) {
            pcm.getCommandReplacements().addReplacement(command, commandsSection.getString(command) + "|" + command);
        }
    }

    private void registerContexts() {
        pcm.getCommandContexts().registerIssuerOnlyContext(Game.class, supplier -> gameManager.getCurrentGame().orElse(null));
        pcm.getCommandContexts().registerContext(Date.class, supplier -> {
            try {
                return new SimpleDateFormat(configManager.getDateFormat()).parse(supplier.popFirstArg());
            } catch (ParseException ex) {
                supplier.getSender().sendMessage(getLang("invalid-date"));
                throw new InvalidCommandArgument();
            }
        });
    }

    private void registerCommands() {
        pcm.registerCommand(new TBCommands());
    }

    private void registerConditions() {
        pcm.getCommandConditions().addCondition("happening", handler -> {
            if (!gameManager.getCurrentGame().isPresent()) {
                handler.getIssuer().sendMessage(getLang("not-starting-or-started"));
                throw new ConditionFailedException();
            }
        });
    }

    private void registerCompletions() {
        pcm.getCommandCompletions().registerCompletion("winners_dates", handler -> databaseManager.getWinners()
                .stream().map(Winners::getDate).map(new SimpleDateFormat(configManager.getDateFormat())::format)
                .collect(Collectors.toList()));
        pcm.getCommandCompletions().registerCompletion("group_pages", handler -> {
            int pages = databaseManager.getGroups().size() / configManager.getPageLimitRanking();
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < pages; i++) {
                list.add(String.valueOf(i + 1));
            }
            return list;
        });
        pcm.getCommandCompletions().registerCompletion("warrior_pages", handler -> {
            int pages = databaseManager.getWarriors().size() / configManager.getPageLimitRanking();
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < pages; i++) {
                list.add(String.valueOf(i + 1));
            }
            return list;
        });
        pcm.getCommandCompletions().registerStaticCompletion("prizes_config_fields", ConfigUtils.getEditableFields(Prizes.class));
        pcm.getCommandCompletions().registerStaticCompletion("game_config_fields", ConfigUtils.getEditableFields(GameConfiguration.class));
        pcm.getCommandCompletions().registerStaticCompletion("warrior_order", Arrays.asList("kills", "deaths"));
        pcm.getCommandCompletions().registerStaticCompletion("group_order", Arrays.asList("kills", "deaths", "defeats"));
        pcm.getCommandCompletions().registerCompletion("games", handler -> gameConfigurationDao.getGameConfigurations()
                .keySet());
    }

    private void registerDependencies() {
        pcm.registerDependency(GameManager.class, gameManager);
        pcm.registerDependency(GameConfigurationDao.class, gameConfigurationDao);
        pcm.registerDependency(ConfigManager.class, configManager);
        pcm.registerDependency(DatabaseManager.class, databaseManager);
    }

    private void loadGroupsPlugin() {
        if (Bukkit.getPluginManager().getPlugin("SimpleClans") != null) {
            setGroupManager(new SimpleClansGroupManager(this));
        }
    }

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new PlayerCommandPreprocessListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(), this);
        Bukkit.getPluginManager().registerEvents(new EntityDamageListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerRespawnListener(), this);
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    @Override
    public void onDisable() {
        gameManager.getCurrentGame().ifPresent(g -> g.cancel(Bukkit.getConsoleSender()));
        databaseManager.close();
    }

    public static TitansBattle getInstance() {
        return instance;
    }

    public @Nullable GroupManager getGroupManager() {
        return groupManager;
    }

    /**
     * Sets the GroupManager
     *
     * @param groupManager the GroupManager
     */
    public void setGroupManager(@NotNull GroupManager groupManager) {
        this.groupManager = groupManager;
        getLogger().info(String.format("Registered %s", groupManager.getClass().getSimpleName()));
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    /**
     * Returns the language for the path on the config
     *
     * @param path where the String is
     * @param config a FileConfiguration to access
     * @return the language from the config, with its color codes (&) translated
     */
    public @NotNull String getLang(@NotNull String path, @Nullable FileConfiguration config) {
        String language = null;
        if (config != null) {
            language = config.getString("language." + path);
        }
        if (language == null) {
            language = getLanguageManager().getConfig().getString(path,
                    getLanguageManager().getEnglishLanguageFile().getString(path, "<MISSING KEY: " + path + ">"));
        }
        return ChatColor.translateAlternateColorCodes('&', language);
    }

    /**
     * Returns the language for the path
     *
     * @param path where the String is
     * @return the language from the default language file
     */
    public String getLang(@NotNull String path) {
        return getLang(path, (FileConfiguration) null);
    }

    /**
     * Returns the language for the path on the Game config file
     *
     * @param path where the String is
     * @param game the Game to find the String
     * @return the overrider language if found, or from the default language
     * file
     */
    @NotNull
    public String getLang(@NotNull String path, Game game) {
        YamlConfiguration configFile = null;
        if (game != null) {
            configFile = gameConfigurationDao.getConfigFile(game.getConfig());
        }
        return getLang(path, configFile);
    }

    public String getLang(@NotNull String path, BaseGame game) {
        // TODO Implement
        throw new UnsupportedOperationException();
    }

    /**
     * Sends a message to the console
     *
     * @param message message to send
     * @param respectUserDecision should the message be sent if debug is false?
     */
    public void debug(String message, boolean respectUserDecision) {
        if (respectUserDecision && !configManager.isDebug()) {
            return;
        }
        getLogger().info(message);
    }

    public void debug(String message) {
        debug(message, true);
    }

}
