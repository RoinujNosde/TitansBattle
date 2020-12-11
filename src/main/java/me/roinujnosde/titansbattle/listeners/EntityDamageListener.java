package me.roinujnosde.titansbattle.listeners;

import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.managers.GameManager;
import me.roinujnosde.titansbattle.managers.GroupManager;
import me.roinujnosde.titansbattle.types.Game;
import me.roinujnosde.titansbattle.utils.Helper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Objects;

public class EntityDamageListener implements Listener {

    private final GroupManager groupManager;
    private final GameManager gm;

    public EntityDamageListener() {
        TitansBattle plugin = TitansBattle.getInstance();
        gm = plugin.getGameManager();
        groupManager = plugin.getGroupManager();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDamageLowest(EntityDamageEvent event) {
        if (isParticipant(event.getEntity())) {
            // Cancelling so other plugins don't display messages such as "can't hit an ally" during the game
            event.setCancelled(true);
        }
    }

    private boolean isParticipant(Entity entity) {
        if (!(entity instanceof Player)) {
            return false;
        }

        if (gm.getCurrentGame() == null) {
            return false;
        }

        return gm.getCurrentGame().getPlayerParticipants().contains(entity.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {
        if (!isParticipant(event.getEntity())) {
            return;
        }
        Player defender = (Player) event.getEntity();

        Game game = Objects.requireNonNull(gm.getCurrentGame());

        if (!game.isBattle()) {
            event.setCancelled(true);
            return;
        }
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
            Player attacker = Helper.getPlayerAttackerOrKiller(subEvent.getDamager());
            if (attacker == null || !game.getPlayerParticipants().contains(attacker.getUniqueId())) {
                return;
            }
            if (!game.getConfig().isGroupMode()) {
                event.setCancelled(false);
                return;
            }

            if (groupManager != null) {
                event.setCancelled(groupManager.sameGroup(defender.getUniqueId(), attacker.getUniqueId()));
            }
        }
    }

}
