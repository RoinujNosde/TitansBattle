package me.roinujnosde.titansbattle.commands;

import java.text.MessageFormat;
import java.util.List;
import me.roinujnosde.titansbattle.TitansBattle;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author RoinujNosde
 */
public class HelpCommand {

    private final String permission = "titansbattle.help";

    public HelpCommand() {
    }

    public void execute(CommandSender sender) {
        if (sender instanceof Player && !sender.hasPermission(permission)) {
            sender.sendMessage(MessageFormat.format(TitansBattle.getLang("no-permission"), permission));
            return;
        }
        List<String> help = TitansBattle.getLanguageManager().getConfig().getStringList("help_command");
        for (String a : help) {
            if (a == null) {
                continue;
            }
            a = ChatColor.translateAlternateColorCodes('&', a);
            sender.sendMessage(a);
        }
    }
}
