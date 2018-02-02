package me.roinujnosde.titansbattle.managers;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import me.roinujnosde.titansbattle.Helper;
import me.roinujnosde.titansbattle.types.Scheduler;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.events.GameStartEvent;
import me.roinujnosde.titansbattle.events.GroupDefeatedEvent;
import me.roinujnosde.titansbattle.events.GroupWinEvent;
import me.roinujnosde.titansbattle.events.LobbyStartEvent;
import me.roinujnosde.titansbattle.events.NewKillerEvent;
import me.roinujnosde.titansbattle.events.PlayerWinEvent;
import me.roinujnosde.titansbattle.types.Game;
import me.roinujnosde.titansbattle.types.Game.Mode;
import me.roinujnosde.titansbattle.types.Prizes;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GameManager {

    TitansBattle plugin;
    ConfigManager cm;
    TaskManager tm;
    Helper helper;

    boolean starting = false;
    boolean happening = false;
    HashMap<Clan, Integer> clans;
    HashMap<Faction, Integer> factions;
    HashMap<Player, Integer> killsCount;
    List<UUID> participants;
    List<Player> casualties;
    Game currentGame;

    public void load() {
        plugin = TitansBattle.getInstance();
        cm = TitansBattle.getConfigManager();
        tm = TitansBattle.getTaskManager();
        helper = TitansBattle.getHelper();
        clans = new HashMap<>();
        factions = new HashMap<>();
        killsCount = new HashMap<>();
        participants = new ArrayList<>();
        casualties = new ArrayList<>();
    }

    public Game getCurrentGame() {
        return currentGame;
    }

    private void addWinner(Player winner, Player killer) {
        finishGame(getCurrentGame());
        NewKillerEvent nke = new NewKillerEvent(killer, null);
        Bukkit.getPluginManager().callEvent(nke);
        PlayerWinEvent pwe = new PlayerWinEvent(winner);
        Bukkit.getPluginManager().callEvent(pwe);
        List<Player> players = new ArrayList<>();
        players.add(winner);
        givePrizesToMembersAndLeaders(currentGame, null, players);
        setKiller(currentGame, killer);
        Bukkit.getServer().broadcastMessage(MessageFormat.format(TitansBattle.getLang("who_won", getCurrentGame()), winner.getName()));
        List<UUID> uuids = new ArrayList<>();
        uuids.add(winner.getUniqueId());
        currentGame.setWinners(uuids);
    }

    public void addWinners(Game game, Object group, Player killerPlayer) {
        NewKillerEvent nke = new NewKillerEvent(killerPlayer, null);
        Bukkit.getPluginManager().callEvent(nke);
        GroupWinEvent gwe = new GroupWinEvent(group);
        Bukkit.getPluginManager().callEvent(gwe);
        List<UUID> currentWinners = new ArrayList<>();
        List<Player> leaders = new ArrayList<>();
        List<Player> members = new ArrayList<>();
        for (UUID player : participants) {
            currentWinners.add(player);
        }
        if (plugin.isSimpleClans()) {
            Clan clan = (Clan) group;
            for (UUID uuid : participants) {
                Player player = Bukkit.getPlayer(uuid);
                if (plugin.getClanManager().getClanPlayer(player).isLeader()) {
                    leaders.add(player);
                } else {
                    members.add(player);
                }
            }
            for (Player player : casualties) {
                if (clan.isMember(player)) {
                    currentWinners.add(player.getUniqueId());
                    if (plugin.getClanManager().getClanPlayer(player).isLeader()) {
                        leaders.add(player);
                    } else {
                        members.add(player);
                    }
                }
            }
            game.setWinnerGroup(clan.getTag());
            Bukkit.getServer().broadcastMessage(MessageFormat.format(TitansBattle.getLang("who_won", getCurrentGame()), clan.getTag()));
        }
        if (plugin.isFactions()) {
            Faction faction = (Faction) group;
            for (UUID uuid : participants) {
                Player player = Bukkit.getPlayer(uuid);
                if (MPlayer.get(player).getRole() == Rel.LEADER || MPlayer.get(player).getRole() == Rel.OFFICER) {
                    leaders.add(player);
                } else {
                    members.add(player);
                }
            }
            for (Player player : casualties) {
                if (MPlayer.get(player).getFaction().getName().equals(faction.getName())) {
                    currentWinners.add(player.getUniqueId());
                    if (MPlayer.get(player).getRole() == Rel.LEADER || MPlayer.get(player).getRole() == Rel.OFFICER) {
                        leaders.add(player);
                    } else {
                        members.add(player);
                    }
                }
            }
            game.setWinnerGroup(faction.getName());
            Bukkit.getServer().broadcastMessage(MessageFormat.format(TitansBattle.getLang("who_won", getCurrentGame()), faction.getName()));
        }
        givePrizesToMembersAndLeaders(game, leaders, members);
        setKiller(game, killerPlayer);
        game.setWinners(currentWinners);
        game.setKiller(killerPlayer.getUniqueId());
        members.addAll(leaders);
        PlayerWinEvent pwe = new PlayerWinEvent(members);
        Bukkit.getPluginManager().callEvent(pwe);
        cm.save();
        finishGame(game);
    }

    public void givePrizesToMembersAndLeaders(Game game, List<Player> leaders, List<Player> members) {
        Prizes prizes = game.getPrizes();
        if (prizes.isTreatLeadersAsMembers()) {
            members.addAll(leaders);
            leaders.clear();
        }
        if (prizes.isLeaderItemsEnabled()) {
            helper.giveItemsToPlayers(game, leaders, prizes.getLeaderItems().toArray(new ItemStack[0]));
        }
        if (prizes.isMemberItemsEnabled()) {
            helper.giveItemsToPlayers(game, members, prizes.getMemberItems().toArray(new ItemStack[0]));
        }
        if (TitansBattle.getEconomy() != null) {
            if (prizes.isLeaderMoneyEnabled()) {
                helper.giveMoneyToPlayers(leaders, prizes.getLeaderMoneyAmount(), prizes.isLeaderMoneyDivide());
            }
            if (prizes.isMemberMoneyEnabled()) {
                helper.giveMoneyToPlayers(members, prizes.getMemberMoneyAmount(), prizes.isMemberMoneyDivide());
            }
        }
        if (prizes.isLeaderCommandsEnabled()) {
            helper.runCommandsOnPlayers(leaders, prizes.getLeaderCommands(), prizes.getLeaderCommandsSomeNumber(), prizes.isLeaderCommandsSomeNumberDivide());
        }
        if (prizes.isMemberCommandsEnabled()) {
            helper.runCommandsOnPlayers(members, prizes.getMemberCommands(), prizes.getMemberCommandsSomeNumber(), prizes.isMemberCommandsSomeNumberDivide());
        }
    }

    public void startOrSchedule() {
        if (cm.isScheduler()) {
            return;
        }
        if (isHappening() || isStarting()) {
            tm.startSchedulerTask(300);
            return;
        }
        int currentTimeInSeconds = helper.getCurrentTimeInSeconds();
        int oneDay = 86400;

        Scheduler nextScheduler = helper.getNextSchedulerOfDay();
        if (nextScheduler == null) {
            tm.startSchedulerTask(oneDay - currentTimeInSeconds);
            return;
        }
        int nextHour = nextScheduler.getHour();
        int nextMinute = nextScheduler.getMinute();
        int nextTimeInSeconds = (nextHour * 60 * 60) + (nextMinute * 60);

        if (currentTimeInSeconds == nextTimeInSeconds) {
            startLobby(nextScheduler.getMode());
        } else {
            tm.startSchedulerTask(nextTimeInSeconds - currentTimeInSeconds);
        }
    }

    public void startLobby(Mode mode) {
        if (isStarting() || isHappening()) {
            return;
        }
        if (helper.isGroupBased(helper.getGame(mode))) {
            if (!plugin.isFactions() && !plugin.isSimpleClans()) {
                throw new IllegalStateException("You cannot start a group based game without SimpleClans or Factions!");
            }
        }
        currentGame = helper.getGame(mode);
        LobbyStartEvent event = new LobbyStartEvent(currentGame);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        setStarting(true);
        int times = currentGame.getAnnouncementStartingTimes();
        long interval = currentGame.getAnnouncementStartingInterval();
        long seconds = times * interval;
        Bukkit.getServer().broadcastMessage(MessageFormat.format(TitansBattle.getLang("starting_game", getCurrentGame()),
                Long.toString(seconds),
                Integer.toString(currentGame.getMinimumGroups()),
                Integer.toString(currentGame.getMinimumPlayers()),
                Integer.toString(getGroupsParticipatingCount()),
                Integer.toString(getPlayersParticipatingCount())));
        tm.startLobbyAnnouncementTask(times, interval);
    }

    public void startGame(Game game) {
        if (isHappening()) {
            return;
        }
        GameStartEvent event = new GameStartEvent(game);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            cancelGame(Bukkit.getConsoleSender(), game);
        }
        if (helper.isGroupBased(game)) {
            if (plugin.isFactions()) {
                if (getParticipants().size() < getMinimumPlayers() || factions.size() < getMinimumGroups()) {
                    Bukkit.broadcastMessage(TitansBattle.getLang("not_enough_participants", getCurrentGame()));
                    finishGame(game);
                    return;
                }
            }
            if (plugin.isSimpleClans()) {
                if (getParticipants().size() < getMinimumPlayers() || clans.size() < getMinimumGroups()) {
                    Bukkit.broadcastMessage(TitansBattle.getLang("not_enough_participants", getCurrentGame()));
                    finishGame(game);
                    return;
                }
            }
        } else {
            if (getParticipants().size() < getMinimumPlayers()) {
                Bukkit.broadcastMessage(TitansBattle.getLang("not_enough_participants", getCurrentGame()));
                finishGame(game);
                return;
            }
        }
        tm.startPreparationTimeTask(game.getPreparationTime());
        Bukkit.getServer().broadcastMessage(MessageFormat.format(TitansBattle.getLang("game_started", getCurrentGame()), Long.toString(game.getPreparationTime())));
        helper.teleportAll(game.getArena());
        tm.startGameExpirationTask(game.getExpirationTime());
        tm.startArenaAnnouncementTask(game.getAnnouncementGameInfoInterval());
        if (game.isDeleteGroups()) {
            int deleted = 0;
            if (plugin.isSimpleClans()) {
                for (Clan clan : plugin.getClanManager().getClans()) {
                    if (!clans.containsKey(clan)) {
                        clan.disband();
                        deleted++;
                    }
                }
            }
            if (plugin.isFactions()) {
                for (Faction faction : FactionColl.get().getAll()) {
                    if (!factions.containsKey(faction)) {
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "f disband " + faction.getName());
                        deleted++;
                    }
                }
            }
            if (deleted != 0) {
                Bukkit.getServer().broadcastMessage(MessageFormat.format(TitansBattle.getLang("deleted_groups", getCurrentGame()), Integer.toString(deleted)));
            }
        }
    }

    public void addParticipant(Player player) {
        killsCount.put(player, 0);
        getParticipants().add(player.getUniqueId());
        for (UUID uuid : getParticipants()) {
            Player p = Bukkit.getPlayer(uuid);
            p.sendMessage(MessageFormat.format(TitansBattle.getLang("player_joined", getCurrentGame()), player.getName()));
        }
        if (helper.isGroupBased(currentGame)) {
            if (plugin.isSimpleClans()) {
                Clan clan = plugin.getClanManager().getClanPlayer(player).getClan();
                if (clans.containsKey(clan)) {
                    int newMembers = clans.get(clan) + 1;
                    clans.replace(clan, newMembers);
                } else {
                    clans.put(clan, 1);
                }
            }
            if (plugin.isFactions()) {
                Faction faction = MPlayer.get(player).getFaction();
                if (factions.containsKey(faction)) {
                    int newMembers = factions.get(faction) + 1;
                    factions.replace(faction, newMembers);

                } else {
                    factions.put(faction, 1);
                }
            }
        }
    }

    public void setKiller(Game game, Player killer) {
        Bukkit.getServer().broadcastMessage(MessageFormat.format(TitansBattle.getLang("new_killer", game), killer.getName()));
        game.setKiller(killer.getUniqueId());
        cm.save();
    }

    public void cancelGame(CommandSender cs, Game game) {
        String sender = "Console";
        if (cs instanceof Player) {
            sender = cs.getName();
        }
        Bukkit.broadcastMessage(MessageFormat.format(TitansBattle.getLang("cancelled", getCurrentGame()), sender));
        finishGame(game);
    }

    public void finishGame(Game game) {
        if (isStarting() == false && isHappening() == false) {
            return;
        }
        setStarting(false);
        setHappening(false);
        helper.teleportAll(game.getExit());
        tm.killAllTasks();
        currentGame = null;
        if (helper.isGroupBased(game)) {
            if (plugin.isSimpleClans()) {
                clans.clear();
            }
            if (plugin.isFactions()) {
                factions.clear();
            }
            killsCount.clear();
        }
        if (helper.isFun(game)) {
            for (UUID uuid : getParticipants()) {
                Player player = Bukkit.getPlayer(uuid);
                helper.clearInventory(player);
            }
        }
        participants.clear();
    }

    public List<UUID> getParticipants() {
        return participants;
    }

    public List<Player> getCasualties() {
        return casualties;
    }

    public void setHappening(boolean happening) {
        this.happening = happening;
    }

    public void setStarting(boolean starting) {
        this.starting = starting;
    }

    public HashMap<Clan, Integer> getClans() {
        return clans;
    }

    public HashMap<Faction, Integer> getFactions() {
        return factions;
    }

    public HashMap<Player, Integer> getKillsCount() {
        return killsCount;
    }

    public int getPlayerKillsCount(Player player) {
        if (killsCount.containsKey(player)) {
            return killsCount.get(player);
        }
        return 0;
    }

    public UUID getKiller(Game game) {
        return game.getKiller();
    }

    public void removeParticipant(Game game, Player player) {
        if (!getParticipants().contains(player.getUniqueId())) {
            return;
        }
        if (player.isValid()) {
            player.teleport(game.getExit());
        }
        getParticipants().remove(player.getUniqueId());
        if (helper.isGroupBased(game)) {
            if (plugin.isSimpleClans()) {
                Clan clan = plugin.getClanManager().getClanPlayer(player).getClan();
                int members = clans.get(clan);

                if (members == 1 && isStarting()) {
                    clans.remove(clan);
                }

                if (!isHappening()) {
                    return;
                }

                if (members == 1) {
                    Bukkit.broadcastMessage(MessageFormat.format(TitansBattle.getLang("group_defeated", getCurrentGame()), clan.getTag()));
                    GroupDefeatedEvent event = new GroupDefeatedEvent(clan, player);
                    Bukkit.getPluginManager().callEvent(event);
                    clans.remove(clan);
                } else {
                    members--;
                    clans.replace(clan, members);
                }

                List<Clan> remainingClans = new ArrayList<>();
                remainingClans.addAll(clans.keySet());
                Player killer = null;
                if (remainingClans.size() == 1) {
                    int mostKills = 0;
                    for (Player p : killsCount.keySet()) {
                        if (killsCount.get(p) >= mostKills) {
                            mostKills = killsCount.get(p);
                            killer = p;
                        }
                    }
                    addWinners(game, remainingClans.get(0), killer);
                }
            }
            if (plugin.isFactions()) {
                Faction faction = MPlayer.get(player).getFaction();
                Integer members = factions.get(faction);

                if (members == 1 && isStarting()) {
                    factions.remove(faction);
                }

                if (!isHappening()) {
                    return;
                }

                if (members == 1) {
                    Bukkit.broadcastMessage(MessageFormat.format(TitansBattle.getLang("group_defeated", getCurrentGame()), faction.getName()));
                    factions.remove(faction);
                    GroupDefeatedEvent event = new GroupDefeatedEvent(faction, player);
                    Bukkit.getPluginManager().callEvent(event);
                } else {
                    members--;
                    factions.replace(faction, members);
                }
                
                List<Faction> remainingFactions = new ArrayList<>();
                remainingFactions.addAll(factions.keySet());
                Player killer = null;
                if (remainingFactions.size() == 1) {
                    int mostKills = 0;
                    for (Player p : killsCount.keySet()) {
                        if (killsCount.get(p) >= mostKills) {
                            mostKills = killsCount.get(p);
                            killer = p;
                        }
                    }
                    addWinners(game, remainingFactions.get(0), killer);
                }
            }
        } else {
            if (getParticipants().size() == 1) {
                Player killerPlayer = null;
                int mostKills = 0;
                for (Player p : killsCount.keySet()) {
                    if (killsCount.get(p) > mostKills) {
                        mostKills = killsCount.get(p);
                        killerPlayer = p;
                    }
                }
                addWinner(Bukkit.getPlayer(getParticipants().get(0)), killerPlayer);
            }
        }
    }

    /**
     * Checks if the game has started
     *
     * @return if it has started (true) or not (false)
     */
    public boolean isHappening() {
        return happening;
    }

    /**
     * Checks if the game is starting
     *
     * @return if it is starting (true) or not (false)
     */
    public boolean isStarting() {
        return starting;
    }

    public int getMinimumPlayers() {
        int a = currentGame.getMinimumPlayers();
        if (a <= 1) {
            return 2;
        }
        return a;
    }

    public int getMinimumGroups() {
        int a = currentGame.getMinimumGroups();
        if (a <= 1) {
            return 2;
        }
        return a;
    }

    /*
	 * Returns the amount of players that are in the game
     */
    public int getPlayersParticipatingCount() {
        return participants.size();
    }

    /* 
    * Returns the amount of groups that are in the game  
     */
    public int getGroupsParticipatingCount() {
        if (isHappening() == false && isStarting() == false) {
            return 0;
        }
        if (TitansBattle.isFactions()) {
            return factions.size();
        }
        if (TitansBattle.isSimpleClans()) {
            return clans.size();
        }
        return 0;
    }

}
