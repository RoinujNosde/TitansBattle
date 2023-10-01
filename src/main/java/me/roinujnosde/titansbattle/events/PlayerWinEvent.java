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

import me.roinujnosde.titansbattle.BaseGame;
import me.roinujnosde.titansbattle.types.Warrior;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author RoinujNosde
 */
public class PlayerWinEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final BaseGame game;
    private final List<Warrior> players;

    public PlayerWinEvent(@NotNull BaseGame game, @NotNull List<Warrior> players) {
        this.game = game;
        this.players = players;
    }

    public @NotNull BaseGame getGame() {
        return game;
    }

    /**
     * Returns the winner of the event (or the first of the list, if there are more than one winner)
     * @return the winner of the event
     */
    public Warrior getPlayer() {
        return players.get(0);
    }

    /**
     * Returns an Unmodifiable List of the Winners
     * @return an Unmodifiable List of the Winners
     */
    public List<Warrior> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
