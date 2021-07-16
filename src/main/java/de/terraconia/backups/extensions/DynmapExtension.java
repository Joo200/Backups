package de.terraconia.backups.extensions;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import de.terraconia.backups.tasks.AbstractTask;
import org.bukkit.Location;
import org.bukkit.World;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;

public class DynmapExtension extends AbstractExtension {
    public static DynmapExtension DEFAULT = new DynmapExtension();
    private static DynmapCommonAPI api = null;

    private DynmapExtension() {
        DynmapCommonAPIListener.register(new DynmapCommonAPIListener() {
            @Override
            public void apiEnabled(DynmapCommonAPI dynmapCommonAPI) {
                api = dynmapCommonAPI;
            }

            @Override
            public void apiDisabled(DynmapCommonAPI dis) {
                api = null;
            }
        });
    }

    @Override
    public void postExecute(AbstractTask task) {
        Region region = task.getAffectedRegion();
        if (region == null || region.getWorld() == null) return;
        if (api == null) return;
        World world = BukkitAdapter.adapt(region.getWorld());
        Location l0 = BukkitAdapter.adapt(world, region.getMinimumPoint());
        Location l1 = BukkitAdapter.adapt(world, region.getMaximumPoint());

        api.triggerRenderOfVolume(world.getName(),
                l0.getBlockX(), l0.getBlockY(), l0.getBlockZ(),
                l1.getBlockX(), l1.getBlockY(), l1.getBlockZ());
        task.getPlugin().getLogger().info(
                "Started dynmap rendering for restored region in " + world.getName() + ". " +
                        "From " + region.getMinimumPoint().toString() + " to " + region.getMaximumPoint().toString());
    }
}
