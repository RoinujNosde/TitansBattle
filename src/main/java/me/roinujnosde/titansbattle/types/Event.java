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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * @author RoinujNosde
 */
public class Event {

    private final String gameName;
    private final Frequency frequency;
    private final int day;
    private final int hour;
    private final int minute;

    public Event(@NotNull String gameName, @NotNull Frequency frequency, int day, int hour, int minute) {
        if (day < 0 || (day > 7 && frequency == Frequency.WEEKLY) || day > 31) {
            throw new IllegalArgumentException("invalid day");
        }
        if (hour < 0 || hour > 24) {
            throw new IllegalArgumentException("invalid hour");
        }
        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException("invalid minute");
        }
        this.day = day;
        this.gameName = gameName;
        this.frequency = frequency;
        this.hour = hour;
        this.minute = minute;
    }

    public @NotNull String getGameName() {
        return gameName;
    }

    public Frequency getFrequency() {
        return frequency;
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

    public long getDelay() {
        switch (frequency) {
            case HOURLY:
                return getHourlyDelay();
            case DAILY:
                return getDailyDelay();
            case WEEKLY:
                return getWeeklyDelay();
            case MONTHLY:
                return getMonthlyDelay();
        }
        return -1;
    }

    private int getHourlyDelay() {
        int difference = getMinute() - LocalTime.now().getMinute();
        if (difference < 0) {
            difference += 60;
        }
        return difference * 60 * 1000;
    }

    private long getDailyDelay() {
        LocalDateTime target = LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, minute));
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(target)) {
            target = target.plusDays(1);
        }

        return now.until(target, ChronoUnit.MILLIS);
    }

    private long getWeeklyDelay() {
        int dayOfWeek = LocalDate.now().getDayOfWeek().getValue();
        int dayOfWeekToStart = day;
        if (dayOfWeekToStart < dayOfWeek) {
            dayOfWeekToStart += 7;
        }
        int difference = dayOfWeekToStart - dayOfWeek;
        LocalDateTime target = LocalDateTime.of(LocalDate.now().plusDays(difference), LocalTime.of(hour, minute));
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(target)) {
            target = target.plusDays(7);
        }

        return now.until(target, ChronoUnit.MILLIS);
    }

    private long getMonthlyDelay() {
        int dayOfMonth = LocalDate.now().getDayOfMonth();
        int dayOfMonthToStart = day;
        if (dayOfMonthToStart < dayOfMonth) {
            dayOfMonthToStart += LocalDate.now().lengthOfMonth();
        }
        int difference = dayOfMonthToStart - dayOfMonth;
        LocalDateTime target = LocalDateTime.of(LocalDate.now().plusDays(difference), LocalTime.of(hour, minute));
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(target)) {
            target = target.plusMonths(1);
        }

        return now.until(target, ChronoUnit.MILLIS);
    }

    public enum Frequency {
        HOURLY(3600000),
        DAILY(86400000),
        WEEKLY(604800000),
        MONTHLY(2628000000L);

        private final long periodInMillis;

        Frequency(long periodInMillis) {
            this.periodInMillis = periodInMillis;
        }

        /**
         * Returns the frequency period in milliseconds.
         *
         * @return the period in millis
         */
        public long getPeriod() {
            return periodInMillis;
        }
    }

}
