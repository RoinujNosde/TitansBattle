package me.roinujnosde.titansbattle.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.challenges.*;
import me.roinujnosde.titansbattle.dao.ConfigurationDao;
import me.roinujnosde.titansbattle.managers.ChallengeManager;
import me.roinujnosde.titansbattle.managers.DatabaseManager;
import me.roinujnosde.titansbattle.types.Group;
import me.roinujnosde.titansbattle.types.Warrior;

import java.util.List;
import java.util.Objects;
import java.util.Set;

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
        //noinspection ConstantConditions
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
    public void accept(@Conditions("is_invited") Warrior warrior, @Optional @Values("@requests") ChallengeRequest<?> challenger) {
        if (challenger == null) {
            List<ChallengeRequest<?>> requests = challengeManager.getRequestsByInvited(warrior);
            requests.get(requests.size() - 1).getChallenge().onJoin(warrior);
            return;
        }
        challenger.getChallenge().onJoin(warrior);
    }

}
