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

import me.roinujnosde.titansbattle.types.Game.Mode;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author RoinujNosde
 */
public class Scheduler {

    private String id;
    private Mode mode;
    private int day;
    private int hour;
    private int minute;

    private Scheduler() {
    }

    public Scheduler(@NotNull String id, @NotNull Mode mode, int day, int hour, int minute) {
        if (day < 0 || day > 7) {
            throw new IllegalArgumentException();
        }
        if (hour < 0 || hour > 24) {
            throw new IllegalArgumentException();
        }
        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException();
        }
        this.day = day;
        this.id = id;
        this.mode = mode;
        this.hour = hour;
        this.minute = minute;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public Mode getMode() {
        return mode;
    }

    public int getDay() {
        return day;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }
}
