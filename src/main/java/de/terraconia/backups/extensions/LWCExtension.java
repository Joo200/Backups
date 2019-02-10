package de.terraconia.backups.extensions;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import de.terraconia.backups.tasks.AbstractTask;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class LWCExtension extends AbstractExtension {
    public static LWCExtension REMOVE_ALL = new LWCExtension(true);
    public static LWCExtension REMOVE_UNPROTECTABLE = new LWCExtension(false);

    private boolean removeLocks;
    private Collection<UUID> allowedUuids = null;

    private LWCExtension(boolean removeLocks) {
        this.removeLocks = removeLocks;
    }

    public LWCExtension(Set<UUID> allowedUuids) {
        this.allowedUuids = allowedUuids;
    }

    @Override
    public void postExecute(AbstractTask task) {
        Region region = task.getAffectedRegion();
        if(region == null || region.getWorld() == null) return;
        BlockVector3 minimum = region.getMinimumPoint();
        BlockVector3 maximum = region.getMaximumPoint();
        int minBlockX = minimum.getBlockX();
        int minBlockY = minimum.getBlockY();
        int minBlockZ = minimum.getBlockZ();
        int maxBlockX = maximum.getBlockX();
        int maxBlockY = maximum.getBlockY();
        int maxBlockZ = maximum.getBlockZ();
        int numBlocks = (maxBlockX - minBlockX + 1) * (maxBlockY - minBlockY + 1) * (maxBlockZ - minBlockZ + 1);
        List<Protection> protections = LWC.getInstance().getPhysicalDatabase().loadProtections(
                region.getWorld().getName(), minBlockX, maxBlockX, minBlockY, maxBlockY, minBlockZ, maxBlockZ);
        if(allowedUuids != null) {
            protections.stream().filter(protection -> {
                try {
                    UUID uuid = UUID.fromString(protection.getOwner());
                    return allowedUuids.contains(uuid);
                } catch(IllegalArgumentException ignored) { }
                return false;
            }).forEach(Protection::remove);
        }
        protections.stream().filter(protection -> !LWC.getInstance().isProtectable(protection.getBlock()))
                .forEach(Protection::remove);
        task.getPlugin().getLogger()
                .info("Removed " + protections.size() + " from changed region in world " + region.getWorld().getName() + ".");
    }
}
