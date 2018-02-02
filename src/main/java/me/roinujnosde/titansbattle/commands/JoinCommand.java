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
package me.roinujnosde.titansbattle.commands;

import com.massivecraft.factions.entity.MPlayer;
import java.text.MessageFormat;
import me.roinujnosde.titansbattle.Helper;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.events.PlayerJoinGameEvent;
import me.roinujnosde.titansbattle.managers.ConfigManager;
import me.roinujnosde.titansbattle.managers.GameManager;
import me.roinujnosde.titansbattle.types.Game.Mode;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.Permission;

/**
 *
 * @author RoinujNosde
 */
public class JoinCommand {

    private final TitansBattle plugin;
    private final GameManager gm;
    private final ConfigManager cm;
    private final Helper helper;
    private final String permission = "titansbattle.join";

    public JoinCommand() {
        plugin = TitansBattle.getInstance();
        gm = TitansBattle.getGameManager();
        cm = TitansBattle.getConfigManager();
        helper = TitansBattle.getHelper();
    }

    public void execute(CommandSender sender) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(MessageFormat.format(TitansBattle.getLang("no-permission"), permission));
            return;
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (gm.isHappening() == false && gm.isStarting() == false) {
                sender.sendMessage(TitansBattle.getLang("not-starting-or-started"));
            } else {
                if (gm.getParticipants().contains(player.getUniqueId())) {
                    player.sendMessage(TitansBattle.getLang("already-joined", gm.getCurrentGame()));
                    return;
                }
                if (helper.isGroupBased(gm.getCurrentGame())) {
                    if (plugin.isFactions()) {
                        if (!MPlayer.get(player).hasFaction()) {
                            player.sendMessage(TitansBattle.getLang("not_in_a_group", gm.getCurrentGame()));
                            return;
                        }
                    }
                    if (plugin.isSimpleClans()) {
                        if (plugin.getClanManager().getClanPlayer(player) == null) {
                            player.sendMessage(TitansBattle.getLang("not_in_a_group", gm.getCurrentGame()));
                            return;
                        }
                    }
                }
                if (gm.isHappening()) {
                    player.sendMessage(TitansBattle.getLang("game_is_happening", gm.getCurrentGame()));
                    return;
                }
                if (gm.isStarting()) {
                    if (helper.isFun(gm.getCurrentGame())) {
                        for (ItemStack item : player.getInventory().getContents()) {
                            if (item != null) {
                                player.sendMessage(TitansBattle.getLang("clear-your-inventory", gm.getCurrentGame()));
                                return;
                            }
                        }
                        for (ItemStack item : player.getInventory().getArmorContents()) {
                            if (item != null) {
                                player.sendMessage(TitansBattle.getLang("clear-your-inventory", gm.getCurrentGame()));
                                return;
                            }
                        }
                    }
                    PlayerJoinGameEvent event = new PlayerJoinGameEvent(player, gm.getCurrentGame());
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        return;
                    }
                    if (helper.isFun(gm.getCurrentGame())) {
                        player.getInventory().addItem(gm.getCurrentGame().getKit().toArray(new ItemStack[0]));
                    }
                    player.teleport(gm.getCurrentGame().getLobby());
                    gm.addParticipant(player);
                }
            }
        } else {
            sender.sendMessage(TitansBattle.getLang("player-command"));
        }
    }
}
