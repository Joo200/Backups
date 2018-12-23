package de.terraconia.backups.manager;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.Tag;
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
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terraconia.backups.blockbag.CityBlockBag;
import de.terraconia.backups.plugin.BackupPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class WorldEditBackupManager extends BackupManager {


    public WorldEditBackupManager(BackupPlugin plugin) {
        super(plugin);
    }

    @Override
    public void backupSubRegion(ProtectedRegion protectedRegion, World world, String schematicPath) throws IOException, WorldEditException {
        Region region = toRegion(world, protectedRegion);

        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1);
        ForwardExtentCopy copy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
        copy.setCopyingEntities(false);
        Operations.completeLegacy(copy);
        getSchematicManager().saveSubRegionSchematic(schematicPath, clipboard);
    }

    @Override
    public Map<BlockType, Integer> restoreSubRegion(
            ProtectedRegion subRegion,
            Set<Location> cityChestLocations,
            World world,
            // int worldId,
            boolean ignoreChests,
            String schematicPath) throws WorldEditException, IOException {
        Logger log = Bukkit.getLogger();
        // Getting a set of containers for the items.
        Set<Container> chests = cityChestLocations.stream().map(Location::getBlock).map(Block::getState)
                .filter(c -> c instanceof Container).map(c -> (Container)c).collect(Collectors.toSet());
        EditSession session = WorldEdit.getInstance().getEditSessionFactory()
                .getEditSession(world, 500);
        Clipboard toCopy = getSchematicManager().loadSchematic(schematicPath);

        CityBlockBag bag = new CityBlockBag(chests, getDeniedBlocks(), getFreeBlocks());
        session.setBlockBag(bag);

        Mask mask = Masks.negate(new BlockTypeMask(session, getDeniedBlocks()));
        session.setMask(mask);

        removeInventory(toCopy);

        Operation operation = new ClipboardHolder(toCopy)
                .createPaste(session)
                .to(subRegion.getMinimumPoint())
                .ignoreAirBlocks(false)
                .build();

        Operations.complete(operation);
        session.flushSession();
        return session.popMissingBlocks();
    }

    public static void removeInventory(Clipboard clipboard) throws WorldEditException {
        for (BlockVector3 vector3 : clipboard.getRegion()) {
            BaseBlock block = clipboard.getFullBlock(vector3);
            if(block.getNbtData() != null && block.getNbtData().getListTag("Items") != null) {
                CompoundTag nbtData = block.getNbtData();
                Map<String, Tag> newNbtData = new HashMap<>(nbtData.getValue());
                newNbtData.remove("Items");
                CompoundTag compoundTag = nbtData.setValue(newNbtData);
                BaseBlock newBaseBlock = block.toBaseBlock(compoundTag);
                clipboard.setBlock(vector3, newBaseBlock);
            }
        }
    }
}
