package me.roinujnosde.titansbattle;

import me.roinujnosde.titansbattle.types.Kit;
import me.roinujnosde.titansbattle.utils.Path;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public abstract class BaseGameConfiguration {

    // TODO Check fields in common in subclasses

    protected File file; // TODO Transient?
    protected FileConfiguration fileConfiguration;  // TODO Does transient keeps it from being saved?
    protected String name;
    protected Boolean groupMode;
    protected Boolean clearItemsOnDeath = false;

    @Path("damage-type.melee")
    protected Boolean meleeDamage = true;
    @Path("damage-type.ranged")
    protected Boolean rangedDamage = true;
    @Path("minimum.players")
    protected Integer minimumPlayers = 2;
    @Path("maximum.players")
    protected Integer maximumPlayers = 100;

    @Path("time.preparation")
    protected Integer preparationTime = 30;
    @Path("time.expiration")
    protected Integer expirationTime = 3600;

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

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotNull
    public abstract Location getLobby();

    @NotNull
    public abstract Location getWatchroom();

    @NotNull
    public abstract Location getExit();

    @Nullable
    public abstract Kit getKit();

    public abstract boolean isUseKits();

    public abstract boolean isGroupMode();

    public abstract List<String> getCommandsBeforeBattle();

    public abstract List<String> getCommandsAfterBattle();

    public abstract Integer getMinimumPlayers();

    public abstract Integer getMinimumGroups();

    public abstract Integer getMaximumPlayers();

    public abstract Integer getMaximumPlayersPerGroup();

    public abstract Integer getMaximumGroups();
}
