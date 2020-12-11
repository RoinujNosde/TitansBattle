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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 *
 * @author RoinujNosde
 */
public class Scheduler {

    private static final List<Scheduler> SCHEDULERS = new ArrayList<>();

    private final String id;
    private final String gameName;
    private final int day;
    private final int hour;
    private final int minute;

    public Scheduler(@NotNull String id, @NotNull String gameName, int day, int hour, int minute) {
        if (day < 0 || day > 7) {
            throw new IllegalArgumentException("invalid day");
        }
        if (hour < 0 || hour > 24) {
            throw new IllegalArgumentException("invalid hour");
        }
        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException("invalid minute");
        }
        this.day = day;
        this.id = id;
        this.gameName = gameName;
        this.hour = hour;
        this.minute = minute;
    }

    @NotNull
    public String getId() {
        return id;
    }

    public @NotNull String getGameName() {
        return gameName;
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

    public static List<Scheduler> getSchedulers() {
        return SCHEDULERS;
    }

    public static Scheduler getNextSchedulerOfDay() {
        Calendar today = Calendar.getInstance();
        int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);
        int hour = today.get(Calendar.HOUR_OF_DAY);
        int minute = today.get(Calendar.MINUTE);

        Scheduler nextScheduler = null;
        boolean firstScheduler = true;
        int nextHour = 0;
        int nextMinute = 0;

        for (Scheduler s : SCHEDULERS) {
            //Is it the day of the Scheduler?
            if (s.getDay() == dayOfWeek) {
                //Is it the first Scheduler looped?
                if (firstScheduler) {
                    if (!(s.getHour() < hour) && !(s.getMinute() < minute)) {
                        nextScheduler = s;
                        nextHour = s.getHour();
                        nextMinute = s.getMinute();
                        firstScheduler = false;
                    }
                    continue;
                }
                if ((s.getHour() <= nextHour) && !(s.getHour() < hour)) {
                    if (s.getHour() == nextHour) {
                        if ((s.getMinute() < nextMinute) && !(s.getMinute() < minute)) {
                            nextScheduler = s;
                            nextHour = s.getHour();
                            nextMinute = s.getMinute();
                            continue;
                        }
                    }
                    if (s.getHour() < nextHour) {
                        if (s.getMinute() >= minute) {
                            nextScheduler = s;
                            nextHour = s.getHour();
                            nextMinute = s.getMinute();
                        }
                    }
                }
            }
        }
        return nextScheduler;
    }
}
