package me.roinujnosde.titansbattle.commands;

import java.text.MessageFormat;
import me.roinujnosde.titansbattle.Helper;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.managers.ConfigManager;
import me.roinujnosde.titansbattle.types.Game;
import me.roinujnosde.titansbattle.types.Game.Mode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author RoinujNosde
 */
public class SetDestinationCommand {

    private final String permission = "titansbattle.setdestination";
    private final ConfigManager cm;
    private final Helper helper;
    private final TitansBattle plugin;

    public SetDestinationCommand() {
        plugin = TitansBattle.getInstance();
        cm = plugin.getConfigManager();
        helper = plugin.getHelper();
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(permission)) {
            plugin.debug("" + sender.getName() + " tried to use the "
                    + getClass().getName() + " without permission", true);
            sender.sendMessage(MessageFormat.format(
                    plugin.getLang("no-permission"), permission));
            return true;
        }
        if (args.length < 1) {
            return false;
        }
        Game game = null;
        if (!args[0].equalsIgnoreCase("generalexit")) {
            if (args.length == 1) {
                if (cm.isAskForGameMode()) {
                    return false;
                } else {
                    game = helper.getGame(cm.getDefaultGameMode());
                }
            }
            if (args.length == 2) {
                try {
                    game = helper.getGame(Mode.valueOf(args[1].toUpperCase()));
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
            if (game == null) {
                return false;
            }
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            switch (args[0].toLowerCase()) {
                case "exit":
                    game.setExit(player.getLocation());
                    break;
                case "watchroom":
                    game.setWatchroom(player.getLocation());
                    break;
                case "arena":
                    game.setArena(player.getLocation());
                    break;
                case "lobby":
                    game.setLobby(player.getLocation());
                    break;
                case "generalexit":
                    cm.setGeneralExit(player.getLocation());
                    break;
                default:
                    player.sendMessage(plugin.getLang("invalid-destination"));
                    return true;
            }
            player.sendMessage(MessageFormat.format(plugin.getLang("destination_setted"), args[0]));
            cm.save();
            return true;
        } else {
            sender.sendMessage(plugin.getLang("player-command"));
            return true;
        }
    }
}
