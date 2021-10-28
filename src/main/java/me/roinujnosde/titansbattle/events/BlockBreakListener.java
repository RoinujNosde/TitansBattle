package me.roinujnosde.titansbattle.listeners;

import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.games.Game;
import me.roinujnosde.titansbattle.managers.DatabaseManager;
import me.roinujnosde.titansbattle.managers.GameManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockBreakListener implements Listener {

    private final TitansBattle plugin = TitansBattle.getInstance();
    private final GameManager gm;
    private final DatabaseManager dm;

    public BlockBreakListener() {
        gm = plugin.getGameManager();
        dm = plugin.getDatabaseManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {
        Game game = gm.getCurrentGame().orElse(null);
        if (game == null) {
            return;
        }

        Player defender = event.getPlayer();
        if (!game.isParticipant(dm.getWarrior(defender))) {
            return;
        }

        if (!game.isInBattle(dm.getWarrior(defender))) {
            event.setCancelled(true);
        }
    }

}
