package me.roinujnosde.titansbattle.types;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Game {

    private final GameConfiguration config;

    private final List<UUID> playerParticipants = new ArrayList<>();
    private final Map<Group, Integer> groupParticipants = new HashMap<>();

    private final HashMap<Player, Integer> killsCount = new HashMap<>();
    private final List<Player> casualties = new ArrayList<>();

    private boolean happening;
    private boolean lobby;
    private boolean battle;
    private boolean preparation;

    public Game(@NotNull GameConfiguration config) {
        this.config = config;
    }

    public @NotNull GameConfiguration getConfig() {
        return config;
    }

    public @NotNull List<UUID> getPlayerParticipants() {
        return playerParticipants;
    }

    public int getGroupsParticipatingCount() {
        if (!happening) {
            return 0;
        }
        return groupParticipants.size();
    }

    public int getPlayersParticipatingCount() {
        return playerParticipants.size();
    }

    public List<Player> getCasualties() {
        return casualties;
    }

    public Map<Group, Integer> getGroups() {
        return groupParticipants;
    }

    public HashMap<Player, Integer> getKillsCount() {
        return killsCount;
    }

    public int getPlayerKillsCount(Player player) {
        return killsCount.getOrDefault(player, 0);
    }

    public void increaseKillsCount(Player player) {
        killsCount.put(player, killsCount.getOrDefault(player, 0) + 1);
    }

    public void teleportAll(Location destination) {
        if (destination == null) {
            return;
        }

        getPlayerParticipants().forEach(uuid -> Bukkit.getPlayer(uuid).teleport(destination));
    }

    public void setLobby(boolean lobby) {
        this.lobby = lobby;
    }

    public void setBattle(boolean battle) {
        this.battle = battle;
    }

    public void setHappening(boolean happening) {
        this.happening = happening;
    }

    public void setPreparation(boolean preparation) {
        this.preparation = preparation;
    }

    public boolean isLobby() {
        return lobby;
    }

    public boolean isPreparation() {
        return preparation;
    }

    public boolean isBattle() {
        return battle;
    }

    public boolean isHappening() {
        return happening;
    }

    @Override
    public int hashCode() {
        return getConfig().getName().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Game)) {
            return false;
        }
        Game otherGame = (Game) other;
        return otherGame.getConfig().getName().equals(getConfig().getName());
    }

}
