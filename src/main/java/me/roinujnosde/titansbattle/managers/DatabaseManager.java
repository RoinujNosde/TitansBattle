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
package me.roinujnosde.titansbattle.managers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.massivecraft.factions.entity.FactionColl;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import me.roinujnosde.titansbattle.Helper;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.Game.Mode;
import me.roinujnosde.titansbattle.types.Group;
import me.roinujnosde.titansbattle.types.GroupWrapper;
import me.roinujnosde.titansbattle.types.Warrior;
import me.roinujnosde.titansbattle.types.Winners;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author RoinujNosde
 */
public class DatabaseManager {

    private TitansBattle plugin;
    private Helper helper;
    private ConfigManager cm;

    private Connection connection;
    private String hostname;
    private int port;
    private String database;
    private String username;
    private String password;

    private Set<Group> groups;
    private Set<Warrior> warriors;
    private List<Winners> winners;

    private enum CountType {
        KILLS, DEATHS, VICTORIES
    }

    private enum WinnerType {
        KILLER, WINNER_GROUP, PLAYER_WINNER;
    }

    public void load() {
        groups = new HashSet<>();
        warriors = new HashSet<>();
        winners = new ArrayList<>();

        plugin = TitansBattle.getInstance();
        helper = plugin.getHelper();
        cm = plugin.getConfigManager();

        hostname = cm.getSqlHostname();
        port = cm.getSqlPort();
        database = cm.getSqlDatabase();
        username = cm.getSqlUsername();
        password = cm.getSqlPassword();

        try {
            setup();
        } catch (SQLException ex) {
            plugin.debug("Error while creating the tables: " + ex.getMessage(), false);
        }
    }

