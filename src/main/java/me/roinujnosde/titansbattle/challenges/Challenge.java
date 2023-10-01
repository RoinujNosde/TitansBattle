package me.roinujnosde.titansbattle.challenges;

import me.roinujnosde.titansbattle.BaseGame;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.events.GroupWinEvent;
import me.roinujnosde.titansbattle.events.PlayerWinEvent;
import me.roinujnosde.titansbattle.types.Group;
import me.roinujnosde.titansbattle.types.Warrior;
import me.roinujnosde.titansbattle.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static me.roinujnosde.titansbattle.BaseGameConfiguration.Prize.FIRST;
import static me.roinujnosde.titansbattle.utils.SoundUtils.Type.VICTORY;

public class Challenge extends BaseGame {

    private Group winnerGroup;
    private List<Warrior> winners = new ArrayList<>();
    private Warrior lastCasualty;
    
    public Challenge(@NotNull TitansBattle plugin, @NotNull ArenaConfiguration config) {
        super(plugin, config);
    }

    @Override
    public @NotNull String getLang(@NotNull String key, Object... args) {
        String lang = null;
        if (!key.startsWith("challenge_")) {
            lang = super.getLang("challenge_" + key, args);
        }
        if (lang == null || lang.startsWith("<MISSING KEY:")) {
            lang = super.getLang(key, args);
        }
        return lang;
    }

    @Override
    protected void onLobbyEnd() {
        broadcastKey("game_started", getConfig().getPreparationTime());
        teleportToArena(getParticipants());
        startPreparation();
    }

    @Override
    protected void processRemainingPlayers(@NotNull Warrior warrior) {
    	lastCasualty = warrior;
        if (getConfig().isGroupMode()) {
            if (getGroupParticipants().size() == 1) {
                getGroupParticipants().keySet().stream().findAny().ifPresent(g -> {
                    winnerGroup = g;
                    getParticipants().stream().filter(p -> g.isMember(p.getUniqueId())).forEach(winners::add);
                });
                finish(false);
            }
        } else if (participants.size() == 1) {
            winners = getParticipants();
            finish(false);
        }
    }

    @NotNull
    @Override
    public ArenaConfiguration getConfig() {
        return (ArenaConfiguration) config;
    }

    @Override
    public boolean shouldClearDropsOnDeath(@NotNull Warrior warrior) {
        return isParticipant(warrior) && config.isClearItemsOnDeath();
    }

    @Override
    public boolean shouldKeepInventoryOnDeath(@NotNull Warrior warrior) {
        return false;
    }

    @Override
    public @NotNull Collection<Warrior> getCurrentFighters() {
        return participants;
    }

    @Override
    public void finish(boolean cancelled) {
        super.finish(cancelled);
        plugin.getChallengeManager().remove(this);
    }

    @Override
    protected void processWinners() {
        if (winnerGroup != null) {
            Bukkit.getPluginManager().callEvent(new GroupWinEvent(winnerGroup));
            getCasualties().stream().filter(p -> winnerGroup.isMember(p.getUniqueId())).forEach(winners::add);
        }
        PlayerWinEvent event = new PlayerWinEvent(this, winners);
        Bukkit.getPluginManager().callEvent(event);
        String winnerName = getConfig().isGroupMode() ? winnerGroup.getName() : winners.get(0).getName();
        SoundUtils.playSound(VICTORY, plugin.getConfig(), winners);
        givePrizes(FIRST, winnerGroup, winners);
        broadcastKey("who_won", winnerName, getLoserName());
        discordAnnounce("discord_who_won", winnerName, getLoserName());
    }

    @Override
    public void setWinner(@NotNull Warrior warrior) {
        if (!isParticipant(warrior)) {
            return;
        }
        if (getConfig().isGroupMode()) {
            winnerGroup = getGroup(warrior);
            winners = getParticipants().stream().filter(p -> winnerGroup.isMember(p.getUniqueId())).collect(Collectors.toList());
        } else {
            winners.add(warrior);
        }
        finish(false);
    }

    @Override
    public boolean isInBattle(@NotNull Warrior warrior) {
        return battle && participants.contains(warrior);
    }

    private String getLoserName() {
        if (!getConfig().isGroupMode()) {
            return lastCasualty.getName();
        }
        
        return getGroup(lastCasualty).getName();
    }
}
