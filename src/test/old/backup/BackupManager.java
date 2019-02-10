package de.terraconia.backups.old.backup;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.Tag;
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
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terraconia.backups.blockbag.CityBlockBag;
import de.terraconia.backups.misc.RegionBlocks;
import de.terraconia.backups.plugin.BackupPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static de.terraconia.backups.helper.WorldEditHelper.toRegion;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;

public abstract class BackupManager {


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
        deniedBlocks.addAll(Arrays.stream(notAllowedBlockTypes).map(BukkitAdapter::asBlockType).collect(Collectors.toSet()));
        deniedBlocks.addAll(Arrays.stream(Material.values()).filter(mat -> !(mat.createBlockData() instanceof Ageable))
                .map(BukkitAdapter::asBlockType).collect(Collectors.toSet()));
    }

    public abstract void backupSubRegion(
            JavaPlugin requester,
            ProtectedRegion protectedRegion,
            World world,
            String schematicPath) throws IOException, WorldEditException;

    public abstract CompletableFuture<Map<BlockType, Integer>> restoreSubRegion(
            JavaPlugin requester,
            Player player,
            ProtectedRegion subRegion,
            Set<Location> cityChestLocations,
            World world,
            String schematicPath);

    public abstract CompletableFuture<Map<BlockType, Integer>> restoreSubRegion(
            JavaPlugin requester,
            Player player,
            ProtectedRegion subRegion,
            CityBlockBag bag,
            World world,
            String schematicPath);


    private static boolean findMaterial(BlockType type, Map<Material, Integer> amountByType) {
        Material adapt = BukkitAdapter.adapt(type);
        boolean containsMaterial = amountByType.containsKey(adapt);
        amountByType.computeIfPresent(adapt, (material, amount) -> (--amount == 0) ? null : amount);
        return containsMaterial;
    }

    public CompletableFuture<RegionBlocks> getNeededMaterials(World world,
                                                              ProtectedRegion region,
                                                              Set<Location> cityChests,
                                                              String schematicPath)
            throws IOException, MaxChangedBlocksException {

        Map<Material, Integer> amountByType = cityChests.stream()
                .map(location -> location.getBlock().getState())
                .filter(c -> c instanceof Container)
                .map(c -> ((Container) c).getSnapshotInventory())
                .map(Inventory::getContents)
                .flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .collect(groupingBy(
                        ItemStack::getType,
                        () -> new EnumMap<>(Material.class),
                        summingInt(ItemStack::getAmount)
                ));

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
                BlockType currentBlock = clipboard.getBlock(blockVector3).getBlockType();
                if (newBlock.equals(currentBlock)) {
                    blocks.addBlock(newBlock, RegionBlocks.Status.PLACED);
                } else if (deniedBlocks.contains(newBlock)) {
                    blocks.addBlock(newBlock, RegionBlocks.Status.DENIED);
                } else if (freeBlocks.contains(newBlock)) {
                    blocks.addBlock(newBlock, RegionBlocks.Status.FREE);
                } else if (findMaterial(newBlock, amountByType)) {
                    blocks.addBlock(newBlock, RegionBlocks.Status.IN_BLOCKBAG);
                } else {
                    blocks.addBlock(newBlock, RegionBlocks.Status.MISSING);
                }
            }
            future.complete(blocks);
        });
        return future;
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
