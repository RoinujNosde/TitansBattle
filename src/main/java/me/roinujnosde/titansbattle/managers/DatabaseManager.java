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
        try (Statement statement = getConnection().createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS tb_warriors "
                              + "(displayname varchar(30) NOT NULL,"
                              + " uuid varchar(255) NOT NULL,"
                              + " kills int NOT NULL,"
                              + " deaths int NOT NULL,"
                              + " victories int NOT NULL,"
                              + " game varchar(20) NOT NULL);");
            statement.execute("CREATE TABLE IF NOT EXISTS tb_groups"
                              + "(identification varchar(255) NOT NULL,"
                              + " kills int NOT NULL,"
                              + " deaths int NOT NULL,"
                              + " victories int NOT NULL,"
                              + " defeats int NOT NULL,"
                              + " game varchar(20) NOT NULL);");
            statement.execute("CREATE TABLE IF NOT EXISTS tb_winners"
                              + "(date varchar(10) NOT NULL,"
                              + " killer varchar(255),"
                              + " player_winners text,"
                              + " winner_group varchar(255),"
                              + " game varchar(20) NOT NULL);");
        } catch (SQLException ex) {
            plugin.debug("Error while creating the tables: " + ex.getMessage(), false);
        }
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
                                                     "?useSSL=false", username, password);
        } else {
            File dbFile = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + database + ".db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
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
        HashSet<GameConfiguration> updated = new HashSet<>();

        String update = "UPDATE tb_winners SET killer=?, player_winners=?, winner_group=? WHERE date=? AND game=?;";
        try (PreparedStatement statement = getConnection().prepareStatement(update)) {
            for (GameConfiguration game : getGames()) {
                if (winners.isEmpty(game.getName())) {
                    continue;
                }
                setValues(winners, statement, game);

                int count = statement.executeUpdate();
                if (count != 0) {
                    updated.add(game);
                }
            }
        } catch (SQLException ex) {
            plugin.debug("Error while saving the winners: " + ex.getMessage(), false);
        }

        String insert = "INSERT INTO tb_winners (killer, player_winners, winner_group, date, game) VALUES (?, ?, ?, ?, ?);";
        try (PreparedStatement statement = getConnection().prepareStatement(insert)) {
            for (GameConfiguration game : getGames()) {
                if (!updated.contains(game) && !winners.isEmpty(game.getName())) {
                    setValues(winners, statement, game);

                    statement.execute();
                }
            }
        } catch (SQLException ex) {
            plugin.debug("Error while saving the winners: " + ex.getMessage(), false);
        }
    }

    private void setValues(@NotNull Winners winners, PreparedStatement statement, @NotNull GameConfiguration game) throws SQLException {
        String date = new SimpleDateFormat("dd/MM/yyyy").format(winners.getDate());

        String name = game.getName();
        String killer = null;
        if (winners.getKiller(name) != null) {
            killer = winners.getKiller(name).toString();
        }
        String playerWinners;
        JsonArray ja = new JsonArray();
        if (winners.getPlayerWinners(name) != null) {
            winners.getPlayerWinners(name).stream().map(UUID::toString).forEach(ja::add);
        }
        playerWinners = new Gson().toJson(ja);
        String winnerGroup = winners.getWinnerGroup(name);

        statement.setString(1, killer);
        statement.setString(2, playerWinners);
        statement.setString(3, winnerGroup);
        statement.setString(4, date);
        statement.setString(5, name);
    }

    private void update(@NotNull String id, @NotNull GroupData data) {
        HashSet<GameConfiguration> updated = new HashSet<>();

        String update = "UPDATE tb_groups SET kills=?, deaths=?, victories=?,"
                        + " defeats=? WHERE identification=? AND game=?;";
        try (PreparedStatement statement = getConnection().prepareStatement(update)) {
            for (GameConfiguration game : getGames()) {
                String gameName = game.getName();
                int kills = data.getKills(gameName);
                int deaths = data.getDeaths(gameName);
                int victories = data.getVictories(gameName);
                int defeats = data.getDefeats(gameName);
                statement.setInt(1, kills);
                statement.setInt(2, deaths);
                statement.setInt(3, victories);
                statement.setInt(4, defeats);
                statement.setString(5, id);
                statement.setString(6, gameName);

                int count = statement.executeUpdate();
                if (count != 0) {
                    updated.add(game);
                }
            }
        } catch (SQLException ex) {
            plugin.debug("Error while saving a Group: " + ex.getMessage(), false);
        }
        String insert = "INSERT INTO tb_groups (identification, kills, deaths, victories, game, defeats) VALUES (?,?,?,?,?,?);";
        try (PreparedStatement statement = getConnection().prepareStatement(insert)) {
            for (GameConfiguration game : getGames()) {
                if (!updated.contains(game)) {
                    String gameName = game.getName();
                    int kills = data.getKills(gameName);
                    int deaths = data.getDeaths(gameName);
                    int victories = data.getVictories(gameName);
                    int defeats = data.getDefeats(gameName);
                    statement.setString(1, id);
                    statement.setInt(2, kills);
                    statement.setInt(3, deaths);
                    statement.setInt(4, victories);
                    statement.setString(5, gameName);
                    statement.setInt(6, defeats);

                    statement.execute();
                }
            }
        } catch (SQLException ex) {
            plugin.debug("Error while saving a Group: " + ex.getMessage(), false);
        }
    }

    private void update(@NotNull Warrior warrior) {
        ArrayList<GameConfiguration> updated = new ArrayList<>();
        String uuid = warrior.toPlayer().getUniqueId().toString();
        String name = warrior.toPlayer().getName();
        if (name == null) {
            plugin.debug(String.format("Name not found for %s", uuid));
            return;
        }

        String update = "UPDATE tb_warriors SET kills=?, deaths=?, victories=?, displayname=? WHERE uuid=? AND game=?;";
        try (PreparedStatement statement = getConnection().prepareStatement(update)) {
            for (GameConfiguration game : getGames()) {
                String gameName = game.getName();
                int kills = warrior.getKills(gameName);
                int deaths = warrior.getDeaths(gameName);
                int victories = warrior.getVictories(gameName);
                statement.setInt(1, kills);
                statement.setInt(2, deaths);
                statement.setInt(3, victories);
                statement.setString(4, name);
                statement.setString(5, uuid);
                statement.setString(6, gameName);

                int count = statement.executeUpdate();
                if (count != 0) {
                    updated.add(game);
                }
            }

        } catch (SQLException ex) {
            plugin.debug("Error while saving a Warrior: " + ex.getMessage(), false);
        }

        String insert = "INSERT INTO tb_warriors (uuid, kills, deaths, victories, game, displayname) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = getConnection().prepareStatement(insert)) {
            for (GameConfiguration game : getGames()) {
                if (!updated.contains(game)) {
                    String gameName = game.getName();
                    int kills = warrior.getKills(gameName);
                    int deaths = warrior.getDeaths(gameName);
                    int victories = warrior.getVictories(gameName);
                    statement.setString(1, uuid);
                    statement.setInt(2, kills);
                    statement.setInt(3, deaths);
                    statement.setInt(4, victories);
                    statement.setString(5, gameName);
                    statement.setString(6, name);

                    statement.execute();
                }
            }
        } catch (SQLException ex) {
            plugin.debug("Error while saving a Warrior: " + ex.getMessage(), false);
        }
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
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            ResultSet query = statement.executeQuery();

            Map<String, Map<CountType, Map<String, Integer>>> dataMap = new HashMap<>();

            while (query.next()) {
                String id = query.getString("identification");
                String game = String.valueOf(query.getString("game"));
                dataMap.computeIfAbsent(id, k -> new EnumMap<>(CountType.class));
                final Map<CountType, Map<String, Integer>> groupData = dataMap.get(id);
                for (CountType t : CountType.values()) {
                    groupData.computeIfAbsent(t, k -> new HashMap<>());
                    groupData.get(t).put(game, query.getInt(t.name()));
                }
            }

            for (Map.Entry<String, Map<CountType, Map<String, Integer>>> entry : dataMap.entrySet()) {
                Map<CountType, Map<String, Integer>> data = entry.getValue();

                GroupData groupData = new GroupData(data.get(CountType.VICTORIES), data.get(CountType.DEFEATS),
                        data.get(CountType.KILLS), data.get(CountType.DEATHS));
                groups.put(entry.getKey(), groupData);
            }
        } catch (SQLException ex) {
            plugin.debug("Error while getting a Group: " + ex.getMessage(), false);
        }
    }

    private void loopThroughWarriors() {
        String sql = "SELECT * FROM tb_warriors;";
        try (Statement statement = getConnection().createStatement()) {
            ResultSet rs = statement.executeQuery(sql);

            Map<UUID, Map<CountType, Map<String, Integer>>> players = new HashMap<>();

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                String game = String.valueOf(rs.getString("game"));
                players.computeIfAbsent(uuid, k -> new EnumMap<>(CountType.class));
                final Map<CountType, Map<String, Integer>> playerData = players.get(uuid);
                for (CountType t : CountType.values()) {
                    if (t == CountType.DEFEATS) continue;
                    playerData.computeIfAbsent(t, k -> new HashMap<>());
                    playerData.get(t).put(game, rs.getInt(t.name()));
                }
            }

            for (Map.Entry<UUID, Map<CountType, Map<String, Integer>>> entry : players.entrySet()) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getKey());
                Map<CountType, Map<String, Integer>> playerData = entry.getValue();

                Warrior warrior = new Warrior(player, plugin::getGroupManager, playerData.get(CountType.KILLS),
                        playerData.get(CountType.DEATHS), playerData.get(CountType.VICTORIES));
                warriors.add(warrior);
            }


        } catch (SQLException ex) {
            plugin.debug("An error ocurred while trying to load the players data! " + ex.getMessage(),
                    false);
        }
    }

    @SuppressWarnings("unchecked")
    private void loopThroughWinners() {
        String sql = "SELECT * FROM tb_winners;";
        try (Statement statement = getConnection().createStatement()) {
            ResultSet rs = statement.executeQuery(sql);

            Map<Date, Map<WinnerType, Map<String, Object>>> winnersData = new HashMap<>();

            while (rs.next()) {
                Date date = parseDate(rs.getString("date"));
                if (date == null) continue;

                String game = String.valueOf(rs.getString("game"));

                winnersData.computeIfAbsent(date, k -> new EnumMap<>(WinnerType.class));
                Map<WinnerType, Map<String, Object>> data = winnersData.get(date);
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
                            break;
                    }
                }
            }

            for (Map.Entry<Date, Map<WinnerType, Map<String, Object>>> dateEntry : winnersData.entrySet()) {
                Map<WinnerType, Map<String, Object>> data = dateEntry.getValue();

                Map<String, UUID> killer = new HashMap<>();
                Map<String, List<UUID>> playerWinners = new HashMap<>();
                Map<String, String> winnerGroup = new HashMap<>();

                for (Map.Entry<WinnerType, Map<String, Object>> wtEntry : data.entrySet()) {
                    for (Map.Entry<String, Object> gameEntry : wtEntry.getValue().entrySet()) {
                        String game = gameEntry.getKey();
                        Object innerObject = gameEntry.getValue();
                        switch (wtEntry.getKey()) {
                            case KILLER:
                                killer.put(game, (UUID) innerObject);
                                break;
                            case WINNER_GROUP:
                                winnerGroup.put(game, (String) innerObject);
                                break;
                            case PLAYER_WINNER:
                                playerWinners.put(game, (List<UUID>) innerObject);
                                break;
                        }
                    }
                }

                Winners winner = new Winners(dateEntry.getKey(), killer, playerWinners, winnerGroup);
                winners.add(winner);
            }

        } catch (SQLException ex) {
            plugin.debug("An error occurred while trying to load the winner data! " + ex.getMessage(), false);
        }

        winners.sort(Comparator.naturalOrder());
    }

    public void loadDataToMemory() {
        loopThroughGroups();
        loopThroughWarriors();
        loopThroughWinners();
    }

    public void saveAll() {
        // Copying collections for async use, avoiding ConcurrentModificationException
        final Map<String, GroupData> groupMap = new HashMap<>(getGroups());
        final Set<Warrior> warriorsSet = new HashSet<>(getWarriors());
        final List<Winners> winnersList = new ArrayList<>(getWinners());

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (Map.Entry<String, GroupData> data : groupMap.entrySet()) {
                if (data.getValue().isModified()) {
                    update(data.getKey(), data.getValue());
                    data.getValue().setModified(false);
                }
            }

            for (Warrior warrior : warriorsSet) {
                if (warrior.isModified()) {
                    update(warrior);
                    warrior.setModified(false);
                }
            }

            for (Winners winner : winnersList) {
                if (winner.isModified()) {
                    update(winner);
                    winner.setModified(false);
                }
            }
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
            if (latest != null && Helper.equalDates(today, latest)) {
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

    private @NotNull Winners getEmptyWinners(@Nullable Date date) {
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

    private @NotNull Set<GameConfiguration> getGames() {
        return plugin.getConfigurationDao().getConfigurations(GameConfiguration.class);
    }

    private @Nullable Date parseDate(String dateString) {
        try {
            return new SimpleDateFormat("dd/MM/yyyy").parse(dateString);
        } catch (ParseException ex) {
            plugin.debug("Invalid date! " + ex.getMessage(), false);
            return null;
        }
    }
}
