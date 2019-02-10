package de.terraconia.backups.old.copy;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import de.terraconia.backups.helper.AsyncWorldEditQueue;
import de.terraconia.backups.helper.PasteAction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

public class AweCopyManager extends CopyManager {
    @Override
    public CompletableFuture<Boolean> copyClipboard(JavaPlugin plugin, Player player, Clipboard clipboard, World target, BlockVector3 origin) {
        ClipboardHolder holder = new ClipboardHolder(clipboard);
        PasteAction action = new PasteAction(origin, holder, false);

        AsyncWorldEditQueue queue = new AsyncWorldEditQueue(player, target, "Copy Task");
        // queue.setBlockMask(new BlockTypeMask(queue.getEditSession(), getDeniedBlocks()));
        queue.addJob(action);
        queue.start();
        return queue.getFuture().thenApply(aBoolean -> {
                    Bukkit.getLogger().info("Missing blocks: " + queue.getEditSession().popMissingBlocks().size());
                    return aBoolean;
                }
        );
    }
}
