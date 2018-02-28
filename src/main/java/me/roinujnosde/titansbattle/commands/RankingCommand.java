/*
 * The MIT License
 *
 * Copyright 2018 Edson Passos - edsonpassosjr@outlook.com.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.roinujnosde.titansbattle.commands;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import me.roinujnosde.titansbattle.Helper;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.managers.ConfigManager;
import me.roinujnosde.titansbattle.managers.DatabaseManager;
import me.roinujnosde.titansbattle.types.Game.Mode;
import me.roinujnosde.titansbattle.types.Group;
import me.roinujnosde.titansbattle.types.Warrior;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author RoinujNosde
 */
public class RankingCommand {

    private final String permission = "titansbattle.ranking";
    private final TitansBattle plugin;
    private final ConfigManager cm;
    private final DatabaseManager dm;
    private final Helper helper;

    private String[] args;

    private String nicknameTitle;
    private String victoriesTitle;
    private String killsTitle;
    private String deathsTitle;
    private String nameTitle;
    private String defeatsTitle;
    private int nickSize;
    private int victoriesSize;
    private int killsSize;
    private int deathsSize;
    private int nameSize;
    private int defeatsSize;
    private int limit;
    private int page;
    private String order;
    private String rankingType;

    public RankingCommand() {
        plugin = TitansBattle.getInstance();
        cm = plugin.getConfigManager();
        dm = plugin.getDatabaseManager();
        helper = plugin.getHelper();

        limit = cm.getPageLimitRanking();
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (sender instanceof Player && !sender.hasPermission(permission)) {
            plugin.debug("" + sender.getName() + " tried to use the "
                    + getClass().getName() + " without permission", true);
            sender.sendMessage(MessageFormat.format(
                    plugin.getLang("no-permission"), permission));
            return true;
        }

        this.args = args;

        if (args.length == 0) {
            return false;
        }

        Mode tempMode = cm.getDefaultGameMode();
        limit = cm.getPageLimitRanking();

        if (cm.isAskForGameMode()) {
            try {
                tempMode = Mode.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException ex) {
                return false;
            }

            if (args.length <= 1) {
                return false;
            }
        }

        setup();

        final Mode mode = tempMode;

        if (rankingType.equalsIgnoreCase("players")) {
            return processWarriorRanking(sender, mode);
        }

        if (rankingType.equalsIgnoreCase("groups")) {
            return processGroupRanking(sender, mode);
        }
        return false;
    }

    private boolean processGroupRanking(CommandSender sender, final Mode mode) {
        final List<Group> groups = new ArrayList<>(dm.getGroups());
        if (groups.isEmpty()) {
            sender.sendMessage(plugin.getLang("no-data-found"));
            return true;
        }

        sortGroups(groups, mode, order);
        loadGroupTitles();
        calculateGroupSizes(groups, mode);

        int first = (page == 1) ? 0 : ((page - 1) * limit);
        int last = first + (limit - 1);

        if (groups.size() <= first) {
            sender.sendMessage(plugin.getLang("inexistent-page"));
            return true;
        }

        sender.sendMessage(makeGroupTitle());

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
            sender.sendMessage(makeGroupLine(g, mode, line, pos));
        }

