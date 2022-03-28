package me.roinujnosde.titansbattle.commands.contexts;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.contexts.ContextResolver;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.dao.ConfigurationDao;
import me.roinujnosde.titansbattle.managers.*;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class AbstractContextResolver<T> implements ContextResolver<T, BukkitCommandExecutionContext> {

    protected final TitansBattle plugin;

    public AbstractContextResolver(@NotNull TitansBattle plugin) {
        this.plugin = plugin;
    }

    public abstract @NotNull Class<T> getType();

    protected GameManager getGameManager() {
        return plugin.getGameManager();
    }

    protected ChallengeManager getChallengeManager() {
        return plugin.getChallengeManager();
    }

    protected ConfigManager getConfigManager() {
        return plugin.getConfigManager();
    }

    protected DatabaseManager getDatabaseManager() {
        return plugin.getDatabaseManager();
    }

    protected Optional<GroupManager> getGroupManager() {
        return Optional.ofNullable(plugin.getGroupManager());
    }

    protected ConfigurationDao getConfigurationDao() {
        return plugin.getConfigurationDao();
    }
}
