package me.roinujnosde.titansbattle.commands;

import co.aikar.commands.*;
import co.aikar.commands.CommandConditions.ParameterCondition;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.challenges.ArenaConfiguration;
import me.roinujnosde.titansbattle.types.Kit;
import org.bukkit.entity.Player;

public class EmptyInventoryCondition implements
        ParameterCondition<ArenaConfiguration, BukkitCommandExecutionContext, BukkitCommandIssuer> {

    private final TitansBattle plugin;

    public EmptyInventoryCondition(TitansBattle plugin) {
        this.plugin = plugin;
    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> context,
                                  BukkitCommandExecutionContext execContext,
                                  ArenaConfiguration value) throws InvalidCommandArgument {
        Player player = execContext.getPlayer();
        if (value.isUseKits() && Kit.inventoryHasItems(player)) {
            player.sendMessage(plugin.getLang("clear-your-inventory", value));
            throw new ConditionFailedException();
        }
    }
}
