package me.roinujnosde.titansbattle.types;

import me.roinujnosde.titansbattle.BaseGameConfiguration;
import me.roinujnosde.titansbattle.serialization.ConfigUtils;
import me.roinujnosde.titansbattle.serialization.Path;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "unused"})
@SerializableAs("game")
public class GameConfiguration extends BaseGameConfiguration {

    private String type = "FreeForAllGame";
    private Boolean powerOfTwo = false;
    private Boolean killer = true;

    @Path("minimum.groups")
    private Integer minimumGroups = 2;
    @Path("maximum.groups")
    private Integer maximumGroups = 0;
    @Path("maximum.players_per_group")
    private Integer maximumPlayersPerGroup = 0;

    private Boolean deleteGroups = false;

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
    private String permission = "";

    public GameConfiguration() {
        this(Collections.emptyMap());
    }

    public GameConfiguration(@NotNull Map<String, Object> data) {
        ConfigUtils.deserialize(this, data);
    }

    @Override
    public Map<String, Object> serialize() {
        return ConfigUtils.serialize(this);
    }

    /**
     * The name of the class responsible for managing this game
     *
     * @return the class name
     */
    public String getType() {
        return type;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    @Override
    public Integer getMinimumGroups() {
        return Math.max(2, minimumGroups);
    }

    @Override
    public Integer getMaximumPlayersPerGroup() {
        return maximumPlayersPerGroup;
    }

    @Override
    public Integer getMaximumGroups() {
        return maximumGroups;
    }

    @Override
    public Integer getMinimumPlayers() {
        return Math.max(2, minimumPlayers);
    }

    public Integer getPreparationTime() {
        return preparationTime;
    }

    public Integer getExpirationTime() {
        return expirationTime;
    }

    public Integer getAnnouncementGameInfoInterval() {
        return announcementGameInfoInterval;
    }

    public Boolean isPowerOfTwo() {
        return powerOfTwo;
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

    public String getPermission() {
        return permission;
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
}
