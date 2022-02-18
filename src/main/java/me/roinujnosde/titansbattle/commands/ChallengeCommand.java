package me.roinujnosde.titansbattle.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.BaseGameConfiguration.Destination;
import me.roinujnosde.titansbattle.challenges.*;
import me.roinujnosde.titansbattle.dao.ConfigurationDao;
import me.roinujnosde.titansbattle.managers.ChallengeManager;
import me.roinujnosde.titansbattle.managers.DatabaseManager;
import me.roinujnosde.titansbattle.types.Group;
import me.roinujnosde.titansbattle.types.Kit;
import me.roinujnosde.titansbattle.types.Warrior;
import me.roinujnosde.titansbattle.utils.SoundUtils;

import java.util.Objects;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("%titansbattle|tb")
@Subcommand("%challenge|challenge")
public class ChallengeCommand extends BaseCommand {

    @Dependency
    private TitansBattle plugin;
    @Dependency
    private ChallengeManager challengeManager;
    @Dependency
    private DatabaseManager databaseManager;
    @Dependency
    private ConfigurationDao configDao;

    @Subcommand("%player|player")
    @CommandCompletion("@players @arenas:group=false")
    @Conditions("can_challenge:group=false")
    @CommandPermission("titansbattle.challenge.player")
    @Description("{@@command.description.challenge.player}")
    public void challengePlayer(Warrior challenger, @Conditions("other") OnlinePlayer target,
            @Conditions("ready:group=false|empty_inventory") ArenaConfiguration arena) {
        Challenge challenge = new Challenge(plugin, arena);
        Warrior challenged = databaseManager.getWarrior(target.player);
        WarriorChallengeRequest request = new WarriorChallengeRequest(challenge, challenger, challenged);

        challengeManager.add(request);

        challenger.sendMessage(plugin.getLang("you.challenged.player", challenge, target.player.getName()));
        target.player.sendMessage(plugin.getLang("challenged.you", challenge, challenger.getName()));
        challenge.onJoin(challenger);
    }

    @Subcommand("%group|group")
    @CommandCompletion("@groups @arenas:group=true")
    @Conditions("can_challenge:group=true")
    @CommandPermission("titansbattle.challenge.group")
    @Description("{@@command.description.challenge.group}")
    public void challengeGroup(Warrior sender, @Conditions("other") Group target,
            @Conditions("ready:group=true|empty_inventory") ArenaConfiguration arena) {
        Challenge challenge = new Challenge(plugin, arena);
        Group challenger = Objects.requireNonNull(sender.getGroup());
        GroupChallengeRequest request = new GroupChallengeRequest(challenge, challenger, target);

        challengeManager.add(request);

        sender.sendMessage(plugin.getLang("you.challenged.group", challenge, target.getName()));
        String msgRivals = plugin.getLang("challenged.your.group", challenge, challenger.getName(),
                challenger.getUniqueName());
        plugin.getGroupManager().getWarriors(target).forEach(w -> w.sendMessage(msgRivals));
        String msgOwn = plugin.getLang("your.group.challenged", challenge, challenger.getUniqueName(),
                target.getName());
        Set<Warrior> members = plugin.getGroupManager().getWarriors(challenger);
        members.remove(sender);
        members.forEach(w -> w.sendMessage(msgOwn));
        challenge.onJoin(sender);
    }

    @Subcommand("%accept|accept")
    @CommandCompletion("@requests")
    @CommandPermission("titansbattle.challenge.accept")
    @Description("{@@command.description.challenge.accept}")
    public void accept(@Conditions("is_invited") Warrior warrior, @Values("@requests") ChallengeRequest<?> challenger) {
        challenger.getChallenge().onJoin(warrior);
    }

    @Subcommand("%watch|watch")
    @CommandPermission("titansbattle.watch")
    @CommandCompletion("@arenas:in_use")
    @Description("{@@command.description.challenge.watch}")
    public void watch(Player sender, ArenaConfiguration arena) {
        Location watchroom = arena.getWatchroom();
        if (watchroom == null) {
            sender.sendMessage(plugin.getLang("teleport.error"));
            return;
        }
        sender.teleport(watchroom);
        SoundUtils.playSound(SoundUtils.Type.WATCH, plugin.getConfig(), sender);
    }

    @Subcommand("%create|create")
    @CommandPermission("titansbattle.create")
    @Description("{@@command.description.challenge.create}")
    public void create(CommandSender sender, String arena) {
        if (configDao.create(arena, ArenaConfiguration.class)) {
            sender.sendMessage(plugin.getLang("arena-created", arena));
        } else {
            sender.sendMessage(plugin.getLang("config-creation-error"));
        }
    }

    @Subcommand("%setdestination|setdestination")
    @CommandPermission("titansbattle.setdestination")
    @CommandCompletion("@arenas")
    @Description("{@@command.description.challenge.setdestination}")
    public void setDestination(Player player, @Values("@arenas") ArenaConfiguration arena, Destination destination) {
        Location loc = player.getLocation();
        switch (destination) {
            case EXIT:
                arena.setExit(loc);
                break;
            case ARENA:
                arena.setArena(loc);
                break;
            case LOBBY:
                arena.setLobby(loc);
                break;
            case WATCHROOM:
                arena.setWatchroom(loc);
                break;
            case BORDER_CENTER:
                arena.setBorderCenter(loc);
                break;
        }
        configDao.save(arena);
        player.sendMessage(plugin.getLang("destination_set", destination));
    }

    @Subcommand("%setkit|setkit")
    @CommandPermission("titansbattle.setinventory")
    @CommandCompletion("@arenas")
    @Description("{@@command.description.challenge.setkit}")
    public void setKit(Player sender, @Values("@arenas") ArenaConfiguration arena) {
        arena.setKit(new Kit(sender.getInventory()));
        if (configDao.save(arena)) {
            sender.sendMessage(plugin.getLang("inventory-set"));
        } else {
            sender.sendMessage(plugin.getLang("error-saving-config"));
        }
    }

}
