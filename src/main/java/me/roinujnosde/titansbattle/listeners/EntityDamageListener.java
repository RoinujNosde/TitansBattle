package me.roinujnosde.titansbattle.listeners;

import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.games.Game;
import me.roinujnosde.titansbattle.managers.DatabaseManager;
import me.roinujnosde.titansbattle.managers.GameManager;
import me.roinujnosde.titansbattle.managers.GroupManager;
import me.roinujnosde.titansbattle.types.GameConfiguration;
import me.roinujnosde.titansbattle.utils.Helper;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamageListener implements Listener {

    private final TitansBattle plugin = TitansBattle.getInstance();
    private final GameManager gm;
    private final DatabaseManager dm;

    public EntityDamageListener() {
        gm = plugin.getGameManager();
        dm = plugin.getDatabaseManager();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDamageLowest(EntityDamageEvent event) {
        boolean disableFfMessages = plugin.getConfig().getBoolean("disable-ff-messages", true);
        if (disableFfMessages && isParticipant(event.getEntity())) {
            // Cancelling so other plugins don't display messages such as "can't hit an ally" during the game
            event.setCancelled(true);
        }
    }

    //un-cancelling so mcMMO skills can be used
    //mcMMO's listener is on HIGHEST and ignoreCancelled = true, this will run before
    @EventHandler(priority = EventPriority.HIGH)
    public void onDamageHigh(EntityDamageEvent event) {
        if (isParticipant(event.getEntity())) {
            event.setCancelled(false);
        }
    }

    private boolean isParticipant(Entity entity) {
        Game game = gm.getCurrentGame().orElse(null);
        if (game == null || !(entity instanceof Player)) {
            return false;
        }
        return game.isParticipant(dm.getWarrior((OfflinePlayer) entity));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {
        if (!isParticipant(event.getEntity())) {
            return;
        }
        Player defender = (Player) event.getEntity();

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        Game game = gm.getCurrentGame().get();

        if (!game.isInBattle(dm.getWarrior(defender))) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(false);
        if (event instanceof EntityDamageByEntityEvent) {
            processEntityDamageByEntityEvent(event, defender, game);
        }
    }

    private void processEntityDamageByEntityEvent(EntityDamageEvent event, Player defender, Game game) {
        EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
        Player attacker = Helper.getPlayerAttackerOrKiller(subEvent.getDamager());
        if (!isDamageTypeAllowed(subEvent, game)) {
            event.setCancelled(true);
            return;
        }
        if (attacker != null && !game.getConfig().isPvP()) {
            event.setCancelled(true);
            return;
        }
        if (attacker == null || !game.isParticipant(dm.getWarrior(attacker))) {
            return;
        }

        if (!game.getConfig().isGroupMode()) {
            return;
        }

        GroupManager groupManager = TitansBattle.getInstance().getGroupManager();
        if (groupManager != null) {
            event.setCancelled(groupManager.sameGroup(defender.getUniqueId(), attacker.getUniqueId()));
        }
    }

    private boolean isDamageTypeAllowed(EntityDamageByEntityEvent event, Game game) {
        GameConfiguration config = game.getConfig();
        if (event.getDamager() instanceof Projectile) {
            return config.isRangedDamage();
        } else {
            return config.isMeleeDamage();
        }
    }

}
