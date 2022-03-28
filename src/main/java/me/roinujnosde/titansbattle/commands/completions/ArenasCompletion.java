package me.roinujnosde.titansbattle.commands.completions;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.InvalidCommandArgument;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.challenges.ArenaConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ArenasCompletion extends AbstractCompletion {
    public ArenasCompletion(TitansBattle plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getId() {
        return "arenas";
    }

    @Override
    public Collection<String> getCompletions(BukkitCommandCompletionContext context) throws InvalidCommandArgument {
        List<String> inUse = getChallengeManager().getRequests().stream()
                .map(cr -> cr.getChallenge().getConfig().getName()).collect(Collectors.toList());
        if (context.hasConfig("in_use")) {
            return inUse;
        }

        final boolean group = Boolean.parseBoolean(context.getConfig("group"));
        List<String> arenas = getConfigurationDao().getConfigurations(ArenaConfiguration.class).stream()
                .filter(a -> a.isGroupMode() == group).map(ArenaConfiguration::getName)
                .collect(Collectors.toList());

        arenas.removeAll(inUse);
        return arenas;
    }
}
