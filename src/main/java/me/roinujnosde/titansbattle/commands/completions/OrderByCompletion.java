package me.roinujnosde.titansbattle.commands.completions;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.InvalidCommandArgument;
import me.roinujnosde.titansbattle.TitansBattle;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class OrderByCompletion extends AbstractAsyncCompletion {
    public OrderByCompletion(TitansBattle plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getId() {
        return "order_by";
    }

    @Override
    public Collection<String> getCompletions(BukkitCommandCompletionContext context) throws InvalidCommandArgument {
        String type = context.getConfig("type", null);
        if (type != null) {
            switch (type) {
                case "group":
                    return Arrays.asList("kills", "deaths", "defeats");
                case "warrior":
                    return Arrays.asList("kills", "deaths");
            }
        }
        return Collections.emptySet();
    }
}
