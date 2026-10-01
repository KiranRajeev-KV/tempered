package com.auco.tempered.presentation;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import com.auco.tempered.Tempered;

/** Stable topic identities shared by tooltips, commands and resource-pack pages. */
public enum GuideTopic {
    OVERVIEW("overview"), SMITHING("smithing"), REINFORCED("reinforced", true),
    SWIFT("swift", true), EXECUTIONER("executioner", true), LOOT("loot", true),
    MAINTENANCE("maintenance", true), CONFIGURATION("configuration");

    private final String id;
    private final boolean hasSettings;

    GuideTopic(String id) { this(id, false); }

    GuideTopic(String id, boolean hasSettings) {
        this.id = id;
        this.hasSettings = hasSettings;
    }

    public String id() { return id; }
    public boolean hasSettings() { return hasSettings; }
    public Component title() { return Component.translatable("guide.tempered.topic." + id); }
    public ResourceLocation pageId() {
        return ResourceLocation.fromNamespaceAndPath(Tempered.MODID, this == OVERVIEW ? "index.md" : id + ".md");
    }
}
