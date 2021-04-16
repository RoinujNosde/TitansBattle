package me.roinujnosde.titansbattle.games;

import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.events.*;
import me.roinujnosde.titansbattle.managers.DatabaseManager;
import me.roinujnosde.titansbattle.managers.GameManager;
import me.roinujnosde.titansbattle.managers.GroupManager;
import me.roinujnosde.titansbattle.types.GameConfiguration;
import me.roinujnosde.titansbattle.types.Group;
import me.roinujnosde.titansbattle.types.Kit;
import me.roinujnosde.titansbattle.types.Warrior;
import me.roinujnosde.titansbattle.utils.SoundUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.roinujnosde.titansbattle.utils.SoundUtils.Type.*;

public abstract class Game {

    protected final TitansBattle plugin;
    protected final GroupManager groupManager;
    protected final GameManager gameManager;
    protected final DatabaseManager databaseManager;
    private final GameConfiguration config;
    protected final List<Warrior> playerParticipants = new ArrayList<>();
    protected final HashMap<Warrior, Integer> killsCount = new HashMap<>();
    protected final Set<Warrior> casualties = new HashSet<>();
    protected boolean lobby;
    private final List<BukkitTask> tasks = new ArrayList<>();
    protected boolean battle = false;

    public Game(TitansBattle plugin, GameConfiguration config) {
        this.plugin = plugin;
        this.config = config;
        this.groupManager = plugin.getGroupManager();
        if (config.isGroupMode() && groupManager == null) {
            throw new IllegalStateException("gameManager cannot be null in a group mode game");
        }
        this.databaseManager = plugin.getDatabaseManager();
        gameManager = plugin.getGameManager();
    }

