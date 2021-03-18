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
package me.roinujnosde.titansbattle.listeners;

import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.managers.ConfigManager;
import me.roinujnosde.titansbattle.managers.GameManager;
import me.roinujnosde.titansbattle.types.Kit;
import me.roinujnosde.titansbattle.utils.Helper;
import me.roinujnosde.titansbattle.utils.SoundUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 *
 * @author RoinujNosde
 */
public class PlayerJoinListener implements Listener {

    private final ConfigManager cm;
    private final TitansBattle plugin;
    private final GameManager gameManager;

    public PlayerJoinListener() {
        plugin = TitansBattle.getInstance();
        cm = plugin.getConfigManager();
        gameManager = plugin.getGameManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        teleportToExit(player);
        clearInventory(player);
        sendJoinMessage(player);
    }

    private void sendJoinMessage(Player player) {
        if (Helper.isWinner(player) || Helper.isKiller(player)) {
            boolean killerJoinMessageEnabled = Helper.isKillerJoinMessageEnabled(player);
            boolean winnerJoinMessageEnabled = Helper.isWinnerJoinMessageEnabled(player);
            FileConfiguration config = Helper.getConfigFromWinnerOrKiller(player);
            if (Helper.isKiller(player) && Helper.isWinner(player)) {
                if (Helper.isKillerPriority(player) && killerJoinMessageEnabled) {
                    gameManager.broadcastKey("killer-has-joined", config, player.getName());
                    return;
                }
                if (winnerJoinMessageEnabled) {
                    gameManager.broadcastKey("winner-has-joined", config, player.getName());
                }
                return;
            }
            if (Helper.isKiller(player) && killerJoinMessageEnabled) {
                gameManager.broadcastKey("killer-has-joined", config, player.getName());
            }
            if (Helper.isWinner(player) && winnerJoinMessageEnabled) {
                gameManager.broadcastKey("winner-has-joined", config, player.getName());
            }
        }
    }

    private void clearInventory(Player player) {
        if (cm.getClearInventory().contains(player.getUniqueId())) {
            Kit.clearInventory(player);
            cm.getClearInventory().remove(player.getUniqueId());
            cm.save();
        }
    }

    private void teleportToExit(Player player) {
        if (cm.getRespawn().contains(player.getUniqueId())) {
            if (cm.getGeneralExit() != null) {
                SoundUtils.playSound(SoundUtils.Type.TELEPORT, plugin.getConfig(), player);
                player.teleport(cm.getGeneralExit());
            } else {
                plugin.getLogger().warning(String.format("GENERAL_EXIT is not set, it was not possible to teleport %s",
                        player.getName()));
            }
            cm.getRespawn().remove(player.getUniqueId());
            cm.save();
        }
    }
}
