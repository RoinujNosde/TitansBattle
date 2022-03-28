package me.roinujnosde.titansbattle.commands.contexts;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.challenges.ChallengeRequest;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("rawtypes")
public class ChallengeRequestContext extends AbstractContextResolver<ChallengeRequest> {

    public ChallengeRequestContext(@NotNull TitansBattle plugin) {
        super(plugin);
    }

    @Override
    public @NotNull Class<ChallengeRequest> getType() {
        return ChallengeRequest.class;
    }

    @Override
    public ChallengeRequest getContext(BukkitCommandExecutionContext context) throws InvalidCommandArgument {
        String arg = context.popFirstArg();
        for (ChallengeRequest<?> request : getChallengeManager().getRequests()) {
            if (request.getChallengerName().equalsIgnoreCase(arg)) {
                return request;
            }
        }
        return null;
    }
}
