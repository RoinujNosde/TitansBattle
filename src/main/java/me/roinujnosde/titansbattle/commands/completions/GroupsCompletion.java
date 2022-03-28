package me.roinujnosde.titansbattle.commands.completions;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.InvalidCommandArgument;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.Group;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class GroupsCompletion extends AbstractCompletion {
    public GroupsCompletion(TitansBattle plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getId() {
        return "groups";
    }

    @Override
    public Collection<String> getCompletions(BukkitCommandCompletionContext context) throws InvalidCommandArgument {
        if (getGroupManager() == null)
            return Collections.emptyList();
        return getGroupManager().getGroups().stream().map(Group::getUniqueName).collect(Collectors.toList());
    }
}
