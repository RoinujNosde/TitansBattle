package me.roinujnosde.titansbattle.games;

import me.roinujnosde.titansbattle.*;
import me.roinujnosde.titansbattle.managers.ConfigManager;
import me.roinujnosde.titansbattle.managers.DatabaseManager;
import me.roinujnosde.titansbattle.managers.GameManager;
import me.roinujnosde.titansbattle.types.GameConfiguration;
import me.roinujnosde.titansbattle.types.Prizes;
import me.roinujnosde.titansbattle.types.Warrior;
import me.roinujnosde.titansbattle.types.Winners;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class EliminationTournamentGameTest {


    static Server server;
    static PluginManager pluginManager;
    static FakeBukkitScheduler scheduler = new FakeBukkitScheduler();
    static TitansBattle plugin;
    static DatabaseManager databaseManager;
    static GameConfiguration config;
    static EliminationTournamentGame game;

    @BeforeAll
    public static void setup() {

        pluginManager = mock(PluginManager.class);

        server = mock(Server.class);
        when(server.getLogger()).thenReturn(mock(Logger.class));
        when(server.getScheduler()).thenReturn(scheduler);
        when(server.getPluginManager()).thenReturn(pluginManager);

        databaseManager = mock(DatabaseManager.class);
        when(databaseManager.getTodaysWinners()).thenReturn(mock(Winners.class));

        FileConfiguration fileConfiguration = mock(FileConfiguration.class);

        plugin = mock(TitansBattle.class);
        when(plugin.getLogger()).thenReturn(mock(Logger.class));
        when(plugin.getGameManager()).thenReturn(mock(GameManager.class));
        when(plugin.getDatabaseManager()).thenReturn(databaseManager);
        when(plugin.getConfigManager()).thenReturn(mock(ConfigManager.class));
        when(plugin.getLang(anyString(), any(BaseGame.class), any(Object[].class))).thenReturn("");
        when(plugin.getConfig()).thenReturn(fileConfiguration);
        when(fileConfiguration.getString(anyString(), anyString())).thenReturn("");

        config = mock(GameConfiguration.class);
        when(config.getAnnouncementStartingInterval()).thenReturn(5);
        when(config.getAnnouncementStartingTimes()).thenReturn(2);
        when(config.locationsSet()).thenReturn(true);
        when(config.getLobby()).thenReturn(mock(Location.class));
        when(config.getExit()).thenReturn(mock(Location.class));
        when(config.getWatchroom()).thenReturn(mock(Location.class));
        when(config.getPrizes(any(BaseGameConfiguration.Prize.class))).thenReturn(mock(Prizes.class));
        Map<Integer, Location> entrances = Collections.singletonMap(0, mock(Location.class));
        when(config.getArenaEntrances()).thenReturn(entrances);
        when(config.getName()).thenReturn("test-game");

        Bukkit.setServer(server);
    }

    @BeforeEach
    public void setupGame() {
        game = new EliminationTournamentGame(plugin, config);
    }

    @RepeatedTest(50)
    public void simpleWinnerOdd(RepetitionInfo repetitionInfo) {
        simpleWinner(repetitionInfo.getCurrentRepetition(), 1);
    }

    @RepeatedTest(50)
    public void simpleWinnerEven(RepetitionInfo repetitionInfo) {
        simpleWinner(repetitionInfo.getCurrentRepetition(), 0);
    }

    private void simpleWinner(int players, int oddOrEven) {
        game.start();
        scheduler.performOneTick();
        for (int i = 0; i < players; i++) {
            game.onJoin(new Warrior(new FakePlayer(), plugin::getGroupManager));
        }
        assertTrue(game.isLobby());
        scheduler.performTicks(300); //lobby announcements
        assertFalse(game.isLobby());

        Warrior expectedWinner = game.getParticipants().get(0);
        int count = 0;
        while (game.getParticipants().size() > 1) {
            List<Warrior> current = new ArrayList<>(game.getCurrentFighters());
            Warrior victim = null;
            if (count % 2 == oddOrEven) {
                List<Warrior> list = game.getParticipants().stream().filter(w -> !current.contains(w) && w != expectedWinner).collect(Collectors.toList());
                if (!list.isEmpty()) {
                    game.onDisconnect(list.get((int) (list.size() * Math.random())));
                }
            } else {
                victim = current.get(0) != expectedWinner ? current.get(0) : current.get(1);
                Warrior killer = current.get(0) == victim ? current.get(1) : current.get(0);
                game.onDeath(victim, killer);
            }

            scheduler.performTicks(21); //respawn delay
            if (victim != null) {
                assertTrue(game.getCasualties().contains(victim));
            }
            count++;
        }
        assertEquals(1, expectedWinner.getVictories("test-game"));
    }

}