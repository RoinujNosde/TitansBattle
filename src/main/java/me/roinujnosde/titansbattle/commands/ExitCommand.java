package me.roinujnosde.titansbattle.commands;

import java.text.MessageFormat;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.events.PlayerExitGameEvent;
import me.roinujnosde.titansbattle.managers.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author RoinujNosde
 */
public class ExitCommand {

    private final String permission = "titansbattle.exit";
    private final GameManager gm;
    private final TitansBattle plugin;

    public ExitCommand() {
        plugin = TitansBattle.getInstance();
        gm = plugin.getGameManager();
    }

    public void execute(CommandSender sender) {
        if (!sender.hasPermission(permission)) {
            plugin.debug("" + sender.getName() + " tried to use the "
                    + getClass().getName() + " without permission", true);
            sender.sendMessage(MessageFormat.format(
                    plugin.getLang("no-permission"), permission));
            return;
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!gm.getParticipants().contains(player.getUniqueId())) {
                player.sendMessage(plugin.getLang("not_participating"));
                return;
            }

            if (gm.isHappening() == false && gm.isStarting() == false) {
                sender.sendMessage(plugin.getLang("not-starting-or-started"));
                return;
            }

            PlayerExitGameEvent event = new PlayerExitGameEvent(player, gm.getCurrentGame());
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return;
            }

            gm.removeParticipant(gm.getCurrentGame(), player);

            sender.sendMessage(plugin.getLang("you-have-left", gm.getCurrentGame()));
        } else {
            sender.sendMessage(plugin.getLang("player-command"));
        }
    }

}
