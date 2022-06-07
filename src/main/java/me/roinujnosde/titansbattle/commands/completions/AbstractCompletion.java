package me.roinujnosde.titansbattle.commands.completions;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions.CommandCompletionHandler;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.dao.ConfigurationDao;
import me.roinujnosde.titansbattle.managers.ChallengeManager;
import me.roinujnosde.titansbattle.managers.ConfigManager;
import me.roinujnosde.titansbattle.managers.DatabaseManager;
import me.roinujnosde.titansbattle.managers.GroupManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractCompletion implements CommandCompletionHandler<BukkitCommandCompletionContext> {

    protected final TitansBattle plugin;

    public AbstractCompletion(TitansBattle plugin) {
        this.plugin = plugin;
    }

    public abstract @NotNull String getId();

    protected @Nullable GroupManager getGroupManager() {
        return plugin.getGroupManager();
    }

    protected DatabaseManager getDatabaseManager() {
        return plugin.getDatabaseManager();
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
