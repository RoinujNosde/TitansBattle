package me.roinujnosde.titansbattle.commands.conditions;

import co.aikar.commands.*;
import me.roinujnosde.titansbattle.BaseGame;
import me.roinujnosde.titansbattle.TitansBattle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ParticipantCondition extends AbstractCommandCondition {

    public ParticipantCondition(TitansBattle plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getId() {
        return "participant";
    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> context) throws InvalidCommandArgument {
        Player player = context.getIssuer().getPlayer();
        BaseGame game = plugin.getBaseGameFrom(player);
        if (game == null) {
            player.sendMessage(plugin.getLang("not_participating"));
            throw new ConditionFailedException();
        }

    }
}
