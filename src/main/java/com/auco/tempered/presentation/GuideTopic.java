package com.auco.tempered.presentation;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import com.auco.tempered.Tempered;

/** Stable topic identities shared by tooltips, commands and resource-pack pages. */
public enum GuideTopic {
    OVERVIEW("overview"), SMITHING("smithing"), REINFORCED("reinforced"),
    SWIFT("swift"), EXECUTIONER("executioner"), LOOT("loot");

    private final String id;

    GuideTopic(String id) { this.id = id; }

    public String id() { return id; }
    public Component title() { return Component.translatable("guide.tempered.topic." + id); }
    public ResourceLocation pageId() {
        return ResourceLocation.fromNamespaceAndPath(Tempered.MODID, this == OVERVIEW ? "index.md" : id + ".md");
    }
}
