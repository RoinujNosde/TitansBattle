package me.roinujnosde.titansbattle;

import me.roinujnosde.titansbattle.types.Kit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public abstract class BaseGameConfiguration {

    protected File file; // TODO Transient?
    protected FileConfiguration fileConfiguration;  // TODO Does transient keeps it from being saved?
    protected String name;

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
    abstract Location getLobby();

    @NotNull
    abstract Location getWatchroom();

    @NotNull
    abstract Location getExit();

    @Nullable
    abstract Kit getKit();

    abstract boolean isUseKits();

    abstract boolean isGroupMode();

    abstract List<String> getCommandsBeforeBattle();

    abstract List<String> getCommandsAfterBattle();

    abstract Integer getMinimumPlayers();

    abstract Integer getMinimumGroups();

    abstract Integer getMaximumPlayers();

    abstract Integer getMaximumPlayersPerGroup();

    abstract Integer getMaximumGroups();
}
