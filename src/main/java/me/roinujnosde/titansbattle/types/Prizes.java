package me.roinujnosde.titansbattle.types;

import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.serialization.ConfigUtils;
import me.roinujnosde.titansbattle.serialization.Path;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings({"FieldCanBeLocal", "unused", "FieldMayBeFinal"})
@SerializableAs("prize")
public class Prizes implements ConfigurationSerializable {
    private static final Map<Player, Collection<ItemStack>> ITEMS_NOT_GIVEN = new HashMap<>();

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

    public Prizes() {
    }

    public Prizes(@NotNull Map<String, Object> data) {
        ConfigUtils.deserialize(this, data);
    }

    @Override
    public Map<String, Object> serialize() {
        return ConfigUtils.serialize(this);
    }

    public void setMemberItems(@Nullable List<ItemStack> memberItems) {
        this.memberItems = memberItems;
    }

    public void setLeaderItems(@Nullable List<ItemStack> leaderItems) {
        this.leaderItems = leaderItems;
    }

    public void give(@NotNull TitansBattle plugin, @Nullable List<Player> leaders, @NotNull List<Player> members) {
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
        if (leaderCommandsEnabled) {
            runCommandsOnPlayers(plugin, leaders, leaderCommands, leaderCommandsSomeNumber, leaderCommandsSomeNumberDivide);
        }
        if (memberCommandsEnabled) {
            runCommandsOnPlayers(plugin, members, memberCommands, memberCommandsSomeNumber, memberCommandsSomeNumberDivide);
        }
    }

    private void giveItemsToPlayers(@NotNull TitansBattle plugin,
                                    @NotNull List<Player> players,
                                    @NotNull List<ItemStack> items) {
        for (Player player : players) {
            Inventory inventory = player.getInventory();
            HashMap<Integer, ItemStack> remainingItems = inventory.addItem(items.toArray(new ItemStack[0]));
            if (!remainingItems.isEmpty()) {
                ITEMS_NOT_GIVEN.put(player, remainingItems.values());
                plugin.getTaskManager().startGiveItemsTask(itemsGiveInterval);
            }
        }
    }

    private void runCommandsOnPlayers(TitansBattle plugin, List<Player> players, List<String> commands,
                                      double someNumber, boolean divide) {
        if (divide) {
            someNumber = someNumber / players.size();
        }
        for (Player player : players) {
            for (String command : commands) {
                command = plugin.getPlaceholderHook().parse(player, command,
                        "%player%", player.getName(), "%some_number%", Double.toString(someNumber));
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        }
    }

    public static Map<Player, Collection<ItemStack>> getPlayersWithItemsToReceive() {
        ITEMS_NOT_GIVEN.keySet().removeIf(p -> !p.isOnline());
        return ITEMS_NOT_GIVEN;
    }
}
