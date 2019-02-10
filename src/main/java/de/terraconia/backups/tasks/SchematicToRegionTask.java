package de.terraconia.backups.tasks;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SchematicToRegionTask extends SchematicTask {
    private ProtectedRegion region;

    public SchematicToRegionTask(JavaPlugin plugin, Player player, String tag, Clipboard clipboard, World target, ProtectedRegion region) {
        super(plugin, player, tag, clipboard, region.getMinimumPoint(), target);
        this.region = region;
    }

    public ProtectedRegion getRegion() {
        return region;
    }
}
