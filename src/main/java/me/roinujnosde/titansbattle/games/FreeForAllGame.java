package me.roinujnosde.titansbattle.games;

import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.events.GroupWinEvent;
import me.roinujnosde.titansbattle.events.PlayerWinEvent;
import me.roinujnosde.titansbattle.managers.DatabaseManager;
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
                    winners = new ArrayList<>();
                    getPlayerParticipants().stream().filter(p -> g.isMember(p.getUniqueId())).forEach(winners::add);
                    getCasualties().stream().filter(p -> g.isMember(p.getUniqueId())).forEach(winners::add);
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
    }

    @Override
    protected void processWinners() {
        SoundUtils.playSound(VICTORY, plugin.getConfig(), winners);
        Winners today = databaseManager.getTodaysWinners();

        PlayerWinEvent event = new PlayerWinEvent(winners);
        Bukkit.getPluginManager().callEvent(event);
        if (killer != null) {
            plugin.getGameManager().setKiller(getConfig(), killer, null);
            SoundUtils.playSound(VICTORY, plugin.getConfig(), killer.toOnlinePlayer());
        }
        String gameName = getConfig().getName();
        if (winnerGroup != null) {
            Bukkit.getPluginManager().callEvent(new GroupWinEvent(winnerGroup));
            winnerGroup.getData().increaseVictories(gameName);
            today.setWinnerGroup(gameName, winnerGroup.getName());
        }
        today.setWinners(gameName, Helper.warriorListToUuidList(winners));
        String winnerName = getConfig().isGroupMode() ? winnerGroup.getName() : winners.get(0).getName();
        Bukkit.getServer().broadcastMessage(MessageFormat.format(plugin.getLang("who_won", this), winnerName));
        winners.forEach(w -> w.increaseVictories(gameName));
        if (getConfig().isUseKits()) {
            winners.forEach(Kit::clearInventory);
        }
        givePrizes(FIRST, winnerGroup, winners);
        givePrizes(KILLER, null, Collections.singletonList(killer));
    }

    @Override
    protected @NotNull String getGameInfoMessage() {
        String groupsText;
        GroupManager groupManager = plugin.getGroupManager();
        if (groupManager != null) {
            groupsText = groupManager.buildStringFrom(getGroupParticipants().keySet());
        } else {
            groupsText = "";
        }
        return MessageFormat.format(plugin.getLang("game_info", this),
                getPlayerParticipants().size(), getGroupParticipants().size(), groupsText);
    }
}
