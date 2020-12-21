package me.roinujnosde.titansbattle.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.*;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.dao.GameConfigurationDao;
import me.roinujnosde.titansbattle.events.PlayerExitGameEvent;
import me.roinujnosde.titansbattle.managers.ConfigManager;
import me.roinujnosde.titansbattle.managers.DatabaseManager;
import me.roinujnosde.titansbattle.managers.GameManager;
import me.roinujnosde.titansbattle.types.*;
import me.roinujnosde.titansbattle.utils.ConfigUtils;
import me.roinujnosde.titansbattle.utils.Helper;
import me.roinujnosde.titansbattle.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@CommandAlias("%titansbattle|tb")
public class TBCommands extends BaseCommand {

    @Dependency
    private TitansBattle plugin;
    @Dependency
    private GameManager gameManager;
    @Dependency
    private ConfigManager configManager;
    @Dependency
    private DatabaseManager databaseManager;
    @Dependency
    private GameConfigurationDao gameConfigDao;

    @Subcommand("%create|create")
    @CommandPermission("titansbattle.create")
    public void create(CommandSender sender, String game) {
        game = game.replace(" ", "_").replace(".", "");
        gameConfigDao.createGame(game);
        sender.sendMessage(String.format(ChatColor.GREEN + "[TitansBattle] Created game %s! Now edit its file in " +
                "the games folder and reload.", game));
        gameConfigDao.loadGameConfigurations();
    }

    @Subcommand("%edit|edit %prize|prize")
    @CommandPermission("titansbattle.edit")
    @CommandCompletion("@games @prizes_config_fields")
    public void editPrizes(CommandSender sender,
                           @Values("@games") String game,
                           @Values("@prizes_config_fields") String field,
                           String value) {
        GameConfiguration config = gameConfigDao.getGameConfiguration(game);
        Prizes prizes = Objects.requireNonNull(config).getPrizes();
        if (ConfigUtils.setValue(prizes, field, value)) {
            sender.sendMessage(ChatColor.GREEN + "[TitansBattle] Successfully changed the field's value!");
            gameConfigDao.save(config);
        } else {
            sender.sendMessage(ChatColor.GREEN + "[TitansBattle] Error changing the field's value!");
        }
    }

    @Subcommand("%edit|edit %game|game")
    @CommandPermission("titansbattle.edit")
    @CommandCompletion("@games @game_config_fields")
    public void editGame(CommandSender sender,
                         @Values("@games") String game,
                         @Values("@game_config_fields") String field,
                         String value) {
        GameConfiguration config = gameConfigDao.getGameConfiguration(game);
        if (ConfigUtils.setValue(Objects.requireNonNull(config), field, value)) {
            sender.sendMessage(ChatColor.GREEN + "[TitansBattle] Successfully changed the field's value!");
            gameConfigDao.save(config);
        } else {
            sender.sendMessage(ChatColor.GREEN + "[TitansBattle] Error changing the field's value!");
        }
    }

    @Subcommand("%start|start")
    @CommandPermission("titansbattle.start")
    @CommandCompletion("@games")
    public void start(CommandSender sender, @Values("@games") String game) {
        if (gameManager.getCurrentGame() != null) {
            sender.sendMessage(plugin.getLang("starting-or-started", gameManager.getCurrentGame()));
            return;
        }

        GameConfiguration config = gameConfigDao.getGameConfiguration(game);
        gameManager.start(new Game(Objects.requireNonNull(config)));
    }

    @Subcommand("%cancel|cancel")
    @CommandPermission("titansbattle.cancel")
    @Conditions("happening")
    public void cancelGame(CommandSender sender) {
        gameManager.cancelGame(sender);
    }

    public enum Destination {
        EXIT, ARENA, LOBBY, WATCHROOM
    }

