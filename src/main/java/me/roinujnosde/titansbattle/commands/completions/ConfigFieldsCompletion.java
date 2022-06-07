package me.roinujnosde.titansbattle.commands.completions;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.InvalidCommandArgument;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.challenges.ArenaConfiguration;
import me.roinujnosde.titansbattle.types.GameConfiguration;
import me.roinujnosde.titansbattle.types.Prizes;
import me.roinujnosde.titansbattle.serialization.ConfigUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public class ConfigFieldsCompletion extends AbstractAsyncCompletion {
    public ConfigFieldsCompletion(TitansBattle plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getId() {
        return "config_fields";
    }

    @Override
    public Collection<String> getCompletions(BukkitCommandCompletionContext context) throws InvalidCommandArgument {
        String clazz = context.getConfig("class", null);
        if (clazz != null) {
            switch (clazz) {
                case "prizes":
                    return ConfigUtils.getEditableFields(Prizes.class);
                case "game":
                    return ConfigUtils.getEditableFields(GameConfiguration.class);
                case "arena":
                    return ConfigUtils.getEditableFields(ArenaConfiguration.class);
            }
        }
        return Collections.emptySet();
    }
}
