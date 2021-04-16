package me.roinujnosde.titansbattle.types;

import me.roinujnosde.titansbattle.utils.ConfigUtils;
import me.roinujnosde.titansbattle.utils.Path;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "unused"})
public class GameConfiguration implements ConfigurationSerializable {

    private String name;
    private Boolean groupMode = false;
    private Boolean eliminationTournament = false;
    private Boolean powerOfTwo = false;
    private Boolean killer = true;
    private Boolean pvp = true;
    private Boolean clearItemsOnDeath = false;
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
    @Path("run_commands.before_battle")
    private @Nullable List<String> commandsBeforeBattle;
    @Path("run_commands.after_battle")
    private @Nullable List<String> commandsAfterBattle;

    private Boolean useKits = false;
    private Kit kit;

    @Path("prizes")
    private Map<String, Prizes> prizesMap = createPrizesMap();

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

    public @Nullable List<String> getCommandsBeforeBattle() {
        return commandsBeforeBattle;
    }

    public @Nullable List<String> getCommandsAfterBattle() {
        return commandsAfterBattle;
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
        Prizes prizes = prizesMap.get(prize.name());
        if (prizes == null) {
            Logger.getLogger("TitansBattle").warning(String.format("Prizes not set for %s!", prize.name()));
            prizes = new Prizes();
        }
        return prizes;
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

    public Boolean isPvP() {
        return pvp;
    }

    public Boolean isClearItemsOnDeath() {
        return clearItemsOnDeath;
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

    private Map<String, Prizes> createPrizesMap() {
        LinkedHashMap<String, Prizes> map = new LinkedHashMap<>();
        for (Prize p : Prize.values()) {
            map.put(p.name(), new Prizes());
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
