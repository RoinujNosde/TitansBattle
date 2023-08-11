package me.roinujnosde.titansbattle.commands.contexts;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.MinecraftMessageKeys;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import me.roinujnosde.titansbattle.TitansBattle;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class OnlinePlayerContext extends AbstractContextResolver<OnlinePlayer>  {

    public OnlinePlayerContext(@NotNull TitansBattle plugin) {
        super(plugin);
    }

    @Override
    public @NotNull Class<OnlinePlayer> getType() {
        return OnlinePlayer.class;
    }

    @Override
    public OnlinePlayer getContext(BukkitCommandExecutionContext cec) throws InvalidCommandArgument {
        String name = cec.popFirstArg();
        Player player = Bukkit.getPlayerExact(name);
        if (player == null) {
            throw new InvalidCommandArgument(MinecraftMessageKeys.NO_PLAYER_FOUND_SERVER, "{search}", name);
        }
        return new OnlinePlayer(player);
    }
}
