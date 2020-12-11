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

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author RoinujNosde
 */
public class Winners {

    private final Date date;
    private final Map<String, UUID> killer;
    private final Map<String, Set<UUID>> playerWinners;
    private final Map<String, String> winnerGroup;

    public Winners(@NotNull Date date,
                   @NotNull Map<String, UUID> killer,
                   @NotNull Map<String, Set<UUID>> playerWinners,
                   @NotNull Map<String, String> winnerGroup) {
        this.date = date;
        this.killer = killer;
        this.playerWinners = playerWinners;
        this.winnerGroup = winnerGroup;
    }

    public Date getDate() {
        return date;
    }

    public UUID getKiller(String game) {
        return killer.get(game);
    }

    public Set<UUID> getPlayerWinners(String game) {
        return playerWinners.get(game);
    }

    public String getWinnerGroup(String game) {
        return winnerGroup.get(game);
    }

    public void setKiller(String game, UUID uuid) {
        killer.put(game, uuid);
    }

    public void setWinnerGroup(String game, String group) {
        winnerGroup.put(game, group);
    }

    public void setWinners(String game, Set<UUID> winners) {
        playerWinners.put(game, winners);
    }
}
