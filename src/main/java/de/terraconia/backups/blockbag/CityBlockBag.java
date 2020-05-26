package de.terraconia.backups.blockbag;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.extent.inventory.BlockBagException;
import com.sk89q.worldedit.extent.inventory.OutOfBlocksException;
import com.sk89q.worldedit.extent.inventory.UnplaceableBlockException;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.inventory.ItemStack;

import java.util.*;


public class CityBlockBag extends BlockBag {
    private Map<Container, ItemStack[]> inventories = new HashMap<>();
    private final Collection<BlockType> dontReplace;
    private final Collection<BlockType> replaceFree;

    public CityBlockBag(Set<Container> chests,
                        Collection<BlockType> dontReplace,
                        Collection<BlockType> replaceFree) {
        chests.forEach(c -> inventories.put(c, null));
        this.dontReplace = dontReplace;
        this.replaceFree = replaceFree;
    }

    private void loadInventory() {
        if(inventories.isEmpty()) return;
        for(Map.Entry<Container, ItemStack[]> inv : inventories.entrySet()) {
            inv.getKey().getWorld().loadChunk(inv.getKey().getLocation().getChunk());
            if(inv.getValue() == null) {
                ItemStack[] is = inv.getKey().getInventory().getContents();
                inv.setValue(is);
            }
        }
    }

    @Override
    public void fetchBlock(BlockState blockState) throws BlockBagException {
        BlockType type = blockState.getBlockType();
        if(dontReplace.contains(type)) {
            throw new UnplaceableBlockException();
        }
        if(replaceFree.contains(type)) {
            return;
        }

        loadInventory();

        int needed = 1;

        BlockType blockType = blockState.getBlockType();
        Map<String, ? extends Property<?>> properties = blockType.getPropertyMap();
        Property<?> property = properties.get("type");
        if(property != null) {
            // workaround für doppelte Slabs
            if(!blockState.getBlockType().equals(BlockTypes.CHEST) &&
                    !blockState.getBlockType().equals(BlockTypes.TRAPPED_CHEST) &&
                    blockState.getState(property).equals("double")) {
                needed += 1;
            }
        }

        for (Map.Entry<Container, ItemStack[]> itemsEntry : inventories.entrySet()) {
            ItemStack[] items = itemsEntry.getValue();
            for (int slot = 0; slot < items.length; ++slot) {
                ItemStack bukkitItem = items[slot];

                if (bukkitItem == null) {
                    continue;
                }

                if (!BukkitAdapter.equals(blockState.getBlockType(), bukkitItem.getType())) {
                    // Type id doesn't fit
                    continue;
                }


                int currentAmount = bukkitItem.getAmount();
                if (currentAmount < 0) {
                    // Unlimited
                    return;
                }
                if (currentAmount > needed) {
                    bukkitItem.setAmount(currentAmount - 1);
                    return;
                } else if(currentAmount < needed) {
                    needed -= currentAmount;
                    items[slot] = null;
                } else { // if currentAmount == needed
                    items[slot] = null;
                    return;
                }
            }
        }
        throw new OutOfBlocksException();
    }

    @Override
    public void storeBlock(BlockState blockState, int amount) throws BlockBagException {
        // Do nothing, we don't want to store blocks
    }

    @Override
    public void flushChanges() {
        for(Map.Entry<Container, ItemStack[]> inv : inventories.entrySet()) {
            if(inv.getValue() == null) continue;
            inv.getKey().getWorld().loadChunk(inv.getKey().getLocation().getChunk());
            inv.getKey().getInventory().setContents(inv.getValue());
            inv.setValue(null);
        }
    }

    @Override
    public void addSourcePosition(Location pos) {

    }

    @Override
    public void addSingleSourcePosition(Location pos) {

    }

}
