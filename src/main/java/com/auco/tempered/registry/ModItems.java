package com.auco.tempered.registry;

import com.auco.tempered.Tempered;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Holds all item registry entries owned by Tempered. */
public final class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Tempered.MODID);

    public static final DeferredItem<Item> REINFORCED_ASPECT =
            ITEMS.register(
                    "reinforced_aspect",
                    () -> new Item(
                            new Item.Properties()
                                    // Aspects are consumables, but keeping a
                                    // small stack size makes inventory use less tedious.
                                    .stacksTo(16)
                    )
            );

    public static final DeferredItem<Item> SWIFT_ASPECT =
            ITEMS.register(
                    "swift_aspect",
                    () -> new Item(
                            new Item.Properties().stacksTo(16)
                    )
            );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    private ModItems() {
    }
}
