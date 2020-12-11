package me.roinujnosde.titansbattle.types;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

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
        HashMap<String, Object> data = new HashMap<>();
        for (int i = 0; i < contents.length; i++) {
            data.put(String.valueOf(i), contents[i]);
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
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            System.out.println("i = " + i);
            if (item == null) {
                System.out.println("item é null");
                continue;
            }
            System.out.println("item.getType() = " + item.getType());
            if (item.getType() != Material.AIR) {
                System.out.println("item não é AIR");
                return true;
            }
        }
//        for (ItemStack item : player.getInventory().getContents()) {
//            if (item != null && !item.getType().toString().contains("AIR")) {
//                return true;
//            }
//        }
        return false;
    }

    public static void clearInventory(@NotNull Player player) {
        player.getInventory().clear();
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
    }
}
