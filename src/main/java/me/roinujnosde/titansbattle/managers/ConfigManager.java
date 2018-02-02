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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.roinujnosde.titansbattle.Helper;
import me.roinujnosde.titansbattle.types.Prizes;
import me.roinujnosde.titansbattle.types.Scheduler;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.Game;
import me.roinujnosde.titansbattle.types.Game.Mode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author RoinujNosde
 */
public final class ConfigManager {

    private TitansBattle plugin;
    private Helper helper;
    private final String groupsFunPath = "games" + File.separator + "GROUPS_FUN.yml";
    private final String groupsRealPath = "games" + File.separator + "GROUPS_REAL.yml";
    private final String freeforallFunPath = "games" + File.separator + "FREEFORALL_FUN.yml";
    private final String freeforallRealPath = "games" + File.separator + "FREEFORALL_REAL.yml";

    private File groupsFunFile;
    private File groupsRealFile;
    private File freeforallFunFile;
    private File freeforallRealFile;
    private FileConfiguration groupsFun;
    private FileConfiguration groupsReal;
    private FileConfiguration freeforallFun;
    private FileConfiguration freeforallReal;
    private HashMap<FileConfiguration, Game> filesAndGames = new HashMap<>();

    private String language;
    private boolean debug;
    private List<String> allowedCommands = new ArrayList<>();
    private boolean scheduler;
    private List<Scheduler> schedulers = new ArrayList<>();
    private Location generalExit;
    private List<UUID> respawn = new ArrayList<>();
    private List<UUID> clearInventory = new ArrayList<>();
    private boolean askForGameMode;
    private Mode defaultGameMode;
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

