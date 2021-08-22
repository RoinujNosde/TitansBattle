package me.roinujnosde.titansbattle.challenges;

import me.roinujnosde.titansbattle.types.Warrior;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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

    public @NotNull Challenge getChallenge() {
        return challenge;
    }

    public T getChallenger() {
        return challenger;
    }

    public abstract String getChallengerName();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChallengeRequest<?> that = (ChallengeRequest<?>) o;
        return challenge.equals(that.challenge) && challenger.equals(that.challenger) && challenged.equals(that.challenged);
    }

    @Override
    public int hashCode() {
        return Objects.hash(challenge, challenger, challenged);
    }
}
