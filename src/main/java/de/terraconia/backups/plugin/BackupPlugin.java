package de.terraconia.backups.plugin;

import de.terraconia.backups.helper.SchematicManager;
import de.terraconia.backups.impl.AsyncWorldEditImpl;
import de.terraconia.backups.impl.WorldEditImpl;
import de.terraconia.backups.manager.BackupManager;
import de.terraconia.backups.Commands;
import de.terraconia.backups.extensions.DynmapExtension;
//import de.terraconia.backups.extensions.LWCExtension;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.bukkit.DynmapPlugin;

public class BackupPlugin extends JavaPlugin {
    private static BackupManager manager;
    private static WorldEditImpl worldEdit;
    private static AsyncWorldEditImpl asyncWorldEdit;
    private static SchematicManager schematicManager;

    @Override
    public void onEnable() {
        worldEdit = new WorldEditImpl();
        asyncWorldEdit = new AsyncWorldEditImpl();
        schematicManager = new SchematicManager(this.getDataFolder());
        manager = new BackupManager(worldEdit, asyncWorldEdit, schematicManager);
        Commands commands = new Commands(this);
        getCommand("backupmanager").setExecutor(commands);
    }

    public static BackupManager getManager() {
        return manager;
    }

    public static AsyncWorldEditImpl getAsyncWorldEdit() {
        return asyncWorldEdit;
    }

    public static WorldEditImpl getWorldEdit() {
        return worldEdit;
    }

    public static SchematicManager getSchematicManager() {
        return schematicManager;
    }
}