    void setupGameFiles() {
        groupsFunFile = new File(plugin.getDataFolder(), groupsFunPath);
        if (!groupsFunFile.exists()) {
            plugin.saveResource(groupsFunPath, false);
        }
        groupsFun = YamlConfiguration.loadConfiguration(groupsFunFile);

        groupsRealFile = new File(plugin.getDataFolder(), groupsRealPath);
        if (!groupsRealFile.exists()) {
            plugin.saveResource(groupsRealPath, false);
        }
        groupsReal = YamlConfiguration.loadConfiguration(groupsRealFile);

        freeforallFunFile = new File(plugin.getDataFolder(), freeforallFunPath);
        if (!freeforallFunFile.exists()) {
            plugin.saveResource(freeforallFunPath, false);
        }
        freeforallFun = YamlConfiguration.loadConfiguration(freeforallFunFile);

        freeforallRealFile = new File(plugin.getDataFolder(), freeforallRealPath);
        if (!freeforallRealFile.exists()) {
            plugin.saveResource(freeforallRealPath, false);
        }
        freeforallReal = YamlConfiguration.loadConfiguration(freeforallRealFile);
        if (filesAndGames == null) {
            filesAndGames = new HashMap<>();
        }
        filesAndGames.clear();
        filesAndGames.put(groupsFun, null);
        filesAndGames.put(groupsReal, null);
        filesAndGames.put(freeforallFun, null);
        filesAndGames.put(freeforallReal, null);
        for (FileConfiguration file : filesAndGames.keySet()) {
            long itemsGiveInterval = file.getLong("prizes.items_give_interval");
            boolean treatLeadersAsMembers = file.getBoolean("prizes.treat_leaders_as_members");
            boolean leaderItemsEnabled = file.getBoolean("prizes.leaders.items.enabled");
            List<ItemStack> leaderItems = (List<ItemStack>) file.getList("prizes.leaders.items.item_list");
            boolean leaderCommandsEnabled = file.getBoolean("prizes.leaders.commands.enabled");
            List<String> leaderCommands = file.getStringList("prizes.leaders.commands.command_list");
            double leaderCommandsSomeNumber = file.getDouble("prizes.leaders.commands.some_number.value");
            boolean leaderCommandsSomeNumberDivide = file.getBoolean("prizes.leaders.commands.some_number.divide");
            boolean leaderMoneyEnabled = file.getBoolean("prizes.leaders.money.enabled");
            boolean leaderMoneyDivide = file.getBoolean("prizes.leaders.money.divide");
            double leaderMoneyAmount = file.getDouble("prizes.leaders.money.amount");
            boolean memberItemsEnabled = file.getBoolean("prizes.members.items.enabled");
            List<ItemStack> memberItems = (List<ItemStack>) file.getList("prizes.members.items.item_list");
            boolean memberCommandsEnabled = file.getBoolean("prizes.members.commands.enabled");
            List<String> memberCommands = file.getStringList("prizes.members.commands.command_list");
            double memberCommandsSomeNumber = file.getDouble("prizes.members.commands.some_number.value");
            boolean memberCommandsSomeNumberDivide = file.getBoolean("prizes.members.commands.some_number.divide");
            boolean memberMoneyEnabled = file.getBoolean("prizes.members.money.enabled");
            boolean memberMoneyDivide = file.getBoolean("prizes.members.money.divide");
            double memberMoneyAmount = file.getDouble("prizes.members.money.amount");
            Prizes prizes = new Prizes(itemsGiveInterval,
                    treatLeadersAsMembers,
                    leaderItemsEnabled,
                    leaderItems,
                    leaderCommandsEnabled,
                    leaderCommands,
                    leaderCommandsSomeNumber,
                    leaderCommandsSomeNumberDivide,
                    leaderMoneyEnabled,
                    leaderMoneyDivide,
                    leaderMoneyAmount,
                    memberItemsEnabled,
                    memberItems,
                    memberCommandsEnabled,
                    memberCommands,
                    memberCommandsSomeNumber,
                    memberCommandsSomeNumberDivide,
                    memberMoneyEnabled,
                    memberMoneyDivide,
                    memberMoneyAmount);
            Mode mode = null;
            if (file.equals(groupsFun)) {
                mode = Mode.GROUPS_FUN;
            }
            if (file.equals(groupsReal)) {
                mode = Mode.GROUPS_REAL;
            }
            if (file.equals(freeforallFun)) {
                mode = Mode.FREEFORALL_FUN;
            }
            if (file.equals(freeforallReal)) {
                mode = Mode.FREEFORALL_REAL;
            }
            int minimumPlayer = file.getInt("minimum.players");
            int minimumGroups = file.getInt("minimum.groups");
            Location exit = (Location) file.get("destinations.exit");
            Location arena = (Location) file.get("destinations.arena");
            Location watchroom = (Location) file.get("destinations.watchroom");
            Location lobby = (Location) file.get("destinations.lobby");
            int announcementStartingTimes = file.getInt("announcements.starting.times");
            long announcementStartingInterval = file.getLong("announcements.starting.interval");
            long announcementGameInfoInterval = file.getLong("announcements.game_info.interval");
            boolean deleteGroups = file.getBoolean("delete_groups");
            long expirationTime = file.getLong("expiration_time");
            String joinOrQuitMessagePriority = file.getString("join_or_quit_message_priority");
            boolean killerJoinMessage = file.getBoolean("killer-join-message");
            boolean winnerJoinMessage = file.getBoolean("winner-join-message");
            boolean killerQuitMessage = file.getBoolean("killer-quit-message");
            boolean winnerQuitMessage = file.getBoolean("winner-quit-message");
            String killerPrefix = file.getString("hooks.legendchat.killer.prefix");
            String winnerPrefix = file.getString("hooks.legendchat.winner.prefix");
            List<UUID> winners = helper.stringListToUuidList(file.getStringList("data.winners"));
            UUID killer = UUID.fromString(file.getString("data.killer", UUID.randomUUID().toString()));
            String winnerGroup = file.getString("data.winner_group");
            long preparationTime = file.getLong("preparation_time");
            List<ItemStack> kit = (List<ItemStack>) file.getList("kit");
            Game game = new Game(mode,
                    prizes,
                    minimumPlayer,
                    minimumGroups,
                    exit,
                    arena,
                    watchroom,
                    lobby,
                    announcementStartingTimes,
                    announcementStartingInterval,
                    announcementGameInfoInterval,
                    deleteGroups,
                    expirationTime,
                    joinOrQuitMessagePriority,
                    killerJoinMessage,
                    winnerJoinMessage,
                    killerQuitMessage,
                    winnerQuitMessage,
                    killerPrefix,
                    winnerPrefix, winners, killer, winnerGroup, preparationTime, kit);
            filesAndGames.replace(file, game);
        }
    }

