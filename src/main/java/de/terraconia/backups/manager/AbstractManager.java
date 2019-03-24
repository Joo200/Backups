package de.terraconia.backups.manager;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terraconia.backups.CopyInterface;
import de.terraconia.backups.helper.SchematicManager;
import de.terraconia.backups.tasks.AbstractTask;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

import static de.terraconia.backups.helper.WorldEditHelper.toRegion;

abstract class AbstractManager {
    CopyInterface worldEdit;
    CopyInterface asyncWorldEdit;
    SchematicManager schemManager;
    int maxWorldEditBlockAmount = 25000;

    AbstractManager(CopyInterface worldEdit, CopyInterface asyncWorldEdit, SchematicManager schemManager) {
        this.worldEdit = worldEdit;
        this.schemManager = schemManager;
        this.asyncWorldEdit = asyncWorldEdit;
    }

    public void saveSchematic(JavaPlugin plugin, World world, ProtectedRegion protectedRegion, String file) throws MaxChangedBlocksException, IOException {
        Region region = toRegion(world, protectedRegion);

        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1);
        ForwardExtentCopy copy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
        copy.setCopyingEntities(false);
        Operations.completeLegacy(copy);
        schemManager.saveSubRegionSchematic(file, clipboard);
    }

    public boolean hasSchematic(String path) {
        return schemManager.hasSchematic(path);
    }

    public void removeSchematic(String path) {
        schemManager.removeSchematic(path);
    }
}
