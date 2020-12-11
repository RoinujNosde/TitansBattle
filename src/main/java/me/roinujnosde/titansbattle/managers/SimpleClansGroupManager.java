package me.roinujnosde.titansbattle.managers;

import me.roinujnosde.titansbattle.types.Group;
import me.roinujnosde.titansbattle.types.SimpleClansGroup;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.utils.Helper;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class SimpleClansGroupManager extends GroupManager {

    private final SimpleClans simpleClans;

    public SimpleClansGroupManager(@NotNull TitansBattle plugin) {
        super(plugin);
        simpleClans = (SimpleClans) Bukkit.getPluginManager().getPlugin("SimpleClans");
    }

    @Override
    public @NotNull Set<Group> getGroups() {
        return simpleClans.getClanManager().getClans().stream()
                .map(clan -> new SimpleClansGroup(clan, idToData.apply(clan.getTag()))).collect(Collectors.toSet());
    }

    @Override
    public @Nullable Group getGroup(@NotNull UUID uuid) {
        Clan clan = simpleClans.getClanManager().getClanByPlayerUniqueId(uuid);
        if (clan != null) {
            return new SimpleClansGroup(clan, idToData.apply(clan.getTag()));
        }
        return null;
    }

    @Override
    public boolean sameGroup(@NotNull UUID player1, @NotNull UUID player2) {
        Clan clan1 = simpleClans.getClanManager().getClanByPlayerUniqueId(player1);
        if (clan1 != null) {
            return clan1.isMember(player2);
        }
        return false;
    }

    @Override
    public @NotNull String buildStringFrom(@NotNull Collection<Group> groups) {
        return Helper.buildStringFrom(groups.stream().map(Group::getId).collect(Collectors.toList()));
    }
}
