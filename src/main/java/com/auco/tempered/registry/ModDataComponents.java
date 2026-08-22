package com.auco.tempered.registry;

import com.auco.tempered.Tempered;
import com.auco.tempered.equipment.attribute.reinforced.ReinforcedData;
import com.auco.tempered.equipment.attribute.swift.SwiftData;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers custom values that can be stored directly on an {@code ItemStack}.
 *
 * <p>Data components replace ad-hoc NBT for new Minecraft versions. The
 * component type is the typed key; {@code ReinforcedData} is its value.</p>
 */
public final class ModDataComponents {

    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(
                    Registries.DATA_COMPONENT_TYPE,
                    Tempered.MODID
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ReinforcedData>> REINFORCED_DATA =
            DATA_COMPONENTS.registerComponentType(
                    "reinforced_data",
                    // persistent makes the Codec responsible for saving the
                    // value and also provides the default network codec.
                    builder -> builder.persistent(ReinforcedData.CODEC)
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SwiftData>> SWIFT_DATA =
            DATA_COMPONENTS.registerComponentType(
                    "swift_data",
                    builder -> builder.persistent(SwiftData.CODEC)
            );

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }

    private ModDataComponents() {
    }
}
