package com.auco.tempered.registry;

import com.auco.tempered.Tempered;

import net.minecraft.world.item.Item;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Tempered.MODID);

    public static final DeferredItem<Item> TEST_ASPECT =
            ITEMS.registerSimpleItem(
                    "test_aspect",
                    new Item.Properties()
            );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    private ModItems() {
    }
}