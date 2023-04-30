package me.roinujnosde.titansbattle;

import me.roinujnosde.titansbattle.events.*;
import me.roinujnosde.titansbattle.exceptions.CommandNotSupportedException;
import me.roinujnosde.titansbattle.hooks.papi.PlaceholderHook;
import me.roinujnosde.titansbattle.managers.GameManager;
import me.roinujnosde.titansbattle.managers.GroupManager;
import me.roinujnosde.titansbattle.types.Group;
import me.roinujnosde.titansbattle.types.Kit;
import me.roinujnosde.titansbattle.types.Warrior;
import me.roinujnosde.titansbattle.utils.SoundUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.roinujnosde.titansbattle.BaseGameConfiguration.Prize;
import static me.roinujnosde.titansbattle.utils.SoundUtils.Type.*;
import static org.bukkit.ChatColor.*;

public abstract class BaseGame {

    private static final int MAX_ENTRANCES = 2;

    protected final TitansBattle plugin;
    protected final GroupManager groupManager;
    protected final GameManager gameManager;

    protected BaseGameConfiguration config;
    protected boolean lobby;
    protected boolean battle;
    protected final List<Warrior> participants = new ArrayList<>();
    protected final HashMap<Warrior, Integer> killsCount = new HashMap<>();
    protected final Set<Warrior> casualties = new HashSet<>();
    protected final Set<Warrior> casualtiesWatching = new HashSet<>();

    private final List<BukkitTask> tasks = new ArrayList<>();
    private LobbyAnnouncementTask lobbyTask;

    public BaseGame(TitansBattle plugin, BaseGameConfiguration config) {
        this.plugin = plugin;
        this.groupManager = plugin.getGroupManager();
        this.gameManager = plugin.getGameManager();
        this.config = config;
        if (getConfig().isGroupMode() && groupManager == null) {
            throw new IllegalStateException("gameManager cannot be null in a group mode game");
        }
    }

    public void start() {
        if (getConfig().isGroupMode() && plugin.getGroupManager() == null) {
            throw new IllegalStateException("You cannot start a group based game without a supported Groups plugin!");
        }
        if (!getConfig().locationsSet()) {
            throw new IllegalStateException("You didn't set all locations!");
        }
        LobbyStartEvent event = new LobbyStartEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        lobby = true;
        Integer interval = getConfig().getAnnouncementStartingInterval();
        lobbyTask = new LobbyAnnouncementTask(getConfig().getAnnouncementStartingTimes(), interval);
        addTask(lobbyTask.runTaskTimer(plugin, 0, interval * 20));
    }

    public void finish(boolean cancelled) {
        teleportAll(getConfig().getExit());
        killTasks();
        runCommandsAfterBattle(getParticipants());
        if (getConfig().isUseKits()) {
            getPlayerParticipantsStream().forEach(Kit::clearInventory);
        }
        if (getConfig().isWorldBorder()) {
            getConfig().getBorderCenter().getWorld().getWorldBorder().reset();
        }
        Bukkit.getScheduler().runTask(plugin, () -> plugin.getDatabaseManager().saveAll());
        if (!cancelled) {
            processWinners();
        }
    }

    public abstract void setWinner(@NotNull Warrior warrior) throws CommandNotSupportedException;

    public void cancel(@NotNull CommandSender sender) {
        broadcastKey("cancelled", sender.getName());
        finish(true);
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
        if (!teleport(warrior, getConfig().getLobby())) {
            plugin.debug(String.format("Player %s is dead: %s", player, player.isDead()), false);
            player.sendMessage(getLang("teleport.error"));
            return;
        }
        SoundUtils.playSound(JOIN_GAME, plugin.getConfig(), player);
        participants.add(warrior);
        setKit(warrior);
        broadcastKey("player_joined", warrior.getName());
        player.sendMessage(getLang("objective"));
        if (participants.size() == getConfig().getMaximumPlayers() && lobbyTask != null) {
            lobbyTask.processEnd();
        }
    }