    protected boolean canJoin(@NotNull Warrior warrior) {
        Player player = warrior.toOnlinePlayer();
        if (player == null) {
            plugin.debug(String.format("canJoin() -> player %s %s == null", warrior.getName(), warrior.getUniqueId()));
            return false;
        }
        if (!isLobby()) {
            player.sendMessage(plugin.getLang("game_is_happening", this));
            return false;
        }
        if (isParticipant(warrior)) {
            player.sendMessage(plugin.getLang("already-joined", this));
            return false;
        }
        if (playerParticipants.size() >= getConfig().getMaximumPlayers() && getConfig().getMaximumPlayers() > 0) {
            player.sendMessage(plugin.getLang("maximum-players", this));
            return false;
        }
        if (config.isGroupMode()) {
            if (warrior.getGroup() == null) {
                player.sendMessage(plugin.getLang("not_in_a_group", this));
                return false;
            }
            if (getGroupParticipants().size() >= getConfig().getMaximumGroups() && getConfig().getMaximumGroups() > 0) {
                player.sendMessage(plugin.getLang("maximum-groups", this));
                return false;
            }
            Integer amountOfPlayers = getGroupParticipants().getOrDefault(warrior.getGroup(), 0);
            if (amountOfPlayers >= getConfig().getMaximumPlayersPerGroup() &&
                    getConfig().getMaximumPlayersPerGroup() > 0) {
                player.sendMessage(plugin.getLang("maximum-players-per-group", this));
                return false;
            }
        }
        if (getConfig().isUseKits() && Kit.inventoryHasItems(player)) {
            player.sendMessage(plugin.getLang("clear-your-inventory", this));
            return false;
        }
        PlayerJoinGameEvent event = new PlayerJoinGameEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public abstract boolean isInBattle(@NotNull Warrior warrior);

    public boolean isParticipant(@NotNull Warrior warrior) {
        return playerParticipants.contains(warrior);
    }

    public void onJoin(@NotNull Warrior warrior) {
        if (!canJoin(warrior)) {
            plugin.debug(String.format("Warrior %s can't join", warrior.getName()));
            return;
        }
        Player player = warrior.toOnlinePlayer();
        if (player == null) {
            plugin.debug(String.format("onJoin() -> player %s %s == null", warrior.getName(), warrior.getUniqueId()));
            return;
        }
        teleport(warrior, getConfig().getLobby());
        SoundUtils.playSound(JOIN_GAME, plugin.getConfig(), player);
        playerParticipants.add(warrior);
        setKit(warrior);
        sendMessageToParticipants(MessageFormat.format(plugin.getLang("player_joined", this), player.getName()));
    }

    protected void setKit(@NotNull Warrior warrior) {
        Kit kit = getConfig().getKit();
        Player player = warrior.toOnlinePlayer();
        if (getConfig().isUseKits() && kit != null && player != null) {
            Kit.clearInventory(player);
            kit.set(player);
        }
    }

    public boolean shouldClearDropsOnDeath(@NotNull Warrior warrior) {
        return false;
    }

    public boolean shouldKeepInventoryOnDeath(@NotNull Warrior warrior) {
        return false;
    }

    protected void processPlayerExit(@NotNull Warrior warrior) {
        if (!isParticipant(warrior)) {
            return;
        }
        Player player = warrior.toOnlinePlayer();
        if (player != null) {
            teleport(warrior, getConfig().getExit());
            PlayerExitGameEvent event = new PlayerExitGameEvent(player, this);
            Bukkit.getPluginManager().callEvent(event);
        }
        playerParticipants.remove(warrior);
        Group group = warrior.getGroup();
        if (!isLobby()) {
            runCommandsAfterBattle(Collections.singletonList(warrior));
            processRemainingPlayers(warrior);
            //last participant
            if (config.isGroupMode() && group != null && !getGroupParticipants().containsKey(group)) {
                gameManager.broadcastKey("group_defeated", this, group.getName());
                Bukkit.getPluginManager().callEvent(new GroupDefeatedEvent(group, warrior.toOnlinePlayer()));
                group.getData().increaseDefeats(getConfig().getName());
            }
            sendRemainingOpponentsCount();
        }
    }

    /**
     * Attempts to find the Killer in the game, returns null if none found or if it's disabled
     */
    @Nullable
    public Warrior findKiller() {
        if (!config.isKiller()) {
            return null;
        }
        Warrior killerPlayer = null;
        int mostKills = 0;

        for (Map.Entry<Warrior, Integer> entry : getKillsCount().entrySet()) {
            Integer kills = entry.getValue();
            if (kills > mostKills) {
                killerPlayer = entry.getKey();
                mostKills = kills;
            }
        }
        return killerPlayer;
    }

    protected abstract void processRemainingPlayers(@NotNull Warrior warrior);

    public void onLeave(@NotNull Warrior warrior) {
        if (!isParticipant(warrior)) {
            return;
        }
        if (getConfig().isUseKits()) {
            Kit.clearInventory(warrior.toOnlinePlayer());
        }
        Player player = Objects.requireNonNull(warrior.toOnlinePlayer());
        player.sendMessage(plugin.getLang("you-have-left", this));
        SoundUtils.playSound(LEAVE_GAME, plugin.getConfig(), player);
        processPlayerExit(warrior);
    }

    public void onDisconnect(@NotNull Warrior warrior) {
        if (!isParticipant(warrior)) {
            return;
        }
        if (config.isUseKits()) {
            plugin.getConfigManager().getClearInventory().add(warrior.getUniqueId());
        }
        plugin.getConfigManager().getRespawn().add(warrior.getUniqueId());
        plugin.getConfigManager().save();
        processPlayerExit(warrior);
    }

    public void onRespawn(@NotNull Warrior warrior) {
        if (casualties.contains(warrior)) {
            teleport(warrior, getConfig().getWatchroom());
        }
    }

    public void onDeath(@NotNull Warrior victim, @Nullable Warrior killer) {
        if (!isParticipant(victim)) {
            return;
        }
        if (killer == null) {
            gameManager.broadcastKey("died_by_himself", this, victim.getName());
        }
        if (!isLobby()) {
            ParticipantDeathEvent event = new ParticipantDeathEvent(victim, killer);
            Bukkit.getPluginManager().callEvent(event);
            String gameName = getConfig().getName();
            casualties.add(victim);
            if (config.isGroupMode()) {
                victim.sendMessage(plugin.getLang("watch_to_the_end", this));
            }
            if (killer != null) {
                killer.increaseKills(gameName);
                gameManager.broadcastKey("killed_by", this, victim.getName(), killer.getName());
                gameManager.broadcastKey("has_killed_times", this, killer.getName(), increaseKills(killer));
            }
            victim.increaseDeaths(gameName);
            playDeathSound(victim);
        }
        processPlayerExit(victim);
    }

    protected abstract void onLobbyEnd();

    protected boolean canStartBattle() {
        GameStartEvent event = new GameStartEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            gameManager.broadcastKey("cancelled", this, "CONSOLE");
            return false;
        }
        if (getPlayerParticipants().size() < getConfig().getMinimumPlayers()) {
            gameManager.broadcastKey("not_enough_participants", this);
            return false;
        }
        if (getConfig().isGroupMode()) {
            if (getGroupParticipants().size() < getConfig().getMinimumGroups()) {
                gameManager.broadcastKey("not_enough_participants", this);
                return false;
            }
        }
        return true;
    }

