package me.roinujnosde.titansbattle.types;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static me.roinujnosde.titansbattle.utils.Helper.caseInsensitiveMap;

public class GroupData {

    private final Map<String, Integer> victories;
    private final Map<String, Integer> defeats;
    private final Map<String, Integer> kills;
    private final Map<String, Integer> deaths;
    private boolean isModified;

    public GroupData() {
        this(null, null, null, null);
        isModified = true;
    }

    public GroupData(
            @Nullable Map<String, Integer> victories,
            @Nullable Map<String, Integer> defeats,
            @Nullable Map<String, Integer> kills,
            @Nullable Map<String, Integer> deaths) {
        this.victories = caseInsensitiveMap(victories);
        this.kills = caseInsensitiveMap(kills);
        this.deaths = caseInsensitiveMap(deaths);
        this.defeats = caseInsensitiveMap(defeats);
    }

    public int getTotalVictories() {
        return victories.values().stream().mapToInt(i -> i).sum();
    }

    public int getVictories(String game) {
        return victories.getOrDefault(game, 0);
    }

    public int getKills(String game) {
        return kills.getOrDefault(game, 0);
    }

    public int getDeaths(String game) {
        return deaths.getOrDefault(game, 0);
    }

    public int getDefeats(String game) {
        return defeats.getOrDefault(game, 0);
    }

    public void increaseDefeats(@NotNull String game) {
        defeats.compute(game, (g, i) -> i == null ? 1 : i + 1);
        isModified = true;
    }

    public void increaseVictories(@NotNull String game) {
        victories.compute(game, (g, i) -> i == null ? 1 : i + 1);
        isModified = true;
    }

    public void setVictories(String game, int newVictories) {
        victories.put(game, newVictories);
    }

    public void setKills(String game, int newKills) {
        kills.put(game, newKills);
        isModified = true;
    }

    public void setDeaths(String game, int newDeaths) {
        deaths.put(game, newDeaths);
        isModified = true;
    }

    public void setDefeats(String game, int newDefeats) {
        defeats.put(game, newDefeats);
    }

    public boolean isModified() {
        return isModified;
    }

    public void setModified(boolean isModified) {
        this.isModified = isModified;
    }
}
