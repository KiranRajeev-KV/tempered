package com.auco.tempered.config.acquisition;

import java.util.Set;
import net.minecraft.resources.ResourceLocation;

/** Validated, parsed source IDs; acquisition never reads mutable config values. */
public record AspectLootSettings(boolean enabled, double chance, int minCount, int maxCount,
                                 Set<ResourceLocation> lootTables) {
    public static final int MAX_COUNT = 16;
    public static final int MAX_SOURCES = 256;
    public static final AspectLootSettings DEFAULT = new AspectLootSettings(true, 0.15, 1, 1, Set.of(
            ResourceLocation.withDefaultNamespace("chests/simple_dungeon"),
            ResourceLocation.withDefaultNamespace("chests/abandoned_mineshaft"),
            ResourceLocation.withDefaultNamespace("chests/desert_pyramid"),
            ResourceLocation.withDefaultNamespace("chests/jungle_temple")));

    public AspectLootSettings {
        if (!Double.isFinite(chance) || chance < 0 || chance > 1) {
            throw new IllegalArgumentException("chance must be between 0 and 1");
        }
        if (minCount < 1 || maxCount > MAX_COUNT || minCount > maxCount) {
            throw new IllegalArgumentException("counts must satisfy 1 <= min_count <= max_count <= " + MAX_COUNT);
        }
        if (lootTables.size() > MAX_SOURCES) {
            throw new IllegalArgumentException("too many loot table IDs (maximum " + MAX_SOURCES + ")");
        }
        lootTables = Set.copyOf(lootTables);
    }
}
