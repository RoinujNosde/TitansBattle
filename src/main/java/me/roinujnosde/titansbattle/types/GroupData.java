package me.roinujnosde.titansbattle.types;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class GroupData {

    private final Map<String, Integer> victories;
    private final Map<String, Integer> defeats;
    private final Map<String, Integer> kills;
    private final Map<String, Integer> deaths;

    public GroupData(
            @NotNull Map<String, Integer> victories,
            @NotNull Map<String, Integer> defeats,
            @NotNull Map<String, Integer> kills,
            @NotNull Map<String, Integer> deaths) {
        this.victories = victories;
        this.kills = kills;
        this.deaths = deaths;
        this.defeats = defeats;
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
    }

    public void increaseVictories(@NotNull String game) {
        victories.compute(game, (g, i) -> i == null ? 1 : i + 1);
    }

    public void setVictories(String game, int newVictories) {
        victories.put(game, newVictories);
    }

    public void setKills(String game, int newKills) {
        kills.put(game, newKills);
    }

    public void setDeaths(String game, int newDeaths) {
        deaths.put(game, newDeaths);
    }

    public void setDefeats(String game, int newDefeats) {
        defeats.put(game, newDefeats);
    }
}
