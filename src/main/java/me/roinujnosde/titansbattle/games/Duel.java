package me.roinujnosde.titansbattle.games;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class Duel<T> {
    private final List<T> duelists;

    public Duel(T duelist1, T duelist2) {
        this.duelists = Arrays.asList(duelist1, duelist2);
    }

    @Contract("null -> false")
    public boolean isDuelist(@Nullable T t) {
        for (T duelist : duelists) {
            if (duelist.equals(t)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public T getOther(T t) {
        for (T duelist : duelists) {
            if (!duelist.equals(t)) {
                return duelist;
            }
        }
        return null;
    }

    public List<T> getDuelists() {
        return duelists;
    }
}
