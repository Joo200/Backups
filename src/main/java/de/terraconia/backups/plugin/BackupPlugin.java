package de.terraconia.backups.plugin;

import de.terraconia.backups.Commands;
import de.terraconia.backups.extensions.DynmapExtension;
//import de.terraconia.backups.extensions.LWCExtension;
import de.terraconia.backups.manager.AweBackupManager;
import de.terraconia.backups.manager.BackupManager;
import de.terraconia.backups.manager.WorldEditBackupManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.bukkit.DynmapPlugin;

public class BackupPlugin extends JavaPlugin {
    private static BackupManager manager;

    @Override
    public void onEnable() {
        manager = new AweBackupManager(this);
        Commands commands = new Commands(this);
        getCommand("backupmanager").setExecutor(commands);
/*
        try {
            //LWC ignored = LWC.getInstance();
            Bukkit.getPluginManager().registerEvents(new LWCExtension(), this);
        } catch (Exception e) {
            getLogger().warning("LWC not found.");
        }*/

        try {
            DynmapPlugin ignored = DynmapPlugin.plugin;
            Bukkit.getPluginManager().registerEvents(new DynmapExtension(), this);
        } catch (Exception e) {
            getLogger().warning("LWC not found.");
        }
    }

    public static BackupManager getManager() {
        return manager;
    }
}
