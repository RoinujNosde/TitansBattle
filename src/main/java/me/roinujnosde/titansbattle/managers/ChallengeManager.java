package me.roinujnosde.titansbattle.managers;

import me.roinujnosde.titansbattle.challenges.Challenge;
import me.roinujnosde.titansbattle.challenges.ChallengeRequest;
import me.roinujnosde.titansbattle.types.Warrior;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class ChallengeManager {

    private final Set<Challenge> challenges = new HashSet<>();
    private final Set<ChallengeRequest<?>> requests = new HashSet<>();

    public Set<ChallengeRequest<?>> getRequests() {
        return requests;
    }

    public @Nullable ChallengeRequest<?> getChallengeRequest(@NotNull Warrior challenger, boolean groupMode) {
        for (ChallengeRequest<?> request : requests) {
            if (groupMode && request.getChallenger().equals(challenger.getGroup())) {
                return request;
            }
            if (!groupMode && request.getChallenger().equals(challenger)) {
                return request;
            }
        }
        return null;
    }

    public void addRequest(@NotNull ChallengeRequest<?> request) {
        Challenge challenge = request.getChallenge();
        if (challenges.contains(challenge)) {
            throw new IllegalStateException("cannot add another challenge in the same arena");
        }
        requests.add(request); // TODO Remove from request after?
        challenges.add(challenge);
        challenge.start();
    }

}
