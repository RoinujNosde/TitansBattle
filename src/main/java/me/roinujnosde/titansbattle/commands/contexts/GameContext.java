package me.roinujnosde.titansbattle.commands.contexts;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.games.Game;
import org.jetbrains.annotations.NotNull;

public class GameContext extends AbstractIssuerOnlyContextResolver<Game> {

    public GameContext(TitansBattle plugin) {
        super(plugin);
    }

    @Override
    public @NotNull Class<Game> getType() {
        return Game.class;
    }

    @Override
    public Game getContext(BukkitCommandExecutionContext context) throws InvalidCommandArgument {
        return getGameManager().getCurrentGame().orElse(null);
    }
}