    public void start() {
        LobbyStartEvent event = new LobbyStartEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        gameManager.setCurrentGame(this);
        if (getConfig().isGroupMode() && plugin.getGroupManager() == null) {
            throw new IllegalStateException("You cannot start a group based game without a supported Groups plugin!");
        }
        if (!getConfig().locationsSet()) {
            throw new IllegalStateException("You didn't set all locations!");
        }
        lobby = true;
        Integer interval = config.getAnnouncementStartingInterval();
        BukkitTask lobbyTask = new LobbyAnnouncementTask(config.getAnnouncementStartingTimes(), interval)
                .runTaskTimer(plugin, 0, interval * 20);
        addTask(lobbyTask);
    }

    protected void addTask(@NotNull BukkitTask task) {
        tasks.add(task);
    }

    protected void killTasks() {
        tasks.forEach(BukkitTask::cancel);
        tasks.clear();
        plugin.getTaskManager().killAllTasks();
    }

    public void cancel(@NotNull CommandSender sender) {
        gameManager.broadcastKey("cancelled", this, sender.getName());
        finish(true);
    }

    private void deleteGroups() {
        if (getConfig().isDeleteGroups()) {
            int deleted = 0;
            for (Group group : Objects.requireNonNull(plugin.getGroupManager()).getGroups()) {
                if (!getGroupParticipants().containsKey(group)) {
                    group.disband();
                    deleted++;
                }
            }
            if (deleted != 0) {
                Bukkit.getServer().broadcastMessage(MessageFormat.format(plugin.getLang("deleted_groups", this),
                        deleted));
            }
        }
    }

    protected abstract void processWinners();

    protected void givePrizes(GameConfiguration.Prize prize, @Nullable Group group, @Nullable List<Warrior> warriors) {
        List<Player> leaders = new ArrayList<>();
        List<Player> members = new ArrayList<>();
        if (warriors == null) {
            return;
        }
        List<Player> players = warriors.stream().filter(Objects::nonNull).map(Warrior::toOnlinePlayer)
                .filter(Objects::nonNull).collect(Collectors.toList());
        if (group != null) {
            for (Player p : players) {
                if (group.isLeaderOrOfficer(p.getUniqueId())) {
                    leaders.add(p);
                } else {
                    members.add(p);
                }
            }
        } else {
            members = players;
        }
        getConfig().getPrizes(prize).give(plugin, leaders, members);
    }

    public void finish(boolean cancelled) {
        teleportAll(getConfig().getExit());
        killTasks();
        runCommandsAfterBattle(getPlayerParticipants());
        if (getConfig().isUseKits()) {
            getPlayerParticipantsStream().forEach(Kit::clearInventory);
        }
        gameManager.setCurrentGame(null);
        if (!cancelled) {
            processWinners();
        }
        plugin.getDatabaseManager().saveAll();
    }

    public @NotNull GameConfiguration getConfig() {
        return config;
    }

    public @NotNull List<Warrior> getPlayerParticipants() {
        return Collections.unmodifiableList(playerParticipants);
    }

