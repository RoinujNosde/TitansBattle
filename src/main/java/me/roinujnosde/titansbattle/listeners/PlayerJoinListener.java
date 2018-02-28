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
import org.bukkit.Bukkit;
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
    private final Helper helper;
    private final TitansBattle plugin;

    public PlayerJoinListener() {
        plugin = TitansBattle.getInstance();
        cm = plugin.getConfigManager();
        helper = plugin.getHelper();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (cm.getRespawn().contains(player.getUniqueId())) {
            player.teleport(cm.getGeneralExit());
            cm.getRespawn().remove(player.getUniqueId());
            cm.save();
        }
        if (cm.getClearInventory().contains(player.getUniqueId())) {
            player.getInventory().clear();
            player.getInventory().setHelmet(null);
            player.getInventory().setChestplate(null);
            player.getInventory().setLeggings(null);
            player.getInventory().setBoots(null);
            cm.getClearInventory().remove(player.getUniqueId());
            cm.save();
        }
        if (helper.isWinner(player) || helper.isKiller(player)) {
            if (helper.isKiller(player) && helper.isWinner(player)) {
                if (helper.isKillerPriority(player)) {
                    Bukkit.broadcastMessage(MessageFormat.format(plugin.getLang("killer-has-joined", helper.getGameFromWinnerOrKiller(player)), player.getName()));
                } else {
                    Bukkit.broadcastMessage(MessageFormat.format(plugin.getLang("winner-has-joined", helper.getGameFromWinnerOrKiller(player)), player.getName()));
                }
            }
            if (helper.isKiller(player) && helper.isKillerJoinMessageEnabled(player)) {
                Bukkit.broadcastMessage(MessageFormat.format(plugin.getLang("killer-has-joined", helper.getGameFromWinnerOrKiller(player)), player.getName()));

            }
            if (helper.isWinner(player) && helper.isWinnerJoinMessageEnabled(player)) {
                Bukkit.broadcastMessage(MessageFormat.format(plugin.getLang("winner-has-joined", helper.getGameFromWinnerOrKiller(player)), player.getName()));

            }
        }
    }
}
