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
package me.roinujnosde.titansbattle.types;

import java.util.Map;
import java.util.UUID;
import me.roinujnosde.titansbattle.Helper;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.Game.Mode;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author RoinujNosde
 */
public class Warrior {

    private OfflinePlayer player;
    private Group group;
    private Map<Game.Mode, Integer> kills;
    private Map<Game.Mode, Integer> deaths;
    private Map<Game.Mode, Integer> victories;

    public Warrior(OfflinePlayer player, Group group, Map<Game.Mode, Integer> kills, Map<Game.Mode, Integer> deaths, Map<Game.Mode, Integer> victories) {
        if (player == null || group == null || kills == null || deaths == null || victories == null) {
            throw new IllegalArgumentException("None of the parametres may be null");
        }

        Helper helper = TitansBattle.getInstance().getHelper();

        helper.fillEmptyCountMaps(victories);
        helper.fillEmptyCountMaps(kills);
        helper.fillEmptyCountMaps(deaths);
        
        this.player = player;
        this.group = group;
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

    public OfflinePlayer toPlayer() {
        return player;
    }

    public Group getGroup() {
        return group;
    }

    public int getKills(Mode mode) {
        return kills.get(mode);
    }

    public int getDeaths(Mode mode) {
        return deaths.get(mode);
    }

    public int getVictories(Mode mode) {
        return victories.get(mode);
    }
    
    public void setKills(Mode mode, int newKills) {
        kills.put(mode, newKills);
    }
    
    public void setDeaths(Mode mode, int newDeaths) {
        deaths.put(mode, newDeaths);
    }

    public void setVictories(Mode mode, int newVictories) {
        victories.put(mode, newVictories);
    }
}
