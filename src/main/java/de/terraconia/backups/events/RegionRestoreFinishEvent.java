package de.terraconia.backups.events;

import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class RegionRestoreFinishEvent extends Event {
    private JavaPlugin restorePlugin;
    private ProtectedRegion region;
    private World world;

    private static HandlerList handlerList = new HandlerList();

    public RegionRestoreFinishEvent(JavaPlugin restorePlugin, ProtectedRegion region, World world) {
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
}
