package me.roinujnosde.titansbattle.commands;

import java.text.MessageFormat;
import me.roinujnosde.titansbattle.managers.GameManager;
import me.roinujnosde.titansbattle.TitansBattle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author RoinujNosde
 */
public class CancelCommand {

    private final GameManager gm;
    private final String permission = "titansbattle.cancel";
    private final TitansBattle plugin;

    public CancelCommand() {
        plugin = TitansBattle.getInstance();
        gm = plugin.getGameManager();
    }

    public void execute(CommandSender sender) {
        if (sender instanceof Player && !sender.hasPermission(permission)) {
            plugin.debug("" + sender.getName() + " tried to use the "
                    + getClass().getName() + " without permission", true);
            sender.sendMessage(MessageFormat.format(
                    plugin.getLang("no-permission"), permission));
            return;
        }
        if (gm.isStarting() == false && gm.isHappening() == false) {
            sender.sendMessage(plugin.getLang("not-starting-or-started"));
            return;
        }
        gm.cancelGame(sender, gm.getCurrentGame());
    }
}
