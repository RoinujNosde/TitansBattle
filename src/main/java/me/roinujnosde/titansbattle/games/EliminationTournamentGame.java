package me.roinujnosde.titansbattle.games;

import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.*;
import me.roinujnosde.titansbattle.types.GameConfiguration.Prize;
import me.roinujnosde.titansbattle.utils.Helper;
import me.roinujnosde.titansbattle.utils.SoundUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

public class EliminationTournamentGame extends Game {
    private final List<Duel<Warrior>> playerDuelists = new ArrayList<>();
    private final List<Duel<Group>> groupDuelists = new ArrayList<>();
    private final List<Warrior> waitingThirdPlace = new ArrayList<>();

    private @NotNull List<Warrior> firstPlaceWinners = new ArrayList<>();
    private @Nullable List<Warrior> secondPlaceWinners;
    private @Nullable List<Warrior> thirdPlaceWinners;

    private boolean nextToWinIsFirstWinner = false;
    private boolean nextToLoseIsThirdWinner = false;
    private boolean battleForThirdPlace = false;

    public EliminationTournamentGame(TitansBattle plugin, GameConfiguration config) {
        super(plugin, config);
    }

    @Override
    public boolean isInBattle(@NotNull Warrior warrior) {
        if (!battle) {
            return false;
        }
        return isCurrentDuelist(warrior);
    }

    private boolean isCurrentDuelist(@NotNull Warrior warrior) {
        if (!getConfig().isGroupMode()) {
            return playerDuelists.get(0).isDuelist(warrior);
        }
        return groupDuelists.get(0).isDuelist(warrior.getGroup());
    }

    private List<Warrior> getDuelLosers(@NotNull Warrior defeated) {
        Group group = defeated.getGroup();
        if (group != null && getConfig().isGroupMode()) {
            return casualties.stream().filter(p -> group.isMember(p.getUniqueId())).collect(Collectors.toList());
        }
        return Collections.singletonList(defeated);
    }

    private List<Warrior> getDuelWinners(@NotNull Warrior defeated) {
        List<Warrior> list = new ArrayList<>();
        if (getConfig().isGroupMode()) {
            Group winnerGroup = Objects.requireNonNull(groupDuelists.get(0).getOther(defeated.getGroup()));
            list = getPlayerParticipants().stream().filter(p -> winnerGroup.isMember(p.getUniqueId()))
                    .collect(Collectors.toList());
        } else {
            Warrior other = playerDuelists.get(0).getOther(defeated);
            list.add(other);
        }
        return list;
    }

    @Override
    public boolean isParticipant(@NotNull Warrior warrior) {
        return super.isParticipant(warrior) || waitingThirdPlace.contains(warrior);
    }

    private void removeDuelist(@NotNull Warrior warrior) {
        if (getConfig().isGroupMode()) {
            if (lost(warrior)) {
                groupDuelists.removeIf(duel -> duel.isDuelist(warrior.getGroup()));
            }
            return;
        }
        playerDuelists.removeIf(duel -> duel.isDuelist(warrior));
    }

    @Override
    protected void processRemainingPlayers(@NotNull Warrior warrior) {
        List<Warrior> duelLosers = getDuelLosers(warrior);
        if (!isCurrentDuelist(warrior)) {
            processNotCurrentDuelistLeaving(warrior, duelLosers);
            return;
        }
        if (isSemiFinals() && !battleForThirdPlace) {
            processLeavingDuringSemiFinals(warrior);
        }
        if (lost(warrior)) {
            processLoss(warrior, duelLosers);
        }
    }

    private void processLoss(@NotNull Warrior warrior, List<Warrior> duelLosers) {
        battle = false;
        List<Warrior> duelWinners = getDuelWinners(warrior);
        if (nextToLoseIsThirdWinner) {
            thirdPlaceWinners = duelLosers;
        }
        if (!battleForThirdPlace) {
            for (Warrior dw : duelWinners) {
                setKit(dw);
            }
            teleport(duelWinners, getConfig().getLobby());
        } else {
            battleForThirdPlace = false;
            thirdPlaceWinners = duelWinners;
            teleport(duelWinners, getConfig().getWatchroom());
            playerParticipants.removeIf(thirdPlaceWinners::contains);
            if (getConfig().isUseKits()) {
                thirdPlaceWinners.forEach(Kit::clearInventory);
            }
        }
        if (nextToWinIsFirstWinner) {
            firstPlaceWinners = duelWinners;
        } else if (getPlayerOrGroupCount() == 1) {
            firstPlaceWinners = duelWinners;
            secondPlaceWinners = duelLosers;
        }
        startNextDuel();
    }

    private void processLeavingDuringSemiFinals(@NotNull Warrior warrior) {
        Player player = Objects.requireNonNull(warrior.toOnlinePlayer());
        waitingThirdPlace.add(warrior);
        player.spigot().respawn();
    }

