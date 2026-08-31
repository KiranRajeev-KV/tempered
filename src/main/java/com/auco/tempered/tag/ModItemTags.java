package com.auco.tempered.tag;

import com.auco.tempered.Tempered;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/** Item tags that let datapacks configure Tempered's gameplay rules. */
public final class ModItemTags {

    /** Weapons that can earn and use the Executioner affix. */
    public static final TagKey<Item> EXECUTIONER_APPLICABLE = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(Tempered.MODID, "executioner_applicable")
    );

    /**
     * Declares which items can receive Swift. Its default JSON definition
     * includes vanilla mining-tool tags, while datapacks can extend it.
     */
    public static final TagKey<Item> SWIFT_APPLICABLE = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(Tempered.MODID, "swift_applicable")
    );

    private ModItemTags() {
    }
}
