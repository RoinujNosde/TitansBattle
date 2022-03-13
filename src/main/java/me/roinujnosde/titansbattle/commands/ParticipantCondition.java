package me.roinujnosde.titansbattle.commands;

import co.aikar.commands.*;
import me.roinujnosde.titansbattle.BaseGame;
import me.roinujnosde.titansbattle.TitansBattle;
import org.bukkit.entity.Player;

public class ParticipantCondition implements CommandConditions.Condition<BukkitCommandIssuer> {

    private final TitansBattle plugin;

    public ParticipantCondition(TitansBattle plugin) {
        this.plugin = plugin;
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
