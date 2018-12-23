package de.terraconia.backups.plugin;

import de.terraconia.backups.Commands;
import de.terraconia.backups.manager.WorldEditBackupManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Johannes on 24.11.2018.
 */
public class BackupPlugin extends JavaPlugin {
    private WorldEditBackupManager manager;

    @Override
    public void onEnable() {
        manager = new WorldEditBackupManager(this);
        Commands commands = new Commands(this);
        getCommand("backupmanager").setExecutor(commands);
    }

    public WorldEditBackupManager getManager() {
        return manager;
    }
}
