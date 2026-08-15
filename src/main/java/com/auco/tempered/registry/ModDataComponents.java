package com.auco.tempered.registry;

import com.auco.tempered.Tempered;
import com.mojang.serialization.Codec;

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

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TEST_LEVEL =
            DATA_COMPONENTS.registerComponentType(
                    "test_level",
                    builder -> builder.persistent(Codec.INT)
            );

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }

    private ModDataComponents() {
    }
}