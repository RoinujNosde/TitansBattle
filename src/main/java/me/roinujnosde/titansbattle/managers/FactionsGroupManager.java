package me.roinujnosde.titansbattle.managers;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import me.roinujnosde.titansbattle.types.FactionsGroup;
import me.roinujnosde.titansbattle.types.Group;
import me.roinujnosde.titansbattle.TitansBattle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class FactionsGroupManager extends GroupManager {

    public FactionsGroupManager(@NotNull TitansBattle plugin) {
        super(plugin);
    }

    @Override
    public @NotNull Set<Group> getGroups() {
        return FactionColl.get().getAll().stream()
                .map(fac -> new FactionsGroup(fac, idToData.apply(fac.getId()))).collect(Collectors.toSet());
    }

    @Override
    public @Nullable Group getGroup(@NotNull UUID uuid) {
        MPlayer mp = MPlayer.get(uuid);
        if (mp != null) {
            if (mp.hasFaction()) {
                Faction faction = mp.getFaction();
                return new FactionsGroup(faction, idToData.apply(faction.getId()));
            } else {
                plugin.debug(String.format("Player %s is not in a Faction", uuid), true);
            }
        } else {
            plugin.debug(String.format("Player data not found for %s", uuid), true);
        }
        return null;
    }

    @Override
    public boolean sameGroup(@NotNull UUID player1, @NotNull UUID player2) {
        MPlayer mp1 = MPlayer.get(player1);
        MPlayer mp2 = MPlayer.get(player2);
        if (mp1 == null || mp2 == null || !mp1.hasFaction() || !mp2.hasFaction()) {
            return false;
        }
        return mp1.getFaction().getId().equals(mp2.getFaction().getId());
    }
}
