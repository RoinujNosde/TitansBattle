package me.roinujnosde.titansbattle.listeners;

import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.challenges.ChallengeRequest;
import me.roinujnosde.titansbattle.types.Warrior;

import java.util.Set;

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
            plugin.getGameManager().getCurrentGame().ifPresent(game -> game.onRespawn(warrior));
            Set<ChallengeRequest<?>> requests = plugin.getChallengeManager().getRequests();
            for (ChallengeRequest<?> request : requests) {
                request.getChallenge().onRespawn(warrior);
            }
        });
    }
}
