package me.roinujnosde.titansbattle;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.roinujnosde.titansbattle.types.GameConfiguration;
import me.roinujnosde.titansbattle.types.Winners;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TBExpansion extends PlaceholderExpansion {

    private final TitansBattle plugin;
    private static final Pattern PREFIX_PATTERN = Pattern.compile("(?<game>^[A-Za-z]+)_(?<type>winner|killer)_prefix");

    public TBExpansion(TitansBattle plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return plugin.getName().toLowerCase();
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        Matcher matcher = PREFIX_PATTERN.matcher(params);
        if (player != null && matcher.find()) {
            String game = matcher.group("game");
            String type = matcher.group("type").toLowerCase();
            switch (type) {
                case "killer":
                    return getKillerPrefix(player, game);
                case "winner":
                    return getWinnerPrefix(player, game);
            }
        }
        return "";
    }

    @NotNull
    public String getWinnerPrefix(@NotNull OfflinePlayer player, @NotNull String game) {
        GameConfiguration gameConfig = GameConfigurationDao.getInstance(plugin).getGameConfiguration(game);
        if (gameConfig == null) {
            plugin.debug(String.format("game %s not found", game));
            return "";
        }
        Winners latestWinners = plugin.getDatabaseManager().getLatestWinners();
        List<UUID> playerWinners = latestWinners.getPlayerWinners(game);
        if (playerWinners == null || !playerWinners.contains(player.getUniqueId())) {
            plugin.debug(String.format("player winners: %s", playerWinners));
            return "";
        }
        String prefix = gameConfig.getWinnerPrefix();
        plugin.debug("prefix: " + prefix);
        return prefix != null ? prefix : "";
    }

    @NotNull
    public String getKillerPrefix(@NotNull OfflinePlayer player, @NotNull String game) {
        GameConfiguration gameConfig = GameConfigurationDao.getInstance(plugin).getGameConfiguration(game);
        if (gameConfig == null) {
            return "";
        }
        Winners latestWinners = plugin.getDatabaseManager().getLatestWinners();
        UUID killerUuid = latestWinners.getKiller(game);
        if (killerUuid == null || !killerUuid.equals(player.getUniqueId())) {
            return "";
        }
        String prefix = gameConfig.getKillerPrefix();
        return prefix != null ? prefix : "";
    }
}
