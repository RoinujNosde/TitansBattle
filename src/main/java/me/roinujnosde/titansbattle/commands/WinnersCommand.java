package me.roinujnosde.titansbattle.commands;

import java.text.MessageFormat;
import me.roinujnosde.titansbattle.Helper;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.managers.ConfigManager;
import me.roinujnosde.titansbattle.types.Game;
import me.roinujnosde.titansbattle.types.Game.Mode;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

/**
 *
 * @author RoinujNosde
 */
public class WinnersCommand {

    String permission = "titansbattle.winners";
    TitansBattle plugin;
    Helper helper;
    ConfigManager cm;

    public WinnersCommand() {
        plugin = TitansBattle.getInstance();
        helper = TitansBattle.getHelper();
        cm = TitansBattle.getConfigManager();
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (sender instanceof Player && !sender.hasPermission(permission)) {
            sender.sendMessage(MessageFormat.format(
                    TitansBattle.getLang("no-permission"), permission));
            return true;
        }
        Game game;
        if (cm.isAskForGameMode()) {
            if (args.length == 0) {
                return false;
            } else {
                try {
                    game = helper.getGame(Mode.valueOf(args[0].toUpperCase()));
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
        } else {
            game = helper.getGame(cm.getDefaultGameMode());
        }
        if (game == null) {
            return false;
        }
        String members = helper.getStringFromStringList(
                helper.uuidListToPlayerNameList(game.getWinners()));
        sender.sendMessage(MessageFormat.format(TitansBattle.getLang("winners", game),
                Bukkit.getOfflinePlayer(game.getKiller()).getName(),
                game.getWinnerGroup(), members));
        return true;
    }
}
