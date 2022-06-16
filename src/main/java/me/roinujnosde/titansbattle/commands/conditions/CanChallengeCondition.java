package me.roinujnosde.titansbattle.commands.conditions;

import co.aikar.commands.*;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.challenges.ChallengeRequest;
import me.roinujnosde.titansbattle.types.Warrior;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CanChallengeCondition extends AbstractCommandCondition {

    public CanChallengeCondition(TitansBattle plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getId() {
        return "can_challenge";
    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> context) throws InvalidCommandArgument {
        Player player = context.getIssuer().getPlayer();
        if (player == null) {
            throw new ConditionFailedException(MessageKeys.NOT_ALLOWED_ON_CONSOLE);
        }
        Warrior warrior = plugin.getDatabaseManager().getWarrior(player);
        boolean groupMode = Boolean.parseBoolean(context.getConfigValue("group", "false"));

        if (groupMode && warrior.getGroup() == null) {
            player.sendMessage(plugin.getLang("you.must.be.in.a.group.to.challenge"));
            throw new ConditionFailedException();
        }

        ChallengeRequest<?> request = getChallengeManager().getChallengeRequest(warrior, groupMode);
        if (request != null) {
            player.sendMessage(plugin.getLang("already.challenged"));
            throw new ConditionFailedException();
        }
    }
}
