package me.roinujnosde.titansbattle.listeners;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.Kit;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CraftingListener extends TBListener {

    public CraftingListener(@NotNull TitansBattle plugin) {
        super(plugin);
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        CraftingInventory inventory = event.getInventory();
        ItemStack result = inventory.getResult();

        for (ItemStack item : inventory.getMatrix()) {
            if (item != null && hasKitTag(item)) {
                applyNBTTag(result);
                return;
            }
        }
    }

    private boolean hasKitTag(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        return new NBTItem(item).getBoolean(Kit.NBT_TAG);
    }

    private void applyNBTTag(ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            NBTItem nbtItem = new NBTItem(item);
            nbtItem.setBoolean(Kit.NBT_TAG, true);
            nbtItem.applyNBT(item);
        }
    }
}
