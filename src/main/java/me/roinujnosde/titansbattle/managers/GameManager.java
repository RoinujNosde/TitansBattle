package me.roinujnosde.titansbattle.managers;

import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.dao.GameConfigurationDao;
import me.roinujnosde.titansbattle.events.*;
import me.roinujnosde.titansbattle.types.*;
import me.roinujnosde.titansbattle.utils.Helper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

public class GameManager {

    private final TitansBattle plugin = TitansBattle.getInstance();


    @Nullable
    private Game currentGame;

    /**
     * Gets the current game, or null, if there is no one happening
     *
     * @return the current game
     */
    @Nullable
    public Game getCurrentGame() {
        return currentGame;
    }

    private void processWinner(@NotNull Game game, @NotNull Player winner, @Nullable Player killer) {

        //Chama os eventos de Novo Killer e Novo Vencedor
        Bukkit.getPluginManager().callEvent(new PlayerWinEvent(winner));

        //Define o novo killer
        if (killer != null) {
            setKiller(game.getConfig().getName(), killer, null);
        }
        //Anuncia
        Bukkit.getServer().broadcastMessage(MessageFormat.format(plugin.getLang("who_won", getCurrentGame()),
                winner.getName()));

        Warrior w = plugin.getDatabaseManager().getWarrior(winner.getUniqueId());
        String gameName = game.getConfig().getName();
        w.setVictories(gameName, w.getVictories(gameName) + 1);

        setWinner(winner);

        //Finaliza o jogo atual
        finishGame(null, null, winner);
    }

    private void setWinner(@NotNull Player winner) {
        Set<UUID> uuids = new HashSet<>();
        uuids.add(winner.getUniqueId());
        plugin.getDatabaseManager().getTodaysWinners().setWinners(Objects.requireNonNull(currentGame).getConfig()
                .getName(), uuids);
    }

    private void processWinners(@NotNull Game game, @NotNull Group group, @Nullable Player killerPlayer) {
        //Chama os eventos
        if (killerPlayer != null) {
            Bukkit.getPluginManager().callEvent(new NewKillerEvent(killerPlayer, null));
            setKiller(game.getConfig().getName(), killerPlayer, null);
        }
        Bukkit.getPluginManager().callEvent(new GroupWinEvent(group));

        List<Player> leaders = new ArrayList<>();
        List<Player> members = new ArrayList<>();

        //Adiciona os participantes restantes à lista de vencedores
        List<UUID> currentWinners = game.getPlayerParticipants().stream().
                filter(group::isMember)
                .collect(Collectors.toList());
        //List<UUID> currentWinners = new ArrayList<>(participants);

        //Filtra os membros e líderes da lista de participantes
        game.getPlayerParticipants().stream().map(Bukkit::getPlayer).forEach(player -> {
            if (group.isLeaderOrOfficer(player.getUniqueId())) {
                leaders.add(player);
            } else {
                members.add(player);
            }
        });

        //Filtra os membros e líderes da lista de mortos e os adiciona à lista de vencedores
        game.getCasualties().stream().filter(player -> group.isMember(player.getUniqueId())).forEach(player -> {
            currentWinners.add(player.getUniqueId());
            if (group.isLeaderOrOfficer(player.getUniqueId())) {
                leaders.add(player);
            } else {
                members.add(player);
            }
        });

        String gameName = game.getConfig().getName();
        //Salva os vencedores
        DatabaseManager db = plugin.getDatabaseManager();
        Winners today = db.getTodaysWinners();
        today.setWinnerGroup(gameName, group.getName());
        today.setWinners(gameName, new HashSet<>(currentWinners));

        group.getData().setVictories(gameName, (group.getData().getVictories(gameName) + 1));

        currentWinners.stream().map(db::getWarrior).forEach(w -> w.increaseVictories(gameName));

        //Faz o anuncio
        Bukkit.getServer().broadcastMessage(MessageFormat.format(plugin.getLang("who_won", game),
                group.getName()));

        members.addAll(leaders);
        Bukkit.getPluginManager().callEvent(new PlayerWinEvent(members));
        //Salva as alterações
        plugin.getConfigManager().save();
        //Finaliza o jogo
        finishGame(leaders, members, null);
        //Dá os prêmios
    }

