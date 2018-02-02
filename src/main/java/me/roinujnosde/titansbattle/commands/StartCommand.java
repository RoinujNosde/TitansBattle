package me.roinujnosde.titansbattle.commands;

import java.text.MessageFormat;
import me.roinujnosde.titansbattle.managers.GameManager;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.managers.ConfigManager;
import me.roinujnosde.titansbattle.types.Game.Mode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

/**
 *
 * @author RoinujNosde
 */
public class StartCommand {

    private final String permission = "titansbattle.start";
    private final GameManager gm;
    private final ConfigManager cm;

    public StartCommand() {
        gm = TitansBattle.getGameManager();
        cm = TitansBattle.getConfigManager();
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (sender instanceof Player && !sender.hasPermission(permission)) {
            sender.sendMessage(MessageFormat.format(TitansBattle.getLang("no-permission"), permission));
            return true;
        }
        if (gm.isHappening() || gm.isStarting()) {
            sender.sendMessage(TitansBattle.getLang("starting-or-started", gm.getCurrentGame()));
            return true;
        }
        Mode mode = null;
        if (cm.isAskForGameMode()) {
            if (args.length == 0) {
                return false;
            } else {
                try {
                    mode = Mode.valueOf(args[0].toUpperCase());
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
        } else {
            mode = cm.getDefaultGameMode();
        }
        if (mode == null) {
            return false;
        }
        gm.startLobby(mode);
        return true;
    }
}
