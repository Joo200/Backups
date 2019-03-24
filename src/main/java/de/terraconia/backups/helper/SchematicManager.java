package de.terraconia.backups.helper;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.util.io.Closer;

import java.io.*;

public class SchematicManager {
    private String baseDir;
    private static final String FORMAT_NAME = "sponge";

    public SchematicManager(File pluginDirectory) {
        File file = new File(pluginDirectory.getAbsoluteFile() + "/snapshots/");
        file.mkdirs();
        this.baseDir = file.getAbsolutePath();
    }

    public void saveSubRegionSchematic(String path, Clipboard clipboard) throws IOException {
        File file = new File(path);
        if(!file.isAbsolute()) {
            file = new File(this.baseDir + path);
        }
        saveClipboard(clipboard, file);
    }

    public boolean hasSchematic(String path) {
        File file = new File(path);
        if(!file.isAbsolute()) {
            file = new File(this.baseDir + path);
        }
        return file.exists();
    }

    public Clipboard loadSchematic(String path) throws IOException {
        File file = new File(path);
        return getSchematic(file);
    }

    public void removeSchematic(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    private Clipboard getSchematic(File file) throws IOException {
        if(file == null || !file.exists()) {
            throw new IOException("No File found.");
        }
        ClipboardFormat clipboardFormat = ClipboardFormats.findByFile(file);
        if(clipboardFormat == null) {
            throw new IOException("No clipboard found by file name " + file.getAbsolutePath());
        }

        try(ClipboardReader reader = clipboardFormat.getReader(new FileInputStream(file))) {
            return reader.read();

        }
        /*
        Closer closer = Closer.create();
        FileInputStream fis = closer.register(new FileInputStream(file));
        BufferedInputStream bis = closer.register(new BufferedInputStream(fis));
        ClipboardReader reader = closer.register(clipboardFormat.getReader(bis));
        Clipboard clipboard = reader.read();
        closer.close();
        return clipboard;
        */
    }

    private void saveClipboard(Clipboard clipboard, File file) throws IOException {
        ClipboardFormat format = ClipboardFormats.findByAlias(FORMAT_NAME);
        if (format == null) {
            throw new IOException("Unknown schematic format: " + FORMAT_NAME);
        }
        File parent = file.getParentFile();
        if(parent != null && !parent.exists()) {
            if(!parent.mkdirs())
                throw new IOException("Could not create folder for schematics.");
        }

        try(ClipboardWriter writer = format.getWriter(new FileOutputStream(file))) {
            writer.write(clipboard);
        }
        /*
        Closer closer = Closer.create();
        FileOutputStream fos = closer.register(new FileOutputStream(file));
        BufferedOutputStream bos = closer.register(new BufferedOutputStream(fos));
        ClipboardWriter writer = closer.register(format.getWriter(bos));
        writer.write(clipboard);
        closer.close();
        */
    }
}
