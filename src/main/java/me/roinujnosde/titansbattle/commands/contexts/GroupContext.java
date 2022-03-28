package me.roinujnosde.titansbattle.commands.contexts;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.Group;
import org.jetbrains.annotations.NotNull;

public class GroupContext extends AbstractContextResolver<Group> {

    public GroupContext(@NotNull TitansBattle plugin) {
        super(plugin);
    }

    @Override
    public @NotNull Class<Group> getType() {
        return Group.class;
    }

    @Override
    public Group getContext(BukkitCommandExecutionContext context) throws InvalidCommandArgument {
        String arg = context.popFirstArg();
        if (getGroupManager().isPresent()) {
            for (Group group : getGroupManager().get().getGroups()) {
                if (arg.equalsIgnoreCase(group.getUniqueName())) {
                    return group;
                }
            }
        }
        throw new InvalidCommandArgument(plugin.getLang("group.not.found"));
    }
}
