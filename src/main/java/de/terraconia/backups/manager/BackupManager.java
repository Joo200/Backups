package de.terraconia.backups.manager;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
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

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Johannes on 13.12.2018.
 */
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

    public BackupManager(BackupPlugin plugin) {
        this.schematicManager = new SchematicManager(plugin.getDataFolder());
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

    protected abstract void backupSubRegion(ProtectedRegion protectedRegion, World world, String schematicPath) throws IOException, WorldEditException;
    public abstract Map<BlockType, Integer> restoreSubRegion(
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

    public RegionBlocks getNeededMaterials(World world,
                                           ProtectedRegion region,
                                           Set<Location> cityChests,
                                           String schematicPath) throws IOException {
        RegionBlocks blocks = new RegionBlocks();
        // Getting a set of containers for the items.
        Set<Container> chests = cityChests.stream().map(Location::getBlock).map(Block::getState)
                .filter(c -> c instanceof Container).map(c -> (Container)c).collect(Collectors.toSet());
        Clipboard toCopy = schematicManager.loadSchematic(schematicPath);

        List<Inventory> collect = chests.stream().map(Container::getSnapshotInventory).map(Arrays::asList)
                .collect(Collectors.toList()).stream().flatMap(Collection::stream).collect(Collectors.toList());
        List<ItemStack> itemStacks = collect.stream().map(Inventory::getContents).flatMap(Arrays::stream)
                .filter(Objects::nonNull).collect(Collectors.toList());

        Region weRegion = toRegion(world, region);
        for (BlockVector3 blockVector3 : weRegion) {
            Bukkit.getLogger().info("Now looking: " + blockVector3.toString());
            BlockType clipboard = toCopy.getBlock(blockVector3).getBlockType();
            BlockType currentBlock = world.getBlock(blockVector3).getBlockType();
            Bukkit.getLogger().info(currentBlock.getName() + " -> " + clipboard.getName());
            if(clipboard.equals(currentBlock)) {
                blocks.addBlock(clipboard, RegionBlocks.Status.PLACED);
                continue;

            }
            if(deniedBlocks.contains(clipboard)) {
                blocks.addBlock(clipboard, RegionBlocks.Status.DENIED);
                continue;
            }
            if(freeBlocks.contains(clipboard)) {
                blocks.addBlock(clipboard, RegionBlocks.Status.FREE);
                continue;
            }
            if(findMaterial(clipboard, itemStacks)) {
                blocks.addBlock(clipboard, RegionBlocks.Status.IN_BLOCKBAG);
                continue;
            }
            blocks.addBlock(clipboard, RegionBlocks.Status.MISSING);
        }
        return blocks;
    }

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
