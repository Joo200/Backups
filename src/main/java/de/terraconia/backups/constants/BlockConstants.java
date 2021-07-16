package de.terraconia.backups.constants;

import org.bukkit.Material;

public class BlockConstants {
    public static final Material[] defaultFreeBlockTypes = {
            Material.AIR,
            Material.DIRT,
            Material.GRASS,
            Material.GRASS_BLOCK,
            Material.COARSE_DIRT,
            Material.FARMLAND,
            Material.DIRT_PATH,
            Material.DEEPSLATE,
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
            Material.OAK_SIGN,
            Material.ACACIA_SIGN,
            Material.BIRCH_SIGN,
            Material.DARK_OAK_SIGN,
            Material.JUNGLE_SIGN,
            Material.SPRUCE_SIGN,
            Material.OAK_WALL_SIGN,
            Material.ACACIA_WALL_SIGN,
            Material.BIRCH_WALL_SIGN,
            Material.DARK_OAK_WALL_SIGN,
            Material.JUNGLE_WALL_SIGN,
            Material.SPRUCE_WALL_SIGN,
    };
}
