package de.terraconia.backups.events;

import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terraconia.backups.tasks.AbstractTask;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class RegionFinishEvent extends Event {
    private AbstractTask task;

    private static HandlerList handlerList = new HandlerList();

    public RegionFinishEvent(AbstractTask task) {
        this.task = task;
    }

    public AbstractTask getTask() {
        return task;
    }

    public JavaPlugin getRestorePlugin() {
        return task.getPlugin();
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
