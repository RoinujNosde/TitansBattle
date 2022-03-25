package me.roinujnosde.titansbattle.managers;

import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.listeners.*;
import org.bukkit.event.HandlerList;

import java.util.HashSet;
import java.util.Set;

public class ListenerManager {

    private final TitansBattle plugin;
    private final Set<Class<? extends TBListener>> registered = new HashSet<>();

    public ListenerManager(TitansBattle plugin) {
        this.plugin = plugin;
    }

    public void registerGeneralListeners() {
        registerListener(new PlayerQuitListener(plugin), true);
        registerListener(new PlayerJoinListener(plugin), true);
    }

    public void registerBattleListeners() {
        registerListener(new PlayerRespawnListener(plugin));
        registerListener(new PlayerCommandPreprocessListener(plugin));
        registerListener(new PlayerDeathListener(plugin));
        registerListener(new EntityDamageListener(plugin));
        registerListener(new PlayerTeleportListener(plugin));
        registerListener(new BlockUpdateListener(plugin));
        plugin.getLogger().info("Registering battle listeners...");
    }

    public void unregisterBattleListeners() {
        if (plugin.getGameManager().getCurrentGame().isPresent()) {
            return;
        }
        if (!plugin.getChallengeManager().getChallenges().isEmpty()) {
            return;
        }
        for (TBListener listener : registered) {
            HandlerList.unregisterAll(listener);
            registered.remove(listener.getClass());
        }
        plugin.getLogger().info("Unregistering battle listeners...");
    }

    private void registerListener(TBListener listener, boolean permanent) {
        if (permanent || registered.add(listener.getClass())) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }

    private void registerListener(TBListener listener) {
        registerListener(listener, false);
    }
}
