package me.roinujnosde.titansbattle.commands.contexts;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.challenges.ArenaConfiguration;
import org.jetbrains.annotations.NotNull;

public class ArenaConfigurationContext extends AbstractContextResolver<ArenaConfiguration> {
    public ArenaConfigurationContext(@NotNull TitansBattle plugin) {
        super(plugin);
    }

    @Override
    public @NotNull Class<ArenaConfiguration> getType() {
        return ArenaConfiguration.class;
    }

    @Override
    public ArenaConfiguration getContext(BukkitCommandExecutionContext context) throws InvalidCommandArgument {
        return getConfigurationDao().getConfiguration(context.popFirstArg(), ArenaConfiguration.class).orElse(null);
    }
}
