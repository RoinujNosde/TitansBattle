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
import me.roinujnosde.titansbattle.commands.TBCommands;
import me.roinujnosde.titansbattle.dao.GameConfigurationDao;
import me.roinujnosde.titansbattle.listeners.*;
import me.roinujnosde.titansbattle.managers.*;
import me.roinujnosde.titansbattle.types.*;
import me.roinujnosde.titansbattle.utils.ConfigUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
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
    private Economy economy;

    @Override
    public void onEnable() {
        ConfigurationSerialization.registerClass(GameConfiguration.class);
        ConfigurationSerialization.registerClass(Kit.class);
        ConfigurationSerialization.registerClass(Prizes.class);
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
        saveDefaultConfig();
        setupEconomy();

        pcm = new PaperCommandManager(this);
        pcm.enableUnstableAPI("help");
        configureCommands();
        databaseManager.loadDataToMemory();
        gameManager.startOrSchedule();
    }

    private void configureCommands() {
        setDefaultLocale();
        registerDependencies();
        registerCompletions();
        registerContexts();
        registerCommands();
        registerConditions();
        registerEvents();
    }

    private void setDefaultLocale() {
        String[] s = configManager.getLanguage().split("_");
        pcm.getLocales().setDefaultLocale(new Locale(s[0]));
    }

    private void registerContexts() {
        pcm.getCommandContexts().registerIssuerOnlyContext(Game.class, supplier -> gameManager.getCurrentGame());
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
            if (gameManager.getCurrentGame() == null) {
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
            groupManager = new SimpleClansGroupManager(this);
            debug("SimpleClans found.", false);
        } else if (Bukkit.getPluginManager().getPlugin("Factions") != null) {
            groupManager = new FactionsGroupManager(this);
            debug("Factions found.", false);
        }
    }

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new PlayerCommandPreprocessListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(), this);
        Bukkit.getPluginManager().registerEvents(new EntityDamageListener(), this);
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public @Nullable Economy getEconomy() {
        return economy;
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        economy = rsp.getProvider();
        debug("Vault and economy plugin found.", false);
    }

    @Override
    public void onDisable() {
        gameManager.finishGame(null, null, null);
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
    public @NotNull String getLang(String path, @Nullable FileConfiguration config) {
        if (config == null) {
            config = getLanguageManager().getConfig();
        }
        String language = config.getString(path);
        if (language == null) {
            return path;
        }
        return ChatColor.translateAlternateColorCodes('&', language);
    }

    /**
     * Returns the language for the path
     *
     * @param path where the String is
     * @return the language from the default language file
     */
    public String getLang(String path) {
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
    public String getLang(String path, @Nullable Game game) {
        if (game != null) {
            YamlConfiguration configFile = gameConfigurationDao.getConfigFile(game.getConfig());
            if (configFile != null) {
                getLang("language." + path, configFile);
            }
        }
        return getLang(path);
    }

    /**
     * Sends a message to the console
     *
     * @param message message to send
     * @param respectUserDecision should the message be sent if debug is false?
     */
    public void debug(String message, boolean respectUserDecision) {
        if (respectUserDecision) {
            if (!configManager.isDebug()) {
                return;
            }
        }
        Bukkit.getConsoleSender().sendMessage(ChatColor.BLUE + "[TitansBattle] " + message);
    }
}
