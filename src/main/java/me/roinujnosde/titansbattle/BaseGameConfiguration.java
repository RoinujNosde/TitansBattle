package me.roinujnosde.titansbattle;

import me.roinujnosde.titansbattle.types.Kit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface BaseGameConfiguration {

    @NotNull FileConfiguration getFileConfiguration(); // TODO Does transient keeps it from being saved?

    @NotNull String getName();

    @NotNull Location getLobby();

    @NotNull Location getWatchroom();

    @NotNull Location getExit();

    @Nullable Kit getKit();

    boolean isUseKits();

    boolean isGroupMode();

    List<String> getCommandsBeforeBattle();

    List<String> getCommandsAfterBattle();

    Integer getMinimumPlayers();

    Integer getMinimumGroups();

    Integer getMaximumPlayers();

    Integer getMaximumPlayersPerGroup();

    Integer getMaximumGroups();
}
