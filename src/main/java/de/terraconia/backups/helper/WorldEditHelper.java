package de.terraconia.backups.helper;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.util.WorldEditRegionConverter;

public abstract class WorldEditHelper {
    public static Region toRegion(World world, ProtectedRegion region) {
        Region blockVector3s = WorldEditRegionConverter.convertToRegion(region);
        blockVector3s.setWorld(world);
        return blockVector3s;
    }
}
