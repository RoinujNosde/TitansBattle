package me.roinujnosde.titansbattle.challenges;

import me.roinujnosde.titansbattle.BaseGame;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.Warrior;
import org.jetbrains.annotations.NotNull;

public class Challenge extends BaseGame {

    private final ArenaConfiguration config;

    public Challenge(@NotNull TitansBattle plugin, @NotNull ArenaConfiguration config) {
        super(plugin);
        this.config = config;
    }

    public void start() {

    }

    @Override
    public boolean canJoin(@NotNull Warrior warrior) {

    }

    public abstract boolean isInBattle(@NotNull Warrior warrior);

    public abstract boolean isParticipant(@NotNull Warrior warrior);

    public abstract boolean shouldClearDropsOnDeath(@NotNull Warrior warrior);

    public abstract boolean shouldKeepInventoryOnDeath(@NotNull Warrior warrior);

    protected abstract boolean processRemainingPlayers(@NotNull Warrior warrior);

    public abstract void onDisconnect(@NotNull Warrior warrior);

    public abstract void onLeave(@NotNull Warrior warrior);

    public abstract void onRespawn(@NotNull Warrior warrior);

    public abstract void onDeath(@NotNull Warrior warrior);

    public abstract void onLobbyEnd();

    public abstract boolean finish(boolean canceled);


    public @NotNull ArenaConfiguration getConfig() {
        return config;
    }
}