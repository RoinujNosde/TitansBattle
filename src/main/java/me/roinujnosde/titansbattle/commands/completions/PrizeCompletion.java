package me.roinujnosde.titansbattle.commands.completions;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.InvalidCommandArgument;
import me.roinujnosde.titansbattle.BaseGameConfiguration.Prize;
import me.roinujnosde.titansbattle.TitansBattle;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class PrizeCompletion extends AbstractAsyncCompletion {
    public PrizeCompletion(TitansBattle plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getId() {
        return "prizes";
    }

    @Override
    public Collection<String> getCompletions(BukkitCommandCompletionContext context) throws InvalidCommandArgument {
        List<String> completion = new ArrayList<>();
        for (Prize prize : Prize.values()) {
            completion.add(prize.name());
        }

        return completion;
    }

}
