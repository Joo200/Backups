package de.terraconia.backups.manager;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.NullExtent;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.function.mask.BlockTypeMask;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terraconia.backups.blockbag.CityBlockBag;
import de.terraconia.backups.events.RegionEvent;
import de.terraconia.backups.events.RegionFinishEvent;
import de.terraconia.backups.extensions.AbstractExtension;
import de.terraconia.backups.helper.SchematicManager;
import de.terraconia.backups.impl.WorldEditImpl;
import de.terraconia.backups.misc.RegionBlocks;
import de.terraconia.backups.plugin.BackupPlugin;
import de.terraconia.backups.tasks.SchematicToRegionTask;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static de.terraconia.backups.constants.BlockConstants.defaultFreeBlockTypes;
import static de.terraconia.backups.constants.BlockConstants.notAllowedBlockTypes;
import static de.terraconia.backups.helper.WorldEditHelper.toRegion;
import static de.terraconia.backups.misc.InventoryRemover.removeInventory;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;

public class BackupManager extends AbstractManager {

    private List<BlockType> defaultBlocked = new ArrayList<>();
    private List<BlockType> defaultFree = new ArrayList<>();

    public BackupManager(WorldEditImpl worldEdit, SchematicManager schematicManager) {
        super(worldEdit, schematicManager);

        defaultFree.addAll(Arrays.stream(defaultFreeBlockTypes).map(BukkitAdapter::asBlockType).collect(Collectors.toSet()));
        defaultBlocked.addAll(Arrays.stream(notAllowedBlockTypes).map(BukkitAdapter::asBlockType).collect(Collectors.toSet()));
        BlockType.REGISTRY.forEach(blockType -> {
            if(blockType.getPropertyMap().containsKey("age"))
                defaultBlocked.add(blockType);
        });
    }

    public BlockBag getBlockBag(Collection<Location> locs) {
        Set<Container> chests = locs.stream().map(Location::getBlock).map(Block::getState)
                .filter(c -> c instanceof Container).map(c -> (Container)c).collect(Collectors.toSet());
        return new CityBlockBag(chests, defaultBlocked, defaultFree);
    }

    public CompletableFuture<Boolean> restoreRegion(JavaPlugin requester, Player player,
                                                                    String schematic, World world, ProtectedRegion target,
                                                                    BlockBag bag, AbstractExtension ... extensions) {
        Clipboard clipboard;
        try {
            clipboard = schemManager.loadSchematic(schematic);
            removeInventory(clipboard);
        } catch (IOException | WorldEditException e) {
            return CompletableFuture.failedFuture(e);
        }
        SchematicToRegionTask task = new SchematicToRegionTask(requester, player, "Backup", clipboard, world, target);
        task.setBlockBag(bag);
        task.setMask(Masks.negate(new BlockTypeMask(new NullExtent(), defaultBlocked)));
        task.getExtensions().addAll(Arrays.asList(extensions));

        RegionEvent restoreEvent = new RegionEvent(task);
        Bukkit.getPluginManager().callEvent(restoreEvent);
        if(restoreEvent.isCancelled()) return CompletableFuture.failedFuture(new InterruptedException());

        task.getExtensions().forEach(extension -> extension.preExecute(task));
        CompletableFuture<Boolean> future;
        if(clipboard.getRegion().getArea() > maxWorldEditBlockAmount) {
            player.sendMessage(ChatColor.RED + "Dein Grundstück ist zu groß. Grundstücke über " +  maxWorldEditBlockAmount +" Blöcke wieder herzustellen ist nicht möglich.");
            return CompletableFuture.failedFuture(new InterruptedException());
            //requester.getLogger().info("Using AWE for completing task \"" + task.getTag() + "\".");
            //future = asyncWorldEdit.copySchematic(task);
        } else {
            requester.getLogger().info("Using WorldEdit for completing task \"" + task.getTag() + "\".");
            future = worldEdit.copySchematic(task);
        }
        future.thenApply(blocks -> {
            task.getExtensions().forEach(extension -> extension.postExecute(task));
            return blocks;
        });
        return future;
    }


    public CompletableFuture<RegionBlocks> getNeededMaterial
            (JavaPlugin plugin, World world, ProtectedRegion protectedRegion, String file, Collection<Location> cityChests) {
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

        Clipboard toCopy;
        try {
            toCopy = schemManager.loadSchematic(file);
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }

        Region weRegion = toRegion(world, protectedRegion);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(weRegion);
        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1);
        ForwardExtentCopy copy = new ForwardExtentCopy(editSession, weRegion, clipboard, weRegion.getMinimumPoint());
        copy.setCopyingEntities(false);
        try {
            Operations.completeLegacy(copy);
        } catch (MaxChangedBlocksException e) {
            return CompletableFuture.failedFuture(e);
        }

        CompletableFuture<RegionBlocks> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            RegionBlocks blocks = new RegionBlocks();
            for (BlockVector3 blockVector3 : weRegion) {
                BlockType newBlock = toCopy.getBlock(blockVector3).getBlockType();
                BlockType currentBlock = clipboard.getBlock(blockVector3).getBlockType();
                if (defaultBlocked.contains(newBlock)) {
                    blocks.addBlock(newBlock, RegionBlocks.Status.DENIED);
                } else if (newBlock.equals(currentBlock)) {
                    blocks.addBlock(newBlock, RegionBlocks.Status.PLACED);
                } else if (defaultFree.contains(newBlock)) {
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

    private static boolean findMaterial(BlockType type, Map<Material, Integer> amountByType) {
        Material adapt = BukkitAdapter.adapt(type);
        boolean containsMaterial = amountByType.containsKey(adapt);
        amountByType.computeIfPresent(adapt, (material, amount) -> (--amount == 0) ? null : amount);
        return containsMaterial;
    }
}
