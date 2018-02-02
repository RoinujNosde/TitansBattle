/** *****************************************************************************
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
import me.roinujnosde.titansbattle.managers.GameManager;
import me.roinujnosde.titansbattle.managers.LanguageManager;
import me.roinujnosde.titansbattle.managers.TaskManager;
import me.roinujnosde.titansbattle.types.Game;

import net.milkbowl.vault.economy.Economy;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

//TODO: Throw exceptions on methods
//TODO: Javadoc
//TODO: Add debug messages
//todo: Insert comments in the code

/**
 * @author RoinujNosde
 *
 */
public final class TitansBattle extends JavaPlugin {

    private static TitansBattle instance;
    private static GameManager gameManager;
    private static ConfigManager configManager;
    private static TaskManager taskManager;
    private static LanguageManager languageManager;
    private static Helper helper;
    private static Economy economy;
    private static SimpleClans simpleClans;
    private static Factions factions;

    @Override
    public void onEnable() {
        instance = this;
        gameManager = new GameManager();
        configManager = new ConfigManager();
        taskManager = new TaskManager();
        languageManager = new LanguageManager();
        helper = new Helper();

        configManager.load();
        gameManager.load();
        taskManager.load();
        languageManager.setup();
        helper.load();

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
        gameManager.startOrSchedule();
    }

    /**
     *
     * @return if the server is using SimpleClans
     */
    public static boolean isSimpleClans() {
        if (simpleClans != null) {
            return true;
        }
        return false;
    }

    /**
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
     *
     * @return the Economy system
     */
    public static Economy getEconomy() {
        return economy;
    }

    /**
     *
     * @return if the server is using Factions
     */
    public static boolean isFactions() {
        if (factions != null) {
            return true;
        }
        return false;
    }
    
    void setupEconomy() {
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
        gameManager.finishGame(gameManager.getCurrentGame());
    }

    /**
     *
     * @return the plugin instance
     */
    public static TitansBattle getInstance() {
        return instance;
    }

    /**
     *
     * @return the GameManager instance
     */
    public static GameManager getGameManager() {
        return gameManager;
    }

    /**
     *
     * @return the TaskManager instance
     */
    public static TaskManager getTaskManager() {
        return taskManager;
    }

    /**
     *
     * @return the ConfigManager instance
     */
    public static ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     *
     * @return the LanguageManager instance
     */
    public static LanguageManager getLanguageManager() {
        return languageManager;
    }

    /**
     *
     * @return the Helper instance
     */
    public static Helper getHelper() {
        return helper;
    }

    /**
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
     *
     * @param path where the String is
     * @return the language from the default language file
     */
    public static String getLang(String path) {
        return getLang(path, getLanguageManager().getConfig());
    }

    /**
     *
     * @param path where the String is
     * @param game the Game to find the String
     * @return the overrider language if found, or from the default language file
     */
    public static String getLang(String path, Game game) {
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
    public static void debug(String message, boolean respectUserDecision) {
        if (respectUserDecision) {
            if (!configManager.isDebug()) {
                return;
            }
        }
        Bukkit.getConsoleSender().sendMessage(ChatColor.BLUE + "[TitansBattle] " + message);
    }
}
