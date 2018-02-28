package me.roinujnosde.titansbattle.managers;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import me.roinujnosde.titansbattle.Helper;
import me.roinujnosde.titansbattle.types.Scheduler;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.events.GameStartEvent;
import me.roinujnosde.titansbattle.events.GroupDefeatedEvent;
import me.roinujnosde.titansbattle.events.GroupWinEvent;
import me.roinujnosde.titansbattle.events.LobbyStartEvent;
import me.roinujnosde.titansbattle.events.NewKillerEvent;
import me.roinujnosde.titansbattle.events.PlayerJoinGameEvent;
import me.roinujnosde.titansbattle.events.PlayerWinEvent;
import me.roinujnosde.titansbattle.types.Game;
import me.roinujnosde.titansbattle.types.Game.Mode;
import me.roinujnosde.titansbattle.types.Group;
import me.roinujnosde.titansbattle.types.Prizes;
import me.roinujnosde.titansbattle.types.Warrior;
import me.roinujnosde.titansbattle.types.Winners;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GameManager {

    private TitansBattle plugin;
    private ConfigManager cm;
    private TaskManager tm;
    private DatabaseManager dm;

    private Helper helper;

    private boolean starting = false;
    private boolean happening = false;

    private Map<Group, Integer> groups;

    private HashMap<Player, Integer> killsCount;
    private List<UUID> participants;
    private List<Player> casualties;
    private Game currentGame;

    public void load() {
        plugin = TitansBattle.getInstance();
        cm = plugin.getConfigManager();
        tm = plugin.getTaskManager();
        dm = plugin.getDatabaseManager();
        helper = plugin.getHelper();

        groups = new HashMap<>();
        killsCount = new HashMap<>();
        participants = new ArrayList<>();
        casualties = new ArrayList<>();
    }

    /**
     * Gets the current game, or null, if there is no one happening
     *
     * @return the current game
     */
    public Game getCurrentGame() {
        return currentGame;
    }

    private void addWinner(Player winner, Player killer) {
        //Finaliza o jogo atual
        finishGame(getCurrentGame());
        //Chama os eventos de Novo Killer e Novo Vencedor
        Bukkit.getPluginManager().callEvent(new PlayerWinEvent(winner));
        //Dá os prêmios para o vencedor
        givePrizes(winner);
        //Define o novo killer
        setKiller(getCurrentGame(), killer, null);
        //Anuncia
        Bukkit.getServer().broadcastMessage(MessageFormat.format(plugin.getLang("who_won", getCurrentGame()), winner.getName()));

        helper.increaseVictories(dm.getWarrior(winner.getUniqueId()));

        setWinner(winner);
    }

    private void setWinner(Player winner) {
        Set<UUID> uuids = new HashSet<>();
        uuids.add(winner.getUniqueId());
        plugin.getDatabaseManager().getTodaysWinners().setWinners(getCurrentGame().getMode(), uuids);
    }

    private void addWinners(Game game, Group group, Player killerPlayer) {
        //Chama os eventos
        Bukkit.getPluginManager().callEvent(new NewKillerEvent(killerPlayer, null));
        Bukkit.getPluginManager().callEvent(new GroupWinEvent(group));

        List<UUID> currentWinners = new ArrayList<>();
        List<Player> leaders = new ArrayList<>();
        List<Player> members = new ArrayList<>();

        //Adiciona os participantes restantes à lista de vencedores
        currentWinners.addAll(participants);

        //Filtra os membros e líderes da lista de participantes
        participants.stream().map(Bukkit::getPlayer).forEach(player -> {
            if (group.getWrapper().isLeaderOrOfficer(player.getUniqueId())) {
                leaders.add(player);
            } else {
                members.add(player);
            }
        });

        //Filtra os membros e líderes da lista de mortos e os adiciona à lista de vencedores
        casualties.stream().filter(player -> group.getWrapper().isMember(player.getUniqueId())).forEach(player -> {
            currentWinners.add(player.getUniqueId());
            if (group.getWrapper().isLeaderOrOfficer(player.getUniqueId())) {
                leaders.add(player);
            } else {
                members.add(player);
            }
        });

        Mode mode = getCurrentGame().getMode();

        //Salva os vencedores
        DatabaseManager db = plugin.getDatabaseManager();
        Winners today = db.getTodaysWinners();
        today.setWinnerGroup(mode, group);
        today.setWinners(mode, new HashSet<>(currentWinners));
        setKiller(game, killerPlayer, null);

        group.setVictories(mode, (group.getVictories(mode) + 1));

        currentWinners.stream().map(Bukkit::getOfflinePlayer).forEach(player
                -> {
            helper.increaseVictories(dm.getWarrior(player.getUniqueId()));
        });

        //Faz o anuncio
        Bukkit.getServer().broadcastMessage(MessageFormat.format(plugin.getLang("who_won", getCurrentGame()), group.getWrapper().getName()));

        //Dá os prêmios
        givePrizesToMembersAndLeaders(game, leaders, members);

        //TODO: Fazer este som ser configuravel
//        Bukkit.getOnlinePlayers().forEach(player -> {
//            player.getLocation().getWorld().playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1F, 1F);
//        });
        members.addAll(leaders);
        Bukkit.getPluginManager().callEvent(new PlayerWinEvent(members));
        //Salva as alterações
        cm.save();
        //Finaliza o jogo
        finishGame(game);
    }

    private void givePrizes(Player winner) {
        ArrayList<Player> list = new ArrayList<>();
        list.add(winner);

        givePrizesToMembersAndLeaders(currentGame, null, list);
    }

    private void givePrizesToMembersAndLeaders(Game game, List<Player> leaders, List<Player> members) {
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
        if (plugin.getEconomy() != null) {
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

    /**
     * Initiates the schedule task or starts the game
     */
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
            start(nextScheduler.getMode());
        } else {
            tm.startSchedulerTask(nextTimeInSeconds - currentTimeInSeconds);
        }
    }

    /**
     * Starts a game of the specified mode
     *
     * @param mode the mode
     */
    public void start(Mode mode) {
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
        Bukkit.getServer().broadcastMessage(MessageFormat.format(plugin.getLang("starting_game", getCurrentGame()),
                Long.toString(seconds),
                Integer.toString(currentGame.getMinimumGroups()),
                Integer.toString(currentGame.getMinimumPlayers()),
                Integer.toString(getGroupsParticipatingCount()),
                Integer.toString(getPlayersParticipatingCount())));
        tm.startLobbyAnnouncementTask(times, interval);
    }

    /**
     * Initiates the battle
     *
     * @param game the game
     */
    void startBattle(Game game) {
        if (isHappening()) {
            return;
        }
        GameStartEvent event = new GameStartEvent(game);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            cancelGame(Bukkit.getConsoleSender(), game);
        }
        if (helper.isGroupBased(game)) {
            if (getParticipants().size() < getMinimumPlayers() || groups.size() < getMinimumGroups()) {
                Bukkit.broadcastMessage(plugin.getLang("not_enough_participants", getCurrentGame()));
                finishGame(game);
                return;
            }
        } else {
            if (getParticipants().size() < getMinimumPlayers()) {
                Bukkit.broadcastMessage(plugin.getLang("not_enough_participants", getCurrentGame()));
                finishGame(game);
                return;
            }
        }
        tm.startPreparationTimeTask(game.getPreparationTime());
        Bukkit.getServer().broadcastMessage(MessageFormat.format(plugin.getLang("game_started", getCurrentGame()), Long.toString(game.getPreparationTime())));
        helper.teleportAll(game.getArena());
        tm.startGameExpirationTask(game.getExpirationTime());
        tm.startArenaAnnouncementTask(game.getAnnouncementGameInfoInterval());
        if (game.isDeleteGroups()) {
            int deleted = 0;
            for (Group group : plugin.getDatabaseManager().getGroups()) {
                if (!groups.containsKey(group)) {
                    group.getWrapper().disband();
                    deleted++;
                }
            }
            if (deleted != 0) {
                Bukkit.getServer().broadcastMessage(MessageFormat.format(plugin.getLang("deleted_groups", getCurrentGame()), Integer.toString(deleted)));
            }
        }
    }

    /**
     * Adds a player to the casualty list, broadcasts it and removes from the
     * participants list
     *
     * @param victim the victim
     * @param killer the killer
     */
    public void addCasualty(Player victim, Player killer) {
        casualties.add(victim);
        victim.sendMessage(plugin.getLang("watch_to_the_end", getCurrentGame()));
        if (killer != null) {
            helper.increaseKillsCount(killer);
            Bukkit.broadcastMessage(MessageFormat.format(plugin.getLang("killed_by", getCurrentGame()), victim.getName(), killer.getName()));
            Bukkit.broadcastMessage(MessageFormat.format(plugin.getLang("has_killed_times", getCurrentGame()), killer.getName(), Integer.toString(getPlayerKillsCount(killer))));
        } else {
            Bukkit.broadcastMessage(MessageFormat.format(plugin.getLang("died_by_himself", getCurrentGame()), victim.getName()));
        }
        Mode mode = getCurrentGame().getMode();

        Warrior vWarrior = dm.getWarrior(victim.getUniqueId());
        Warrior kWarrior = dm.getWarrior(killer.getUniqueId());

        if (helper.isGroupBased(currentGame)) {
            Group vGroup = vWarrior.getGroup();
            Group kGroup = kWarrior.getGroup();

            vGroup.setDeaths(mode, vGroup.getDeaths(mode) + 1);
            kGroup.setKills(mode, kGroup.getKills(mode) + 1);
        }

        kWarrior.setKills(mode, kWarrior.getKills(mode) + 1);
        vWarrior.setDeaths(mode, vWarrior.getDeaths(mode) + 1);

        removeParticipant(getCurrentGame(), victim);
    }

    /**
     * Adds a player to the quitters list and removes from the participants list
     *
     * @param player the quitter
     */
    public void addQuitter(Player player) {
        if (isHappening()) {
            if (helper.isFun(getCurrentGame())) {
                cm.getClearInventory().add(player.getUniqueId());
            }
            cm.getRespawn().add(player.getUniqueId());
            cm.save();
            removeParticipant(getCurrentGame(), player);
        }
        if (isStarting()) {
            removeParticipant(getCurrentGame(), player);
        }
    }

    private boolean tryToAdd(Player player) {
        if (!isHappening() && !isStarting()) {
            player.sendMessage(plugin.getLang("not-starting-or-started"));
        } else {
            if (getParticipants().contains(player.getUniqueId())) {
                player.sendMessage(plugin.getLang("already-joined", getCurrentGame()));
                return false;
            }
            if (helper.isGroupBased(getCurrentGame())) {
                if (!helper.isMemberOfAGroup(player)) {
                    player.sendMessage(plugin.getLang("not_in_a_group", getCurrentGame()));
                    return false;
                }
            }
            if (isHappening()) {
                player.sendMessage(plugin.getLang("game_is_happening", getCurrentGame()));
                return false;
            }
            if (isStarting()) {
                if (helper.isFun(getCurrentGame())) {
                    if (helper.inventoryHasItems(player)) {
                        player.sendMessage(plugin.getLang("clear-your-inventory", getCurrentGame()));
                        return false;
                    }
                }

                PlayerJoinGameEvent event = new PlayerJoinGameEvent(player, getCurrentGame());
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a player to the game
     *
     * @param player the player
     */
    public void addParticipant(Player player) {
        if (!tryToAdd(player)) {
            return;
        }

        try {
            player.teleport(getCurrentGame().getLobby());
        } catch (NullPointerException ex) {
            player.sendMessage(ChatColor.RED + "An error has ocurred while trying to teleport you! Contact the admin! :o");
            plugin.debug("You have not setted the Lobby teleport destination!", false);
            return;
        }

        Warrior warrior = plugin.getDatabaseManager().getWarrior(player.getUniqueId());

        if (helper.isFun(getCurrentGame())) {
            player.getInventory().addItem(getCurrentGame().getKit().toArray(new ItemStack[0]));
        }

        killsCount.put(player, 0);
        participants.add(player.getUniqueId());
        for (UUID uuid : getParticipants()) {
            Player p = Bukkit.getPlayer(uuid);
            p.sendMessage(MessageFormat.format(plugin.getLang("player_joined", getCurrentGame()), player.getName()));
        }
        if (helper.isGroupBased(currentGame)) {
            Group group = warrior.getGroup();
            if (groups.containsKey(group)) {
                //Adiciona mais um jogador na contagem
                groups.replace(group, groups.get(group) + 1);
            } else {
                //Adiciona o grupo na lista
                groups.put(group, 1);
            }
        }
    }

    public void setKiller(Game game, Player killer, Player victim) {
        Bukkit.getPluginManager().callEvent(new NewKillerEvent(killer, victim));
        Bukkit.getServer().broadcastMessage(MessageFormat.format(plugin.getLang("new_killer", game), killer.getName()));
        plugin.getDatabaseManager().getTodaysWinners().setKiller(game.getMode(), killer.getUniqueId());
    }

    public void cancelGame(CommandSender cs, Game game) {
        String sender = "Console";
        if (cs instanceof Player) {
            sender = cs.getName();
        }
        Bukkit.broadcastMessage(MessageFormat.format(plugin.getLang("cancelled", getCurrentGame()), sender));
        finishGame(game);
    }

    public void finishGame(Game game) {
        if (!isStarting() && !isHappening()) {
            return;
        }
        setStarting(false);
        setHappening(false);
        helper.teleportAll(game.getExit());
        tm.killAllTasks();
        currentGame = null;
        if (helper.isGroupBased(game)) {
            groups.clear();
            killsCount.clear();
        }
        if (helper.isFun(game)) {
            for (UUID uuid : getParticipants()) {
                Player player = Bukkit.getPlayer(uuid);
                helper.clearInventory(player);
            }
        }
        participants.clear();
        dm.saveAll();
    }

    public List<UUID> getParticipants() {
        return Collections.unmodifiableList(participants);
    }

    public List<Player> getCasualties() {
        return Collections.unmodifiableList(casualties);
    }

    public Map<Group, Integer> getGroups() {
        return Collections.unmodifiableMap(groups);
    }

    void setHappening(boolean happening) {
        this.happening = happening;
    }

    void setStarting(boolean starting) {
        this.starting = starting;
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

    /**
     * Returns the killer of the specified game
     *
     * @param game the game
     * @return the killer UUID
     */
    public UUID getKiller(Game game) {
        return plugin.getDatabaseManager().getLatestWinners().getKiller(game.getMode());
    }

    /**
     * Removes a player from the game and clears his inventory if the mode is
     * FUN
     *
     * @param game the game
     * @param player the player
     */
    public void removeParticipant(Game game, Player player) {
        if (!getParticipants().contains(player.getUniqueId())) {
            return;
        }
        Warrior warrior = plugin.getDatabaseManager().getWarrior(player.getUniqueId());

        if (helper.isFun(game)) {
            player.getInventory().clear();
            player.getInventory().setHelmet(null);
            player.getInventory().setChestplate(null);
            player.getInventory().setLeggings(null);
            player.getInventory().setBoots(null);
        }

        try {
            player.teleport(game.getExit());
        } catch (NullPointerException ex) {
            player.sendMessage(ChatColor.RED + "An error has ocurred while trying to teleport you! Contact the admin! :o");
            plugin.debug("You have not setted the Exit teleport destination!", false);
            return;
        }

        participants.remove(player.getUniqueId());
        if (helper.isGroupBased(game)) {
            Group group = warrior.getGroup();
            int members = groups.get(group);
            if (members == 1 && isStarting()) {
                groups.remove(group);
            }

            if (!isHappening()) {
                return;
            }

            if (members == 1) {
                Bukkit.broadcastMessage(MessageFormat.format(plugin.getLang("group_defeated", getCurrentGame()), group.getWrapper().getName()));
                Bukkit.getPluginManager().callEvent(new GroupDefeatedEvent(group, player));
                groups.remove(group);
                final Mode mode = getCurrentGame().getMode();
                group.setDefeats(mode, group.getDefeats(mode) + 1);
            } else {
                members--;
                groups.replace(group, members);
            }

            List<Group> remainingClans = new ArrayList<>();
            remainingClans.addAll(groups.keySet());
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

    /**
     * Gets the minimum players to start the game
     *
     * @return the minimum
     */
    public int getMinimumPlayers() {
        if (currentGame == null) {
            return 2;
        }
        int a = currentGame.getMinimumPlayers();
        if (a <= 1) {
            return 2;
        }
        return a;
    }

    /**
     * Gets the minimum groups to start the game
     *
     * @return the minimum
     */
    public int getMinimumGroups() {
        if (currentGame == null) {
            return 2;
        }
        int a = currentGame.getMinimumGroups();
        if (a <= 1) {
            return 2;
        }
        return a;
    }

    /**
     * Returns the amount of players that are in the game
     *
     * @return the amount of participants
     */
    public int getPlayersParticipatingCount() {
        return participants.size();
    }

    /**
     * Returns the amount of groups that are in the game
     */
    int getGroupsParticipatingCount() {
        if (isHappening() == false && isStarting() == false) {
            return 0;
        }
        return groups.size();
    }
}
