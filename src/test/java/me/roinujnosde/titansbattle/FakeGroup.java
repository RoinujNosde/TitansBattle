package me.roinujnosde.titansbattle;

import me.roinujnosde.titansbattle.types.Group;
import me.roinujnosde.titansbattle.types.GroupData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class FakeGroup extends Group {

    private static int lastId = 0;
    private final int id;
    private final List<UUID> members = new ArrayList<>();

    public FakeGroup(@NotNull GroupData data) {
        super(data);
        id = ++lastId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        FakeGroup fakeGroup = (FakeGroup) object;
        return getId() == fakeGroup.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getId());
    }

    @Override
    public @NotNull String getName() {
        return Integer.toString(id);
    }

    @Override
    public @NotNull String getId() {
        return Integer.toString(id);
    }

    @Override
    public void disband() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isMember(@NotNull UUID uuid) {
        return members.contains(uuid);
    }

    public void addMember(@NotNull UUID uuid) {
        members.add(uuid);
    }

    @Override
    public boolean isLeaderOrOfficer(@NotNull UUID uuid) {
        return members.contains(uuid) && members.get(0) == uuid;
    }
}
