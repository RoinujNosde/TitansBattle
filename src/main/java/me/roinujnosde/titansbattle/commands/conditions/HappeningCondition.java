package me.roinujnosde.titansbattle.commands.conditions;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import me.roinujnosde.titansbattle.TitansBattle;
import org.jetbrains.annotations.NotNull;

public class HappeningCondition extends AbstractCommandCondition {
    public HappeningCondition(TitansBattle plugin) {
        super(plugin);
    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> context) throws InvalidCommandArgument {
        if (!getGameManager().getCurrentGame().isPresent()) {
            context.getIssuer().sendMessage(plugin.getLang("not-starting-or-started"));
            throw new ConditionFailedException();
        }
    }

    @Override
    public @NotNull String getId() {
        return "happening";
    }
}
