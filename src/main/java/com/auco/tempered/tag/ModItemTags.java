package com.auco.tempered.tag;

import com.auco.tempered.Tempered;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModItemTags {

    public static final TagKey<Item> REINFORCEABLE =
            TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(
                            Tempered.MODID,
                            "reinforceable"
                    )
            );

    private ModItemTags() {
    }
}