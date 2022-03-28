package me.roinujnosde.titansbattle.commands.completions;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions.AsyncCommandCompletionHandler;
import me.roinujnosde.titansbattle.TitansBattle;

public abstract class AbstractAsyncCompletion extends AbstractCompletion
        implements AsyncCommandCompletionHandler<BukkitCommandCompletionContext> {

    public AbstractAsyncCompletion(TitansBattle plugin) {
        super(plugin);
    }
}
