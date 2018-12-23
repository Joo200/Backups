package de.terraconia.backups.misc;

import com.sk89q.worldedit.world.block.BlockType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Johannes on 13.12.2018.
 */
public class RegionBlocks {
    private Map<BlockType, StorageBlocks> storageContent = new HashMap<>();

    public RegionBlocks() {

    }

    public void addBlock(BlockType type, Status status) {
        if(!storageContent.containsKey(type)) {
            storageContent.put(type, new StorageBlocks(type));
        }
        storageContent.get(type).addStorageElement(status, 1);
    }

    public Map<BlockType, StorageBlocks> getStorageContent() {
        return Collections.unmodifiableMap(storageContent);
    }

    public enum Status {
        PLACED,
        IN_BLOCKBAG,
        MISSING,
        DENIED,
        FREE,
    }

    public class StorageBlocks {
        private BlockType blockType;
        private Map<Status, Integer> storage;

        public StorageBlocks(BlockType blockType) {
            this.blockType = blockType;
            this.storage = new HashMap<>();
        }

        public void addStorageElement(Status status, int amount) {
            this.storage.merge(status, amount, Integer::sum);
        }

        public int get(Status status) {
            return this.storage.getOrDefault(status, 0);
        }

        public int getTotal() {
            return this.storage.values().stream().mapToInt(Integer::intValue).sum();
        }
    }
}
