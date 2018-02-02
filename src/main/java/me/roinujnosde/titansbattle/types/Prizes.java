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
import me.roinujnosde.titansbattle.Helper;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author RoinujNosde
 */
public class Prizes {

    long itemsGiveInterval;
    boolean treatLeadersAsMembers;
    private boolean leaderItemsEnabled;
    private List<ItemStack> leaderItems;
    private boolean leaderCommandsEnabled;
    private List<String> leaderCommands;
    private double leaderCommandsSomeNumber;
    private boolean leaderCommandsSomeNumberDivide;
    private boolean leaderMoneyEnabled;
    private boolean leaderMoneyDivide;
    private double leaderMoneyAmount;
    private boolean memberItemsEnabled;
    private List<ItemStack> memberItems;
    private boolean memberCommandsEnabled;
    private List<String> memberCommands;
    private double memberCommandsSomeNumber;
    private boolean memberCommandsSomeNumberDivide;
    private boolean memberMoneyEnabled;
    private boolean memberMoneyDivide;
    private double memberMoneyAmount;

    private Prizes() {
    }

    public Prizes(long itemsGiveInterval,
            boolean treatLeadersAsMembers,
            boolean leaderItemsEnabled,
            List<ItemStack> leaderItems,
            boolean leaderCommandsEnabled,
            List<String> leaderCommands,
            double leaderCommandsSomeNumber,
            boolean leaderCommandsSomeNumberDivide,
            boolean leaderMoneyEnabled,
            boolean leaderMoneyDivide,
            double leaderMoneyAmount,
            boolean memberItemsEnabled,
            List<ItemStack> memberItems,
            boolean memberCommandsEnabled,
            List<String> memberCommands,
            double memberCommandsSomeNumber,
            boolean memberCommandsSomeNumberDivide,
            boolean memberMoneyEnabled,
            boolean memberMoneyDivide,
            double memberMoneyAmount) {
        if (leaderItemsEnabled == true && leaderItems == null) {
            throw new IllegalArgumentException("leaderItems must not be null if enabled.");
        }
        if (leaderCommandsEnabled == true && leaderCommands == null) {
            throw new IllegalArgumentException("leaderCommands must not be null if enabled.");
        }
        if (memberItemsEnabled == true && memberItems == null) {
            throw new IllegalArgumentException("memberItems must not be null if enabled.");
        }
        if (memberCommandsEnabled == true && memberCommands == null) {
            throw new IllegalArgumentException("memberCommands must not be null if enabled.");
        }
        this.itemsGiveInterval = itemsGiveInterval;
        this.treatLeadersAsMembers = treatLeadersAsMembers;
        this.leaderItemsEnabled = leaderItemsEnabled;
        this.leaderItems = leaderItems;
        this.leaderCommandsEnabled = leaderCommandsEnabled;
        this.leaderCommands = leaderCommands;
        this.leaderCommandsSomeNumber = leaderCommandsSomeNumber;
        this.leaderCommandsSomeNumberDivide = leaderCommandsSomeNumberDivide;
        this.leaderMoneyEnabled = leaderMoneyEnabled;
        this.leaderMoneyDivide = leaderMoneyDivide;
        this.leaderMoneyAmount = leaderMoneyAmount;
        this.memberItemsEnabled = memberItemsEnabled;
        this.memberItems = memberItems;
        this.memberCommandsEnabled = memberCommandsEnabled;
        this.memberCommands = memberCommands;
        this.memberCommandsSomeNumber = memberCommandsSomeNumber;
        this.memberCommandsSomeNumberDivide = memberCommandsSomeNumberDivide;
        this.memberMoneyEnabled = memberMoneyEnabled;
        this.memberMoneyDivide = memberMoneyDivide;
        this.memberMoneyAmount = memberMoneyAmount;
    }

    public long getItemsGiveInterval() {
        return itemsGiveInterval;
    }

    public List<String> getLeaderCommands() {
        return leaderCommands;
    }

    public boolean isLeaderCommandsEnabled() {
        return leaderCommandsEnabled;
    }

    public boolean isLeaderCommandsSomeNumberDivide() {
        return leaderCommandsSomeNumberDivide;
    }

    public List<ItemStack> getMemberItems() {
        return memberItems;
    }

    public double getLeaderMoneyAmount() {
        return leaderMoneyAmount;
    }

