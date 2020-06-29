package me.roinujnosde.titansbattle.utils;

import com.massivecraft.factions.entity.MPlayer;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.Group;
import me.roinujnosde.titansbattle.types.GroupWrapper;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Groups {
    private static final TitansBattle plugin = TitansBattle.getInstance();

    private Groups() {}

    /**
     * Gets the group the player is part of or null if they are not in one
     *
     * @param uuid the player's uuid
     * @return the group or null
     */
    @Nullable
    public static Group getGroup(@NotNull UUID uuid) {
        return plugin.getDatabaseManager().getGroup(getGroupWrapper(uuid));
    }

    /**
     * Gets a GroupWrapper for this OfflinePlayer, or null if he is not a member
     *
     * @param uuid the player's uuid
     * @return the GroupWrapper, or null
     */
    @Nullable
    public static GroupWrapper getGroupWrapper(@NotNull UUID uuid) {
        if (plugin.isFactions()) {
            MPlayer mp = MPlayer.get(uuid);
            if (mp == null) {
                plugin.debug(String.format("Player data not found for %s", uuid), true);
                return null;
            }
            if (mp.hasFaction()) {
                plugin.debug(String.format("Player %s is in the Faction %s", uuid, mp.getFactionName()), true);
                return new GroupWrapper(mp.getFaction());
            }
            plugin.debug(String.format("Player %s is not in a Faction", uuid), true);
        }
        if (plugin.isSimpleClans()) {
            @SuppressWarnings("ConstantConditions")
            ClanPlayer cp = plugin.getClanManager().getClanPlayer(uuid);
            if (cp == null) {
                return null;
            }
            return new GroupWrapper(cp.getClan());
        }
        return null;
    }
}
