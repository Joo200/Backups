package de.terraconia.backups.impl;

import com.sk89q.worldedit.world.block.BlockType;
import de.terraconia.backups.CopyInterface;
import de.terraconia.backups.helper.AsyncWorldEditQueue;
import de.terraconia.backups.helper.PasteAction;
import de.terraconia.backups.tasks.SchematicTask;
import org.bukkit.Bukkit;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AsyncWorldEditImpl implements CopyInterface {
    @Override
    public CompletableFuture<Boolean> copyClipboard(SchematicTask task) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> copyFromTo() {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> copySchematic(SchematicTask task) {
        PasteAction action = new PasteAction(task);
        AsyncWorldEditQueue queue = new AsyncWorldEditQueue(task);
        queue.addJob(action);
        queue.start();
        Bukkit.getLogger().info("Queue started");
        return queue.getFuture().thenApply(aBoolean -> {
            task.setMissingBlocks(queue.getEditSession().popMissingBlocks());

            return true;
        });
    }
}
