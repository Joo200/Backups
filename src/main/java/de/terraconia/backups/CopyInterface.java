package de.terraconia.backups;

import com.sk89q.worldedit.world.block.BlockType;
import de.terraconia.backups.misc.RegionBlocks;
import de.terraconia.backups.tasks.SchematicTask;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface CopyInterface {
    CompletableFuture<Boolean> copyClipboard(SchematicTask task);

    CompletableFuture<Boolean> copyFromTo();

    CompletableFuture<Boolean> copySchematic(SchematicTask task);
}
