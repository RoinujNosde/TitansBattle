package me.roinujnosde.titansbattle.challenges;

import me.roinujnosde.titansbattle.types.Group;
import me.roinujnosde.titansbattle.types.Warrior;
import org.jetbrains.annotations.NotNull;

public class GroupChallengeRequest  extends ChallengeRequest<Group> {

    public GroupChallengeRequest(Challenge challenge, @NotNull Group challenger, @NotNull Group challenged) {
        super(challenge, challenger, challenged);
    }

    @Override
    public String getChallengerName() {
        return challenger.getUniqueName();
    }

    @Override
    public boolean isInvited(@NotNull Warrior warrior) {
        Group group = warrior.getGroup();
        return challenger.equals(group) || challenged.equals(group);
    }

}
