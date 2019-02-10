package de.terraconia.backups.extensions;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import de.baba43.lib.helper.TranslationHelper;
import de.baba43.lib.helper.textcomponents.ComponentInfoBuilder;
import de.terraconia.backups.tasks.AbstractTask;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PopMissingBlockExtension extends AbstractExtension {
    public static PopMissingBlockExtension DEFAULT = new PopMissingBlockExtension();

    private PopMissingBlockExtension() {}

    @Override
    public void postExecute(AbstractTask task) {
        Player player = task.getPlayer();
        if(task.getMissingBlocks().isEmpty()) {
            player.spigot().sendMessage(new ComponentBuilder("Der Task " + task.getTag() +
                    " wurde erfolgreich ausgeführt.").color(ChatColor.GRAY).create());
            return;
        }
        ComponentInfoBuilder infoBuilder = new ComponentInfoBuilder();
        infoBuilder.appendInfo("Fehlende Materialien bei Task " + task.getTag() + ":");
        task.getMissingBlocks().forEach(
            (blockType, amount) -> {
                Material mat = BukkitAdapter.adapt(blockType);
                String translate = TranslationHelper.toTranslatableComponent(mat);
                if (translate == null) translate = blockType.getId();
                TranslatableComponent translation = new TranslatableComponent(translate);
                infoBuilder.newLine().getBuilder().reset();
                infoBuilder.append(translation).append(new TextComponent(": " + amount));
            });
        player.spigot().sendMessage(infoBuilder.getBuilder().create());
    }
}