    void saveGameFiles() {
        for (FileConfiguration file : filesAndGames.keySet()) {
            Game g = filesAndGames.get(file);
            Prizes p = g.getPrizes();
            file.set("prizes.items_give_interval", p.getItemsGiveInterval());
            file.set("prizes.treat_leaders_as_members", p.isTreatLeadersAsMembers());
            file.set("prizes.leaders.items.enabled", p.isLeaderItemsEnabled());
            file.set("prizes.leaders.items.item_list", p.getLeaderItems());
            file.set("prizes.leaders.commands.enabled", p.isLeaderCommandsEnabled());
            file.set("prizes.leaders.commands.command_list", p.getLeaderCommands());
            file.set("prizes.leaders.commands.some_number.value", p.getLeaderCommandsSomeNumber());
            file.set("prizes.leaders.commands.some_number.divide", p.isLeaderCommandsSomeNumberDivide());
            file.set("prizes.leaders.money.enabled", p.isLeaderMoneyEnabled());
            file.set("prizes.leaders.money.divide", p.isLeaderMoneyDivide());
            file.set("prizes.leaders.money.amount", p.getLeaderMoneyAmount());
            file.set("prizes.members.items.enabled", p.isMemberItemsEnabled());
            file.set("prizes.members.items.item_list", p.getMemberItems());
            file.set("prizes.members.commands.enabled", p.isMemberCommandsEnabled());
            file.set("prizes.members.commands.command_list", p.getMemberCommands());
            file.set("prizes.members.commands.some_number.value", p.getMemberCommandsSomeNumber());
            file.set("prizes.members.commands.some_number.divide", p.isMemberCommandsSomeNumberDivide());
            file.set("prizes.members.money.enabled", p.isMemberMoneyEnabled());
            file.set("prizes.members.money.divide", p.isMemberMoneyDivide());
            file.set("prizes.members.money.amount", p.getMemberMoneyAmount());
            Mode mode = Mode.FREEFORALL_REAL;
            if (file.equals(groupsFun)) {
                mode = Mode.GROUPS_FUN;
            }
            if (file.equals(groupsReal)) {
                mode = Mode.GROUPS_REAL;
            }
            if (file.equals(freeforallFun)) {
                mode = Mode.FREEFORALL_FUN;
            }
            if (file.equals(freeforallReal)) {
                mode = Mode.FREEFORALL_REAL;
            }
            file.set("minimum.players", g.getMinimumPlayers());
            file.set("minimum.groups", g.getMinimumGroups());
            file.set("destinations.exit", g.getExit());
            file.set("destinations.arena", g.getArena());
            file.set("destinations.watchroom", g.getWatchroom());
            file.set("destinations.lobby", g.getLobby());
            file.set("announcements.starting.times", g.getAnnouncementStartingTimes());
            file.set("announcements.starting.interval", g.getAnnouncementStartingInterval());
            file.set("announcements.game_info.interval", g.getAnnouncementGameInfoInterval());
            file.set("delete_groups", g.isDeleteGroups());
            file.set("expiration_time", g.getExpirationTime());
            file.set("join_or_quit_message_priority", g.getJoinOrQuitMessagePriority());
            file.set("killer-join-message", g.isKillerJoinMessage());
            file.set("winner-join-message", g.isWinnerJoinMessage());
            file.set("killer-quit-message", g.isKillerQuitMessage());
            file.set("winner-quit-message", g.isWinnerQuitMessage());
            file.set("hooks.legendchat.killer.prefix", g.getKillerPrefix());
            file.set("hooks.legendchat.winner.prefix", g.getWinnerPrefix());
            file.set("data.winners", helper.uuidListToStringList(g.getWinners()));
            file.set("data.killer", g.getKiller().toString());
            file.set("data.winner_group", g.getWinnerGroup());
            file.set("preparation_time", g.getPreparationTime());
            file.set("kit", g.getKit());
        }
        try {
            freeforallReal.save(freeforallRealFile);
            freeforallFun.save(freeforallFunFile);
            groupsReal.save(groupsRealFile);
            groupsFun.save(groupsFunFile);
        } catch (IOException ex) {
            Logger.getLogger(ConfigManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void save() {
        saveGameFiles();
        plugin.getConfig().set("allowed_commands", allowedCommands);
        plugin.getConfig().set("language", language);
        plugin.getConfig().set("debug", debug);
        plugin.getConfig().set("scheduler.enabled", scheduler);
        if (scheduler) {
            for (Scheduler a : schedulers) {
                String id = Integer.toString(a.getId());
                plugin.getConfig().set("scheduler.schedulers." + id + "game", a.getMode().toString());
                plugin.getConfig().set("scheduler.schedulers." + id + "day", a.getDay());
                plugin.getConfig().set("scheduler.schedulers." + id + "hour", a.getHour());
                plugin.getConfig().set("scheduler.schedulers." + id + "minute", a.getMinute());
            }
        }
        plugin.getConfig().set("destinations.general_exit", getGeneralExit());
        plugin.getConfig().set("data.respawn", helper.uuidListToStringList(respawn));
        plugin.getConfig().set("data.clear_inv", helper.uuidListToStringList(clearInventory));
        plugin.getConfig().set("ask-for-game-mode", askForGameMode);
        plugin.getConfig().set("default-game-mode", defaultGameMode.toString());
        plugin.getConfig().set("commands.join", commandJoin);
        plugin.getConfig().set("commands.exit", commandExit);;
        plugin.getConfig().set("commands.start", commandStart);
        plugin.getConfig().set("commands.cancel", commandCancel);
        plugin.getConfig().set("commands.setdestination", commandSetDestination);
        plugin.getConfig().set("commands.setinventory", commandSetInventory);
        plugin.getConfig().set("commands.help", commandHelp);
        plugin.getConfig().set("commands.reload", commandReload);
        plugin.getConfig().set("commands.watch", commandWatch);
        plugin.getConfig().set("commands.winners", commandWinners);

        plugin.saveConfig();
    }

    public void load() {
        plugin = TitansBattle.getInstance();
        helper = TitansBattle.getHelper();
        plugin.reloadConfig();
        setupGameFiles();

        allowedCommands = plugin.getConfig().getStringList("allowed_commands");
        language = plugin.getConfig().getString("language");
        debug = plugin.getConfig().getBoolean("debug");
        scheduler = plugin.getConfig().getBoolean("scheduler.enabled", false);
        if (scheduler) {
            schedulers.clear();
            List<Integer> ids = (List<Integer>) plugin.getConfig().getList("scheduler.schedulers", new ArrayList<>());
            for (Integer id : ids) {
                String idString = Integer.toString(id);
                String game = plugin.getConfig().getString("scheduler.schedulers." + idString + "game");
                int day = plugin.getConfig().getInt("scheduler.schedulers." + idString + "day");
                int hour = plugin.getConfig().getInt("scheduler.schedulers." + idString + "hour");
                int minute = plugin.getConfig().getInt("scheduler.schedulers." + idString + "minute");
                Scheduler s = new Scheduler(id, Game.Mode.valueOf(game.toUpperCase()), day, hour, minute);
                schedulers.add(s);
            }
        }
        generalExit = (Location) plugin.getConfig().get("destinations.general_exit");
        clearInventory.clear();
        respawn.clear();
        clearInventory = helper.stringListToUuidList(plugin.getConfig().getStringList("data.clear_inv"));
        respawn = helper.stringListToUuidList(plugin.getConfig().getStringList("data.respawn"));
        askForGameMode = plugin.getConfig().getBoolean("ask-for-game-mode");
        defaultGameMode = Mode.valueOf(plugin.getConfig().getString("default-game-mode").toUpperCase());
        commandJoin = plugin.getConfig().getString("commands.join");
        commandExit = plugin.getConfig().getString("commands.exit");;
        commandStart = plugin.getConfig().getString("commands.start");
        commandCancel = plugin.getConfig().getString("commands.cancel");
        commandSetDestination = plugin.getConfig().getString("commands.setdestination");
        commandSetInventory = plugin.getConfig().getString("commands.setinventory");
        commandHelp = plugin.getConfig().getString("commands.help");
        commandReload = plugin.getConfig().getString("commands.reload");
        commandWatch = plugin.getConfig().getString("commands.watch");
        commandWinners = plugin.getConfig().getString("commands.winners");
    }

    public Mode getDefaultGameMode() {
        return defaultGameMode;
    }

    public boolean isAskForGameMode() {
        return askForGameMode;
    }

    public void setDefaultGameMode(Mode defaultGameMode) {
        this.defaultGameMode = defaultGameMode;
    }

    public void setAskForGameMode(boolean askForGameMode) {
        this.askForGameMode = askForGameMode;
    }

    public HashMap<FileConfiguration, Game> getFilesAndGames() {
        return filesAndGames;
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
    public void setAllowedCommands(List<String> allowedCommands) {
        if (allowedCommands == null) {
            throw new IllegalArgumentException();
        }
        this.allowedCommands = allowedCommands;
    }

    /**
     * Gets the schedulers
     *
     * @return the schedulers
     */
    public List<Scheduler> getSchedulers() {
        return schedulers;
    }

    /**
     * Checks if the scheduler feature is enabled
     *
     * @return if scheduler is enabled
     */
    public boolean isScheduler() {
        return scheduler;
    }

    /**
     * Sets the schedulers
     *
     * @param schedulers the schedulers to set
     */
    public void setSchedulers(List<Scheduler> schedulers) {
        if (schedulers == null) {
            throw new IllegalArgumentException();
        }
        this.schedulers = schedulers;
    }

    /**
     * Enables or not the scheduler feature
     *
     * @param enabled
     */
    public void setScheduler(boolean enabled) {
        this.scheduler = enabled;
    }

    public List<UUID> getClearInventory() {
        return clearInventory;
    }

    public List<UUID> getRespawn() {
        return respawn;
    }
}
