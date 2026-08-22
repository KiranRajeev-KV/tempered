package com.auco.tempered.registry;

import com.auco.tempered.Tempered;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registers Tempered's Creative inventory tab. */
public final class ModCreativeTabs {
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
                            .icon(() -> ModItems.REINFORCED_ASPECT.get().getDefaultInstance())
                            .displayItems((parameters, output) -> {
                                // A registered item does not automatically
                                // appear in Creative mode; add it explicitly.
                                output.accept(ModItems.REINFORCED_ASPECT.get());
                                output.accept(ModItems.SWIFT_ASPECT.get());
                            })
                            .build()
            );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }

    private ModCreativeTabs() {
    }
}
