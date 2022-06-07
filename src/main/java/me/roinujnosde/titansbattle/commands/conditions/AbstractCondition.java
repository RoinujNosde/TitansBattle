package me.roinujnosde.titansbattle.commands.conditions;

import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.dao.ConfigurationDao;
import me.roinujnosde.titansbattle.managers.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractCondition {

    protected final TitansBattle plugin;

    public AbstractCondition(TitansBattle plugin) {
        this.plugin = plugin;
    }

    public abstract @NotNull String getId();

    protected @Nullable GroupManager getGroupManager() {
        return plugin.getGroupManager();
    }

    protected DatabaseManager getDatabaseManager() {
        return plugin.getDatabaseManager();
    }

    protected GameManager getGameManager() {
        return plugin.getGameManager();
    }

    protected ChallengeManager getChallengeManager() {
        return plugin.getChallengeManager();
    }

    protected ConfigManager getConfigManager() {
        return plugin.getConfigManager();
    }

    protected ConfigurationDao getConfigurationDao() {
        return plugin.getConfigurationDao();
    }

}
