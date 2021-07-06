package me.roinujnosde.titansbattle.challenges;

import me.roinujnosde.titansbattle.types.Warrior;
import org.jetbrains.annotations.NotNull;

public abstract class ChallengeRequest<T> {

    protected final Challenge challenge;
    protected final T challenger;
    protected final T challenged;

    public ChallengeRequest(Challenge challenge, @NotNull T challenger, @NotNull T challenged) {
        this.challenge = challenge;
        this.challenger = challenger;
        this.challenged = challenged;
    }

    public abstract boolean isInvited(@NotNull Warrior warrior);

    public @NotNull Challenge getArena() {
        return challenge;
    }
}
