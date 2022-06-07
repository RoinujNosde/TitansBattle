package me.roinujnosde.titansbattle.listeners;

import me.roinujnosde.titansbattle.BaseGame;
import me.roinujnosde.titansbattle.TitansBattle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

import static org.bukkit.event.player.PlayerTeleportEvent.*;

public class PlayerTeleportListener extends TBListener {

    public PlayerTeleportListener(@NotNull TitansBattle plugin) {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommandTeleport(PlayerTeleportEvent event) {
        if (event.getCause() != TeleportCause.COMMAND) {
            return;
        }
        final Player player = event.getPlayer();

        BaseGame game = plugin.getBaseGameFrom(player);
        if (game != null) {
            plugin.getLogger().log(Level.INFO, "Cancelled a teleport started via command for %s", player.getName());
            event.setCancelled(true);
        }
    }

}
