package me.roinujnosde.titansbattle.managers;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.listeners.RedisMessageListener;

public class RedisManager {
    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> connection;
    private StatefulRedisPubSubConnection<String, String> pubSubConnection;

    public RedisManager(TitansBattle plugin, String redisUrl) {
        this.redisClient = RedisClient.create(redisUrl);
        this.connection = redisClient.connect();
    }

    public void registerPubSubListener() {
        this.pubSubConnection = redisClient.connectPubSub();
        pubSubConnection.addListener(new RedisMessageListener());
        pubSubConnection.sync().subscribe("titansbattle-broadcasts");
    }

    public RedisCommands<String, String> getCommands() {
        return connection.sync();
    }

    public void closeConnection() {
        if (pubSubConnection != null) {
            pubSubConnection.close();
        }
        connection.close();
        redisClient.shutdown();
    }
}
