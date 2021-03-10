package me.roinujnosde.titansbattle.games;

import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.events.GroupWinEvent;
import me.roinujnosde.titansbattle.events.PlayerWinEvent;
import me.roinujnosde.titansbattle.managers.GroupManager;
import me.roinujnosde.titansbattle.types.*;
import me.roinujnosde.titansbattle.utils.Helper;
import me.roinujnosde.titansbattle.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.roinujnosde.titansbattle.types.GameConfiguration.Prize.FIRST;
import static me.roinujnosde.titansbattle.types.GameConfiguration.Prize.KILLER;
import static me.roinujnosde.titansbattle.utils.SoundUtils.Type.VICTORY;

public class FreeForAllGame extends Game {

    private @Nullable Group winnerGroup;
    private @Nullable Warrior killer;
    private @NotNull List<Warrior> winners = new ArrayList<>();

    public FreeForAllGame(TitansBattle plugin, GameConfiguration config) {
        super(plugin, config);
    }

    @Override
    public boolean isInBattle(@NotNull Warrior warrior) {
        return battle && playerParticipants.contains(warrior);
    }

    @Override
    protected void processRemainingPlayers(@NotNull Warrior warrior) {
        if (getConfig().isGroupMode()) {
            if (getGroupParticipants().size() == 1) {
                killer = findKiller();
                getGroupParticipants().keySet().stream().findAny().ifPresent(g -> {
                    winnerGroup = g;
                    getPlayerParticipants().stream().filter(p -> g.isMember(p.getUniqueId())).forEach(winners::add);
                });
                finish(false);
            }
        } else if (playerParticipants.size() == 1) {
            killer = findKiller();
            winners = getPlayerParticipants();
            finish(false);
        }
    }

    @Override
    protected void onLobbyEnd() {
        Bukkit.getServer().broadcastMessage(MessageFormat.format(plugin.getLang("game_started", this),
                getConfig().getPreparationTime()));
        teleportAll(getConfig().getArena());
        startPreparationTask();
        startCountdownTitleTask(getPlayerParticipants());
    }

    @Override
    protected void processWinners() {
        String gameName = getConfig().getName();
        Winners today = databaseManager.getTodaysWinners();
        if (getConfig().isUseKits()) {
            winners.forEach(Kit::clearInventory);
        }
        if (winnerGroup != null) {
            Bukkit.getPluginManager().callEvent(new GroupWinEvent(winnerGroup));
            winnerGroup.getData().increaseVictories(gameName);
            today.setWinnerGroup(gameName, winnerGroup.getName());
            getCasualties().stream().filter(p -> winnerGroup.isMember(p.getUniqueId())).forEach(winners::add);
        }
        SoundUtils.playSound(VICTORY, plugin.getConfig(), winners);
        PlayerWinEvent event = new PlayerWinEvent(winners);
        Bukkit.getPluginManager().callEvent(event);
        if (killer != null) {
            plugin.getGameManager().setKiller(getConfig(), killer, null);
            SoundUtils.playSound(VICTORY, plugin.getConfig(), killer.toOnlinePlayer());
            givePrizes(KILLER, null, Collections.singletonList(killer));
        }
        today.setWinners(gameName, Helper.warriorListToUuidList(winners));
        String winnerName = getConfig().isGroupMode() ? winnerGroup.getName() : winners.get(0).getName();
        Bukkit.getServer().broadcastMessage(MessageFormat.format(plugin.getLang("who_won", this), winnerName));
        winners.forEach(w -> w.increaseVictories(gameName));
        givePrizes(FIRST, winnerGroup, winners);
    }

    @Override
    protected @NotNull String getGameInfoMessage() {
        String groupsText = "";
        GroupManager groupManager = plugin.getGroupManager();
        if (groupManager != null && getConfig().isGroupMode()) {
            groupsText = groupManager.buildStringFrom(getGroupParticipants().keySet());
        }
        return MessageFormat.format(plugin.getLang("game_info", this),
                getPlayerParticipants().size(), getGroupParticipants().size(), groupsText);
    }
}