    public abstract @NotNull Collection<Warrior> getCurrentFighters();

    public Map<Group, Integer> getGroupParticipants() {
        Map<Group, Integer> groups = new HashMap<>();
        for (Warrior w : playerParticipants) {
            groups.compute(w.getGroup(), (g, i) -> i == null ? 1 : i + 1);
        }
        return groups;
    }

    public Set<Warrior> getCasualties() {
        return casualties;
    }

    public HashMap<Warrior, Integer> getKillsCount() {
        return killsCount;
    }

    protected int increaseKills(Warrior warrior) {
        return killsCount.compute(warrior, (p, i) -> i == null ? 1 : i + 1);
    }

    public void sendMessageToParticipants(@NotNull String message) {
        getPlayerParticipantsStream().forEach(p -> p.sendMessage(message));
    }

    @NotNull
    protected Stream<Player> getPlayerParticipantsStream() {
        return getPlayerParticipants().stream().map(Warrior::toOnlinePlayer).filter(Objects::nonNull);
    }

    protected int getRemainingOpponents(@NotNull Player player) {
        if (!getConfig().isGroupMode()) {
            return getPlayerParticipants().size() - 1;
        }
        int opponents = 0;
        Warrior warrior = plugin.getDatabaseManager().getWarrior(player.getUniqueId());
        for (Map.Entry<Group, Integer> entry : getGroupParticipants().entrySet()) {
            Group group = entry.getKey();
            if (group.equals(warrior.getGroup())) {
                continue;
            }
            opponents += entry.getValue();
        }
        return opponents;
    }

    private void playDeathSound(@NotNull Warrior victim) {
        Stream<Player> players = getPlayerParticipantsStream();
        if (!getConfig().isGroupMode()) {
            players.forEach(p -> SoundUtils.playSound(ENEMY_DEATH, plugin.getConfig(), p));
            return;
        }
        GroupManager groupManager = plugin.getGroupManager();
        if (groupManager == null) {
            return;
        }
        Group victimGroup = groupManager.getGroup(victim.getUniqueId());
        players.forEach(participant -> {
            Group group = groupManager.getGroup(participant.getUniqueId());
            if (group == null) {
                return;
            }
            if (group.equals(victimGroup)) {
                SoundUtils.playSound(ALLY_DEATH, plugin.getConfig(), participant);
            } else {
                SoundUtils.playSound(ENEMY_DEATH, plugin.getConfig(), participant);
            }
        });
    }

