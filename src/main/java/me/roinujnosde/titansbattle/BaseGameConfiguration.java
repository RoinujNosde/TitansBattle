package me.roinujnosde.titansbattle;

import me.roinujnosde.titansbattle.types.Kit;
import me.roinujnosde.titansbattle.utils.ConfigUtils;
import me.roinujnosde.titansbattle.utils.Path;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Map;

public abstract class BaseGameConfiguration implements ConfigurationSerializable {

    protected transient File file;
    protected transient FileConfiguration fileConfiguration;

    protected String name;
    protected Boolean groupMode;
    protected Boolean clearItemsOnDeath = false;
    protected Boolean useKits = false;
    protected Kit kit;

    protected Boolean pvp = true;
    @Path("damage-type.melee")
    protected Boolean meleeDamage = true;
    @Path("damage-type.ranged")
    protected Boolean rangedDamage = true;
    @Path("minimum.players")
    protected Integer minimumPlayers = 2;
    @Path("maximum.players")
    protected Integer maximumPlayers = 100;

    @Path("destination.watchroom")
    protected Location watchroom;
    @Path("destination.exit")
    protected Location exit;
    @Path("destination.lobby")
    protected Location lobby;
    @Path("destination.arena")
    protected Location arena; // TODO Make all games support multiple entrances

    @Path("time.preparation")
    protected Integer preparationTime = 30;
    @Path("time.expiration")
    protected Integer expirationTime = 3600;

    @Path("run_commands.before_battle")
    protected @Nullable List<String> commandsBeforeBattle;
    @Path("run_commands.after_battle")
    protected @Nullable List<String> commandsAfterBattle;

    public BaseGameConfiguration(Map<String, Object> data) {
        ConfigUtils.deserialize(this, data);
    }

    public @NotNull FileConfiguration getFileConfiguration() {
        if (fileConfiguration == null) {
            throw new IllegalStateException();
        }
        return fileConfiguration;
    }

    public void setFileConfiguration(@NotNull FileConfiguration fileConfiguration) {
        this.fileConfiguration = fileConfiguration;
    }

    public @NotNull File getFile() {
        if (file == null) {
            throw new IllegalStateException();
        }
        return file;
    }

    public void setFile(@NotNull File file) {
        this.file = file;
    }

    @Override
    public Map<String, Object> serialize() {
        return ConfigUtils.serialize(this);
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean locationsSet() {
        return arena != null && exit != null && lobby != null && watchroom != null;
    }

    public abstract Integer getMinimumGroups();

    public abstract Integer getMaximumPlayersPerGroup();

    public abstract Integer getMaximumGroups();

    public Boolean isGroupMode() {
        return groupMode;
    }

    public Boolean isClearItemsOnDeath() {
        return clearItemsOnDeath;
    }

    public Boolean isUseKits() {
        return useKits;
    }

    public Kit getKit() {
        return kit;
    }

    public Boolean isPvP() {
        return pvp;
    }

    public Boolean isMeleeDamage() {
        return meleeDamage;
    }

    public Boolean isRangedDamage() {
        return rangedDamage;
    }

    public Integer getMinimumPlayers() {
        return minimumPlayers;
    }

    public Integer getMaximumPlayers() {
        return maximumPlayers;
    }

    public Location getWatchroom() {
        return watchroom;
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
}
