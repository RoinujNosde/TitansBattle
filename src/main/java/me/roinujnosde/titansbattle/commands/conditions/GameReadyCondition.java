package me.roinujnosde.titansbattle.commands.conditions;

import co.aikar.commands.*;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.GameConfiguration;
import org.jetbrains.annotations.NotNull;

public class GameReadyCondition extends AbstractParameterCondition<GameConfiguration> {

    public GameReadyCondition(TitansBattle plugin) {
        super(plugin);
    }

    @Override
    public @NotNull Class<GameConfiguration> getType() {
        return GameConfiguration.class;
    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> cc,
                                  BukkitCommandExecutionContext cec,
                                  GameConfiguration value) throws InvalidCommandArgument {
        if (!value.locationsSet()) {
            cc.getIssuer().sendMessage(plugin.getLang("this.game.isnt.ready"));
            throw new ConditionFailedException();
        }
    }

    @Override
    public @NotNull String getId() {
        return "ready";
    }
}