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

import me.roinujnosde.titansbattle.BaseGame;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.Warrior;
import me.roinujnosde.titansbattle.utils.Helper;
import me.roinujnosde.titansbattle.utils.MessageUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author RoinujNosde
 */
public class PlayerQuitListener extends TBListener {

    public PlayerQuitListener(@NotNull TitansBattle plugin) {
        super(plugin);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Warrior warrior = plugin.getDatabaseManager().getWarrior(player);
        BaseGame game = plugin.getBaseGameFrom(player);
        if (game != null) {
            game.onDisconnect(warrior);
        }
        sendQuitMessage(player);
    }

    private void sendQuitMessage(Player player) {
        if (Helper.isWinner(player) || Helper.isKiller(player)) {
            boolean killerQuitMessageEnabled = Helper.isKillerQuitMessageEnabled(player);
            boolean winnerQuitMessageEnabled = Helper.isWinnerQuitMessageEnabled(player);
            FileConfiguration config = Helper.getConfigFromWinnerOrKiller(player);
            if (Helper.isKiller(player) && Helper.isWinner(player)) {
                if (Helper.isKillerPriority(player) && killerQuitMessageEnabled) {
                    MessageUtils.broadcastKey("killer-has-left", config, player.getName());
                    return;
                }
                if (winnerQuitMessageEnabled) {
                    MessageUtils.broadcastKey("winner-has-left", config, player.getName());
                }
                return;
            }
            if (Helper.isKiller(player) && killerQuitMessageEnabled) {
                MessageUtils.broadcastKey("killer-has-left", config, player.getName());
            }
            if (Helper.isWinner(player) && winnerQuitMessageEnabled) {
                MessageUtils.broadcastKey("winner-has-left", config, player.getName());
            }
        }
    }
}
