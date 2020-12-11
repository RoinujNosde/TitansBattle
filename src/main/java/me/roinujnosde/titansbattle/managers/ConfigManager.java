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

import me.roinujnosde.titansbattle.utils.Helper;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.Scheduler;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author RoinujNosde
 */
public final class ConfigManager {

    private TitansBattle plugin;
    private FileConfiguration config;

    private String language;
    private boolean debug;
    private List<String> allowedCommands = new ArrayList<>();
    private boolean scheduler;
    private Location generalExit;
    private List<UUID> respawn = new ArrayList<>();
    private List<UUID> clearInventory = new ArrayList<>();
    private String commandJoin;
    private String commandExit;
    private String commandStart;
    private String commandCancel;
    private String commandSetDestination;
    private String commandSetInventory;
    private String commandHelp;
    private String commandReload;
    private String commandWatch;
    private String commandWinners;
    private String commandRanking;
    private boolean sqlUseMysql;
    private String sqlHostname;
    private int sqlPort;
    private String sqlDatabase;
    private String sqlUsername;
    private String sqlPassword;
    private int pageLimitHelp;
    private int pageLimitRanking;
    private String dateFormat;

    public void save() {
        config.set("allowed_commands", allowedCommands);
        config.set("language", language);
        config.set("debug", debug);
        config.set("scheduler.enabled", scheduler);
        if (scheduler) {
            for (Scheduler a : Scheduler.getSchedulers()) {
                String id = a.getId();
                config.set("scheduler.schedulers." + id + ".game", a.getGameName());
                config.set("scheduler.schedulers." + id + ".day", a.getDay());
                config.set("scheduler.schedulers." + id + ".hour", a.getHour());
                config.set("scheduler.schedulers." + id + ".minute", a.getMinute());
            }
        }
        config.set("destinations.general_exit", getGeneralExit());
        config.set("data.respawn", Helper.uuidListToStringList(respawn));
        config.set("data.clear_inv", Helper.uuidListToStringList(clearInventory));
        config.set("commands.join", commandJoin);
        config.set("commands.exit", commandExit);
        config.set("commands.start", commandStart);
        config.set("commands.cancel", commandCancel);
        config.set("commands.setdestination", commandSetDestination);
        config.set("commands.setinventory", commandSetInventory);
        config.set("commands.help", commandHelp);
        config.set("commands.reload", commandReload);
        config.set("commands.watch", commandWatch);
        config.set("commands.winners", commandWinners);
        config.set("commands.ranking", commandRanking);
        config.set("sql.use-mysql", sqlUseMysql);
        config.set("sql.mysql.hostname", sqlHostname);
        config.set("sql.mysql.port", sqlPort);
        config.set("sql.database", sqlDatabase);
        config.set("sql.mysql.username", sqlUsername);
        config.set("sql.mysql.password", sqlPassword);
        config.set("page-limit.help", pageLimitHelp);
        config.set("page-limit.ranking", pageLimitRanking);
        config.set("date-format", dateFormat);

        plugin.saveConfig();
    }

