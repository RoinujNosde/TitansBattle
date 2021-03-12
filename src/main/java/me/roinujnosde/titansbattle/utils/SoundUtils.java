package me.roinujnosde.titansbattle.utils;

import me.roinujnosde.titansbattle.types.Warrior;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Locale;
import java.util.logging.Logger;

public class SoundUtils {

    private static final Logger LOGGER = Logger.getLogger("TitansBattle");

    private SoundUtils() {
    }

    @SafeVarargs
    public static void playSound(@NotNull Type type,
                                 @NotNull FileConfiguration config,
                                 @Nullable Collection<Warrior>... args) {
        if (args != null) {
            for (Collection<Warrior> warriors : args) {
                if (warriors == null) continue;
                warriors.stream().map(Warrior::toOnlinePlayer).forEach(player -> playSound(type, config, player));
            }
        }
    }

    public static void playSound(@NotNull Type type, @NotNull FileConfiguration config, @Nullable Player player) {
        if (player == null) {
            return;
        }
        String soundName = config.getString("sounds." + type.name().toLowerCase(Locale.ROOT), "");
        if (soundName.isEmpty()) return;
        Sound sound = null;
        try {
             sound = Sound.valueOf(soundName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            LOGGER.warning(String.format("Invalid sound: %s", soundName));
        }
        player.playSound(player.getLocation(), sound, 1F, 1F);
    }

    public enum Type {
        JOIN_GAME, LEAVE_GAME, ALLY_DEATH, ENEMY_DEATH, WATCH, TELEPORT, VICTORY
    }
}
