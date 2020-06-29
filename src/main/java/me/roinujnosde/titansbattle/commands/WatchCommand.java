package me.roinujnosde.titansbattle.commands;

import java.text.MessageFormat;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.managers.ConfigManager;
import me.roinujnosde.titansbattle.managers.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author RoinujNosde
 */
public class WatchCommand {

    private final TitansBattle plugin;
    private final GameManager gm;

    public WatchCommand() {
        plugin = TitansBattle.getInstance();
        gm = plugin.getGameManager();
    }

    public void execute(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLang("player-command"));
            return;
        }
        Player player = (Player) sender;
        String permission = "titansbattle.watch";
        if (!player.hasPermission(permission)) {
            plugin.debug("" + sender.getName() + " tried to use the " + getClass().getName() + " without permission", true);
            player.sendMessage(MessageFormat.format(plugin.getLang("no-permission"), permission));
            return;
        }
        if (!gm.isHappening()) {
            player.sendMessage(plugin.getLang("not-starting-or-started"));
            return;
        }

        Location watchroom = gm.getCurrentGame().getWatchroom();
        if (watchroom != null) {
            player.teleport(watchroom);
        } else {
            player.sendMessage(ChatColor.RED + "An error has ocurred while trying to teleport you! Contact the admin! :o");
            plugin.debug("You have not setted the Watchroom teleport destination!", false);
        }
    }
}
