package me.roinujnosde.titansbattle.managers;

import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.challenges.Challenge;
import me.roinujnosde.titansbattle.challenges.ChallengeRequest;
import me.roinujnosde.titansbattle.types.Warrior;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ChallengeManager {

    private final TitansBattle plugin;
    private final Set<Challenge> challenges = new HashSet<>();
    private final List<ChallengeRequest<?>> requests = new ArrayList<>();

    public ChallengeManager(TitansBattle plugin) {
        this.plugin = plugin;
    }

    public Set<Challenge> getChallenges() {
        return Collections.unmodifiableSet(challenges);
    }

    public List<ChallengeRequest<?>> getRequests() {
        return requests;
    }

    public @NotNull List<ChallengeRequest<?>> getRequestsByInvited(@NotNull Warrior warrior) {
        List<ChallengeRequest<?>> result = new ArrayList<>();
        for (ChallengeRequest<?> request : requests) {
            if (request.isInvited(warrior)) {
                result.add(request);
            }
        }
        return result;
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

    public void add(@NotNull ChallengeRequest<?> request) {
        Challenge challenge = request.getChallenge();
        if (challenges.contains(challenge)) {
            throw new IllegalStateException("cannot add another challenge in the same arena");
        }
        challenge.start();
        requests.add(request);
        challenges.add(challenge);
        plugin.getListenerManager().registerBattleListeners();
    }

    public void remove(@NotNull ChallengeRequest<?> request) {
        requests.remove(request);
        challenges.remove(request.getChallenge());
    }

    public void remove(final Challenge challenge) {
        challenges.remove(challenge);
        requests.removeIf(r -> r.getChallenge().equals(challenge));
        plugin.getListenerManager().unregisterBattleListeners();
    }

    public boolean isArenaInUse(@NotNull String name) {
        for (Challenge challenge : challenges) {
            if (challenge.getConfig().getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
