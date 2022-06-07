package me.roinujnosde.titansbattle.commands.contexts;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.GameConfiguration;
import org.jetbrains.annotations.NotNull;

public class GameConfigurationContext extends AbstractContextResolver<GameConfiguration> {
    public GameConfigurationContext(@NotNull TitansBattle plugin) {
        super(plugin);
    }

    @Override
    public @NotNull Class<GameConfiguration> getType() {
        return GameConfiguration.class;
    }

    @Override
    public GameConfiguration getContext(BukkitCommandExecutionContext context) throws InvalidCommandArgument {
        return getConfigurationDao().getConfiguration(context.popFirstArg(), GameConfiguration.class).orElse(null);
    }
}
