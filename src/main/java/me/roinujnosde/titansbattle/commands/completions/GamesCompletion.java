package me.roinujnosde.titansbattle.commands.completions;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.InvalidCommandArgument;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.GameConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;

public class GamesCompletion extends AbstractCompletion {
    public GamesCompletion(TitansBattle plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getId() {
        return "games";
    }

    @Override
    public Collection<String> getCompletions(BukkitCommandCompletionContext context) throws InvalidCommandArgument {
        return getConfigurationDao().getConfigurations(GameConfiguration.class).stream()
                .map(GameConfiguration::getName).collect(Collectors.toList());
    }
}
