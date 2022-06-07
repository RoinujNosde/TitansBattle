package me.roinujnosde.titansbattle.commands.conditions;

import co.aikar.commands.*;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.challenges.ArenaConfiguration;
import me.roinujnosde.titansbattle.commands.conditions.AbstractParameterCondition;
import me.roinujnosde.titansbattle.types.Kit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EmptyInventoryCondition extends AbstractParameterCondition<ArenaConfiguration> {

    public EmptyInventoryCondition(TitansBattle plugin) {
        super(plugin);
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

    @Override
    public @NotNull String getId() {
        return "empty_inventory";
    }

    @Override
    public @NotNull Class<ArenaConfiguration> getType() {
        return ArenaConfiguration.class;
    }
}
