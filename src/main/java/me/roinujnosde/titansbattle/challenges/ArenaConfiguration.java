package me.roinujnosde.titansbattle.challenges;

import me.roinujnosde.titansbattle.BaseGameConfiguration;
import me.roinujnosde.titansbattle.types.Kit;
import me.roinujnosde.titansbattle.utils.ConfigUtils;
import me.roinujnosde.titansbattle.utils.Path;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ArenaConfiguration implements ConfigurationSerializable, BaseGameConfiguration {

    // TODO Private fields
    public String name;
    public Boolean useKits = false;
    public Kit kit;
    public Boolean groupMode = false;
    public Boolean pvp = true;
    public Boolean clearItemsOnDeath = false;

    @Path("damage-type.melee")
    public Boolean meleeDamage = true;
    @Path("damage-type.ranged")
    public Boolean rangedDamage = true;
    @Path("minimum.players")
    public Integer minimumPlayers = 2;
    @Path("maximum.players")
    public Integer maximumPlayers = 100;

    @Path("time.expiration")
    public Integer expirationTime = 3600;
    @Path("time.preparation")
    public Integer preparationTime = 30;

    @Path("destination.entrance.1")
    public Location entrance1;
    @Path("destination.entrance.2")
    public Location entrance2;
    @Path("destination.watchroom")
    public Location watchroom;
    @Path("destination.exit")
    public Location exit;
    @Path("destination.lobby")
    public Location lobby;

    public ArenaConfiguration() {}

    public ArenaConfiguration(@NotNull Map<String, Object> data) {
        ConfigUtils.deserialize(this, data);
    }

    @Override
    public Map<String, Object> serialize() {
        return ConfigUtils.serialize(this);
    }

    @Override
    public Integer getMaximumGroups() {
        return 2;
    }

}
