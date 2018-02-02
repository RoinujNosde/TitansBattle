package me.roinujnosde.titansbattle;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import me.roinujnosde.titansbattle.managers.ConfigManager;
import me.roinujnosde.titansbattle.managers.GameManager;
import me.roinujnosde.titansbattle.managers.TaskManager;
import me.roinujnosde.titansbattle.types.Game;
import me.roinujnosde.titansbattle.types.Game.Mode;
import me.roinujnosde.titansbattle.types.Scheduler;
import net.milkbowl.vault.economy.EconomyResponse;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

public class Helper {

    TitansBattle plugin;
    GameManager gm;
    TaskManager tm;
    ConfigManager cm;

    HashMap<Player, HashMap<Integer, ItemStack>> itemsNotGiven = new HashMap<>();

    public void load() {
        plugin = TitansBattle.getInstance();
        gm = TitansBattle.getGameManager();
        tm = TitansBattle.getTaskManager();
        cm = TitansBattle.getConfigManager();
    }

    public void teleportAll(Location destination) {
        if (gm.getParticipants().isEmpty()) {
            return;
        }
        for (UUID uuid : gm.getParticipants()) {
            Player p = Bukkit.getPlayer(uuid);
            p.teleport(destination);
        }
    }
    
    public void clearInventory(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
    }

    public boolean isFun(Game game) {
        if (game.getMode().equals(Mode.FREEFORALL_FUN)) {
            return true;
        }
        if (game.getMode().equals(Mode.GROUPS_FUN)) {
            return true;
        }
        return false;
    }

    public boolean isReal(Game game) {
        return !isFun(game);
    }

    public boolean isGroupBased(Game game) {
        if (game.getMode() == Mode.GROUPS_FUN) {
            return true;
        }
        if (game.getMode() == Mode.GROUPS_REAL) {
            return true;
        }
        return false;
    }

    public Game getGameFromWinnerOrKiller(Player player) {
        for (Game game : cm.getFilesAndGames().values()) {
            if (game.getKiller().equals(player.getUniqueId())) {
                return game;
            }
            if (game.getWinners().contains(player.getUniqueId())) {
                return game;
            }
        }
        return null;
    }

    public static List<ItemStack> removeNullItems(List<ItemStack> items) {
        List<ItemStack> nonNull = new ArrayList<>();
        for (ItemStack item : items) {
            if (item == null) {
                continue;
            }
            nonNull.add(item);
        }
        return nonNull;
    }

