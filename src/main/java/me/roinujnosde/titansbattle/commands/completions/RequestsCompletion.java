package me.roinujnosde.titansbattle.commands.completions;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.InvalidCommandArgument;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.challenges.ChallengeRequest;
import me.roinujnosde.titansbattle.types.Warrior;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;

public class RequestsCompletion extends AbstractCompletion {
    public RequestsCompletion(TitansBattle plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getId() {
        return "requests";
    }

    @Override
    public Collection<String> getCompletions(BukkitCommandCompletionContext context) throws InvalidCommandArgument {
        Warrior warrior = getDatabaseManager().getWarrior(context.getIssuer().getUniqueId());
        return getChallengeManager().getRequests().stream().filter(cr -> cr.isInvited(warrior))
                .map(ChallengeRequest::getChallengerName).collect(Collectors.toList());
    }
}
