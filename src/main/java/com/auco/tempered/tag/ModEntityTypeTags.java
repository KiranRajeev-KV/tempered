package com.auco.tempered.tag;

import com.auco.tempered.Tempered;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

/** Entity-type tags that let datapacks configure Tempered's combat rules. */
public final class ModEntityTypeTags {

    /** Hostile entity types in this tag cannot be executed or grant progress. */
    public static final TagKey<EntityType<?>> EXECUTIONER_IMMUNE = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(Tempered.MODID, "executioner_immune")
    );

    private ModEntityTypeTags() {
    }
}
