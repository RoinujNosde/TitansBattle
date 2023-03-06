package me.roinujnosde.titansbattle.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.roinujnosde.titansbattle.BaseGameConfiguration;
import me.roinujnosde.titansbattle.BaseGameConfiguration.Prize;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.challenges.ArenaConfiguration;
import me.roinujnosde.titansbattle.dao.ConfigurationDao;
import me.roinujnosde.titansbattle.managers.ConfigManager;
import me.roinujnosde.titansbattle.serialization.ConfigUtils;
import me.roinujnosde.titansbattle.types.GameConfiguration;
import me.roinujnosde.titansbattle.types.Kit;
import me.roinujnosde.titansbattle.types.Prizes;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static me.roinujnosde.titansbattle.BaseGameConfiguration.Destination;

@CommandAlias("%titansbattle|tb")
public class ConfigCommands extends BaseCommand {

    @Dependency
    private TitansBattle plugin;
    @Dependency
    private ConfigurationDao configDao;
    @Dependency
    private ConfigManager configManager;

    @Subcommand("%setdestination|setdestination GENERAL_EXIT")
    @CommandPermission("titansbattle.setdestination")
    @Description("{@@command.description.setdestination.general}")
    public void setGeneralExit(Player player) {
        configManager.setGeneralExit(player.getLocation());
        configManager.save();
        player.sendMessage(plugin.getLang("destination_set", "GENERAL_EXIT"));
    }

    private void createFile(@NotNull CommandSender sender,
                            @NotNull String name,
                            @NotNull Class<? extends BaseGameConfiguration> clazz,
                            @NotNull String successMessageKey) {
        if (configDao.create(name, clazz)) {
            sender.sendMessage(plugin.getLang(successMessageKey, name));
        } else {
            sender.sendMessage(plugin.getLang("config-creation-error"));
        }
    }

    private void setKit(@NotNull Player sender, @NotNull BaseGameConfiguration config) {
        config.setKit(new Kit(sender.getInventory()));
        saveInventory(sender, config);
    }

    private void setPrize(Player sender, BaseGameConfiguration config, Prize prize, PrizeReceiver receiver) {
        Prizes prizes = config.getPrizes(prize);
        List<ItemStack> items = Arrays.stream(sender.getInventory().getContents()).filter(Objects::nonNull)
                .filter(item -> item.getType() != Material.AIR).collect(Collectors.toList());
        switch (receiver) {
            case LEADERS:
                prizes.setLeaderItems(items);
                break;
            case MEMBERS:
                prizes.setMemberItems(items);
                break;
        }
        saveInventory(sender, config);
    }

    private void editPrizes(CommandSender sender, BaseGameConfiguration config, Prize prize, String field, String value) {
        Prizes prizes = config.getPrizes(prize);
        if (ConfigUtils.setValue(prizes, field, value)) {
            sender.sendMessage(plugin.getLang("changed-field-value"));
            configDao.save(config);
        } else {
            sender.sendMessage(plugin.getLang("error-changing-field-value"));
        }
    }

    public void editConfig(CommandSender sender, BaseGameConfiguration config, String field, String value) {
        if (ConfigUtils.setValue(config, field, value)) {
            sender.sendMessage(plugin.getLang("changed-field-value"));
            configDao.save(config);
        } else {
            sender.sendMessage(plugin.getLang("error-changing-field-value"));
        }
    }

    private void saveInventory(@NotNull CommandSender sender, @NotNull BaseGameConfiguration config) {
        if (configDao.save(config)) {
            sender.sendMessage(plugin.getLang("inventory-set"));
        } else {
            sender.sendMessage(plugin.getLang("error-saving-game-config"));
        }
    }

    private void setArenaEntrance(Player sender, BaseGameConfiguration config, int index) {
        config.setArenaEntrance(index, sender.getLocation());
        configDao.save(config);
        sender.sendMessage(plugin.getLang("destination_set", "ARENA_ENTRANCE"));
    }

    private void setDestination(Player player, BaseGameConfiguration config, Destination destination) {
        Location loc = player.getLocation();
        switch (destination) {
            case EXIT:
                config.setExit(loc);
                break;
            case LOBBY:
                config.setLobby(loc);
                break;
            case WATCHROOM:
                config.setWatchroom(loc);
                break;
            case BORDER_CENTER:
                config.setBorderCenter(loc);
                break;
        }
        configDao.save(config);
        player.sendMessage(plugin.getLang("destination_set", destination));
    }

    @Subcommand("%game|game")
    public class GameCommand extends BaseCommand {

        @Subcommand("%create|create")
        @CommandPermission("titansbattle.create")
        @Description("{@@command.description.create}")
        public void create(CommandSender sender, String game) {
            createFile(sender, game, GameConfiguration.class, "game-created");
        }

        @Subcommand("%setkit|setkit")
        @CommandPermission("titansbattle.setinventory")
        @CommandCompletion("@games")
        @Description("{@@command.description.setkit}")
        public void setKit(Player sender, @Values("@games") GameConfiguration game) {
            ConfigCommands.this.setKit(sender, game);
        }

        @Subcommand("%setprize|setprize")
        @CommandPermission("titansbattle.setinventory")
        @CommandCompletion("@games")
        @Description("{@@command.description.setprize}")
        public void setPrize(Player sender,
                             @Values("@games") GameConfiguration game,
                             Prize prize,
                             PrizeReceiver receiver) {
            ConfigCommands.this.setPrize(sender, game, prize, receiver);
        }

