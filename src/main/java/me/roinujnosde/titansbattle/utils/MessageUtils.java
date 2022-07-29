package me.roinujnosde.titansbattle.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.roinujnosde.titansbattle.TitansBattle;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public final class MessageUtils {

    private static Class<?> baseComponentClass;
    private static Class<?> chatSerializerClass;
    private static Class<?> packetClass;
    private static Class<?> chatPacketClass;

    static {
        try {
            baseComponentClass = Class.forName("net.minecraft.server.v1_8_R3.IChatBaseComponent");
            chatSerializerClass = Class.forName("net.minecraft.server.v1_8_R3.IChatBaseComponent$ChatSerializer");
            packetClass = Class.forName("net.minecraft.server.v1_8_R3.Packet");
            chatPacketClass = Class.forName("net.minecraft.server.v1_8_R3.PacketPlayOutChat");
        } catch (ClassNotFoundException ignored) {
        }
    }

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

    public static void sendActionBar(@NotNull Player player, @NotNull String message) {
        try {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
        } catch (NoSuchMethodError ex) {
            sendActionBar1_8(player, message);
        }
    }

    private static void sendActionBar1_8(Player player, String message) {
        String jsonMessage = "{\"text\": \"" + message + "\"}";

        try {
            Method getHandle = player.getClass().getMethod("getHandle");
            Object handle = getHandle.invoke(player);
            Field playerConnectionField = handle.getClass().getField("playerConnection");
            Object playerConnection = playerConnectionField.get(handle);

            Method sendPacket = playerConnection.getClass().getMethod("sendPacket", packetClass);
            Method serializer = chatSerializerClass.getMethod("a", String.class);
            Object baseComponent = serializer.invoke(null, jsonMessage);

            Object packet = chatPacketClass.getConstructor(baseComponentClass, byte.class).newInstance(baseComponent,
                    (byte) 2);
            sendPacket.invoke(playerConnection, packet);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

}