    public boolean isWinner(Player player) {
        for (Game game : cm.getFilesAndGames().values()) {
            if (game.getWinners().contains(player.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public String getWinnerPrefix(Player player) {
        if (isWinner(player)) {
            for (Game game : cm.getFilesAndGames().values()) {
                if (game.getWinners().contains(player.getUniqueId())) {
                    if (game.getWinnerPrefix() == null || game.getWinnerPrefix().equals("")) {
                        return null;
                    }
                    return game.getWinnerPrefix();
                }
            }
        }
        return null;
    }

    public String getWinnerPrefixPlaceholder(Player player) {
        for (Game game : cm.getFilesAndGames().values()) {
            if (game.getWinners().contains(player.getUniqueId())) {
                if (game.getMode() == Mode.GROUPS_FUN) {
                    return "tb_groupsfun_winner";
                }
                if (game.getMode() == Mode.GROUPS_REAL) {
                    return "tb_groupsreal_winner";
                }
                if (game.getMode() == Mode.FREEFORALL_FUN) {
                    return "tb_freeforallfun_winner";
                }
                if (game.getMode() == Mode.FREEFORALL_REAL) {
                    return "tb_freeforallreal_winner";
                }
            }
        }
        return "";
    }

    public boolean isKiller(Player player) {
        for (Game game : cm.getFilesAndGames().values()) {
            if (game.getKiller().equals(player.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public String getKillerPrefix(Player player) {
        if (isKiller(player)) {
            for (Game game : cm.getFilesAndGames().values()) {
                if (game.getKiller().equals(player.getUniqueId())) {
                    if (game.getKillerPrefix() == null || game.getKillerPrefix().equals("")) {
                        return null;
                    }
                    return game.getKillerPrefix();
                }
            }
        }
        return null;
    }

    public String getKillerPrefixPlaceholder(Player player) {
        for (Game game : cm.getFilesAndGames().values()) {
            if (game.getKiller().equals(player.getUniqueId())) {
                if (game.getMode() == Mode.GROUPS_FUN) {
                    return "tb_groupsfun_killer";
                }
                if (game.getMode() == Mode.GROUPS_REAL) {
                    return "tb_groupsreal_killer";
                }
                if (game.getMode() == Mode.FREEFORALL_FUN) {
                    return "tb_freeforallfun_killer";
                }
                if (game.getMode() == Mode.FREEFORALL_REAL) {
                    return "tb_freeforallreal_killer";
                }
            }
        }
        return "";
    }

    public boolean isKillerPriority(Player player) {
        if (isKiller(player)) {
            for (Game game : cm.getFilesAndGames().values()) {
                if (game.getKiller().equals(player.getUniqueId())) {
                    return game.isKillerPriority();
                }
            }
        }
        return false;
    }

    public boolean isKillerJoinMessageEnabled(Player player) {
        if (isKiller(player)) {
            for (Game game : cm.getFilesAndGames().values()) {
                if (game.getKiller().equals(player.getUniqueId())) {
                    return game.isKillerJoinMessage();
                }
            }
        }
        return false;
    }

    public boolean isKillerQuitMessageEnabled(Player player) {
        if (isKiller(player)) {
            for (Game game : cm.getFilesAndGames().values()) {
                if (game.getKiller().equals(player.getUniqueId())) {
                    return game.isKillerQuitMessage();
                }
            }
        }
        return false;
    }

    public static List<UUID> stringListToUuidList(List<String> list) {
        List<UUID> uuidList = new ArrayList<>();
        for (String uuid : list) {
            uuidList.add(UUID.fromString(uuid));
        }
        return uuidList;
    }

    public static List<String> uuidListToPlayerNameList(List<UUID> list) {
        List<String> playerNameList = new ArrayList<>();
        for (UUID uuid : list) {
            playerNameList.add(Bukkit.getOfflinePlayer(uuid).getName());
        }
        return playerNameList;
    }

    public static List<String> uuidListToStringList(List<UUID> list) {
        List<String> stringList = new ArrayList<>();
        for (UUID uuid : list) {
            stringList.add(uuid.toString());
        }
        return stringList;
    }

    public Game getGame(Mode mode) {
        for (Game g : cm.getFilesAndGames().values()) {
            if (g.getMode() == mode) {
                return g;
            }
        }
        return null;
    }

    public boolean isWinnerJoinMessageEnabled(Player player) {
        if (isKiller(player)) {
            for (Game game : cm.getFilesAndGames().values()) {
                if (game.getWinners().contains(player.getUniqueId())) {
                    return game.isWinnerJoinMessage();
                }
            }
        }
        return false;
    }

    public boolean isWinnerQuitMessageEnabled(Player player) {
        if (isWinner(player)) {
            for (Game game : cm.getFilesAndGames().values()) {
                if (game.getWinners().contains(player.getUniqueId())) {
                    return game.isWinnerQuitMessage();
                }
            }
        }
        return false;
    }

    public List<String> getStringList(String path) {
        List<String> list;
        list = plugin.getConfig().getStringList(path);
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    public boolean areAllied(Player player1, Player player2) {
        if (plugin.isFactions()) {
            Faction faction1 = MPlayer.get(player1).getFaction();
            Faction faction2 = MPlayer.get(player2).getFaction();
            if (faction1.getRelationWish(faction2) == Rel.TRUCE || faction1.getRelationWish(faction2) == Rel.ALLY) {
                return true;
            }
        }
        if (plugin.isSimpleClans()) {
            Clan clan1 = plugin.getClanManager().getClanPlayer(player1.getUniqueId()).getClan();
            Clan clan2 = plugin.getClanManager().getClanPlayer(player2.getUniqueId()).getClan();
            if (clan1.isAlly(clan2.getTag())) {
                return true;
            }
        }
        return false;
    }

    public void increaseKillsCount(Player killer) {
        int kills = gm.getPlayerKillsCount(killer);
        kills++;
        gm.getKillsCount().replace(killer, kills);
    }

    public Player getPlayerAttackerOrKiller(Entity toInvestigate) {
        Player investigated = null;
        if (toInvestigate instanceof Player) {
            investigated = (Player) toInvestigate;
        }
        if (toInvestigate instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) toInvestigate).getShooter();
            if (shooter instanceof Player) {
                investigated = (Player) shooter;
            }
        }
        return investigated;
    }

    public static String getStringFromStringList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        List<String> aList = (List<String>) list;
        for (String a : aList) {
            //Checa se é o primeiro vencedor
            if (a.equalsIgnoreCase(aList.get(0))) {
                sb.append(a);
                //Checa se é o último vencedor
            } else if (a.equals(aList.get(aList.size() - 1))) {
                sb.append(" & ");
                sb.append(a);
                //Não é nenhum dos acima
            } else {
                sb.append(", ");
                sb.append(a);
            }
        }
        return sb.toString();
    }

    public static String getStringFromClanList(List<Clan> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        List<Clan> clans = (List<Clan>) list;
        for (Clan clan : clans) {
            String tag = clan.getTag();
            if (tag.equalsIgnoreCase(clans.get(0).getTag())) {
                sb.append(tag);
            } else if (tag.equals(clans.get(clans.size() - 1).getTag())) {
                sb.append(" & ");
                sb.append(tag);
            } else {
                sb.append(", ");
                sb.append(tag);
            }
        }
        return sb.toString();
    }

    public static String getStringFromFactionList(List<Faction> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        List<Faction> factions = (List<Faction>) list;
        for (Faction faction : factions) {
            String name = faction.getName();
            if (name.equalsIgnoreCase(factions.get(0).getName())) {
                sb.append(name);
            } else if (name.equals(factions.get(factions.size() - 1).getName())) {
                sb.append(" & ");
                sb.append(name);
            } else {
                sb.append(", ");
                sb.append(name);
            }
        }
        return sb.toString();
    }

    public HashMap<Player, HashMap<Integer, ItemStack>> getItemsNotGiven() {
        return itemsNotGiven;
    }

    public Set<Player> getPlayersWithItemsToReceive() {
        if (itemsNotGiven.isEmpty()) {
            return new TreeSet<>();
        }
        return itemsNotGiven.keySet();
    }

    public ItemStack[] getItemsNotGivenToPlayer(Player player) {
        if (itemsNotGiven.containsKey(player)) {
            List<ItemStack> items = (List<ItemStack>) ((HashMap<Integer, ItemStack>) itemsNotGiven.values()).values();
            return items.toArray(new ItemStack[0]);
        }
        return new ItemStack[0];
    }

    public void giveItemsToPlayers(Game game, List<Player> players, ItemStack[] items) {
        for (Player player : players) {
            Inventory inventory = player.getInventory();
            HashMap<Integer, ItemStack> remainingItems = inventory.addItem(items);
            if (!remainingItems.isEmpty()) {
                itemsNotGiven.put(player, remainingItems);
                tm.startGiveItemsTask(game.getPrizes().getItemsGiveInterval());
            }
        }
    }

    public void runCommandsOnPlayers(List<Player> players, List<String> commands, double someNumber, boolean divide) {
        if (divide) {
            someNumber = someNumber / players.size();
        }
        for (Player player : players) {
            for (String command : commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        command.replaceAll("%player%", player.getName()).replaceAll("%some_number%", Double.toString(someNumber)));
            }
        }
    }

    public void giveMoneyToPlayers(List<Player> players, double amount, boolean share) {
        if (TitansBattle.getEconomy() == null) {
            return;
        }
        if (share) {
            //Share prize
            amount = (amount / players.size());
            for (Player player : players) {
                EconomyResponse r = TitansBattle.getEconomy().depositPlayer(player, amount);
                if (!r.transactionSuccess()) {
                    System.out.println("[TitansBattle] Error: " + r.errorMessage);
                }
            }
            return;
        }
        for (Player player : players) {
            EconomyResponse r = TitansBattle.getEconomy().depositPlayer(player, amount);
            if (!r.transactionSuccess()) {
                System.out.println("[TitansBattle] Error: " + r.errorMessage);
            }
        }
    }

    public String[] removeFirstArg(String[] args) {
        int length = args.length;
        if (length <= 1) {
            return new String[0];
        }
        int newLength = length - 1;
        String[] newArgs = new String[newLength];
        for (int i = 0; i < newLength;) {
            newArgs[i] = args[i + 1];
            i++;
        }
        return newArgs;
    }

    public int getCurrentTimeInSeconds() {
        Calendar today = Calendar.getInstance();
        int hour = today.get(Calendar.HOUR_OF_DAY);
        int minute = today.get(Calendar.MINUTE);

        return ((hour * 60 * 60) + (minute * 60));
    }

    public Scheduler getNextSchedulerOfDay() {
        Calendar today = Calendar.getInstance();
        int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);
        int hour = today.get(Calendar.HOUR_OF_DAY);
        int minute = today.get(Calendar.MINUTE);

        Scheduler nextScheduler = null;
        boolean firstScheduler = true;
        int nextHour = 0;
        int nextMinute = 0;

        for (Scheduler s : cm.getSchedulers()) {
            //Is it the day of the Scheduler?
            if (s.getDay() == dayOfWeek) {
                //Is it the first Scheduler looped?
                if (firstScheduler) {
                    if (!(s.getHour() < hour) && !(s.getMinute() < minute)) {
                        nextScheduler = s;
                        nextHour = s.getHour();
                        nextMinute = s.getMinute();
                        firstScheduler = false;
                    }
                    continue;
                }
                if ((s.getHour() <= nextHour) && !(s.getHour() < hour)) {
                    if (s.getHour() == nextHour) {
                        if ((s.getMinute() < nextMinute) && !(s.getMinute() < minute)) {
                            nextScheduler = s;
                            nextHour = s.getHour();
                            nextMinute = s.getMinute();
                            continue;
                        }
                    }
                    if (s.getHour() < nextHour) {
                        if (s.getMinute() >= minute) {
                            nextScheduler = s;
                            nextHour = s.getHour();
                            nextMinute = s.getMinute();
                        }
                    }
                }
            }
        }
        return nextScheduler;
    }
}
