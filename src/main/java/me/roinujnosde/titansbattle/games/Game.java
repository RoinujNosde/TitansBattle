package me.roinujnosde.titansbattle.games;

import me.roinujnosde.titansbattle.BaseGame;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.managers.DatabaseManager;
import me.roinujnosde.titansbattle.types.GameConfiguration;
import me.roinujnosde.titansbattle.types.Group;
import me.roinujnosde.titansbattle.types.Warrior;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class Game extends BaseGame {

    protected final DatabaseManager databaseManager;
    private final GameConfiguration config;
    protected boolean battle = false;

    public Game(TitansBattle plugin, GameConfiguration config) {
        super(plugin);
        this.config = config;
        this.databaseManager = plugin.getDatabaseManager();
    }

    public boolean shouldClearDropsOnDeath(@NotNull Warrior warrior) {
        return isParticipant(warrior) && config.isClearItemsOnDeath();
    }

    public boolean shouldKeepInventoryOnDeath(@NotNull Warrior warrior) {
        return false;
    }

    /**
     * Attempts to find the Killer in the game, returns null if none found or if it's disabled
     */
    @Nullable
    public Warrior findKiller() {
        if (!config.isKiller()) {
            return null;
        }
        Warrior killerPlayer = null;
        int mostKills = 0;

        for (Map.Entry<Warrior, Integer> entry : getKillsCount().entrySet()) {
            Integer kills = entry.getValue();
            if (kills > mostKills) {
                killerPlayer = entry.getKey();
                mostKills = kills;
            }
        }
        return killerPlayer;
    }

    @Override
    public void start() {
        super.start();
        gameManager.setCurrentGame(this);
        Integer interval = getConfig().getAnnouncementStartingInterval();
        BukkitTask lobbyTask = new Game.LobbyAnnouncementTask(config.getAnnouncementStartingTimes(), interval)
                .runTaskTimer(plugin, 0, interval * 20);
        addTask(lobbyTask);
    }

    @Override
    public void finish(boolean cancelled) {
        super.finish(cancelled);
        gameManager.setCurrentGame(null);
        if (!cancelled) {
            processWinners();
        }
    }

    private void deleteGroups() {
        if (getConfig().isDeleteGroups()) {
            int deleted = 0;
            for (Group group : Objects.requireNonNull(plugin.getGroupManager()).getGroups()) {
                if (!getGroupParticipants().containsKey(group)) {
                    group.disband();
                    deleted++;
                }
            }
            if (deleted != 0) {
                broadcastKey("deleted_groups", deleted);
            }
        }
    }

    protected abstract void processWinners();

    protected void givePrizes(GameConfiguration.Prize prize, @Nullable Group group, @Nullable List<Warrior> warriors) {
        List<Player> leaders = new ArrayList<>();
        List<Player> members = new ArrayList<>();
        if (warriors == null) {
            return;
        }
        List<Player> players = warriors.stream().filter(Objects::nonNull).map(Warrior::toOnlinePlayer)
                .filter(Objects::nonNull).collect(Collectors.toList());
        if (group != null) {
            for (Player p : players) {
                if (group.isLeaderOrOfficer(p.getUniqueId())) {
                    leaders.add(p);
                } else {
                    members.add(p);
                }
            }
        } else {
            members = players;
        }
        getConfig().getPrizes(prize).give(plugin, leaders, members);
    }

    public @NotNull GameConfiguration getConfig() {
        return config;
    }

    @Override
    protected void killTasks() {
        super.killTasks();
        plugin.getTaskManager().killAllTasks();
    }

    protected class LobbyAnnouncementTask extends BukkitRunnable {
        int times;
        long interval, seconds;

        public LobbyAnnouncementTask(int times, long interval) {
            this.times = times + 1;
            this.interval = interval;
        }

        @Override
        public void run() {
            seconds = times * interval;
            if (times > 0) {
                broadcastKey("starting_game", seconds, getConfig().getMinimumGroups(),
                        getConfig().getMinimumPlayers(), getGroupParticipants().size(), getParticipants().size());
                times--;
            } else {
                preLobbyEnd();
                this.cancel();
            }
        }
    }

    private void preLobbyEnd() {
        if (canStartBattle()) {
            deleteGroups();
            lobby = false;
            onLobbyEnd();
            addTask(new GameExpirationTask().runTaskLater(plugin, getConfig().getExpirationTime() * 20));
            int gameInfoInterval = getConfig().getAnnouncementGameInfoInterval() * 20;
            addTask(new ArenaAnnouncementTask().runTaskTimer(plugin, gameInfoInterval, gameInfoInterval));
        } else {
            finish(true);
        }
    }

    protected abstract @NotNull String getGameInfoMessage();

    protected class ArenaAnnouncementTask extends BukkitRunnable {

        @Override
        public void run() {
            broadcast(getGameInfoMessage(), Game.this);
        }
    }

}
