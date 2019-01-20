package de.terraconia.backups.extensions;
/*
import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.sql.PhysDB;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockState;
import de.terraconia.backups.events.RegionRestoreEvent;
import de.terraconia.backups.plugin.BackupPlugin;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

import static de.terraconia.backups.manager.BackupManager.toRegion;

public class LWCExtension implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onRestoreEvent(RegionRestoreEvent event) {
        if(event.isCancelled()) return;
        BlockVector3 minimum = event.getRegion().getMinimumPoint();
        BlockVector3 maximum = event.getRegion().getMaximumPoint();
        int minBlockX = minimum.getBlockX();
        int minBlockY = minimum.getBlockY();
        int minBlockZ = minimum.getBlockZ();
        int maxBlockX = maximum.getBlockX();
        int maxBlockY = maximum.getBlockY();
        int maxBlockZ = maximum.getBlockZ();
        int numBlocks = (maxBlockX - minBlockX + 1) * (maxBlockY - minBlockY + 1) * (maxBlockZ - minBlockZ + 1);
        List<Protection> protections = LWC.getInstance().getPhysicalDatabase().loadProtections(
                event.getWorld().getName(), minBlockX, maxBlockX, minBlockY, maxBlockY, minBlockZ, maxBlockZ);
        protections.forEach(Protection::remove);
        event.getRestorePlugin().getLogger()
                .info("Removed " + protections + " from restored region " + event.getRegion().getId());
    }
}*/
