package me.roinujnosde.titansbattle.commands.conditions;

import co.aikar.commands.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import me.roinujnosde.titansbattle.TitansBattle;
import org.jetbrains.annotations.NotNull;

public class OtherPlayerCondition extends AbstractParameterCondition<OnlinePlayer> {
    public OtherPlayerCondition(TitansBattle plugin) {
        super(plugin);
    }

    @Override
    public @NotNull Class<OnlinePlayer> getType() {
        return OnlinePlayer.class;
    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> cc,
                                  BukkitCommandExecutionContext cec,
                                  OnlinePlayer v) throws InvalidCommandArgument {
        if (v.getPlayer().getUniqueId().equals(cc.getIssuer().getUniqueId())) {
            cec.getIssuer().sendMessage(plugin.getLang("you.cannot.challenge.yourself"));
            throw new ConditionFailedException();
        }
    }

    @Override
    public @NotNull String getId() {
        return "other";
    }
}
