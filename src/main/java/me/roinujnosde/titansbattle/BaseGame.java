package me.roinujnosde.titansbattle;

import me.roinujnosde.titansbattle.events.*;
import me.roinujnosde.titansbattle.managers.GameManager;
import me.roinujnosde.titansbattle.managers.GroupManager;
import me.roinujnosde.titansbattle.types.Group;
import me.roinujnosde.titansbattle.types.Kit;
import me.roinujnosde.titansbattle.types.Warrior;
import me.roinujnosde.titansbattle.utils.SoundUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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

public abstract class BaseGame {

    protected final TitansBattle plugin;
    protected final GroupManager groupManager;
    protected final GameManager gameManager;

    protected boolean lobby;
    protected boolean battle;
    protected final List<Warrior> participants = new ArrayList<>();
    protected final HashMap<Warrior, Integer> killsCount = new HashMap<>();
    protected final Set<Warrior> casualties = new HashSet<>();
    protected final Set<Warrior> casualtiesWatching = new HashSet<>();

    private final List<BukkitTask> tasks = new ArrayList<>();

    public BaseGame(TitansBattle plugin) {
        this.plugin = plugin;
        this.groupManager = plugin.getGroupManager();
        this.gameManager = plugin.getGameManager();
        if (getConfig().isGroupMode() && groupManager == null) {
            throw new IllegalStateException("gameManager cannot be null in a group mode game");
        }
    }

    public abstract void start();

    public abstract void finish(boolean cancelled);

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
            player.sendMessage(plugin.getLang("teleport.error", this));
            return;
        }
        SoundUtils.playSound(JOIN_GAME, plugin.getConfig(), player);
        participants.add(warrior);
        setKit(warrior);
        broadcastKey("player_joined", player.getName());
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
                victim.sendMessage(plugin.getLang("watch_to_the_end", this));
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

    public abstract boolean isInBattle(@NotNull Warrior warrior);

    public abstract @NotNull BaseGameConfiguration getConfig();

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
        player.sendMessage(plugin.getLang("you-have-left", this));
        SoundUtils.playSound(LEAVE_GAME, plugin.getConfig(), player);
        processPlayerExit(warrior);
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

    public Set<Warrior> getCasualties() {
        return casualties;
    }

    public abstract @NotNull Collection<Warrior> getCurrentFighters();

    public HashMap<Warrior, Integer> getKillsCount() {
        return killsCount;
    }

    public void broadcastKey(@NotNull String key, Object... args) {
        broadcast(getLang(key), args);
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

    protected @Nullable String getLang(@NotNull String key) {
        throw new UnsupportedOperationException(); //TODO Implement
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

    protected boolean canStartBattle() {
        // TODO Fix broadcast methods to respect challenge mode
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

    @SuppressWarnings({"BooleanMethodIsAlwaysInverted", "ConstantConditions"}) // TODO Report possible bug on ConstantConditions
    protected boolean canJoin(@NotNull Warrior warrior) {
        Player player = warrior.toOnlinePlayer();
        String reason = null;
        if (player == null) {
            plugin.debug(String.format("canJoin() -> player %s %s == null", warrior.getName(), warrior.getUniqueId()), false);
            return false;
        }
        if (!isLobby()) {
            reason = plugin.getLang("game_is_happening", this);
            plugin.debug("happening");
        }
        if (isParticipant(warrior)) {
            reason = plugin.getLang("already-joined", this);
            plugin.debug("already in");
        }
        if (participants.size() >= getConfig().getMaximumPlayers() && getConfig().getMaximumPlayers() > 0) {
            reason = plugin.getLang("maximum-players", this);
            plugin.debug("max players");
        }
        if (getConfig().isGroupMode()) {
            if (warrior.getGroup() == null) {
                reason = plugin.getLang("not_in_a_group", this);
                plugin.debug("not in group");
            }
            if (!getGroupParticipants().containsKey(warrior.getGroup())
                    && getGroupParticipants().size() >= getConfig().getMaximumGroups()
                    && getConfig().getMaximumGroups() > 0) {
                reason = plugin.getLang("maximum-groups", this);
                plugin.debug("max groups");
            }
            Integer amountOfPlayers = getGroupParticipants().getOrDefault(warrior.getGroup(), 0);
            if (amountOfPlayers >= getConfig().getMaximumPlayersPerGroup() &&
                    getConfig().getMaximumPlayersPerGroup() > 0) {
                reason = plugin.getLang("maximum-players-per-group", this);
                plugin.debug("max per group");
            }
        }
        if (getConfig().isUseKits() && Kit.inventoryHasItems(player)) {
            reason = plugin.getLang("clear-your-inventory", this);
            plugin.debug("clear inv");
        }

        PlayerJoinGameEvent event = new PlayerJoinGameEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);
        plugin.debug("cancel: " + event.isCancelled());
        plugin.debug("reason: >>>" + reason + "<<<");
        if (reason != null) {
            player.sendMessage(reason);
        }
        return reason == null && !event.isCancelled();
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
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(
                        MessageFormat.format(plugin.getLang("action-bar-remaining-opponents", this),
                                remaining)));
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

    protected void teleportAll(Location destination) {
        getParticipants().forEach(player -> teleport(player, destination));
    }

    @SuppressWarnings("deprecation")
    protected void broadcastDeathMessage(@NotNull Warrior victim, @Nullable Warrior killer) {
        if (killer == null) {
            broadcastKey("died_by_himself", victim.getName());
        } else {
            ItemStack itemInHand = Objects.requireNonNull(killer.toOnlinePlayer()).getItemInHand();
            String weaponName = plugin.getLang("fist", this);
            if (itemInHand != null && itemInHand.getType() != Material.AIR) {
                ItemMeta itemMeta = itemInHand.getItemMeta();
                if (itemMeta != null && itemMeta.hasDisplayName()) {
                    weaponName = itemMeta.getDisplayName();
                }
            }
            broadcastKey("killed_by", victim.getName(), killsCount.getOrDefault(victim, 0),
                    killer.getName(), killsCount.get(killer), weaponName);
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
            List<Player> players = warriors.stream().map(Warrior::toOnlinePlayer).filter(Objects::nonNull)
                    .collect(Collectors.toList());
            String title;
            if (timer > 0) {
                title = getColor() + "" + timer;
            } else {
                title = ChatColor.RED + plugin.getLang("title.fight", BaseGame.this);
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
