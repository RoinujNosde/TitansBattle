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
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.GameConfiguration;
import me.roinujnosde.titansbattle.types.GroupData;
import me.roinujnosde.titansbattle.types.Warrior;
import me.roinujnosde.titansbattle.types.Winners;
import me.roinujnosde.titansbattle.utils.Helper;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author RoinujNosde
 */
public class DatabaseManager {

    private final TitansBattle plugin = TitansBattle.getInstance();

    private Connection connection;
    private boolean isSQLite;

    private final Map<String, GroupData> groups = new HashMap<>();
    private final Set<Warrior> warriors = new HashSet<>();
    private final List<Winners> winners = new ArrayList<>();

    private enum CountType {
        KILLS, DEATHS, VICTORIES, DEFEATS
    }

    private enum WinnerType {
        KILLER, WINNER_GROUP, PLAYER_WINNER
    }

    public void setup() {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS tb_warriors "
                              + "(displayname varchar(30) NOT NULL,"
                              + " uuid varchar(255) NOT NULL,"
                              + " kills int NOT NULL,"
                              + " deaths int NOT NULL,"
                              + " victories int NOT NULL,"
                              + " game varchar(20) NOT NULL,"
                              + " PRIMARY KEY (uuid, game));");
            addPrimaryKeyIfNotExists("tb_warriors", "uuid, game");

            stmt.execute("CREATE TABLE IF NOT EXISTS tb_groups"
                              + "(identification varchar(255) NOT NULL,"
                              + " kills int NOT NULL,"
                              + " deaths int NOT NULL,"
                              + " victories int NOT NULL,"
                              + " defeats int NOT NULL,"
                              + " game varchar(20) NOT NULL,"
                              + " PRIMARY KEY (identification, game));");
            addPrimaryKeyIfNotExists("tb_groups", "identification, game");