    private void processNotCurrentDuelistLeaving(@NotNull Warrior warrior, List<Warrior> duelLosers) {
        removeDuelist(warrior);
        if (getPlayerOrGroupCount() == 3) {
            if (!waitingThirdPlace.remove(warrior)) {
                if (lost(warrior)) {
                    secondPlaceWinners = duelLosers;
                    nextToWinIsFirstWinner = true;
                }
                return;
            }
            if (waitingThirdPlace.size() == 0) {
                nextToLoseIsThirdWinner = true;
            }
        }
    }

    private boolean lost(@NotNull Warrior warrior) {
        if (getConfig().isGroupMode()) {
            return !getGroupParticipants().containsKey(warrior.getGroup());
        }
        return true;
    }

    @Override
    public void onRespawn(@NotNull Warrior warrior) {
        if (waitingThirdPlace.contains(warrior)) {
            Player player = warrior.toOnlinePlayer();
            if (player == null) return;
            setKit(warrior);
            teleport(warrior, getConfig().getLobby());
        } else if (casualties.contains(warrior)) {
            teleport(warrior, getConfig().getWatchroom());
        }
    }

    @Override
    public boolean shouldKeepInventoryOnDeath(@NotNull Warrior warrior) {
        return isSemiFinals() && isCurrentDuelist(warrior) && !battleForThirdPlace;
    }

