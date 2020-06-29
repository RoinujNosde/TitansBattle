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

import java.text.MessageFormat;
import java.util.HashMap;
import me.roinujnosde.titansbattle.Helper;
import me.roinujnosde.titansbattle.TitansBattle;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author RoinujNosde
 */
public class TaskManager {

    private TitansBattle plugin;
    private GameManager gm;
    private Helper helper;

    BukkitTask lobbyAnnouncementTask;
    BukkitTask arenaAnnouncementTask;
    BukkitTask schedulerTask;
    BukkitTask gameExpirationTask;
    BukkitTask preparationTimeTask;
    BukkitTask giveItemsTask;

    public void load() {
        plugin = TitansBattle.getInstance();
        gm = plugin.getGameManager();
        helper = plugin.getHelper();
    }

    public void setGiveItemsTask(BukkitTask giveItemsTask) {
        this.giveItemsTask = giveItemsTask;
    }

    public BukkitTask getLobbyAnnouncementTask() {
        return lobbyAnnouncementTask;
    }

    public BukkitTask getArenaAnnouncementTask() {
        return arenaAnnouncementTask;
    }

    public BukkitTask getGameExpirationTask() {
        return gameExpirationTask;
    }

    public BukkitTask getPreparationTimeTask() {
        return preparationTimeTask;
    }

    public BukkitTask getSchedulerTask() {
        return schedulerTask;
    }

    public void startArenaAnnouncementTask(long interval) {
        arenaAnnouncementTask = new ArenaAnnouncementTask().runTaskTimerAsynchronously(plugin, interval * 20, interval * 20);
    }

    public void startPreparationTimeTask(long interval) {
        preparationTimeTask = new PreparationTimeTask().runTaskLaterAsynchronously(plugin, interval * 20);
    }

    public void startLobbyAnnouncementTask(int times, long interval) {
        lobbyAnnouncementTask = new LobbyAnnouncementTask(times, interval)
                .runTaskTimerAsynchronously(plugin, (long) interval * 20, (long) interval * 20);
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

        @Override
        public void run() {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!helper.getPlayersWithItemsToReceive().isEmpty()) {
                        for (Player player : helper.getPlayersWithItemsToReceive()) {
                            if (!player.isOnline()) {
                                helper.getItemsNotGiven().remove(player);
                                continue;
                            }
                            HashMap<Integer, ItemStack> remainingItems = player.getInventory().addItem(
                                    helper.getItemsNotGivenToPlayer(player));
                            if (remainingItems.isEmpty()) {
                                helper.getItemsNotGiven().remove(player);
                            } else {
                                helper.getItemsNotGiven().replace(player, remainingItems);
                                player.sendMessage(MessageFormat.format(plugin.getLang("items_to_receive"), Integer.toString(remainingItems.size())));
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
                    Bukkit.getServer().broadcastMessage(plugin.getLang("preparation_over", gm.getCurrentGame()));
                    gm.setPreparation(false);
                    gm.setBattle(true);
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
                    seconds = times * interval;
                    if (times > 0) {
                        Bukkit.getServer().broadcastMessage(MessageFormat.format(plugin.getLang("starting_game", gm.getCurrentGame()),
                                Long.toString(seconds),
                                Integer.toString(gm.getCurrentGame().getMinimumGroups()),
                                Integer.toString(gm.getCurrentGame().getMinimumPlayers()),
                                Integer.toString(gm.getGroupsParticipatingCount()),
                                Integer.toString(gm.getPlayersParticipatingCount())));
                        times--;
                    } else {
                        gm.startBattle();
                        lobbyAnnouncementTask.cancel();
                    }
                }
            }.runTask(plugin);
        }
    }

    private class ArenaAnnouncementTask extends BukkitRunnable {

        @Override
        public void run() {
            if (gm.getCurrentGame() == null) {
                this.cancel();
                return;
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    String groupsText = helper.getStringFromGroupSet(gm.getGroups().keySet());
                    Bukkit.getServer().broadcastMessage(MessageFormat.format(plugin.getLang("game_info", gm.getCurrentGame()),
                            Integer.toString(gm.getPlayersParticipatingCount()),
                            Integer.toString(gm.getGroupsParticipatingCount()),
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
                    if (gm.isHappening()) {
                        Bukkit.broadcastMessage(plugin.getLang("game_expired", gm.getCurrentGame()));
                        gm.finishGame(null, null, null);
                        gameExpirationTask.cancel();
                    } else {
                        gameExpirationTask.cancel();
                    }
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
                    gm.startOrSchedule();
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
            gm.startOrSchedule();
        }
        if (gameExpirationTask != null) {
            gameExpirationTask.cancel();
        }
        if (preparationTimeTask != null) {
            preparationTimeTask.cancel();
        }
    }
}
