package me.roinujnosde.titansbattle;

import me.roinujnosde.titansbattle.types.Kit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BaseGameConfiguration {

    @NotNull FileConfiguration getFileConfiguration(); // TODO Does transient keeps it from being saved?

    @NotNull Location getLobby();

    @Nullable Kit getKit();

    boolean isUseKits();

    boolean isGroupMode();
}
