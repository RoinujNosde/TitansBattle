package me.roinujnosde.titansbattle.commands;

import java.text.MessageFormat;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.managers.ConfigManager;
import me.roinujnosde.titansbattle.managers.GameManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author RoinujNosde
 */
public class ReloadCommand {

    private final String permission = "titansbattle.reload";
    private final TitansBattle plugin;
    private final GameManager gm;
    private final ConfigManager cm;

    public ReloadCommand() {
        plugin = TitansBattle.getInstance();
        gm = plugin.getGameManager();
        cm = plugin.getConfigManager();
    }

    public void execute(CommandSender sender) {
        if (sender instanceof Player && !sender.hasPermission(permission)) {
            plugin.debug("" + sender.getName() + " tried to use the "
                    + getClass().getName() + " without permission", true);
            sender.sendMessage(MessageFormat.format(
                    plugin.getLang("no-permission"), permission));
            return;
        }
        gm.finishGame(null, null, null);
        plugin.saveDefaultConfig();
        cm.load();
        plugin.getLanguageManager().reload();
        sender.sendMessage(plugin.getLang("configuration-reloaded"));
    }
}
