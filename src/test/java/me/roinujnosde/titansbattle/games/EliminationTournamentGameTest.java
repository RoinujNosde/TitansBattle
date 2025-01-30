package me.roinujnosde.titansbattle.games;

import me.roinujnosde.titansbattle.*;
import me.roinujnosde.titansbattle.managers.ConfigManager;
import me.roinujnosde.titansbattle.managers.DatabaseManager;
import me.roinujnosde.titansbattle.managers.GameManager;
import me.roinujnosde.titansbattle.types.GameConfiguration;
import me.roinujnosde.titansbattle.types.GroupData;
import me.roinujnosde.titansbattle.types.Prizes;
import me.roinujnosde.titansbattle.types.Warrior;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.mockito.Mockito;

import java.util.*;
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
    static FakeGroupManager groupManager;
    static DatabaseManager databaseManager;
    static GameConfiguration config;
    EliminationTournamentGame game = new EliminationTournamentGame(plugin, config);


    @BeforeAll
    public static void setup() {
        plugin = mock(TitansBattle.class);
        Mockito.mockStatic(TitansBattle.class).when(TitansBattle::getInstance).thenReturn(plugin);

        pluginManager = mock(PluginManager.class);

        server = mock(Server.class);
        when(server.getLogger()).thenReturn(mock(Logger.class));
        when(server.getScheduler()).thenReturn(scheduler);
        when(server.getPluginManager()).thenReturn(pluginManager);
        when(server.getOfflinePlayer(any(UUID.class))).thenAnswer(i -> new FakePlayer(i.getArgument(0)));

        databaseManager = new DatabaseManager();

        FileConfiguration fileConfiguration = mock(FileConfiguration.class);

        when(plugin.getLogger()).thenReturn(mock(Logger.class));
        when(plugin.getGameManager()).thenReturn(mock(GameManager.class));
        when(plugin.getDatabaseManager()).thenReturn(databaseManager);
        when(plugin.getConfigManager()).thenReturn(mock(ConfigManager.class));
        when(plugin.getLang(anyString(), any(BaseGame.class), any(Object[].class))).thenReturn("");
        when(plugin.getConfig()).thenReturn(fileConfiguration);
        when(fileConfiguration.getString(anyString(), anyString())).thenReturn("");

        groupManager = new FakeGroupManager(plugin);
        when(plugin.getGroupManager()).thenReturn(groupManager);

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

    @RepeatedTest(50)
    public void simpleWinnerOdd(RepetitionInfo repetitionInfo) {
        simpleWinner(repetitionInfo.getCurrentRepetition(), 1);
    }

    @RepeatedTest(50)
    public void simpleWinnerEven(RepetitionInfo repetitionInfo) {
        simpleWinner(repetitionInfo.getCurrentRepetition(), 0);
    }

    private void simpleWinner(int players, int oddOrEven) {
        when(config.isGroupMode()).thenReturn(false);

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

    @RepeatedTest(20)
    public void groupModeOdd(RepetitionInfo repetitionInfo) {
        groupMode(repetitionInfo.getCurrentRepetition(), 1);
    }

    @RepeatedTest(20)
    public void groupModeEven(RepetitionInfo repetitionInfo) {
        groupMode(repetitionInfo.getCurrentRepetition(), 0);
    }

    private void groupMode(int size, int oddOrEven) {
        groupManager.getGroups().clear();
        when(config.isGroupMode()).thenReturn(true);

        game.start();
        scheduler.performOneTick();
        for (int i = 0; i < size; i++) {
            FakeGroup group = new FakeGroup(new GroupData());
            groupManager.addGroup(group);
            for (int j = 0; j < 4 + oddOrEven; j++) {
                Warrior warrior = databaseManager.getWarrior(UUID.randomUUID());
                group.addMember(warrior.getUniqueId());

                game.onJoin(warrior);
            }
        }

        assertTrue(game.isLobby());
        scheduler.performTicks(300); //lobby announcements
        assertFalse(game.isLobby());

        Warrior expectedWinner = game.getParticipants().get(0);

        int count = 0;
        while (game.getGroupParticipants().size() > 1) {
            List<Warrior> current = new ArrayList<>(game.getCurrentFighters());
            Warrior victim = null;
            if (count % 2 == oddOrEven) {
                List<Warrior> list = game.getParticipants().parallelStream().filter(w -> !current.contains(w) && w != expectedWinner).collect(Collectors.toList());
                if (!list.isEmpty()) {
                    Warrior warrior = list.get(list.size() - 1);
                    game.onDisconnect(warrior);
                }
            } else {
                Warrior killer = null;
                for (Warrior warrior : current) {
                    if (warrior != expectedWinner) {
                        victim = warrior;
                        break;
                    }
                }
                for (Warrior warrior : current) {
                    if (victim != null && warrior != victim && warrior.getGroup() != victim.getGroup()) {
                        killer = warrior;
                        break;
                    }
                }

                if (victim != null && killer != null) {
                    game.onDeath(victim, killer);
                } else {
                    throw new IllegalStateException();
                }
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