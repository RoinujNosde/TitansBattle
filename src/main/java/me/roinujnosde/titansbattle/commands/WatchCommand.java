package me.roinujnosde.titansbattle.commands;

import java.text.MessageFormat;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.managers.ConfigManager;
import me.roinujnosde.titansbattle.managers.GameManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

/**
 *
 * @author RoinujNosde
 */
public class WatchCommand {

    private final String permission = "titansbattle.watch";
    private final ConfigManager cm;
    private final GameManager gm;

    public WatchCommand() {
        cm = TitansBattle.getConfigManager();
        gm = TitansBattle.getGameManager();
    }

    public void execute(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(TitansBattle.getLang("player-command"));
            return;
        }
        Player player = (Player) sender;
        if (!player.hasPermission(permission)) {
            player.sendMessage(MessageFormat.format(TitansBattle.getLang("no-permission"), permission));
            return;
        }
        if (!gm.isHappening() && !gm.isStarting()) {
            player.sendMessage(TitansBattle.getLang("not-starting-or-started"));
			return;
        }
        player.teleport(gm.getCurrentGame().getWatchroom());
    }
}
