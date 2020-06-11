package me.roinujnosde.titansbattle;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import me.roinujnosde.titansbattle.managers.ConfigManager;
import me.roinujnosde.titansbattle.managers.GameManager;
import me.roinujnosde.titansbattle.managers.TaskManager;
import me.roinujnosde.titansbattle.types.Game;
import me.roinujnosde.titansbattle.types.Game.Mode;
import me.roinujnosde.titansbattle.types.Group;
import me.roinujnosde.titansbattle.types.GroupWrapper;
import me.roinujnosde.titansbattle.types.Scheduler;
import me.roinujnosde.titansbattle.types.Warrior;
import me.roinujnosde.titansbattle.types.Winners;
import net.milkbowl.vault.economy.EconomyResponse;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;

public class Helper {

    private TitansBattle plugin;
    private GameManager gm;
    private TaskManager tm;
    private ConfigManager cm;

    HashMap<Player, HashMap<Integer, ItemStack>> itemsNotGiven = new HashMap<>();

    /**
     * Loads the Helper class (used internally)
     */
    public void load() {
        plugin = TitansBattle.getInstance();
        gm = plugin.getGameManager();
        tm = plugin.getTaskManager();
        cm = plugin.getConfigManager();
    }

    /**
     * Teleports all participants to the destination
     *
     * @param destination the destination
     */
    public void teleportAll(Location destination) {
        if (gm.getParticipants().isEmpty()) {
            return;
        }

        if (destination == null) {
            gm.getParticipants().stream().map(Bukkit::getPlayer).forEach(player -> {
                player.sendMessage(ChatColor.RED + "An error has ocurred while trying to teleport you! Contact the admin! :o");
            });
            plugin.debug("You have not setted all teleport destinations!", false);
            return;
        }

        gm.getParticipants().forEach(uuid -> Bukkit.getPlayer(uuid).teleport(destination));
    }

    /**
     * Clears the inventory of this player
     *
     * @param player the player
     */
    public void clearInventory(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
    }

    /**
     * Gets the player inventory and converts it to a List
     *
     * @param player the player
     * @return the inventory as list
     */
    public List<ItemStack> getInventoryAsList(Player player) {
        return Arrays.asList(player.getInventory().getContents());
    }

    /**
     * Checks if the Mode of the specified Game is FUN
     *
     * @param game the game
     * @return if it is FUN, or not
     */
    public boolean isFun(Game game) {
        return game.getMode().equals(Mode.FREEFORALL_FUN) || game.getMode().equals(Mode.GROUPS_FUN);
    }

    /**
     * Checks if the Mode of the specified Game is REAL
     *
     * @param game the game
     * @return if it is REAL, or not
     */
    public boolean isReal(Game game) {
        return !isFun(game);
    }

    /**
     * Checks if this Game is based on Groups
     *
     * @param game the game
     * @return if it is group based or not
     */
    public boolean isGroupBased(Game game) {
        return game.getMode() == Mode.GROUPS_FUN || game.getMode() == Mode.GROUPS_REAL;
    }

    /**
     * Gets a Game object where this Player is a Winner or Killer, or null if he
     * is neither one
     *
     * @param player the player
     * @return the game
     */
    public Game getGameFromWinnerOrKiller(Player player) {
        if (player == null) {
            return null;
        }
        final UUID uniqueId = player.getUniqueId();
        for (Game game : cm.getFilesAndGames().values()) {
            final Mode mode = game.getMode();
            Winners w = plugin.getDatabaseManager().getLatestWinners();
            if (w.getKiller(mode) == null) {
                continue;
            }
            if (w.getKiller(mode).equals(uniqueId)) {
                return game;
            }
            Set<UUID> playerWinners = w.getPlayerWinners(mode);
            if (playerWinners == null) {
                continue;
            }
            if (playerWinners.contains(uniqueId)) {
                return game;
            }
        }
        return null;
    }

    /**
     * Removes the null items from the inventory
     *
     * @param items the inventory
     * @return the list without null items
     */
    public static List<ItemStack> removeNullItems(List<ItemStack> items) {
        if (items == null) {
            return new ArrayList<>();
        }
        return items.stream().filter(item -> item != null).collect(Collectors.toList());
    }

