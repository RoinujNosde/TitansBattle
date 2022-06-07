package me.roinujnosde.titansbattle.commands.contexts;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.MessageKeys;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.Warrior;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WarriorContext extends AbstractIssuerOnlyContextResolver<Warrior> {

    public WarriorContext(TitansBattle plugin) {
        super(plugin);
    }

    @Override
    public @NotNull Class<Warrior> getType() {
        return Warrior.class;
    }

    @Override
    public Warrior getContext(BukkitCommandExecutionContext context) throws InvalidCommandArgument {
        Player player = context.getPlayer();
        if (player == null) {
            throw new InvalidCommandArgument(MessageKeys.NOT_ALLOWED_ON_CONSOLE);
        }
        return getDatabaseManager().getWarrior(player);
    }
}
