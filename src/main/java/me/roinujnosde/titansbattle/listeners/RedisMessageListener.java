package me.roinujnosde.titansbattle.listeners;

import io.lettuce.core.pubsub.RedisPubSubListener;
import org.bukkit.Bukkit;

public class RedisMessageListener implements RedisPubSubListener<String, String> {
    public RedisMessageListener() {
    }

    @Override
    public void message(String channel, String message) {
        if ("titansbattle-broadcasts".equals(channel)) {
            Bukkit.getServer().broadcastMessage(message);
        }
    }

    @Override
    public void message(String pattern, String channel, String message) {
        // Não necessário para este caso de uso
    }

    @Override
    public void subscribed(String channel, long count) {
        // Quando o canal é subscrito
    }

    @Override
    public void psubscribed(String pattern, long count) {
        // Quando o padrão é subscrito
    }

    @Override
    public void unsubscribed(String channel, long count) {
        // Quando o canal é desinscrito
    }

    @Override
    public void punsubscribed(String pattern, long count) {
        // Quando o padrão é desinscrito
    }
}