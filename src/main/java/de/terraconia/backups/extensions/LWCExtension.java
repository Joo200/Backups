package de.terraconia.backups.extensions;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockState;
import de.terraconia.backups.events.RegionRestoreEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import static de.terraconia.backups.manager.BackupManager.toRegion;

public class LWCExtension implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onRestoreEvent(RegionRestoreEvent event) {
        //Done: Remove all LWC secures.
        LWC lwc = LWC.getInstance();
        Region region = toRegion(event.getWorld(), event.getRegion());
        for (BlockVector3 vector3 : region) {
            Location loc = BukkitAdapter.adapt(BukkitAdapter.adapt(event.getWorld()), vector3);
            if(!lwc.isProtectable(loc.getBlock())) continue;

            Protection protection = lwc.findProtection(loc.getBlock());
            protection.remove();
        }
    }
}
