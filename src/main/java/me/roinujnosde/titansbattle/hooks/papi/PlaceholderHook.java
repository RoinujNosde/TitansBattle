package me.roinujnosde.titansbattle.hooks.papi;

import me.clip.placeholderapi.PlaceholderAPI;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.Warrior;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderHook {

    private final TitansBattle plugin;

    public PlaceholderHook(@NotNull TitansBattle plugin) {
        this.plugin = plugin;
        if (isPapi()) {
            new TBExpansion(plugin).register();
        }
    }

    public String parse(@Nullable OfflinePlayer player, @NotNull String text, String... internalPlaceholders) {
        for (int i = 0; i + 1 < internalPlaceholders.length; i = i + 2) {
            text = text.replace(internalPlaceholders[i], internalPlaceholders[i + 1]);
        }
        if (isPapi()) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }

    public String parse(@NotNull Warrior warrior, @NotNull String text, String... internalPlaceholders) {
        return parse(warrior.toPlayer(), text, internalPlaceholders);
    }

    public boolean isPapi() {
        return plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

}
