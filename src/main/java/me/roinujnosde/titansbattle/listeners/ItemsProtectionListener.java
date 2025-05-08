package me.roinujnosde.titansbattle.listeners;

import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.Kit;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import static java.lang.String.format;

public class ItemsProtectionListener extends TBListener {

    public ItemsProtectionListener(@NotNull TitansBattle plugin) {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void on(InventoryCloseEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> clearItems(((Player) event.getPlayer())), 1L);
    }

    @EventHandler
    public void on(PlayerRespawnEvent event) {
        clearItems(event.getPlayer());
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        clearItems(event.getPlayer());
    }

    private void clearItems(@NotNull Player player) {
        // Player is in a game, it's possible (and normal) that they have a kit
        if (plugin.getBaseGameFrom(player) != null) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            if (Kit.isKitItem(item)) {
                plugin.debug(format("Removing kit item from %s's inventory", player.getName()));
                inventory.remove(item);
                item.setAmount(0); //needed for some Minecraft versions
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on(CraftItemEvent event) {
        CraftingInventory inventory = event.getInventory();
        ItemStack result = inventory.getResult();

        for (ItemStack item : inventory.getMatrix()) {
            if (item != null && Kit.isKitItem(item)) {
                Kit.applyNBTTag(result);
                return;
            }
        }
    }

}
