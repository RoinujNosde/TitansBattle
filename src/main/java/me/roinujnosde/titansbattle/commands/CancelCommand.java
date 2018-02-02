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

    public CancelCommand() {
        gm = TitansBattle.getGameManager();

    }

    public void execute(CommandSender sender) {
        if (sender instanceof Player && !sender.hasPermission(permission)) {
            sender.sendMessage(MessageFormat.format(TitansBattle.getLang("no-permission"), permission));
            return;
        }
        if (gm.isStarting() == false && gm.isHappening() == false) {
            sender.sendMessage(TitansBattle.getLang("not-starting-or-started"));
			return;
        }
        gm.cancelGame(sender, gm.getCurrentGame());
    }
}
