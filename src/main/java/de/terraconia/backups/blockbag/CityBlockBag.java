package de.terraconia.backups.blockbag;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.extent.inventory.BlockBagException;
import com.sk89q.worldedit.extent.inventory.OutOfBlocksException;
import com.sk89q.worldedit.extent.inventory.UnplaceableBlockException;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import org.bukkit.Bukkit;
import org.bukkit.block.Container;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


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

        for (ItemStack[] items : inventories.values()) {
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
                if (currentAmount > 1) {
                    //Bukkit.getLogger().info("old: " + entry.getKey().getInventory().getItem(slot).getAmount());
                    bukkitItem.setAmount(currentAmount - 1);
                    //Bukkit.getLogger().info("old: " + entry.getKey().getInventory().getItem(slot).getAmount());
                    return;
                } else {
                    items[slot] = null;
                    return;
                }
            }
        }
        throw new OutOfBlocksException();
    }

    private int removeItem(ItemStack is) {
        int finalAmount = 0;
        for (ItemStack[] items : inventories.values()) {
            for (int slot = 0; slot < items.length; ++slot) {
                ItemStack bukkitItem = items[slot];
                if (bukkitItem == null) {
                    continue;
                }
                if (!is.isSimilar(bukkitItem)) continue;
                int currentAmount = bukkitItem.getAmount();
                if (currentAmount < 0) {
                    // Unlimited
                    return is.getAmount();
                }
                int missing = is.getAmount()-finalAmount;
                if (currentAmount > missing) {
                    bukkitItem.setAmount(currentAmount - missing);
                    finalAmount = is.getAmount();
                    return finalAmount;
                } else {
                    items[slot] = null;
                    finalAmount += currentAmount;
                }
            }
        }
        return finalAmount;
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
