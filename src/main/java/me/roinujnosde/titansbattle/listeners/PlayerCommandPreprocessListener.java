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

import me.roinujnosde.titansbattle.BaseGame;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.managers.ConfigManager;
import me.roinujnosde.titansbattle.managers.GameManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;

/**
 *
 * @author RoinujNosde
 */
public class PlayerCommandPreprocessListener extends TBListener {

    public PlayerCommandPreprocessListener(@NotNull TitansBattle plugin) {
        super(plugin);
    }

    @EventHandler
    public void onCommandEveryone(PlayerCommandPreprocessEvent event) {
        GameManager gm = plugin.getGameManager();
        ConfigManager cm = plugin.getConfigManager();

        BaseGame game = gm.getCurrentGame().orElse(null);
        if (game == null) {
            return;
        }
        Player player = event.getPlayer();
        if (canBypassCommandRestrictions(player)) {
            return;
        }
        for (String command : cm.getBlockedCommandsEveryone()) {
            if (event.getMessage().startsWith(command)) {
                player.sendMessage(MessageFormat.format(plugin.getLang("command-blocked-for-everyone", game),
                        event.getMessage()));
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        ConfigManager cm = plugin.getConfigManager();

        Player player = event.getPlayer();
        BaseGame game = getBaseGameFrom(player);
        if (game == null) {
            return;
        }
        for (String command : cm.getAllowedCommands()) {
            if (event.getMessage().startsWith(command)) {
                return;
            }
        }
        if (!canBypassCommandRestrictions(player)) {
            player.sendMessage(MessageFormat.format(plugin.getLang("command-not-allowed", game), event.getMessage()));
            event.setCancelled(true);
        }
    }

    private boolean canBypassCommandRestrictions(Player player) {
        return player.hasPermission("titansbattle.command-bypass");
    }

}
