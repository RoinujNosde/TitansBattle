package me.roinujnosde.titansbattle.games;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Duel<T> {
    private final List<T> duelists = new ArrayList<>();

    public Duel(T duelist1, T duelist2) {
        add(duelist1);
        add(duelist2);
    }

    private void add(T duelist) {
        if (duelist != null) {
            duelists.add(duelist);
        }
    }

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

    public void remove(T t) {
        duelists.remove(t);
    }

    public boolean isValid() {
        return duelists.size() == 2;
    }

    public List<T> getDuelists() {
        return duelists;
    }
}
