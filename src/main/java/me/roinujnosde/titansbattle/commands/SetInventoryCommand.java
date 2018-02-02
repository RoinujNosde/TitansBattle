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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.roinujnosde.titansbattle.Helper;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.managers.ConfigManager;
import me.roinujnosde.titansbattle.types.Game.Mode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author RoinujNosde
 */
public class SetInventoryCommand {

    private final ConfigManager cm;
    private final String permission = "titansbattle.setinventory";
    private final Helper helper;

    public SetInventoryCommand() {
        cm = TitansBattle.getConfigManager();
        helper = TitansBattle.getHelper();
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission(permission)) {
                sender.sendMessage(MessageFormat.format(TitansBattle.getLang("no-permission"), permission));
                return true;
            }
            //Digitou apenas /tb setinventory
            if (args.length < 1) {
                return false;
            }
            //Digitou /tb setinventory kit
            if (args[0].equalsIgnoreCase("kit")) {
                Mode mode = null;
                if (cm.isAskForGameMode()) {
                    if (args.length < 2) {
                        return false;
                    }
                    try {
                        mode = Mode.valueOf(args[1].toUpperCase());
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                    if (helper.isReal(helper.getGame(mode))) {
                        sender.sendMessage(TitansBattle.getLang("game-does-not-accept-kits"));
                        return true;
                    }
                } else {
                    mode = cm.getDefaultGameMode();
                    if (helper.isReal(helper.getGame(mode))) {
                        sender.sendMessage(TitansBattle.getLang("game-does-not-accept-kits"));
                        return true;
                    }
                }
                List<ItemStack> items = new ArrayList<>();
                items.addAll(Arrays.asList(player.getInventory().getContents()));
                helper.getGame(mode).setKit(items);
                player.sendMessage(TitansBattle.getLang("inventory-set"));
                cm.save();
                return true;
            }
            //Digitou /tb setinventory prize
            if (args[0].equalsIgnoreCase("prize")) {
                if (args.length < 2) {
                    return false;
                }
                if (!args[1].equalsIgnoreCase("leaders") && !args[1].equalsIgnoreCase("members")) {
                    return false;
                }
                Mode mode = null;
                if (cm.isAskForGameMode()) {
                    if (args.length < 3) {
                        return false;
                    }
                    try {
                        mode = Mode.valueOf(args[1].toUpperCase());
                    } catch (IllegalArgumentException ex) {
                        return false;
                    }
                } else {
                    mode = cm.getDefaultGameMode();
                }
                if (args[1].equalsIgnoreCase("leaders")) {
                    List<ItemStack> items = new ArrayList<>();
                    items.addAll(Arrays.asList(player.getInventory().getContents()));
                    helper.getGame(mode).getPrizes().setLeaderItems(items);
                    cm.save();
                    player.sendMessage(TitansBattle.getLang("inventory-set"));
                    return true;
                }
                if (args[1].equalsIgnoreCase("members")) {
                    List<ItemStack> items = new ArrayList<>();
                    items.addAll(Arrays.asList(player.getInventory().getContents()));
                    helper.getGame(mode).getPrizes().setMemberItems(items);
                    cm.save();
                    player.sendMessage(TitansBattle.getLang("inventory-set"));
                    return true;
                }
            }
        } else {
            sender.sendMessage(TitansBattle.getLang("player-command"));
            return true;
        }
        return true;
    }
}
