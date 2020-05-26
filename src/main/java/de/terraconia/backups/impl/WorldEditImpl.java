package de.terraconia.backups.impl;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import de.terraconia.backups.CopyInterface;
import de.terraconia.backups.tasks.SchematicTask;
import de.terraconia.backups.manager.BackupManager;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class WorldEditImpl implements CopyInterface {
    @Override
    public CompletableFuture<Boolean> copySchematic(SchematicTask task) {
        EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(task.getTarget(),
                BackupManager.maxWorldEditBlockAmount);
        session.setFastMode(true);
        session.setReorderMode(EditSession.ReorderMode.FAST);
        if(task.getBlockBag() != null) session.setBlockBag(task.getBlockBag());
        if(task.getMask() != null) session.setMask(task.getMask());

        ClipboardHolder holder = new ClipboardHolder(task.getClipboard());
        if(task.getTransform() != null) holder.setTransform(task.getTransform());

        Operation operation = holder.createPaste(session)
                .to(task.getOrigin())
                .ignoreAirBlocks(task.isIgnoreAir())
                .build();
        try {
            Operations.complete(operation);
        } catch (WorldEditException e) {
            return CompletableFuture.failedFuture(e);
        }
        session.flushSession();
        if(session.getBlockBag() != null) session.getBlockBag().flushChanges();
        task.setMissingBlocks(session.popMissingBlocks());
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> pasteRegion(Player player, ClipboardHolder clipboard, World world, String tag) {
        EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world,
                BackupManager.maxWorldEditBlockAmount);
        session.setFastMode(true);
        session.setReorderMode(EditSession.ReorderMode.FAST);

        Operation operation = clipboard.createPaste(session)
                .ignoreAirBlocks(false)
                .build();
        try {
            Operations.complete(operation);
        } catch (WorldEditException e) {
            return CompletableFuture.failedFuture(e);
        }
        session.flushSession();
        return CompletableFuture.completedFuture(true);
    }
}