    private void setup() throws SQLException {
        Statement statement = getConnection().createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS tb_warriors "
                + "(displayname varchar(30) NOT NULL,"
                + " uuid varchar(255) NOT NULL,"
                + " kills int NOT NULL,"
                + " deaths int NOT NULL,"
                + " victories int NOT NULL,"
                + " mode varchar(20) NOT NULL);");
        statement.execute("CREATE TABLE IF NOT EXISTS tb_groups"
                + "(identification varchar(255) NOT NULL,"
                + " kills int NOT NULL,"
                + " deaths int NOT NULL,"
                + " victories int NOT NULL,"
                + " defeats int NOT NULL,"
                + " mode varchar(20) NOT NULL);");
        statement.execute("CREATE TABLE IF NOT EXISTS tb_winners"
                + "(date varchar(10) NOT NULL,"
                + " killer varchar(255),"
                + " player_winners varchar,"
                + " winner_group varchar(255),"
                + " mode varchar(20) NOT NULL);");
    }

    private void start() throws SQLException {
        if (cm.isSqlUseMysql()) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database + "?useSSL=false", username, password);
            } catch (ClassNotFoundException ex) {
                plugin.debug("MySQL driver not found!", false);
            }
        } else {
            File dbFile = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + database + ".db");
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            } catch (ClassNotFoundException ex) {
                plugin.debug("SQLite driver not found!", false);
            }
        }
    }

    private Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            start();
        }
        return connection;
    }

    public void close() {
        if (connection == null) {
            return;
        }
        try {
            if (connection.isClosed()) {
                return;
            }
            connection.close();
        } catch (SQLException ex) {
            plugin.debug("Error while closing connection: " + ex.getMessage(), true);
        }
    }

    private void update(Winners winners) {
        ArrayList<Mode> modes = new ArrayList<>();
        String date = new SimpleDateFormat("dd/MM/yyyy").format(winners.getDate());

        String update = "UPDATE tb_winners SET killer=?, player_winners=?, winner_group=? WHERE date=? AND mode=?;";
        try (PreparedStatement statement = getConnection().prepareStatement(update)) {
            for (Mode mode : Mode.values()) {
                String killer = null;
                if (winners.getKiller(mode) != null) {
                    killer = winners.getKiller(mode).toString();
                }
                String player_winners;
                JsonArray ja = new JsonArray();
                if (winners.getPlayerWinners(mode) != null) {
                    winners.getPlayerWinners(mode).stream().map(UUID::toString).forEach(ja::add);
                }
                player_winners = new Gson().toJson(ja);
                String winner_group = null;
                if (winners.getWinnerGroup(mode) != null) {
                    winner_group = winners.getWinnerGroup(mode).getWrapper().getId();
                }

                statement.setString(1, killer);
                statement.setString(2, player_winners);
                statement.setString(3, winner_group);
                statement.setString(4, date);
                statement.setString(5, mode.name());

                int count = statement.executeUpdate();
                if (count != 0) {
                    modes.add(mode);
                }
            }
        } catch (SQLException ex) {
            plugin.debug("Error while saving the winners: " + ex.getMessage(), false);
        }

        String insert = "INSERT INTO tb_winners (killer, player_winners, winner_group, date, mode) VALUES (?, ?, ?, ?, ?);";
        try (PreparedStatement statement = getConnection().prepareStatement(insert)) {
            for (Mode mode : Mode.values()) {
                if (!modes.contains(mode)) {
                    String killer = null;
                    if (winners.getKiller(mode) != null) {
                        killer = winners.getKiller(mode).toString();
                    }
                    String player_winners;
                    JsonArray ja = new JsonArray();
                    if (winners.getPlayerWinners(mode) != null) {
                        winners.getPlayerWinners(mode).stream().map(UUID::toString).forEach(ja::add);
                    }
                    player_winners = new Gson().toJson(ja);
                    String winner_group = null;
                    if (winners.getWinnerGroup(mode) != null) {
                        winner_group = winners.getWinnerGroup(mode).getWrapper().getId();
                    }
                    statement.setString(1, killer);
                    statement.setString(2, player_winners);
                    statement.setString(3, winner_group);
                    statement.setString(4, date);
                    statement.setString(5, mode.name());

                    statement.execute();
                }
            }
        } catch (SQLException ex) {
            plugin.debug("Error while saving the winners: " + ex.getMessage(), false);
        }
    }

    private void update(Group group) {
        ArrayList<Mode> modes = new ArrayList<>();
        String identification = group.getWrapper().getId();

        String update = "UPDATE tb_groups SET kills=?, deaths=?, victories=?,"
                + " defeats=? WHERE identification=? AND mode=?;";
        try (PreparedStatement statement = getConnection().prepareStatement(update)) {
            for (Mode mode : Mode.values()) {
                int kills = group.getKills(mode);
                int deaths = group.getDeaths(mode);
                int victories = group.getVictories(mode);
                int defeats = group.getDefeats(mode);
                statement.setInt(1, kills);
                statement.setInt(2, deaths);
                statement.setInt(3, victories);
                statement.setInt(4, defeats);
                statement.setString(5, identification);
                statement.setString(6, mode.name());

                int count = statement.executeUpdate();
                if (count != 0) {
                    modes.add(mode);
                }
            }
        } catch (SQLException ex) {
            plugin.debug("Error while saving a Group: " + ex.getMessage(), false);
        }
        String insert = "INSERT INTO tb_groups (identification, kills, deaths, victories, mode, defeats) VALUES (?,?,?,?,?,?);";
        try (PreparedStatement statement = getConnection().prepareStatement(insert)) {
            for (Mode mode : Mode.values()) {
                if (!modes.contains(mode)) {
                    int kills = group.getKills(mode);
                    int deaths = group.getDeaths(mode);
                    int victories = group.getVictories(mode);
                    int defeats = group.getDefeats(mode);
                    statement.setString(1, identification);
                    statement.setInt(2, kills);
                    statement.setInt(3, deaths);
                    statement.setInt(4, victories);
                    statement.setString(5, mode.toString());
                    statement.setInt(6, defeats);

                    statement.execute();
                }
            }
        } catch (SQLException ex) {
            plugin.debug("Error while saving a Group: " + ex.getMessage(), false);
        }
    }

    private void update(Warrior warrior) {
        ArrayList<Mode> modes = new ArrayList<>();
        String uuid = warrior.toPlayer().getUniqueId().toString();
        String name = warrior.toPlayer().getName();

        String update = "UPDATE tb_warriors SET kills=?, deaths=?, victories=?, displayname=? WHERE uuid=? AND mode=?;";
        try (PreparedStatement statement = getConnection().prepareStatement(update)) {
            for (Mode mode : Mode.values()) {
                int kills = warrior.getKills(mode);
                int deaths = warrior.getDeaths(mode);
                int victories = warrior.getVictories(mode);
                statement.setInt(1, kills);
                statement.setInt(2, deaths);
                statement.setInt(3, victories);
                statement.setString(4, name);
                statement.setString(5, uuid);
                statement.setString(6, mode.name());

                int count = statement.executeUpdate();
                if (count != 0) {
                    modes.add(mode);
                }
            }

        } catch (SQLException ex) {
            plugin.debug("Error while saving a Warrior: " + ex.getMessage(), false);
        }

        String insert = "INSERT INTO tb_warriors (uuid, kills, deaths, victories, mode, displayname) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = getConnection().prepareStatement(insert)) {
            for (Mode mode : Mode.values()) {
                if (!modes.contains(mode)) {
                    int kills = warrior.getKills(mode);
                    int deaths = warrior.getDeaths(mode);
                    int victories = warrior.getVictories(mode);
                    statement.setString(1, uuid);
                    statement.setInt(2, kills);
                    statement.setInt(3, deaths);
                    statement.setInt(4, victories);
                    statement.setString(5, mode.toString());
                    statement.setString(6, name);

                    statement.execute();
                }
            }
        } catch (SQLException ex) {
            plugin.debug("Error while saving a Warrior: " + ex.getMessage(), false);
        }
    }

    private Group getGroup(String identification) {
        if (identification != null) {
            plugin.debug("Trying to load a group...", true);
            for (Group group : groups) {
                if (group.getWrapper().getId().equals(identification)) {
                    plugin.debug("Group " + group.getWrapper().getName() + "loaded!", true);
                    return group;
                }
            }
            plugin.debug("Group " + identification + " not found", true);
        }
        return null;
    }

    public Group getGroup(GroupWrapper wrapper) {
        plugin.debug("Trying to load a group...", true);
        if (wrapper != null) {
            String id = wrapper.getId();

            Group temp = getGroup(id);
            if (temp != null) {
                return temp;
            }

            HashMap<Mode, Integer> kills = new HashMap<>();
            HashMap<Mode, Integer> deaths = new HashMap<>();
            HashMap<Mode, Integer> victories = new HashMap<>();
            HashMap<Mode, Integer> defeats = new HashMap<>();

            String sql = "SELECT * FROM tb_groups WHERE identification = ?;";
            try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
                statement.setString(1, id);
                ResultSet query = statement.executeQuery();

                while (query.next()) {
                    Mode mode = Mode.valueOf(query.getString("mode"));
                    int k = query.getInt("kills");
                    int d = query.getInt("deaths");
                    int v = query.getInt("victories");
                    int d2 = query.getInt("defeats");

                    kills.put(mode, k);
                    deaths.put(mode, d);
                    victories.put(mode, v);
                    defeats.put(mode, d2);
                }

            } catch (SQLException ex) {
                plugin.debug("Error while getting a Group: " + ex.getMessage(), false);
            }
            Group group = new Group(wrapper, victories, defeats, kills, deaths);
            groups.add(group);
            plugin.debug("Group " + group.getWrapper().getName() + "loaded!", true);
            return group;
        }
        plugin.debug("Group not found", true);
        return null;
    }

    public Warrior getWarrior(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

        for (Warrior warrior : warriors) {
            if (warrior.toPlayer().getUniqueId().equals(uuid)) {
                return warrior;
            }
        }

        HashMap<Mode, Integer> kills = new HashMap<>();
        HashMap<Mode, Integer> deaths = new HashMap<>();
        HashMap<Mode, Integer> victories = new HashMap<>();

        String sql = "SELECT * FROM tb_warriors WHERE uuid = ?;";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            ResultSet query = statement.executeQuery();

            while (query.next()) {
                Mode mode = Mode.valueOf(query.getString("mode"));
                int k = query.getInt("kills");
                int d = query.getInt("deaths");
                int v = query.getInt("victories");

                kills.put(mode, k);
                deaths.put(mode, d);
                victories.put(mode, v);
            }

        } catch (SQLException ex) {
            plugin.debug("Error while getting a Warrior: " + ex.getMessage(), false);
        }
        Warrior warrior = new Warrior(player, getGroup(helper.getGroupWrapper(player)), kills, deaths, victories);
        warriors.add(warrior);
        return warrior;
    }

    private void loopThroughWarriors() {
        String sql = "SELECT * FROM tb_warriors;";
        try (Statement statement = getConnection().createStatement()) {
            ResultSet rs = statement.executeQuery(sql);

            HashMap<UUID, HashMap<CountType, HashMap<Mode, Integer>>> players = new HashMap<>();

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                Mode mode = Mode.valueOf(rs.getString("mode"));
                if (players.get(uuid) == null) {
                    players.put(uuid, new HashMap<>());
                }
                final HashMap<CountType, HashMap<Mode, Integer>> playerData = players.get(uuid);
                for (CountType t : CountType.values()) {
                    if (playerData.get(t) == null) {
                        playerData.put(t, new HashMap<>());
                    }
                    playerData.get(t).put(mode, rs.getInt(t.name()));
                }
            }

            for (UUID uuid : players.keySet()) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                Group group = getGroup(helper.getGroupWrapper(player));
                HashMap<CountType, HashMap<Mode, Integer>> playerData = players.get(uuid);

                Warrior warrior = new Warrior(player, group, playerData.get(CountType.KILLS), playerData.get(CountType.DEATHS), playerData.get(CountType.VICTORIES));
                warriors.add(warrior);
            }

        } catch (SQLException ex) {
            plugin.debug("An error ocurred while trying to load the players data! " + ex.getMessage(), false);
        }
    }

    private void loopThroughWinners() {
        String sql = "SELECT * FROM tb_winners;";
        try (Statement statement = getConnection().createStatement()) {
            ResultSet rs = statement.executeQuery(sql);

            Map<Date, Map<WinnerType, Map<Mode, Object>>> winnersData = new HashMap<>();

            while (rs.next()) {
                Date date = null;
                try {
                    date = new SimpleDateFormat("dd/MM/yyyy").parse(rs.getString("date"));
                } catch (ParseException ex) {
                    plugin.debug("Invalid date! " + ex.getMessage(), false);
                    continue;
                }
                Mode mode = Mode.valueOf(rs.getString("mode"));

                winnersData.computeIfAbsent(date, k -> new HashMap<>());
                Map<WinnerType, Map<Mode, Object>> data = winnersData.get(date);
                for (WinnerType wt : WinnerType.values()) {
                    data.computeIfAbsent(wt, k -> new HashMap<>());
                    final Map<Mode, Object> innerData = data.get(wt);
                    switch (wt) {
                        case KILLER:
                            UUID k = null;
                            try {
                                k = UUID.fromString(rs.getString("killer"));
                            } catch (NullPointerException ignored) {
                            }
                            innerData.put(mode, k);
                            break;
                        case WINNER_GROUP:
                            Group wg = getGroup(rs.getString("winner_group"));
                            innerData.put(mode, wg);
                            break;
                        case PLAYER_WINNER:
                            Set<UUID> pw = new HashSet<>();
                            JsonArray ja = new Gson().fromJson(rs.getString("player_winners"), JsonArray.class);
                            if (ja != null) {
                                ja.forEach(uuid -> pw.add(UUID.fromString(uuid.getAsString())));
                            }
                            innerData.put(mode, pw);
                            break;
                    }
                }
            }

            for (Date date : winnersData.keySet()) {
                Map<WinnerType, Map<Mode, Object>> data = winnersData.get(date);

                Map<Mode, UUID> killer = new HashMap<>();
                Map<Mode, Set<UUID>> playerWinners = new HashMap<>();
                Map<Mode, Group> winnerGroup = new HashMap<>();

                for (WinnerType wt : data.keySet()) {
                    for (Mode mode : data.get(wt).keySet()) {
                        Object innerObject = data.get(wt).get(mode);
                        switch (wt) {
                            case KILLER:
                                killer.put(mode, (UUID) innerObject);
                                break;
                            case WINNER_GROUP:
                                winnerGroup.put(mode, (Group) innerObject);
                                break;
                            case PLAYER_WINNER:
                                playerWinners.put(mode, (Set<UUID>) innerObject);
                        }
                    }
                }

                Winners w = new Winners(date, killer, playerWinners, winnerGroup);
                winners.add(w);
            }

        } catch (SQLException ex) {
            plugin.debug("An error ocurred while trying to load the winners data! " + ex.getMessage(), false);
        }

        sortWinners();
    }

    private void sortWinners() {
        winners.sort((w, w2) -> w.getDate().compareTo(w2.getDate()));
    }

    public void loadDataToMemory() {
        if (plugin.isFactions()) {
            FactionColl.get().getAll().forEach(faction -> {
                getGroup(new GroupWrapper(faction));
                faction.getMPlayers().forEach(
                        member -> getWarrior(member.getUuid()));
            });
        }
        if (plugin.isSimpleClans()) {
            //noinspection ConstantConditions
            plugin.getClanManager().getClans().forEach(clan -> {
                getGroup(new GroupWrapper(clan));
                clan.getMembers().forEach(
                        member -> getWarrior(member.getUniqueId()));
            });
        }
        loopThroughWarriors();
        loopThroughWinners();
    }

    public void saveAll() {
        getGroups().forEach(this::update);
        getWarriors().forEach(this::update);
        getWinners().forEach(this::update);
    }

    public Winners getLatestWinners() {
        if (winners.isEmpty()) {
            return getEmptyWinners(null);
        }
        return winners.get(winners.size() - 1);
    }

    public Winners getTodaysWinners() {
        if (!winners.isEmpty()) {
            Date today = Calendar.getInstance().getTime();
            Date latest = getLatestWinners().getDate();
            if (latest != null) {
                if (helper.equalDates(today, latest)) {
                    return getLatestWinners();
                }
            }
            for (Winners w : winners) {
                if (helper.equalDates(w.getDate(), today)) {
                    return w;
                }
            }
        }
        return getEmptyWinners(null);
    }

    private Winners getEmptyWinners(Date date) {
        if (date == null) {
            date = Calendar.getInstance().getTime();
        }
        Map<Mode, UUID> killer = new HashMap<>();
        Map<Mode, Set<UUID>> playerWinners = new HashMap<>();
        Map<Mode, Group> winnerGroup = new HashMap<>();

        Winners w = new Winners(date, killer, playerWinners, winnerGroup);
        winners.add(w);
        sortWinners();
        return w;
    }

    public Winners getWinners(Date date) {
        for (Winners w : winners) {
            if (helper.equalDates(date, w.getDate())) {
                return w;
            }
        }
        return getEmptyWinners(date);
    }

    public Set<Warrior> getWarriors() {
        return Collections.unmodifiableSet(warriors);
    }

    public Set<Group> getGroups() {
        return Collections.unmodifiableSet(groups);
    }

    public List<Winners> getWinners() {
        return winners;
    }
}
