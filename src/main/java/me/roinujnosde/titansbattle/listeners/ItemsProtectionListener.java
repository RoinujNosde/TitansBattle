package me.roinujnosde.titansbattle.listeners;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.Kit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class ItemsProtectionListener extends TBListener {

    public ItemsProtectionListener(@NotNull TitansBattle plugin) {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void on(InventoryOpenEvent event) {
        // Not recommended modifying inventories in Inventory events
        Bukkit.getScheduler().runTaskLater(plugin, () -> process(event), 1L);
    }

    private void process(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        // Player is in a game, it's possible (and normal) that they have a kit
        if (plugin.getBaseGameFrom(player) != null) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        for (ItemStack item : inventory.getContents()) {
            if (item == null) {
                continue;
            }
            if (new NBTItem(item).getBoolean(Kit.NBT_TAG)) {
                plugin.getLogger().log(Level.INFO, "Removed kit item from %s's inventory", player.getName());
                inventory.remove(item);
            }
        }
    }

}