    /**
     * Initiates the schedule task or starts the game
     */
    public void startOrSchedule() {
        if (!plugin.getConfigManager().isScheduler()) {
            plugin.debug("Scheduler disabled", true);
            return;
        }
        plugin.debug("Scheduler enabled", true);
        TaskManager tm = plugin.getTaskManager();
        if (currentGame != null) {
            plugin.debug("Already happening or starting", true);
            tm.startSchedulerTask(300);
            return;
        }
        int currentTimeInSeconds = Helper.getCurrentTimeInSeconds();
        int oneDay = 86400;

        Scheduler nextScheduler = Scheduler.getNextSchedulerOfDay();
        if (nextScheduler == null) {
            plugin.debug("No next scheduler today", true);
            tm.startSchedulerTask(oneDay - currentTimeInSeconds);
            return;
        }
        int nextHour = nextScheduler.getHour();
        int nextMinute = nextScheduler.getMinute();
        int nextTimeInSeconds = (nextHour * 60 * 60) + (nextMinute * 60);
        plugin.debug(String.format("Scheduler Time: %s:%s", nextHour, nextMinute), true);

        if (currentTimeInSeconds == nextTimeInSeconds) {
            plugin.debug("It's time!", true);
            GameConfigurationDao dao = GameConfigurationDao.getInstance(plugin);
            GameConfiguration config = dao.getGameConfiguration(nextScheduler.getGameName());
            if (config == null) {
                plugin.debug(String.format("Game %s not found!", nextScheduler.getGameName()), false);
                return;
            }
            Game game = new Game(config);
            start(game);
        } else {
            plugin.debug("It's not time yet!", true);
            tm.startSchedulerTask(nextTimeInSeconds - currentTimeInSeconds);
        }
    }

    public void start(@NotNull Game game) {
        if (game.isHappening()) {
            return;
        }
        if (game.getConfig().isGroupMode() && plugin.getGroupManager() == null) {
            throw new IllegalStateException("You cannot start a group based game without a supported Groups plugin!");
        }
        if (!game.getConfig().locationsSet()) {
            throw new IllegalStateException("You didn't set all locations!");
        }
        currentGame = game;
        LobbyStartEvent event = new LobbyStartEvent(currentGame);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        game.setLobby(true);
        game.setHappening(true);
        int times = currentGame.getConfig().getAnnouncementStartingTimes();
        long interval = currentGame.getConfig().getAnnouncementStartingInterval();
        plugin.getTaskManager().startLobbyAnnouncementTask(times, interval);
    }

