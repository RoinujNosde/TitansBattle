package me.roinujnosde.titansbattle.commands.contexts;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.contexts.IssuerOnlyContextResolver;
import me.roinujnosde.titansbattle.TitansBattle;

public abstract class AbstractIssuerOnlyContextResolver<T>
        extends AbstractContextResolver<T> implements IssuerOnlyContextResolver<T, BukkitCommandExecutionContext> {

    public AbstractIssuerOnlyContextResolver(TitansBattle plugin) {
        super(plugin);
    }
}
