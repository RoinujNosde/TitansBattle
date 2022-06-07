package me.roinujnosde.titansbattle.commands.conditions;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.CommandConditions.ParameterCondition;
import me.roinujnosde.titansbattle.TitansBattle;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractParameterCondition<T> extends AbstractCondition
        implements ParameterCondition<T, BukkitCommandExecutionContext, BukkitCommandIssuer> {

    public AbstractParameterCondition(TitansBattle plugin) {
        super(plugin);
    }

    public abstract @NotNull Class<T> getType();
}
