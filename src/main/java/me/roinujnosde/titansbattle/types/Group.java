package me.roinujnosde.titansbattle.types;

import me.roinujnosde.titansbattle.games.Game;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a Group in group {@link Game}s
 *
 * @see FactionsGroup
 * @see SimpleClansGroup
 */
public abstract class Group {

    private final GroupData data;

    public Group(@NotNull GroupData data) {
        this.data = data;
    }

    /**
     *
     * @return the {@link Group}'s name
     */
    public abstract @NotNull String getName();

    /**
     *
     * @return the {@link Group}'s ID (unique)
     */
    public abstract @NotNull String getId();

    /**
     * Deletes this Group
     */
    public abstract void disband();

    /**
     * Checks whether a {@link Player} belongs in this {@link Group}
     * @param uuid the {@link Player}'s UUID
     * @return true if the {@link Player} is a member
     */
    public abstract boolean isMember(@NotNull UUID uuid);

    /**
     * Checks whether a {@link Warrior} belongs in this {@link Group}
     * @param warrior the Warrior
     * @return true if member
     */
    public boolean isMember(@NotNull Warrior warrior) {
        return isMember(warrior.getUniqueId());
    }

    /**
     * Checks whether a {@link Player} is a Leader, Officer or equivalent in this {@link Group}
     * @param uuid the {@link Player}'s UUID
     * @return true if the {@link Player} is a Leader
     */
    public abstract boolean isLeaderOrOfficer(@NotNull UUID uuid);

    /**
     * Gets this {@link Group}'s data
     * @return the GroupData
     */
    public GroupData getData() {
        return data;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Group) {
            return getId().equals(((Group) other).getId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
