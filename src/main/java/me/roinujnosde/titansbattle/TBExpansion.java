package me.roinujnosde.titansbattle;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.roinujnosde.titansbattle.types.GameConfiguration;
import me.roinujnosde.titansbattle.types.Warrior;
import me.roinujnosde.titansbattle.types.Winners;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
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
        if (params.startsWith("arena_in_use_")) {
            String arenaName = params.replace("arena_in_use_", "");
            return toString(plugin.getChallengeManager().isArenaInUse(arenaName));
        }

        if (player == null) {
            return "";
        }
        Matcher prefixMatcher = PREFIX_PATTERN.matcher(params);
        if (prefixMatcher.find()) {
            String game = prefixMatcher.group("game");
            String type = prefixMatcher.group("type").toLowerCase();
            switch (type) {
                case "killer":
                    return getKillerPrefix(player, game);
                case "winner":
                    return getWinnerPrefix(player, game);
            }
        }
        Warrior warrior = plugin.getDatabaseManager().getWarrior(player);
        switch (params) {
            case "group_total_victories":
                return warrior.getGroup() != null ? String.valueOf(warrior.getGroup().getData().getTotalVictories()) : "0";
            case "total_victories":
                return String.valueOf(warrior.getTotalVictories());
            case "total_kills":
                return String.valueOf(warrior.getTotalKills());
            case "total_deaths":
                return String.valueOf(warrior.getTotalDeaths());
        }
        return null;
    }

    @NotNull
    public String getWinnerPrefix(@NotNull OfflinePlayer player, @NotNull String game) {
        Optional<GameConfiguration> config = plugin.getConfigurationDao().getConfiguration(game, GameConfiguration.class);
        if (!config.isPresent()) {
            plugin.debug(String.format("game %s not found", game));
            return "";
        }
        Winners latestWinners = plugin.getDatabaseManager().getLatestWinners();
        List<UUID> playerWinners = latestWinners.getPlayerWinners(game);
        if (playerWinners == null || !playerWinners.contains(player.getUniqueId())) {
            plugin.debug(String.format("player winners: %s", playerWinners));
            return "";
        }
        String prefix = config.get().getWinnerPrefix();
        plugin.debug("prefix: " + prefix);
        return prefix != null ? prefix : "";
    }

    @NotNull
    public String getKillerPrefix(@NotNull OfflinePlayer player, @NotNull String game) {
        Optional<GameConfiguration> config = plugin.getConfigurationDao().getConfiguration(game, GameConfiguration.class);
        if (!config.isPresent()) {
            return "";
        }
        Winners latestWinners = plugin.getDatabaseManager().getLatestWinners();
        UUID killerUuid = latestWinners.getKiller(game);
        if (killerUuid == null || !killerUuid.equals(player.getUniqueId())) {
            return "";
        }
        String prefix = config.get().getKillerPrefix();
        return prefix != null ? prefix : "";
    }

    private String toString(boolean bool) {
        return bool ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
    }
}