    protected void sendRemainingOpponentsCount() {
        try {
            getPlayerParticipantsStream().forEach(p -> {
                int remaining = getRemainingOpponents(p);
                if (remaining <= 0) {
                    return;
                }
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(
                        MessageFormat.format(plugin.getLang("action-bar-remaining-opponents", this),
                                remaining)));
            });
        } catch (NoSuchMethodError ignored) {
        }
    }

    protected void runCommandsBeforeBattle(@NotNull Collection<Warrior> warriors) {
        runCommands(warriors, getConfig().getCommandsBeforeBattle());
    }

    protected void runCommandsAfterBattle(@NotNull Collection<Warrior> warriors) {
        runCommands(warriors, getConfig().getCommandsAfterBattle());
    }

    private void runCommands(@NotNull Collection<Warrior> warriors, @Nullable Collection<String> commands) {
        if (commands == null) return;
        Consumer<Player> dispatchCommands = (player) -> {
            for (String command : commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
            }
        };
        warriors.stream().map(Warrior::toOnlinePlayer).filter(Objects::nonNull).forEach(dispatchCommands);
    }

    protected void teleport(@NotNull Collection<Warrior> collection, @NotNull Location destination) {
        collection.forEach(p -> teleport(p, destination));
    }

    protected void teleport(@Nullable Warrior warrior, Location destination) {
        plugin.debug(String.format("teleport() -> destination %s", destination));
        Player player = warrior != null ? warrior.toOnlinePlayer() : null;
        if (player == null) {
            plugin.debug(String.format("teleport() -> warrior %s", warrior));
            return;
        }
        player.teleport(destination);
        SoundUtils.playSound(TELEPORT, plugin.getConfig(), player);
    }

    protected void teleportAll(Location destination) {
        getPlayerParticipants().forEach(player -> teleport(player, destination));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isLobby() {
        return lobby;
    }

    @Override
    public int hashCode() {
        return getConfig().getName().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Game)) {
            return false;
        }
        Game otherGame = (Game) other;
        return otherGame.getConfig().getName().equals(getConfig().getName());
    }

    protected void startPreparationTask() {
        addTask(new PreparationTimeTask().runTaskLater(plugin, config.getPreparationTime() * 20));
        addTask(new CountdownTitleTask(getCurrentFighters(), getConfig().getPreparationTime())
                .runTaskTimer(plugin, 0L, 20L));
    }

    protected class PreparationTimeTask extends BukkitRunnable {

        @Override
        public void run() {
            Bukkit.getServer().broadcastMessage(plugin.getLang("preparation_over", Game.this));
            runCommandsBeforeBattle(getCurrentFighters());
            battle = true;
        }
    }

    protected class CountdownTitleTask extends BukkitRunnable {

        private final Collection<Warrior> warriors;
        private int timer;

        public CountdownTitleTask(Collection<Warrior> warriors, int timer) {
            this.warriors = warriors;
            if (timer < 0) {
                timer = 0;
            }
            this.timer = timer;
        }

        @SuppressWarnings("deprecation")
        @Override
        public void run() {
            List<Player> players = warriors.stream().map(Warrior::toOnlinePlayer).filter(Objects::nonNull)
                    .collect(Collectors.toList());
            String title;
            if (timer > 0) {
                title = getColor() + "" + timer;
            } else {
                title = ChatColor.RED + plugin.getLang("title.fight", Game.this);
                this.cancel();
                Bukkit.getScheduler().runTaskLater(plugin, () -> players.forEach(Player::resetTitle), 20L);
            }
            players.forEach(player -> player.sendTitle(title, ""));
            timer--;
        }

        private ChatColor getColor() {
            ChatColor color;
            if (timer > 6) {
                color = ChatColor.GREEN;
            } else if (timer > 3) {
                color = ChatColor.YELLOW;
            } else {
                color = ChatColor.RED;
            }
            return color;
        }
    }

    protected class LobbyAnnouncementTask extends BukkitRunnable {
        int times;
        long interval, seconds;

        public LobbyAnnouncementTask(int times, long interval) {
            this.times = times + 1;
            this.interval = interval;
        }

        @Override
        public void run() {
            seconds = times * interval;
            if (times > 0) {
                Bukkit.getServer().broadcastMessage(MessageFormat.format(plugin.getLang("starting_game", Game.this),
                        seconds, getConfig().getMinimumGroups(), getConfig().getMinimumPlayers(),
                        getGroupParticipants().size(), getPlayerParticipants().size()));
                times--;
            } else {
                preLobbyEnd();
                this.cancel();
            }
        }
    }

    private void preLobbyEnd() {
        if (canStartBattle()) {
            deleteGroups();
            lobby = false;
            onLobbyEnd();
            addTask(new GameExpirationTask().runTaskLater(plugin, getConfig().getExpirationTime() * 20));
            int gameInfoInterval = getConfig().getAnnouncementGameInfoInterval() * 20;
            addTask(new ArenaAnnouncementTask().runTaskTimer(plugin, gameInfoInterval, gameInfoInterval));
        } else {
            finish(true);
        }
    }

    protected abstract @NotNull String getGameInfoMessage();

    protected class ArenaAnnouncementTask extends BukkitRunnable {

        @Override
        public void run() {
            gameManager.broadcast(getGameInfoMessage(), Game.this);
        }
    }

    protected class GameExpirationTask extends BukkitRunnable {

        @Override
        public void run() {
            gameManager.getCurrentGame().ifPresent(game -> {
                game.finish(true);
                gameManager.broadcastKey("game_expired", game);
            });
        }
    }
}
