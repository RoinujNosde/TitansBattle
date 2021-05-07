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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.roinujnosde.titansbattle.listeners;

import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.managers.ConfigManager;
import me.roinujnosde.titansbattle.managers.GameManager;
import me.roinujnosde.titansbattle.games.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.text.MessageFormat;

/**
 *
 * @author RoinujNosde
 */
public class PlayerCommandPreprocessListener implements Listener {

    private final GameManager gm;
    private final ConfigManager cm;
    private final TitansBattle plugin;

    public PlayerCommandPreprocessListener() {
        plugin = TitansBattle.getInstance();
        gm = plugin.getGameManager();
        cm = plugin.getConfigManager();
    }

    @EventHandler
    public void onCommandEveryone(PlayerCommandPreprocessEvent event) {
        Game game = gm.getCurrentGame().orElse(null);
        if (game == null) {
            return;
        }
        for (String command : cm.getBlockedCommandsEveryone()) {
            if (event.getMessage().startsWith(command)) {
                event.getPlayer().sendMessage(MessageFormat.format(plugin.getLang("command-blocked-for-everyone",
                        game), event.getMessage()));
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        Game game = gm.getCurrentGame().orElse(null);
        if (game == null) {
            return;
        }
        if (!game.isParticipant(plugin.getDatabaseManager().getWarrior(player))) {
            return;
        }
        for (String command : cm.getAllowedCommands()) {
            if (event.getMessage().startsWith(command)) {
                return;
            }
        }
        player.sendMessage(MessageFormat.format(plugin.getLang("command-not-allowed", game), event.getMessage()));
        event.setCancelled(true);
    }

}
