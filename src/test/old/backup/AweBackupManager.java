package de.terraconia.backups.old.backup;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terraconia.backups.blockbag.CityBlockBag;
import de.terraconia.backups.events.RegionRestoreEvent;
import de.terraconia.backups.helper.AsyncWorldEditQueue;
import de.terraconia.backups.helper.PasteAction;
import de.terraconia.backups.plugin.BackupPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.primesoft.asyncworldedit.AsyncWorldEditBukkit;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerManager;
import org.primesoft.asyncworldedit.api.worldedit.IAsyncEditSessionFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static de.terraconia.backups.helper.WorldEditHelper.toRegion;

public class AweBackupManager extends BackupManager {
    private IAsyncEditSessionFactory factory;
    private IPlayerManager playerManager;

    public AweBackupManager(BackupPlugin plugin) {
        super(plugin);
        EditSessionFactory weFactory = WorldEdit.getInstance().getEditSessionFactory();
        if(weFactory instanceof IAsyncEditSessionFactory)
            this.factory = (IAsyncEditSessionFactory)weFactory;
        else throw new RuntimeException("AsyncWorldEdit not injected.");
        playerManager = AsyncWorldEditBukkit.getInstance().getPlayerManager();
    }

    @Override
    public void backupSubRegion(JavaPlugin requester, ProtectedRegion protectedRegion, World world, String schematicPath) throws IOException, WorldEditException {

        Region region = toRegion(world, protectedRegion);

        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1);
        ForwardExtentCopy copy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
        copy.setCopyingEntities(false);
        Operations.completeLegacy(copy);
        getSchematicManager().saveSubRegionSchematic(schematicPath, clipboard);
    }

    @Override
    public CompletableFuture<Map<BlockType, Integer>> restoreSubRegion(JavaPlugin requester,
                                                                       Player player,
                                                                       ProtectedRegion subRegion,
                                                                       Set<Location> cityChestLocations,
                                                                       World world,
                                                                       String schematicPath) {
        Set<Container> chests = cityChestLocations.stream().map(Location::getBlock).map(Block::getState)
                .filter(c -> c instanceof Container).map(c -> (Container)c).collect(Collectors.toSet());
        Bukkit.getLogger().info("Size of chests: " + chests.size());
        CityBlockBag bag = new CityBlockBag(chests, getDeniedBlocks(), getFreeBlocks());
        return restoreSubRegion(requester, player, subRegion, bag, world, schematicPath);
    }
}