    public boolean isSemiFinals() {
        return getPlayerOrGroupCount() > 2 && getPlayerOrGroupCount() < 5;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isPowerOfTwo(int x) {
        return x > 0 && (x & (x - 1)) == 0;
    }

    @Override
    protected void onLobbyEnd() {
        if (!isPowerOfTwo(getPlayerOrGroupCount())) {
            kickExcessiveParticipants();
        }
        startNextDuel();
    }

    private void kickExcessiveParticipants() {
        if (getConfig().isGroupMode()) {
            kickExcessiveGroups();
        } else {
            kickExcessivePlayers();
        }
    }

    private void kickExcessivePlayers() {
        Set<Warrior> toKick = new HashSet<>();
        for (int i = playerParticipants.size(); i > 2; i--) {
            if (!isPowerOfTwo(i)) {
                toKick.add(playerParticipants.get(i - 1));
                continue;
            }
            break;
        }
        kickExcessive(toKick);
    }

    private void kickExcessiveGroups() {
        Set<Group> toKick = new HashSet<>();
        List<Group> groups = new ArrayList<>(getGroupParticipants().keySet());
        for (int i = groups.size(); i > 2; i--) {
            if (!isPowerOfTwo(i)) {
                toKick.add(groups.get(i - 1));
                continue;
            }
            break;
        }
        for (Group group : toKick) {
            kickExcessive(playerParticipants.stream().filter(group::isMember).collect(Collectors.toSet()));
        }
    }

    private void kickExcessive(@NotNull Set<Warrior> warriors) {
        playerParticipants.removeIf(warriors::contains);
        Set<Player> players = warriors.stream().map(Warrior::toOnlinePlayer).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        teleport(warriors, getConfig().getWatchroom());
        players.forEach(player -> {
            player.sendMessage(plugin.getLang("kicked_to_adjust_duels", this));
            if (getConfig().isUseKits()) {
                Kit.clearInventory(player);
            }
        });
    }

    private long getWaitingThirdPlaceCount() {
        long count;
        if (getConfig().isGroupMode()) {
            count = getWaitingThirdPlaceGroups().size();
        } else {
            count = waitingThirdPlace.size();
        }
        return count;
    }

    @NotNull
    private List<Group> getWaitingThirdPlaceGroups() {
        return waitingThirdPlace.stream().map(Warrior::getGroup).distinct().collect(Collectors.toList());
    }

    private void generateDuelists() {
        if (getWaitingThirdPlaceCount() == 2) {
            gameManager.broadcastKey("battle_for_third_place", this);
            playerParticipants.addAll(waitingThirdPlace);
            if (getConfig().isGroupMode()) {
                generateDuelist(getWaitingThirdPlaceGroups(), groupDuelists);
            } else {
                generateDuelist(waitingThirdPlace, playerDuelists);
            }
            waitingThirdPlace.clear();
            battleForThirdPlace = true;
            return;
        }
        if (!getConfig().isGroupMode()) {
            generateDuelist(playerParticipants, playerDuelists);
        } else {
            ArrayList<Group> groups = new ArrayList<>(getGroupParticipants().keySet());
            generateDuelist(groups, groupDuelists);
        }
    }

    private <T> void generateDuelist(List<T> list, List<Duel<T>> duelList) {
        if (duelList.size() >= 1) {
            duelList.remove(0);
        }
        if (duelList.size() < 1) {
            Collections.shuffle(list);
            duelList.clear();
            for (int i = 0; i < list.size(); i = i + 2) {
                duelList.add(new Duel<>(list.get(i), list.get(i + 1)));
            }
        }
    }

    private void startNextDuel() {
        int count = getPlayerOrGroupCount();
        if (count <= 1) {
            finish(false);
            return;
        }
        if (count == 2) {
            gameManager.broadcastKey("final_battle", this);
        }
        generateDuelists();
        teleportNextDuelists();
        startPreparationTask();
    }

    private int getPlayerOrGroupCount() {
        int participants;
        if (getConfig().isGroupMode()) {
            participants = getGroupParticipants().size();
        } else {
            participants = playerParticipants.size();
        }
        return participants;
    }

    private void teleportNextDuelists() {
        if (!getConfig().isGroupMode()) {
            for (Warrior w : playerDuelists.get(0).getDuelists()) {
                teleport(w, getConfig().getArena());
            }
        } else {
            List<Group> duelists = groupDuelists.get(0).getDuelists();
            getPlayerParticipants().stream().filter(player -> {
                for (Group g : duelists) {
                    if (g.isMember(player.getUniqueId())) {
                        return true;
                    }
                }
                return false;
            }).forEach(p -> teleport(p, getConfig().getArena()));
        }
    }

    private @Nullable Group getAnyGroup(@Nullable List<Warrior> warriors) {
        if (warriors != null && getConfig().isGroupMode()) {
            for (Warrior warrior : warriors) {
                Group group = warrior.getGroup();
                if (group != null) {
                    return group;
                }
            }
        }
        return null;
    }

    @NotNull
    private String getWinnerName(@Nullable List<Warrior> warriors) {
        String name = plugin.getLang("no_winner_tournament", this);
        if (getConfig().isGroupMode()) {
            Group group = getAnyGroup(warriors);
            if (group != null) {
                name = group.getName();
            }
        } else if (warriors != null && warriors.size() > 0) {
            name = warriors.get(0).getName();
        }
        return name;
    }

    @Override
    protected void processWinners() {
        Winners todaysWinners = databaseManager.getTodaysWinners();

        todaysWinners.setWinners(getConfig().getName(), Helper.warriorListToUuidList(firstPlaceWinners));
        Group firstGroup = getAnyGroup(firstPlaceWinners);
        if (getConfig().isGroupMode() && firstGroup != null) {
            casualties.stream().filter(firstGroup::isMember).forEach(firstPlaceWinners::add);
            todaysWinners.setWinnerGroup(getConfig().getName(), firstGroup.getName());
        }
        if (getConfig().isUseKits()) {
            firstPlaceWinners.forEach(Kit::clearInventory);
        }
        givePrizes(Prize.FIRST, firstGroup, firstPlaceWinners);
        givePrizes(Prize.SECOND, getAnyGroup(secondPlaceWinners), secondPlaceWinners);
        givePrizes(Prize.THIRD, getAnyGroup(thirdPlaceWinners), thirdPlaceWinners);
        SoundUtils.playSound(SoundUtils.Type.VICTORY, plugin.getConfig(), firstPlaceWinners, secondPlaceWinners,
                thirdPlaceWinners);
        Warrior killer = findKiller();
        if (killer != null) {
            givePrizes(Prize.KILLER, null, Collections.singletonList(killer));
            gameManager.setKiller(getConfig(), killer, null);
            SoundUtils.playSound(SoundUtils.Type.VICTORY, plugin.getConfig(), killer.toOnlinePlayer());
            todaysWinners.setKiller(getConfig().getName(), killer.getUniqueId());
        }
        gameManager.broadcastKey("who_won_tournament", this, getWinnerName(firstPlaceWinners),
                getWinnerName(secondPlaceWinners), getWinnerName(thirdPlaceWinners));
        firstPlaceWinners.forEach(warrior -> warrior.increaseVictories(getConfig().getName()));
    }

    @Override
    protected @NotNull String getGameInfoMessage() {
        String gameInfo = plugin.getLang("game_info_duels", this);
        String nextDuelsLineMessage = plugin.getLang("game_info_duels_line", this);
        String[] firstDuel;
        StringBuilder builder = new StringBuilder();
        if (getConfig().isGroupMode()) {
            firstDuel = groupDuelistsToNameArray(0);
            if (groupDuelists.size() > 1) {
                for (int i = 1; i < groupDuelists.size(); i++) {
                    builder.append(MessageFormat.format(nextDuelsLineMessage, i + 1, groupDuelistsToNameArray(i)));
                }
            }
        } else {
            firstDuel = playerDuelistsToNameArray(0);
            if (playerDuelists.size() > 1) {
                for (int i = 1; i < playerDuelists.size(); i++) {
                    builder.append(MessageFormat.format(nextDuelsLineMessage, i + 1, playerDuelistsToNameArray(i)));
                }
            }
        }

        return MessageFormat.format(gameInfo, firstDuel, builder.toString());
    }

    @NotNull
    private String[] playerDuelistsToNameArray(int index) {
        return playerDuelists.get(index).getDuelists().stream().map(Warrior::getName).toArray(String[]::new);
    }

    @NotNull
    private String[] groupDuelistsToNameArray(int index) {
        return groupDuelists.get(index).getDuelists().stream().map(Group::getName).toArray(String[]::new);
    }
}
