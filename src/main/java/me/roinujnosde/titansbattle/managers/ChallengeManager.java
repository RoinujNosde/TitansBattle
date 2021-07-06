package me.roinujnosde.titansbattle.managers;

import me.roinujnosde.titansbattle.challenges.ChallengeRequest;

import java.util.ArrayList;
import java.util.List;

public class ChallengeManager {

    private final List<ChallengeRequest<?>> requests = new ArrayList<>();

    public List<ChallengeRequest<?>> getRequests() {
        return requests;
    }
}
