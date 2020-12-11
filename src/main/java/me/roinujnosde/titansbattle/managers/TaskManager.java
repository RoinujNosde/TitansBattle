/*
 * The MIT License
 *
 * Copyright 2017 Edson Passos - edsonpassosjr@outlook.com.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.roinujnosde.titansbattle.managers;

import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.Game;
import me.roinujnosde.titansbattle.types.Prizes;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 *
 * @author RoinujNosde
 */
public class TaskManager {

    private final TitansBattle plugin = TitansBattle.getInstance();

    BukkitTask lobbyAnnouncementTask;
    BukkitTask arenaAnnouncementTask;
    BukkitTask schedulerTask;
    BukkitTask gameExpirationTask;
    BukkitTask preparationTimeTask;
    BukkitTask giveItemsTask;


    public void startArenaAnnouncementTask(long interval) {
        arenaAnnouncementTask = new ArenaAnnouncementTask().runTaskTimerAsynchronously(plugin, interval * 20, interval * 20);
    }

    public void startPreparationTimeTask(long interval) {
        preparationTimeTask = new PreparationTimeTask().runTaskLaterAsynchronously(plugin, interval * 20);
    }

    public void startLobbyAnnouncementTask(int times, long interval) {
        lobbyAnnouncementTask = new LobbyAnnouncementTask(times, interval)
                .runTaskTimerAsynchronously(plugin, 0, interval * 20);
    }

    public void startSchedulerTask(long interval) {
        schedulerTask = new SchedulerTask().runTaskLaterAsynchronously(plugin, interval * 20);
    }

    public void startGameExpirationTask(long interval) {
        gameExpirationTask = new GameExpirationTask().runTaskLaterAsynchronously(plugin, interval * 20);
    }

    public void startGiveItemsTask(long interval) {
        interval = interval * 20;
        giveItemsTask = new GiveItemsTask().runTaskTimerAsynchronously(plugin, interval, interval);
    }

    private class GiveItemsTask extends BukkitRunnable {

        private ItemStack[] toArray(HashMap<Integer, ItemStack> map) {
            return map.values().toArray(new ItemStack[0]);
        }

        @Override
        public void run() {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!Prizes.getPlayersWithItemsToReceive().isEmpty()) {
                        Iterator<Entry<Player, HashMap<Integer, ItemStack>>> iterator =
                                Prizes.getPlayersWithItemsToReceive().entrySet().iterator();
                        while (iterator.hasNext()) {
                            Entry<Player, HashMap<Integer, ItemStack>> entry = iterator.next();
                            Player player = entry.getKey();
                            HashMap<Integer, ItemStack> remainingItems = player.getInventory().
                                    addItem(toArray(entry.getValue()));
                            if (remainingItems.isEmpty()) {
                                iterator.remove();
                            } else {
                                entry.setValue(remainingItems);
                                player.sendMessage(MessageFormat.format(plugin.getLang("items_to_receive"),
                                        Integer.toString(remainingItems.size())));
                            }
                        }
                    } else {
                        giveItemsTask.cancel();
                    }
                }
            }.runTask(plugin);
        }

    }

    private class PreparationTimeTask extends BukkitRunnable {

        @Override
        public void run() {
            new BukkitRunnable() {
                @Override
                public void run() {
                    GameManager gm = plugin.getGameManager();
                    Game currentGame = gm.getCurrentGame();
                    if (currentGame == null) {
                        return;
                    }
                    Bukkit.getServer().broadcastMessage(plugin.getLang("preparation_over", currentGame));
                    currentGame.setPreparation(false);
                    currentGame.setBattle(true);
                }
            }.runTask(plugin);
        }
    }

    private class LobbyAnnouncementTask extends BukkitRunnable {

        int times;
        long interval, seconds;

        public LobbyAnnouncementTask(int times, long interval) {
            times--;
            this.times = times;
            this.interval = interval;
        }

        @Override
        public void run() {
            new BukkitRunnable() {
                @Override
                public void run() {
                    GameManager gm = plugin.getGameManager();
                    seconds = times * interval;
                    if (times > 0) {
                        Game currentGame = gm.getCurrentGame();
                        if (currentGame == null) {
                            cancel();
                            return;
                        }
                        Bukkit.getServer().broadcastMessage(MessageFormat.format(plugin.getLang("starting_game",
                                currentGame), Long.toString(seconds),
                                Integer.toString(currentGame.getConfig().getMinimumGroups()),
                                Integer.toString(currentGame.getConfig().getMinimumPlayers()),
                                Integer.toString(currentGame.getGroupsParticipatingCount()),
                                Integer.toString(currentGame.getPlayersParticipatingCount())));
                        times--;
                    } else {
                        gm.startBattle();
                        cancel();
                    }
                }
            }.runTask(plugin);
        }
    }

    private class ArenaAnnouncementTask extends BukkitRunnable {

        @Override
        public void run() {
            GameManager gm = plugin.getGameManager();
            final Game currentGame = gm.getCurrentGame();
            if (currentGame == null) {
                this.cancel();
                return;
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    String groupsText;
                    GroupManager groupManager = plugin.getGroupManager();
                    if (groupManager != null) {
                        groupsText = groupManager.buildStringFrom(currentGame.getGroups().keySet());
                    } else {
                        return;
                    }
                    Bukkit.getServer().broadcastMessage(MessageFormat.format(plugin.getLang("game_info", currentGame),
                            Integer.toString(currentGame.getPlayersParticipatingCount()),
                            Integer.toString(currentGame.getGroupsParticipatingCount()),
                            groupsText));
                }
            }.runTask(plugin);
        }
    }

    private class GameExpirationTask extends BukkitRunnable {

        @Override
        public void run() {
            new BukkitRunnable() {
                @Override
                public void run() {
                    GameManager gm = plugin.getGameManager();
                    Game currentGame = gm.getCurrentGame();
                    if (currentGame != null && currentGame.isHappening()) {
                        Bukkit.broadcastMessage(plugin.getLang("game_expired", currentGame));
                        gm.finishGame(null, null, null);
                    }
                    gameExpirationTask.cancel();
                }
            }.runTask(plugin);
        }
    }

    private class SchedulerTask extends BukkitRunnable {

        @Override
        public void run() {
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getGameManager().startOrSchedule();
                }
            }.runTask(plugin);
        }
    }

    public void killAllTasks() {
        if (lobbyAnnouncementTask != null) {
            lobbyAnnouncementTask.cancel();
        }
        if (arenaAnnouncementTask != null) {
            arenaAnnouncementTask.cancel();
        }
        if (schedulerTask != null) {
            schedulerTask.cancel();
            plugin.getGameManager().startOrSchedule();
        }
        if (gameExpirationTask != null) {
            gameExpirationTask.cancel();
        }
        if (preparationTimeTask != null) {
            preparationTimeTask.cancel();
        }
    }
}
