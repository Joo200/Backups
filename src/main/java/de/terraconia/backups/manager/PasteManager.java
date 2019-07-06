package de.terraconia.backups.manager;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import de.terraconia.backups.CopyInterface;
import de.terraconia.backups.events.RegionEvent;
import de.terraconia.backups.events.RegionFinishEvent;
import de.terraconia.backups.helper.SchematicManager;
import de.terraconia.backups.tasks.CopyPasteTask;
import de.terraconia.backups.tasks.SchematicTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class PasteManager extends AbstractManager {
    PasteManager(CopyInterface worldEdit, SchematicManager schemManager) {
        super(worldEdit, schemManager);
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
            requester.getLogger().info("Using AWE for completing task \"" + task.getTag() + "\" with BIG size.");
            future = worldEdit.copySchematic(task);
        } else {
            requester.getLogger().info("Using WorldEdit for completing task \"" + task.getTag() + "\".");
            future = worldEdit.copySchematic(task);
        }
        future.thenApply(aBoolean -> {
            RegionFinishEvent finishEvent = new RegionFinishEvent(task);
            Bukkit.getPluginManager().callEvent(finishEvent);
            return aBoolean;
        });
        return future;
    }

    public CompletableFuture<Boolean> copyPaste(JavaPlugin requester, Player player, Region start, World target, String tag) throws MaxChangedBlocksException {
        BlockArrayClipboard clipboard = new BlockArrayClipboard(start);
        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(start.getWorld(), -1);
        ForwardExtentCopy copy = new ForwardExtentCopy(editSession, start, clipboard, start.getMinimumPoint());
        copy.setCopyingEntities(false);
        Operations.completeLegacy(copy);
        ClipboardHolder holder = new ClipboardHolder(clipboard);

        Mask mask = new BlockMask(editSession);

        CompletableFuture<Boolean> future;
        if(clipboard.getRegion().getArea() > maxWorldEditBlockAmount) {
            requester.getLogger().info("Using WE for completing task \"" + tag + "\" with BIG size.");
            future = worldEdit.pasteRegion(player, holder, target, tag);
        } else {
            requester.getLogger().info("Using WorldEdit for completing task \"" + tag + "\".");
            future = worldEdit.pasteRegion(player, holder, target, tag);
        }
        return future;
    }
}
