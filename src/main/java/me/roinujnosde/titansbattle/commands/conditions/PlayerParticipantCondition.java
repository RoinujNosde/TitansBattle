package me.roinujnosde.titansbattle.commands.conditions;

import co.aikar.commands.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import me.roinujnosde.titansbattle.BaseGame;
import me.roinujnosde.titansbattle.TitansBattle;
import org.jetbrains.annotations.NotNull;

public class PlayerParticipantCondition extends AbstractParameterCondition<OnlinePlayer>{
    public PlayerParticipantCondition(TitansBattle plugin) {
        super(plugin);
    }

    @Override
    public @NotNull Class<OnlinePlayer> getType() {
        return OnlinePlayer.class;
    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> context,
                                  BukkitCommandExecutionContext execContext,
                                  OnlinePlayer value) throws InvalidCommandArgument {
        BaseGame game = plugin.getBaseGameFrom(value.player);
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
