package de.terraconia.backups.extensions;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import de.terraconia.backups.tasks.AbstractTask;
import de.terraconia.core.lib.helper.TranslationHelper;
import de.terraconia.core.lib.helper.textcomponents.ComponentInfoBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
        player.sendMessage(Component.text("Fehlende Materialien bei Task " + task.getTag() + ":", NamedTextColor.GRAY));
        task.getMissingBlocks().forEach(
            (blockType, amount) -> {
                Material mat = BukkitAdapter.adapt(blockType);
                String translate = TranslationHelper.toTranslatableComponent(mat);
                if (translate == null) translate = blockType.getId();
                Component comp = Component.translatable(translate)
                        .append(Component.text(": " + amount));
                player.sendMessage(comp);
            });
    }
}
