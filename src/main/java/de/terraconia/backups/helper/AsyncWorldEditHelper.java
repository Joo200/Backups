/*
 * Copyright (c) 2016. Starlis LLC / dba Empire Minecraft
 *
 * This source code is proprietary software and must not be redistributed without Starlis LLC's approval
 *
 */

package de.terraconia.backups.helper;

import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.io.file.FilenameException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.api.utils.IFuncParamEx;
import org.primesoft.asyncworldedit.api.worldedit.ICancelabeEditSession;

public final class AsyncWorldEditHelper {
    static final AsyncWorldEditMain awe = (AsyncWorldEditMain) Bukkit.getPluginManager().getPlugin("AsyncWorldEdit");
    private static final WorldEdit worldEdit = WorldEdit.getInstance();

    private AsyncWorldEditHelper() {}

    static void setAweMode(Player player, boolean mode) {
        awe.getPlayerManager().getPlayer(player.getUniqueId()).setAweMode(mode);
    }

    public static class PasteAction implements IFuncParamEx<Integer, ICancelabeEditSession, MaxChangedBlocksException> {
        private final BlockVector3 origin;
        private final ClipboardHolder holder;
        private final boolean air;

        public PasteAction(BlockVector3 origin, ClipboardHolder holder, boolean air){
            this.holder = holder;
            this.air = air;
            this.origin = origin;
        }

        @Override
        public Integer execute(ICancelabeEditSession editSession) throws MaxChangedBlocksException {
            editSession.enableQueue();

            final Operation operation = this.holder
                .createPaste(editSession)
                .to(this.origin)
                .ignoreAirBlocks(this.air)
                .build();

            Operations.completeBlindly(operation);

            editSession.flushSession();

            return 32768;
        }
    }


}
