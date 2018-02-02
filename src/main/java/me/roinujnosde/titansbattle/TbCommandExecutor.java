package me.roinujnosde.titansbattle;

import me.roinujnosde.titansbattle.commands.CancelCommand;
import me.roinujnosde.titansbattle.commands.ExitCommand;
import me.roinujnosde.titansbattle.commands.HelpCommand;
import me.roinujnosde.titansbattle.commands.JoinCommand;
import me.roinujnosde.titansbattle.commands.ReloadCommand;
import me.roinujnosde.titansbattle.commands.SetDestinationCommand;
import me.roinujnosde.titansbattle.commands.SetInventoryCommand;
import me.roinujnosde.titansbattle.commands.StartCommand;
import me.roinujnosde.titansbattle.commands.WatchCommand;
import me.roinujnosde.titansbattle.commands.WinnersCommand;
import me.roinujnosde.titansbattle.managers.ConfigManager;
import me.roinujnosde.titansbattle.managers.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TbCommandExecutor implements CommandExecutor {
    TitansBattle plugin;
    GameManager gm;
    Helper helper;
    ConfigManager cm;

    StartCommand startCommand;
    HelpCommand helpCommand;
    CancelCommand cancelCommand;
    ReloadCommand reloadCommand;
    WatchCommand watchCommand;
    WinnersCommand winnersCommand;
    SetDestinationCommand setDestinationCommand;
    ExitCommand exitCommand;
    JoinCommand joinCommand;
    SetInventoryCommand setInventoryCommand;

    public TbCommandExecutor() {
        plugin = TitansBattle.getInstance();
        gm = TitansBattle.getGameManager();
        helper = TitansBattle.getHelper();
        cm = TitansBattle.getConfigManager();

        startCommand = new StartCommand();
        helpCommand = new HelpCommand();
        cancelCommand = new CancelCommand();
        reloadCommand = new ReloadCommand();
        watchCommand = new WatchCommand();
        winnersCommand = new WinnersCommand();
        setDestinationCommand = new SetDestinationCommand();
        exitCommand = new ExitCommand();
        joinCommand = new JoinCommand();
        setInventoryCommand = new SetInventoryCommand();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }
        if (args[0].equalsIgnoreCase(cm.getCommandSetInventory())) {
            return setInventoryCommand.execute(sender, helper.removeFirstArg(args));
        }
        if (args[0].equalsIgnoreCase(cm.getCommandWinners())) {
            return winnersCommand.execute(sender, helper.removeFirstArg(args));
        }
        if (args[0].equalsIgnoreCase(cm.getCommandCancel())) {
            cancelCommand.execute(sender);
            return true;
        }
        if (args[0].equalsIgnoreCase(cm.getCommandStart())) {
            return startCommand.execute(sender, helper.removeFirstArg(args));
        }
        if (args[0].equalsIgnoreCase(cm.getCommandHelp())) {
            helpCommand.execute(sender);
            return true;
        }
        if (args[0].equalsIgnoreCase(cm.getCommandReload())) {
            reloadCommand.execute(sender);
            return true;
        }
        if (args[0].equalsIgnoreCase(cm.getCommandWatch())) {
            watchCommand.execute(sender);
            return true;
        }
        if (args[0].equalsIgnoreCase(cm.getCommandSetDestination())) {
            return setDestinationCommand.execute(sender, helper.removeFirstArg(args));
        }
        if (args[0].equalsIgnoreCase(cm.getCommandExit())) {
            exitCommand.execute(sender);
            return true;
        }
        if (args[0].equalsIgnoreCase(cm.getCommandJoin())) {
            joinCommand.execute(sender);
            return true;
        }
        return false;
    }
}
