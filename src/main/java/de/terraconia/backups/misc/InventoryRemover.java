package de.terraconia.backups.misc;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;

import java.util.HashMap;
import java.util.Map;

public class InventoryRemover {
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