            stmt.execute("CREATE TABLE IF NOT EXISTS tb_winners"
                              + "(date varchar(10) NOT NULL,"
                              + " killer varchar(255),"
                              + " player_winners text,"
                              + " winner_group varchar(255),"
                              + " game varchar(20) NOT NULL,"
                              + " PRIMARY KEY (date, game));");
            addPrimaryKeyIfNotExists("tb_winners", "date, game");

        } catch (SQLException ex) {
            plugin.debug("Error while creating the tables: " + ex.getMessage(), false);
        }
    }

    private void addPrimaryKeyIfNotExists(String tableName, String columns) throws SQLException {
        long start = System.currentTimeMillis();
        boolean hasPrimaryKey = false;

        if (isSQLite()) {
            try (Statement stmt = getConnection().createStatement();
                 ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tableName + ")")) {
                while (rs.next()) {
                    if (rs.getInt("pk") == 1) {
                        hasPrimaryKey = true;
                        break;
                    }
                }
            }
        } else {
            try (Statement stmt = getConnection().createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM information_schema.KEY_COLUMN_USAGE WHERE TABLE_NAME = '" + tableName + "' AND CONSTRAINT_NAME = 'PRIMARY'")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    hasPrimaryKey = true;
                }
            }
        }

        if (!hasPrimaryKey) {
            if (isSQLite()) {
                plugin.debug("The SQLite table " + tableName + " doesn't have a primary key. Adding one...", false);
                try (Statement stmt = getConnection().createStatement()) {
                    String tempTableName = tableName + "_temp";

                    StringBuilder columnDefinitions = new StringBuilder();
                    try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tableName + ")")) {
                        while (rs.next()) {
                            if (columnDefinitions.length() > 0) {
                                columnDefinitions.append(", ");
                            }
                            columnDefinitions.append(rs.getString("name")).append(" ").append(rs.getString("type"));
                            if (rs.getInt("notnull") == 1) {
                                columnDefinitions.append(" NOT NULL");
                            }
                        }
                    }

                    columnDefinitions.append(", PRIMARY KEY(").append(columns).append(")");

                    stmt.execute("PRAGMA foreign_keys=OFF");
                    stmt.execute("BEGIN TRANSACTION");
                    stmt.execute("CREATE TABLE " + tempTableName + "(" + columnDefinitions + ")");
                    stmt.execute("INSERT OR IGNORE INTO " + tempTableName + " SELECT * FROM " + tableName);
                    stmt.execute("DROP TABLE " + tableName);
                    stmt.execute("ALTER TABLE " + tempTableName + " RENAME TO " + tableName);
                    stmt.execute("COMMIT");
                    stmt.execute("PRAGMA foreign_keys=ON");
                    stmt.execute("VACUUM");
                } catch (SQLException e) {
                    plugin.debug("Error while updating table " + tableName + ": " + e.getMessage(), false);
                }
            } else {
                plugin.debug("The MySQL table " + tableName + " doesn't have a primary key. Adding one...", false);

                try (Statement stmt = getConnection().createStatement()) {

                    stmt.execute("CREATE TABLE " + tableName + "_temp LIKE " + tableName + ";");
                    stmt.execute("INSERT INTO " + tableName + "_temp SELECT DISTINCT * FROM " + tableName + ";");
                    stmt.execute("DROP TABLE " + tableName + ";");
                    stmt.execute("RENAME TABLE " + tableName + "_temp TO " + tableName + ";");
                    stmt.execute("ALTER TABLE " + tableName + " ADD PRIMARY KEY(" + columns + ")");
                } catch (SQLException e) {
                    plugin.debug("Error while updating table " + tableName + ": " + e.getMessage(), false);
                }
            }
        }

        long end = System.currentTimeMillis();
        plugin.debug("Adding primary key to " + tableName + " took " + (end - start) + "ms", false);
    }

    private void start() throws SQLException {
        ConfigManager cm = plugin.getConfigManager();
        String database = cm.getSqlDatabase();

        if (cm.isSqlUseMysql()) {
            String hostname = cm.getSqlHostname();
            int port = cm.getSqlPort();
            String username = cm.getSqlUsername();
            String password = cm.getSqlPassword();

            connection = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database +
                                                     "?useSSL=false&autoReconnect=true", username, password);
            isSQLite = false;
        } else {
            File dbFile = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + database + ".db");

            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            isSQLite = true;
        }
    }

    private boolean isSQLite() {
        return isSQLite;
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

    private void updateWinners(@NotNull List<Winners> winners) {
        long start = System.currentTimeMillis();
        Set<GameConfiguration> games = getGames();
        Connection conn = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            String upsertWinners = getUpsertWinners();

            try (PreparedStatement stmt = conn.prepareStatement(upsertWinners)) {
                for (Winners winner : winners) {
                    for (GameConfiguration game : games) {
                        String gameName = game.getName();
                        if (!winner.isEmpty(gameName)) {
                            setValues(stmt, winner, gameName);
                            stmt.addBatch();
                        }
                    }
                }
                stmt.executeBatch();
            }

            conn.commit();

        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e) {
                    plugin.debug("Error while rolling back: " + e.getMessage(), false);
                }
            }
            plugin.debug("Error while updating/inserting winners: " + ex.getMessage(), false);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    plugin.debug("Error while setting auto commit or closing connection: " + e.getMessage(), false);
                }
            }
        }

        long end = System.currentTimeMillis();
        plugin.debug("Saving winners took " + (end - start) + "ms", false);
    }

    @NotNull
    private String getUpsertWinners() {
        String upsertWinners;
        if (isSQLite()) {
            upsertWinners = "INSERT INTO tb_winners (killer, player_winners, winner_group, date, game) VALUES (?, ?, ?, ?, ?) " +
                            "ON CONFLICT(date, game) DO UPDATE SET killer=excluded.killer, player_winners=excluded.player_winners, winner_group=excluded.winner_group;";
        } else {
            upsertWinners = "INSERT INTO tb_winners (killer, player_winners, winner_group, date, game) VALUES (?, ?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE killer=VALUES(killer), player_winners=VALUES(player_winners), winner_group=VALUES(winner_group);";
        }
        return upsertWinners;
    }

    private void setValues(PreparedStatement stmt, @NotNull Winners winner, String gameName) throws SQLException {
        String date = new SimpleDateFormat("dd/MM/yyyy").format(winner.getDate());
        String killer = null;
        if (winner.getKiller(gameName) != null) {
            killer = winner.getKiller(gameName).toString();
        }
        JsonArray ja = new JsonArray();
        if (winner.getPlayerWinners(gameName) != null) {
            winner.getPlayerWinners(gameName).stream().map(UUID::toString).forEach(ja::add);
        }
        String playerWinners = new Gson().toJson(ja);
        String winnerGroup = winner.getWinnerGroup(gameName);

        stmt.setString(1, killer);
        stmt.setString(2, playerWinners);
        stmt.setString(3, winnerGroup);
        stmt.setString(4, date);
        stmt.setString(5, gameName);
    }

    private void updateGroups(@NotNull Map<String, GroupData> groups) {
        long start = System.currentTimeMillis();

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            String upsertGroups = getUpsertGroups();

            try (PreparedStatement stmt = conn.prepareStatement(upsertGroups)) {
                for (Map.Entry<String, GroupData> entry : groups.entrySet()) {
                    String id = entry.getKey();
                    GroupData data = entry.getValue();

                    Set<GameConfiguration> games = getGames();
                    for (GameConfiguration game : games) {
                        setValues(stmt, id, data, game.getName());
                        stmt.addBatch();
                    }
                }

                stmt.executeBatch();
            }

            conn.commit();

        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e) {
                    plugin.debug("Error while rolling back: " + e.getMessage(), false);
                }
            }
            plugin.debug("Error while saving groups: " + ex.getMessage(), false);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    plugin.debug("Error while setting auto commit: " + e.getMessage(), false);
                }
            }
        }

        long end = System.currentTimeMillis();
        plugin.debug("Saving groups took " + (end - start) + "ms", false);
    }

    @NotNull
    private String getUpsertGroups() {
        String upsertGroups;
        if (isSQLite()) {
            upsertGroups = "INSERT INTO tb_groups (identification, kills, deaths, victories, game, defeats) " +
                           "VALUES (?, ?, ?, ?, ?, ?) " +
                           "ON CONFLICT(identification, game) DO UPDATE SET " +
                           "kills=excluded.kills, deaths=excluded.deaths, victories=excluded.victories, defeats=excluded.defeats;";
        } else {
            upsertGroups = "INSERT INTO tb_groups (identification, kills, deaths, victories, game, defeats) VALUES (?,?,?,?,?,?) " +
                           "ON DUPLICATE KEY UPDATE kills=VALUES(kills), deaths=VALUES(deaths), victories=VALUES(victories), defeats=VALUES(defeats);";
        }
        return upsertGroups;
    }

    private void setValues(@NotNull PreparedStatement stmt, String id, @NotNull GroupData data, String gameName) throws SQLException {
        stmt.setString(1, id);
        stmt.setInt(2, data.getKills(gameName));
        stmt.setInt(3, data.getDeaths(gameName));
        stmt.setInt(4, data.getVictories(gameName));
        stmt.setString(5, gameName);
        stmt.setInt(6, data.getDefeats(gameName));
    }

    private void updateWarriors(@NotNull Set<Warrior> warriors) {
        long start = System.currentTimeMillis();

        Set<GameConfiguration> games = getGames();
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            String upsertWarrior = getUpsertWarrior();

            try (PreparedStatement stmt = conn.prepareStatement(upsertWarrior)) {
                for (Warrior warrior : warriors) {
                    String uuid = warrior.toPlayer().getUniqueId().toString();
                    String name = warrior.toPlayer().getName();
                    if (name == null) {
                        plugin.debug(String.format("Name not found for %s", uuid));
                        continue;
                    }

                    for (GameConfiguration game : games) {
                        setValues(stmt, uuid, warrior, game.getName(), name);
                        stmt.addBatch();
                    }
                }

                stmt.executeBatch();
            }

            conn.commit();

        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e) {
                    plugin.debug("Error while rolling back: " + e.getMessage(), false);
                }
            }
            plugin.debug("Error while saving warriors: " + ex.getMessage(), false);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    plugin.debug("Error while setting auto commit or closing connection: " + e.getMessage(), false);
                }
            }
        }

        long end = System.currentTimeMillis();
        plugin.debug("Saving warriors took " + (end - start) + "ms", false);
    }

    @NotNull
    private String getUpsertWarrior() {
        String upsertWarrior;
        if (isSQLite()) {
            upsertWarrior = "INSERT INTO tb_warriors (uuid, kills, deaths, victories, game, displayname) " +
                            "VALUES (?, ?, ?, ?, ?, ?) " +
                            "ON CONFLICT(uuid, game) DO UPDATE SET " +
                            "kills=excluded.kills, deaths=excluded.deaths, victories=excluded.victories, displayname=excluded.displayname;";
        } else {
            upsertWarrior = "INSERT INTO tb_warriors (uuid, kills, deaths, victories, game, displayname) VALUES (?, ?, ?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE kills=VALUES(kills), deaths=VALUES(deaths), victories=VALUES(victories), displayname=VALUES(displayname);";
        }
        return upsertWarrior;
    }

    private void setValues(PreparedStatement stmt, String uuid, @NotNull Warrior warrior, String gameName, String name) throws SQLException {
        stmt.setString(1, uuid);
        stmt.setInt(2, warrior.getKills(gameName));
        stmt.setInt(3, warrior.getDeaths(gameName));
        stmt.setInt(4, warrior.getVictories(gameName));
        stmt.setString(5, gameName);
        stmt.setString(6, name);
    }

    @NotNull
    public GroupData getGroupData(@NotNull String id) {
        return groups.computeIfAbsent(id, k -> new GroupData());
    }

    @NotNull
    public Warrior getWarrior(@NotNull OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        for (Warrior warrior : warriors) {
            if (warrior.toPlayer().getUniqueId().equals(uuid)) {
                if (player instanceof Player) {
                    warrior.setOnlinePlayer((Player) player);
                }
                return warrior;
            }
        }

        Warrior warrior = new Warrior(player, plugin::getGroupManager);
        warriors.add(warrior);
        return warrior;
    }

    @NotNull
    public Warrior getWarrior(@NotNull UUID uuid) {
        return getWarrior(Bukkit.getOfflinePlayer(uuid));
    }

    private void loopThroughGroups() {
        String sql = "SELECT * FROM tb_groups;";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            ResultSet query = stmt.executeQuery();

            Map<String, EnumMap<CountType, Map<String, Integer>>> dataMap = new HashMap<>();

            while (query.next()) {
                String id = query.getString("identification");
                String game = String.valueOf(query.getString("game"));
                dataMap.computeIfAbsent(id, k -> new EnumMap<>(CountType.class));

                final EnumMap<CountType, Map<String, Integer>> groupData = dataMap.get(id);

                for (CountType t : CountType.values()) {
                    groupData.computeIfAbsent(t, k -> new HashMap<>());
                    groupData.get(t).put(game, query.getInt(t.name()));
                }
            }

            for (Map.Entry<String, EnumMap<CountType, Map<String, Integer>>> entry : dataMap.entrySet()) {
                String id = entry.getKey();
                EnumMap<CountType, Map<String, Integer>> data = entry.getValue();

                GroupData groupData = new GroupData(
                        data.get(CountType.VICTORIES),
                        data.get(CountType.DEFEATS),
                        data.get(CountType.KILLS),
                        data.get(CountType.DEATHS)
                );

                groups.put(id, groupData);
            }

        } catch (SQLException ex) {
            plugin.debug("Error while getting a Group: " + ex.getMessage(), false);
        }
    }


    private void loopThroughWarriors() {
        String sql = "SELECT * FROM tb_warriors;";
        try (Statement stmt = getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);

            Map<UUID, EnumMap<CountType, Map<String, Integer>>> players = new HashMap<>();

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                String game = String.valueOf(rs.getString("game"));
                players.computeIfAbsent(uuid, k -> new EnumMap<>(CountType.class));

                final EnumMap<CountType, Map<String, Integer>> playerData = players.get(uuid);

                for (CountType t : CountType.values()) {
                    if (t == CountType.DEFEATS) {
                        continue;
                    }
                    playerData.computeIfAbsent(t, k -> new HashMap<>());
                    playerData.get(t).put(game, rs.getInt(t.name()));
                }
            }


            for (Map.Entry<UUID, EnumMap<CountType, Map<String, Integer>>> entry : players.entrySet()) {
                UUID uuid = entry.getKey();
                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                EnumMap<CountType, Map<String, Integer>> playerData = entry.getValue();

                Warrior warrior = new Warrior(player, plugin::getGroupManager, playerData.get(CountType.KILLS),
                        playerData.get(CountType.DEATHS), playerData.get(CountType.VICTORIES));
                warriors.add(warrior);
            }

        } catch (SQLException ex) {
            plugin.debug("An error occurred while trying to load the players data! " + ex.getMessage(), false);
        }
    }

    @SuppressWarnings("unchecked")
    private void loopThroughWinners() {
        String sql = "SELECT * FROM tb_winners;";
        try (Statement stmt = getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);

            Map<Date, EnumMap<WinnerType, Map<String, Object>>> winnersData = new HashMap<>();

            while (rs.next()) {
                Date date = parseDate(rs.getString("date"));
                if (date == null) {
                    continue;
                }

                String game = String.valueOf(rs.getString("game"));

                winnersData.computeIfAbsent(date, k -> new EnumMap<>(WinnerType.class));
                EnumMap<WinnerType, Map<String, Object>> data = winnersData.get(date);

                for (WinnerType wt : WinnerType.values()) {
                    data.computeIfAbsent(wt, k -> new HashMap<>());
                    final Map<String, Object> innerData = data.get(wt);

                    switch (wt) {
                        case KILLER:
                            UUID k = null;
                            String killer = rs.getString("killer");
                            if (killer != null) {
                                k = UUID.fromString(killer);
                            }
                            innerData.put(game, k);
                            break;
                        case WINNER_GROUP:
                            String wg = rs.getString("winner_group");
                            innerData.put(game, wg);
                            break;
                        case PLAYER_WINNER:
                            List<UUID> pw = new ArrayList<>();
                            JsonArray ja = new Gson().fromJson(rs.getString("player_winners"), JsonArray.class);
                            if (ja != null) {
                                ja.forEach(uuid -> pw.add(UUID.fromString(uuid.getAsString())));
                            }
                            innerData.put(game, pw);
                    }
                }
            }

            for (Map.Entry<Date, EnumMap<WinnerType, Map<String, Object>>> entry : winnersData.entrySet()) {
                Date date = entry.getKey();
                EnumMap<WinnerType, Map<String, Object>> data = entry.getValue();

                Map<String, UUID> killer = new HashMap<>();
                Map<String, List<UUID>> playerWinners = new HashMap<>();
                Map<String, String> winnerGroup = new HashMap<>();

                for (Map.Entry<WinnerType, Map<String, Object>> dataEntry : data.entrySet()) {
                    WinnerType wt = dataEntry.getKey();
                    for (Map.Entry<String, Object> gameEntry : dataEntry.getValue().entrySet()) {
                        String game = gameEntry.getKey();
                        Object innerObject = gameEntry.getValue();

                        switch (wt) {
                            case KILLER:
                                killer.put(game, (UUID) innerObject);
                                break;
                            case WINNER_GROUP:
                                winnerGroup.put(game, (String) innerObject);
                                break;
                            case PLAYER_WINNER:
                                playerWinners.put(game, (List<UUID>) innerObject);
                        }
                    }
                }

                Winners w = new Winners(date, killer, playerWinners, winnerGroup);
                winners.add(w);
            }

        } catch (SQLException ex) {
            plugin.debug("An error occurred while trying to load the winners data! " + ex.getMessage(), false);
        }

        winners.sort(Comparator.naturalOrder());
    }

    public void loadDataToMemory() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            loopThroughGroups();
            loopThroughWarriors();
            loopThroughWinners();
        });
    }

    public void saveAll() {
        // Copying collections for async use, avoiding ConcurrentModificationException
        final Map<String, GroupData> groupsMap = new HashMap<>(getGroups());
        final Set<Warrior> warriorsSet = new HashSet<>(getWarriors());
        final List<Winners> winnersList = new ArrayList<>(getWinners());

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            updateGroups(groupsMap);
            updateWarriors(warriorsSet);
            updateWinners(winnersList);
        });
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
            if (latest != null && (Helper.equalDates(today, latest))) {
                return getLatestWinners();
            }
            for (Winners w : winners) {
                if (Helper.equalDates(w.getDate(), today)) {
                    return w;
                }
            }
        }
        return getEmptyWinners(null);
    }

    @NotNull
    private Winners getEmptyWinners(@Nullable Date date) {
        if (date == null) {
            date = Calendar.getInstance().getTime();
        }

        Winners w = new Winners(date);
        winners.add(w);
        winners.sort(Comparator.naturalOrder());
        return w;
    }

    public Winners getWinners(Date date) {
        for (Winners w : winners) {
            if (Helper.equalDates(date, w.getDate())) {
                return w;
            }
        }
        return getEmptyWinners(date);
    }

    public Set<Warrior> getWarriors() {
        return Collections.unmodifiableSet(warriors);
    }

    public Map<String, GroupData> getGroups() {
        return Collections.unmodifiableMap(groups);
    }

    public List<Winners> getWinners() {
        return winners;
    }

    @NotNull
    private Set<GameConfiguration> getGames() {
        return plugin.getConfigurationDao().getConfigurations(GameConfiguration.class);
    }

    @Nullable
    private Date parseDate(String dateString) {
        try {
            return new SimpleDateFormat("dd/MM/yyyy").parse(dateString);
        } catch (ParseException ex) {
            plugin.debug("Invalid date! " + ex.getMessage(), false);
            return null;
        }
    }
}
