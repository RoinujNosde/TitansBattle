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
    private final TitansBattle plugin;

    public HelpCommand() {
        plugin = TitansBattle.getInstance();
    }

    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player && !sender.hasPermission(permission)) {
            plugin.debug("" + sender.getName() + " tried to use the "
                    + getClass().getName() + " without permission", true);
            sender.sendMessage(MessageFormat.format(
                    plugin.getLang("no-permission"), permission));
            return;
        }
        List<String> help = plugin.getLanguageManager().getConfig()
                .getStringList("help_command");
        if (help == null || help.isEmpty()) {
            return;
        }

        if (args.length == 0) {
            args = new String[]{"1"};
        }

        int page = 0;
        try {
            page = Integer.valueOf(args[0]);
        } catch (NumberFormatException ex) {
        }

        if (page <= 0) {
            page = 1;
        }

        int limit = plugin.getConfigManager().getPageLimitHelp();

        int first = (page == 1) ? 0 : ((page - 1) * limit);

        if (help.size() <= first) {
            sender.sendMessage(plugin.getLang("inexistent-page"));
            return;
        }

        int last = first + (limit - 1);

        sender.sendMessage(MessageFormat.format(plugin.getLang("help-title"), page));
        for (int i = first; i <= last; i++) {
            String message = "";
            try {
                message = help.get(i);
            } catch (IndexOutOfBoundsException ex) {
            }
            if (message.isEmpty()) {
                continue;
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
        if (help.size() > last) {
            sender.sendMessage(MessageFormat.format(plugin.getLang("help-next-page"), page + 1));
        }
    }
}
