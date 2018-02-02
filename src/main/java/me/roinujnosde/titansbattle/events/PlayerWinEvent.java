/*
 * The MIT License
 *
 * Copyright 2017 Edson Passos - edsonpassosjr@outlook.com.
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
package me.roinujnosde.titansbattle.events;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author RoinujNosde
 */
public class PlayerWinEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private List<Player> players;

    private PlayerWinEvent() {
    }

    public PlayerWinEvent(List<Player> players) {
        if (players == null) {
            throw new IllegalArgumentException("Players must not be null.");
        }
        this.players = players;
    }

    public PlayerWinEvent(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player must not be null.");
        }
        players = new ArrayList<>();
        players.add(player);
    }

    public Player getPlayer() {
        return players.get(0);
    }

    public List<Player> getPlayers() {
        return players;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
