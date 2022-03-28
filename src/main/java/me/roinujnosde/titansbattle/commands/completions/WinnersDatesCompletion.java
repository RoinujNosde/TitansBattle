package me.roinujnosde.titansbattle.commands.completions;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.InvalidCommandArgument;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.Winners;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.stream.Collectors;

public class WinnersDatesCompletion extends AbstractCompletion {
    public WinnersDatesCompletion(TitansBattle plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getId() {
        return "winners_dates";
    }

    @Override
    public Collection<String> getCompletions(BukkitCommandCompletionContext context) throws InvalidCommandArgument {
        SimpleDateFormat dateFormat = new SimpleDateFormat(getConfigManager().getDateFormat());

        return getDatabaseManager().getWinners().stream().map(Winners::getDate).map(dateFormat::format)
                .collect(Collectors.toList());
    }
}
