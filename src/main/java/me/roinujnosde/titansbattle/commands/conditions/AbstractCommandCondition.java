package me.roinujnosde.titansbattle.commands.conditions;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.CommandConditions.Condition;
import me.roinujnosde.titansbattle.TitansBattle;

public abstract class AbstractCommandCondition extends AbstractCondition implements Condition<BukkitCommandIssuer>  {

    public AbstractCommandCondition(TitansBattle plugin) {
        super(plugin);
    }

}
