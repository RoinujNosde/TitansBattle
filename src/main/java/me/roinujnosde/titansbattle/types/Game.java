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
package me.roinujnosde.titansbattle.types;

import java.util.List;
import java.util.UUID;
import me.roinujnosde.titansbattle.Helper;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 *
 * @author RoinujNosde
 */
public class Game {    
    Mode mode;
    Prizes prizes;
    int minimumPlayers;
    int minimumGroups;
    Location exit;
    Location arena;
    Location watchroom;
    Location lobby;
    int announcementStartingTimes;
    long announcementStartingInterval;
    long announcementGameInfoInterval;
    boolean deleteGroups;
    long expirationTime;
    String joinOrQuitMessagePriority;
    boolean killerJoinMessage;
    boolean winnerJoinMessage;
    boolean killerQuitMessage;
    boolean winnerQuitMessage;
    String killerPrefix;
    String winnerPrefix;
    long preparationTime;
    List<ItemStack> kit;

    public Game(Mode mode, Prizes prizes, int minimumPlayers, int minimumGroups,
            Location exit, Location arena, Location watchroom, Location lobby,
            int announcementStartingTimes, long announcementStartingInterval,
            long announcementGameInfoInterval, boolean deleteGroups,
            long expirationTime, String joinOrQuitMessagePriority,
            boolean killerJoinMessage, boolean winnerJoinMessage,
            boolean killerQuitMessage, boolean winnerQuitMessage,
            String killerPrefix, String winnerPrefix, long preparationTime, @Nullable List<ItemStack> kit) {
        this.mode = mode;
        this.prizes = prizes;
        this.minimumPlayers = minimumPlayers;
        this.minimumGroups = minimumGroups;
        this.exit = exit;
        this.arena = arena;
        this.watchroom = watchroom;
        this.lobby = lobby;
        this.announcementStartingTimes = announcementStartingTimes;
        this.announcementStartingInterval = announcementStartingInterval;
        this.announcementGameInfoInterval = announcementGameInfoInterval;
        this.deleteGroups = deleteGroups;
        this.expirationTime = expirationTime;
        this.joinOrQuitMessagePriority = joinOrQuitMessagePriority;
        this.killerJoinMessage = killerJoinMessage;
        this.winnerJoinMessage = winnerJoinMessage;
        this.killerQuitMessage = killerQuitMessage;
        this.winnerQuitMessage = winnerQuitMessage;
        this.killerPrefix = killerPrefix;
        this.winnerPrefix = winnerPrefix;
        this.preparationTime = preparationTime;
        this.kit = kit;
    }

    public long getPreparationTime() {
        return preparationTime;
    }

    public void setPreparationTime(long preparationTime) {
        this.preparationTime = preparationTime;
    }

    @Nullable
    public List<ItemStack> getKit() {
        return kit;
    }    

    public void setKit(List<ItemStack> kit) {
        this.kit = Helper.removeNullItems(kit);
    }

    public Location getExit() {
        return exit;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public String getKillerPrefix() {
        return killerPrefix;
    }

    public Location getLobby() {
        return lobby;
    }

    public Location getArena() {
        return arena;
    }

    public long getAnnouncementGameInfoInterval() {
        return announcementGameInfoInterval;
    }

    public int getAnnouncementStartingTimes() {
        return announcementStartingTimes;
    }

    public long getAnnouncementStartingInterval() {
        return announcementStartingInterval;
    }

    public String getJoinOrQuitMessagePriority() {
        return joinOrQuitMessagePriority;
    }

    public void setAnnouncementGameInfoInterval(long announcementGameInfoInterval) {
        this.announcementGameInfoInterval = announcementGameInfoInterval;
    }

    public void setAnnouncementStartingInterval(long announcementStartingInterval) {
        this.announcementStartingInterval = announcementStartingInterval;
    }

    public void setAnnouncementStartingTimes(int announcementStartingTimes) {
        this.announcementStartingTimes = announcementStartingTimes;
    }

    public void setArena(Location arena) {
        this.arena = arena;
    }

    public void setDeleteGroups(boolean deleteGroups) {
        this.deleteGroups = deleteGroups;
    }

    public void setKillerPrefix(String killerPrefix) {
        this.killerPrefix = killerPrefix;
    }

    public void setKillerQuitMessage(boolean killerQuitMessage) {
        this.killerQuitMessage = killerQuitMessage;
    }

    public void setJoinOrQuitMessagePriority(String joinOrQuitMessagePriority) {
        this.joinOrQuitMessagePriority = joinOrQuitMessagePriority;
    }

    public void setKillerJoinMessage(boolean killerJoinMessage) {
        this.killerJoinMessage = killerJoinMessage;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public void setWatchroom(Location watchroom) {
        this.watchroom = watchroom;
    }

    public void setPrizes(Prizes prizes) {
        this.prizes = prizes;
    }

    public void setWinnerJoinMessage(boolean winnerJoinMessage) {
        this.winnerJoinMessage = winnerJoinMessage;
    }

    public void setLobby(Location lobby) {
        this.lobby = lobby;
    }

    public void setExit(Location exit) {
        this.exit = exit;
    }

    public void setWinnerQuitMessage(boolean winnerQuitMessage) {
        this.winnerQuitMessage = winnerQuitMessage;
    }

    public void setMinimumPlayers(int minimumPlayers) {
        this.minimumPlayers = minimumPlayers;
    }

    public void setMinimumGroups(int minimumGroups) {
        this.minimumGroups = minimumGroups;
    }

    public void setWinnerPrefix(String winnerPrefix) {
        this.winnerPrefix = winnerPrefix;
    }

    public String getWinnerPrefix() {
        return winnerPrefix;
    }

    public Location getWatchroom() {
        return watchroom;
    }

    public boolean isDeleteGroups() {
        return deleteGroups;
    }

    public boolean isWinnerQuitMessage() {
        return winnerQuitMessage;
    }

    public boolean isKillerQuitMessage() {
        return killerQuitMessage;
    }

    public boolean isWinnerJoinMessage() {
        return winnerJoinMessage;
    }

    public boolean isKillerJoinMessage() {
        return killerJoinMessage;
    }

    public boolean isKillerPriority() {
        if (joinOrQuitMessagePriority.equalsIgnoreCase("winner")) {
            return false;
        }
        return true;
    }

    public Mode getMode() {
        return mode;
    }

    public int getMinimumGroups() {
        return minimumGroups;
    }

    public int getMinimumPlayers() {
        return minimumPlayers;
    }

    public Prizes getPrizes() {
        return prizes;
    }

    public enum Mode {
        FREEFORALL_FUN, GROUPS_FUN, FREEFORALL_REAL, GROUPS_REAL
    }
}
