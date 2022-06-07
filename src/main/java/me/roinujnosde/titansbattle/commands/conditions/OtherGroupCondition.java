package me.roinujnosde.titansbattle.commands.conditions;

import co.aikar.commands.*;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.Group;
import org.jetbrains.annotations.NotNull;

public class OtherGroupCondition extends AbstractParameterCondition<Group> {

    public OtherGroupCondition(TitansBattle plugin) {
        super(plugin);
    }

    @Override
    public @NotNull Class<Group> getType() {
        return Group.class;
    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> cc,
                                  BukkitCommandExecutionContext cec,
                                  Group v) throws InvalidCommandArgument {
        if (v.isMember(cc.getIssuer().getUniqueId())) {
            cec.getIssuer().sendMessage(plugin.getLang("you.cannot.challenge.your.group"));
            throw new ConditionFailedException();
        }
    }

    @Override
    public @NotNull String getId() {
        return "other";
    }
}
