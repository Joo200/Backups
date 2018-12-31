package de.terraconia.backups.extensions;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import de.terraconia.backups.events.RegionRestoreEvent;
import de.terraconia.backups.events.RegionRestoreFinishEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.dynmap.bukkit.DynmapPlugin;

public class DynmapExtension implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onRestore(RegionRestoreFinishEvent event) {
        World world = BukkitAdapter.adapt(event.getWorld());
        Location l0 = BukkitAdapter.adapt(world, event.getRegion().getMinimumPoint());
        Location l1 = BukkitAdapter.adapt(world, event.getRegion().getMaximumPoint());
        DynmapPlugin.plugin.triggerRenderOfVolume(l0, l1);
        event.getRestorePlugin().getLogger().info(
                "Started dynmap rendering for restored region " + event.getRegion().getId() + ".");
    }
}
