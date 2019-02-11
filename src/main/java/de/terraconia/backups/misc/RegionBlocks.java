package de.terraconia.backups.misc;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BlockType;
import de.baba43.lib.helper.TranslationHelper;
import de.baba43.lib.helper.textcomponents.ComponentInfoBuilder;
import de.baba43.lib.helper.textcomponents.PageHeader;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

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


    public void send(Player player, int page, String header, String command) {
        int max = (getStorageContent().size() + 15 - 1) / 15;
        if(max >= page) page = 1;
        int offset = (page-1)*15;
        new PageHeader(header, command, page, max).send(player);
        getStorageContent().entrySet().stream().
                filter(entry -> entry.getValue().get(Status.DENIED) == 0).
                sorted(Collections.reverseOrder(
                        Comparator.comparingInt(entry -> entry.getValue().get(RegionBlocks.Status.MISSING)))).
                skip(offset).limit(15).
                forEach((entry) -> {
                    RegionBlocks.StorageBlocks storage = entry.getValue();
                    Material mat = BukkitAdapter.adapt(entry.getKey());
                    String translate = TranslationHelper.toTranslatableComponent(mat);
                    if(translate == null) translate = entry.getKey().getId();
                    TranslatableComponent translation = new TranslatableComponent(translate);
                    int missing = storage.get(RegionBlocks.Status.MISSING);
                    int free = storage.get(RegionBlocks.Status.FREE);
                    int placed = storage.get(RegionBlocks.Status.PLACED);
                    int needed = missing + free + placed;
                    ComponentInfoBuilder hover =
                            new ComponentInfoBuilder("Fehlend", String.valueOf(missing))
                            .appendInfo("Kostenlos", String.valueOf(free))
                            .appendInfo("schon platziert", String.valueOf(placed))
                            .appendInfo("gesamt benötigt", String.valueOf(needed));
                    HoverEvent hEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover.getBuilder().create());
                    translation.setHoverEvent(hEvent);
                    translation.setColor(missing > 0 ? ChatColor.RED : ChatColor.GREEN);
                    player.spigot().sendMessage(new ComponentBuilder(translation)
                                    .append(": " + missing).color(ChatColor.GRAY).event(hEvent).create());
                });
    }
}
