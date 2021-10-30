package me.roinujnosde.titansbattle.utils;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MessageUtils {

    private static Class<?> baseComponentClass;
    private static Class<?> chatSerializerClass;
    private static Class<?> packetClass;

    static {
        try {
            baseComponentClass = Class.forName("net.minecraft.server.v1_8_R3.IChatBaseComponent");
            chatSerializerClass = Class.forName("net.minecraft.server.v1_8_R3.IChatBaseComponent$ChatSerializer");
            packetClass = Class.forName("net.minecraft.server.v1_8_R3.PacketPlayOutChat");
        } catch (ClassNotFoundException ignored) {
        }
    }

    private MessageUtils() {
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

            Method sendPacket = playerConnection.getClass().getMethod("sendPacket", packetClass.getSuperclass());
            Method serializer = chatSerializerClass.getMethod("a", String.class);
            Object baseComponent = serializer.invoke(null, jsonMessage);

            Object packet = packetClass.getConstructor(baseComponentClass, byte.class).newInstance(baseComponent,
                    (byte) 2);
            sendPacket.invoke(playerConnection, packet);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

}
