package me.roinujnosde.titansbattle.commands;

import java.text.MessageFormat;
import me.roinujnosde.titansbattle.Helper;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.events.PlayerExitGameEvent;
import me.roinujnosde.titansbattle.managers.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

/**
 *
 * @author RoinujNosde
 */
public class ExitCommand {

    private final String permission = "titansbattle.exit";
    private final Helper helper;
    private final GameManager gm;

    public ExitCommand() {
        gm = TitansBattle.getGameManager();
        helper = TitansBattle.getHelper();
    }

    public void execute(CommandSender sender) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(MessageFormat.format(TitansBattle.getLang("no-permission"), permission));
            return;
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!gm.getParticipants().contains(player.getUniqueId())) {
                player.sendMessage(TitansBattle.getLang("not_participating"));
                return;
            }
            if (gm.isHappening() == false && gm.isStarting() == false) {
                sender.sendMessage(TitansBattle.getLang("not-starting-or-started"));
                return;
            }
            PlayerExitGameEvent event = new PlayerExitGameEvent(player, gm.getCurrentGame());
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
                    if (helper.isFun(gm.getCurrentGame())) {
                player.getInventory().clear();
                player.getInventory().setHelmet(null);
                player.getInventory().setChestplate(null);
                player.getInventory().setLeggings(null);
                player.getInventory().setBoots(null);
            }
            sender.sendMessage(TitansBattle.getLang("you-have-left", gm.getCurrentGame()));
            gm.removeParticipant(gm.getCurrentGame(), player);
        } else {
            sender.sendMessage(TitansBattle.getLang("player-command"));
        }
    }

}
