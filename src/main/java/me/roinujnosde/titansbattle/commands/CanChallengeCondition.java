package me.roinujnosde.titansbattle.commands;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.CommandConditions.Condition;
import co.aikar.commands.ConditionContext;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.challenges.ChallengeRequest;
import me.roinujnosde.titansbattle.managers.ChallengeManager;
import me.roinujnosde.titansbattle.types.Warrior;
import org.bukkit.entity.Player;

public class CanChallengeCondition implements Condition<BukkitCommandIssuer> {

    private final TitansBattle plugin;
    private final ChallengeManager challengeManager;

    public CanChallengeCondition(TitansBattle plugin) {
        this.plugin = plugin;
        this.challengeManager = plugin.getChallengeManager();
    }

    @Override
    public void validateCondition(ConditionContext<BukkitCommandIssuer> context) throws InvalidCommandArgument {
        Player player = context.getIssuer().getPlayer();
        Warrior warrior = plugin.getDatabaseManager().getWarrior(player);
        boolean groupMode = Boolean.parseBoolean(context.getConfigValue("group", "false"));

        if (groupMode && warrior.getGroup() == null) {
            player.sendMessage(plugin.getLang("you.must.be.in.a.group.to.challenge"));
            throw new ConditionFailedException();
        }

        ChallengeRequest<?> request = challengeManager.getChallengeRequest(warrior, groupMode);
        if (request != null) {
            player.sendMessage(plugin.getLang("already.challenged"));
            throw new ConditionFailedException();
        }
    }
}
