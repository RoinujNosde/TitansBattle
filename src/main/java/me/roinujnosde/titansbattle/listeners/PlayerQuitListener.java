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

import java.text.MessageFormat;
import me.roinujnosde.titansbattle.Helper;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.managers.ConfigManager;
import me.roinujnosde.titansbattle.managers.GameManager;
import me.roinujnosde.titansbattle.types.Game;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author RoinujNosde
 */
public class PlayerQuitListener implements Listener {

    private final GameManager gm;
    private final ConfigManager cm;
    private final Helper helper;

    public PlayerQuitListener() {
        helper = TitansBattle.getHelper();
        gm = TitansBattle.getGameManager();
        cm = TitansBattle.getConfigManager();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (gm.getParticipants().contains(player.getUniqueId())) {
            if (gm.isHappening()) {
                if (helper.isFun(gm.getCurrentGame())) {
                    cm.getClearInventory().add(player.getUniqueId());
                }
                cm.getRespawn().add(player.getUniqueId());
                cm.save();
                gm.removeParticipant(gm.getCurrentGame(), player);
            }
            if (gm.isStarting()) {
                gm.removeParticipant(gm.getCurrentGame(), player);
            }
        }
        if (helper.isWinner(player) || helper.isKiller(player)) {
            if (helper.isKiller(player) && helper.isWinner(player)) {
                if (helper.isKillerPriority(player)) {
                    Bukkit.broadcastMessage(MessageFormat.format(TitansBattle.getLang("killer-has-left", helper.getGameFromWinnerOrKiller(player)), player.getName()));
                } else {
                    Bukkit.broadcastMessage(MessageFormat.format(TitansBattle.getLang("winner-has-left", helper.getGameFromWinnerOrKiller(player)), player.getName()));
                }
            }
            if (helper.isKiller(player) && helper.isKillerQuitMessageEnabled(player)) {
                Bukkit.broadcastMessage(MessageFormat.format(TitansBattle.getLang("killer-has-left", helper.getGameFromWinnerOrKiller(player)), player.getName()));

            }
            if (helper.isWinner(player) && helper.isWinnerQuitMessageEnabled(player)) {
                Bukkit.broadcastMessage(MessageFormat.format(TitansBattle.getLang("winner-has-left", helper.getGameFromWinnerOrKiller(player)), player.getName()));

            }
        }
    }
}
