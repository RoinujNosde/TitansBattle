package me.roinujnosde.titansbattle.listeners;

import me.roinujnosde.titansbattle.BaseGame;
import me.roinujnosde.titansbattle.BaseGameConfiguration;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.events.PlayerJoinGameEvent;
import me.roinujnosde.titansbattle.types.Group;
import me.roinujnosde.titansbattle.types.Kit;
import me.roinujnosde.titansbattle.types.Warrior;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class JoinGameListener extends TBListener {

    public JoinGameListener(@NotNull TitansBattle plugin) {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void gameHappening(PlayerJoinGameEvent event) {
        BaseGame game = event.getGame();
        if (!game.isLobby()) {
            cancelWithMessage(event, "game_is_happening");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void alreadyJoined(PlayerJoinGameEvent event) {
        BaseGame game = event.getGame();
        if (game.isParticipant(event.getWarrior())) {
            cancelWithMessage(event, "already-joined");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void maximumPlayers(PlayerJoinGameEvent event) {
        BaseGame game = event.getGame();
        BaseGameConfiguration config = game.getConfig();
        if (game.getParticipants().size() >= config.getMaximumPlayers() && config.getMaximumPlayers() > 0) {
            cancelWithMessage(event, "maximum-players");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void maximumGroups(PlayerJoinGameEvent event) {
        BaseGame game = event.getGame();
        BaseGameConfiguration config = game.getConfig();
        Warrior warrior = event.getWarrior();

        if (!config.isGroupMode()) {
            return;
        }
        Map<Group, Integer> groups = game.getGroupParticipants();
        if (!groups.containsKey(warrior.getGroup()) && groups.size() >= config.getMaximumGroups()
                && config.getMaximumGroups() > 0) {
            cancelWithMessage(event, "maximum-groups");
            return;
        }
        int players = groups.getOrDefault(warrior.getGroup(), 0);
        if (players >= config.getMaximumPlayersPerGroup() && config.getMaximumPlayersPerGroup() > 0) {
            cancelWithMessage(event, "maximum-players-per-group");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void groupMode(PlayerJoinGameEvent event) {
        if (event.getGame().getConfig().isGroupMode() && event.getWarrior().getGroup() == null) {
            cancelWithMessage(event, "not_in_a_group");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void inventoryCheck(PlayerJoinGameEvent event) {
        if (event.getGame().getConfig().isUseKits() && Kit.inventoryHasItems(event.getPlayer())) {
            cancelWithMessage(event, "clear-your-inventory");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void whitelist(PlayerJoinGameEvent event) {
        Player player = event.getPlayer();
        List<String> whitelist = event.getGame().getConfig().getWhitelistedItems();
        if (whitelist == null || whitelist.isEmpty()) {
            return;
        }
        items:
        for (ItemStack item : player.getInventory().getContents()) {
            for (String allowedItem : whitelist) {
                if (allowedItem.equals(item.getType().name())) {
                    continue items;
                }

                cancelWithMessage(event, "item_not_allowed", item.getType());
                break items;
            }
        }
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void offhandWhitelist(PlayerJoinGameEvent event) {
        try {
            Player player = event.getPlayer();
            List<String> whitelist = event.getGame().getConfig().getWhitelistedItems();
            if (whitelist == null) {
                return;
            }

            ItemStack item = player.getInventory().getItemInOffHand();
            if (item.getType() == Material.AIR || whitelist.contains(item.getType().name())) {
                return;
            }
            cancelWithMessage(event, "item_not_allowed", item.getType());
        } catch (NoSuchMethodError ignored) {
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void blacklist(PlayerJoinGameEvent event) {
        Player player = event.getPlayer();
        List<String> blacklist = event.getGame().getConfig().getBlacklistedItems();
        if (blacklist == null || blacklist.isEmpty()) {
            return;
        }
        for (ItemStack item : player.getInventory().getContents()) {
            for (String blockedItem : blacklist) {
                if (blockedItem.equals(item.getType().name())) {
                    cancelWithMessage(event, "item_not_allowed", item.getType());
                    break;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void offhandBlacklist(PlayerJoinGameEvent event) {
        try {
            Player player = event.getPlayer();
            List<String> blacklist = event.getGame().getConfig().getBlacklistedItems();
            if (blacklist == null) {
                return;
            }
            ItemStack item = player.getInventory().getItemInOffHand();
            if (item.getType() == Material.AIR || !blacklist.contains(item.getType().name())) {
                return;
            }
            cancelWithMessage(event, "item_not_allowed", item.getType());
        } catch (NoSuchMethodError ignored) {
        }
    }

    private void cancelWithMessage(PlayerJoinGameEvent event, String key, Object... args) {
        Player player = event.getPlayer();
        String message = event.getGame().getLang(key, args);

        player.sendMessage(message);
        event.setCancelled(true);
        plugin.debug(String.format("Blocked player %s join, message %s", player.getName(), message));
    }
}
