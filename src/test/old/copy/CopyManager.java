package de.terraconia.backups.old.copy;

import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terraconia.backups.old.backup.SchematicManager;
import de.terraconia.backups.plugin.BackupPlugin;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static de.terraconia.backups.helper.WorldEditHelper.toRegion;

public abstract class CopyManager {
    private SchematicManager manager = BackupPlugin.getManager().getSchematicManager();
    public CompletableFuture<Boolean> copyRegion(JavaPlugin plugin, Player player,
                                                          World start, ProtectedRegion region, World target, BlockVector3 origin) {
        return copyRegion(plugin, player, toRegion(start, region), target, origin);
    }
    public CompletableFuture<Boolean> copyRegion(JavaPlugin plugin, Player player,
                                                          Region region, World target, BlockVector3 origin) {
        Clipboard clipboard = new BlockArrayClipboard(region);
        return copyClipboard(plugin, player, clipboard, target, origin);
    }

    public CompletableFuture<Boolean> restoreRegion(
            JavaPlugin requester, Player player, ProtectedRegion region, World world, String schematicPath, BlockVector3 origin) {
        Clipboard toCopy;
        try {
            toCopy = manager.loadSchematic(schematicPath);
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
        return copyClipboard(requester, player, toCopy, world, origin);
    }

    public abstract CompletableFuture<Boolean> copyClipboard(JavaPlugin plugin, Player player,
                                                             Clipboard clipboard, World target, BlockVector3 origin);
}
