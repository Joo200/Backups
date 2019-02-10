package de.terraconia.backups.tasks;

import com.sk89q.worldedit.regions.Region;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CopyPasteTask extends AbstractTask {
    public CopyPasteTask(JavaPlugin plugin, Player player, String tag) {
        super(plugin, player, tag);
    }

    @Override
    public Region getAffectedRegion() {
        return null;
    }
}