    /**
     * Returns whether he is a Winner or not
     *
     * @param player the player
     * @return true if he is a Winner
     */
    public boolean isWinner(Player player) {
        for (Mode mode : Mode.values()) {
            Set<UUID> winners = plugin.getDatabaseManager().getLatestWinners().getPlayerWinners(mode);
            if (winners == null) {
                continue;
            }
            if (winners.contains(player.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the Winner Prefix for this player, or null if he is not a Winner
     *
     * @param player the player
     * @return the prefix or null
     */
    public String getWinnerPrefix(Player player) {
        if (isWinner(player)) {
            for (Game game : cm.getFilesAndGames().values()) {
                Winners latestWinners = plugin.getDatabaseManager().getLatestWinners();
                Set<UUID> playerWinners = latestWinners.getPlayerWinners(game.getMode());
                if (playerWinners == null) {
                    continue;
                }
                if (playerWinners.contains(player.getUniqueId())) {
                    if (game.getWinnerPrefix() == null || game.getWinnerPrefix().equals("")) {
                        return null;
                    }
                    return game.getWinnerPrefix();
                }
            }
        }
        return null;
    }

    /**
     * Gets the Winner Prefix Placeholder for this player
     *
     * @param player the player
     * @return the Winner Prefix Placeholder
     */
    public String getWinnerPrefixPlaceholder(Player player) {
        for (Game game : cm.getFilesAndGames().values()) {
            Winners latestWinners = plugin.getDatabaseManager().getLatestWinners();
            Set<UUID> playerWinners = latestWinners.getPlayerWinners(game.getMode());
            if (playerWinners == null) {
                continue;
            }
            if (playerWinners.contains(player.getUniqueId())) {
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

    /**
     * Returns whether he is a Winner or not
     *
     * @param player the player
     * @return true if he is a Winner
     */
    public boolean isKiller(Player player) {
        for (Game game : cm.getFilesAndGames().values()) {
            Winners latestWinners = plugin.getDatabaseManager().getLatestWinners();
            UUID killer = latestWinners.getKiller(game.getMode());
            if (killer == null) {
                continue;
            }
            if (killer.equals(player.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the Killer Prefix for this player, or null if he is not a Killer
     *
     * @param player the player
     * @return the prefix or null
     */
    public String getKillerPrefix(Player player) {
        if (isKiller(player)) {
            for (Game game : cm.getFilesAndGames().values()) {
                Winners latestWinners = plugin.getDatabaseManager().getLatestWinners();
                UUID killer = latestWinners.getKiller(game.getMode());
                if (killer == null) {
                    continue;
                }
                if (killer.equals(player.getUniqueId())) {
                    if (game.getKillerPrefix() == null || game.getKillerPrefix().equals("")) {
                        return null;
                    }
                    return game.getKillerPrefix();
                }
            }
        }
        return null;
    }

    /**
     * Gets the Killer Prefix Placeholder for this player
     *
     * @param player the player
     * @return the Killer Prefix Placeholder
     */
    public String getKillerPrefixPlaceholder(Player player) {
        for (Game game : cm.getFilesAndGames().values()) {
            Winners latestWinners = plugin.getDatabaseManager().getLatestWinners();
            UUID killer = latestWinners.getKiller(game.getMode());
            if (killer == null) {
                continue;
            }
            if (killer.equals(player.getUniqueId())) {
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

    /**
     * Checks if Killer is set as priority for this Killer's game config
     *
     * @param player the killer
     * @return true if it is priority
     */
    public boolean isKillerPriority(Player player) {
        if (isKiller(player)) {
            for (Game game : cm.getFilesAndGames().values()) {
                Winners latestWinners = plugin.getDatabaseManager().getLatestWinners();
                UUID killer = latestWinners.getKiller(game.getMode());
                if (killer == null) {
                    continue;
                }
                if (killer.equals(player.getUniqueId())) {
                    return game.isKillerPriority();
                }
            }
        }
        return false;
    }

    /**
     * Checks if Killer Join Message is enabled for this Killer's game config
     *
     * @param player the killer
     * @return if it is enabled
     */
    public boolean isKillerJoinMessageEnabled(Player player) {
        if (isKiller(player)) {
            for (Game game : cm.getFilesAndGames().values()) {
                Winners latestWinners = plugin.getDatabaseManager().getLatestWinners();
                UUID killer = latestWinners.getKiller(game.getMode());
                if (killer == null) {
                    continue;
                }
                if (killer.equals(player.getUniqueId())) {
                    return game.isKillerJoinMessage();
                }
            }
        }
        return false;
    }

    /**
     * Checks if Killer Quit Message is enabled for this Killer's game config
     *
     * @param player the killer
     * @return if it is enabled
     */
    public boolean isKillerQuitMessageEnabled(Player player) {
        if (isKiller(player)) {
            for (Game game : cm.getFilesAndGames().values()) {
                Winners latestWinners = plugin.getDatabaseManager().getLatestWinners();
                UUID killer = latestWinners.getKiller(game.getMode());
                if (killer == null) {
                    continue;
                }
                if (killer.equals(player.getUniqueId())) {
                    return game.isKillerQuitMessage();
                }
            }
        }
        return false;
    }

    /**
     * Converts a list of Strings to a list of UUIDs
     *
     * @param list the String list
     * @return the UUID list
     */
    public static List<UUID> stringListToUuidList(List<String> list) {
        List<UUID> uuidList = new ArrayList<>();
        for (String uuid : list) {
            uuidList.add(UUID.fromString(uuid));
        }
        return uuidList;
    }

    /**
     * Converts a set of UUIDs to a list of Strings using
     * {@link org.bukkit.Bukkit#getOfflinePlayer(java.util.UUID) getOfflinePlayer}
     *
     * @param set the UUID set
     * @return the String list
     */
    public List<String> uuidListToPlayerNameList(Set<UUID> set) {
        if (set == null) {
            return null;
        }
        return uuidListToPlayerNameList(new ArrayList<>(set));
    }

    /**
     * Converts a list of UUIDs to a list of Strings using
     * {@link org.bukkit.Bukkit#getOfflinePlayer(java.util.UUID) getOfflinePlayer}
     *
     * @param list the UUID list
     * @return the String list
     */
    public List<String> uuidListToPlayerNameList(List<UUID> list) {
        if (list == null) {
            return null;
        }
        List<String> playerNameList = new ArrayList<>();
        for (UUID uuid : list) {
            try {
                playerNameList.add(Bukkit.getOfflinePlayer(uuid).getName());
            } catch (NullPointerException ex) {
                plugin.debug("UUID " + uuid + " is not a valid player", true);
            }
        }
        return playerNameList;
    }

    /**
     * Converts a UUID list to a String list using {@link java.util.UUID#toString() toString}
     * @param list the UUID list
     * @return the String list
     */
    public List<String> uuidListToStringList(List<UUID> list) {
        List<String> stringList = new ArrayList<>();
        for (UUID uuid : list) {
            stringList.add(uuid.toString());
        }
        return stringList;
    }
    
    /**
     * Gets a Game for the specified Mode
     * @param mode the mode
     * @return the game
     */
    public Game getGame(Mode mode) {
        for (Game g : cm.getFilesAndGames().values()) {
            if (g.getMode() == mode) {
                return g;
            }
        }
        return null;
    }

    /**
     * Checks if Winner Join Message is enabled for this Winner's game config
     *
     * @param player the winner
     * @return if it is enabled
     */
    public boolean isWinnerJoinMessageEnabled(Player player) {
        if (isKiller(player)) {
            for (Game game : cm.getFilesAndGames().values()) {
                Winners latestWinners = plugin.getDatabaseManager().getLatestWinners();
                Set<UUID> playerWinners = latestWinners.getPlayerWinners(game.getMode());
                if (playerWinners == null) {
                    continue;
                }
                if (playerWinners.contains(player.getUniqueId())) {
                    return game.isWinnerJoinMessage();
                }
            }
        }
        return false;
    }

    /**
     * Checks if Winner Quit Message is enabled for this Winner's game config
     *
     * @param player the winner
     * @return if it is enabled
     */
    public boolean isWinnerQuitMessageEnabled(Player player) {
        if (isWinner(player)) {
            for (Game game : cm.getFilesAndGames().values()) {
                Winners latestWinners = plugin.getDatabaseManager().getLatestWinners();
                Set<UUID> playerWinners = latestWinners.getPlayerWinners(game.getMode());
                if (playerWinners == null) {
                    continue;
                }
                if (playerWinners.contains(player.getUniqueId())) {
                    return game.isWinnerQuitMessage();
                }
            }
        }
        return false;
    }

    /**
     * Checks if the two players are allied
     * @param player1
     * @param player2
     * @return true if they are allied
     */
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

    /**
     * Increases this player's kills count
     * @param player the killer
     */
    public void increaseKillsCount(Player player) {
        int kills = gm.getPlayerKillsCount(player);
        kills++;
        gm.getKillsCount().replace(player, kills);
    }

    /**
     * Gets a Player's attacker or Killer from an {@link org.bukkit.entity.Entity}
     * @param entity the entity to investigate
     * @return the attacker/killer or null
     */
    public Player getPlayerAttackerOrKiller(Entity entity) {
        Player investigated = null;
        if (entity instanceof Player) {
            investigated = (Player) entity;
        }
        if (entity instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) entity).getShooter();
            if (shooter instanceof Player) {
                investigated = (Player) shooter;
            }
        }
        return investigated;
    }

    /**
     * Gets a String representation of a String List
     * Example: "RoinujNosde, Lannister & Killer07"
     * @param list the String list
     * @return the String representation
     */
    public String getStringFromStringList(List<String> list) {
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

    /**
     * Gets the Items Not Given map
     * @return the map
     */
    public Map<Player, HashMap<Integer, ItemStack>> getItemsNotGiven() {
        return itemsNotGiven;
    }

    /**
     * Gets the players with items to receive
     * @return the players with items to receive
     */
    public Set<Player> getPlayersWithItemsToReceive() {
        if (itemsNotGiven.isEmpty()) {
            return new TreeSet<>();
        }
        return itemsNotGiven.keySet();
    }

    /**
     * Gets the items that the Player has not received
     * @param player the player
     * @return the items not received
     */
    public ItemStack[] getItemsNotGivenToPlayer(Player player) {
        if (itemsNotGiven.containsKey(player)) {
            List<ItemStack> items = (List<ItemStack>) ((HashMap<Integer, ItemStack>) itemsNotGiven.values()).values();
            return items.toArray(new ItemStack[0]);
        }
        return new ItemStack[0];
    }

    /**
     * Gives the items to the players according to the Game
     * @param game the game
     * @param players the player
     * @param items the items
     */
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

    /**
     * Runs the commands on the players, specifying the "some number" placeholder and if should be divided between the players
     * @param players the players
     * @param commands the commands
     * @param someNumber a amount
     * @param divide should it be divided?
     */
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

    /**
     * Gives the money amount to the players, sharing the amount or not
     * @param players the players
     * @param amount the amount
     * @param share should it be shared between them?
     */
    public void giveMoneyToPlayers(List<Player> players, double amount, boolean share) {
        if (plugin.getEconomy() == null) {
            return;
        }
        if (share) {
            //Share prize
            amount = (amount / players.size());
            for (Player player : players) {
                EconomyResponse r = plugin.getEconomy().depositPlayer(player, amount);
                if (!r.transactionSuccess()) {
                    System.out.println("[TitansBattle] Error: " + r.errorMessage);
                }
            }
            return;
        }
        for (Player player : players) {
            EconomyResponse r = plugin.getEconomy().depositPlayer(player, amount);
            if (!r.transactionSuccess()) {
                System.out.println("[TitansBattle] Error: " + r.errorMessage);
            }
        }
    }

    /**
     * Removes the first argument of the String Array
     * @param array the String array
     * @return the modified String array
     */
    public String[] removeFirstArg(String[] array) {
        int length = array.length;
        if (length <= 1) {
            return new String[0];
        }
        int newLength = length - 1;
        String[] newArgs = new String[newLength];
        for (int i = 0; i < newLength;) {
            newArgs[i] = array[i + 1];
            i++;
        }
        return newArgs;
    }

    /**
     * Gets the current time in seconds
     * @return the current time in seconds
     */
    public int getCurrentTimeInSeconds() {
        Calendar today = Calendar.getInstance();
        int hour = today.get(Calendar.HOUR_OF_DAY);
        int minute = today.get(Calendar.MINUTE);

        return ((hour * 60 * 60) + (minute * 60));
    }

    /**
     * Gets the next Scheduler of the day, or null, if there is not one
     * @return the next Scheduler, or null
     */
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

    /**
     * Gets a GroupWrapper for this OfflinePlayer, or null if he is not a member
     *
     * @param player the player
     * @return the GroupWrapper, or null
     */
    public GroupWrapper getGroupWrapper(OfflinePlayer player) {
        if (plugin.isFactions()) {
            MPlayer mp = MPlayer.get(player);
            if (mp == null) {
                return null;
            }
            return new GroupWrapper(mp.getFaction());
        }
        if (plugin.isSimpleClans()) {
            ClanPlayer cp = plugin.getClanManager().getClanPlayer(player.getUniqueId());
            if (cp == null) {
                return null;
            }
            return new GroupWrapper(cp.getClan());
        }
        return null;
    }

    /**
     * Checks if this player is member of any Group
     *
     * @param player the player
     * @return true if he is a member
     */
    public boolean isMemberOfAGroup(Player player) {
        if (plugin.isFactions()) {
            if (MPlayer.get(player).hasFaction()) {
                return true;
            }
        }
        if (plugin.isSimpleClans()) {
            if (plugin.getClanManager().getClanPlayer(player) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the player has items in his inventory
     *
     * @param player the player
     * @return true if he has items
     */
    public boolean inventoryHasItems(@NotNull Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().equals(Material.AIR)) {
                return true;
            }
        }
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && !item.getType().equals(Material.AIR)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fills this count map to avoid NullPointerException
     *
     * @param map the map
     */
    public void fillEmptyCountMaps(Map<Mode, Integer> map) {
        map.putIfAbsent(Mode.GROUPS_FUN, 0);
        map.putIfAbsent(Mode.GROUPS_REAL, 0);
        map.putIfAbsent(Mode.FREEFORALL_FUN, 0);
        map.putIfAbsent(Mode.FREEFORALL_REAL, 0);
    }

    /**
     * Gets a String representation of this Group set Example: "kol, ks97 & dst"
     *
     * @param groups the groups
     * @return the String representation
     */
    public String getStringFromGroupSet(Set<Group> groups) {
        if (groups != null && !groups.isEmpty()) {
            if (plugin.isSimpleClans()) {
                StringBuilder sb = new StringBuilder();
                List<Clan> clans = groups.stream().map(group -> (Clan) group.getWrapper().getBase()).collect(Collectors.toList());
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
            if (plugin.isFactions()) {
                StringBuilder sb = new StringBuilder();
                List<Faction> factions = groups.stream().map(group -> (Faction) group.getWrapper().getBase()).collect(Collectors.toList());
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
        }
        return "";
    }

    /**
     * Increases the victories of this Warrior
     *
     * @param warrior the warrior
     */
    public void increaseVictories(Warrior warrior) {
        Mode mode = gm.getCurrentGame().getMode();
        warrior.setVictories(mode, warrior.getVictories(mode) + 1);
    }

    /**
     * Gets a String filled with the specified quantity of spaces
     *
     * @param spaces the spaces
     * @return the spaces filled String
     */
    public String getSpaces(int spaces) {
        String s = "";
        for (int i = 0; i < spaces; i++) {
            s = s + " ";
        }
        return s;
    }

    /**
     * Gets the length of this number
     *
     * @param integer the number
     * @return the length
     */
    public int getLength(int integer) {
        return String.valueOf(integer).length();
    }

    /**
     * Checks if the dates are equals, ignoring the time
     *
     * @param date1 date 1
     * @param date2 date 2
     * @return true if they are equals
     */
    public boolean equalDates(Date date1, Date date2) {
        Calendar d1 = Calendar.getInstance();
        d1.setTime(date1);
        Calendar d2 = Calendar.getInstance();
        d2.setTime(date2);

        if (d1.get(Calendar.YEAR) != d2.get(Calendar.YEAR)) {
            return false;
        }
        if (d1.get(Calendar.MONTH) != d2.get(Calendar.MONTH)) {
            return false;
        }
        if (d1.get(Calendar.DAY_OF_MONTH) != d2.get(Calendar.DAY_OF_MONTH)) {
            return false;
        }
        return true;
    }
}