    /**
     * Initiates the battle
     *
     */
    void startBattle() {
        Game game = getCurrentGame();
        if (game == null || game.isBattle()) {
            return;
        }
        GameStartEvent event = new GameStartEvent(game);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            cancelGame(Bukkit.getConsoleSender());
            return;
        }
        if (game.getPlayersParticipatingCount() < game.getConfig().getMinimumPlayers()) {
            Bukkit.broadcastMessage(plugin.getLang("not_enough_participants", getCurrentGame()));
            finishGame(null, null, null);
            return;
        }
        if (game.getConfig().isGroupMode()) {
            if (game.getGroupsParticipatingCount() < game.getConfig().getMinimumGroups()) {
                Bukkit.broadcastMessage(plugin.getLang("not_enough_participants", getCurrentGame()));
                finishGame(null, null, null);
                return;
            }
        }
        game.setLobby(false);
        game.setPreparation(true);
        TaskManager tm = plugin.getTaskManager();
        tm.startPreparationTimeTask(game.getConfig().getPreparationTime());
        Bukkit.getServer().broadcastMessage(MessageFormat.format(plugin.getLang("game_started", getCurrentGame()),
                Long.toString(game.getConfig().getPreparationTime())));
        game.teleportAll(game.getConfig().getArena());
        tm.startGameExpirationTask(game.getConfig().getExpirationTime());
        tm.startArenaAnnouncementTask(game.getConfig().getAnnouncementGameInfoInterval());
        if (game.getConfig().isDeleteGroups()) {
            int deleted = 0;
            for (Group group : Objects.requireNonNull(plugin.getGroupManager()).getGroups()) {
                if (!game.getGroups().containsKey(group)) {
                    group.disband();
                    deleted++;
                }
            }
            if (deleted != 0) {
                Bukkit.getServer().broadcastMessage(MessageFormat.format(plugin.getLang("deleted_groups",
                        getCurrentGame()), Integer.toString(deleted)));
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
    public void addCasualty(@NotNull Player victim, @Nullable Player killer) {
        Game game = getCurrentGame();
        if (game == null) {
            return;
        }
        List<Player> casualties = game.getCasualties();
        casualties.add(victim);
        victim.sendMessage(plugin.getLang("watch_to_the_end", game));
        if (killer != null) {
            game.increaseKillsCount(killer);
            Bukkit.broadcastMessage(MessageFormat.format(plugin.getLang("killed_by", game), victim.getName(),
                    killer.getName()));
            Bukkit.broadcastMessage(MessageFormat.format(plugin.getLang("has_killed_times", game), killer.getName()
                    , Integer.toString(game.getPlayerKillsCount(killer))));
        } else {
            Bukkit.broadcastMessage(MessageFormat.format(plugin.getLang("died_by_himself", game), victim.getName()));
        }

        String gameName = game.getConfig().getName();
        //Victim
        DatabaseManager dm = plugin.getDatabaseManager();
        Warrior vWarrior = dm.getWarrior(victim.getUniqueId());
        vWarrior.increaseDeaths(gameName);

        //Killer
        if (killer != null) {
            Warrior kWarrior = dm.getWarrior(killer.getUniqueId());
            kWarrior.increaseKills(gameName);
        }

        removeParticipant(victim);
    }

    /**
     * Adds a player to the quitters list and removes from the participants list
     *
     * @param player the quitter
     */
    public void addQuitter(Player player) {
        Game currentGame = getCurrentGame();
        if (currentGame == null) return;
        ConfigManager cm = plugin.getConfigManager();
        if (!currentGame.isLobby()) {
            if (currentGame.getConfig().isUseKits()) {
                cm.getClearInventory().add(player.getUniqueId());
            }
            cm.getRespawn().add(player.getUniqueId());
            cm.save();
        }
        removeParticipant(player);
    }

    private boolean tryToAddParticipant(Player player) {
        Game currentGame = getCurrentGame();
        if (currentGame == null) return false;

        if (!currentGame.isLobby()) {
            player.sendMessage(plugin.getLang("not-starting-or-started"));
        } else {
            if (currentGame.getPlayerParticipants().contains(player.getUniqueId())) {
                player.sendMessage(plugin.getLang("already-joined", currentGame));
                return false;
            }
            Integer maximumPlayers = currentGame.getConfig().getMaximumPlayers();
            if (currentGame.getPlayersParticipatingCount() == maximumPlayers && maximumPlayers != 0) {
                player.sendMessage(plugin.getLang("maximum-players", currentGame));
                return false;
            }
            if (currentGame.getConfig().isGroupMode()) {
                Group group = Objects.requireNonNull(plugin.getGroupManager()).getGroup(player.getUniqueId());
                if (group == null) {
                    player.sendMessage(plugin.getLang("not_in_a_group", currentGame));
                    return false;
                }
                Integer members = currentGame.getGroups().getOrDefault(group, 0);
                Integer maximumPlayersPerGroup = currentGame.getConfig().getMaximumPlayersPerGroup();
                if (members.equals(maximumPlayersPerGroup) && maximumPlayersPerGroup != 0) {
                    player.sendMessage(plugin.getLang("maximum-players-per-group", currentGame));
                    return false;
                }
                Integer maxGroups = currentGame.getConfig().getMaximumGroups();
                if (currentGame.getGroupsParticipatingCount() == maxGroups && maxGroups != 0) {
                    player.sendMessage(plugin.getLang("maximum-groups", currentGame));
                    return false;
                }
            }
            if (getCurrentGame().isBattle() || currentGame.isPreparation()) {
                player.sendMessage(plugin.getLang("game_is_happening", currentGame));
                return false;
            }
            if (currentGame.isLobby()) {
                if (currentGame.getConfig().isUseKits()) {
                    if (Kit.inventoryHasItems(player)) {
                        player.sendMessage(plugin.getLang("clear-your-inventory", currentGame));
                        return false;
                    }
                }

                PlayerJoinGameEvent event = new PlayerJoinGameEvent(player, currentGame);
                Bukkit.getPluginManager().callEvent(event);
                return !event.isCancelled();
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
        if (!tryToAddParticipant(player) || currentGame == null) {
            return;
        }

        Location lobby = currentGame.getConfig().getLobby();
        if (lobby != null) {
            player.teleport(lobby);
        } else {
            player.sendMessage(ChatColor.RED + "An error has ocurred while trying to teleport you! Contact the admin! :o");
            plugin.debug("You have not setted the Lobby teleport destination!", false);
            return;
        }

        Warrior warrior = plugin.getDatabaseManager().getWarrior(player.getUniqueId());
        if (currentGame.getConfig().isGroupMode()) {
            Group group = warrior.getGroup();
            if (group == null) {
                plugin.debug("Player not in group", true);
                return;
            }
            String groupName = group.getName();
            Map<Group, Integer> groups = currentGame.getGroups();
            if (groups.containsKey(group)) {
                //Adiciona mais um jogador na contagem
                plugin.debug(String.format("The group %s is already in the game. Increasing player count.", groupName),
                        true);
                groups.replace(group, groups.get(group) + 1);
            } else {
                plugin.debug(String.format("Adding group %s to the game for the first time", groupName), true);
                //Adiciona o grupo na lista
                groups.put(group, 1);
            }
            plugin.debug(String.format("Group %s member count %d", groupName, groups.get(group)), true);
        }

        if (currentGame.getConfig().isUseKits()) {
            Kit kit = currentGame.getConfig().getKit();
            if (kit != null) {
                kit.set(player);
            }
        }
        List<UUID> participants = currentGame.getPlayerParticipants();
        participants.add(player.getUniqueId());
        for (UUID uuid : participants) {
            Player p = Bukkit.getPlayer(uuid);
            p.sendMessage(MessageFormat.format(plugin.getLang("player_joined", getCurrentGame()), player.getName()));
        }
    }

    public void setKiller(@NotNull String game, @NotNull Player killer, @Nullable Player victim) {
        Bukkit.getPluginManager().callEvent(new NewKillerEvent(killer, victim));
        GameConfigurationDao dao = GameConfigurationDao.getInstance(plugin);
        Bukkit.getServer().broadcastMessage(MessageFormat.format(plugin.getLang("new_killer",
                dao.getConfigFile(Objects.requireNonNull(dao.getGameConfiguration(game)))), killer.getName()));
        plugin.getDatabaseManager().getTodaysWinners().setKiller(game, killer.getUniqueId());
    }

    public void cancelGame(CommandSender cs) {
        String sender = "Console";
        if (cs instanceof Player) {
            sender = cs.getName();
        }
        Bukkit.broadcastMessage(MessageFormat.format(plugin.getLang("cancelled", getCurrentGame()), sender));
        finishGame(null, null, null);
    }

    public void finishGame(@Nullable List<Player> leaders, @Nullable List<Player> members,
                           @Nullable Player winner) {
        Game game = getCurrentGame();
        if (game == null || !game.isHappening()) {
            return;
        }
        game.setLobby(false);
        game.setBattle(false);
        game.setPreparation(false);
        game.setHappening(false);
        game.teleportAll(game.getConfig().getExit());
        plugin.getTaskManager().killAllTasks();
        if (game.getConfig().isUseKits()) {
            for (UUID uuid : game.getPlayerParticipants()) {
                Player player = Bukkit.getPlayer(uuid);
                Kit.clearInventory(player);
            }
        }
        Prizes prizes = game.getConfig().getPrizes();
        if (winner != null) {
            prizes.give(plugin, null, Collections.singletonList(winner));
        }
        if (leaders != null && members != null) {
            prizes.give(plugin, leaders, members);
        }

        plugin.getDatabaseManager().saveAll();
        currentGame = null;
    }


    /**
     * Returns the killer of the specified game
     *
     * @param game the game
     * @return the killer UUID
     */
    @SuppressWarnings("unused")
    public UUID getKiller(@NotNull Game game) {
        return plugin.getDatabaseManager().getLatestWinners().getKiller(game.getConfig().getName());
    }

    /**
     * Removes a player from the game and clears his inventory if the mode is
     * FUN
     *
     * @param player the player
     */
    public boolean removeParticipant(@NotNull Player player) {
        Game game = getCurrentGame();
        List<UUID> participants = game == null ? null : game.getPlayerParticipants();
        if (game == null || !participants.contains(player.getUniqueId())) {
            return false;
        }

        participants.remove(player.getUniqueId());

        if (game.getConfig().isGroupMode()) {
            processGroupMemberLeaving(game, player);
        } else {
            processRemainingParticipants(game);
        }
        if (!teleportToExit(game, player)) return false;

        if (game.getConfig().isUseKits()) {
            Kit.clearInventory(player);
        }

        return true;
    }

    private void processGroupMemberLeaving(@NotNull Game game, @NotNull Player player) {
        Warrior warrior = plugin.getDatabaseManager().getWarrior(player.getUniqueId());
        Group group = warrior.getGroup();
        if (group == null) {
            plugin.debug(String.format("Player %s is not in a group", player.getName()), false);
            return;
        }
        Map<Group, Integer> groups = game.getGroups();
        int members = groups.getOrDefault(group, 0);
        groups.replace(group, --members);

        if (members < 1) {
            groups.remove(group);

            if (!game.isBattle()) {
                return;
            }
            Bukkit.broadcastMessage(MessageFormat.format(plugin.getLang("group_defeated",
                    getCurrentGame()), group.getName()));
            Bukkit.getPluginManager().callEvent(new GroupDefeatedEvent(group, player));
            group.getData().increaseDefeats(game.getConfig().getName());

            processRemainingGroups(game);
        }
    }

    private void processRemainingParticipants(@NotNull Game game) {
        List<UUID> playerParticipants = game.getPlayerParticipants();
        if (playerParticipants.size() == 1) {
            Player killerPlayer = null;
            int mostKills = 0;
            HashMap<Player, Integer> killsCount = game.getKillsCount();
            for (Player p : killsCount.keySet()) {
                if (killsCount.get(p) > mostKills) {
                    mostKills = killsCount.get(p);
                    killerPlayer = p;
                }
            }
            processWinner(game, Bukkit.getPlayer(playerParticipants.get(0)), killerPlayer);
        }
    }

    private void processRemainingGroups(@NotNull Game game) {
        List<Group> remainingClans = new ArrayList<>(game.getGroups().keySet());
        Player killer = null;
        if (remainingClans.size() == 1) {
            int mostKills = 0;
            HashMap<Player, Integer> killsCount = game.getKillsCount();
            for (Player p : killsCount.keySet()) {
                if (killsCount.get(p) >= mostKills) {
                    mostKills = killsCount.get(p);
                    killer = p;
                }
            }
            processWinners(game, remainingClans.get(0), killer);
        }
    }

    private boolean teleportToExit(@NotNull Game game, @NotNull Player player) {
        Location exit = game.getConfig().getExit();
        if (exit != null) {
            player.teleport(exit);
        } else {
            player.sendMessage(ChatColor.RED + "An error has ocurred while trying to teleport you! Contact the admin! :o");
            plugin.debug("You have not setted the Exit teleport destination!", false);
            return false;
        }
        return true;
    }

}
