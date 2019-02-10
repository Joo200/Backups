package de.terraconia.backups.extensions;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import de.terraconia.backups.tasks.AbstractTask;
import org.bukkit.Location;
import org.bukkit.World;
import org.dynmap.bukkit.DynmapPlugin;

public class DynmapExtension extends AbstractExtension {
    public static DynmapExtension DEFAULT = new DynmapExtension();

    private DynmapExtension() {}

    @Override
    public void postExecute(AbstractTask task) {
        Region region = task.getAffectedRegion();
        if(region == null || region.getWorld() == null) return;
        World world = BukkitAdapter.adapt(region.getWorld());
        Location l0 = BukkitAdapter.adapt(world, region.getMinimumPoint());
        Location l1 = BukkitAdapter.adapt(world, region.getMaximumPoint());
        DynmapPlugin.plugin.triggerRenderOfVolume(l0, l1);
        task.getPlugin().getLogger().info(
                "Started dynmap rendering for restored region in " + world.getName() + ". " +
                        "From " + region.getMinimumPoint().toString() + " to " + region.getMaximumPoint().toString());
    }
}
