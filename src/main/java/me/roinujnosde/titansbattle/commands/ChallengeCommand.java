package me.roinujnosde.titansbattle.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import me.roinujnosde.titansbattle.challenges.ArenaConfiguration;
import me.roinujnosde.titansbattle.types.Group;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.Subcommand;

@CommandAlias("%titansbattle|tb")
@Subcommand("%challenge")
// TODO Permissions
public class ChallengeCommand extends BaseCommand {

    // TODO Arena context resolver
    // TODO Group context resolver
    // TODO Arenas completion
    // TODO Groups completion
    // TODO ChallengeRequest completions

    @Subcommand("%player")
    @CommandCompletion("@players @arenas")
    public void challengePlayer(Player player, OnlinePlayer target, ArenaConfiguration arena) {

    }

    @Subcommand("%group")
    @CommandCompletion("@groups @arenas")
    public void challengeGroup(Player player, Group group, ArenaConfiguration arena) {
        // TODO Different group
        // TODO Online group
    }

    @Subcommand("%accept")
    @CommandCompletion("@requests")
    public void accept(Player player) {

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
