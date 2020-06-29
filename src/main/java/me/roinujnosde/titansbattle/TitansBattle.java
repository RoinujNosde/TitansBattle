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

import me.roinujnosde.titansbattle.listeners.EntityDamageListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.massivecraft.factions.Factions;
import me.roinujnosde.titansbattle.listeners.ChatMessageListener;
import me.roinujnosde.titansbattle.listeners.PlayerCommandPreprocessListener;
import me.roinujnosde.titansbattle.listeners.PlayerDeathListener;
import me.roinujnosde.titansbattle.listeners.PlayerJoinListener;
import me.roinujnosde.titansbattle.listeners.PlayerQuitListener;

import me.roinujnosde.titansbattle.managers.ConfigManager;
import me.roinujnosde.titansbattle.managers.DatabaseManager;
import me.roinujnosde.titansbattle.managers.GameManager;
import me.roinujnosde.titansbattle.managers.LanguageManager;
import me.roinujnosde.titansbattle.managers.TaskManager;
import me.roinujnosde.titansbattle.types.Game;

import net.milkbowl.vault.economy.Economy;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

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
    private Helper helper;
    private Economy economy;
    private SimpleClans simpleClans;
    private Factions factions;

    @Override
    public void onEnable() {
        instance = this;
        gameManager = new GameManager();
        configManager = new ConfigManager();
        taskManager = new TaskManager();
        languageManager = new LanguageManager();
        databaseManager = new DatabaseManager();
        helper = new Helper();

        configManager.load();
        gameManager.load();
        taskManager.load();
        languageManager.setup();
        helper.load();
        databaseManager.load();

        debug("Plugin by RoinujNosde", false);
        debug("Special thanks to Pedro Silva for helping me test it", false);
        debug("Like this plugin? Leave a review on Spigot, please ;)", false);
        debug("If you need help, contact me on:", false);
        debug("Discord/Spigot: RoinujNosde - Email: edsonpassosjr@outlook.com", false);
        if ((simpleClans = (SimpleClans) Bukkit.getPluginManager().getPlugin("SimpleClans")) != null) {
            debug("SimpleClans found.", false);
        }
        if ((factions = (Factions) (Bukkit.getPluginManager().getPlugin("Factions"))) != null) {
            debug("Factions found.", false);
        }
        if (factions != null && simpleClans != null) {
            debug("Factions and SimpleClans found. Disable one!", false);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        saveDefaultConfig();
        setupEconomy();
        if (Bukkit.getPluginManager().getPlugin("Legendchat") != null) {
            debug("Legendchat found.", false);
            Bukkit.getPluginManager().registerEvents(new ChatMessageListener(), this);
        }
        Bukkit.getPluginManager().registerEvents(new PlayerCommandPreprocessListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(), this);
        Bukkit.getPluginManager().registerEvents(new EntityDamageListener(), this);
        getCommand("tb").setExecutor(new TbCommandExecutor());

        databaseManager.loadDataToMemory();
        gameManager.startOrSchedule();
    }

    /**
     * Checks if the server is using SimpleClans
     *
     * @return if the server is using SimpleClans
     */
    public boolean isSimpleClans() {
        return simpleClans != null;
    }

    /**
     * Returns the DatabaseManager instance
     *
     * @return the DatabaseManager
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /**
     * Returns the ClanManager if SimpleClans is installed, null otherwise
     *
     * @return the ClanManager if SimpleClans is installed, null otherwise
     */
    public ClanManager getClanManager() {
        if (isSimpleClans()) {
            return simpleClans.getClanManager();
        }
        return null;
    }

    /**
     * Returns the Economy system, null if none found
     *
     * @return the Economy system, or null
     */
    public Economy getEconomy() {
        return economy;
    }

    /**
     * Returns if the server is using Factions
     *
     * @return if the server is using Factions
     */
    public boolean isFactions() {
        return factions != null;
    }

    /**
     * Sets up the Economy system (used internally)
     */
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

    /**
     * Returns the plugin instance
     *
     * @return the plugin instance
     */
    public static TitansBattle getInstance() {
        return instance;
    }

    /**
     * Returns the GameManager instance
     *
     * @return the GameManager instance
     */
    public GameManager getGameManager() {
        return gameManager;
    }

    /**
     * Returns the TaskManager instance
     *
     * @return the TaskManager instance
     */
    public TaskManager getTaskManager() {
        return taskManager;
    }

    /**
     * Returns the ConfigManager instance
     *
     * @return the ConfigManager instance
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Returns the LanguageManager instance
     *
     * @return the LanguageManager instance
     */
    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    /**
     * Returns the Helper instance
     *
     * @return the Helper instance
     */
    public Helper getHelper() {
        return helper;
    }

    /**
     * Returns the language for the path on the config
     *
     * @param path where the String is
     * @param config a FileConfiguration to access
     * @return the language from the config, with its color codes (&) translated
     */
    public static String getLang(String path, FileConfiguration config) {
        String language = config.getString(path);
        if (language == null) {
            return null;
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
        return getLang(path, getLanguageManager().getConfig());
    }

    /**
     * Returns the language for the path on the Game config file
     *
     * @param path where the String is
     * @param game the Game to find the String
     * @return the overrider language if found, or from the default language
     * file
     */
    public String getLang(String path, Game game) {
        for (FileConfiguration file : configManager.getFilesAndGames().keySet()) {
            if (configManager.getFilesAndGames().get(file).equals(game)) {
                String language = getLang("language." + path, file);
                if (language != null) {
                    return language;
                }
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
