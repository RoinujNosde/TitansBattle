package me.roinujnosde.titansbattle.games;

import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.managers.GroupManager;
import me.roinujnosde.titansbattle.types.Group;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FakeGroupManager extends GroupManager {

    private final Set<Group> groups = new HashSet<>();

    public FakeGroupManager(@NotNull TitansBattle plugin) {
        super(plugin);
    }

    @Override
    public @NotNull Set<Group> getGroups() {
        return groups;
    }

    public void addGroup(@NotNull Group group) {
        groups.add(group);
    }

    @Override
    public @Nullable Group getGroup(@NotNull UUID uuid) {
        for (Group group : groups) {
            if (group.isMember(uuid)) {
                return group;
            }
        }
        return null;
    }

    @Override
    public boolean sameGroup(@NotNull UUID player1, @NotNull UUID player2) {
        Group firstGroup = getGroup(player1);
        if (firstGroup == null) {
            return false;
        }
        return firstGroup.equals(getGroup(player2));
    }
}
