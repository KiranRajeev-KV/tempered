package com.auco.tempered.registry;

import com.auco.tempered.Tempered;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    //
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(
                    Registries.CREATIVE_MODE_TAB,
                    Tempered.MODID
            );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TEMPERED_TAB =
            CREATIVE_MODE_TABS.register(
                    "tempered_tab",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.tempered"))
                            .withTabsBefore(CreativeModeTabs.BUILDING_BLOCKS)
                            .icon(() -> ModItems.TEST_ASPECT.get().getDefaultInstance())
                            .displayItems((parameters, output) -> {
                                output.accept(ModItems.TEST_ASPECT.get());
                            })
                            .build()
            );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }

    private ModCreativeTabs() {
    }
}