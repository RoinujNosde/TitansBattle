package me.roinujnosde.titansbattle.listeners;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.Kit;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
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
        Bukkit.getScheduler().runTaskLater(plugin, () -> process(((Player) event.getPlayer())), 1L);
    }
    
    @EventHandler
    public void on(PlayerRespawnEvent event) {
         process(event.getPlayer());
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        process(event.getPlayer());
    }

    private void process(@NotNull Player player) {
        // Player is in a game, it's possible (and normal) that they have a kit
        if (plugin.getBaseGameFrom(player) != null) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            if (new NBTItem(item).getBoolean(Kit.NBT_TAG)) {
                plugin.debug(format("Removing kit item from %s's inventory", player.getName()));
                inventory.remove(item);
            }
        }
    }

}
