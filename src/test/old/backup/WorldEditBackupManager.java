package de.terraconia.backups.old.backup;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.mask.BlockTypeMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terraconia.backups.blockbag.CityBlockBag;
import de.terraconia.backups.events.RegionFinishEvent;
import de.terraconia.backups.events.RegionRestoreEvent;
import de.terraconia.backups.plugin.BackupPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static de.terraconia.backups.helper.WorldEditHelper.toRegion;

public class WorldEditBackupManager extends BackupManager {


    public WorldEditBackupManager(BackupPlugin plugin) {
        super(plugin);
    }

    @Override
    public void backupSubRegion(
            JavaPlugin requester,
            ProtectedRegion protectedRegion,
            World world,
            String schematicPath) throws IOException, WorldEditException {
        Region region = toRegion(world, protectedRegion);

        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1);
        ForwardExtentCopy copy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
        copy.setCopyingEntities(false);
        Operations.completeLegacy(copy);
        getSchematicManager().saveSubRegionSchematic(schematicPath, clipboard);
    }

    @Override
    public CompletableFuture<Map<BlockType, Integer>> restoreSubRegion(
            JavaPlugin requester,
            Player player,
            ProtectedRegion subRegion,
            Set<Location> cityChestLocations,
            World world,
            String schematicPath) {
        // Getting a set of containers for the items.
        Set<Container> chests = cityChestLocations.stream().map(Location::getBlock).map(Block::getState)
                .filter(c -> c instanceof Container).map(c -> (Container)c).collect(Collectors.toSet());
        CityBlockBag bag = new CityBlockBag(chests, getDeniedBlocks(), getFreeBlocks());
        return restoreSubRegion(requester, player, subRegion, bag, world, schematicPath);
    }

    @Override
    public CompletableFuture<Map<BlockType, Integer>> restoreSubRegion(JavaPlugin requester,
                                                                       Player player,
                                                                       ProtectedRegion subRegion,
                                                                       CityBlockBag bag,
                                                                       World world,
                                                                       String schematicPath) {
        RegionRestoreEvent restoreEvent = new RegionRestoreEvent(requester, subRegion, world);
        Bukkit.getPluginManager().callEvent(restoreEvent);
        if(restoreEvent.isCancelled()) return null;

        EditSession session = WorldEdit.getInstance().getEditSessionFactory()
                .getEditSession(world, 500);
        Clipboard toCopy = null;
        try {
            toCopy = getSchematicManager().loadSchematic(schematicPath);
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
        try {
            removeInventory(toCopy);
        } catch (WorldEditException e) {
            return CompletableFuture.failedFuture(e);
        }

        if(bag != null) session.setBlockBag(bag);

        Mask mask = Masks.negate(new BlockTypeMask(session, getDeniedBlocks()));
        session.setMask(mask);


        Operation operation = new ClipboardHolder(toCopy)
                .createPaste(session)
                .to(subRegion.getMinimumPoint())
                .ignoreAirBlocks(false)
                .build();

        try {
            Operations.complete(operation);
        } catch (WorldEditException e) {
            return CompletableFuture.failedFuture(e);
        }
        session.flushSession();

        RegionFinishEvent finishEvent = new RegionFinishEvent(requester, player, subRegion, world);
        Bukkit.getPluginManager().callEvent(finishEvent);
        return CompletableFuture.completedFuture(session.popMissingBlocks());
    }
}
