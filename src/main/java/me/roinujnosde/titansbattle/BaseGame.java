package me.roinujnosde.titansbattle;

import me.roinujnosde.titansbattle.managers.GroupManager;
import me.roinujnosde.titansbattle.types.Kit;
import me.roinujnosde.titansbattle.types.Warrior;
import me.roinujnosde.titansbattle.utils.SoundUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Stream;

import static me.roinujnosde.titansbattle.utils.SoundUtils.Type.JOIN_GAME;
import static me.roinujnosde.titansbattle.utils.SoundUtils.Type.TELEPORT;

public abstract class BaseGame {

    protected final TitansBattle plugin;
    protected final GroupManager groupManager;

    protected final List<Warrior> participants = new ArrayList<>();
    protected final HashMap<Warrior, Integer> killsCount = new HashMap<>();
    protected final Set<Warrior> casualties = new HashSet<>();
    protected final Set<Warrior> casualtiesWatching = new HashSet<>();

    public BaseGame(TitansBattle plugin) {
        this.plugin = plugin;
        this.groupManager = plugin.getGroupManager();
        if (getConfig().isGroupMode() && groupManager == null) {
            throw new IllegalStateException("gameManager cannot be null in a group mode game");
        }
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
        sendMessageToParticipants(MessageFormat.format(plugin.getLang("player_joined", this), player.getName()));
    }

    public abstract @NotNull BaseGameConfiguration getConfig();

    public void sendMessageToParticipants(@NotNull String message) {
        getPlayerParticipantsStream().forEach(p -> p.sendMessage(message));
    }

    public @NotNull List<Warrior> getParticipants() {
        return Collections.unmodifiableList(participants);
    }

    @NotNull
    protected Stream<Player> getPlayerParticipantsStream() {
        return getParticipants().stream().map(Warrior::toOnlinePlayer).filter(Objects::nonNull);
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected abstract boolean canJoin(@NotNull Warrior warrior);

    protected void setKit(@NotNull Warrior warrior) {
        Player player = warrior.toOnlinePlayer();
        Kit kit = getConfig().getKit();
        if (getConfig().isUseKits() && kit != null && player != null) {
            Kit.clearInventory(player);
            kit.set(player);
        }
    }
}
