package me.roinujnosde.titansbattle.types;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.TreeMap;

@SerializableAs("kit")
public class Kit implements ConfigurationSerializable {

    private final ItemStack[] contents;

    public Kit(@NotNull PlayerInventory inventory) {
        contents = inventory.getContents().clone();
    }

    public Kit(@NotNull Map<String, Object> data) {
        contents = new ItemStack[41];
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getKey().equals("==")) {
                continue;
            }
            int index = Integer.parseInt(entry.getKey());
            if (index >= 41) {
                continue;
            }
            contents[index] = ((ItemStack) entry.getValue());
        }
    }

    @Override
    public Map<String, Object> serialize() {
        TreeMap<String, Object> data = new TreeMap<>();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null) {
                data.put(String.valueOf(i), item);
            }
        }
        return data;
    }

    public ItemStack[] getContents() {
        return contents.clone();
    }

    public void set(@NotNull Player player) {
        player.getInventory().setContents(contents);
    }

    public static boolean inventoryHasItems(@NotNull Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        for (ItemStack item : contents) {
            if (item == null) {
                continue;
            }
            if (item.getType() != Material.AIR) {
                return true;
            }
        }
        return false;
    }

    public static void clearInventory(@NotNull Warrior warrior) {
        Player player = warrior.toOnlinePlayer();
        if (player != null) {
            clearInventory(player);
        }
    }

    public static void clearInventory(@Nullable Player player) {
        if (player == null) return;

        player.getInventory().clear();
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
    }
}
