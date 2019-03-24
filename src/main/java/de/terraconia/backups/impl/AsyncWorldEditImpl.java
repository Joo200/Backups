package de.terraconia.backups.impl;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockType;
import de.terraconia.backups.CopyInterface;
import de.terraconia.backups.helper.AsyncWorldEditQueue;
import de.terraconia.backups.helper.PasteAction;
import de.terraconia.backups.tasks.SchematicTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AsyncWorldEditImpl implements CopyInterface {
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

    @Override
    public CompletableFuture<Boolean> pasteRegion(Player player, ClipboardHolder clipboard, World world, String tag) {
        PasteAction action = new PasteAction(clipboard.getClipboard().getOrigin(), clipboard, false);
        AsyncWorldEditQueue queue = new AsyncWorldEditQueue(player, world, tag);
        queue.addJob(action);
        queue.start();
        return queue.getFuture();
    }
}
