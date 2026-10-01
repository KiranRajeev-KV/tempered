package com.auco.tempered.presentation;

import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Names are presentation metadata, never an acquisition allowlist. */
public final class LootSourceNames {
    private static final Map<ResourceLocation, String> NAMES = Map.of(
            ResourceLocation.withDefaultNamespace("chests/simple_dungeon"), "dungeon",
            ResourceLocation.withDefaultNamespace("chests/abandoned_mineshaft"), "mineshaft",
            ResourceLocation.withDefaultNamespace("chests/desert_pyramid"), "desert_pyramid",
            ResourceLocation.withDefaultNamespace("chests/jungle_temple"), "jungle_temple");

    public static Component name(ResourceLocation table) {
        String key = NAMES.get(table);
        return key == null
                ? Component.translatable("guide.tempered.source.custom", table.toString())
                : Component.translatable("guide.tempered.source." + key);
    }

    private LootSourceNames() {}
}
