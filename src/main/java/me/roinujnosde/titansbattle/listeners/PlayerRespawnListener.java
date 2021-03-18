package me.roinujnosde.titansbattle.listeners;

import me.roinujnosde.titansbattle.TitansBattle;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnListener implements Listener {

    private final TitansBattle plugin;

    public PlayerRespawnListener() {
        this.plugin = TitansBattle.getInstance();
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        plugin.getGameManager().getCurrentGame().ifPresent(game -> Bukkit.getScheduler().runTask(plugin,
                () -> game.onRespawn(plugin.getDatabaseManager().getWarrior(event.getPlayer().getUniqueId()))));
    }
}
