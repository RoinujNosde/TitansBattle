package me.roinujnosde.titansbattle.challenges;

import me.roinujnosde.titansbattle.types.Warrior;
import org.jetbrains.annotations.NotNull;

public class WarriorChallengeRequest extends ChallengeRequest<Warrior> {

    public WarriorChallengeRequest(Challenge challenge, @NotNull Warrior challenger, @NotNull Warrior challenged) {
        super(challenge, challenger, challenged);
    }

    @Override
    public boolean isInvited(@NotNull Warrior warrior) {
        return warrior.equals(challenged) || warrior.equals(challenger);
    }

}
