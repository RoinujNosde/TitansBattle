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
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author RoinujNosde
 */
public class Group {

    private final GroupWrapper base;
    private final Map<Game.Mode, Integer> victories;
    private final Map<Mode, Integer> defeats;
    private final Map<Game.Mode, Integer> kills;
    private final Map<Game.Mode, Integer> deaths;

    public Group(@NotNull GroupWrapper base, @NotNull Map<Game.Mode, Integer> victories, @NotNull Map<Mode,
            @NotNull Integer> defeats, @NotNull Map<Game.Mode, Integer> kills, @NotNull Map<Game.Mode, Integer> deaths) {
        TitansBattle plugin = TitansBattle.getInstance();
        Helper helper = plugin.getHelper();
        
        helper.fillEmptyCountMaps(defeats);
        helper.fillEmptyCountMaps(victories);
        helper.fillEmptyCountMaps(kills);
        helper.fillEmptyCountMaps(deaths);

        this.base = base;
        this.victories = victories;
        this.kills = kills;
        this.deaths = deaths;
        this.defeats = defeats;

    }

    @NotNull
    public GroupWrapper getWrapper() {
        return base;
    }

    public int getVictories(Mode mode) {
        return victories.get(mode);
    }

    public int getKills(Mode mode) {
        return kills.get(mode);
    }

    public int getDeaths(Mode mode) {
        return deaths.get(mode);
    }
    
    public int getDefeats(Mode mode) {
        return defeats.get(mode);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Group) {
            return getWrapper().equals(((Group) o).getWrapper());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getWrapper().hashCode();
    }

    public void setVictories(Mode mode, int newVictories) {
        victories.put(mode, newVictories);
    }

    public void setKills(Mode mode, int newKills) {
        kills.put(mode, newKills);
    }

    public void setDeaths(Mode mode, int newDeaths) {
        deaths.put(mode, newDeaths);
    }
    
    public void setDefeats(Mode mode, int newDefeats) {
        defeats.put(mode, newDefeats);
    }
}
