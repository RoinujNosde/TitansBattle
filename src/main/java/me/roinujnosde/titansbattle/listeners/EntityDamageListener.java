package me.roinujnosde.titansbattle.listeners;

import me.roinujnosde.titansbattle.Helper;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.managers.GameManager;
import me.roinujnosde.titansbattle.types.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamageListener implements Listener {

    private final GameManager gm;
    private final Helper helper;

    public EntityDamageListener() {
        TitansBattle plugin = TitansBattle.getInstance();
        gm = plugin.getGameManager();
        helper = plugin.getHelper();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Game currentGame = gm.getCurrentGame();
        if (currentGame == null) {
            return;
        }

        Player defender = (Player) event.getEntity();
        if (gm.getParticipants().contains(defender.getUniqueId())) {
            if (!gm.isBattle()) {
                event.setCancelled(true);
                return;
            }
            if (event instanceof EntityDamageByEntityEvent && gm.isBattle()) {
                EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
                Player attacker = helper.getPlayerAttackerOrKiller(subEvent.getDamager());
                if (attacker == null || !gm.getParticipants().contains(attacker.getUniqueId())) {
                    return;
                }
                Game.Mode mode = currentGame.getMode();
                if (mode.equals(Game.Mode.FREEFORALL_FUN) || mode.equals(Game.Mode.FREEFORALL_REAL)) {
                    event.setCancelled(false);
                    return;
                }
                boolean sameGroup = helper.areInSameGroup(defender.getUniqueId(), attacker.getUniqueId());
                event.setCancelled(sameGroup);
            }
        }
    }
}