    public void load() {
        plugin = TitansBattle.getInstance();
        plugin.reloadConfig();
        config = plugin.getConfig();

        allowedCommands = config.getStringList("allowed_commands");
        language = config.getString("language");
        debug = config.getBoolean("debug");
        scheduler = config.getBoolean("scheduler.enabled", false);
        if (scheduler) {
            List<Scheduler> schedulers = Scheduler.getSchedulers();
            schedulers.clear();
            ConfigurationSection schedulersSection = config.getConfigurationSection("scheduler.schedulers");
            if (schedulersSection == null) {
                plugin.debug("Couldn't find the schedulers section in the config file!", false);
            } else {
                Set<String> ids = schedulersSection.getKeys(false);
                plugin.debug(String.format("Scheduler IDs: %s", ids.size()), true);
                for (String id : ids) {
                    String game = config.getString("scheduler.schedulers." + id + ".game");
                    int day = config.getInt("scheduler.schedulers." + id + ".day");
                    int hour = config.getInt("scheduler.schedulers." + id + ".hour");
                    int minute = config.getInt("scheduler.schedulers." + id + ".minute");
                    Scheduler s = new Scheduler(id, game, day, hour, minute);
                    schedulers.add(s);
                }
            }
        }
        generalExit = (Location) config.get("destinations.general_exit");
        clearInventory.clear();
        respawn.clear();
        clearInventory = Helper.stringListToUuidList(config.getStringList("data.clear_inv"));
        respawn = Helper.stringListToUuidList(config.getStringList("data.respawn"));
        commandJoin = config.getString("commands.join");
        commandExit = config.getString("commands.exit");
        commandStart = config.getString("commands.start");
        commandCancel = config.getString("commands.cancel");
        commandSetDestination = config.getString("commands.setdestination");
        commandSetInventory = config.getString("commands.setinventory");
        commandHelp = config.getString("commands.help");
        commandReload = config.getString("commands.reload");
        commandWatch = config.getString("commands.watch");
        commandWinners = config.getString("commands.winners");
        commandRanking = config.getString("commands.ranking");
        sqlUseMysql = config.getBoolean("sql.use-mysql");
        sqlHostname = config.getString("sql.mysql.hostname");
        sqlPort = config.getInt("sql.mysql.port");
        sqlDatabase = config.getString("sql.database");
        sqlUsername = config.getString("sql.mysql.username");
        sqlPassword = config.getString("sql.mysql.password");
        pageLimitHelp = config.getInt("page-limit.help");
        pageLimitRanking = config.getInt("page-limit.ranking");
        dateFormat = config.getString("date-format");
    }

    public void setGeneralExit(Location generalExit) {
        this.generalExit = generalExit;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getCommandCancel() {
        return commandCancel;
    }

    public String getCommandHelp() {
        return commandHelp;
    }

    public String getCommandJoin() {
        return commandJoin;
    }

    public String getCommandSetInventory() {
        return commandSetInventory;
    }

    public String getCommandWatch() {
        return commandWatch;
    }

    public String getCommandWinners() {
        return commandWinners;
    }

    public String getCommandStart() {
        return commandStart;
    }

    public String getCommandSetDestination() {
        return commandSetDestination;
    }

    public String getCommandReload() {
        return commandReload;
    }

    public String getCommandExit() {
        return commandExit;
    }

    /**
     * Gets the language code
     *
     * @return the language code
     */
    public String getLanguage() {
        if (language == null || language.equalsIgnoreCase("")) {
            language = "en";
        }
        return language;
    }

    public Location getGeneralExit() {
        return generalExit;
    }

    /**
     * Sets the language code
     *
     * @param language the language code
     */
    public void setLanguage(String language) {
        if (language == null || language.equalsIgnoreCase("")) {
            throw new IllegalArgumentException();
        }
        this.language = language;
    }

    /**
     * Gets the commands allowed in battle
     *
     * @return the allowed commands
     */
    public List<String> getAllowedCommands() {
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
    public void setAllowedCommands(List<String> allowedCommands) {
        if (allowedCommands == null) {
            throw new IllegalArgumentException();
        }
        this.allowedCommands = allowedCommands;
    }

    /**
     * Checks if the scheduler feature is enabled
     *
     * @return if scheduler is enabled
     */
    public boolean isScheduler() {
        return scheduler;
    }

    public void setScheduler(boolean enabled) {
        this.scheduler = enabled;
    }

    public List<UUID> getClearInventory() {
        return clearInventory;
    }

    public List<UUID> getRespawn() {
        return respawn;
    }

    public String getSqlHostname() {
        return sqlHostname;
    }

    public int getSqlPort() {
        return sqlPort;
    }

    public String getSqlDatabase() {
        return sqlDatabase;
    }

    public String getSqlUsername() {
        return sqlUsername;
    }

    public String getSqlPassword() {
        return sqlPassword;
    }

    public String getCommandRanking() {
        return commandRanking;
    }

    public boolean isSqlUseMysql() {
        return sqlUseMysql;
    }

    public int getPageLimitHelp() {
        return pageLimitHelp;
    }

    public int getPageLimitRanking() {
        return pageLimitRanking;
    }

    public String getDateFormat() {
        return dateFormat;
    }
}
