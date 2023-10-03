package me.roinujnosde.titansbattle.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.managers.ConfigManager;
import me.roinujnosde.titansbattle.managers.DatabaseManager;
import me.roinujnosde.titansbattle.types.Group;
import me.roinujnosde.titansbattle.types.Warrior;
import me.roinujnosde.titansbattle.utils.Helper;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@CommandAlias("%titansbattle|tb")
@Subcommand("%ranking|ranking")
public class RankingCommand extends BaseCommand {

    @Dependency
    private TitansBattle plugin;
    @Dependency
    private ConfigManager configManager;
    @Dependency
    private DatabaseManager databaseManager;

    @Subcommand("%groups|groups")
    @CommandPermission("titansbattle.ranking")
    @CommandCompletion("@games @order_by:type=group @pages:type=group")
    @Description("{@@command.description.ranking.groups}")
    public void groupsRanking(CommandSender sender,
                              @Values("@games") String game,
                              @Values("@order_by:type=group") @Optional @Nullable String order,
                              @Optional @Default("1") int page) {
        final List<Group> groups;
        if (plugin.getGroupManager() != null) {
            groups = new ArrayList<>(plugin.getGroupManager().getGroups());
        } else {
            groups = new ArrayList<>(0);
        }

        sortGroups(groups, game, order);

        showRanking(sender, game, groups, this::makeGroupTitle, this::makeGroupLine, page);
    }

    @Subcommand("%players|players")
    @CommandPermission("titansbattle.ranking")
    @CommandCompletion("@games @order_by:type=warrior @pages:type=warrior")
    @Description("{@@command.description.ranking.players}")
    public void playersRanking(CommandSender sender,
                               @Values("@games") String game,
                               @Values("@order_by:type=warrior") @Optional @Nullable String order,
                               @Optional @Default("1") int page) {
        final List<Warrior> warriors = new ArrayList<>(databaseManager.getWarriors());
        sortWarriors(warriors, game, order);

        showRanking(sender, game, warriors, this::makeWarriorTitle, this::makeWarriorLine, page);
    }

    private <T> void showRanking(CommandSender sender,
                                 String gameName,
                                 List<T> list,
                                 BiFunction<List<T>, String, String> titleFunction,
                                 Function<LineElement<T>, String> lineFunction,
                                 int page) {
        if (list.isEmpty()) {
            sender.sendMessage(plugin.getLang("no-data-found"));
            return;
        }

        int pageLimit = configManager.getPageLimitRanking();
        int first = (page - 1) * pageLimit;
        int last = first + pageLimit;

        if (list.size() <= first) {
            sender.sendMessage(plugin.getLang("inexistent-page"));
            return;
        }
        list = list.subList(first, Math.min(last, list.size()));

        sender.sendMessage(titleFunction.apply(list, gameName));

        for (int i = 0; i < list.size(); i++) {
            LineElement<T> element = new LineElement<>();
            element.gameName = gameName;
            element.list = list;
            element.data = list.get(i);
            element.pos = String.format("%2d", (page - 1) * configManager.getPageLimitRanking() + i + 1);
            sender.sendMessage(lineFunction.apply(element));
        }
    }

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
        int nickSize = warriors.stream().mapToInt(w -> w.getName().length()).max().orElse(0);
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
                .replace("%name-title", getNameTitle())
                .replace("%n-space", Helper.getSpaces(getNameSize(groups) - getNameTitle().length()))
                .replace("%v-space", Helper.getSpaces(getGroupsVictoriesSize(groups, game) -
                        getGroupsVictoriesTitle().length()))
                .replace("%v-title", getGroupsVictoriesTitle())
                .replace("%k-space", Helper.getSpaces(getGroupsKillsSize(groups, game) -
                        getGroupsKillsTitle().length()))
                .replace("%k-title", getGroupsKillsTitle())
                .replace("%deaths-space", Helper.getSpaces(getGroupsDeathsSize(groups, game) -
                        getGroupsDeathsTitle().length()))
                .replace("%deaths-title", getGroupsDeathsTitle())
                .replace("%defeats-space", Helper.getSpaces(getGroupsDeathsSize(groups, game)
                        - getDefeatsTitle().length()))
                .replace("%defeats-title", getDefeatsTitle());
    }

    private String makeWarriorTitle(final List<Warrior> warriors, final String game) {
        return plugin.getLang("players-ranking.title")
                .replace("%nickname-title", getNicknameTitle())
                .replace("%v-title", getVictoriesTitle())
                .replace("%k-title", getKillsTitle())
                .replace("%d-title", getDeathsTitle())
                .replace("%n-space", Helper.getSpaces(getNickSize(warriors) - getNicknameTitle().length()))
                .replace("%v-space", Helper.getSpaces(getVictoriesSize(warriors, game) - getVictoriesTitle().length()))
                .replace("%k-space", Helper.getSpaces(getKillsSize(warriors, game) - getKillsTitle().length()))
                .replace("%d-space", Helper.getSpaces(getDeathsSize(warriors, game) - getDeathsTitle().length()));
    }

    private String makeGroupLine(LineElement<Group> lineElement) {
        String line = plugin.getLang("groups-ranking.line");

        List<Group> groups = lineElement.list;
        Group g = lineElement.data;
        String game = lineElement.gameName;
        String name = g.getName();

        final int victories = g.getData().getVictories(game);
        final int kills = g.getData().getKills(game);
        final int deaths = g.getData().getDeaths(game);
        final int defeats = g.getData().getDefeats(game);
        return line.replace("%position", lineElement.pos)
                .replace("%name", name)
                .replace("%n-space", Helper.getSpaces(getNameSize(groups) - name.length()))
                .replace("%v-space", Helper.getSpaces(getGroupsVictoriesSize(groups, game) -
                        Helper.getLength(victories)))
                .replace("%victories", String.valueOf(victories))
                .replace("%k-space", Helper.getSpaces(getGroupsKillsSize(groups, game) -
                        Helper.getLength(kills)))
                .replace("%kills", String.valueOf(kills))
                .replace("%deaths-space", Helper.getSpaces(getGroupsDeathsSize(groups, game) -
                        Helper.getLength(deaths)))
                .replace("%deaths", String.valueOf(deaths))
                .replace("%defeats-space", Helper.getSpaces(getDefeatsSize(groups, game) -
                        Helper.getLength(defeats)))
                .replace("%defeats", String.valueOf(defeats));
    }

    private String makeWarriorLine(LineElement<Warrior> lineElement) {
        String line = plugin.getLang("players-ranking.line");

        List<Warrior> warriors = lineElement.list;
        Warrior w = lineElement.data;
        String game = lineElement.gameName;

        String name = w.getName();
        int victories = w.getVictories(game);
        int kills = w.getKills(game);
        int deaths = w.getDeaths(game);

        return line.replace("%position", lineElement.pos)
                .replace("%nick", name)
                .replace("%n-space", Helper.getSpaces(getNickSize(warriors) - name.length()))
                .replace("%v-space", Helper.getSpaces(getVictoriesSize(warriors, game) - Helper.getLength(victories)))
                .replace("%victories", String.valueOf(victories))
                .replace("%k-space", Helper.getSpaces(getKillsSize(warriors, game) - Helper.getLength(kills)))
                .replace("%kills", String.valueOf(kills))
                .replace("%d-space", Helper.getSpaces(getDeathsSize(warriors, game) - Helper.getLength(deaths)))
                .replace("%deaths", String.valueOf(deaths));
    }

    static class LineElement<T> {
        String gameName;
        List<T> list;
        T data;
        String pos;
    }

}
