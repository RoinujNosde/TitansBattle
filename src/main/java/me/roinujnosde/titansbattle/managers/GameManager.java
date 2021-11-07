package me.roinujnosde.titansbattle.managers;

import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.events.NewKillerEvent;
import me.roinujnosde.titansbattle.games.EliminationTournamentGame;
import me.roinujnosde.titansbattle.games.FreeForAllGame;
import me.roinujnosde.titansbattle.games.Game;
import me.roinujnosde.titansbattle.types.GameConfiguration;
import me.roinujnosde.titansbattle.types.Scheduler;
import me.roinujnosde.titansbattle.types.Warrior;
import me.roinujnosde.titansbattle.utils.Helper;
import me.roinujnosde.titansbattle.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.UUID;

public class GameManager {

    private final TitansBattle plugin = TitansBattle.getInstance();
    private @Nullable Game currentGame;

    /**
     * Gets the current game
     *
     * @return the current game
     */
    public @NotNull Optional<Game> getCurrentGame() {
        return Optional.ofNullable(currentGame);
    }

    public void setCurrentGame(@Nullable Game game) {
        this.currentGame = game;
    }

    /**
     * Initiates the schedule task or starts the game
     */
    public void startOrSchedule() {
        if (!plugin.getConfigManager().isScheduler()) {
            plugin.debug("Scheduler disabled", true);
            return;
        }
        plugin.debug("Scheduler enabled", true);
        TaskManager tm = plugin.getTaskManager();
        if (currentGame != null) {
            plugin.debug("Already happening or starting", true);
            tm.startSchedulerTask(300);
            return;
        }
        int currentTimeInSeconds = Helper.getCurrentTimeInSeconds();
        int oneDay = 86400;

        Scheduler nextScheduler = Scheduler.getNextSchedulerOfDay();
        if (nextScheduler == null) {
            plugin.debug("No next scheduler today", true);
            tm.startSchedulerTask(oneDay - currentTimeInSeconds);
            return;
        }
        int nextHour = nextScheduler.getHour();
        int nextMinute = nextScheduler.getMinute();
        int nextTimeInSeconds = (nextHour * 60 * 60) + (nextMinute * 60);
        plugin.debug(String.format("Scheduler Time: %s:%s", nextHour, nextMinute), true);

        if (currentTimeInSeconds == nextTimeInSeconds) {
            plugin.debug("It's time!", true);
            Optional<GameConfiguration> config = plugin.getConfigurationDao()
                    .getConfiguration(nextScheduler.getGameName(), GameConfiguration.class);
            if (!config.isPresent()) {
                plugin.debug(String.format("Game %s not found!", nextScheduler.getGameName()), false);
                return;
            }
            start(config.get());
        } else {
            plugin.debug("It's not time yet!", true);
            tm.startSchedulerTask(nextTimeInSeconds - currentTimeInSeconds);
        }
    }

    public void setKiller(@NotNull GameConfiguration gameConfig, @NotNull Warrior killer, @Nullable Player victim) {
        Player player = killer.toOnlinePlayer();
        if (player == null) return;
        setKiller(gameConfig, player, victim);
    }

    public void setKiller(@NotNull GameConfiguration gameConfig, @NotNull Player killer, @Nullable Player victim) {
        if (!gameConfig.isKiller()) {
            return;
        }
        Bukkit.getPluginManager().callEvent(new NewKillerEvent(killer, victim));
        Bukkit.getServer().getOnlinePlayers().forEach((p) -> MessageUtils.sendActionBar(p,
                MessageFormat.format(plugin.getLang("new_killer", gameConfig.getFileConfiguration()), killer.getName())));
        plugin.getDatabaseManager().getTodaysWinners().setKiller(gameConfig.getName(), killer.getUniqueId());
    }

    /**
     * Returns the killer of the specified game
     *
     * @param game the game
     * @return the killer UUID
     */
    @SuppressWarnings("unused")
    public UUID getKiller(@NotNull Game game) {
        return plugin.getDatabaseManager().getLatestWinners().getKiller(game.getConfig().getName());
    }

    public void start(@NotNull GameConfiguration config) {
        Game game;
        if (config.isEliminationTournament()) {
            game = new EliminationTournamentGame(plugin, config);
        } else {
            game = new FreeForAllGame(plugin, config);
        }
        game.start();
    }

}