        if (groups.size() > last) {
            String gm = cm.isAskForGameMode() ? mode.name() + " " : "";
            String args = gm + "groups";
            sender.sendMessage(MessageFormat.format(plugin.getLang("ranking-next-page"), args, page + 1));
        }
        return true;
    }

    private boolean processWarriorRanking(CommandSender sender, final Mode mode) {
        final List<Warrior> warriors = new ArrayList<>(dm.getWarriors());
        if (warriors.isEmpty()) {
            sender.sendMessage(plugin.getLang("no-data-found"));
            return true;
        }

        sortWarriors(warriors, mode, order);
        loadWarriorTitles();
        calculateWarriorSizes(warriors, mode);

        int first = (page == 1) ? 0 : ((page - 1) * limit);
        int last = first + (limit - 1);

        if (warriors.size() <= first) {
            sender.sendMessage(plugin.getLang("inexistent-page"));
            return true;
        }

        sender.sendMessage(makeWarriorTitle());

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
            sender.sendMessage(makeWarriorLine(line, pos, w, mode));
        }

        if (warriors.size() > last) {
            String gm = cm.isAskForGameMode() ? mode.name() + " " : "";
            String args = gm + "players";
            sender.sendMessage(MessageFormat.format(plugin.getLang("ranking-next-page"), args, page + 1));
        }
        return true;
    }

    private void setup() {

        int alt = cm.isAskForGameMode() ? 1 : 0;

        rankingType = args[alt];

        if (args.length >= (3 + alt)) {
            try {
                page = Integer.parseInt(args[alt + 1]);
            } catch (NumberFormatException ex) {
                order = args[alt + 1];
            }
            if (page < 1) {
                try {
                    page = Integer.parseInt(args[alt + 2]);
                } catch (NumberFormatException ex) {
                }
            }
            if (order == null) {
                order = args[alt + 2];
            }
        }

        if (args.length == (2 + alt)) {
            try {
                page = Integer.parseInt(args[alt + 1]);
            } catch (NumberFormatException ex) {
                order = args[alt + 1];
            }
        }

        if (order == null) {
            order = "";
        }

        if (page < 1) {
            page = 1;
        }
    }

    private String makeGroupLine(Group g, final Mode mode, String line, int pos) {
        String name = g.getWrapper().getName();
        
        final int victories = g.getVictories(mode);
        final int kills = g.getKills(mode);
        final int deaths = g.getDeaths(mode);
        final int defeats = g.getDefeats(mode);
        String lineToSend = line.replaceAll("%position", String.valueOf(pos))
                .replaceAll("%name", name)
                .replaceAll("%n-space", helper.getSpaces(nameSize - name.length()))
                .replaceAll("%v-space", helper.getSpaces(victoriesSize - helper.getLength(victories)))
                .replaceAll("%victories", String.valueOf(victories))
                .replaceAll("%k-space", helper.getSpaces(killsSize - helper.getLength(kills)))
                .replaceAll("%kills", String.valueOf(kills))
                .replaceAll("%deaths-space", helper.getSpaces(deathsSize - helper.getLength(deaths)))
                .replaceAll("%deaths", String.valueOf(deaths))
                .replaceAll("%defeats-space", helper.getSpaces(defeatsSize - helper.getLength(defeats)))
                .replaceAll("%defeats", String.valueOf(defeats));
        return lineToSend;
    }

    private String makeGroupTitle() {
        String title = plugin.getLang("groups-ranking.title")
                .replaceAll("%name-title", nameTitle)
                .replaceAll("%n-space", helper.getSpaces(nameSize - nameTitle.length()))
                .replaceAll("%v-space", helper.getSpaces(victoriesSize - victoriesTitle.length()))
                .replaceAll("%v-title", victoriesTitle)
                .replaceAll("%k-space", helper.getSpaces(killsSize - killsTitle.length()))
                .replaceAll("%k-title", killsTitle)
                .replaceAll("%deaths-space", helper.getSpaces(deathsSize - deathsTitle.length()))
                .replaceAll("%deaths-title", deathsTitle)
                .replaceAll("%defeats-space", helper.getSpaces(defeatsSize - defeatsTitle.length()))
                .replaceAll("%defeats-title", defeatsTitle);
        return title;
    }

    private String makeWarriorLine(String line, int pos, Warrior w, Mode mode) {
        String name = w.toPlayer().getName();
        int victories = w.getVictories(mode);
        int kills = w.getKills(mode);
        int deaths = w.getDeaths(mode);

        String lineToSend = line.replaceAll("%position", String.valueOf(pos))
                .replaceAll("%nick", name)
                .replaceAll("%n-space", helper.getSpaces(nickSize - name.length()))
                .replaceAll("%v-space", helper.getSpaces(victoriesSize - helper.getLength(victories)))
                .replaceAll("%victories", String.valueOf(victories))
                .replaceAll("%k-space", helper.getSpaces(killsSize - helper.getLength(kills)))
                .replaceAll("%kills", String.valueOf(kills))
                .replaceAll("%d-space", helper.getSpaces(deathsSize - helper.getLength(deaths)))
                .replaceAll("%deaths", String.valueOf(deaths));
        return lineToSend;
    }

    private String makeWarriorTitle() {
        String titleMessage = plugin.getLang("players-ranking.title")
                .replaceAll("%nickname-title", nicknameTitle)
                .replaceAll("%v-title", victoriesTitle)
                .replaceAll("%k-title", killsTitle)
                .replaceAll("%d-title", deathsTitle)
                .replaceAll("%n-space", helper.getSpaces(nickSize - nicknameTitle.length()))
                .replaceAll("%v-space", helper.getSpaces(victoriesSize - victoriesTitle.length()))
                .replaceAll("%k-space", helper.getSpaces(killsSize - killsTitle.length()))
                .replaceAll("%d-space", helper.getSpaces(deathsSize - deathsTitle.length()));
        return titleMessage;
    }

    private void calculateWarriorSizes(final List<Warrior> warriors, final Mode mode) {
        nickSize = warriors.stream().mapToInt(w -> w.toPlayer().getName().length()).max().getAsInt();
        if (nicknameTitle.length() > nickSize) {
            nickSize = nicknameTitle.length();
        }
        victoriesSize = String.valueOf(warriors.stream().mapToInt(w -> w.getVictories(mode)).max().getAsInt()).length();
        if (victoriesTitle.length() > victoriesSize) {
            victoriesSize = victoriesTitle.length();
        }
        killsSize = String.valueOf(warriors.stream().mapToInt(w -> w.getKills(mode)).max().getAsInt()).length();
        if (killsTitle.length() > killsSize) {
            killsSize = killsTitle.length();
        }
        deathsSize = String.valueOf(warriors.stream().mapToInt(w -> w.getDeaths(mode)).max().getAsInt()).length();
        if (deathsTitle.length() > deathsSize) {
            deathsSize = deathsTitle.length();
        }
    }

    private void calculateGroupSizes(final List<Group> groups, final Mode mode) {
        nameSize = groups.stream().mapToInt(g -> g.getWrapper().getId().length()).max().getAsInt();
        if (nameTitle.length() > nameSize) {
            nameSize = nameTitle.length();
        }
        victoriesSize = String.valueOf(groups.stream().mapToInt(g -> g.getVictories(mode)).max().getAsInt()).length();
        if (victoriesTitle.length() > victoriesSize) {
            victoriesSize = victoriesTitle.length();
        }
        killsSize = String.valueOf(groups.stream().mapToInt(g -> g.getKills(mode)).max().getAsInt()).length();
        if (killsTitle.length() > killsSize) {
            killsSize = killsTitle.length();
        }
        deathsSize = String.valueOf(groups.stream().mapToInt(g -> g.getDeaths(mode)).max().getAsInt()).length();
        if (deathsTitle.length() > deathsSize) {
            deathsSize = deathsTitle.length();
        }
        defeatsSize = String.valueOf(groups.stream().mapToInt(g -> g.getDefeats(mode)).max().getAsInt()).length();
        if (defeatsTitle.length() > defeatsSize) {
            defeatsSize = defeatsTitle.length();
        }
    }

    private void sortWarriors(final List<Warrior> warriors, final Mode mode, String order) {
        Collections.sort(warriors, (w, w2) -> Integer.compare(w.getVictories(mode), w2.getVictories(mode)) * -1);
        if (order.equalsIgnoreCase("kills")) {
            Collections.sort(warriors, (w, w2) -> Integer.compare(w.getKills(mode), w2.getKills(mode)) * -1);
        }
        if (order.equalsIgnoreCase("deaths")) {
            Collections.sort(warriors, (w, w2) -> Integer.compare(w.getDeaths(mode), w2.getDeaths(mode)) * -1);
        }
    }

    private void loadWarriorTitles() {
        nicknameTitle = plugin.getLang("players-ranking.nickname-title");
        victoriesTitle = plugin.getLang("players-ranking.victories-title");
        killsTitle = plugin.getLang("players-ranking.kills-title");
        deathsTitle = plugin.getLang("players-ranking.deaths-title");
    }

    private void sortGroups(final List<Group> groups, final Mode mode, String order) {
        Collections.sort(groups, (g, g2) -> Integer.compare(g.getVictories(mode), g2.getVictories(mode)) * -1);
        if (order.equalsIgnoreCase("kills")) {
            Collections.sort(groups, (g, g2) -> Integer.compare(g.getKills(mode), g2.getKills(mode)) * -1);
        }
        if (order.equalsIgnoreCase("deaths")) {
            Collections.sort(groups, (g, g2) -> Integer.compare(g.getDeaths(mode), g2.getDeaths(mode)) * -1);
        }
        if (order.equalsIgnoreCase("defeats")) {
            Collections.sort(groups, (g, g2) -> Integer.compare(g.getDefeats(mode), g2.getDefeats(mode)) * -1);
        }
    }

    private void loadGroupTitles() {
        nameTitle = plugin.getLang("groups-ranking.name-title");
        victoriesTitle = plugin.getLang("groups-ranking.victories-title");
        killsTitle = plugin.getLang("groups-ranking.kills-title");
        deathsTitle = plugin.getLang("groups-ranking.deaths-title");
        defeatsTitle = plugin.getLang("groups-ranking.defeats-title");
    }
}