    public void onDeath(@NotNull Warrior victim, @Nullable Warrior killer) {
        if (!isParticipant(victim)) {
            return;
        }
        if (!isLobby()) {
            ParticipantDeathEvent event = new ParticipantDeathEvent(victim, killer);
            Bukkit.getPluginManager().callEvent(event);
            String gameName = getConfig().getName();
            casualties.add(victim);
            if (getConfig().isGroupMode()) {
                victim.sendMessage(getLang("watch_to_the_end"));
            }
            if (killer != null) {
                killer.increaseKills(gameName);
                increaseKills(killer);
            }
            victim.increaseDeaths(gameName);
            playDeathSound(victim);
        }
        broadcastDeathMessage(victim, killer);
        processPlayerExit(victim);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isLobby() {
        return lobby;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public abstract boolean isInBattle(@NotNull Warrior warrior);

    public @NotNull BaseGameConfiguration getConfig() {
        return config;
    }

    public boolean isParticipant(@NotNull Warrior warrior) {
        return participants.contains(warrior);
    }

    public void onDisconnect(@NotNull Warrior warrior) {
        if (!isParticipant(warrior)) {
            return;
        }
        if (getConfig().isUseKits()) {
            plugin.getConfigManager().getClearInventory().add(warrior.getUniqueId());
        }
        if (!isLobby() && getCurrentFighters().contains(warrior)) {
            //processInventoryOnExit(warrior);
            //onDeath(warrior, getLastAttacker(warrior));
            Player player = warrior.toOnlinePlayer();
            if (player != null) {
                player.setHealth(0);
            }
            return;
        }
        casualties.add(warrior);
        casualtiesWatching.add(warrior); //adding to this Collection, so they are not teleported on respawn
        plugin.getConfigManager().getRespawn().add(warrior.getUniqueId());
        plugin.getConfigManager().save();
        processPlayerExit(warrior);
    }

    public void onLeave(@NotNull Warrior warrior) {
        if (!isParticipant(warrior)) {
            return;
        }
        if (getConfig().isUseKits()) {
            Kit.clearInventory(warrior.toOnlinePlayer());
        }
        Player player = Objects.requireNonNull(warrior.toOnlinePlayer());
        if (!isLobby() && getCurrentFighters().contains(warrior)) {
            //processInventoryOnExit(warrior);
            //onDeath(warrior, getLastAttacker(warrior));
            player.setHealth(0);
            return;
        }
        player.sendMessage(getLang("you-have-left"));
        SoundUtils.playSound(LEAVE_GAME, plugin.getConfig(), player);
        processPlayerExit(warrior);
    }

    protected @Nullable Warrior getLastAttacker(@NotNull Warrior victim) {
        Player player = victim.toOnlinePlayer();
        EntityDamageEvent event = player != null ? player.getLastDamageCause() : null;
        if (event instanceof EntityDamageByEntityEvent) {
            Entity attacker = ((EntityDamageByEntityEvent) event).getDamager();
            if (attacker instanceof Player) {
                return plugin.getDatabaseManager().getWarrior((Player) attacker);
            }
            if (attacker instanceof Projectile) {
                return plugin.getDatabaseManager().getWarrior((Player) ((Projectile) attacker).getShooter());
            }
        }
        return null;
    }

    protected void processInventoryOnExit(@NotNull Warrior warrior) {
        Player player = warrior.toOnlinePlayer();
        if (player == null) {
            plugin.debug("processInventoryOnExit() -> null player");
            return;
        }
        World world = player.getWorld();
        if (shouldKeepInventoryOnDeath(warrior) || Boolean.parseBoolean(world.getGameRuleValue("keepInventory"))) {
            return;
        }
        if (shouldClearDropsOnDeath(warrior)) {
            return;
        }
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            world.dropItemNaturally(player.getLocation(), item.clone());
        }
        Kit.clearInventory(player);
    }

    public void onRespawn(@NotNull Warrior warrior) {
        if (casualties.contains(warrior) && !casualtiesWatching.contains(warrior)) {
            teleport(warrior, getConfig().getWatchroom());
            casualtiesWatching.add(warrior);
        }
    }

    public abstract boolean shouldClearDropsOnDeath(@NotNull Warrior warrior);

    public abstract boolean shouldKeepInventoryOnDeath(@NotNull Warrior warrior);

    public @NotNull List<Warrior> getParticipants() {
        return Collections.unmodifiableList(participants);
    }

    @NotNull
    protected Stream<Player> getPlayerParticipantsStream() {
        return getParticipants().stream().map(Warrior::toOnlinePlayer).filter(Objects::nonNull);
    }

    public Map<Group, Integer> getGroupParticipants() {
        if (!getConfig().isGroupMode()) {
            return Collections.emptyMap();
        }
        Map<Group, Integer> groups = new HashMap<>();
        for (Warrior w : participants) {
            groups.compute(w.getGroup(), (g, i) -> i == null ? 1 : i + 1);
        }
        return groups;
    }

    public Collection<Warrior> getCasualties() {
        return casualties;
    }

    public abstract @NotNull Collection<Warrior> getCurrentFighters();

    public HashMap<Warrior, Integer> getKillsCount() {
        return killsCount;
    }

    public void broadcastKey(@NotNull String key, Object... args) {
        broadcast(getLang(key), args);
    }

    public void discordAnnounce(@NotNull String key, Object... args) {
        plugin.sendDiscordMessage(getLang(key, args));
    }

    public void broadcast(@Nullable String message, Object... args) {
        if (message == null || message.isEmpty()) {
            return;
        }
        message = MessageFormat.format(message, args);
        if (message.startsWith("!!broadcast")) {
            Bukkit.broadcastMessage(message.replace("!!broadcast", ""));
        } else {
            for (Warrior warrior : getParticipants()) {
                warrior.sendMessage(message);
            }
        }
    }

    @Override
    public int hashCode() {
        return getConfig().getName().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof BaseGame)) {
            return false;
        }
        BaseGame otherGame = (BaseGame) other;
        return otherGame.getConfig().getName().equals(getConfig().getName());
    }

    public @NotNull String getLang(@NotNull String key, Object... args) {
        return plugin.getLang(key, this, args);
    }

    protected boolean teleport(@Nullable Warrior warrior, @NotNull Location destination) {
        plugin.debug(String.format("teleport() -> destination %s", destination));
        Player player = warrior != null ? warrior.toOnlinePlayer() : null;
        if (player == null) {
            plugin.debug(String.format("teleport() -> warrior %s", warrior));
            return false;
        }
        SoundUtils.playSound(TELEPORT, plugin.getConfig(), player);
        return player.teleport(destination);
    }

    protected void addTask(@NotNull BukkitTask task) {
        tasks.add(task);
    }

    protected void killTasks() {
        tasks.forEach(BukkitTask::cancel);
        tasks.clear();
    }

    protected void increaseKills(Warrior warrior) {
        killsCount.compute(warrior, (p, i) -> i == null ? 1 : i + 1);
    }

    protected abstract void onLobbyEnd();

    protected abstract void processWinners();

    protected void givePrizes(Prize prize, @Nullable Group group, @Nullable List<Warrior> warriors) {
        List<Player> leaders = new ArrayList<>();
        List<Player> members = new ArrayList<>();
        if (warriors == null) {
            return;
        }
        List<Player> players = warriors.stream().map(Warrior::toOnlinePlayer).filter(Objects::nonNull)
                .collect(Collectors.toList());
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

    protected boolean canStartBattle() {
        GameStartEvent event = new GameStartEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            broadcastKey("cancelled", "CONSOLE");
            return false;
        }
        if (getParticipants().size() < getConfig().getMinimumPlayers()) {
            broadcastKey("not_enough_participants");
            return false;
        }
        if (getConfig().isGroupMode()) {
            if (getGroupParticipants().size() < getConfig().getMinimumGroups()) {
                broadcastKey("not_enough_participants");
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings({"BooleanMethodIsAlwaysInverted"})
    protected boolean canJoin(@NotNull Warrior warrior) {
        Player player = warrior.toOnlinePlayer();
        if (player == null) {
            plugin.getLogger().log(Level.WARNING, "Joining player {0} ({1}) is null",
                    new Object[]{warrior.getName(), warrior.getUniqueId()});
            return false;
        }

        PlayerJoinGameEvent event = new PlayerJoinGameEvent(warrior, player, this);
        Bukkit.getPluginManager().callEvent(event);
        plugin.debug("cancel: " + event.isCancelled());

        return !event.isCancelled();
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
        participants.remove(warrior);
        Group group = warrior.getGroup();
        if (!isLobby()) {
            runCommandsAfterBattle(Collections.singletonList(warrior));
            processRemainingPlayers(warrior);
            //last participant
            if (getConfig().isGroupMode() && group != null && !getGroupParticipants().containsKey(group)) {
                broadcastKey("group_defeated", group.getName());
                Bukkit.getPluginManager().callEvent(new GroupDefeatedEvent(group, warrior.toOnlinePlayer()));
                group.getData().increaseDefeats(getConfig().getName());
            }
            sendRemainingOpponentsCount();
        }
    }

    protected abstract void processRemainingPlayers(@NotNull Warrior warrior);

    protected void setKit(@NotNull Warrior warrior) {
        Player player = warrior.toOnlinePlayer();
        Kit kit = getConfig().getKit();
        if (getConfig().isUseKits() && kit != null && player != null) {
            Kit.clearInventory(player);
            kit.set(player);
        }
    }

    protected void playDeathSound(@NotNull Warrior victim) {
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
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(MessageFormat.format(getLang("action-bar-remaining-opponents"), remaining)));
            });
        } catch (NoSuchMethodError ignored) {
        }
    }

    protected int getRemainingOpponents(@NotNull Player player) {
        if (!getConfig().isGroupMode()) {
            return getParticipants().size() - 1;
        }
        int opponents = 0;
        Warrior warrior = plugin.getDatabaseManager().getWarrior(player);
        for (Map.Entry<Group, Integer> entry : getGroupParticipants().entrySet()) {
            Group group = entry.getKey();
            if (group.equals(warrior.getGroup())) {
                continue;
            }
            opponents += entry.getValue();
        }
        return opponents;
    }

    protected void runCommandsBeforeBattle(@NotNull Collection<Warrior> warriors) {
        runCommands(warriors, getConfig().getCommandsBeforeBattle());
    }

    protected void runCommandsAfterBattle(@NotNull Collection<Warrior> warriors) {
        runCommands(warriors, getConfig().getCommandsAfterBattle());
    }

    protected void runCommands(@NotNull Collection<Warrior> warriors, @Nullable Collection<String> commands) {
        if (commands == null) return;
        PlaceholderHook hook = plugin.getPlaceholderHook();

        for (String command : commands) {
            for (Warrior warrior : warriors) {
                Player player = warrior.toOnlinePlayer();
                if (player == null) {
                    continue;
                }
                if (!command.contains("%player%")) { // Runs the command once when %player% is not used
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), hook.parse((OfflinePlayer) null, command));
                    break;
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), hook.parse(warrior, command,
                        "%player%", warrior.getName()));
            }
        }
    }

    protected void teleport(@NotNull Collection<Warrior> warriors, @NotNull Location destination) {
        warriors.forEach(warrior -> teleport(warrior, destination));
    }

    protected void teleportAll(Location destination) {
        getParticipants().forEach(player -> teleport(player, destination));
    }

    protected void teleportToArena(List<Warrior> warriors) {
        Location entrance1 = getConfig().getArenaEntrance(1);
        Location entrance2 = getConfig().getArenaEntrance(2);
        if (entrance2 == null) {
            teleport(warriors, entrance1);
            return;
        }
        if (getConfig().isGroupMode()) {
            List<Group> groups = warriors.stream().map(Warrior::getGroup).distinct().collect(Collectors.toList());
            if (groups.size() != MAX_ENTRANCES) {
                teleport(warriors, entrance1);
                return;
            }
            for (Warrior warrior : warriors) {
                if (groups.get(0).equals(warrior.getGroup())) {
                    teleport(warrior, entrance1);
                } else {
                    teleport(warrior, entrance2);
                }
            }
        } else {
            if (warriors.size() != MAX_ENTRANCES) {
                teleport(warriors, entrance1);
                return;
            }
            teleport(warriors.get(0), entrance1);
            teleport(warriors.get(1), entrance2);
        }
    }

    @SuppressWarnings("deprecation")
    protected void broadcastDeathMessage(@NotNull Warrior victim, @Nullable Warrior killer) {
        if (killer == null) {
            broadcastKey("died_by_himself", victim.getName());
        } else {
            ItemStack itemInHand = Objects.requireNonNull(killer.toOnlinePlayer()).getItemInHand();
            String weaponName = getLang("fist");
            if (itemInHand != null && itemInHand.getType() != Material.AIR) {
                ItemMeta itemMeta = itemInHand.getItemMeta();
                if (itemMeta != null && itemMeta.hasDisplayName()) {
                    weaponName = itemMeta.getDisplayName();
                }
            }
            broadcastKey("killed_by", victim.getName(), killsCount.getOrDefault(victim, 0), killer.getName(), killsCount.get(killer), weaponName);
        }
    }

    protected void startPreparation() {
        addTask(new PreparationTimeTask().runTaskLater(plugin, getConfig().getPreparationTime() * 20));
        addTask(new CountdownTitleTask(getCurrentFighters(), getConfig().getPreparationTime()).runTaskTimer(plugin, 0L, 20L));
        if (getConfig().isWorldBorder()) {
            long borderInterval = getConfig().getBorderInterval() * 20L;
            WorldBorder worldBorder = getConfig().getBorderCenter().getWorld().getWorldBorder();
            addTask(new BorderTask(worldBorder).runTaskTimer(plugin, borderInterval, borderInterval));
        }
    }

    public class LobbyAnnouncementTask extends BukkitRunnable {
        private int times;
        private final long interval;

        public LobbyAnnouncementTask(int times, long interval) {
            this.times = times + 1;
            this.interval = interval;
        }

        @Override
        public void run() {
            long seconds = times * interval;
            if (times > 0) {
                broadcastKey("starting_game", seconds, getConfig().getMinimumGroups(), getConfig().getMinimumPlayers(), getGroupParticipants().size(), getParticipants().size());
                times--;
            } else {
                processEnd();
            }
        }

        public void processEnd() {
            if (canStartBattle()) {
                lobby = false;
                onLobbyEnd();
                addTask(new GameExpirationTask().runTaskLater(plugin, getConfig().getExpirationTime() * 20));
            } else {
                finish(true);
            }
            this.cancel();
            lobbyTask = null;
        }
    }

    public class BorderTask extends BukkitRunnable {

        private final WorldBorder worldBorder;
        private int currentSize;

        public BorderTask(WorldBorder worldBorder) {
            this.worldBorder = worldBorder;
            currentSize = getConfig().getBorderInitialSize();
            worldBorder.setCenter(getConfig().getBorderCenter());
            worldBorder.setSize(currentSize);
            worldBorder.setDamageAmount(getConfig().getBorderDamage());
            worldBorder.setDamageBuffer(0);
        }

        @Override
        @SuppressWarnings("deprecation")
        public void run() {
            int shrinkSize = getConfig().getBorderShrinkSize();
            int newSize = currentSize - shrinkSize;

            if (getConfig().getBorderFinalSize() > newSize) {
                this.cancel();
                return;
            }
            worldBorder.setSize(newSize, shrinkSize);
            getPlayerParticipantsStream().forEach(player -> {
                player.sendTitle(getLang("border.title"), getLang("border.subtitle"));
                SoundUtils.playSound(BORDER, getConfig().getFileConfiguration(), player);
            });
            currentSize = newSize;
        }

    }

    public class PreparationTimeTask extends BukkitRunnable {

        @Override
        public void run() {
            broadcastKey("preparation_over");
            runCommandsBeforeBattle(getCurrentFighters());
            battle = true;
        }
    }

    public class CountdownTitleTask extends BukkitRunnable {

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
            List<Player> players = warriors.stream().map(Warrior::toOnlinePlayer).filter(Objects::nonNull).collect(Collectors.toList());
            String title;
            if (timer > 0) {
                title = getColor() + "" + timer;
            } else {
                title = RED + getLang("title.fight");
                this.cancel();
                Bukkit.getScheduler().runTaskLater(plugin, () -> players.forEach(Player::resetTitle), 20L);
            }
            players.forEach(player -> player.sendTitle(title, ""));
            timer--;
        }

        private ChatColor getColor() {
            ChatColor color = GREEN;
            if (timer <= 3) {
                color = RED;
            } else if (timer <= 7) {
                color = YELLOW;
            }
            return color;
        }
    }

    public class GameExpirationTask extends BukkitRunnable {

        @Override
        public void run() {
            gameManager.getCurrentGame().ifPresent(game -> {
                game.finish(true);
                broadcastKey("game_expired");
            });
        }
    }
}
