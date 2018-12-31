package de.terraconia.backups.manager;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terraconia.backups.misc.RegionBlocks;
import de.terraconia.backups.plugin.BackupPlugin;
import de.terraconia.backups.plugin.SchematicManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public abstract class BackupManager {

    private static final Material[] defaultFreeBlockTypes = {
            Material.AIR,
            Material.DIRT,
            Material.GRASS,
            Material.STONE,
            //Material.SAND.ordinal(),
            Material.WATER,
            Material.FIRE,
            Material.COBBLESTONE
    };
    private static final Material[] notAllowedBlockTypes = {
            // sowie Ageable
            Material.BEDROCK,
            Material.PUMPKIN,
            Material.MELON,
    };
    private SchematicManager schematicManager;

    private Set<BlockType> freeBlocks = new HashSet<>();
    private Set<BlockType> deniedBlocks = new HashSet<>();

    private BackupPlugin plugin;

    public BackupManager(BackupPlugin plugin) {
        this.schematicManager = new SchematicManager(plugin.getDataFolder());
        this.plugin = plugin;
/*
        ConfigurationSection subRegionResetSection = config.getConfigurationSection("Modules.SubRegionReset");
        if (subRegionResetSection == null)
            subRegionResetSection = config.createSection("Modules.SubRegionReset");
*/
        List<Material> allowedBlocks = null; //(List<Material>)subRegionResetSection.getList("AllowedBlockTypes");
        if (allowedBlocks == null || allowedBlocks.isEmpty()) {
            freeBlocks.addAll(Arrays.stream(defaultFreeBlockTypes).map(BukkitAdapter::asBlockType).collect(Collectors.toSet()));
        } else {
            freeBlocks.addAll(allowedBlocks.stream().map(BukkitAdapter::asBlockType).collect(Collectors.toSet()));
        }
    }

    public abstract void backupSubRegion(
                                        JavaPlugin requester,
                                        ProtectedRegion protectedRegion,
                                        World world,
                                        String schematicPath) throws IOException, WorldEditException;

    public abstract Map<BlockType, Integer> restoreSubRegion(
            JavaPlugin requester,
            ProtectedRegion subRegion,
            Set<Location> cityChestLocations,
            World world,
            // int worldId,
            boolean ignoreChests,
            String schematicPath) throws WorldEditException, IOException;


    public static Region toRegion(World world, ProtectedRegion region) {
        if(region instanceof ProtectedCuboidRegion) {
            BlockVector3 min = region.getMinimumPoint();
            BlockVector3 max = region.getMaximumPoint();
            return new CuboidRegion(world, min, max);
        } else if(region instanceof ProtectedPolygonalRegion) {
            return new Polygonal2DRegion(world, region.getPoints(),
                    region.getMinimumPoint().getY(), region.getMaximumPoint().getY());
        } else {
            throw new RuntimeException("Unknown region type: " + region.getClass().getCanonicalName());
        }
    }

    private static boolean findMaterial(BlockType type, List<ItemStack> itemStacks) {
        for(ItemStack is : itemStacks) {
            if (!BukkitAdapter.equals(type, is.getType())) {
                // Type id doesn't fit
                continue;
            }
            int amount = is.getAmount();
            if(amount > 1) {
                is.setAmount(amount-1);
                return true;
            } else if(amount == 1) {
                itemStacks.remove(is);
                return true;
            }
        }
        return false;
    }

    public CompletableFuture<RegionBlocks> getNeededMaterials(World world,
                                   ProtectedRegion region,
                                   Set<Location> cityChests,
                                   String schematicPath)
            throws IOException, MaxChangedBlocksException {
        // Getting a set of containers for the items.
        List<ItemStack> itemStacks = cityChests.stream()
                .map(location -> location.getBlock().getState())
                .filter(c -> c instanceof Container)
                .map(c -> ((Container) c).getSnapshotInventory())
                .map(Inventory::getContents)
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());

        Clipboard toCopy = schematicManager.loadSchematic(schematicPath);

        Region weRegion = toRegion(world, region);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(weRegion);
        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1);
        ForwardExtentCopy copy = new ForwardExtentCopy(editSession, weRegion, clipboard, weRegion.getMinimumPoint());
        copy.setCopyingEntities(false);
        Operations.completeLegacy(copy);

        CompletableFuture<RegionBlocks> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            RegionBlocks blocks = new RegionBlocks();
            for (BlockVector3 blockVector3 : weRegion) {
                BlockType newBlock = toCopy.getBlock(blockVector3).getBlockType();
                BlockType currentBlock = world.getBlock(blockVector3).getBlockType();
                if(newBlock.equals(currentBlock)) {
                    blocks.addBlock(newBlock, RegionBlocks.Status.PLACED);
                    continue;

                }
                if(deniedBlocks.contains(newBlock)) {
                    blocks.addBlock(newBlock, RegionBlocks.Status.DENIED);
                    continue;
                }
                if(freeBlocks.contains(newBlock)) {
                    blocks.addBlock(newBlock, RegionBlocks.Status.FREE);
                    continue;
                }
                if(findMaterial(newBlock, itemStacks)) {
                    blocks.addBlock(newBlock, RegionBlocks.Status.IN_BLOCKBAG);
                    continue;
                }
                blocks.addBlock(newBlock, RegionBlocks.Status.MISSING);
            }
            future.complete(blocks);
        });
        return future;
    };

    public boolean hasBackup(String path) {
        return getSchematicManager().hasSchematic(path);
    }

    public void removeBackup(String schematicPath) {
        schematicManager.removeSchematic(schematicPath);
    }

    public SchematicManager getSchematicManager() {
        return schematicManager;
    }

    public Set<BlockType> getDeniedBlocks() {
        return deniedBlocks;
    }

    public Set<BlockType> getFreeBlocks() {
        return freeBlocks;
    }
}
