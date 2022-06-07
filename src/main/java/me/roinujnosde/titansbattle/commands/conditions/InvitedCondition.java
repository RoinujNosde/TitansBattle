package me.roinujnosde.titansbattle.commands.conditions;

import co.aikar.commands.*;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.Warrior;
import org.jetbrains.annotations.NotNull;

public class InvitedCondition extends AbstractParameterCondition<Warrior> {
    public InvitedCondition(TitansBattle plugin) {
        super(plugin);
    }

    @Override
    public @NotNull Class<Warrior> getType() {
        return Warrior.class;
    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> cc,
                                  BukkitCommandExecutionContext cec,
                                  Warrior v) throws InvalidCommandArgument {
        boolean invited = getChallengeManager().getRequests().stream().anyMatch(r -> r.isInvited(v));
        if (!invited) {
            cec.getIssuer().sendMessage(plugin.getLang("no.challenge.to.accept"));
            throw new ConditionFailedException();
        }
    }

    @Override
    public @NotNull String getId() {
        return "is_invited";
    }
}
