package me.roinujnosde.titansbattle;

import me.roinujnosde.titansbattle.commands.CancelCommand;
import me.roinujnosde.titansbattle.commands.ExitCommand;
import me.roinujnosde.titansbattle.commands.HelpCommand;
import me.roinujnosde.titansbattle.commands.JoinCommand;
import me.roinujnosde.titansbattle.commands.RankingCommand;
import me.roinujnosde.titansbattle.commands.ReloadCommand;
import me.roinujnosde.titansbattle.commands.SetDestinationCommand;
import me.roinujnosde.titansbattle.commands.SetInventoryCommand;
import me.roinujnosde.titansbattle.commands.StartCommand;
import me.roinujnosde.titansbattle.commands.WatchCommand;
import me.roinujnosde.titansbattle.commands.WinnersCommand;
import me.roinujnosde.titansbattle.managers.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TbCommandExecutor implements CommandExecutor {

    private final TitansBattle plugin;
    private final Helper helper;
    private final ConfigManager cm;
    
    private final StartCommand startCommand;
    private final HelpCommand helpCommand;
    private final CancelCommand cancelCommand;
    private final ReloadCommand reloadCommand;
    private final WatchCommand watchCommand;
    private final WinnersCommand winnersCommand;
    private final SetDestinationCommand setDestinationCommand;
    private final ExitCommand exitCommand;
    private final JoinCommand joinCommand;
    private final SetInventoryCommand setInventoryCommand;
    
    public TbCommandExecutor() {
        plugin = TitansBattle.getInstance();
        helper = plugin.getHelper();
        cm = plugin.getConfigManager();
        
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
            helpCommand.execute(sender, helper.removeFirstArg(args));
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
        if (args[0].equalsIgnoreCase(cm.getCommandRanking())) {
            return new RankingCommand().execute(sender, helper.removeFirstArg(args));
        }
        return false;
    }
}
