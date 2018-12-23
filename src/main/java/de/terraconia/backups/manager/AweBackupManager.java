package de.terraconia.backups.manager;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terraconia.backups.plugin.BackupPlugin;
import org.bukkit.Location;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Created by Johannes on 13.12.2018.
 */
public class AweBackupManager extends BackupManager {
    public AweBackupManager(BackupPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void backupSubRegion(ProtectedRegion protectedRegion, World world, String schematicPath)
            throws IOException, WorldEditException {

    }

    @Override
    public Map<BlockType, Integer> restoreSubRegion(ProtectedRegion subRegion, Set<Location> cityChestLocations,
                                                    World world, boolean ignoreChests,
                                                    String schematicPath) throws WorldEditException, IOException {
        return null;
    }
}