    public double getLeaderCommandsSomeNumber() {
        return leaderCommandsSomeNumber;
    }

    public boolean isMemberCommandsSomeNumberDivide() {
        return memberCommandsSomeNumberDivide;
    }

    public boolean isLeaderMoneyDivide() {
        return leaderMoneyDivide;
    }

    public double getMemberMoneyAmount() {
        return memberMoneyAmount;
    }

    public double getMemberCommandsSomeNumber() {
        return memberCommandsSomeNumber;
    }

    public boolean isLeaderMoneyEnabled() {
        return leaderMoneyEnabled;
    }

    public boolean isLeaderItemsEnabled() {
        return leaderItemsEnabled;
    }

    public boolean isMemberCommandsEnabled() {
        return memberCommandsEnabled;
    }

    public boolean isMemberItemsEnabled() {
        return memberItemsEnabled;
    }

    public void setLeaderCommandsSomeNumberDivide(boolean leaderCommandsSomeNumberDivide) {
        this.leaderCommandsSomeNumberDivide = leaderCommandsSomeNumberDivide;
    }

    public void setLeaderCommands(List<String> leaderCommands) {
        this.leaderCommands = leaderCommands;
    }

    public void setLeaderItemsEnabled(boolean leaderItemsEnabled) {
        this.leaderItemsEnabled = leaderItemsEnabled;
    }

    public void setLeaderMoneyAmount(double leaderMoneyAmount) {
        this.leaderMoneyAmount = leaderMoneyAmount;
    }

    public void setLeaderCommandsEnabled(boolean leaderCommandsEnabled) {
        this.leaderCommandsEnabled = leaderCommandsEnabled;
    }

    public boolean isMemberMoneyEnabled() {
        return memberMoneyEnabled;
    }

    public boolean isTreatLeadersAsMembers() {
        return treatLeadersAsMembers;
    }

    public void setMemberCommandsEnabled(boolean memberCommandsEnabled) {
        this.memberCommandsEnabled = memberCommandsEnabled;
    }

    public void setMemberCommands(List<String> memberCommands) {
        this.memberCommands = memberCommands;
    }

    public void setLeaderMoneyEnabled(boolean leaderMoneyEnabled) {
        this.leaderMoneyEnabled = leaderMoneyEnabled;
    }

    public void setItemsGiveInterval(long itemsGiveInterval) {
        this.itemsGiveInterval = itemsGiveInterval;
    }

    public List<String> getMemberCommands() {
        return memberCommands;
    }

    public List<ItemStack> getLeaderItems() {
        return leaderItems;
    }

    public boolean isMemberMoneyDivide() {
        return memberMoneyDivide;
    }

    public void setLeaderCommandsSomeNumber(double leaderCommandsSomeNumber) {
        this.leaderCommandsSomeNumber = leaderCommandsSomeNumber;
    }

    public void setLeaderMoneyDivide(boolean leaderMoneyDivide) {
        this.leaderMoneyDivide = leaderMoneyDivide;
    }

    public void setLeaderItems(List<ItemStack> leaderItems) {
        this.leaderItems = Helper.removeNullItems(leaderItems);
    }

    public void setMemberMoneyEnabled(boolean memberMoneyEnabled) {
        this.memberMoneyEnabled = memberMoneyEnabled;
    }

    public void setTreatLeadersAsMembers(boolean treatLeadersAsMembers) {
        this.treatLeadersAsMembers = treatLeadersAsMembers;
    }

    public void setMemberMoneyAmount(double memberMoneyAmount) {
        this.memberMoneyAmount = memberMoneyAmount;
    }

    public void setMemberCommandsSomeNumberDivide(boolean memberCommandsSomeNumberDivide) {
        this.memberCommandsSomeNumberDivide = memberCommandsSomeNumberDivide;
    }

    public void setMemberItems(List<ItemStack> memberItems) {
        this.memberItems = Helper.removeNullItems(memberItems);
    }

    public void setMemberItemsEnabled(boolean memberItemsEnabled) {
        this.memberItemsEnabled = memberItemsEnabled;
    }

    public void setMemberCommandsSomeNumber(double memberCommandsSomeNumber) {
        this.memberCommandsSomeNumber = memberCommandsSomeNumber;
    }

    public void setMemberMoneyDivide(boolean memberMoneyDivide) {
        this.memberMoneyDivide = memberMoneyDivide;
    }
}
