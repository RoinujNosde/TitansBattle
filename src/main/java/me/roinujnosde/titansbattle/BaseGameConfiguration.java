package me.roinujnosde.titansbattle;

import me.roinujnosde.titansbattle.serialization.ConfigUtils;
import me.roinujnosde.titansbattle.serialization.Path;
import me.roinujnosde.titansbattle.types.Kit;
import me.roinujnosde.titansbattle.types.Prizes;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

@SuppressWarnings("FieldMayBeFinal")
public abstract class BaseGameConfiguration implements ConfigurationSerializable {

    public enum Destination {
        EXIT, LOBBY, WATCHROOM, BORDER_CENTER
    }

    protected transient File file;
    protected transient FileConfiguration fileConfiguration;

    protected String name;
    protected Boolean groupMode = false;
    protected Boolean clearItemsOnDeath = false;
    protected Boolean keepExp = false;
    protected Boolean useKits = false;
    protected Kit kit;
    @Path("items.whitelist")
    protected List<String> whitelistedItems;
    @Path("items.blacklist")
    protected List<String> blacklistedItems;
    @Path("prizes")
    private Map<String, Prizes> prizesMap = createPrizesMap();

    protected Boolean pvp = true;
    @Path("damage-type.melee")
    protected Boolean meleeDamage = true;
    @Path("damage-type.ranged")
    protected Boolean rangedDamage = true;
    @Path("minimum.players")
    protected Integer minimumPlayers = 2;
    @Path("maximum.players")
    protected Integer maximumPlayers = 100;

    @Path("announcement.starting.times")
    protected Integer announcementStartingTimes = 5;
    @Path("announcement.starting.interval")
    protected Integer announcementStartingInterval = 20;

    @Path("destination.watchroom")
    protected Location watchroom;
    @Path("destination.exit")
    protected Location exit;
    @Path("destination.lobby")
    protected Location lobby;
    @Path("destination.arena_entrances")
    protected Map<Integer, Location> arenaEntrances = new HashMap<>();
    @Path("destination.border_center")
    protected Location borderCenter;

    @Path("time.preparation")
    protected Integer preparationTime = 30;
    @Path("time.expiration")
    protected Integer expirationTime = 3600;

    @Path("run_commands.before_battle")
    protected @Nullable List<String> commandsBeforeBattle;
    @Path("run_commands.after_battle")
    protected @Nullable List<String> commandsAfterBattle;

    @Path("worldborder.enable")
    protected Boolean worldBorder = false;
    @Path("worldborder.initial_size")
    protected Integer borderInitialSize = 5000;
    @Path("worldborder.final_size")
    protected Integer borderFinalSize = 500;
    @Path("worldborder.shrink")
    protected Integer borderShrink = 25;
    @Path("worldborder.interval")
    protected Integer borderInterval = 120;
    @Path("worldborder.damage")
    protected Double borderDamage = 5.0;

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
        return arenaEntrances.get(1) != null && exit != null && lobby != null && watchroom != null
                && (!worldBorder || borderCenter != null);
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

    public Boolean isKeepExp() {
        return keepExp;
    }

    public Boolean isUseKits() {
        return useKits;
    }

    public @Nullable Kit getKit() {
        return kit;
    }

    public @Nullable List<String> getWhitelistedItems() {
        return whitelistedItems;
    }

    public List<String> getBlacklistedItems() {
        return blacklistedItems;
    }

    public void setKit(Kit kit) {
        this.kit = kit;
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

    public Integer getAnnouncementStartingInterval() {
        return announcementStartingInterval;
    }

    public Integer getAnnouncementStartingTimes() {
        return announcementStartingTimes;
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

    public Map<Integer, Location> getArenaEntrances() {
        return arenaEntrances;
    }

    public Location getBorderCenter() {
        return borderCenter;
    }

    public void setExit(Location exit) {
        this.exit = exit;
    }

    public void setLobby(Location lobby) {
        this.lobby = lobby;
    }

    public void setArenaEntrance(int index, Location entrance) {
        arenaEntrances.put(index, entrance);
    }

    public void setWatchroom(Location watchroom) {
        this.watchroom = watchroom;
    }

    public void setBorderCenter(Location borderCenter) {
        this.borderCenter = borderCenter;
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

    public Boolean isWorldBorder() {
        return worldBorder;
    }

    public Integer getBorderInterval() {
        return borderInterval;
    }

    public Integer getBorderInitialSize() {
        return borderInitialSize;
    }

    public Integer getBorderFinalSize() {
        return borderFinalSize;
    }

    public Integer getBorderShrinkSize() {
        return borderShrink;
    }

    public Double getBorderDamage() {
        return borderDamage;
    }

    public Prizes getPrizes(@NotNull Prize prize) {
        Prizes prizes = prizesMap.get(prize.name());
        if (prizes == null) {
            Logger.getLogger("TitansBattle").warning(String.format("Prizes not set for %s!", prize.name()));
            prizes = new Prizes();
        }
        return prizes;
    }

    private Map<String, Prizes> createPrizesMap() {
        LinkedHashMap<String, Prizes> map = new LinkedHashMap<>();
        for (Prize p : Prize.values()) {
            map.put(p.name(), new Prizes());
        }
        return map;
    }

    public enum Prize implements ConfigurationSerializable {
        FIRST, SECOND, THIRD, KILLER;

        @SuppressWarnings("unused")
        public static Prize deserialize(Map<String, Object> data) {
            return Prize.valueOf((String) data.get("prize"));
        }

        @Override
        public Map<String, Object> serialize() {
            return Collections.singletonMap("prize", this.name());
        }
    }
}