    @Subcommand("%setdestination|setdestination GENERAL_EXIT")
    @CommandPermission("titansbattle.setdestination")
    public void setGeneralExit(Player player) {
        configManager.setGeneralExit(player.getLocation());
        configManager.save();
        player.sendMessage(MessageFormat.format(plugin.getLang("destination_setted"), "GENERAL_EXIT"));
    }

    @Subcommand("%setdestination|setdestination")
    @CommandPermission("titansbattle.setdestination")
    @CommandCompletion("@games")
    public void setDestination(Player player, @Values("@games") String game, Destination destination) {
        GameConfiguration config = Objects.requireNonNull(gameConfigDao.getGameConfiguration(game));
        Location loc = player.getLocation();
        switch (destination) {
            case EXIT:
                config.setExit(loc);
                break;
            case ARENA:
                config.setArena(loc);
                break;
            case LOBBY:
                config.setLobby(loc);
                break;
            case WATCHROOM:
                config.setWatchroom(loc);
                break;
        }
        gameConfigDao.save(config);
        player.sendMessage(MessageFormat.format(plugin.getLang("destination_setted"), destination));
    }

    @Subcommand("%reload|reload")
    @CommandPermission("titansbattle.reload")
    public void reload(CommandSender sender) {
        gameManager.finishGame(null, null, null, null);
        plugin.saveDefaultConfig();
        configManager.load();
        plugin.getLanguageManager().reload();
        gameConfigDao.loadGameConfigurations();
        sender.sendMessage(plugin.getLang("configuration-reloaded"));
    }

    @Subcommand("%setkit|setkit")
    @CommandPermission("titansbattle.setinventory")
    @CommandCompletion("@games")
    public void setKit(Player sender, @Values("@games") String game) {
        GameConfiguration config = gameConfigDao.getGameConfiguration(game);
        Objects.requireNonNull(config);
        config.setKit(new Kit(sender.getInventory()));
        saveInventory(sender, config);
    }

    @Subcommand("%setprize|setprize %members|members")
    @CommandPermission("titansbattle.setinventory")
    @CommandCompletion("@games")
    public void setMembersPrizeInventory(Player sender, @Values("@games") String game) {
        GameConfiguration config = gameConfigDao.getGameConfiguration(game);
        Objects.requireNonNull(config);
        config.getPrizes().setMemberItems(Helper.getInventoryAsList(sender));
        saveInventory(sender, config);
    }

    @Subcommand("%setprize|setprize %killer|killer")
    @CommandPermission("titansbattle.setinventory")
    @CommandCompletion("@games")
    public void setKillerPrizeInventory(Player sender, @Values("@games") String game) {
        GameConfiguration config = gameConfigDao.getGameConfiguration(game);
        Objects.requireNonNull(config);
        config.getPrizes().setKillerItems(Helper.getInventoryAsList(sender));
        saveInventory(sender, config);
    }

    @Subcommand("%setprize|setprize %leaders|leaders")
    @CommandPermission("titansbattle.setinventory")
    @CommandCompletion("@games")
    public void setLeadersPrizeInventory(Player sender, @Values("@games") String game) {
        GameConfiguration config = gameConfigDao.getGameConfiguration(game);
        Objects.requireNonNull(config);
        config.getPrizes().setLeaderItems(Helper.getInventoryAsList(sender));
        saveInventory(sender, config);
    }

    private void saveInventory(@NotNull CommandSender sender, @NotNull GameConfiguration config) {
        if (gameConfigDao.save(config)) {
            sender.sendMessage(plugin.getLang("inventory-set"));
        } else {
            sender.sendMessage(plugin.getLang("error-saving-game-config"));
        }
    }

    @Subcommand("%join|join")
    @CommandPermission("titansbattle.join")
    public void join(Player sender) {
        gameManager.addParticipant(sender);
    }

