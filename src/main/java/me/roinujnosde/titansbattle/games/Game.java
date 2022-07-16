package me.roinujnosde.titansbattle.games;

import me.roinujnosde.titansbattle.BaseGame;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.managers.DatabaseManager;
import me.roinujnosde.titansbattle.types.GameConfiguration;
import me.roinujnosde.titansbattle.types.Group;
import me.roinujnosde.titansbattle.types.Kit;
import me.roinujnosde.titansbattle.types.Warrior;
import me.roinujnosde.titansbattle.utils.SoundUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

import static me.roinujnosde.titansbattle.utils.SoundUtils.Type.LEAVE_GAME;

public abstract class Game extends BaseGame {

    protected final DatabaseManager databaseManager;

    public Game(TitansBattle plugin, GameConfiguration config) {
        super(plugin, config);
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
        if (!getConfig().isKiller()) {
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
        discordAnnounce("discord_game_starting");
        gameManager.setCurrentGame(this);
    }

    @Override
    public void finish(boolean cancelled) {
        super.finish(cancelled);
        gameManager.setCurrentGame(null);
    }

    public void onKick(@NotNull Warrior warrior) {
        if (!isParticipant(warrior)) {
            return;
        }
        Player player = Objects.requireNonNull(warrior.toOnlinePlayer());
        if (getConfig().isUseKits()) {
            Kit.clearInventory(player);
        }
        SoundUtils.playSound(LEAVE_GAME, plugin.getConfig(), player);
        player.sendMessage(getLang("you_have_been_kicked"));
        processPlayerExit(warrior);
    }

    @Override
    protected void onLobbyEnd() {
        deleteGroups();
        int gameInfoInterval = getConfig().getAnnouncementGameInfoInterval() * 20;
        addTask(new ArenaAnnouncementTask().runTaskTimer(plugin, gameInfoInterval, gameInfoInterval));
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

    public @NotNull GameConfiguration getConfig() {
        return (GameConfiguration) config;
    }

    @Override
    protected void killTasks() {
        super.killTasks();
    }

    protected abstract @NotNull String getGameInfoMessage();

    protected class ArenaAnnouncementTask extends BukkitRunnable {

        @Override
        public void run() {
            broadcast(getGameInfoMessage(), Game.this);
        }
    }

}