        @Subcommand("%edit|edit %prize|prize")
        @CommandPermission("titansbattle.edit")
        @CommandCompletion("@games @prizes @config_fields:class=prizes @empty")
        @Description("{@@command.description.edit.prize}")
        public void editPrizes(CommandSender sender,
                               @Values("@games") GameConfiguration game,
                               Prize prize,
                               @Values("@config_fields:class=prizes") String field,
                               String value) {
            ConfigCommands.this.editPrizes(sender, game, prize, field, value);
        }

        @Subcommand("%edit|edit %config|config")
        @CommandPermission("titansbattle.edit")
        @CommandCompletion("@games @config_fields:class=game")
        @Description("{@@command.description.edit.game}")
        public void editConfig(CommandSender sender,
                               @Values("@games") GameConfiguration game,
                               @Values("@config_fields:class=game") String field,
                               String value) {
            ConfigCommands.this.editConfig(sender, game, field, value);
        }


        @Subcommand("%setdestination|setdestination")
        @CommandPermission("titansbattle.setdestination")
        @CommandCompletion("@games @destinations|ARENA_ENTRANCE")
        @Description("{@@command.description.setdestination.game}")
        public void setDestination(Player player, @Values("@games") GameConfiguration game,  @Values("@destinations") Destination destination) {
            ConfigCommands.this.setDestination(player, game, destination);
        }

        @Subcommand("%setdestination|setdestination")
        @CommandPermission("titansbattle.setdestination")
        @CommandCompletion("@games @destinations|ARENA_ENTRANCE @range:1-2")
        @Description("{@@command.description.setdestination.game}")
        public void setArenaEntrance(Player player,
                                     @Values("@games") GameConfiguration game,
                                     @Values("ARENA_ENTRANCE") String destination,
                                     @Values("@range:1-2") int index) {
            ConfigCommands.this.setArenaEntrance(player, game, index);
        }

    }

    @Subcommand("%challenge|challenge")
    public class ArenaCommand extends BaseCommand {

        @Subcommand("%create|create")
        @CommandPermission("titansbattle.create")
        @Description("{@@command.description.challenge.create}")
        public void create(CommandSender sender, String arena) {
            createFile(sender, arena, ArenaConfiguration.class, "arena-created");
        }

        @Subcommand("%setkit|setkit")
        @CommandPermission("titansbattle.setinventory")
        @CommandCompletion("@arenas")
        @Description("{@@command.description.challenge.setkit}")
        public void setKit(Player sender, @Values("@arenas") ArenaConfiguration arena) {
            ConfigCommands.this.setKit(sender, arena);
        }

        @Subcommand("%setprize|setprize")
        @CommandPermission("titansbattle.setinventory")
        @CommandCompletion("@arenas")
        @Description("{@@command.description.setprize}")
        public void setPrize(Player sender,
                             @Values("@arenas") ArenaConfiguration arena,
                             Prize prize,
                             PrizeReceiver receiver) {
            ConfigCommands.this.setPrize(sender, arena, prize, receiver);
        }

        @Subcommand("%edit|edit %prize|prize")
        @CommandPermission("titansbattle.edit")
        @CommandCompletion("@arenas @prizes @config_fields:class=prizes @empty")
        @Description("{@@command.description.edit.prize}")
        public void editPrizes(CommandSender sender,
                               @Values("@arenas") ArenaConfiguration arena,
                               Prize prize,
                               @Values("@config_fields:class=prizes") String field,
                               String value) {
            ConfigCommands.this.editPrizes(sender, arena, prize, field, value);
        }

        @Subcommand("%edit|edit %config|config")
        @CommandPermission("titansbattle.edit")
        @CommandCompletion("@arenas @config_fields:class=arena")
        @Description("{@@command.description.edit.game}")
        public void editConfig(CommandSender sender,
                               @Values("@arenas") ArenaConfiguration arena,
                               @Values("@config_fields:class=arena") String field,
                               String value) {
            ConfigCommands.this.editConfig(sender, arena, field, value);
        }

        @Subcommand("%setdestination|setdestination")
        @CommandPermission("titansbattle.setdestination")
        @CommandCompletion("@arenas @destinations|ARENA_ENTRANCE")
        @Description("{@@command.description.challenge.setdestination}")
        public void setDestination(Player player,
                                   @Values("@arenas") ArenaConfiguration arena,
                                   @Values("@destinations") Destination destination) {
            ConfigCommands.this.setDestination(player, arena, destination);
        }

        @Subcommand("%setdestination|setdestination")
        @CommandPermission("titansbattle.setdestination")
        @CommandCompletion("@arenas @destinations|ARENA_ENTRANCE @range:1-2")
        @Description("{@@command.description.challenge.setdestination}")
        public void setArenaEntrance(Player player,
                                     @Values("@arenas") ArenaConfiguration arena,
                                     @Values("ARENA_ENTRANCE") String destination,
                                     @Values("@range:1-2") int index) {
            ConfigCommands.this.setArenaEntrance(player, arena, index);
        }
    }

    public enum PrizeReceiver {
        LEADERS, MEMBERS
    }
}
