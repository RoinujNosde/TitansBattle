package me.roinujnosde.titansbattle.managers;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import me.roinujnosde.titansbattle.TitansBattle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class LanguageManager {

    private TitansBattle plugin;
    private ConfigManager cm;
    private FileConfiguration configFile;
    private File file;
    private String fileName;

    public void setup() {
        plugin = TitansBattle.getInstance();
        cm = plugin.getConfigManager(); 
        
        fileName = "language-" + cm.getLanguage() + ".yml";
        file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
        configFile = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfig() {
        return configFile;
    }

    public void save() {
        try {
            configFile.save(file);
        } catch (IOException e) {
            Bukkit.getServer().getLogger().log(Level.SEVERE, "{0}[TitansBattle] Could not save {1}!", new Object[]{ChatColor.RED, fileName});
        }
    }

    public void reload() {
        configFile = YamlConfiguration.loadConfiguration(file);
    }
}
