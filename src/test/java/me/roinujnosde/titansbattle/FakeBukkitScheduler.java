package me.roinujnosde.titansbattle;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitWorker;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

public class FakeBukkitScheduler implements BukkitScheduler {

    private long currentTick = 0;
    private int lastId = 0;
    private final Map<Integer, FakeBukkitTask> tasks = new HashMap<>();

    public void performTicks(int ticks) {
        for (int i = 0; i < ticks; i++) {
            performOneTick();
        }
    }

    public void performOneTick() {
        for (FakeBukkitTask task : new CopyOnWriteArrayList<>(tasks.values())) {
            if (task.isCancelled()) {
                continue;
            }
            if (task.getScheduledTick() != currentTick) {
                continue;
            }
            task.getRunnable().run();
            if (task.isRepeating()) {
                task.setScheduledTick(currentTick + task.getPeriod());
            }
        }
        currentTick++;
    }

    @Override
    public int scheduleSyncDelayedTask(@NotNull Plugin plugin, @NotNull Runnable runnable, long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int scheduleSyncDelayedTask(@NotNull Plugin plugin, @NotNull BukkitRunnable bukkitRunnable, long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int scheduleSyncDelayedTask(@NotNull Plugin plugin, @NotNull Runnable runnable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int scheduleSyncDelayedTask(@NotNull Plugin plugin, @NotNull BukkitRunnable bukkitRunnable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int scheduleSyncRepeatingTask(@NotNull Plugin plugin, @NotNull Runnable runnable, long l, long l1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int scheduleSyncRepeatingTask(@NotNull Plugin plugin, @NotNull BukkitRunnable bukkitRunnable, long l, long l1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int scheduleAsyncDelayedTask(@NotNull Plugin plugin, @NotNull Runnable runnable, long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int scheduleAsyncDelayedTask(@NotNull Plugin plugin, @NotNull Runnable runnable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int scheduleAsyncRepeatingTask(@NotNull Plugin plugin, @NotNull Runnable runnable, long l, long l1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull <T> Future<T> callSyncMethod(@NotNull Plugin plugin, @NotNull Callable<T> callable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cancelTask(int id) {
        tasks.get(id).cancel();
    }

    @Override
    public void cancelTasks(@NotNull Plugin plugin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cancelAllTasks() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCurrentlyRunning(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isQueued(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull List<BukkitWorker> getActiveWorkers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull List<BukkitTask> getPendingTasks() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull BukkitTask runTask(@NotNull Plugin plugin, @NotNull Runnable runnable) throws IllegalArgumentException {
        FakeBukkitTask task = new FakeBukkitTask(lastId, plugin, runnable, true, false, currentTick, 0);
        tasks.put(lastId, task);
        lastId++;
        return task;
    }

    @Override
    public @NotNull BukkitTask runTask(@NotNull Plugin plugin, @NotNull BukkitRunnable bukkitRunnable) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull BukkitTask runTaskAsynchronously(@NotNull Plugin plugin, @NotNull Runnable runnable) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull BukkitTask runTaskAsynchronously(@NotNull Plugin plugin, @NotNull BukkitRunnable bukkitRunnable) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull BukkitTask runTaskLater(@NotNull Plugin plugin, @NotNull Runnable runnable, long delay) throws IllegalArgumentException {
        FakeBukkitTask task = new FakeBukkitTask(lastId, plugin, runnable, true, false, currentTick + delay, 0);
        tasks.put(lastId, task);
        lastId++;
        return task;
    }

    @Override
    public @NotNull BukkitTask runTaskLater(@NotNull Plugin plugin, @NotNull BukkitRunnable bukkitRunnable, long l) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull BukkitTask runTaskLaterAsynchronously(@NotNull Plugin plugin, @NotNull Runnable runnable, long l) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull BukkitTask runTaskLaterAsynchronously(@NotNull Plugin plugin, @NotNull BukkitRunnable bukkitRunnable, long l) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull BukkitTask runTaskTimer(@NotNull Plugin plugin, @NotNull Runnable runnable, long delay, long period) throws IllegalArgumentException {
        FakeBukkitTask task = new FakeBukkitTask(lastId, plugin, runnable, true, true, currentTick + delay, period);
        tasks.put(lastId, task);
        lastId++;
        return task;
    }

    @Override
    public @NotNull BukkitTask runTaskTimer(@NotNull Plugin plugin, @NotNull BukkitRunnable bukkitRunnable, long l, long l1) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull BukkitTask runTaskTimerAsynchronously(@NotNull Plugin plugin, @NotNull Runnable runnable, long l, long l1) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull BukkitTask runTaskTimerAsynchronously(@NotNull Plugin plugin, @NotNull BukkitRunnable bukkitRunnable, long l, long l1) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

}
