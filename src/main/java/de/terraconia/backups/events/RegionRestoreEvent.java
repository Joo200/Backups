package de.terraconia.backups.events;

import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class RegionRestoreEvent extends Event implements Cancellable {
    private JavaPlugin restorePlugin;
    private ProtectedRegion region;
    private World world;

    private boolean isCancelled = false;

    private static HandlerList handlerList = new HandlerList();

    public RegionRestoreEvent(JavaPlugin restorePlugin, ProtectedRegion region, World world) {
        this.restorePlugin = restorePlugin;
        this.region = region;
        this.world = world;
    }

    public JavaPlugin getRestorePlugin() {
        return restorePlugin;
    }

    public ProtectedRegion getRegion() {
        return region;
    }

    public World getWorld() {
        return world;
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
