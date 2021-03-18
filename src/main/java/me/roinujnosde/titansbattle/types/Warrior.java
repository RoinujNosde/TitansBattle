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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.roinujnosde.titansbattle.types;

import me.roinujnosde.titansbattle.managers.GroupManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 *
 * @author RoinujNosde
 */
public class Warrior {

    private final Supplier<GroupManager> groupManager;
    private final OfflinePlayer player;
    private final Map<String, Integer> kills;
    private final Map<String, Integer> deaths;
    private final Map<String, Integer> victories;

    public Warrior(@NotNull OfflinePlayer player,
                   @NotNull Supplier<GroupManager> groupManager,
                   @NotNull Map<String, Integer> kills,
                   @NotNull Map<String, Integer> deaths,
                   @NotNull Map<String, Integer> victories) {
        this.groupManager = groupManager;
        this.player = player;
        this.kills = kills;
        this.deaths = deaths;
        this.victories = victories;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Warrior) {
            final UUID uniqueId = toPlayer().getUniqueId();
            final UUID uniqueId2 = ((Warrior) o).toPlayer().getUniqueId();
            return uniqueId.equals(uniqueId2);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return toPlayer().getUniqueId().hashCode();
    }

    @NotNull
    public String getName() {
        return player.getName() != null ? player.getName() : "null";
    }

    public void sendMessage(@Nullable String message) {
        if (message == null) return;
        Player player = toOnlinePlayer();
        if (player != null) {
            player.sendMessage(message);
        }
    }

    @Nullable
    public Player toOnlinePlayer() {
        return player.getPlayer();
    }

    @NotNull
    public OfflinePlayer toPlayer() {
        return player;
    }

    @NotNull
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Nullable
    public Group getGroup() {
        GroupManager groupManager = this.groupManager.get();
        if (groupManager == null) {
            return null;
        }
        return groupManager.getGroup(player.getUniqueId());
    }

    public int getKills(@NotNull String game) {
        return kills.getOrDefault(game, 0);
    }

    public void increaseKills(@NotNull String game) {
        setKills(game, getKills(game) + 1);
        Group group = getGroup();
        if (group != null) {
            GroupData data = group.getData();
            data.setKills(game, data.getKills(game) + 1);
        }
    }

    public int getDeaths(@NotNull String game) {
        return deaths.getOrDefault(game, 0);
    }

    public void increaseDeaths(@NotNull String game) {
        setDeaths(game, getDeaths(game) + 1);
        Group group = getGroup();
        if (group != null) {
            GroupData data = group.getData();
            data.setDeaths(game, data.getDeaths(game) + 1);
        }
    }

    public int getVictories(@NotNull String game) {
        return victories.getOrDefault(game, 0);
    }
    
    public void setKills(@NotNull String game, int newKills) {
        kills.put(game, newKills);
    }
    
    public void setDeaths(@NotNull String game, int newDeaths) {
        deaths.put(game, newDeaths);
    }

    public void setVictories(@NotNull String game, int newVictories) {
        victories.put(game, newVictories);
    }

    public void increaseVictories(@NotNull String game) {
        setVictories(game, getVictories(game) + 1);
    }
}
