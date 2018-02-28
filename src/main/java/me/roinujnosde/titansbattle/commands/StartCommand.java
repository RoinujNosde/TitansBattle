package me.roinujnosde.titansbattle.commands;

import java.text.MessageFormat;
import me.roinujnosde.titansbattle.managers.GameManager;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.managers.ConfigManager;
import me.roinujnosde.titansbattle.types.Game.Mode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author RoinujNosde
 */
public class StartCommand {

    private final String permission = "titansbattle.start";
    private final GameManager gm;
    private final ConfigManager cm;
    private final TitansBattle plugin;

    public StartCommand() {
        plugin = TitansBattle.getInstance();
        gm = plugin.getGameManager();
        cm = plugin.getConfigManager();
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (sender instanceof Player && !sender.hasPermission(permission)) {
            plugin.debug("" + sender.getName() + " tried to use the " 
                    + getClass().getName() + " without permission", true);
            sender.sendMessage(MessageFormat.format(
                    plugin.getLang("no-permission"), permission));
            return true;
        }
        if (gm.isHappening() || gm.isStarting()) {
            sender.sendMessage(plugin.getLang("starting-or-started", gm.getCurrentGame()));
            return true;
        }
        Mode mode = mode = cm.getDefaultGameMode();
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
        }

        gm.start(mode);
        return true;
    }
}
