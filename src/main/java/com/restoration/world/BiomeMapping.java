package com.restoration.world;

import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.Map;

public class BiomeMapping {
    private static final Map<Integer, Identifier> BEDROCK_TO_JAVA = new HashMap<>();

    static {
        map(0, "ocean");
        map(1, "plains");
        map(2, "desert");
        map(3, "windswept_hills");
        map(4, "forest");
        map(5, "taiga");
        map(6, "swamp");
        map(7, "river");
        map(8, "nether_wastes");
        map(9, "the_end");
        map(10, "frozen_ocean");
        map(11, "frozen_river");
        map(12, "snowy_plains");
        map(14, "mushroom_fields");
        map(16, "beach");
        map(21, "jungle");
        map(27, "birch_forest");
        map(29, "dark_forest");
        map(35, "savanna");
        map(37, "badlands");

        // Незер
        map(178, "soul_sand_valley");
        map(179, "crimson_forest");
        map(180, "warped_forest");
        map(181, "basalt_deltas");

        // Горы и новые биомы
        map(182, "dripstone_caves");
        map(183, "lush_caves");
        map(184, "meadow");
        map(185, "grove");
        map(186, "snowy_slopes");
        map(187, "jagged_peaks");
        map(188, "frozen_peaks");
        map(189, "stony_peaks");
        map(190, "deep_dark");
        map(191, "mangrove_swamp");
        map(192, "cherry_grove");
    }

    private static void map(int id, String name) {
        BEDROCK_TO_JAVA.put(id, Identifier.of("minecraft", name));
    }

    public static Identifier getJavaIdentifier(int id) {
        return BEDROCK_TO_JAVA.getOrDefault(id, Identifier.of("minecraft", "plains"));
    }
}
