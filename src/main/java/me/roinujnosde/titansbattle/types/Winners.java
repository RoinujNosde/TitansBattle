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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import me.roinujnosde.titansbattle.types.Game.Mode;

/**
 *
 * @author RoinujNosde
 */
public class Winners {

    private Date date;
    private Map<Mode, UUID> killer;
    private Map<Mode, Set<UUID>> playerWinners;
    private Map<Mode, Group> winnerGroup;

    public Winners(Date date, Map<Mode, UUID> killer, Map<Mode, Set<UUID>> playerWinners, Map<Mode, Group> winnerGroup) {
        if (date == null || killer == null || playerWinners == null || winnerGroup == null) {
            throw new IllegalArgumentException("no argument can be null");
        }
                
        this.date = date;
        this.killer = killer;
        this.playerWinners = playerWinners;
        this.winnerGroup = winnerGroup;
    }

    public Date getDate() {
        return date;
    }

    public UUID getKiller(Mode mode) {
        return killer.get(mode);
    }

    public Set<UUID> getPlayerWinners(Mode mode) {
        return playerWinners.get(mode);
    }

    public Group getWinnerGroup(Mode mode) {
        return winnerGroup.get(mode);
    }

    public void setKiller(Mode mode, UUID uuid) {
        killer.put(mode, uuid);
    }

    public void setWinnerGroup(Mode mode, Group group) {
        winnerGroup.put(mode, group);
    }

    public void setWinners(Mode mode, Set<UUID> winners) {
        playerWinners.put(mode, winners);
    }
}
