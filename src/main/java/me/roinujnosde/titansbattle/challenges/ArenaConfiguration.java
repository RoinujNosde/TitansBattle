package me.roinujnosde.titansbattle.challenges;

import me.roinujnosde.titansbattle.BaseGameConfiguration;
import me.roinujnosde.titansbattle.types.Kit;
import me.roinujnosde.titansbattle.utils.ConfigUtils;
import me.roinujnosde.titansbattle.utils.Path;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public class ArenaConfiguration extends BaseGameConfiguration implements ConfigurationSerializable {

    // TODO Private fields
    private String name;
    private Boolean useKits = false;
    private Kit kit;
    private Boolean groupMode = false;
    private Boolean pvp = true;
    private Boolean clearItemsOnDeath = false;

    @Path("damage-type.melee")
    private Boolean meleeDamage = true;
    @Path("damage-type.ranged")
    private Boolean rangedDamage = true;
    @Path("minimum.players")
    private Integer minimumPlayers = 2;
    @Path("maximum.players")
    private Integer maximumPlayers = 100;

    @Path("time.expiration")
    private Integer expirationTime = 3600;
    @Path("time.preparation")
    private Integer preparationTime = 30;

    @Path("destination.entrance.1")
    private Location entrance1;
    @Path("destination.entrance.2")
    private Location entrance2;
    @Path("destination.watchroom")
    private Location watchroom;
    @Path("destination.exit")
    private Location exit;
    @Path("destination.lobby")
    private Location lobby;

    public ArenaConfiguration() {}

    public ArenaConfiguration(@NotNull Map<String, Object> data) {
        ConfigUtils.deserialize(this, data);
    }

    @Override
    public Map<String, Object> serialize() {
        return ConfigUtils.serialize(this);
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull Location getLobby() {
        return lobby;
    }

    @Override
    public @NotNull Location getWatchroom() {
        return watchroom;
    }

    @Override
    public @NotNull Location getExit() {
        return exit;
    }

    @Override
    public @Nullable Kit getKit() {
        return kit;
    }

    @Override
    public boolean isUseKits() {
        return useKits;
    }

    @Override
    public boolean isGroupMode() {
        return groupMode;
    }

    @Override
    public Integer getMinimumPlayers() {
        return minimumPlayers;
    }

    @Override
    public Integer getMinimumGroups() {
        if (isGroupMode()) {
            return 2;
        }
        return 0;
    }

    @Override
    public Integer getMaximumPlayers() {
        return maximumPlayers;
    }

    @Override
    public Integer getMaximumPlayersPerGroup() {
        return getMaximumPlayers() / 2;
    }

    @Override
    public Integer getMaximumGroups() {
        return 2;
    }

}