    @Subcommand("%exit|exit|leave")
    @CommandPermission("titansbattle.exit")
    @Conditions("happening")
    public void leave(Player sender, Game game) {
        if (!game.getPlayerParticipants().contains(sender.getUniqueId())) {
            sender.sendMessage(plugin.getLang("not_participating", game));
            return;
        }

        PlayerExitGameEvent event = new PlayerExitGameEvent(sender, game);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        if (gameManager.removeParticipant(sender)) {
            sender.sendMessage(plugin.getLang("you-have-left", game));
            SoundUtils.playSound(SoundUtils.Type.LEAVE_GAME, plugin.getConfig(), sender);
        }
    }

    @HelpCommand
    public void doHelp(CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("%ranking|ranking")
    class RankingCommand extends BaseCommand {

        private final int limit = configManager.getPageLimitRanking();

        private void sortGroups(final List<Group> groups, final String game, @Nullable String order) {
            groups.sort((g, g2) -> Integer.compare(g.getData().getVictories(game), g2.getData().getVictories(game)) * -1);
            if (order != null) {
                if (order.equalsIgnoreCase("kills")) {
                    groups.sort((g, g2) -> Integer.compare(g.getData().getKills(game), g2.getData().getKills(game)) * -1);
                }
                if (order.equalsIgnoreCase("deaths")) {
                    groups.sort((g, g2) -> Integer.compare(g.getData().getDeaths(game), g2.getData().getDeaths(game)) * -1);
                }
                if (order.equalsIgnoreCase("defeats")) {
                    groups.sort((g, g2) -> Integer.compare(g.getData().getDefeats(game), g2.getData().getDefeats(game)) * -1);
                }
            }
        }

        private void sortWarriors(final List<Warrior> warriors, final String game, final @Nullable String order) {
            warriors.sort((w, w2) -> Integer.compare(w.getVictories(game), w2.getVictories(game)) * -1);
            if (order != null) {
                if (order.equalsIgnoreCase("kills")) {
                    warriors.sort((w, w2) -> Integer.compare(w.getKills(game), w2.getKills(game)) * -1);
                }
                if (order.equalsIgnoreCase("deaths")) {
                    warriors.sort((w, w2) -> Integer.compare(w.getDeaths(game), w2.getDeaths(game)) * -1);
                }
            }
        }

        private int getDefeatsSize(List<Group> groups, String game) {
            int defeatsSize = String.valueOf(groups.stream().mapToInt(g -> g.getData().getDefeats(game)).max()
                    .orElse(0)).length();
            if (getDefeatsTitle().length() > defeatsSize) {
                defeatsSize = getDeathsTitle().length();
            }
            return defeatsSize;
        }

        private int getGroupsDeathsSize(List<Group> groups, String game) {
            int deathsSize = String.valueOf(groups.stream().mapToInt(g -> g.getData().getDeaths(game)).max().orElse(0)).length();
            if (getGroupsDeathsTitle().length() > deathsSize) {
                deathsSize = getGroupsDeathsTitle().length();
            }
            return deathsSize;
        }

        private int getGroupsKillsSize(List<Group> groups, String game) {
            int killsSize = String.valueOf(groups.stream().mapToInt(g -> g.getData().getKills(game)).max()
                    .orElse(0)).length();
            if (getGroupsKillsTitle().length() > killsSize) {
                killsSize = getGroupsKillsTitle().length();
            }
            return killsSize;
        }

        private int getGroupsVictoriesSize(List<Group> groups, String game) {
            int victoriesSize = String.valueOf(groups.stream().mapToInt(g -> g.getData().getVictories(game)).max()
                    .orElse(0)).length();
            if (getGroupsVictoriesTitle().length() > victoriesSize) {
                victoriesSize = getGroupsVictoriesTitle().length();
            }
            return victoriesSize;
        }

        private int getNameSize(List<Group> groups) {
            int nameSize = groups.stream().mapToInt(g -> g.getId().length()).max().orElse(0);
            if (getNameTitle().length() > nameSize) {
                nameSize = getNameTitle().length();
            }
            return nameSize;
        }

        private int getNickSize(final List<Warrior> warriors) {
            int nickSize = warriors.stream().mapToInt(w -> w.toPlayer().getName().length()).max().orElse(0);
            if (getNicknameTitle().length() > nickSize) {
                nickSize = getNicknameTitle().length();
            }
            return nickSize;
        }

        private int getVictoriesSize(final List<Warrior> warriors, final String game) {
            int victoriesSize = String.valueOf(warriors.stream().mapToInt(w -> w.getVictories(game)).max()
                    .orElse(0)).length();
            if (getVictoriesTitle().length() > victoriesSize) {
                victoriesSize = getVictoriesTitle().length();
            }
            return victoriesSize;
        }

        private int getKillsSize(final List<Warrior> warriors, final String game) {
            int killsSize = String.valueOf(warriors.stream().mapToInt(w -> w.getKills(game)).max().orElse(0))
                    .length();
            if (getKillsTitle().length() > killsSize) {
                killsSize = getKillsTitle().length();
            }
            return killsSize;
        }

        private int getDeathsSize(final List<Warrior> warriors, final String game) {
            int deathsSize = String.valueOf(warriors.stream().mapToInt(w -> w.getDeaths(game)).max().orElse(0))
                    .length();
            if (getDeathsTitle().length() > deathsSize) {
                deathsSize = getDeathsTitle().length();
            }
            return deathsSize;
        }

        private String getDefeatsTitle() {
            return plugin.getLang("groups-ranking.defeats-title");
        }

        private String getGroupsDeathsTitle() {
            return plugin.getLang("groups-ranking.deaths-title");
        }

        private String getGroupsKillsTitle() {
            return plugin.getLang("groups-ranking.kills-title");
        }

        private String getGroupsVictoriesTitle() {
            return plugin.getLang("groups-ranking.victories-title");
        }

        private String getNameTitle() {
            return plugin.getLang("groups-ranking.name-title");
        }

        private String getNicknameTitle() {
            return plugin.getLang("players-ranking.nickname-title");
        }

        private String getVictoriesTitle() {
            return plugin.getLang("players-ranking.victories-title");
        }

        private String getKillsTitle() {
            return plugin.getLang("players-ranking.kills-title");
        }

        private String getDeathsTitle() {
            return plugin.getLang("players-ranking.deaths-title");
        }

        private String makeGroupTitle(List<Group> groups, String game) {
            return plugin.getLang("groups-ranking.title")
                    .replaceAll("%name-title", getNameTitle())
                    .replaceAll("%n-space", Helper.getSpaces(getNameSize(groups) - getNameTitle().length()))
                    .replaceAll("%v-space", Helper.getSpaces(getGroupsVictoriesSize(groups, game) -
                            getGroupsVictoriesTitle().length()))
                    .replaceAll("%v-title", getGroupsVictoriesTitle())
                    .replaceAll("%k-space", Helper.getSpaces(getGroupsKillsSize(groups, game) -
                            getGroupsKillsTitle().length()))
                    .replaceAll("%k-title", getGroupsKillsTitle())
                    .replaceAll("%deaths-space", Helper.getSpaces(getGroupsDeathsSize(groups, game) -
                            getGroupsDeathsTitle().length()))
                    .replaceAll("%deaths-title", getGroupsDeathsTitle())
                    .replaceAll("%defeats-space", Helper.getSpaces(getGroupsDeathsSize(groups, game)
                            - getDefeatsTitle().length()))
                    .replaceAll("%defeats-title", getDefeatsTitle());
        }

        private String makeWarriorTitle(final List<Warrior> warriors, final String game) {
            return plugin.getLang("players-ranking.title")
                    .replaceAll("%nickname-title", getNicknameTitle())
                    .replaceAll("%v-title", getVictoriesTitle())
                    .replaceAll("%k-title", getKillsTitle())
                    .replaceAll("%d-title", getDeathsTitle())
                    .replaceAll("%n-space", Helper.getSpaces(getNickSize(warriors) - getNicknameTitle().length()))
                    .replaceAll("%v-space", Helper.getSpaces(getVictoriesSize(warriors, game) - getVictoriesTitle().length()))
                    .replaceAll("%k-space", Helper.getSpaces(getKillsSize(warriors, game) - getKillsTitle().length()))
                    .replaceAll("%d-space", Helper.getSpaces(getDeathsSize(warriors, game) - getDeathsTitle().length()));
        }

        private String makeGroupLine(Group g, final String game, String line, int pos, List<Group> groups) {
            String name = g.getName();

            final int victories = g.getData().getVictories(game);
            final int kills = g.getData().getKills(game);
            final int deaths = g.getData().getDeaths(game);
            final int defeats = g.getData().getDefeats(game);
            return line.replaceAll("%position", String.valueOf(pos))
                    .replaceAll("%name", name)
                    .replaceAll("%n-space", Helper.getSpaces(getNameSize(groups) - name.length()))
                    .replaceAll("%v-space", Helper.getSpaces(getGroupsVictoriesSize(groups, game) -
                            Helper.getLength(victories)))
                    .replaceAll("%victories", String.valueOf(victories))
                    .replaceAll("%k-space", Helper.getSpaces(getGroupsKillsSize(groups, game) -
                            Helper.getLength(kills)))
                    .replaceAll("%kills", String.valueOf(kills))
                    .replaceAll("%deaths-space", Helper.getSpaces(getGroupsDeathsSize(groups, game) -
                            Helper.getLength(deaths)))
                    .replaceAll("%deaths", String.valueOf(deaths))
                    .replaceAll("%defeats-space", Helper.getSpaces(getDefeatsSize(groups, game) -
                            Helper.getLength(defeats)))
                    .replaceAll("%defeats", String.valueOf(defeats));
        }

        private String makeWarriorLine(String line, int pos, Warrior w, String game, List<Warrior> warriors) {
            String name = w.toPlayer().getName();
            int victories = w.getVictories(game);
            int kills = w.getKills(game);
            int deaths = w.getDeaths(game);

            return line.replaceAll("%position", String.valueOf(pos))
                    .replaceAll("%nick", name)
                    .replaceAll("%n-space", Helper.getSpaces(getNickSize(warriors) - name.length()))
                    .replaceAll("%v-space", Helper.getSpaces(getVictoriesSize(warriors, game) - Helper.getLength(victories)))
                    .replaceAll("%victories", String.valueOf(victories))
                    .replaceAll("%k-space", Helper.getSpaces(getKillsSize(warriors, game) - Helper.getLength(kills)))
                    .replaceAll("%kills", String.valueOf(kills))
                    .replaceAll("%d-space", Helper.getSpaces(getDeathsSize(warriors, game) - Helper.getLength(deaths)))
                    .replaceAll("%deaths", String.valueOf(deaths));
        }

        @Subcommand("%groups|groups")
        @CommandPermission("titansbattle.ranking")
        @CommandCompletion("@games @group_order @group_pages")
        public void groupsRanking(CommandSender sender,
                                  @Values("@games") String game,
                                  @Values("@group_order") @Optional @Nullable String order,
                                  @Optional @Default("1") int page) {
            final List<Group> groups;
            if (plugin.getGroupManager() != null) {
                groups = new ArrayList<>(plugin.getGroupManager().getGroups());
            } else {
                groups = new ArrayList<>(0);
            }
            if (groups.isEmpty()) {
                sender.sendMessage(plugin.getLang("no-data-found"));
                return;
            }

            sortGroups(groups, game, order);

            int first = (page == 1) ? 0 : ((page - 1) * limit);
            int last = first + (limit - 1);

            if (groups.size() <= first) {
                sender.sendMessage(plugin.getLang("inexistent-page"));
                return;
            }

            sender.sendMessage(makeGroupTitle(groups, game));

            String line = plugin.getLang("groups-ranking.line");
            for (int i = first; i <= last; i++) {
                int pos = i + 1;
                Group g;
                try {
                    g = groups.get(i);
                } catch (IndexOutOfBoundsException ex) {
                    g = null;
                }
                if (g == null) {
                    break;
                }
                sender.sendMessage(makeGroupLine(g, game, line, pos, groups));
            }
        }

        @Subcommand("%players|players")
        @CommandPermission("titansbattle.ranking")
        @CommandCompletion("@games @warrior_order @warrior_pages")
        public void playersRanking(CommandSender sender,
                                   @Values("@games") String game,
                                   @Values("@warrior_order") @Optional @Nullable String order,
                                   @Optional @Default("1") int page) {
            final List<Warrior> warriors = new ArrayList<>(databaseManager.getWarriors());
            if (warriors.isEmpty()) {
                sender.sendMessage(plugin.getLang("no-data-found"));
                return;
            }

            sortWarriors(warriors, game, order);

            int first = (page == 1) ? 0 : ((page - 1) * limit);
            int last = first + (limit - 1);

            if (warriors.size() <= first) {
                sender.sendMessage(plugin.getLang("inexistent-page"));
                return;
            }

            sender.sendMessage(makeWarriorTitle(warriors, game));

            String line = plugin.getLang("players-ranking.line");
            for (int i = first; i <= last; i++) {
                int pos = i + 1;
                Warrior w;
                try {
                    w = warriors.get(i);
                } catch (IndexOutOfBoundsException ex) {
                    w = null;
                }
                if (w == null) {
                    break;
                }
                sender.sendMessage(makeWarriorLine(line, pos, w, game, warriors));
            }
        }
    }

    @Subcommand("%winners|winners")
    @CommandPermission("titansbattle.winners")
    @CommandCompletion("@games @winners_dates")
    public void winners(CommandSender sender, @Values("@games") String game, @Optional @Nullable Date date) {
        Winners winners = databaseManager.getLatestWinners();
        if (date != null) {
            winners = databaseManager.getWinners(date);
        }
        date = winners.getDate();
        YamlConfiguration gameConfig = gameConfigDao.getConfigFile(gameConfigDao.getGameConfiguration(game));

        Set<UUID> playerWinners = winners.getPlayerWinners(game);
        String members;
        if (playerWinners == null) {
            members = plugin.getLang("winners-no-player-winners", gameConfig);
        } else {
            members = Helper.buildStringFrom(Helper.uuidListToPlayerNameList(playerWinners));
        }
        UUID uuid = winners.getKiller(game);
        String name;
        if (uuid == null) {
            name = plugin.getLang("winners-no-killer", gameConfig);
        } else {
            name = Bukkit.getOfflinePlayer(uuid).getName();
        }

        String group = winners.getWinnerGroup(game);
        if (group == null) {
            group = plugin.getLang("winners-no-winner-group", gameConfig);
        }
        String dateFormat = plugin.getConfigManager().getDateFormat();
        sender.sendMessage(MessageFormat.format(plugin.getLang("winners", gameConfig),
                new SimpleDateFormat(dateFormat).format(date), name, group, members));
    }

    @Subcommand("%watch|watch")
    @CommandPermission("titansbattle.watch")
    @Conditions("happening")
    public void watch(Player sender, Game game) {
        Location watchroom = game.getConfig().getWatchroom();
        if (watchroom != null) {
            sender.teleport(watchroom);
            SoundUtils.playSound(SoundUtils.Type.WATCH, plugin.getConfig(), sender);
        } else {
            sender.sendMessage(ChatColor.RED + "An error has ocurred while trying to teleport you!" +
                    " Contact the admin! :o");
            plugin.debug("You have not setted the Watchroom teleport destination!", false);
        }
    }
}
