package me.roinujnosde.titansbattle.games;

import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.events.GroupWinEvent;
import me.roinujnosde.titansbattle.events.PlayerWinEvent;
import me.roinujnosde.titansbattle.exceptions.CommandNotSupportedException;
import me.roinujnosde.titansbattle.types.*;
import me.roinujnosde.titansbattle.utils.Helper;
import me.roinujnosde.titansbattle.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static me.roinujnosde.titansbattle.BaseGameConfiguration.Prize.*;

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
            return playerDuelists.size() != 0 && playerDuelists.get(0).isDuelist(warrior);
        }
        return groupDuelists.size() != 0 && groupDuelists.get(0).isDuelist(getGroup(warrior));
    }

    private List<Warrior> getDuelLosers(@NotNull Warrior defeated) {
        Group group = getGroup(defeated);
        if (group != null && getConfig().isGroupMode()) {
            return casualties.stream().filter(p -> group.isMember(p.getUniqueId())).collect(Collectors.toList());
        }
        return Collections.singletonList(defeated);
    }

    private List<Warrior> getDuelWinners(@NotNull Warrior defeated) {
        List<Warrior> list = new ArrayList<>();
        if (getConfig().isGroupMode()) {
            Group winnerGroup = Objects.requireNonNull(groupDuelists.get(0).getOther(getGroup(defeated)));
            list = getParticipants().stream().filter(p -> winnerGroup.isMember(p.getUniqueId()))
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
                groupDuelists.removeIf(duel -> duel.isDuelist(getGroup(warrior)));
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
        if (isSemiFinals(false) && !battleForThirdPlace) {
            processLeavingDuringSemiFinals(warrior);
        }
        if (lost(warrior)) {
            processLoss(warrior, duelLosers);
        }
    }

    private void processLoss(@NotNull Warrior warrior, List<Warrior> duelLosers) {
        battle = false;
        List<Warrior> duelWinners = getDuelWinners(warrior);
        heal(duelWinners);
        if (nextToLoseIsThirdWinner) {
            thirdPlaceWinners = duelLosers;
        }
        if (!battleForThirdPlace) {
            for (Warrior dw : duelWinners) {
                setKit(dw);
            }
            teleport(duelWinners, getConfig().getLobby());
        } else {
            processThirdPlaceBattle(duelWinners);
        }
        if (nextToWinIsFirstWinner) {
            firstPlaceWinners = duelWinners;
        } else if (getPlayerOrGroupCount() == 1) {
            firstPlaceWinners = duelWinners;
            secondPlaceWinners = duelLosers;
        } else {
            runCommandsAfterBattle(duelWinners);
        }
        //delaying the next duel, so there is time for other players to respawn
        Bukkit.getScheduler().runTaskLater(plugin, this::startNextDuel, 20L);
    }

    private void processThirdPlaceBattle(List<Warrior> duelWinners) {
        battleForThirdPlace = false;
        thirdPlaceWinners = duelWinners;
        teleport(duelWinners, getConfig().getWatchroom());
        participants.removeIf(thirdPlaceWinners::contains);
        if (getConfig().isUseKits()) {
            thirdPlaceWinners.forEach(Kit::clearInventory);
        }
    }

    private void heal(List<Warrior> warriors) {
        warriors.stream().map(Warrior::toOnlinePlayer).filter(Objects::nonNull).forEach(player -> {
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            player.setFireTicks(0);
        });
    }

    private void processLeavingDuringSemiFinals(@NotNull Warrior warrior) {
        Player player = warrior.toOnlinePlayer();
        if (player == null) return;

        waitingThirdPlace.add(warrior);
        Bukkit.getScheduler().runTaskLater(plugin, () -> player.spigot().respawn(), 1L);
    }

    private void processNotCurrentDuelistLeaving(@NotNull Warrior warrior, List<Warrior> duelLosers) {
        removeDuelist(warrior);
        if (getPlayerOrGroupCount() == 2 && getWaitingThirdPlaceCount() == 1) {
            thirdPlaceWinners = new ArrayList<>(waitingThirdPlace);
            waitingThirdPlace.clear();
        }
        if (getPlayerOrGroupCount() == 3) {
            if (!waitingThirdPlace.remove(warrior)) {
                if (lost(warrior)) {
                    secondPlaceWinners = duelLosers;
                    nextToWinIsFirstWinner = true;
                }
                return;
            }
            if (getWaitingThirdPlaceCount() == 0) {
                nextToLoseIsThirdWinner = true;
            }
        }
    }

    private boolean lost(@NotNull Warrior warrior) {
        if (getConfig().isGroupMode()) {
            return !getGroupParticipants().containsKey(getGroup(warrior));
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
        } else {
            super.onRespawn(warrior);
        }
    }

    @Override
    public boolean shouldKeepInventoryOnDeath(@NotNull Warrior warrior) {
        if (!isCurrentDuelist(warrior)) {
            return false;
        }
        return isSemiFinals(true) && !battleForThirdPlace;
    }

    private boolean isSemiFinals(boolean deathEvent) {
        // during the DeathEvent, the size of the participants list is unaltered, but after that, it is reduced by 1,
        // so the offset is needed to counterbalance
        int offset = deathEvent ? 0 : 1;
        return (getWaitingThirdPlaceCount() == 0 && getPlayerOrGroupCount() == 4 - offset) ||
                (getWaitingThirdPlaceCount() == 1 && getPlayerOrGroupCount() == 3 - offset);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isPowerOfTwo(int x) {
        return x > 0 && (x & (x - 1)) == 0;
    }

    @Override
    protected void onLobbyEnd() {
        super.onLobbyEnd();
        if (getConfig().isPowerOfTwo() && !isPowerOfTwo(getPlayerOrGroupCount())) {
            kickExcessiveParticipants();
        }
        startNextDuel();
        broadcast(getGameInfoMessage());
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
        for (int i = participants.size(); i > 2; i--) {
            if (!isPowerOfTwo(i)) {
                toKick.add(participants.get(i - 1));
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
            kickExcessive(participants.stream().filter(group::isMember).collect(Collectors.toSet()));
        }
    }

    private void kickExcessive(@NotNull Set<Warrior> warriors) {
        participants.removeIf(warriors::contains);
        Set<Player> players = warriors.stream().map(Warrior::toOnlinePlayer).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        teleport(warriors, getConfig().getWatchroom());
        players.forEach(player -> {
            player.sendMessage(getLang("kicked_to_adjust_duels"));
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
        return waitingThirdPlace.stream().map(this::getGroup).distinct().collect(Collectors.toList());
    }

    private void generateDuelists() {
        if (getWaitingThirdPlaceCount() == 2) {
            broadcastKey("battle_for_third_place");
            participants.addAll(waitingThirdPlace);
            if (getConfig().isGroupMode()) {
                generateDuelist(getWaitingThirdPlaceGroups(), groupDuelists);
            } else {
                generateDuelist(waitingThirdPlace, playerDuelists);
            }
            waitingThirdPlace.clear();
            battleForThirdPlace = true;
            return;
        }
        if (getPlayerOrGroupCount() == 2) {
            broadcastKey("final_battle");
        }
        if (!getConfig().isGroupMode()) {
            generateDuelist(participants, playerDuelists);
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
            for (int i = 0; i + 1 < list.size(); i = i + 2) {
                duelList.add(new Duel<>(list.get(i), list.get(i + 1)));
            }
        }
    }

    private void startNextDuel() {
        if (getPlayerOrGroupCount() <= 1) {
            finish(false);
            return;
        }
        generateDuelists();
        teleportNextDuelists();
        informOtherDuelists();
        startPreparation();
    }

    private int getPlayerOrGroupCount() {
        int participants;
        if (getConfig().isGroupMode()) {
            participants = getGroupParticipants().size();
        } else {
            participants = this.participants.size();
        }
        return participants;
    }

    @Override
    public @NotNull Collection<Warrior> getCurrentFighters() {
        return getParticipants().stream().filter(this::isCurrentDuelist).collect(Collectors.toList());
    }

    private void informOtherDuelists() {
        String message = getLang("wait_for_your_turn");
        Consumer<Warrior> sendMessage = warrior -> {
            if (!isCurrentDuelist(warrior)) {
                warrior.sendMessage(message);
            }
        };
        getParticipants().forEach(sendMessage);
        waitingThirdPlace.forEach(sendMessage);
    }

    private void teleportNextDuelists() {
        if (!getConfig().isGroupMode()) {
            teleportToArena(playerDuelists.get(0).getDuelists());
        } else {
            List<Group> duelists = groupDuelists.get(0).getDuelists();
            List<Warrior> warriors = getParticipants().stream().filter(player -> {
                for (Group g : duelists) {
                    if (g.isMember(player.getUniqueId())) {
                        return true;
                    }
                }
                return false;
            }).collect(Collectors.toList());
            teleportToArena(warriors);
        }
    }

    private @Nullable Group getAnyGroup(@Nullable List<Warrior> warriors) {
        if (warriors != null && getConfig().isGroupMode()) {
            for (Warrior warrior : warriors) {
                Group group = getGroup(warrior);
                if (group != null) {
                    return group;
                }
            }
        }
        return null;
    }

    @NotNull
    private String getWinnerName(@Nullable List<Warrior> warriors) {
        String name = getLang("no_winner_tournament");
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
        Winners todayWinners = databaseManager.getTodaysWinners();

        Group firstGroup = getAnyGroup(firstPlaceWinners);
        //we must clear the inventory before adding the casualties, otherwise the already dead would lose their items again
        if (getConfig().isUseKits()) {
            firstPlaceWinners.forEach(Kit::clearInventory);
        }
        if (getConfig().isGroupMode() && firstGroup != null) {
            casualties.stream().filter(firstGroup::isMember).forEach(firstPlaceWinners::add);
            firstPlaceWinners = firstPlaceWinners.stream().distinct().collect(Collectors.toList());
            todayWinners.setWinnerGroup(getConfig().getName(), firstGroup.getName());
            GroupWinEvent event = new GroupWinEvent(firstGroup);
            Bukkit.getPluginManager().callEvent(event);
        }
        PlayerWinEvent event = new PlayerWinEvent(this, firstPlaceWinners);
        Bukkit.getPluginManager().callEvent(event);
        todayWinners.setWinners(getConfig().getName(), Helper.warriorListToUuidList(firstPlaceWinners));
        givePrizes(FIRST, firstGroup, firstPlaceWinners);
        givePrizes(SECOND, getAnyGroup(secondPlaceWinners), secondPlaceWinners);
        givePrizes(THIRD, getAnyGroup(thirdPlaceWinners), thirdPlaceWinners);
        SoundUtils.playSound(SoundUtils.Type.VICTORY, plugin.getConfig(), firstPlaceWinners, secondPlaceWinners,
                thirdPlaceWinners);
        Warrior killer = findKiller();
        if (killer != null) {
            givePrizes(KILLER, null, Collections.singletonList(killer));
            gameManager.setKiller(getConfig(), killer, null);
            SoundUtils.playSound(SoundUtils.Type.VICTORY, plugin.getConfig(), killer.toOnlinePlayer());
            discordAnnounce("discord_who_won_killer", killer.getName(), killsCount.get(killer));
            todayWinners.setKiller(getConfig().getName(), killer.getUniqueId());
        }
        broadcastKey("who_won_tournament", getWinnerName(firstPlaceWinners),
                getWinnerName(secondPlaceWinners), getWinnerName(thirdPlaceWinners));
        discordAnnounce("discord_who_won_tournament", getWinnerName(firstPlaceWinners),
                getWinnerName(secondPlaceWinners), getWinnerName(thirdPlaceWinners));
        firstPlaceWinners.forEach(warrior -> warrior.increaseVictories(getConfig().getName()));
    }

    @Override
    public void setWinner(@NotNull Warrior warrior) throws CommandNotSupportedException {
        throw new CommandNotSupportedException();
    }

    @Override
    protected @NotNull String getGameInfoMessage() {
        String gameInfo = getLang("game_info_duels");
        String nextDuels = getLang("game_info_next_duels");
        String[] firstDuel;
        StringBuilder builder = new StringBuilder();
        if (getConfig().isGroupMode()) {
            firstDuel = duelistsToNameArray(0, groupDuelists, Group::getName);
            populateDuelsMessage(builder, groupDuelists, Group::getName);
        } else {
            firstDuel = duelistsToNameArray(0, playerDuelists, Warrior::getName);
            populateDuelsMessage(builder, playerDuelists, Warrior::getName);
        }
        if (isMultipleDuels()) {
            gameInfo = gameInfo + nextDuels;
        }

        return MessageFormat.format(gameInfo, firstDuel[0], firstDuel[1], builder.toString());
    }

    private <D> void populateDuelsMessage(StringBuilder builder, List<Duel<D>> list, Function<D, String> getName) {
        String nextDuelsLineMessage = getLang("game_info_duels_line");
        if (list.size() > 1) {
            for (int i = 1; i < list.size(); i++) {
                @NotNull String[] name = duelistsToNameArray(i, list, getName);
                builder.append(MessageFormat.format(nextDuelsLineMessage, i, name[0], name[1]));
            }
        }
    }

    private boolean isMultipleDuels() {
        List<?> duels = getConfig().isGroupMode() ? groupDuelists : playerDuelists;
        return duels.size() > 1;
    }

    private <D> String[] duelistsToNameArray(int index, List<Duel<D>> list, Function<D, String> getName) {
        return list.get(index).getDuelists().stream().map(getName).toArray(String[]::new);
    }
}
