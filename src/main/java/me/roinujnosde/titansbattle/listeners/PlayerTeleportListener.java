package me.roinujnosde.titansbattle.listeners;

import me.roinujnosde.titansbattle.TitansBattle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.logging.Level;

import static org.bukkit.event.player.PlayerTeleportEvent.*;

public class PlayerTeleportListener implements Listener {

    private final TitansBattle plugin = TitansBattle.getInstance();


    @EventHandler(ignoreCancelled = true)
    public void onCommandTeleport(PlayerTeleportEvent event) {
        if (event.getCause() != TeleportCause.COMMAND) {
            return;
        }
        final Player player = event.getPlayer();

        plugin.getGameManager().getCurrentGame().ifPresent(game -> {
            if (!game.isParticipant(plugin.getDatabaseManager().getWarrior(player))) {
                return;
            }
            plugin.getLogger().log(Level.INFO, "Cancelled a teleport started via command for %s", player.getName());
            event.setCancelled(true);

        });
    }

}
