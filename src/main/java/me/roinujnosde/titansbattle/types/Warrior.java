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

import me.roinujnosde.titansbattle.Helper;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.Game.Mode;
import me.roinujnosde.titansbattle.utils.Groups;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

/**
 *
 * @author RoinujNosde
 */
public class Warrior {

    private final OfflinePlayer player;
    private final Map<Game.Mode, Integer> kills;
    private final Map<Game.Mode, Integer> deaths;
    private final Map<Game.Mode, Integer> victories;

    @Deprecated
    public Warrior(@NotNull OfflinePlayer player, @Nullable Group group, @NotNull Map<Game.Mode, Integer> kills,
                   @NotNull Map<Game.Mode, Integer> deaths, @NotNull Map<Game.Mode, Integer> victories) {
        Helper helper = TitansBattle.getInstance().getHelper();

        helper.fillEmptyCountMaps(victories);
        helper.fillEmptyCountMaps(kills);
        helper.fillEmptyCountMaps(deaths);
        
        this.player = player;
        this.kills = kills;
        this.deaths = deaths;
        this.victories = victories;
    }

    public Warrior(@NotNull OfflinePlayer player, @NotNull Map<Game.Mode, Integer> kills,
                   @NotNull Map<Game.Mode, Integer> deaths, @NotNull Map<Game.Mode, Integer> victories) {
        Helper helper = TitansBattle.getInstance().getHelper();

        helper.fillEmptyCountMaps(victories);
        helper.fillEmptyCountMaps(kills);
        helper.fillEmptyCountMaps(deaths);

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
    public OfflinePlayer toPlayer() {
        return player;
    }

    @Nullable
    public Group getGroup() {
        return Groups.getGroup(player.getUniqueId());
    }

    public int getKills(@NotNull Mode mode) {
        return kills.get(mode);
    }

    public int getDeaths(@NotNull Mode mode) {
        return deaths.get(mode);
    }

    public int getVictories(@NotNull Mode mode) {
        return victories.get(mode);
    }
    
    public void setKills(@NotNull Mode mode, int newKills) {
        kills.put(mode, newKills);
    }
    
    public void setDeaths(@NotNull Mode mode, int newDeaths) {
        deaths.put(mode, newDeaths);
    }

    public void setVictories(@NotNull Mode mode, int newVictories) {
        victories.put(mode, newVictories);
    }
}
