package me.roinujnosde.titansbattle.challenges;

import me.roinujnosde.titansbattle.BaseGame;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.Group;
import me.roinujnosde.titansbattle.types.Warrior;
import me.roinujnosde.titansbattle.utils.SoundUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

import static me.roinujnosde.titansbattle.utils.SoundUtils.Type.VICTORY;

public class Challenge extends BaseGame {

    private Group winnerGroup;
    private List<Warrior> winners;

    public Challenge(@NotNull TitansBattle plugin, @NotNull ArenaConfiguration config) {
        super(plugin, config);
    }

    @Override
    protected @Nullable String getLang(@NotNull String key) {
        String lang = null;
        if (!key.startsWith("challenge_")) {
            lang = super.getLang("challenge_" + key);
        }
        if (lang == null || lang.startsWith("<MISSING KEY:")) {
            lang = super.getLang(key);
        }
        return lang;
    }

    @Override
    protected void onLobbyEnd() {
        broadcastKey("game_started", getConfig().getPreparationTime());
        teleportAll(getConfig().getArena());
        startPreparation();
    }

    @Override
    protected void processRemainingPlayers(@NotNull Warrior warrior) {
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
        // TODO Increase stats
        super.finish(cancelled);
        plugin.getChallengeManager().remove(this);
        if (!cancelled) {
            String winnerName = getConfig().isGroupMode() ? winnerGroup.getName() : winners.get(0).getName();
            SoundUtils.playSound(VICTORY, plugin.getConfig(), winners);
            broadcastKey("who_won", winnerName);
        }
    }

    @Override
    public boolean isInBattle(@NotNull Warrior warrior) {
        return battle && participants.contains(warrior);
    }
}