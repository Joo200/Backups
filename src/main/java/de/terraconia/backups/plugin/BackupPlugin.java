package de.terraconia.backups.plugin;

import de.terraconia.backups.manager.BackupManager;
import de.terraconia.backups.Commands;
import de.terraconia.backups.extensions.DynmapExtension;
//import de.terraconia.backups.extensions.LWCExtension;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.bukkit.DynmapPlugin;

public class BackupPlugin extends JavaPlugin {
    private static BackupManager manager;

    @Override
    public void onEnable() {
        manager = new BackupManager(this);
        Commands commands = new Commands(this);
        getCommand("backupmanager").setExecutor(commands);
    }

    public static BackupManager getManager() {
        return manager;
    }
}
