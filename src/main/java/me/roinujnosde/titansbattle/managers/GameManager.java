package me.roinujnosde.titansbattle.managers;

import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.events.NewKillerEvent;
import me.roinujnosde.titansbattle.exceptions.GameTypeNotFoundException;
import me.roinujnosde.titansbattle.exceptions.InvalidGameException;
import me.roinujnosde.titansbattle.games.Game;
import me.roinujnosde.titansbattle.types.GameConfiguration;
import me.roinujnosde.titansbattle.types.Warrior;
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
    public Optional<Game> getCurrentGame() {
        return Optional.ofNullable(currentGame);
    }

    public void setCurrentGame(@Nullable Game game) {
        this.currentGame = game;
        if (game != null) {
            plugin.getListenerManager().registerBattleListeners();
        } else {
            plugin.getListenerManager().unregisterBattleListeners();
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
        if (currentGame != null) {
            plugin.getLogger().warning("A game is already running!");
            return;
        }

        Game game = instantiateGame(config.getType(), config);
        game.start();
    }

    private Game instantiateGame(String className, GameConfiguration config)
            throws InvalidGameException, GameTypeNotFoundException {
        try {
            Class<? extends Game> gameClass = getGameClass(className).asSubclass(Game.class);
            return gameClass.getConstructor(TitansBattle.class, GameConfiguration.class).newInstance(plugin, config);
        } catch (ClassCastException ex) {
            throw new InvalidGameException("class does not extend Game");
        } catch (NoSuchMethodException ex) {
            throw new InvalidGameException("required constructor (TitansBattle, GameConfiguration) not found");
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Class<?> getGameClass(String className) throws GameTypeNotFoundException {
        try {
            return Class.forName("me.roinujnosde.titansbattle.games." + className);
        } catch (ClassNotFoundException ignored) {}
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
            throw new GameTypeNotFoundException(className);
        }
    }

}
