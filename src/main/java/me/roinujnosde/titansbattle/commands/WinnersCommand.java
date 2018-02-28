package me.roinujnosde.titansbattle.commands;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.UUID;
import me.roinujnosde.titansbattle.Helper;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.managers.ConfigManager;
import me.roinujnosde.titansbattle.managers.DatabaseManager;
import me.roinujnosde.titansbattle.types.Game;
import me.roinujnosde.titansbattle.types.Game.Mode;
import me.roinujnosde.titansbattle.types.Group;
import me.roinujnosde.titansbattle.types.Winners;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author RoinujNosde
 */
public class WinnersCommand {

    private String permission = "titansbattle.winners";
    private TitansBattle plugin;
    private Helper helper;
    private ConfigManager cm;
    private DatabaseManager db;

    public WinnersCommand() {
        plugin = TitansBattle.getInstance();
        helper = plugin.getHelper();
        cm = plugin.getConfigManager();
        db = plugin.getDatabaseManager();
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (sender instanceof Player && !sender.hasPermission(permission)) {
            plugin.debug("" + sender.getName() + " tried to use the "
                    + getClass().getName() + " without permission", true);
            sender.sendMessage(MessageFormat.format(
                    plugin.getLang("no-permission"), permission));
            return true;
        }
        Game game;
        if (cm.isAskForGameMode()) {
            if (args.length == 0) {
                return false;
            } else {
                try {
                    game = helper.getGame(Mode.valueOf(args[0].toUpperCase()));
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
        } else {
            game = helper.getGame(cm.getDefaultGameMode());
        }
        String tempDate = null;
        if (cm.isAskForGameMode()) {
            if (args.length >= 2) {
                tempDate = args[1];
            }
        } else {
            if (args.length >= 1) {
                tempDate = args[0];
            }
        }

        if (game == null) {
            return false;
        }

        Winners winners;
        if (tempDate == null) {
            winners = db.getLatestWinners();
        } else {
            try {
                winners = db.getWinners(new SimpleDateFormat(cm.getDateFormat()).parse(tempDate));
            } catch (ParseException ex) {
                return false;
            }
        }

        Set<UUID> playerWinners = winners.getPlayerWinners(game.getMode());
        String members;
        if (playerWinners == null) {
            members = plugin.getLang("winners-no-player-winners", game);
        } else {
            members = helper.getStringFromStringList(helper.uuidListToPlayerNameList(playerWinners));
        }
        UUID uuid = winners.getKiller(game.getMode());
        String name;
        if (uuid == null) {
            name = plugin.getLang("winners-no-killer", game);
        } else {
            name = Bukkit.getOfflinePlayer(uuid).getName();
        }
        Group g = winners.getWinnerGroup(game.getMode());
        String group;
        if (g == null) {
            group = plugin.getLang("winners-no-winner-group", game);
        } else {
            group = g.getWrapper().getName();
        }

        String date;
        try {
            date = new SimpleDateFormat(cm.getDateFormat()).format(winners.getDate());
        } catch (IllegalArgumentException ex) {
            plugin.debug("date-format is invalid, using dd/MM/yyyy instead!", true);
            date = new SimpleDateFormat("dd/MM/yyyy").format(winners.getDate());
        }
        sender.sendMessage(MessageFormat.format(plugin.getLang("winners", game), date, name, group, members));
        return true;
    }
}
