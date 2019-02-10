package de.terraconia.backups.events;

import de.terraconia.backups.tasks.AbstractTask;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class RegionEvent extends Event implements Cancellable {
    private AbstractTask task;

    private boolean isCancelled = false;

    private static HandlerList handlerList = new HandlerList();

    public RegionEvent(AbstractTask task) {
        this.task = task;
    }

    public JavaPlugin getRestorePlugin() {
        return task.getPlugin();
    }

    public AbstractTask getTask() {
        return task;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }
}
