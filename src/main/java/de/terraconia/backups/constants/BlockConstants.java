package de.terraconia.backups.constants;

import org.bukkit.Material;

public class BlockConstants {
    public static final Material[] defaultFreeBlockTypes = {
            Material.AIR,
            Material.DIRT,
            Material.GRASS,
            Material.GRASS_BLOCK,
            Material.FARMLAND,
            Material.GRASS_PATH,
            Material.STONE,
            //Material.SAND.ordinal(),
            Material.WATER,
            Material.FIRE,
            Material.COBBLESTONE,
            Material.ANDESITE,
            Material.DIORITE,
            Material.GRANITE
    };
    public static final Material[] notAllowedBlockTypes = {
            // sowie Ageable
            Material.BEDROCK,
            Material.PUMPKIN,
            Material.MELON,
            Material.BLACK_BED,
            Material.BLUE_BED,
            Material.RED_BED,
            Material.ORANGE_BED,
            Material.PURPLE_BED,
            Material.GREEN_BED,
            Material.GRAY_BED,
            Material.LIGHT_GRAY_BED,
            Material.LIGHT_BLUE_BED,
            Material.WHITE_BED,
            Material.MAGENTA_BED,
            Material.YELLOW_BED,
            Material.LIME_BED,
            Material.PINK_BED,
            Material.CYAN_BED,
            Material.BROWN_BED,
            Material.SIGN,
            Material.WALL_SIGN
    };
}
