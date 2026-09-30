package com.auco.tempered.loot;

import com.auco.tempered.config.GameplaySettings;
import com.auco.tempered.config.acquisition.AspectLootSettings;
import com.auco.tempered.registry.ModItems;
import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;

/** A modifier's identity selects both its item and its config, preventing mismatched rewards. */
public enum AspectLootType implements StringRepresentable {
    REINFORCED("reinforced"), SWIFT("swift");

    public static final Codec<AspectLootType> CODEC = StringRepresentable.fromEnum(AspectLootType::values);
    private final String name;

    AspectLootType(String name) { this.name = name; }

    @Override
    public String getSerializedName() { return name; }

    public Item item() {
        return switch (this) {
            case REINFORCED -> ModItems.REINFORCED_ASPECT.get();
            case SWIFT -> ModItems.SWIFT_ASPECT.get();
        };
    }

    public boolean mechanicEnabled(GameplaySettings settings) {
        return switch (this) {
            case REINFORCED -> settings.reinforced().enabled();
            case SWIFT -> settings.swift().enabled();
        };
    }

    public AspectLootSettings settings(GameplaySettings settings) {
        return switch (this) {
            case REINFORCED -> settings.acquisition().reinforced();
            case SWIFT -> settings.acquisition().swift();
        };
    }
}
