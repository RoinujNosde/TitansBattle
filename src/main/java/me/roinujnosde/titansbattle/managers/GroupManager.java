package me.roinujnosde.titansbattle.managers;

import me.roinujnosde.titansbattle.games.Game;
import me.roinujnosde.titansbattle.types.Group;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.GroupData;
import me.roinujnosde.titansbattle.types.Warrior;
import me.roinujnosde.titansbattle.utils.Helper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class that manages the {@link Group}s required by group {@link Game}s.
 *
 * An instance of this class should be registered using {@link TitansBattle#setGroupManager(GroupManager)}.
 *
 * @see SimpleClansGroupManager
 */
public abstract class GroupManager {

    protected final Function<String, GroupData> idToData;
    protected final TitansBattle plugin;

    public GroupManager(@NotNull TitansBattle plugin) {
        this.plugin = plugin;
        this.idToData = plugin.getDatabaseManager()::getGroupData;
    }

    /**
     * Gets a set containing all {@link Group}s
     *
     * @return the Groups set
     */
    public abstract @NotNull Set<Group> getGroups();

    /**
     * Gets the {@link Group} of a {@link Player}
     *
     * @param uuid the UUID of the Player
     * @return a Group or null if the Player is not in one
     */
    public abstract @Nullable Group getGroup(@NotNull UUID uuid);

    /**
     * Checks whether two {@link Player}s are in the same {@link Group}
     * @param player1 UUID of a Player
     * @param player2 UUID of a Player
     * @return true if they belong to the same {@link Group}
     */
    public abstract boolean sameGroup(@NotNull UUID player1, @NotNull UUID player2);

    /**
     * Returns the members of the {@link Group}
     * @param group the Group
     * @return the members
     */
    public Set<Warrior> getWarriors(@NotNull final Group group) {
        DatabaseManager dm = plugin.getDatabaseManager();
        return Bukkit.getOnlinePlayers().stream().map(dm::getWarrior).filter(group::isMember).collect(Collectors.toSet());
    }

    /**
     * Builds a String from a Collection of {@link Group}s
     * Ex.: TnT, KoL & CDD
     *
     * @param groups the {@link Group}s
     * @return the built String
     */
    public @NotNull String buildStringFrom(@NotNull Collection<Group> groups) {
        return Helper.buildStringFrom(groups.stream().filter(Objects::nonNull).map(Group::getName)
                .collect(Collectors.toList()));
    }

}
