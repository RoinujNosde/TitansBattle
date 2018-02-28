package me.roinujnosde.titansbattle.commands;

import java.text.MessageFormat;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.managers.ConfigManager;
import me.roinujnosde.titansbattle.managers.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author RoinujNosde
 */
public class WatchCommand {

    private final String permission = "titansbattle.watch";
    private final TitansBattle plugin;
    private final ConfigManager cm;
    private final GameManager gm;

    public WatchCommand() {
        plugin = TitansBattle.getInstance();
        cm = plugin.getConfigManager();
        gm = plugin.getGameManager();
    }

    public void execute(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLang("player-command"));
            return;
        }
        Player player = (Player) sender;
        if (!player.hasPermission(permission)) {
            plugin.debug("" + sender.getName() + " tried to use the " + getClass().getName() + " without permission", true);
            player.sendMessage(MessageFormat.format(plugin.getLang("no-permission"), permission));
            return;
        }
        if (!gm.isHappening() && !gm.isStarting()) {
            player.sendMessage(plugin.getLang("not-starting-or-started"));
            return;
        }
        try {
            player.teleport(gm.getCurrentGame().getWatchroom());
        } catch (NullPointerException ex) {
            player.sendMessage(ChatColor.RED + "An error has ocurred while trying to teleport you! Contact the admin! :o");
            plugin.debug("You have not setted the Watchroom teleport destination!", false);
        }
    }
}
