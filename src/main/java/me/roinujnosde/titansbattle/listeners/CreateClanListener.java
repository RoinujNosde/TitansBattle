package me.roinujnosde.titansbattle.listeners;

import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.utils.Groups;
import net.sacredlabyrinth.phaed.simpleclans.events.CreateClanEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CreateClanListener implements Listener {

    private final TitansBattle plugin;

    public CreateClanListener(@NotNull TitansBattle plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCreate(CreateClanEvent event) {
        UUID uuid = event.getClan().getLeaders().get(0).getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> Groups.getGroup(uuid));
    }
}
