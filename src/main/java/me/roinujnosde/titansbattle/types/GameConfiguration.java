package me.roinujnosde.titansbattle.types;

import me.roinujnosde.titansbattle.utils.ConfigUtils;
import me.roinujnosde.titansbattle.utils.Path;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "unused"})
public class GameConfiguration implements ConfigurationSerializable {

    private String name;
    private Boolean groupMode = false;
    private Boolean eliminationTournament = false;
    private Boolean powerOfTwo = false;
    private Boolean killer = true;
    @Path("minimum.groups")
    private Integer minimumGroups = 2;
    @Path("maximum.groups")
    private Integer maximumGroups = 0;
    @Path("maximum.players_per_group")
    private Integer maximumPlayersPerGroup = 0;
    @Path("minimum.players")
    private Integer minimumPlayers = 10;
    @Path("maximum.players")
    private Integer maximumPlayers = 100;
    private Boolean deleteGroups = false;
    @Path("time.expiration")
    private Integer expirationTime = 3600;
    @Path("time.preparation")
    private Integer preparationTime = 30;

    private Boolean useKits = false;
    private Kit kit;

    @Path("prizes")
    private Map<Prize, Prizes> prizesMap = createPrizesMap();

    @Path("destination.exit")
    private Location exit;
    @Path("destination.lobby")
    private Location lobby;
    @Path("destination.watchroom")
    private Location watchroom;
    @Path("destination.arena")
    private Location arena;

    @Path("announcement.starting.times")
    private Integer announcementStartingTimes = 5;
    @Path("announcement.starting.interval")
    private Integer announcementStartingInterval = 20;
    @Path("announcement.game_info.interval")
    private Integer announcementGameInfoInterval = 30;

    @Path("prefix.winner")
    private String winnerPrefix = "&a[Winner] ";
    @Path("prefix.killer")
    private String killerPrefix = "&c[Killer] ";
    @Path("prefix.killer_priority")
    private Boolean killerPriority = true;
    @Path("message.killer.join")
    private Boolean killerJoinMessage = false;
    @Path("message.winner.join")
    private Boolean winnerJoinMessage = false;
    @Path("message.killer.quit")
    private Boolean killerQuitMessage = false;
    @Path("message.winner.quit")
    private Boolean winnerQuitMessage = false;

    public GameConfiguration() {}

    public GameConfiguration(@NotNull Map<String, Object> data) {
        ConfigUtils.deserialize(this, data);
    }

    @Override
    public Map<String, Object> serialize() {
        return ConfigUtils.serialize(this);
    }

    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public Integer getMinimumGroups() {
        return Math.max(2, minimumGroups);
    }

    public Integer getMinimumPlayers() {
        return Math.max(2, minimumPlayers);
    }

    public Integer getMaximumPlayers() {
        return maximumPlayers;
    }

    public Integer getMaximumGroups() {
        return maximumGroups;
    }

    public Integer getMaximumPlayersPerGroup() {
        return maximumPlayersPerGroup;
    }

    public Integer getPreparationTime() {
        return preparationTime;
    }

    public Integer getExpirationTime() {
        return expirationTime;
    }

    public Integer getAnnouncementStartingInterval() {
        return announcementStartingInterval;
    }

    public Integer getAnnouncementStartingTimes() {
        return announcementStartingTimes;
    }

    public Integer getAnnouncementGameInfoInterval() {
        return announcementGameInfoInterval;
    }

    public Prizes getPrizes(@NotNull Prize prize) {
        return prizesMap.get(prize);
    }

    public @Nullable Kit getKit() {
        return kit;
    }

    public void setKit(Kit kit) {
        this.kit = kit;
    }

    public void setExit(Location exit) {
        this.exit = exit;
    }

    public void setLobby(Location lobby) {
        this.lobby = lobby;
    }

    public void setArena(Location arena) {
        this.arena = arena;
    }

    public void setWatchroom(Location watchroom) {
        this.watchroom = watchroom;
    }

    public Location getExit() {
        return exit;
    }

    public Location getLobby() {
        return lobby;
    }

    public Location getArena() {
        return arena;
    }

    public Location getWatchroom() {
        return watchroom;
    }

    public Boolean isUseKits() {
        return useKits;
    }

    public Boolean isEliminationTournament() {
        return eliminationTournament;
    }

    public Boolean isPowerOfTwo() {
        return powerOfTwo;
    }

    public Boolean isGroupMode() {
        return groupMode;
    }

    public Boolean isKiller() {
        return killer;
    }

    public Boolean isDeleteGroups() {
        return deleteGroups;
    }

    public Boolean isKillerPriority() {
        return killerPriority;
    }

    public Boolean isWinnerJoinMessage() {
        return winnerJoinMessage;
    }

    public Boolean isWinnerQuitMessage() {
        return winnerQuitMessage;
    }

    public Boolean isKillerJoinMessage() {
        return killerJoinMessage;
    }

    public Boolean isKillerQuitMessage() {
        return killerQuitMessage;
    }

    public String getKillerPrefix() {
        return killerPrefix;
    }

    public String getWinnerPrefix() {
        return winnerPrefix;
    }

    public Boolean locationsSet() {
        return arena != null && exit != null && lobby != null && watchroom != null;
    }

    private Map<Prize, Prizes> createPrizesMap() {
        HashMap<Prize, Prizes> map = new HashMap<>();
        for (Prize p : Prize.values()) {
            map.put(p, new Prizes());
        }
        return map;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other instanceof GameConfiguration) {
            return getName().equals(((GameConfiguration) other).getName());
        }
        return false;
    }

    public enum Prize implements ConfigurationSerializable {
        FIRST, SECOND, THIRD, KILLER;

        public static Prize deserialize(Map<String, Object> data) {
            return Prize.valueOf((String) data.get("prize"));
        }

        @Override
        public Map<String, Object> serialize() {
            return Collections.singletonMap("prize", this.name());
        }
    }
}
