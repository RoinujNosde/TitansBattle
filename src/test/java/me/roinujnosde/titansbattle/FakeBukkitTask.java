package me.roinujnosde.titansbattle;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class FakeBukkitTask implements BukkitTask {

    private final int taskId;
    private final Plugin plugin;
    private final Runnable runnable;
    private final boolean sync;
    private boolean cancelled;
    private final boolean repeating;
    private long scheduledTick;
    private final long period;

    public FakeBukkitTask(int taskId, Plugin plugin, Runnable runnable, boolean sync, boolean repeating, long scheduledTick, long period) {
        this.taskId = taskId;
        this.plugin = plugin;
        this.runnable = runnable;
        this.sync = sync;
        this.repeating = repeating;
        this.scheduledTick = scheduledTick;
        this.period = period;
    }

    @Override
    public int getTaskId() {
        return taskId;
    }

    @Override
    public @NotNull Plugin getOwner() {
        return plugin;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    @Override
    public boolean isSync() {
        return sync;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isRepeating() {
        return repeating;
    }

    public long getPeriod() {
        return period;
    }

    public long getScheduledTick() {
        return scheduledTick;
    }

    public void setScheduledTick(long scheduledTick) {
        this.scheduledTick = scheduledTick;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }
}
