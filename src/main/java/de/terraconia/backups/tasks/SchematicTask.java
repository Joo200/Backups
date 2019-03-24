package de.terraconia.backups.tasks;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import de.terraconia.backups.CopyInterface;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SchematicTask extends AbstractTask {

    private Clipboard clipboard;
    private BlockVector3 origin;
    private World target;

    private Transform transform = null;

    public SchematicTask(JavaPlugin plugin, Player player, String tag, Clipboard clipboard, BlockVector3 origin, World target) {
        super(plugin, player, tag);
        this.clipboard = clipboard;
        this.origin = origin;
        this.target = target;
    }

    public void setTransform(Transform transform) {
        this.transform = transform;
    }

    public Transform getTransform() {
        return transform;
    }

    public BlockVector3 getOrigin() {
        return origin;
    }

    public Clipboard getClipboard() {
        return clipboard;
    }

    public World getTarget() {
        return target;
    }

    @Override
    public Region getAffectedRegion() {
        return clipboard.getRegion();
    }
}
