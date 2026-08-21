package com.auco.tempered.registry;

import com.auco.tempered.Tempered;
import com.auco.tempered.equipment.attribute.reinforced.ReinforcedData;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {

    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(
                    Registries.DATA_COMPONENT_TYPE,
                    Tempered.MODID
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ReinforcedData>> REINFORCED_DATA =
            DATA_COMPONENTS.registerComponentType(
                    "reinforced_data",
                    builder -> builder.persistent(ReinforcedData.CODEC)
            );

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }

    private ModDataComponents() {
    }
}
