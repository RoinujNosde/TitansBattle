package me.roinujnosde.titansbattle.commands;

import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.challenges.*;
import me.roinujnosde.titansbattle.managers.ChallengeManager;
import me.roinujnosde.titansbattle.managers.DatabaseManager;
import me.roinujnosde.titansbattle.types.Group;
import me.roinujnosde.titansbattle.types.Warrior;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.aikar.commands.BaseCommand;

import java.util.Objects;
import java.util.Set;

@CommandAlias("%titansbattle|tb")
@Subcommand("%challenge")
// TODO Permissions
public class ChallengeCommand extends BaseCommand {

    // TODO Arena context resolver
    // TODO Group context resolver
    // TODO Arenas completion
    // TODO Groups completion
    // TODO Warrior context resolver
    // TODO ChallengeRequest completions
    @Dependency
    private TitansBattle plugin;
    @Dependency
    private ChallengeManager challengeManager;
    @Dependency
    private DatabaseManager databaseManager;

    @Subcommand("%player")
    @CommandCompletion("@players @arenas")
    @Conditions("can_challenge:group=false")
    // TODO Condition: arena not in use (condition or @Values?)
    public void challengePlayer(Warrior challenger, OnlinePlayer target, ArenaConfiguration arena) {
        Challenge challenge = new Challenge(plugin, arena);
        Warrior challenged = databaseManager.getWarrior(target.player);
        WarriorChallengeRequest request = new WarriorChallengeRequest(challenge, challenger, challenged);

        challengeManager.addRequest(request);

        challenger.sendMessage(plugin.getLang("you.challenged.player", challenge, target.player.getName()));
        target.player.sendMessage(plugin.getLang("challenged.you", challenge, challenger.getName()));
        challenge.onJoin(challenger);
    }

    @Subcommand("%group")
    @CommandCompletion("@groups @arenas")
    @Conditions("can_challenge:group=true")
    // TODO Condition: arena not in use (condition or @Values?)
    public void challengeGroup(Warrior sender, Group target, ArenaConfiguration arena) {
        Challenge challenge = new Challenge(plugin, arena);
        Group challenger = Objects.requireNonNull(sender.getGroup());
        GroupChallengeRequest request = new GroupChallengeRequest(challenge, challenger, target);

        challengeManager.addRequest(request);

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

    @Subcommand("%accept")
    @CommandCompletion("@requests")
    public void accept(Player player, String challenger) {

    }

    @Subcommand("%deny")
    @CommandCompletion("@requests")
    public void deny( ) {

    }

    //TODO Shows info about an arena
    @Subcommand("%info")
    @CommandCompletion("@arenas")
    public void info(CommandSender sender, ArenaConfiguration arena) {

    }

    public void createArena() {}

}
