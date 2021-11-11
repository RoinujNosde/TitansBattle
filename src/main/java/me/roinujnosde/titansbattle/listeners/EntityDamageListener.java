package me.roinujnosde.titansbattle.listeners;

import me.roinujnosde.titansbattle.BaseGame;
import me.roinujnosde.titansbattle.BaseGameConfiguration;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.managers.DatabaseManager;
import me.roinujnosde.titansbattle.managers.GroupManager;
import me.roinujnosde.titansbattle.utils.Helper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

public class EntityDamageListener extends TBListener {

    public EntityDamageListener(@NotNull TitansBattle plugin) {
        super(plugin);
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {
        DatabaseManager dm = plugin.getDatabaseManager();

        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player defender = (Player) event.getEntity();
        BaseGame game = getBaseGameFrom(defender);
        if (game == null) {
            return;
        }

        if (!game.isInBattle(dm.getWarrior(defender))) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(false);
        if (event instanceof EntityDamageByEntityEvent) {
            processEntityDamageByEntityEvent(event, defender, game);
        }
    }

    private void processEntityDamageByEntityEvent(EntityDamageEvent event, Player defender, BaseGame game) {
        DatabaseManager dm = plugin.getDatabaseManager();

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

    private boolean isDamageTypeAllowed(EntityDamageByEntityEvent event, BaseGame game) {
        BaseGameConfiguration config = game.getConfig();
        if (event.getDamager() instanceof Projectile) {
            return config.isRangedDamage();
        } else {
            return config.isMeleeDamage();
        }
    }

    private boolean isParticipant(Entity entity) {
        if (entity instanceof Player) {
            return getBaseGameFrom((Player) entity) != null;
        }
        return false;
    }

}
