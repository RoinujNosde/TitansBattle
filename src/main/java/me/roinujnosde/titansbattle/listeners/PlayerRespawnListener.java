package me.roinujnosde.titansbattle.listeners;

import me.roinujnosde.titansbattle.BaseGame;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.Warrior;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerRespawnListener extends TBListener {

    public PlayerRespawnListener(@NotNull TitansBattle plugin) {
        super(plugin);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        final Warrior warrior = plugin.getDatabaseManager().getWarrior(event.getPlayer());

        Bukkit.getScheduler().runTask(plugin, () -> {
            BaseGame game = getBaseGameFrom(event.getPlayer());
            if (game != null) {
                game.onRespawn(warrior);
            }
        });
    }
}
