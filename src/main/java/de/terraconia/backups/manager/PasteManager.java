package de.terraconia.backups.manager;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import de.terraconia.backups.CopyInterface;
import de.terraconia.backups.events.RegionEvent;
import de.terraconia.backups.events.RegionFinishEvent;
import de.terraconia.backups.helper.SchematicManager;
import de.terraconia.backups.tasks.SchematicTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class PasteManager extends AbstractManager {
    PasteManager(CopyInterface worldEdit, CopyInterface asyncWorldEdit, SchematicManager schemManager) {
        super(worldEdit, asyncWorldEdit, schemManager);
    }

    public CompletableFuture<Boolean> pasteSchematic(JavaPlugin requester, String schematic,
                                                     World world, BlockVector3 origin, String tag) {
        Clipboard clipboard;
        try {
            clipboard = schemManager.loadSchematic(schematic);
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
        SchematicTask task = new SchematicTask(requester, null, tag, clipboard, origin, world);
        RegionEvent restoreEvent = new RegionEvent(task);
        Bukkit.getPluginManager().callEvent(restoreEvent);
        if(restoreEvent.isCancelled()) return CompletableFuture.failedFuture(new InterruptedException());

        CompletableFuture<Boolean> future;
        if(clipboard.getRegion().getArea() > maxWorldEditBlockAmount) {
            requester.getLogger().info("Using AWE for completing task \"" + task.getTag() + "\".");
            future = asyncWorldEdit.copyClipboard(task);
        } else {
            requester.getLogger().info("Using WorldEdit for completing task \"" + task.getTag() + "\".");
            future = worldEdit.copyClipboard(task);
        }
        future.thenApply(aBoolean -> {
            RegionFinishEvent finishEvent = new RegionFinishEvent(task);
            Bukkit.getPluginManager().callEvent(finishEvent);
            return aBoolean;
        });
        return future;
    }
}
