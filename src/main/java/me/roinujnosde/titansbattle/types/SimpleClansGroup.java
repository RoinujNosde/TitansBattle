package me.roinujnosde.titansbattle.types;

import net.sacredlabyrinth.phaed.simpleclans.Clan;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SimpleClansGroup extends Group {

    private final Clan clan;

    public SimpleClansGroup(@NotNull Clan clan, @NotNull GroupData data) {
        super(data);
        this.clan = clan;
    }

    @Override
    public @NotNull String getName() {
        return clan.getName();
    }

    @Override
    public @NotNull String getUniqueName() {
        return clan.getTag();
    }

    @Override
    public @NotNull String getId() {
        return clan.getTag();
    }

    @Override
    public void disband() {
        clan.disband();
    }

    @Override
    public boolean isMember(@NotNull UUID uuid) {
        return clan.isMember(uuid);
    }

    @Override
    public boolean isLeaderOrOfficer(@NotNull UUID uuid) {
        return clan.isLeader(uuid);
    }
}
