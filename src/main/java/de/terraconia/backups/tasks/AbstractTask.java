package de.terraconia.backups.tasks;

import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockType;
import de.terraconia.backups.CopyInterface;
import de.terraconia.backups.extensions.AbstractExtension;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractTask {
    private JavaPlugin plugin;
    private Player player;
    private String tag;
    // Nullable
    private BlockBag blockBag = null;
    private Map<BlockType, Integer> missingBlocks;
    // Nullable
    private Mask mask = null;
    private boolean ignoreAir = false;



    List<AbstractExtension> extensions = new ArrayList<>();

    public AbstractTask(JavaPlugin plugin, Player player, String tag) {
        this.plugin = plugin;
        this.player = player;
        this.tag = tag;
    }

    public abstract Region getAffectedRegion();

    public void setMask(Mask mask) {
        this.mask = mask;
    }

    public void setBlockBag(BlockBag blockBag) {
        this.blockBag = blockBag;
    }

    public BlockBag getBlockBag() {
        return blockBag;
    }

    public void setMissingBlocks(Map<BlockType, Integer> missingBlocks) {
        this.missingBlocks = missingBlocks;
    }

    public Map<BlockType, Integer> getMissingBlocks() {
        return missingBlocks;
    }

    public Mask getMask() {
        return mask;
    }

    public boolean isIgnoreAir() {
        return ignoreAir;
    }

    public void setIgnoreAir(boolean ignoreAir) {
        this.ignoreAir = ignoreAir;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public Player getPlayer() {
        return player;
    }

    public String getTag() {
        return tag;
    }

    public List<AbstractExtension> getExtensions() {
        return extensions;
    }

    public void addExtension(AbstractExtension extension) {
        this.extensions.add(extension);
    }
}
