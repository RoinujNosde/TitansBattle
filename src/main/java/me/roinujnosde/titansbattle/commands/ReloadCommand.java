package me.roinujnosde.titansbattle.commands;

import java.io.Console;
import java.text.MessageFormat;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.managers.ConfigManager;
import me.roinujnosde.titansbattle.managers.GameManager;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

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
        gm = TitansBattle.getGameManager();
        cm = TitansBattle.getConfigManager();
    }

    public void execute(CommandSender sender) {
        if (sender instanceof Player && !sender.hasPermission(permission)) {
            sender.sendMessage(MessageFormat.format(TitansBattle.getLang("no-permission"), permission));
            return;
        }
        gm.finishGame(gm.getCurrentGame());
        plugin.saveDefaultConfig();
        cm.load();
        TitansBattle.getLanguageManager().reload();
        sender.sendMessage(TitansBattle.getLang("configuration-reloaded"));
    }
}
