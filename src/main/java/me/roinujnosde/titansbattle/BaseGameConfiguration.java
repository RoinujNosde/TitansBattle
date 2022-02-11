package me.roinujnosde.titansbattle;

import me.roinujnosde.titansbattle.types.Kit;
import me.roinujnosde.titansbattle.utils.ConfigUtils;
import me.roinujnosde.titansbattle.utils.Path;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Map;

public abstract class BaseGameConfiguration implements ConfigurationSerializable {

    public enum Destination {
        EXIT, ARENA, LOBBY, WATCHROOM, BORDER_CENTER
    }

    protected transient File file;
    protected transient FileConfiguration fileConfiguration;

    protected String name;
    protected Boolean groupMode = false;
    protected Boolean clearItemsOnDeath = false;
    protected Boolean keepExp = false;
    protected Boolean useKits = false;
    protected Kit kit;

    protected Boolean pvp = true;
    @Path("damage-type.melee")
    protected Boolean meleeDamage = true;
    @Path("damage-type.ranged")
    protected Boolean rangedDamage = true;
    @Path("minimum.players")
    protected Integer minimumPlayers = 2;
    @Path("maximum.players")
    protected Integer maximumPlayers = 100;

    @Path("announcement.starting.times")
    protected Integer announcementStartingTimes = 5;
    @Path("announcement.starting.interval")
    protected Integer announcementStartingInterval = 20;

    @Path("destination.watchroom")
    protected Location watchroom;
    @Path("destination.exit")
    protected Location exit;
    @Path("destination.lobby")
    protected Location lobby;
    @Path("destination.arena")
    protected Location arena; // TODO Make all games support multiple entrances
    @Path("destination.border_center")
    protected Location borderCenter;

    @Path("time.preparation")
    protected Integer preparationTime = 30;
    @Path("time.expiration")
    protected Integer expirationTime = 3600;

    @Path("run_commands.before_battle")
    protected @Nullable List<String> commandsBeforeBattle;
    @Path("run_commands.after_battle")
    protected @Nullable List<String> commandsAfterBattle;

    @Path("worldborder.enable")
    protected Boolean worldBorder = false;
    @Path("worldborder.initial_size")
    protected Integer borderInitialSize = 5000;
    @Path("worldborder.final_size")
    protected Integer borderFinalSize = 500;
    @Path("worldborder.shrink")
    protected Integer borderShrink = 25;
    @Path("worldborder.interval")
    protected Integer borderInterval = 120;
    @Path("worldborder.damage")
    protected Double borderDamage = 5.0;

    public @NotNull FileConfiguration getFileConfiguration() {
        if (fileConfiguration == null) {
            throw new IllegalStateException();
        }
        return fileConfiguration;
    }

    public void setFileConfiguration(@NotNull FileConfiguration fileConfiguration) {
        this.fileConfiguration = fileConfiguration;
    }

    public @NotNull File getFile() {
        if (file == null) {
            throw new IllegalStateException();
        }
        return file;
    }

    public void setFile(@NotNull File file) {
        this.file = file;
    }

    @Override
    public Map<String, Object> serialize() {
        return ConfigUtils.serialize(this);
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean locationsSet() {
        return arena != null && exit != null && lobby != null && watchroom != null
                && (!worldBorder || borderCenter != null);
    }

    public abstract Integer getMinimumGroups();

    public abstract Integer getMaximumPlayersPerGroup();

    public abstract Integer getMaximumGroups();

    public Boolean isGroupMode() {
        return groupMode;
    }

    public Boolean isClearItemsOnDeath() {
        return clearItemsOnDeath;
    }

    public Boolean isKeepExp() {
        return keepExp;
    }

    public Boolean isUseKits() {
        return useKits;
    }

    public @Nullable Kit getKit() {
        return kit;
    }

    public void setKit(Kit kit) {
        this.kit = kit;
    }

    public Boolean isPvP() {
        return pvp;
    }

    public Boolean isMeleeDamage() {
        return meleeDamage;
    }

    public Boolean isRangedDamage() {
        return rangedDamage;
    }

    public Integer getMinimumPlayers() {
        return minimumPlayers;
    }

    public Integer getMaximumPlayers() {
        return maximumPlayers;
    }

    public Integer getAnnouncementStartingInterval() {
        return announcementStartingInterval;
    }

    public Integer getAnnouncementStartingTimes() {
        return announcementStartingTimes;
    }

    public Location getWatchroom() {
        return watchroom;
    }

    public Location getExit() {
        return exit;
    }

    public Location getLobby() {
        return lobby;
    }

    public Location getArena() {
        return arena;
    }

    public Location getBorderCenter() {
        return borderCenter;
    }

    public void setExit(Location exit) {
        this.exit = exit;
    }

    public void setLobby(Location lobby) {
        this.lobby = lobby;
    }

    public void setArena(Location arena) {
        this.arena = arena;
    }

    public void setWatchroom(Location watchroom) {
        this.watchroom = watchroom;
    }

    public void setBorderCenter(Location borderCenter) {
        this.borderCenter = borderCenter;
    }

    public Integer getPreparationTime() {
        return preparationTime;
    }

    public Integer getExpirationTime() {
        return expirationTime;
    }

    public @Nullable List<String> getCommandsBeforeBattle() {
        return commandsBeforeBattle;
    }

    public @Nullable List<String> getCommandsAfterBattle() {
        return commandsAfterBattle;
    }

    public Boolean isWorldBorder() {
        return worldBorder;
    }

    public Integer getBorderInterval() {
        return borderInterval;
    }

    public Integer getBorderInitialSize() {
        return borderInitialSize;
    }

    public Integer getBorderFinalSize() {
        return borderFinalSize;
    }

    public Integer getBorderShrinkSize() {
        return borderShrink;
    }

    public Double getBorderDamage() {
        return borderDamage;
    }
}
