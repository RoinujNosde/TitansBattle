package me.roinujnosde.titansbattle.challenges;

import me.roinujnosde.titansbattle.BaseGameConfiguration;
import me.roinujnosde.titansbattle.utils.ConfigUtils;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

@SerializableAs("arena")
public class ArenaConfiguration extends BaseGameConfiguration {

    public ArenaConfiguration() {
        this(Collections.emptyMap());
    }

    public ArenaConfiguration(@NotNull Map<String, Object> data) {
        super(data);
    }

    @Override
    public Map<String, Object> serialize() {
        return ConfigUtils.serialize(this);
    }

    @Override
    public Integer getMinimumGroups() {
        if (isGroupMode()) {
            return 2;
        }
        return 0;
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
