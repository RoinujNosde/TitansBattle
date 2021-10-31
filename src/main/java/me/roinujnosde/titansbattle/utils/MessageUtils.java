package me.roinujnosde.titansbattle.utils;

import me.roinujnosde.titansbattle.TitansBattle;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;

public class MessageUtils {

    private MessageUtils() {
    }

    public static void broadcastKey(@NotNull String messageKey, @Nullable FileConfiguration config, Object... args) {
        String message = TitansBattle.getInstance().getLang(messageKey, config);
        broadcast(message, config, args);
    }

    public static void broadcast(@Nullable String message, @Nullable FileConfiguration config, Object... args) {
        if (message == null || message.isEmpty()) {
            return;
        }
        message = MessageFormat.format(message, args);
        Bukkit.broadcastMessage(message);
    }

}
