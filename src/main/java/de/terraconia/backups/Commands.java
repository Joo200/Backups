package de.terraconia.backups;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.terraconia.backups.misc.RegionBlocks;
import de.terraconia.backups.plugin.BackupPlugin;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Created by Johannes on 24.11.2018.
 */
public class Commands implements CommandExecutor {
    private BackupPlugin plugin;

    public Commands(BackupPlugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length < 2) {
            commandSender.sendMessage("2 Argumente erfordert.");
            return true;
        }
        if(!(commandSender instanceof Player)) {
            return true;
        }
        Player player = (Player)commandSender;
        BukkitPlayer lp = BukkitAdapter.adapt(player);
        RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(lp.getWorld());
        if(rm == null) {
            player.sendMessage("RM null.");
            return true;
        }
        ProtectedRegion rg = rm.getRegion(strings[1]);
        if(rg == null) {
            player.sendMessage("Region NULL.");
            return true;
        }
        if(strings[0].equalsIgnoreCase("save")) {
            //SurvivalSubRegion subRegion = SurvivalPlugin.getInstance().getSurvivalRegionManager().getSubRegionByID(id);
            try {
                String path = plugin.getDataFolder().getAbsolutePath() + "/snapshots/" + strings[1] + ".schematic";
                plugin.getManager().backupSubRegion(rg, lp.getWorld(), path);

                commandSender.sendMessage("Erfolgreich.");
            } catch (IOException | WorldEditException e) {
                e.printStackTrace();
                commandSender.sendMessage("Fehler.");
            }
        } else if(strings[0].equalsIgnoreCase("load") || strings[0].equalsIgnoreCase("needed")) {
            if(strings.length < 5) {
                commandSender.sendMessage("5 Argumente erfordert.");
                return true;
            }

            int x, y, z;
            try {
                x = Integer.parseInt(strings[2]);
                y = Integer.parseInt(strings[3]);
                z = Integer.parseInt(strings[4]);
            } catch (NumberFormatException ex){
                commandSender.sendMessage("Keine Zahl du Vogel.");
                return true;
            }
            player.sendMessage("Chest: " + x + ", " + y + ", " + z);
            Set<Location> loc = Collections.singleton(new Location(player.getWorld(), x, y, z));
            //SurvivalSubRegion subRegion = SurvivalPlugin.getInstance().getSurvivalRegionManager().getSubRegionByID(id);
            try {
                String path = plugin.getDataFolder().getAbsolutePath() + "/snapshots/" + strings[1] + ".schematic";
                if(strings[0].equalsIgnoreCase("load")) {
                    plugin.getManager().restoreSubRegion(rg, loc, lp.getWorld(), false, path)
                            .forEach(((blockType, integer) -> {
                        player.sendMessage("BlockType: " + blockType.getId() + ": " + integer);
                    }));
                }
                else {
                    player.sendMessage("Type        - PLACED - IN BAG - DENIED - MISSING - FREE.");
                    plugin.getManager().getNeededMaterials(lp.getWorld(), rg, loc, path).getStorageContent().forEach(
                            (type, storage) -> {
                                player.sendMessage(
                                        String.format("%1$-15s", type.getName()) +
                                        " - " + storage.get(RegionBlocks.Status.PLACED) +
                                        " - " + storage.get(RegionBlocks.Status.IN_BLOCKBAG) +
                                        " - " + storage.get(RegionBlocks.Status.DENIED) +
                                        " - " + storage.get(RegionBlocks.Status.MISSING) +
                                        " - " + storage.get(RegionBlocks.Status.FREE));
                            }
                    );

                    return true;
                }
                commandSender.sendMessage("Erfolgreich.");
            } catch (IOException e) {
                e.printStackTrace();
                commandSender.sendMessage("Fehler.");
            } catch (MaxChangedBlocksException e) {
                e.printStackTrace();
                commandSender.sendMessage("Zu viele Bloecke.");
            } catch (WorldEditException e) {
                commandSender.sendMessage("WE Fehler.");
                e.printStackTrace();
            }

        }

        return true;
    }
}
