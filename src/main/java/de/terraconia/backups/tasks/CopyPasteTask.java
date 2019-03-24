package de.terraconia.backups.tasks;

import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CopyPasteTask extends AbstractTask {
    Region start;
    World target;

    public CopyPasteTask(JavaPlugin plugin, Player player, Region start, World target, String tag) {
        super(plugin, player, tag);
        this.start = start;
        this.target = target;
    }

    @Override
    public Region getAffectedRegion() {
        Region reg = start.clone();
        reg.setWorld(target);
        return reg;
    }
}
