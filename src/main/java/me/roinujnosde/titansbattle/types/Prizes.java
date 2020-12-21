package me.roinujnosde.titansbattle.types;

import me.roinujnosde.titansbattle.utils.ConfigUtils;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.utils.Path;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings({"FieldCanBeLocal", "unused", "FieldMayBeFinal"})
public class Prizes implements ConfigurationSerializable {
    private static final HashMap<Player, HashMap<Integer, ItemStack>> ITEMS_NOT_GIVEN = new HashMap<>();

    private Integer itemsGiveInterval = 30;
    private Boolean treatLeadersAsMembers = false;
    @Path("leader.items.enabled")
    private Boolean leaderItemsEnabled = false;
    @Path("leader.items.item_list")
    private @Nullable List<ItemStack> leaderItems;
    @Path("leader.commands.enabled")
    private Boolean leaderCommandsEnabled = false;
    @Path("leader.commands.command_list")
    private @Nullable List<String> leaderCommands = Arrays.asList("give %player% diamond_sword %some_number%",
            "eco give %player% %some_number%");
    @Path("leader.commands.some_number.value")
    private Double leaderCommandsSomeNumber = 100D;
    @Path("leader.commands.some_number.divided")
    private Boolean leaderCommandsSomeNumberDivide = false;
    @Path("leader.money.enabled")
    private Boolean leaderMoneyEnabled = false;
    @Path("leader.money.divided")
    private Boolean leaderMoneyDivide = false;
    @Path("leader.money.amount")
    private Double leaderMoneyAmount = 10000D;
    @Path("member.items.enabled")
    private Boolean memberItemsEnabled = false;
    @Path("member.items.item_list")
    private @Nullable List<ItemStack> memberItems;
    @Path("member.commands.enabled")
    private Boolean memberCommandsEnabled = false;
    @Path("member.commands.command_list")
    private @Nullable List<String> memberCommands = Arrays.asList("give %player% diamond_sword %some_number%",
            "eco give %player% %some_number%");
    @Path("member.commands.some_number.value")
    private Double memberCommandsSomeNumber = 100D;
    @Path("member.commands.some_number.divided")
    private Boolean memberCommandsSomeNumberDivide = false;
    @Path("member.money.enabled")
    private Boolean memberMoneyEnabled = false;
    @Path("member.money.divided")
    private Boolean memberMoneyDivide = false;
    @Path("member.money.amount")
    private Double memberMoneyAmount = 10000D;
    @Path("killer.items.enabled")
    private Boolean killerItemsEnabled = false;
    @Path("killer.items.item_list")
    private @Nullable List<ItemStack> killerItems;
    @Path("killer.commands.enabled")
    private Boolean killerCommandsEnabled = false;
    @Path("killer.commands.command_list")
    private @Nullable List<String> killerCommands = Arrays.asList("give %player% diamond_sword %some_number%",
            "eco give %player% %some_number%");
    @Path("killer.commands.some_number.value")
    private Double killerCommandsSomeNumber = 100D;
    @Path("killer.commands.some_number.divided")
    private Boolean killerCommandsSomeNumberDivide = false;
    @Path("killer.money.enabled")
    private Boolean killerMoneyEnabled = false;
    @Path("killer.money.divided")
    private Boolean killerMoneyDivide = false;
    @Path("killer.money.amount")
    private Double killerMoneyAmount = 10000D;

    public Prizes() {
    }

    public Prizes(@NotNull Map<String, Object> data) {
        ConfigUtils.deserialize(this, data);
    }

    @Override
    public Map<String, Object> serialize() {
        return ConfigUtils.serialize(this);
    }

    public void setKillerItems(@Nullable List<ItemStack> killerItems) {
        this.killerItems = killerItems;
    }

    public void setMemberItems(@Nullable List<ItemStack> memberItems) {
        this.memberItems = memberItems;
    }

    public void setLeaderItems(@Nullable List<ItemStack> leaderItems) {
        this.leaderItems = leaderItems;
    }

    public void give(@NotNull TitansBattle plugin, @Nullable List<Player> leaders, @NotNull List<Player> members,
                     @Nullable Player killer) {
        if (treatLeadersAsMembers && leaders != null) {
            members.addAll(leaders);
            leaders.clear();
        }
        if (leaderItemsEnabled && leaderItems != null && leaders != null) {
            giveItemsToPlayers(plugin, leaders, leaderItems);
        }
        if (memberItemsEnabled && memberItems != null) {
            giveItemsToPlayers(plugin, members, memberItems);
        }
        if (killer != null) {
            List<Player> killerList = Collections.singletonList(killer);
            if (killerItemsEnabled && killerItems != null) {
                giveItemsToPlayers(plugin, killerList, killerItems);
            }
            if (killerMoneyEnabled) {
                giveMoneyToPlayers(plugin, killerList, killerMoneyAmount, killerMoneyDivide);
            }
            if (killerCommandsEnabled) {
                runCommandsOnPlayers(killerList, killerCommands, killerCommandsSomeNumber, killerCommandsSomeNumberDivide);
            }
        }
        if (leaderMoneyEnabled && leaders != null) {
            giveMoneyToPlayers(plugin, leaders, leaderMoneyAmount, leaderMoneyDivide);
        }
        if (memberMoneyEnabled) {
            giveMoneyToPlayers(plugin, members, memberMoneyAmount, memberMoneyDivide);
        }
        if (leaderCommandsEnabled) {
            runCommandsOnPlayers(leaders, leaderCommands, leaderCommandsSomeNumber,
                    leaderCommandsSomeNumberDivide);
        }
        if (memberCommandsEnabled) {
            runCommandsOnPlayers(members, memberCommands, memberCommandsSomeNumber,
                    memberCommandsSomeNumberDivide);
        }
    }

    private void giveItemsToPlayers(@NotNull TitansBattle plugin,
                                    @NotNull List<Player> players,
                                    @NotNull List<ItemStack> items) {
        for (Player player : players) {
            Inventory inventory = player.getInventory();
            HashMap<Integer, ItemStack> remainingItems = inventory.addItem(items.toArray(new ItemStack[0]));
            if (!remainingItems.isEmpty()) {
                ITEMS_NOT_GIVEN.put(player, remainingItems);
                plugin.getTaskManager().startGiveItemsTask(itemsGiveInterval);
            }
        }
    }

    private void runCommandsOnPlayers(List<Player> players, List<String> commands, double someNumber, boolean divide) {
        if (divide) {
            someNumber = someNumber / players.size();
        }
        for (Player player : players) {
            for (String command : commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        command.replaceAll("%player%", player.getName()).replaceAll("%some_number%",
                                Double.toString(someNumber)));
            }
        }
    }

    private void giveMoneyToPlayers(@NotNull TitansBattle plugin, @NotNull List<Player> players, double amount,
                                    boolean share) {
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

    public static Map<Player, HashMap<Integer, ItemStack>> getPlayersWithItemsToReceive() {
        ITEMS_NOT_GIVEN.keySet().removeIf(p -> !p.isOnline());
        return ITEMS_NOT_GIVEN;
    }
}
