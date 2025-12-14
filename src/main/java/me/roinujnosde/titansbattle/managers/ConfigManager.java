/*
 * The MIT License
 *
 * Copyright 2017 Edson Passos - edsonpassosjr@outlook.com.
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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.roinujnosde.titansbattle.managers;

import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.Event;
import me.roinujnosde.titansbattle.types.Event.Frequency;
import me.roinujnosde.titansbattle.utils.Helper;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import static java.lang.String.format;

/**
 *
 * @author RoinujNosde
 */
public final class ConfigManager {

    private final TitansBattle plugin = TitansBattle.getInstance();
    private FileConfiguration config;

    private final List<Event> events = new ArrayList<>();
    private List<UUID> respawn = new ArrayList<>();
    private List<UUID> clearInventory = new ArrayList<>();

    public void save() {
        config.set("data.respawn", Helper.uuidListToStringList(respawn));
        config.set("data.clear_inv", Helper.uuidListToStringList(clearInventory));
        plugin.saveConfig();
    }

    public void load() {
        plugin.reloadConfig();
        config = plugin.getConfig();

        if (isScheduler()) {
            events.clear();
            ConfigurationSection schedulersSection = config.getConfigurationSection("scheduler.events");
            if (schedulersSection != null) {
                Set<String> ids = schedulersSection.getKeys(false);
                String pathPrefix = "scheduler.events.";
                for (String id : ids) {
                    try {
                        String game = config.getString(pathPrefix + id + ".game");
                        Frequency frequency = Frequency.valueOf(config.getString(pathPrefix + id + ".frequency"));
                        int day = config.getInt(pathPrefix + id + ".day");
                        int hour = config.getInt(pathPrefix + id + ".hour");
                        int minute = config.getInt(pathPrefix + id + ".minute");
                        events.add(new Event(game, frequency, day, hour, minute));
                    } catch (IllegalArgumentException ex) {
                        plugin.getLogger().log(Level.SEVERE, format("Invalid event configuration for ID %s: %s", id, ex.getMessage()));
                    }
                }
            }
        }
        clearInventory = Helper.stringListToUuidList(config.getStringList("data.clear_inv"));
        respawn = Helper.stringListToUuidList(config.getStringList("data.respawn"));
    }

    public void setGeneralExit(Location generalExit) {
        config.set("destinations.general_exit", generalExit);
    }

    public boolean isDebug() {
        return config.getBoolean("debug");
    }

    /**
     * Gets the language code
     *
     * @return the language code
     */
    public String getLanguage() {
        String language = config.getString("language");
        if (language == null || language.equalsIgnoreCase("")) {
            language = "en";
        }
        return language;
    }

    public Location getGeneralExit() {
        return (Location) config.get("destinations.general_exit");
    }

    public List<String> getBlockedCommandsEveryone() {
        List<String> blockedCommandsEveryone = config.getStringList("blocked_commands_everyone");
        if (blockedCommandsEveryone == null) {
            blockedCommandsEveryone = new ArrayList<>();
        }
        return blockedCommandsEveryone;
    }

    /**
     * Gets the commands allowed in battle
     *
     * @return the allowed commands
     */
    public List<String> getAllowedCommands() {
        List<String> allowedCommands = config.getStringList("allowed_commands");
        if (allowedCommands == null) {
            allowedCommands = new ArrayList<>();
        }
        return allowedCommands;
    }

    /**
     * Sets the commands allowed in battle
     *
     * @param allowedCommands the allowed commands
     */
    @SuppressWarnings("unused")
    public void setAllowedCommands(@NotNull List<String> allowedCommands) {
        config.set("allowed_commands", allowedCommands);
    }

    /**
     * Checks if the scheduler feature is enabled
     *
     * @return if scheduler is enabled
     */
    public boolean isScheduler() {
        return config.getBoolean("scheduler.enabled", false);
    }

    /**
     * Gets the schedulers
     *
     * @return the schedulers
     */
    public List<Event> getEvents() {
        return events;
    }

    public List<UUID> getClearInventory() {
        return clearInventory;
    }

    public List<UUID> getRespawn() {
        return respawn;
    }

    public String getSqlHostname() {
        return config.getString("sql.mysql.hostname");
    }

    public int getSqlPort() {
        return config.getInt("sql.mysql.port");
    }

    public String getSqlDatabase() {
        return config.getString("sql.database");
    }

    public String getSqlUsername() {
        return config.getString("sql.mysql.username");
    }

    public String getSqlPassword() {
        return config.getString("sql.mysql.password");
    }

    public boolean isSqlUseMysql() {
        return config.getBoolean("sql.use-mysql");
    }

    public int getPageLimitRanking() {
        return config.getInt("page-limit.ranking");
    }

    public String getDateFormat() {
        return config.getString("date-format");
    }

    public String getTimeFormat() {
        return config.getString("time-format", "{HH}:{mm}:{ss}");
    }
}
