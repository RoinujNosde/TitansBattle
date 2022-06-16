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

import me.roinujnosde.titansbattle.challenges.Challenge;
import me.roinujnosde.titansbattle.challenges.ChallengeRequest;
import me.roinujnosde.titansbattle.dao.ConfigurationDao;
import me.roinujnosde.titansbattle.games.Game;
import me.roinujnosde.titansbattle.hooks.papi.PlaceholderHook;
import me.roinujnosde.titansbattle.managers.*;
import me.roinujnosde.titansbattle.types.GameConfiguration;
import me.roinujnosde.titansbattle.types.Kit;
import me.roinujnosde.titansbattle.types.Prizes;
import me.roinujnosde.titansbattle.types.Warrior;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

/**
 * @author RoinujNosde
 *
 */
public final class TitansBattle extends JavaPlugin {

    private static TitansBattle instance;
    private GameManager gameManager;
    private ConfigManager configManager;
    private TaskManager taskManager;
    private LanguageManager languageManager;
    private DatabaseManager databaseManager;
    private @Nullable GroupManager groupManager;
    private ChallengeManager challengeManager;
    private ListenerManager listenerManager;
    private ConfigurationDao configurationDao;
    private PlaceholderHook placeholderHook;

    @Override
    public void onEnable() {
        setupConfig();
        registerSerializationClasses();
        instance = this;
        gameManager = new GameManager();
        configManager = new ConfigManager();
        taskManager = new TaskManager();
        languageManager = new LanguageManager();
        databaseManager = new DatabaseManager();
        challengeManager = new ChallengeManager(this);
        listenerManager = new ListenerManager(this);
        configurationDao = new ConfigurationDao(getDataFolder());

        configManager.load();
        languageManager.setup();
        databaseManager.setup();

        loadGroupsPlugin();

        new CommandManager(this);
        listenerManager.registerGeneralListeners();
        databaseManager.loadDataToMemory();
        taskManager.setupScheduler();
        placeholderHook = new PlaceholderHook(this);
        new Metrics(this, 14875);
    }

    public @Nullable BaseGame getBaseGameFrom(@NotNull Player player) {
        Warrior warrior = getDatabaseManager().getWarrior(player);

        Optional<Game> currentGame = getGameManager().getCurrentGame();
        if (currentGame.isPresent()) {
            if (currentGame.get().isParticipant(warrior)) {
                return currentGame.get();
            }
        }
        List<ChallengeRequest<?>> requests = getChallengeManager().getRequests();
        for (ChallengeRequest<?> request : requests) {
            Challenge challenge = request.getChallenge();
            if (challenge.isParticipant(warrior)) {
                return challenge;
            }
        }
        return null;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    @Override
    public void onDisable() {
        challengeManager.getChallenges().forEach(c -> c.cancel(Bukkit.getConsoleSender()));
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

    public ChallengeManager getChallengeManager() {
        return challengeManager;
    }

    public ListenerManager getListenerManager() {
        return listenerManager;
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

    public ConfigurationDao getConfigurationDao() {
        return configurationDao;
    }

    public PlaceholderHook getPlaceholderHook() {
        return placeholderHook;
    }

    /**
     * Returns the language for the path on the config
     *
     * @param path   where the String is
     * @param config a FileConfiguration to access
     * @return the language from the config, with its color codes (&) translated
     */
    public @NotNull String getLang(@NotNull String path, @Nullable FileConfiguration config, Object... args) {
        String language = null;
        if (config != null) {
            language = config.getString("language." + path);
        }
        if (language == null) {
            language = getLanguageManager().getConfig().getString(path,
                    getLanguageManager().getEnglishLanguageFile().getString(path, "<MISSING KEY: " + path + ">"));
        }
        return ChatColor.translateAlternateColorCodes('&', MessageFormat.format(language, args));
    }

    public String getLang(@NotNull String path, Object... args) {
        return getLang(path, (FileConfiguration) null, args);
    }

    /**
     * Returns the language for the path on the BaseGame config file
     *
     * @param path where the String is
     * @param game the BaseGame to find the String
     * @return the overrider language if found, or from the default language file
     */
    @NotNull
    public String getLang(@NotNull String path, @Nullable BaseGame game, Object... args) {
        if (game == null) {
            return getLang(path, (FileConfiguration) null, args);
        }
        return getLang(path, game.getConfig().getFileConfiguration(), args);
    }

    public String getLang(@NotNull String path, @NotNull BaseGameConfiguration config, Object... args) {
        return getLang(path, config.getFileConfiguration(), args);
    }

    /**
     * Sends a message to the console
     *
     * @param message             message to send
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

    private void setupConfig() {
        saveDefaultConfig();
        // loads the config and copies default values
        getConfig().options().copyDefaults(true);
        // saves it back (to add new values)
        saveConfig();
    }

    private void registerSerializationClasses() {
        ConfigurationSerialization.registerClass(GameConfiguration.Prize.class);
        ConfigurationSerialization.registerClass(BaseGameConfiguration.class);
        ConfigurationSerialization.registerClass(Kit.class);
        ConfigurationSerialization.registerClass(Prizes.class);
    }

    private void loadGroupsPlugin() {
        if (Bukkit.getPluginManager().getPlugin("SimpleClans") != null) {
            setGroupManager(new SimpleClansGroupManager(this));
        }
    }

}
