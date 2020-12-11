package me.roinujnosde.titansbattle.types;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class FactionsGroup extends Group {

    private final Faction faction;

    public FactionsGroup(@NotNull Faction faction, @NotNull GroupData data) {
        super(data);
        this.faction = faction;
    }

    @Override
    public @NotNull String getName() {
        return faction.getName();
    }

    @Override
    public @NotNull String getId() {
        return faction.getId();
    }

    @Override
    public void disband() {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "f disband " + faction.getName());
    }

    @Override
    public boolean isMember(@NotNull UUID uuid) {
        return MPlayer.get(uuid).getFaction().equals(faction);
    }

    @Override
    public boolean isLeaderOrOfficer(@NotNull UUID uuid) {
        if (!isMember(uuid)) {
            return false;
        }
        return MPlayer.get(uuid).getRole() == Rel.LEADER || MPlayer.get(uuid).getRole() == Rel.OFFICER;
    }
}
