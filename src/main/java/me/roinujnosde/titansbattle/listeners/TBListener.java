package me.roinujnosde.titansbattle.listeners;

import java.util.Optional;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.roinujnosde.titansbattle.BaseGame;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.challenges.Challenge;
import me.roinujnosde.titansbattle.challenges.ChallengeRequest;
import me.roinujnosde.titansbattle.games.Game;
import me.roinujnosde.titansbattle.types.Warrior;

public abstract class TBListener implements Listener {

    protected TitansBattle plugin;

    public TBListener(@NotNull TitansBattle plugin) {
        this.plugin = plugin;
    }

    public @Nullable BaseGame getBaseGameFrom(@NotNull Player player) {
        Warrior warrior = plugin.getDatabaseManager().getWarrior(player);

        Optional<Game> currentGame = plugin.getGameManager().getCurrentGame();
        if (currentGame.isPresent()) {
            if (currentGame.get().isParticipant(warrior)) {
                return currentGame.get();
            }
        }
        Set<ChallengeRequest<?>> requests = plugin.getChallengeManager().getRequests();
        for (ChallengeRequest<?> request : requests) {
            Challenge challenge = request.getChallenge();
            if (challenge.isParticipant(warrior)) {
                return challenge;
            }
        }
        return null;
    }

}
