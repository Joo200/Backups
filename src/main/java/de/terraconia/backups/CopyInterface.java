package de.terraconia.backups;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockType;
import de.terraconia.backups.misc.RegionBlocks;
import de.terraconia.backups.tasks.SchematicTask;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface CopyInterface {
    CompletableFuture<Boolean> copySchematic(SchematicTask task);

    CompletableFuture<Boolean> pasteRegion(Player player, ClipboardHolder clipboard, World world, String tag);
}
