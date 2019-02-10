package de.terraconia.backups.helper;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public abstract class WorldEditHelper {
    public static Region toRegion(World world, ProtectedRegion region) {
        if (region instanceof ProtectedCuboidRegion) {
            BlockVector3 min = region.getMinimumPoint();
            BlockVector3 max = region.getMaximumPoint();
            return new CuboidRegion(world, min, max);
        } else if (region instanceof ProtectedPolygonalRegion) {
            return new Polygonal2DRegion(world, region.getPoints(),
                    region.getMinimumPoint().getY(), region.getMaximumPoint().getY());
        } else {
            throw new RuntimeException("Unknown region type: " + region.getClass().getCanonicalName());
        }
    }
}
