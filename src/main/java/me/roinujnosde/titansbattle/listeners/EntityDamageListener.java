package me.roinujnosde.titansbattle.listeners;

import me.roinujnosde.titansbattle.Helper;
import me.roinujnosde.titansbattle.TitansBattle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import me.roinujnosde.titansbattle.managers.ConfigManager;
import me.roinujnosde.titansbattle.managers.GameManager;
import me.roinujnosde.titansbattle.types.Game.Mode;

public class EntityDamageListener implements Listener {

    private final TitansBattle plugin;
    private final ConfigManager cm;
    private final GameManager gm;
    private final Helper helper;

    public EntityDamageListener() {
        plugin = TitansBattle.getInstance();
        cm = plugin.getConfigManager();
        gm = plugin.getGameManager();
        helper = plugin.getHelper();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player defendor = (Player) event.getEntity();
        if (gm.getParticipants().contains(defendor.getUniqueId())) {
            if (gm.isStarting() && gm.isHappening() || gm.isStarting()) {
                event.setCancelled(true);
            }
            if (event instanceof EntityDamageByEntityEvent && gm.isHappening()) {
                EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
                Player attacker = helper.getPlayerAttackerOrKiller(subEvent.getDamager());
                if (attacker == null || !gm.getParticipants().contains(attacker.getUniqueId())) {
                    return;
                }
                if (event.isCancelled()) {
                    if (helper.isFun(gm.getCurrentGame())) {
                        event.setCancelled(false);
                        return;
                    }
                    if (helper.areAllied(defendor, attacker)) {
                        event.setCancelled(false);
                    }
                }
            }
        }
    }
}
