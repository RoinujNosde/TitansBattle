package me.roinujnosde.titansbattle.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class SoundUtils {

    private SoundUtils() {
    }

    public static void playSound(@NotNull Type type,
                                 @NotNull FileConfiguration config,
                                 @Nullable Collection<Player> players) {
        if (players == null) {
            return;
        }
        for (Player player : players) {
            playSound(type, config, player);
        }
    }

    public static void playSound(@NotNull Type type, @NotNull FileConfiguration config, @Nullable Player player) {
        if (player == null) {
            return;
        }
        String soundName = config.getString("sounds." + type.name().toLowerCase(), "");
        player.playSound(player.getLocation(), soundName, 1F, 1F);
    }

    public enum Type {
        JOIN_GAME, LEAVE_GAME, ALLY_DEATH, ENEMY_DEATH, WATCH, TELEPORT, VICTORY
    }
}
