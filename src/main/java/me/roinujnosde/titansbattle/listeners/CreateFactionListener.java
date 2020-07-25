package me.roinujnosde.titansbattle.listeners;

import com.massivecraft.factions.event.EventFactionsCreate;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.utils.Groups;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class CreateFactionListener implements Listener {

    private final TitansBattle plugin;

    public CreateFactionListener(@NotNull TitansBattle plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCreate(EventFactionsCreate event) {
        //Running later so that the factions gets created
        Bukkit.getScheduler().runTask(plugin, () -> {
            //Loading into memory
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> Groups.getGroup(event.getMPlayer().getUuid()));
        });
    }
}
