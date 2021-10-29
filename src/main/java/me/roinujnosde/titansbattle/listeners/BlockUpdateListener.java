package me.roinujnosde.titansbattle.listeners;

import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.games.Game;
import me.roinujnosde.titansbattle.managers.DatabaseManager;
import me.roinujnosde.titansbattle.managers.GameManager;
import me.roinujnosde.titansbattle.types.Warrior;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockUpdateListener implements Listener {

    private final TitansBattle plugin = TitansBattle.getInstance();
    private final GameManager gm;
    private final DatabaseManager dm;

    public BlockUpdateListener() {
        gm = plugin.getGameManager();
        dm = plugin.getDatabaseManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {
        if (shouldCancel(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlace(BlockPlaceEvent event) {
        if (shouldCancel(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    private boolean shouldCancel(Player player) {
        Game game = gm.getCurrentGame().orElse(null);
        if (game == null) {
            return false;
        }

        Warrior warrior = dm.getWarrior(player);
        if (!game.isParticipant(warrior)) {
            return false;
        }

        return !game.isInBattle(warrior);
    }

}
