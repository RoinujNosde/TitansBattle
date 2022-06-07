package me.roinujnosde.titansbattle.listeners;

import me.roinujnosde.titansbattle.TitansBattle;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public abstract class TBListener implements Listener {

    protected TitansBattle plugin;

    public TBListener(@NotNull TitansBattle plugin) {
        this.plugin = plugin;
    }

}
