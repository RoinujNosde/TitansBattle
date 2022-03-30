package me.roinujnosde.titansbattle.commands.conditions;

import co.aikar.commands.*;
import me.roinujnosde.titansbattle.BaseGame;
import me.roinujnosde.titansbattle.TitansBattle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerParticipantCondition extends AbstractParameterCondition<Player>{
    public PlayerParticipantCondition(TitansBattle plugin) {
        super(plugin);
    }

    @Override
    public @NotNull Class<Player> getType() {
        return Player.class;
    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> context,
                                  BukkitCommandExecutionContext execContext,
                                  Player value) throws InvalidCommandArgument {
        BaseGame game = plugin.getBaseGameFrom(value);
        if (game == null) {
            context.getIssuer().sendMessage(plugin.getLang("not_participating"));
            throw new ConditionFailedException();
        }
    }

    @Override
    public @NotNull String getId() {
        return "participant";
    }
}
